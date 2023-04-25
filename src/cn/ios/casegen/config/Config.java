package cn.ios.casegen.config;

import cn.ios.casegen.util.log.Log;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteDataSource;
import soot.*;
import soot.options.Options;

import java.io.File;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class Config {

	/**
	 * 所有的配置项只调用这一个方法
	 * 如果String为null或者int值是-1就不会去配置
	 */
	public static void onceConfig(String jrePath, String outputPath, String inputPath, int maxlengthOfString, int minSizeOfSet,
								  int maxSizeOfSet, int maxSizeOfCase, int maxTimeOfClass){
		if (!jrePath.isEmpty()) {
			GlobalCons.LOCAL_JRE_PATH = jrePath.endsWith(File.separator)? jrePath.substring(0,jrePath.length()-1) : jrePath;
		}
		if (outputPath != null && !outputPath.isEmpty()) {
			GlobalCons.TEST_OUTPUT_FOLDER = outputPath + File.separator + "justinStr-result" + File.separator + "test";
			GlobalCons.REPORT_OUTPUT_FOLDER = outputPath + File.separator + "justinStr-result" + File.separator + "report";
			GlobalCons.TEST_COMPILE_TEMP_FOLDER = outputPath + File.separator + "justinStr-result" + File.separator + "compile-temp";
			GlobalCons.DB_PATH = outputPath + File.separator + "justinStr-result" + File.separator + "report" + File.separator + "report.db";
		}

		if (inputPath != null && !inputPath.isEmpty()) {
			GlobalCons.TEST_INPUT_FOLDER = inputPath;
		}

		GlobalCons.STRING_MAX_LENGTH = maxlengthOfString == -1? GlobalCons.STRING_MAX_LENGTH : maxlengthOfString;
		GlobalCons.ARRAY_MAX_SIZE = maxSizeOfSet == -1? GlobalCons.ARRAY_MAX_SIZE : Math.max(1, maxSizeOfSet);
		GlobalCons.ARRAY_MIN_SIZE = minSizeOfSet == -1? GlobalCons.ARRAY_MIN_SIZE : Math.max(0,minSizeOfSet);
		GlobalCons.MAX_UNIT_METHOD = maxSizeOfCase == -1? GlobalCons.MAX_UNIT_METHOD : Math.max(1, maxSizeOfCase);
		GlobalCons.MAX_TIME_PER_CLASS = maxTimeOfClass == -1? GlobalCons.MAX_TIME_PER_CLASS : Math.max(0, maxTimeOfClass);

		if (GlobalCons.connection == null) {
			createSQLConnection();
		}

		if (!GlobalCons.sootConfig) {
			setSootConfig();
		}
	}

	private static void createSQLConnection(){
		File file = new File(GlobalCons.REPORT_OUTPUT_FOLDER);
		if (!file.exists()) {
			file.mkdirs();
		}

		SQLiteConfig sqLiteConfig = new SQLiteConfig();
		sqLiteConfig.setSharedCache(true);
		sqLiteConfig.enableRecursiveTriggers(true);

		SQLiteDataSource sqLiteDataSource = new SQLiteDataSource(sqLiteConfig);
		sqLiteDataSource.setUrl("jdbc:sqlite:" + GlobalCons.DB_PATH);

		try {
			GlobalCons.connection = sqLiteDataSource.getConnection();
			String sql = "DROP TABLE IF EXISTS package_info; DROP TABLE IF EXISTS class_info; DROP TABLE IF EXISTS detail_info; DROP TABLE IF EXISTS exception_type;" +

					"create table package_info (package_name VARCHAR PRIMARY KEY);" +

					"create table class_info (id INTEGER PRIMARY KEY AUTOINCREMENT, class_name VARCHAR, method_num INTEGER, test_method_num INTEGER," +
					"compile_case_num INTEGER, execute_case_num INTEGER, success_count INTEGER, skip_count INTEGER, failure_count INTEGER);" +

					"create table exception_type (id INTEGER PRIMARY KEY AUTOINCREMENT, method_name_under_test VARCHAR, class_name_under_test VARCHAR, exception_name VARCHAR, usual_stack_info VARCHAR, " +
					"UNIQUE (method_name_under_test, class_name_under_test, exception_name, usual_stack_info));" +

					"create table detail_info (id INTEGER PRIMARY KEY AUTOINCREMENT, test_method_name VARCHAR, stack_detail VARCHAR, test_method_body VARCHAR, exception_type_id INTEGER);";
			Statement statement = GlobalCons.connection.createStatement();
			statement.executeUpdate(sql);
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void setSootConfig() {
		if (GlobalCons.sootConfig || GlobalCons.LOCAL_JRE_PATH == null || GlobalCons.LOCAL_JRE_PATH.isEmpty()) return;
		System.out.println("-------------soot is loading-------------");
		G.reset();
		String jreDir = GlobalCons.LOCAL_JRE_PATH + File.separator + "lib" + File.separator +"jce.jar";
		String jceDir = GlobalCons.LOCAL_JRE_PATH + File.separator +  "lib" + File.separator +"rt.jar";

		String path = jreDir + File.pathSeparator + jceDir +
				File.pathSeparator + GlobalCons.TEST_INPUT_FOLDER;

		Options.v().set_soot_classpath(path);
		Options.v().set_include_all(true);
		Options.v().set_process_dir(new ArrayList<>(Collections.singletonList(GlobalCons.TEST_INPUT_FOLDER)));
		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_output_format(Options.output_format_shimple);
		Options.v().set_allow_phantom_refs(true);

//		Options.v().set_src_prec(Options.src_prec_only_class);  // class
		Options.v().set_src_prec(Options.src_prec_c);  // jar

		Options.v().allow_phantom_refs();
		Options.v().set_whole_program(true);

		Pack p1 = PackManager.v().getPack("jtp");
		String phaseName = "jtp.bt";

		Transform t1 = new Transform(phaseName, new BodyTransformer() {
			@Override
			protected void internalTransform(Body b, String phase, Map<String, String> options) {
				try {
					b.getMethod().setActiveBody(b);
				} catch (Exception e) {
					Log.e(e);
				}
			}
		});

		p1.add(t1);

		soot.Main.v().autoSetOptions();
		try {
			Scene.v().loadNecessaryClasses();
			PackManager.v().runPacks();
		} catch (Exception e) {
			Log.e(e);
		}

		GlobalCons.sootConfig = true;

	}

}