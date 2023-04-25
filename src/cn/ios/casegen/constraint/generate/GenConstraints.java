package cn.ios.casegen.constraint.generate;

import cn.ios.casegen.constraint.DTO.ParamConstraintDTO;
import cn.ios.casegen.config.GlobalCons;
import cn.ios.casegen.constraint.DTO.MethodCallDTO;
import cn.ios.casegen.constraint.VO.MemberFieldVO;
import cn.ios.casegen.constraint.VO.ParamConstraintVO;
import cn.ios.casegen.enums.GenerationEnum;
import cn.ios.casegen.enums.TraceTypeEnum;
import cn.ios.casegen.generator.JUnitClass;
import cn.ios.casegen.output.JUnitWriter;
import cn.ios.casegen.util.ClassUtil;
import cn.ios.casegen.util.ListUtil;
import cn.ios.casegen.util.TypeUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import soot.*;
import soot.jimple.BinopExpr;
import soot.jimple.Constant;
import soot.jimple.IfStmt;
import soot.jimple.ReturnStmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description: 单纯从soot里面的每个if找到限制条件，不考虑if嵌套。
 * @author: wangmiaomiao
 * @create: 2021-10-10 14:46
 **/
public class GenConstraints {

	public static Map<String, Map<String, Map<Integer, ParamConstraintVO>>> getConstraintsFromDirectIf() {
		Map<String, Map<String, Map<Integer, ParamConstraintVO>>> constraintsForAllClasses = Maps.newHashMap();

		HashSet<SootClass> applicationClasses = new HashSet<>(Scene.v().getApplicationClasses());

		for (SootClass sootClass : applicationClasses) {
			if (ClassUtil.isIgnoredClass(sootClass) ||
					(GlobalCons.pluginStart && !GlobalCons.CLASS_NAME_UNDER_TEST.contains(sootClass.getName()))) {
				continue;
			} else {
				GlobalCons.TEST_CLASS_NUM ++;
			}

			Thread thread = new Thread(() -> {
				Map<String, Map<Integer, ParamConstraintVO>> constraintsForEachClass = Maps.newHashMap();
				HashSet<SootMethod> sootMethods = new HashSet<SootMethod>(sootClass.getMethods());
				for (SootMethod sootMethod : sootMethods) {
					if (ClassUtil.isIgnoredMethod(sootMethod) || !sootMethod.hasActiveBody()) {
						continue;
					}

//				List<FullClassType> genericInfoOfMethod = ClassUtil.getGenericInfoOfMethod(sootMethod);
//				if (!genericInfoOfMethod.isEmpty()) {
//					GlobalCons.GENERIC_INFO_OF_METHOD.put(sootMethod,genericInfoOfMethod);
//				}

					Body body = sootMethod.getActiveBody();

					Map<Integer, ParamConstraintVO> constraintsInMethod = getConstraintsInMethod(new BriefUnitGraph(body), body);
					if (!constraintsInMethod.isEmpty()) {
						constraintsForEachClass.put(sootMethod.getSignature(), constraintsInMethod);
					}
				}
				if (!constraintsForEachClass.isEmpty()) {
					constraintsForAllClasses.put(sootClass.getName(), constraintsForEachClass);
				}
			});

			thread.start();
			try {
				long thread_time =(long) 60 * 1000;
				thread.join(thread_time);
			} catch (InterruptedException e){
				// ...
			}
			// 如果线程仍在执行，就中断它
			if (thread.isAlive()) {
				thread.interrupt();
			}

		}
		return constraintsForAllClasses;
	}

	// 获取一个方法内的所有if的限制条件
	private static Map<Integer, ParamConstraintVO> getConstraintsInMethod(UnitGraph unitGraph, Body body) {
		Map<Integer, ParamConstraintVO> savedResult = Maps.newHashMap();
		for (Unit unit : body.getUnits()) {
			ParamConstraintDTO paramConstraintInfo = new ParamConstraintDTO();
			if (unit instanceof IfStmt) {
				IfStmt ifStmt = (IfStmt) unit;
				Value conditionExpr = ifStmt.getCondition();
				if (conditionExpr instanceof BinopExpr) {
					BinopExpr binopExpr = (BinopExpr) conditionExpr;
					paramConstraintInfo.setOperator(binopExpr.getSymbol());
					Value leftValue = binopExpr.getOp1();
					Value rightValue = binopExpr.getOp2();

					// constant is compareValue
					if (rightValue instanceof Constant) {
						paramConstraintInfo.setCompareValue(rightValue.toString());
						if (leftValue instanceof Local) {
							Trace.traceLocal(unitGraph, unit, (Local) leftValue, paramConstraintInfo, Maps.newHashMap(),
									TraceTypeEnum.IfStmt, -1);
						}
					} else if (leftValue instanceof Constant) {
						paramConstraintInfo.setCompareValue(leftValue.toString());
						if (rightValue instanceof Local) {
							Trace.traceLocal(unitGraph, unit, (Local) rightValue, paramConstraintInfo, Maps.newHashMap(),
									TraceTypeEnum.IfStmt, -1);
						}
					}
				}
			} else if (unit instanceof ReturnStmt) {
				// for : return str.contains("s")
				Value returnValue = ((ReturnStmt) unit).getOp();
				if (returnValue instanceof Local) {
					Trace.traceLocal(unitGraph, unit, (Local) returnValue, paramConstraintInfo, Maps.newHashMap(), TraceTypeEnum.ReturnStmt, -1);
				}
			}

			if (paramConstraintInfo.getParamIndex() >= 0) {
				dealMethodCallList(paramConstraintInfo);
				// the methodCallList has already reversed
				addToParamConstraintVO(savedResult, paramConstraintInfo);
			}
		}
		return savedResult;
	}

	private static void  addToParamConstraintVO(Map<Integer, ParamConstraintVO> paramConstraintVOMap,
												ParamConstraintDTO paramConstraintInfo){

		int paramIndex = paramConstraintInfo.getParamIndex();
		Type paramType = paramConstraintInfo.getParamType();
		String fieldName = paramConstraintInfo.getFieldName();
		if (paramIndex < 0 ) return;

		// generate regex or constraint value
		Set<String> regexSet = GenRegex.genRegexByParamConstraintInfo(paramConstraintInfo);

		ParamConstraintVO paramConstraintVO = paramConstraintVOMap.containsKey(paramIndex) ?
				paramConstraintVOMap.get(paramIndex) :
				new ParamConstraintVO(paramIndex,paramType);

		if (TypeUtil.isPrimType(paramType) || TypeUtil.isStringType(paramType)
				|| TypeUtil.isCollectionType(paramType) || TypeUtil.isArrayType(paramType)) {
			Set<String> possibleValuesForSimpleType = paramConstraintVO.getPossibleValuesForSimpleType();
			possibleValuesForSimpleType.addAll(regexSet);
			paramConstraintVO.setPossibleValuesForSimpleType(possibleValuesForSimpleType);
		} else {
			// need FieldName and regexSet;
			// if paramConstraintInfo.isHasMemberField(), the two values have been already set
			if (!paramConstraintInfo.isHasMemberField()) {
				// for: int age = student.getAge();

				// remove getMethod in methodCallList
				List<MethodCallDTO> methodCallList = paramConstraintInfo.getMethodCallList();
				if (methodCallList.size() > 0) {
					MethodCallDTO getAttributeMethod = ListUtil.getFirstElement(methodCallList);
					ListUtil.removeFirstElement(methodCallList);
					paramConstraintInfo.setMethodCallList(methodCallList);

					regexSet = GenRegex.genRegexByParamConstraintInfo(paramConstraintInfo);

					String getAttributeMethodName = getAttributeMethod.getMethodName();
					List<MemberFieldVO> memberFieldVOS = GlobalCons.MEMBER_FIELD_INFO.get(paramType.toString());
					if (memberFieldVOS !=null && !memberFieldVOS.isEmpty()) {
						MemberFieldVO memberFieldVO = memberFieldVOS.stream().
								filter(object -> object.getNameOfGetMethod() != null && object.getNameOfGetMethod().equals(getAttributeMethodName)).
								findAny().orElse(null);
						if (memberFieldVO == null) return;
						fieldName = memberFieldVO.getMemberFieldName();
					}
				}
			}

			Map<String, Set<String>> possibleValuesForObject = paramConstraintVO.getPossibleValuesForObject();
			Set<String> regexes = possibleValuesForObject.containsKey(fieldName)?
					possibleValuesForObject.get(fieldName) : Sets.newHashSet();
			regexes.addAll(regexSet);

			if (!fieldName.isEmpty()) {
				possibleValuesForObject.put(fieldName,regexes);
			}

			// deal compareValue is "null", for: if (ob == null)
			if (paramConstraintInfo.getCompareValue().equals(GenerationEnum.NULL_OBJECT.getValue())) {
				possibleValuesForObject.put(GenerationEnum.NULL_OBJECT.getValue(),
						Sets.newHashSet(GenerationEnum.NULL_OBJECT.getValue()));
			}

			if (!possibleValuesForObject.isEmpty()) {
				paramConstraintVO.setPossibleValuesForObject(possibleValuesForObject);
			}
		}

		// set numsOfPossibleValues
		paramConstraintVO.setNumsOfPossibleValues();

		if (paramConstraintVO.getNumsOfPossibleValues() > 0) {
			paramConstraintVOMap.put(paramIndex, paramConstraintVO);
		}
	}

	/**
	 * 将最后空的MethodCall去掉，同时，如果有成员变量的约束条件，第一个methodCall是属性名称
	 * @param paramConstraintInfo
	 */
	private static void dealMethodCallList(ParamConstraintDTO paramConstraintInfo){
		// delete the last MethodCall if methodName is "" ;;;; reverse MethodCall
		List<MethodCallDTO> methodCallList = paramConstraintInfo.getMethodCallList();
		MethodCallDTO lastElement = ListUtil.getLastElement(methodCallList);
		if (lastElement != null && lastElement.getMethodName().isEmpty()) {
			ListUtil.removeLastElement(methodCallList);
		}
		paramConstraintInfo.setMethodCallList(ListUtil.reverse(methodCallList));

		/**
		 * 如果记录了成员变量(记录在methodName中)，将成员变量的名称赋值给attributeName
		 */
		Type paramType = paramConstraintInfo.getParamType();
		// isHasMemberField is true only when call field by dot,such as student.name;;
		// isHasMemberField is false when call as: student.getName()
		if (TypeUtil.isObjectType(paramType) && !paramConstraintInfo.isHasMemberField()) {
			dealCallFieldByGet(paramConstraintInfo);
		}
	}

	/**
	 * Determine whether the last methodCall is a get method
	 */
	private static void dealCallFieldByGet(ParamConstraintDTO paramConstraintInfo) {
		Type paramType = paramConstraintInfo.getParamType();
		List<MethodCallDTO> methodCallList = paramConstraintInfo.getMethodCallList();
		MethodCallDTO firstMethodCall = ListUtil.getFirstElement(methodCallList);

		if (firstMethodCall == null) return;

		Map<String, List<MemberFieldVO>> memberVariableInfo = GlobalCons.MEMBER_FIELD_INFO;

		if (memberVariableInfo.containsKey(paramType.toString())) {
			for (MemberFieldVO memberVO : memberVariableInfo.get(paramType.toString())) {
				if (memberVO.hasPublicGetMethod() && memberVO.getNameOfGetMethod().equals(firstMethodCall.getMethodName())){
					paramConstraintInfo.setFieldName(memberVO.getMemberFieldName());
					paramConstraintInfo.setFieldType(memberVO.getMemberFieldType());
					paramConstraintInfo.setHasMemberField(true);

					ListUtil.removeFirstElement(methodCallList);
					paramConstraintInfo.setMethodCallList(methodCallList);
					return;
				}
			}
		}
	}
}