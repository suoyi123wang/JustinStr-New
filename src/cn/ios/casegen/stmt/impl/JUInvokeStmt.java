package cn.ios.casegen.stmt.impl;

import cn.ios.casegen.constraint.DTO.PossibleParamValueDTO;
import cn.ios.casegen.expr.invoke.JUInvokeExpr;
import cn.ios.casegen.enums.GenerationEnum;
import cn.ios.casegen.expr.JUExprFactory;
import cn.ios.casegen.stmt.JUStmt;
import cn.ios.casegen.stmt.JUStmtFactory;
import cn.ios.casegen.util.StringUtil;
import cn.ios.casegen.variable.FullClassType;
import cn.ios.casegen.variable.JUVariable;
import soot.SootClass;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JUInvokeStmt extends JUStmt {

	private SootMethod method = null;

	private JUInvokeExpr invokeExpr = null;

	/**
	 * generate invoke statement for method
	 */
	public JUInvokeStmt(Map<Integer, PossibleParamValueDTO> possibleValues, SootMethod method,
						JUVariable caller, FullClassType fullClassType) {
		if (method == null) {
			throw new IllegalArgumentException("method is null");
		}
		if (fullClassType == null) {
			throw new IllegalArgumentException("classGenericType is null");
		}
		this.method = method;
		this.invokeExpr = JUExprFactory.createInvokeExpr(method, caller, fullClassType);
		createDependentStmts(possibleValues);
	}

	private void createDependentStmts(Map<Integer, PossibleParamValueDTO> onePossibleValue) {
		List<JUVariable> JUVariableList = invokeExpr.getDependentVariableList();
		for (int i = 0; i < JUVariableList.size(); ++i){
			JUDefinitionStmt stmt;
			PossibleParamValueDTO possibleParamValueDTO = null;
			if (onePossibleValue != null && !onePossibleValue.isEmpty() && onePossibleValue.containsKey(i)){
				possibleParamValueDTO = onePossibleValue.get(i);
			}
			stmt = JUStmtFactory.createDefinitionStmt(possibleParamValueDTO, JUVariableList.get(i), this);
			previousStmts.add(stmt);
		}
	}

	@Override
	public String toString() {
		return invokeExpr.toString() + ";";
	}

	private List<String> getParametersDotClass() {
		List<String> list = new ArrayList<String>();
		list.add("\"" + method.getName() + "\"");
		List<JUVariable> parameters = invokeExpr.getDependentVariableList();
		if (parameters != null) {
			for (JUVariable varable : parameters) {
				list.add(varable.getVariableType().toString().replace("$", ".") + ".class");
			}
		}
		return list;
	}

	@Override
	public Set<SootClass> getExceptions() {
		return  invokeExpr.getExceptions();
	}

	protected void processPrivateMethod(StringBuffer stringBuffer) {
		List<JUVariable> parameters = invokeExpr.getDependentVariableList();
		Object caller = invokeExpr.getCaller();
		String methodName = method.getName();
		stringBuffer.append("Method ");
		stringBuffer.append(methodName);
		stringBuffer.append(" = ");
		stringBuffer.append(caller);
		stringBuffer.append(".getClass().getDeclaredMethod");
		List<String> getParametersDotClass = getParametersDotClass();
		stringBuffer.append(
				StringUtil.splicingParameterParentheses(getParametersDotClass, 0, getParametersDotClass.size()));
		stringBuffer.append(";").append(GenerationEnum.ONE_NEW_LINE.getValue());
		stringBuffer.append(GenerationEnum.SIX_SPACE.getValue());
		stringBuffer.append(methodName);
		stringBuffer.append(".setAccessible(true);").
				append(GenerationEnum.ONE_NEW_LINE.getValue());
		stringBuffer.append(GenerationEnum.SIX_SPACE.getValue());
		stringBuffer.append(methodName);
		stringBuffer.append(".invoke");
		int length = parameters == null ? 0 : parameters.size();
		stringBuffer.append(StringUtil.splicingParameterParentheses(parameters, 0, length));

	}
	@Override
	public boolean containsJUInvokeExpr() {
		return true;
	}

	@Override
	public JUInvokeExpr getJUInvokeExpr() {
		return invokeExpr;
	}

}
