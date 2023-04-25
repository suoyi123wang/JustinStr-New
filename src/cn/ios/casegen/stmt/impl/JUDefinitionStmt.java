package cn.ios.casegen.stmt.impl;

import cn.ios.casegen.constraint.DTO.PossibleParamValueDTO;
import cn.ios.casegen.constraint.VO.MemberFieldVO;
import cn.ios.casegen.expr.define.JUDefineExpr;
import cn.ios.casegen.expr.invoke.JUInvokeExpr;
import cn.ios.casegen.config.GlobalCons;
import cn.ios.casegen.enums.GenerationEnum;
import cn.ios.casegen.expr.JUExpr;
import cn.ios.casegen.expr.JUExprFactory;
import cn.ios.casegen.expr.define.newexpr.JUNewExprImpl;
import cn.ios.casegen.stmt.JUStmt;
import cn.ios.casegen.stmt.JUStmtFactory;
import cn.ios.casegen.util.ClassUtil;
import cn.ios.casegen.util.TypeUtil;
import cn.ios.casegen.util.random.RandomUtil;
import cn.ios.casegen.variable.FullClassType;
import cn.ios.casegen.variable.JUVariable;
import com.google.common.collect.Maps;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class JUDefinitionStmt extends JUStmt {

	private JUVariable variable = null;
	private JUExpr defineExpr = null;


	public JUDefinitionStmt(PossibleParamValueDTO possibleParamValueDTO, JUVariable variable, JUStmt stmt) {
		this.variable = variable;
		nextStmt = stmt;
		this.defineExpr = createDefineExpr(possibleParamValueDTO);

		// for method parameters
		createDependentStmts();

		// for object, setAttribute
		addAttributeValueForObject(possibleParamValueDTO);

		// for Collection or Map
		addElement(possibleParamValueDTO);
	}


	private JUDefineExpr createDefineExpr(PossibleParamValueDTO possibleParamValueDTO) {
		FullClassType variableType = this.variable.getVariableType();
		if (variableType == null) {
			throw new IllegalArgumentException("Exception in JUDefinitionStmt.createDefineExpr");
		}
		return JUExprFactory.createDefineExpr(possibleParamValueDTO, variableType);
	}

	// for parameters
	private void createDependentStmts(){
		List<JUVariable> dependentJUVariable = this.defineExpr.getDependentVariableList();
		if (dependentJUVariable != null && !this.defineExpr.getDependentVariableList().isEmpty()) {
			for (JUVariable JUVariable1 : this.defineExpr.getDependentVariableList()) {
				// TODO null??
				JUStmt stmt = JUStmtFactory.createDefinitionStmt(null, JUVariable1, this);
				previousStmts.add(stmt);
			}
		}
	}

	private void addAttributeValueForObject(PossibleParamValueDTO possibleParamValueDTO){
		if (possibleParamValueDTO == null || possibleParamValueDTO.getPossibleFieldValueMap() ==null ||
				possibleParamValueDTO.getPossibleFieldValueMap().isEmpty()) {
			return;
		}

		Type paramType = possibleParamValueDTO.getParamType();
		if (!paramType.equals(variable.getVariableType().getType())) {
			return;
		}

		Map<String, String> possibleFieldValueMap = possibleParamValueDTO.getPossibleFieldValueMap();
		List<MemberFieldVO> memberFieldVOS = GlobalCons.MEMBER_FIELD_INFO.get(paramType.toString());

		for (Map.Entry<String, String> possibleValueMap : possibleFieldValueMap.entrySet()) {
			String fieldName = possibleValueMap.getKey();
			String fieldPossibleValue = possibleValueMap.getValue();
			MemberFieldVO memberFieldVO = memberFieldVOS.stream().
					filter(object -> object.getMemberFieldName().equals(fieldName)).
					findAny().orElse(null);
			if (memberFieldVO != null && memberFieldVO.hasPublicSetMethod()) {
				String objectClassName = paramType.toString();
				String setMethodName = memberFieldVO.getNameOfSetMethod();
				SootMethod setSootMethod = ClassUtil.findSetSootMethod(objectClassName, setMethodName);
				if (setSootMethod != null) {
					PossibleParamValueDTO setValueDTO = new PossibleParamValueDTO(0, memberFieldVO.getMemberFieldType(), fieldPossibleValue);
					Map<Integer, PossibleParamValueDTO> possibleValues = Maps.newHashMap();
					possibleValues.put(0, setValueDTO);
					JUInvokeStmt setInvokeStmt =
							JUStmtFactory.createInvokeStmt(possibleValues, setSootMethod, variable,
									variable.getVariableType());
					succStmts.add(setInvokeStmt);
				}
			}
		}
	}

	/**
	 * for collection or map, need to add element
	 * @param possibleParamValueDTO size of map Of Collection
	 */
	private void addElement(PossibleParamValueDTO possibleParamValueDTO) {
		Type variableType = variable.getVariableType().getType();
		if (!TypeUtil.isMapType(variableType) && !TypeUtil.isCollectionType(variableType)) {
			return;
		}

		List<FullClassType> genericElements = variable.getVariableType().getGenericElements();

		int arraySize = RandomUtil.nextInt(GlobalCons.ARRAY_MIN_SIZE, GlobalCons.ARRAY_MAX_SIZE);
		if (possibleParamValueDTO != null && !possibleParamValueDTO.getPossibleValue().isEmpty()) {
			int possibleSize = Integer.parseInt(possibleParamValueDTO.getPossibleValue());
			arraySize = possibleSize > GlobalCons.ARRAY_MAX_SIZE? arraySize : possibleSize;
		}
		SootClass sootClassOfVariable = ((RefType) (variableType)).getSootClass();

		SootMethod calledMethod = null;
		if (TypeUtil.isCollectionType(variableType) && genericElements != null && genericElements.size() == 1){
			calledMethod= sootClassOfVariable.getMethod(GenerationEnum.SUB_SIG_ADD.getValue());
		} else if (TypeUtil.isMapType(variableType) && genericElements != null && genericElements.size() == 2) {
			calledMethod= sootClassOfVariable.getMethod(GenerationEnum.SUB_SIG_PUT.getValue());
		}

		if (calledMethod != null) {
			for (int i = 0; i < arraySize; i++) {
				JUInvokeStmt invokeStmt = JUStmtFactory.createInvokeStmt(null, calledMethod,
						variable, variable.getVariableType());
				succStmts.add(invokeStmt);
			}
		}
	}

	@Override
	public Set<SootClass> getExceptions() {
		if (defineExpr instanceof JUNewExprImpl) {
			return defineExpr.getExceptions();
		}
		return super.getExceptions();
	}

	@Override
	public String toString() {
		Type variableType = variable.getVariableType().getType();
		List<FullClassType> genericElements = variable.getVariableType().getGenericElements();
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(variableType.toString().replace("$", "."));

		// deal genericInfo
		if (TypeUtil.isCollectionType(variableType)) {
			if (genericElements.size() == 1) {
				stringBuffer.append(GenerationEnum.LEFT_ANGLE_BRACKET.getValue()).
						append(genericElements.get(0).getType().toString()).
						append(GenerationEnum.RIGHT_ANGLE_BRACKET.getValue());
			}
		} else if (TypeUtil.isMapType(variableType)) {
			if (genericElements.size() == 2) {
				stringBuffer.append(GenerationEnum.LEFT_ANGLE_BRACKET.getValue()).
						append(genericElements.get(0).getType().toString()).
						append(GenerationEnum.COMMA.getValue()).
						append(genericElements.get(1).getType().toString()).
						append(GenerationEnum.RIGHT_ANGLE_BRACKET.getValue());
			}
		}

		stringBuffer.append(GenerationEnum.ONE_SPACE.getValue()).
				append(variable).
				append(GenerationEnum.ONE_SPACE.getValue()).
				append(GenerationEnum.EQUAL.getValue()).
				append(GenerationEnum.ONE_SPACE.getValue());

		stringBuffer.append(defineExpr);
		stringBuffer.append(GenerationEnum.SEMICOLON.getValue());
		return stringBuffer.toString();
	}

	@Override
	public boolean containsJUInvokeExpr() {
		return false;
	}

	@Override
	public JUInvokeExpr getJUInvokeExpr() {
		return null;
	}
}