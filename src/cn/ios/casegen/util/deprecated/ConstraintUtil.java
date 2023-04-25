//package cn.ios.junit.util;
//
//import cn.ios.junit.config.GlobalCons;
//import cn.ios.junit.constraint.VO.ParamConstraintVO;
//import cn.ios.junit.enums.GenerationEnum;
//import com.google.common.collect.Lists;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.regex.Pattern;
//
///**
// * @description: TODO
// * @author: wangmiaomiao
// * @create: 2021-10-08 15:05
// **/
//
//public class ConstraintUtil<T> {
//	// for List<List<>>,do Cartesian Product
//	public void descartes(List<List<T>> dimensionValue, List<List<T>> result, int layer, List<T> currentList) {
//
//		if (layer < dimensionValue.size() - 1) {
//			if (dimensionValue.get(layer).size() == 0) {
//				descartes(dimensionValue, result, layer + 1, currentList);
//			} else {
//				for (int i = 0; i < dimensionValue.get(layer).size(); i++) {
//					List<T> list = new ArrayList<T>(currentList);
//					list.add(dimensionValue.get(layer).get(i));
//					descartes(dimensionValue, result, layer + 1, list);
//				}
//			}
//		} else if (layer == dimensionValue.size() - 1) {
//			if (dimensionValue.get(layer).size() == 0) {
//				result.add(currentList);
//			} else {
//				for (int i = 0; i < dimensionValue.get(layer).size(); i++) {
//					List<T> list = new ArrayList<T>(currentList);
//					list.add(dimensionValue.get(layer).get(i));
//					result.add(list);
//				}
//			}
//		}
//	}
//
//	/**
//	 * 多个参数,每个参数有多种取值, 相互组合的情况
//	 * 组合测试，会有爆炸的问题，目前还没有解决， 先不采用笛卡尔积的情况
//	 * @param className       反射得到的类名
//	 * @param methodSignature 反射得到的方法签名
//	 * @return
//	 */
//	public static List<Map<Integer, String>> combineRegex(String className, String methodSignature) {
//		// 通过反射获取的methodSignature 会含有throws信息,在这里去掉
//		methodSignature = methodSignature.split("throws")[0].trim();
//
//		Map<String, Map<String, Map<Integer, ParamConstraintVO>>> finalConstraints = GlobalCons.paramConstraintVOS;
//		if (finalConstraints == null || finalConstraints.isEmpty()) {
//			return null;
//		}
//
//		List<Map<Integer, String>> constraints = Lists.newArrayList();
////		if (finalConstraints.containsKey(className)) {
////			if (finalConstraints.get(className).containsKey(methodSignature)) {
////				Map<Integer, ParamConstraintVO> constraintsInMethod = finalConstraints.get(className).get(methodSignature);
////				List<Integer> keyList = new ArrayList<>(constraintsInMethod.keySet());
////				List<Set<String>> valueSetList = new ArrayList<>(constraintsInMethod.values());
////				List<List<String>> valueList = Lists.newArrayList();
////				valueSetList.forEach(sets -> valueList.add(new ArrayList<>(sets)));
////				List<List<String>> cartesianProduct = Lists.cartesianProduct(valueList);
////				for (List<String> oneResult : cartesianProduct) {
////					Map<Integer, String> collect = keyList.stream()
////							.collect(Collectors.toMap(key -> key, key -> oneResult.get(keyList.indexOf(key))));
////					constraints.add(collect);
////				}
////			}
////		}
//
//		return constraints;
//	}
//
//	public static String escapeSpecialRegexChars(String str) {
//		return Pattern.compile(GenerationEnum.SPECIAL_REGEX_CHARS.getValue()).
//				matcher(str).replaceAll("\\\\$0");
//	}
//
//	/**
//	 * 两个list笛卡尔积
//	 *
//	 * @param oldRegexArrayList
//	 * @param newRegexArrayList
//	 */
//
//}
