package cn.ios.casegen.zother.diff;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.ios.casegen.constraint.DTO.ParamConstraintDTO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.csvreader.CsvWriter;

public class Compare {

	public static final Set<String> JDK_LIST = new HashSet<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		{
			add("0900");
			add("0904");
			add("1000");
			add("1001");
			add("1002");
			add("1101");
			add("1102");
			add("1200");
			add("1201");
			add("1202");
			add("1300");
			add("1301");
			add("1302");
			add("1400");
			add("1401");
			add("1402");
			add("1500");
			add("1501");
			add("1502");
		}

	};

	private static int rowNum = 0;

	public static void main(String[] args) throws IOException {

		String first = args[0];
//		String first = "0900";
		CsvWriter csvWriter = new CsvWriter(first + "_Compare1208.csv", ',', Charset.forName("GBK"));
		csvWriter.writeRecord(new String[] { "No.", "In", "NotIn", "method", "paraIndex", "operator", "compareValue" });
		for (String jdk : JDK_LIST) {
			if (!jdk.equals(first)) {
				String second = jdk;
				compare(first, second, csvWriter);
			}
		}
		csvWriter.close();
	}

	private static void compare(String first, String second, CsvWriter csvWriter) throws IOException {

		String FILE_1 = "jdk" + first + "_1207.txt";

		String FILE_2 = "jdk" + second + "_1207.txt";

		Set<String> firstPublicMethods = fromFile("jdk" + first + "PublicMethods.txt");
		Map<String, Map<String, Map<Integer, Set<ParamConstraintDTO>>>> classConstraintMap1 = getResult(FILE_1);
		Map<String, Map<String, Map<Integer, Set<ParamConstraintDTO>>>> classConstraintMap2 = getResult(FILE_2);
		Set<String> classNames = new HashSet<String>(classConstraintMap1.keySet());
		classNames.addAll(classConstraintMap2.keySet());

		for (String className : classNames) {
			if (!classConstraintMap1.containsKey(className)) {
				Map<String, Map<Integer, Set<ParamConstraintDTO>>> methodsConstraintMap = classConstraintMap2
						.get(className);
				
				for (String methodName : methodsConstraintMap.keySet()) {
					if(methodName.toLowerCase().contains("internal") || methodName.toLowerCase().contains("$")) {
						continue;
					}
					if (firstPublicMethods.contains(methodName)) {
						for (Integer paraIndex : methodsConstraintMap.get(methodName).keySet()) {
							for (ParamConstraintDTO ConstraintSet : methodsConstraintMap.get(methodName)
									.get(paraIndex)) {
								csvWriter.writeRecord(new String[] { (++rowNum) + "", second, first, methodName,
										paraIndex + "", ConstraintSet.getOperator(), ConstraintSet.getCompareValue() });
							}
						}
					}
				}
			} else if (classConstraintMap2.containsKey(className)) {
				Map<String, Map<Integer, Set<ParamConstraintDTO>>> methodsConstraintMap1 = classConstraintMap1
						.get(className);
				Map<String, Map<Integer, Set<ParamConstraintDTO>>> methodsConstraintMap2 = classConstraintMap2
						.get(className);
				Set<String> methodNames = new HashSet<String>(methodsConstraintMap1.keySet());
				methodNames.addAll(methodsConstraintMap2.keySet());
				for (String methodName : methodNames) {
					if(methodName.toLowerCase().contains("internal") || methodName.toLowerCase().contains("$")) {
						continue;
					}
					if (!methodsConstraintMap1.containsKey(methodName)) {
						if (firstPublicMethods.contains(methodName)) {
							if (firstPublicMethods.contains(methodName)) {
								for (Integer paraIndex : methodsConstraintMap2.get(methodName).keySet()) {
									for (ParamConstraintDTO paraConstraint : methodsConstraintMap2.get(methodName)
											.get(paraIndex)) {
										csvWriter.writeRecord(new String[] { (++rowNum) + "", second, first, methodName,
												paraIndex + "", paraConstraint.getOperator(),
												paraConstraint.getCompareValue() });
									}
								}
							}
						}
					} else if (methodsConstraintMap2.containsKey(methodName)) {
						Map<Integer, Set<ParamConstraintDTO>> paraConstraintMap1 = methodsConstraintMap1
								.get(methodName);
						Map<Integer, Set<ParamConstraintDTO>> paraConstraintMap2 = methodsConstraintMap2
								.get(methodName);
						Set<Integer> indeies = new HashSet<Integer>(paraConstraintMap1.keySet());
						indeies.addAll(paraConstraintMap2.keySet());
						for (Integer paraIndex : indeies) {
							if (!paraConstraintMap1.containsKey(paraIndex)) {
								for (ParamConstraintDTO paraConstraint : methodsConstraintMap2.get(methodName)
										.get(paraIndex)) {
									csvWriter.writeRecord(
											new String[] { (++rowNum) + "", second, first, methodName, paraIndex + "",
													paraConstraint.getOperator(), paraConstraint.getCompareValue() });
								}
							} else if (paraConstraintMap2.containsKey(paraIndex)) {
								Set<ParamConstraintDTO> paraConstraintSet1 = paraConstraintMap1.get(paraIndex);
								Set<ParamConstraintDTO> paraConstraintSet2 = paraConstraintMap2.get(paraIndex);
								for (ParamConstraintDTO paraConstraint2 : paraConstraintSet2) {
									boolean notContain = true;
									for (ParamConstraintDTO paraConstraint1 : paraConstraintSet1) {
										if (paraConstraint1.getCompareValue().equals(paraConstraint2.getCompareValue())) {
											notContain = false;
										}
									}
									if (notContain) {
										csvWriter.writeRecord(new String[] { (++rowNum) + "", second, first, methodName,
												paraIndex + "", paraConstraint2.getOperator(),
												paraConstraint2.getCompareValue() });
									}
								}
							}
						}
					}
				}
			}
		}

	}

	static Map<String, Map<String, Map<Integer, Set<ParamConstraintDTO>>>> getResult(String fileName) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String json = "";
			String line = null;
			while ((line = reader.readLine()) != null) {
				json += line;
			}
			Map<String, Map<String, Map<Integer, Set<ParamConstraintDTO>>>> obj = JSON.parseObject(json,
					new TypeReference<Map<String, Map<String, Map<Integer, Set<ParamConstraintDTO>>>>>() {
					});
			reader.close();
			return obj;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	static Set<String> fromFile(String fileName) {
		Set<String> set = new HashSet<>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (!"".equals(line)) {
					set.add(line);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return set;
	}
}
