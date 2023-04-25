package cn.ios.casegen.expr.invoke;

import cn.ios.casegen.config.GlobalCons;
import cn.ios.casegen.enums.GenerationEnum;
import cn.ios.casegen.expr.JUExpr;
import cn.ios.casegen.util.StringUtil;
import cn.ios.casegen.util.TypeUtil;
import cn.ios.casegen.variable.FullClassType;
import cn.ios.casegen.variable.JUVariable;
import cn.ios.casegen.variable.JUVariableFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class JUInvokeExpr implements JUExpr {

	protected SootMethod method = null;

	protected List<JUVariable> dependentJUVariableList = Lists.newArrayList();

	protected FullClassType callerFullClassType = null;

	protected JUInvokeExpr(SootMethod method, FullClassType callerFullClassType) {
		if (method == null) {
			throw new IllegalArgumentException("method is null");
		}
		if (!method.isConstructor() && callerFullClassType == null) {
			throw new IllegalArgumentException("type is null");
		}
		this.callerFullClassType = callerFullClassType;
		this.method = method;
		addDependentVariable();
	}

	private void addDependentVariable() {
		Type variableType = null;
		String variableName = "";

		if (GlobalCons.GENERIC_INFO_OF_METHOD.containsKey(method)) {
			for (FullClassType variableFullClassType : GlobalCons.GENERIC_INFO_OF_METHOD.get(method)) {
				variableType = variableFullClassType.getType();
				variableName = JUVariableFactory.getName(variableType);
				dependentJUVariableList.add(new JUVariable(variableName, variableFullClassType));
			}
		} else {

			Type callerType = callerFullClassType.getType();
			boolean rightCollection = TypeUtil.isCollectionType(callerType) &&
					method.getSubSignature().equals(GenerationEnum.SUB_SIG_ADD.getValue());
			boolean rightMap = TypeUtil.isMapType(callerType) &&
					method.getSubSignature().equals(GenerationEnum.SUB_SIG_PUT.getValue());

			if (rightCollection || rightMap) {
				List<FullClassType> genericElements = callerFullClassType.getGenericElements();
				for (FullClassType genericElement : genericElements) {
					variableType = genericElement.getType();
					variableName = JUVariableFactory.getName(variableType);
					dependentJUVariableList.add(new JUVariable(variableName, genericElement));
				}

			} else {

				List<Type> parameterTypes = Lists.newArrayList(method.getParameterTypes());
				for (Type variableType0 : parameterTypes) {
					variableName = JUVariableFactory.getName(variableType0);
					dependentJUVariableList.add(new JUVariable(variableName, new FullClassType(variableType0, "")));
				}

			}
		}
	}

	@Override
	public List<JUVariable> getDependentVariableList() {
		return dependentJUVariableList;
	}

	public SootMethod getMethod() {
		return method;
	}

	@Override
	public int getArgCount() {
		return dependentJUVariableList.size();
	}

	@Override
	public JUVariable getCaller() {
		return null;
	}

	@Override
	public Set<SootClass> getExceptions() {
		HashSet<SootClass> set = Sets.newHashSet();
		set.addAll(method.getExceptions());
		return set;
	}

	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(method.getName());
		int length = dependentJUVariableList == null ? 0 : dependentJUVariableList.size();
		stringBuffer.append(StringUtil.splicingParameterParentheses(dependentJUVariableList, 0, length));
		return stringBuffer.toString();
	}

	public FullClassType getType() {
		return null;
	}
}