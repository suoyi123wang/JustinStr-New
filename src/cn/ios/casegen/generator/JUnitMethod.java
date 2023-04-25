package cn.ios.casegen.generator;

import cn.ios.casegen.constraint.DTO.PossibleParamValueDTO;
import cn.ios.casegen.enums.GenerationEnum;
import cn.ios.casegen.stmt.JUStmt;
import cn.ios.casegen.stmt.JUStmtFactory;
import cn.ios.casegen.stmt.impl.JUInvokeStmt;
import cn.ios.casegen.util.StringUtil;
import cn.ios.casegen.variable.FullClassType;
import cn.ios.casegen.variable.JUVariable;
import com.google.common.collect.Sets;
import soot.SootClass;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JUnitMethod {
	private SootMethod method = null;
	private int methodIndex = 0;
	private int internalIndex = 0;
	private Set<String> exceptions = null;
	private JUStmt end = null;

	public JUnitMethod(Map<Integer, PossibleParamValueDTO> onePossibleValue, SootMethod method, int methodIndex, int internalIndex) {
		this.method = method;
		this.methodIndex = ++methodIndex;
		this.internalIndex = ++internalIndex;
		this.exceptions = setExceptions(method);
		this.end = generateInvokeStmt(onePossibleValue);
	}

	public SootMethod getMethod(){
		return method;
	}

	public String getTestMethodName(){
		return "test_" +method.getName() + "_" + methodIndex + "_" +internalIndex;
	}

	private JUStmt generateInvokeStmt(Map<Integer, PossibleParamValueDTO> onePossibleValue) {
		// TODO
		// 如果方法所在的类也有泛型呢？
		JUInvokeStmt invokeStmt = JUStmtFactory.createInvokeStmt(onePossibleValue, method, null,
				new FullClassType(method.getDeclaringClass().getType(), ""));

		if (invokeStmt.containsJUInvokeExpr() && invokeStmt.getJUInvokeExpr().getCaller() != null) {
			JUVariable caller = invokeStmt.getJUInvokeExpr().getCaller();
			JUStmt definition = JUStmtFactory.createDefinitionStmt(null, caller, invokeStmt);
			invokeStmt.getPreviousStmts().add(definition);
		}
		return invokeStmt;
	}

	private String getStmtString(JUStmt stmt) {
		StringBuffer result = new StringBuffer();
		if (stmt.getPreviousStmts() != null) {
			for (JUStmt p : stmt.getPreviousStmts()) {
				result.append(getStmtString(p));
			}
		}
		result.append(GenerationEnum.SIX_SPACE.getValue());
		String stmtString = stmt.toString();
		result.append(stmtString);
		result.append(GenerationEnum.ONE_NEW_LINE.getValue());
		if (stmt.getSuccStmts() != null) {
			for (JUStmt p : stmt.getSuccStmts()) {
				result.append(getStmtString(p));
			}
		}
		return result.toString();
	}

	private Set<String> setExceptions(SootMethod sootMethod){
		Set<String> exceptions = Sets.newHashSet();
		List<SootClass> exceptionList = sootMethod.getExceptions();
		if (exceptionList == null || exceptionList.isEmpty()) return exceptions;
		for (SootClass exceptionClass : exceptionList) {
			exceptions.add(exceptionClass.getName());
		}
		return exceptions;
	}

	@Override
	public String toString() {
		StringBuilder stringBuffer = new StringBuilder();
		String stmtString = getStmtString(end);

		stringBuffer.append(GenerationEnum.TWO_SPACE.getValue()).
				append(GenerationEnum.TEST_ANNOTATION.getValue()).
				append(GenerationEnum.ONE_NEW_LINE.getValue()).
				append(GenerationEnum.TWO_SPACE.getValue()).
				append(GenerationEnum.PUBLIC_TEST.getValue()).
				append(method.getName()).
				append(GenerationEnum.UNDERSCORE.getValue()).
				append(methodIndex).
				append(GenerationEnum.UNDERSCORE.getValue()).
				append(internalIndex).
				append(GenerationEnum.LEFT_PARENTHESES.getValue()).
				append(GenerationEnum.RIGHT_PARENTHESES.getValue()).
				append(GenerationEnum.ONE_SPACE.getValue());

		if (exceptions != null && !exceptions.isEmpty()) {
			stringBuffer.append(GenerationEnum.THROWS.getValue()).
					append(GenerationEnum.ONE_SPACE.getValue()).
					append(StringUtil.splicingStrList(new ArrayList<>(exceptions)));
		}

		stringBuffer.append(GenerationEnum.LEFT_CURLY_BRACE.getValue()).
				append(GenerationEnum.TWO_NEW_LINES.getValue()).
				append(stmtString).
				append(GenerationEnum.ONE_NEW_LINE.getValue()).
				append(GenerationEnum.TWO_SPACE.getValue()).
				append(GenerationEnum.RIGHT_CURLY_BRACE.getValue()).
				append(GenerationEnum.TWO_NEW_LINES.getValue());

		return stringBuffer.toString();
	}
}
