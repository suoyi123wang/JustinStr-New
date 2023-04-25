package cn.ios.casegen.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Executable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ErrorWriter {

	public static HashMap<String, HashMap<Class<?>, Set<Executable>>> errorHashMap = new HashMap<String, HashMap<Class<?>, Set<Executable>>>();

	public static Set<Executable> reportSet = new HashSet<Executable>();

	public static void addMethod(Executable method) {
		reportSet.add(method);
	}

	public static void write() {
		File file = new File("./Error.txt");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter(file);
			StringBuffer sb = new StringBuffer();
			sb.append("==============Success Method=========================\n");
			sb.append(reportSet.size() + "\n");
			sb.append("==============Error Method===========================\n");

			String str = "";
			int i = 0;
			for (String exceptionType : errorHashMap.keySet()) {
				str += "---------------" + exceptionType + "-----------------------\n";

				String exceptionString = "";
				int j = 0;
				HashMap<Class<?>, Set<Executable>> value = errorHashMap.get(exceptionType);
				for (Class<?> clazz : value.keySet()) {
					exceptionString += "    " + clazz + "\n";
					for (Executable method : value.get(clazz)) {
						exceptionString += "        " + method + "\n";
						i++;
						j++;
					}
				}
				str += j + "\n";
				str += exceptionString;
			}
			sb.append(i + "\n");
			sb.append(str + "\n");
			sb.append("=======================================");
			printWriter.write(sb.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (printWriter != null) {
				printWriter.close();
			}
		}
	}

	public static void addError(Throwable throwable, Executable method) {
		String exceptionType = throwable.getClass().getName();
		HashMap<Class<?>, Set<Executable>> value = null;
		if (errorHashMap.containsKey(exceptionType)) {
			value = errorHashMap.get(exceptionType);
		} else {
			value = new HashMap<Class<?>, Set<Executable>>();
			errorHashMap.put(exceptionType, value);
		}
		Class<?> clazz = method.getDeclaringClass();
		Set<Executable> methods = null;
		if (value.containsKey(clazz)) {
			methods = value.get(clazz);
		} else {
			methods = new HashSet<Executable>();
			value.put(clazz, methods);
		}
		methods.add(method);
	}
}

