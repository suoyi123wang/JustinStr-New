package cn.ios.casegen.generator;

import cn.ios.casegen.constraint.DTO.PossibleParamValueDTO;
import cn.ios.casegen.constraint.VO.ParamConstraintVO;
import cn.ios.casegen.config.GlobalCons;
import cn.ios.casegen.enums.GenerationEnum;
import cn.ios.casegen.util.ClassUtil;
import cn.ios.casegen.util.log.Log;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.tools.JavaCompiler.CompilationTask;

public class JUnitClass {

	private SootClass clazzUnderAnalysis = null;
	private String className = null;
	private String packageName = null;
	private int testMethodNum = 0;

	JUnitClass result = null;

	private ArrayList<JUnitMethod> methods = new ArrayList<JUnitMethod>();

	public JUnitClass(SootClass sootClass) {
		clazzUnderAnalysis = sootClass;
		className = clazzUnderAnalysis.getShortName() + "_Test";
		packageName = clazzUnderAnalysis.getPackageName();
	}

	public JUnitClass start() {
		for (SootMethod method : clazzUnderAnalysis.getMethods()) {
			if (!ClassUtil.isIgnoredMethod(method)){
				try {
					generateJUnitMethod(method,methods);
					testMethodNum++;
				} catch (Exception | Error e) {
					Log.e("Exception in JUnitClass.start：" + clazzUnderAnalysis );
				}
			}
		}
		result = this;
		return this;
	}

	@Override
	public String toString() {

		StringBuffer stringBuffer = new StringBuffer();

		/** package : take default package into account */
		if (packageName != null && !packageName.isEmpty()) {
			stringBuffer.append(GenerationEnum.PACKAGE.getValue()).
					append(GenerationEnum.ONE_SPACE.getValue()).
					append(packageName).
					append(GenerationEnum.SEMICOLON.getValue()).
					append(GenerationEnum.TWO_NEW_LINES.getValue());
		}

		/** import */
		stringBuffer.append(GenerationEnum.IMPORT_TEST.getValue());

		/** class */
		stringBuffer.append(GenerationEnum.PUBLIC_CLASS.getValue()).
				append(className).
				append(GenerationEnum.ONE_SPACE.getValue()).
				append(GenerationEnum.LEFT_CURLY_BRACE.getValue()).
				append(GenerationEnum.TWO_NEW_LINES.getValue());

		/** methods */
		for (JUnitMethod method : methods) {
			stringBuffer.append(method);
			stringBuffer.append(GenerationEnum.TWO_NEW_LINES.getValue());
		}

		stringBuffer.append(GenerationEnum.RIGHT_CURLY_BRACE.getValue());
		return stringBuffer.toString();
	}

	public String getName() {
		return className;
	}

	public List<JUnitMethod> getTestMethods() {
		return methods;
	}

	public int getTestMethodNum() {
		return testMethodNum;
	}

	public  void generateJUnitMethod(SootMethod sootMethod, ArrayList<JUnitMethod> methods) {
		/**
		 * Take java bridge method into account.( Modifier.isSynthetic(method) )
		 * 20200723
		 */
		String className = sootMethod.getDeclaringClass().getName();
		String methodSignature = sootMethod.getSignature();
		List<Map<Integer, PossibleParamValueDTO>> possibleValuesList =
				getPossibleValuesList(className, methodSignature, GlobalCons.MAX_UNIT_METHOD);

		int index = 0;

		JUnitMethod jUnitMethod = null;
		while (GlobalCons.MAX_UNIT_METHOD > index) {
			try {
				GlobalCons.VARIABLE_INDEX = 0;

				int tempNum = 0;
				boolean compilable = false;
				while (tempNum < 5 && !compilable) {
					Map<Integer, PossibleParamValueDTO> onePossibleValue = index >= possibleValuesList.size()?
							Maps.newHashMap(): possibleValuesList.get(index);
					jUnitMethod = new JUnitMethod(onePossibleValue, sootMethod, index, methods.size());
					// check each generated jUnitMethod is compilable
					compilable = compileTestCase(jUnitMethod);
					tempNum ++;
				}
				if (compilable) {
					methods.add(jUnitMethod);
				}
			} catch (Exception | Error e) {
//				Log.e("in JunitClass.generateJUnitMethod: " + sootMethod.getName());
//				e.printStackTrace();
			}
			index ++;
		}
	}

	public String getPackageName() {
		return packageName;
	}

	public SootClass getClazzUnderAnalysis() {
		return clazzUnderAnalysis;
	}

	public String getClassName() {
		return className;
	}

	public ArrayList<JUnitMethod> getMethods() {
		return methods;
	}

	/**
	 * getAllPossibleValueList for some method, each item in result is a complete combination.
	 * @param className
	 * @param methodSignature
	 * @param totalNum
	 * @return
	 */
	private List<Map<Integer, PossibleParamValueDTO>> getPossibleValuesList(String className, String methodSignature, int totalNum){
		List<Map<Integer, PossibleParamValueDTO>> result = Lists.newArrayList();
		Map<String, Map<String, Map<Integer, ParamConstraintVO>>> paramConstraintVOS = GlobalCons.PARAM_CONSTRAINTS_VOS;
		if (!paramConstraintVOS.containsKey(className)) return result;
		if (!paramConstraintVOS.get(className).containsKey(methodSignature)) return result;

		Map<Integer, ParamConstraintVO> paramConstraintVOMap = paramConstraintVOS.get(className).get(methodSignature);
		int i = 0;
		while (i < totalNum) {
			Map<Integer, PossibleParamValueDTO> onePossibleValues = Maps.newHashMap();
			for (Map.Entry<Integer, ParamConstraintVO> integerParamConstraintVOEntry : paramConstraintVOMap.entrySet()) {
				Integer index = integerParamConstraintVOEntry.getKey();
				ParamConstraintVO paramConstraintVO = integerParamConstraintVOEntry.getValue();
				Type paramType = paramConstraintVO.getParamType();
				int numsOfPossibleValues = paramConstraintVO.getNumsOfPossibleValues();
				if (i >= numsOfPossibleValues) return result;

				Map<String, Set<String>> possibleValuesForObject = paramConstraintVO.getPossibleValuesForObject();
				Set<String> possibleValuesForSimpleType = paramConstraintVO.getPossibleValuesForSimpleType();

				if (possibleValuesForObject != null && !possibleValuesForObject.isEmpty()){
					Map<String, String> objectPossibleValues = Maps.newHashMap();
					for (Map.Entry<String, Set<String>> stringSetEntry : possibleValuesForObject.entrySet()) {
						ArrayList<String> possibleValues = new ArrayList<>(stringSetEntry.getValue());
						if (i < possibleValues.size()) {
							objectPossibleValues.put(stringSetEntry.getKey(),possibleValues.get(i));
						}
					}
					onePossibleValues.put(index, new PossibleParamValueDTO(index, paramType, objectPossibleValues));
				} else if (possibleValuesForSimpleType != null && !possibleValuesForSimpleType.isEmpty()){
					ArrayList<String> simplePossibleValues = new ArrayList<>(possibleValuesForSimpleType);
					if (i < simplePossibleValues.size()) {
						onePossibleValues.put(index, new PossibleParamValueDTO(index, paramType, simplePossibleValues.get(i)));
					}
				}
			}
			if (!onePossibleValues.isEmpty()){
				result.add(onePossibleValues);
			}
			i++;
		}

		return result;

	}

	private String wrapJunitMethodInOneClass(JUnitMethod jUnitMethod){
		StringBuffer stringBuffer = new StringBuffer();

		/** package : take default package into account */
		if (packageName != null && !packageName.isEmpty()) {
			stringBuffer.append(GenerationEnum.PACKAGE.getValue()).
					append(GenerationEnum.ONE_SPACE.getValue()).
					append(packageName).
					append(GenerationEnum.SEMICOLON.getValue()).
					append(GenerationEnum.TWO_NEW_LINES.getValue());
		}

		/** import */
		stringBuffer.append(GenerationEnum.IMPORT_TEST.getValue());

		/** class */
		stringBuffer.append(GenerationEnum.PUBLIC_CLASS.getValue()).
				append(className).
				append(GenerationEnum.ONE_SPACE.getValue()).
				append(GenerationEnum.LEFT_CURLY_BRACE.getValue()).
				append(GenerationEnum.TWO_NEW_LINES.getValue());

		stringBuffer.append(jUnitMethod);
		stringBuffer.append(GenerationEnum.TWO_NEW_LINES.getValue());

		stringBuffer.append(GenerationEnum.RIGHT_CURLY_BRACE.getValue());

		return stringBuffer.toString();
	}

	private boolean compileTestCase(JUnitMethod jUnitMethod){
		Boolean call = false;
		String underTestClass = wrapJunitMethodInOneClass(jUnitMethod);
		String tempDir = GlobalCons.TEST_COMPILE_TEMP_FOLDER + File.separator +  System.currentTimeMillis() + File.separator + packageName.replace(".", File.separator);
		File tempFolder, tempFile = null;
		try {
			tempFolder = new File(tempDir);
			if (!tempFolder.exists()) {
				tempFolder.mkdirs();
			}
			tempFile = new File(tempDir + File.separator + className + ".java");
			tempFile.createNewFile();
			PrintWriter printWriter = new PrintWriter(tempDir + File.separator + className + ".java");
			printWriter.write(underTestClass);
			printWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<File> tempFileList = new ArrayList<>();
		tempFileList.add(tempFile);

		try {
			System.setProperty("java.home", GlobalCons.LOCAL_JRE_PATH);
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			if (compiler == null) {
				System.err.println("compiler = null;");
			}
			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), StandardCharsets.UTF_8);

			Iterable<? extends JavaFileObject> javaFileObjectsFromFiles = fileManager.getJavaFileObjectsFromFiles(tempFileList);

			String junitJarPath = copyJunitJarFile();
//			List<String> optionList = new ArrayList<>(Arrays.asList("-classpath", GlobalCons.TEST_INPUT_FOLDER + File.pathSeparator + "/Users/wangmiaomiao/coding/final/preCondition" + File.separator + "junit-4.13.1.jar"));
			List<String> optionList = new ArrayList<>(Arrays.asList("-classpath", GlobalCons.TEST_INPUT_FOLDER + File.pathSeparator + junitJarPath));

			CompilationTask task = compiler.getTask(null, fileManager, diagnostics, optionList , null, javaFileObjectsFromFiles);
			call = task.call();
			fileManager.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return call;
	}

	public JUnitClass getResult(){
		return result;
	}

	private String copyJunitJarFile(){
		try {
			File destFolder = new File(GlobalCons.TEST_COMPILE_TEMP_FOLDER);
			if (!destFolder.exists()) {
				destFolder.mkdirs();
			}

			File junitJarFile = new File(GlobalCons.TEST_COMPILE_TEMP_FOLDER + File.separator + "junit-4.13.1.jar");
			if (junitJarFile.exists()) {
				return junitJarFile.getPath();
			} else {
				junitJarFile.createNewFile();
			}

			InputStream inputStream = JUnitClass.class.getResourceAsStream("/junit-4.13.1.jar");
			FileUtils.copyInputStreamToFile(inputStream,junitJarFile);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

		return GlobalCons.TEST_COMPILE_TEMP_FOLDER + File.separator + "junit-4.13.1.jar";
	}
}
