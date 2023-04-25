package cn.ios.casegen.expr.define.newexpr;

import cn.ios.casegen.config.GlobalCons;
import cn.ios.casegen.constraint.DTO.PossibleParamValueDTO;
import cn.ios.casegen.expr.define.JUDefineExpr;
import cn.ios.casegen.expr.define.newexpr.util.ConstructorUtil;
import cn.ios.casegen.util.ClassUtil;
import cn.ios.casegen.util.ListUtil;
import cn.ios.casegen.util.StringUtil;
import cn.ios.casegen.util.TypeUtil;
import cn.ios.casegen.variable.FullClassType;
import cn.ios.casegen.variable.JUVariable;
import cn.ios.casegen.variable.JUVariableFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JUNewExprImpl extends JUDefineExpr {
	private SootClass sootClass = null;
	private SootMethod constructorMethod = null;
	private List<JUVariable> dependentJUVariableList = Lists.newArrayList();

 	public JUNewExprImpl(FullClassType variableType, PossibleParamValueDTO possibleParamValueDTO) {
		Type type = variableType.getType();
		if (type instanceof RefType) {
			this.sootClass = ((RefType) type).getSootClass();
			this.constructorMethod = getConstructorMethod();
//			addDependentVariable();
		}
	}

	private SootMethod getConstructorMethod(){
		 SootMethod constructorMethod = null;
		 Type type = this.sootClass.getType();

		Set<SootMethod> constructorMethodOfClass = Sets.newHashSet();
		 if (!GlobalCons.CONSTRUCTOR_MAP.containsKey(sootClass)) {
			 constructorMethodOfClass = ConstructorUtil.getConstructorMethodOfClass(sootClass);
			 GlobalCons.CONSTRUCTOR_MAP.put(sootClass, constructorMethodOfClass);
		 } else {
			 constructorMethodOfClass = GlobalCons.CONSTRUCTOR_MAP.get(sootClass);
		 }

		 constructorMethod =  ListUtil.getOneRandomElement(new ArrayList<>(constructorMethodOfClass));

		 if (!TypeUtil.isMapType(type) && !TypeUtil.isCollectionType(type)) {
			 return constructorMethod;
		 }

		 while (constructorMethod.getParameterCount() != 0) {
			 constructorMethod =  ListUtil.getOneRandomElement(new ArrayList<>(constructorMethodOfClass));
		 }
		 return constructorMethod;
	}

	private void addDependentVariable() {
		// constructor method has no constraints info
		List<FullClassType> genericInfoOfMethod =
				GlobalCons.GENERIC_INFO_OF_METHOD.containsKey(this.constructorMethod)?
						GlobalCons.GENERIC_INFO_OF_METHOD.get(this.constructorMethod) : ClassUtil.getGenericInfoOfMethod(this.constructorMethod);

		for (FullClassType variableFullClassType : genericInfoOfMethod) {
			Type variableType = variableFullClassType.getType();
			String variableName = JUVariableFactory.getName(variableType);
			dependentJUVariableList.add(new JUVariable(variableName, variableFullClassType));
		}
	}

	@Override
	public String toString() {
		 try {
			 SootClass classOfConstructorMethod = this.constructorMethod.getDeclaringClass();
			 StringBuffer stringBuffer = new StringBuffer();
			 if (classOfConstructorMethod != null) {
				 stringBuffer.append("new ").append(classOfConstructorMethod.getName());
				 if (TypeUtil.isCollectionType(classOfConstructorMethod.getType()) || TypeUtil.isMapType(classOfConstructorMethod.getType())) {
					 stringBuffer.append("<>");
				 }
				 int length = dependentJUVariableList == null ? 0 : dependentJUVariableList.size();
				 stringBuffer.append(StringUtil.splicingParameterParentheses(dependentJUVariableList, 0, length));
			 }
			 return stringBuffer.toString();
		 } catch (Exception e){
			 return "";
		 }

	}

	public List<JUVariable> getDependentVariableList() {
		return this.dependentJUVariableList;
	}

}
