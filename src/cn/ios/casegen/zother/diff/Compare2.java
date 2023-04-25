package cn.ios.casegen.zother.diff;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.ios.casegen.constraint.DTO.ParamConstraintDTO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.csvreader.CsvWriter;

import cn.ios.casegen.util.log.Log;

public class Compare2 {

	public static void main(String[] args) throws IOException {

		String first = args[0];
		String second = args[1];

		String FILE_1 = "jdk" + first + ".txt";

		String FILE_2 = "jdk" + second + ".txt";

		String filePre = first + "_VS_" + second + "_1207";

		String output = filePre + ".txt";
		Map<String, Map<String, Map<Integer, Set<ParamConstraintDTO>>>> map1 = getResult(FILE_1);
		Map<String, Map<String, Map<Integer, Set<ParamConstraintDTO>>>> map2 = getResult(FILE_2);
		Set<String> classNames = new HashSet<String>(map1.keySet());
		classNames.addAll(map2.keySet());
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(new File(output));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		CsvWriter csvWriter = new CsvWriter(filePre + ".csv", ',', Charset.forName("GBK"));
		csvWriter.writeRecord(
				new String[] { "In", "NotIn", "Type", "class", "method", "paraIndex", "operator", "compareValue" });

		for (String className : classNames) {
			if (!map1.containsKey(className)) {
				printWriter.write("\r\nIn " + second + " but not in " + first + " class=" + className);
				csvWriter.writeRecord(new String[] { second, first, "class", className });
			} else if (!map2.containsKey(className)) {
				printWriter.write("\r\nIn " + first + " but not in " + second + " class=" + className);
				csvWriter.writeRecord(new String[] { first, second, "class", className });
			} else {
				Map<String, Map<Integer, Set<ParamConstraintDTO>>> methods1 = map1.get(className);
				Map<String, Map<Integer, Set<ParamConstraintDTO>>> methods2 = map2.get(className);
				Set<String> methodNames = new HashSet<String>(methods1.keySet());
				methodNames.addAll(methods2.keySet());
				for (String methodName : methodNames) {
					String methodSig = className + ":" + methodName;
					if (!methods1.containsKey(methodName)) {
						printWriter.write("\r\nIn " + second + " but not in " + first + " method=" + methodSig);
						csvWriter.writeRecord(new String[] { second, first, "method", className, methodName });
					} else if (!methods2.containsKey(methodName)) {
						printWriter.write("\r\nIn " + first + " but not in " + second + " method=" + methodSig);
						csvWriter.writeRecord(new String[] { first, second, "method", className, methodName });
					} else {
						Map<Integer, Set<ParamConstraintDTO>> paras1 = methods1.get(methodName);
						Map<Integer, Set<ParamConstraintDTO>> paras2 = methods2.get(methodName);
						Set<Integer> indeies = new HashSet<Integer>(paras1.keySet());
						indeies.addAll(paras2.keySet());
						for (Integer index : indeies) {
							String paraSig = methodSig + "  index= " + index;
							if (!paras1.containsKey(index)) {
								printWriter
										.write("\r\nIn " + second + " but not in " + first + " parameter=" + paraSig);
								csvWriter.writeRecord(
										new String[] { second, first, "parameter", className, methodName, index + "" });
							} else if (!paras2.containsKey(index)) {
								printWriter
										.write("\r\nIn " + first + " but not in " + second + " parameter=" + paraSig);
								csvWriter.writeRecord(
										new String[] { first, second, "parameter", className, methodName, index + "" });
							} else {
								Set<ParamConstraintDTO> paraConstraintInfoCopies1 = paras1.get(index);
								Set<ParamConstraintDTO> paraConstraintInfoCopies2 = paras2.get(index);
								for (ParamConstraintDTO copy1 : paraConstraintInfoCopies1) {
									boolean notContain = true;
									for (ParamConstraintDTO copy2 : paraConstraintInfoCopies2) {
										if (copy2.getCompareValue().equals(copy1.getCompareValue())
												&& copy2.getOperator().equals(copy1.getOperator())) {
											notContain = false;
										}
									}
									if (notContain) {
										String valueSig = paraSig + " ##" + copy1.getOperator() + " "
												+ copy1.getCompareValue();
										printWriter.write(
												"\r\nIn " + first + " but not in " + second + " value=" + valueSig);
										csvWriter.writeRecord(new String[] { first, second, "compare", className,
												methodName, index + "", copy1.getOperator(), copy1.getCompareValue() });
									}
								}
								for (ParamConstraintDTO copy1 : paraConstraintInfoCopies2) {
									boolean notContain = true;
									for (ParamConstraintDTO copy2 : paraConstraintInfoCopies1) {
										if (copy2.getCompareValue().equals(copy1.getCompareValue())
												&& copy2.getOperator().equals(copy1.getOperator())) {
											notContain = false;
										}
									}
									if (notContain) {
										String valueSig = paraSig + " ##" + copy1.getOperator() + " "
												+ copy1.getCompareValue();
										printWriter.write(
												"\r\nIn " + second + " but not in " + first + " value=" + valueSig);
										csvWriter.writeRecord(new String[] { second, first, "compare", className,
												methodName, index + "", copy1.getOperator(), copy1.getCompareValue() });
									}
								}
							}
						}
					}
				}
			}
		}
		printWriter.close();
		csvWriter.close();
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

			Log.e(obj.size());
			reader.close();
			return obj;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static Set<String> fromFile(String fileName){
		Set<String> set = new HashSet<>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if(!"".equals(line)) {
					set.add(line);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return set;
	}

	static void putDataIn(Map<?, ?> map, String line) {
		if (line == null) {
			return;
		}
	}
}
