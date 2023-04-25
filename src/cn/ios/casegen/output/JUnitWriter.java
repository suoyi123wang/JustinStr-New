package cn.ios.casegen.output;

import cn.ios.casegen.config.GlobalCons;
import cn.ios.casegen.generator.JUnitClass;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

public class JUnitWriter {

	public static final Set<String> srcClasses = new HashSet<String>();

	public static void write(JUnitClass class1) {
		String packageName = class1.getPackageName();
		if (packageName == null) {
			packageName = "";
		}
		File folder = new File(GlobalCons.TEST_OUTPUT_FOLDER+ File.separator + packageName.replace(".", File.separator));
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File file = new File(folder, class1.getName() + ".java");

		try {
			file.createNewFile();
			PrintWriter printWriter = new PrintWriter(file);
			printWriter.write(class1.toString());
			printWriter.close();
			srcClasses.add(file.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
