package cn.ios.casegen.generator;

import cn.ios.casegen.config.GlobalCons;
import cn.ios.casegen.output.JUnitWriter;
import cn.ios.casegen.util.ClassUtil;
import cn.ios.report.ReportFactory;
import cn.ios.report.service.SaveInfoService;
import cn.ios.report.vo.ClassInfoVO;
import com.intellij.ide.plugins.cl.PluginClassLoader;
import org.apache.commons.io.FileUtils;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import soot.Scene;
import soot.SootClass;

import javax.tools.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2022-01-11 10:26
 **/
public class GenerationFactory {

    public static void generateClasses() {
        HashSet<SootClass> applicationClasses = new HashSet<>(Scene.v().getApplicationClasses());
        for (SootClass sootClass : applicationClasses) {
            if (ClassUtil.isIgnoredClass(sootClass) ||
                    (GlobalCons.pluginStart && !GlobalCons.CLASS_NAME_UNDER_TEST.contains(sootClass.getName()))) {
                continue;
            }
            JUnitClass jUnitClass = new JUnitClass(sootClass);
            String before = jUnitClass.toString();

            Thread thread = new Thread(() -> {
                jUnitClass.start();
                // 获取junitclass.start()的返回值
                JUnitClass result = jUnitClass.getResult();
                if (result != null) {
                    String after = result.toString();
                    // new add
                    if (!after.equals(before) && compileEachClass(jUnitClass)){
                        // 写入本地java文件
                        JUnitWriter.write(jUnitClass);
                        runTestCase(jUnitClass);
                    }
                }

            });

            thread.start();

            try {
                long thread_time =(long) GlobalCons.MAX_TIME_PER_CLASS * 1000;
                thread.join(thread_time);
            } catch (InterruptedException e){
                // ...
            }
            // 如果线程仍在执行，就中断它
            if (thread.isAlive()) {
                thread.interrupt();
                System.err.println("generateClasses:" + "junitClass.start() timeout\n");
            }
            GlobalCons.ALREADY_DEAL_CLASS_NUM++;
        }

        try {
            FileUtils.deleteDirectory(new File(GlobalCons.TEST_COMPILE_TEMP_FOLDER));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean compileEachClass(JUnitClass jUnitClass){
        Boolean call = false;
        String tempDir = GlobalCons.TEST_COMPILE_TEMP_FOLDER + File.separator + jUnitClass.getPackageName().replace(".", File.separator);
        File tempFolder, tempFile = null;
        try {
            tempFolder = new File(tempDir);
            if (!tempFolder.exists()) {
                tempFolder.mkdirs();
            }
            tempFile = new File(tempDir + File.separator + jUnitClass.getName() + ".java");
            tempFile.createNewFile();
            PrintWriter printWriter = new PrintWriter(tempDir + File.separator + jUnitClass.getName() + ".java");
            printWriter.write(jUnitClass.toString());
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<File> tempFileList = new ArrayList<>();
        tempFileList.add(tempFile);
        try {
            System.setProperty("java.home", GlobalCons.LOCAL_JRE_PATH);
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), StandardCharsets.UTF_8);

            Iterable<? extends JavaFileObject> javaFileObjectsFromFiles = fileManager.getJavaFileObjectsFromFiles(tempFileList);
//            List<String> optionList = new ArrayList<>(Arrays.asList("-classpath", GlobalCons.TEST_INPUT_FOLDER + File.pathSeparator + System.getProperty("user.dir") + File.separator + "junit-4.13.1.jar"));
//            List<String> optionList = new ArrayList<>(Arrays.asList("-classpath", GlobalCons.TEST_INPUT_FOLDER + File.pathSeparator + "/Users/wangmiaomiao/coding/final/preCondition" + File.separator + "junit-4.13.1.jar"));
            String junitJarPath = GlobalCons.TEST_COMPILE_TEMP_FOLDER + File.separator + "junit-4.13.1.jar";

            List<String> optionList = new ArrayList<>(Arrays.asList("-classpath", GlobalCons.TEST_INPUT_FOLDER + File.pathSeparator + junitJarPath));

            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, optionList , null, javaFileObjectsFromFiles);
            call = task.call();
            fileManager.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return call;
    }

    /**
     * classLoader
     * @param jUnitClass
     */
    private static void runTestCase(JUnitClass jUnitClass){
        Result runResult = null;
        Class<?> junitCoreClass = null;
        Class<?> testClass = null;

        String hamcrestPath= copyHamcrestJarFile();
        String junitJarPath = GlobalCons.TEST_COMPILE_TEMP_FOLDER + File.separator + "junit-4.13.1.jar";
        String testClassName = jUnitClass.getPackageName().isEmpty()? jUnitClass.getName() : jUnitClass.getPackageName() + "." + jUnitClass.getName();
        String sourceClassName = jUnitClass.getPackageName().isEmpty()? jUnitClass.getName().substring(0,jUnitClass.getName().length() - 5) :
                jUnitClass.getPackageName() + "." + jUnitClass.getName().substring(0, jUnitClass.getName().length() - 5);

        try {
            if (GlobalCons.pluginStart) {
                ClassLoader currentClassLoader = GenerationFactory.class.getClassLoader();
                PluginClassLoader pluginClassLoader = null;
                if (currentClassLoader instanceof PluginClassLoader) {
                    pluginClassLoader = (PluginClassLoader) currentClassLoader;
                }
                pluginClassLoader.addURL(new URL("file://" + GlobalCons.TEST_INPUT_FOLDER.replace('\\','/') + "/"));
                pluginClassLoader.addURL(new URL("file://" + hamcrestPath));
                pluginClassLoader.addURL(new URL("file://" + GlobalCons.TEST_COMPILE_TEMP_FOLDER + "/"));

                junitCoreClass = pluginClassLoader.loadClass("org.junit.runner.JUnitCore");
                testClass = pluginClassLoader.loadClass(testClassName);
                pluginClassLoader.loadClass(sourceClassName);
            } else {
                URLClassLoader urlClassLoader = null;
                urlClassLoader = new URLClassLoader(
                        new URL[]{
                                new URL("file://" + GlobalCons.TEST_COMPILE_TEMP_FOLDER + "/"),
                                new URL("file://" + GlobalCons.TEST_INPUT_FOLDER.replace('\\', '/')),
                                new URL("file://" + GlobalCons.TEST_INPUT_FOLDER.replace('\\', '/') + "/"),
                                new URL("file://" + junitJarPath),
                                new URL("file://" + hamcrestPath),
                        });

                junitCoreClass = urlClassLoader.loadClass("org.junit.runner.JUnitCore");
                testClass = urlClassLoader.loadClass(testClassName);
                urlClassLoader.loadClass(sourceClassName);
                urlClassLoader.loadClass("org.junit.runner.Result");

                Thread.currentThread().setContextClassLoader(urlClassLoader);
            }

            if (junitCoreClass == null || testClass == null) {
                return;
            }

            String outFileName = GlobalCons.TEST_COMPILE_TEMP_FOLDER + File.separator +  "aa.txt";

//            File file = new File(outFileName);
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            System.setErr(new PrintStream(outFileName));
//            System.setOut(new PrintStream(outFileName));
//            Method runClassesMethod = junitCoreClass.getMethod("runClasses", Class[].class);
//            runResult = (org.junit.runner.Result) runClassesMethod.invoke(null, new Object[]{new Class[]{testClass}});
//            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
//            System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.out)));


            JUnitCore jUnitCore = new JUnitCore();
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            Class<?> finalTestClass = testClass;
            Callable<Result> callable = new Callable<Result>() {
                @Override
                public Result call() throws Exception {
                    File file = new File(outFileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    System.setErr(new PrintStream(outFileName));
                    System.setOut(new PrintStream(outFileName));
                    Result runResult = jUnitCore.run(finalTestClass);
                    System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                    System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                    return runResult;
                }
            };
            Future<Result> submit = executorService.submit(callable);
            runResult = submit.get(30, TimeUnit.SECONDS);

            // ---start: insert into class_info;
            String fullClassName = jUnitClass.getClazzUnderAnalysis().getName();
            ClassInfoVO classInfoVO = new ClassInfoVO();
            classInfoVO.setFullClassName(fullClassName);
            classInfoVO.setMethodNum(jUnitClass.getClazzUnderAnalysis().getMethods().size());
            classInfoVO.setTestMethodNum(jUnitClass.getTestMethodNum());
            classInfoVO.setTestCaseNum(jUnitClass.getMethods().size());
            int executeCaseNum = runResult.getRunCount();
            int failureCount = runResult.getFailureCount();
            int skipCount = runResult.getIgnoreCount();
            int successCount = executeCaseNum - failureCount - skipCount;
            classInfoVO.setExecuteTestCaseNum(executeCaseNum);
            classInfoVO.setSuccessTestCaseNum(successCount);
            classInfoVO.setSkipTestCaseNum(skipCount);
            classInfoVO.setFailTestCaseNum(failureCount);
            classInfoVO.setExecuteTime((int) runResult.getRunTime());
            SaveInfoService.insertIntoClassInfo(classInfoVO);
            // ---end: insert into general_info;

            // ---start: insert into detail_info;
            for (Failure failure : runResult.getFailures()) {
                try {
                    String testMethodName  = failure.getDescription().getMethodName();
                    String originalException= failure.getException().toString();
                    String exceptionName  = originalException.contains(":") ?
                            originalException.substring(0, failure.getException().toString().indexOf(":")) : originalException;
                    StringBuilder stackDetail = new StringBuilder();

                    StackTraceElement[] stackTrace = failure.getException().getStackTrace();
                    int flag = 0;
                    for (int i = stackTrace.length - 1 ; i >= 0; i--) {
                        if (stackTrace[i].toString().startsWith(fullClassName)) {
                            flag = i;
                            break;
                        }
                    }
                    stackDetail.append(failure.toString()).append("\n");
                    for (int i = 0; i <= flag && i < stackTrace.length; i++) {
                        stackDetail.append(stackTrace[i].toString()).append("\n");
                    }

                    String testMethodBody = "";
                    for (JUnitMethod testMethod : jUnitClass.getTestMethods()) {
                        if (testMethod.toString().contains("public void " + testMethodName + "()")) {
                            testMethodBody = testMethod.toString();
                        }
                    }
                    // 在这里生成堆栈信息详细页面
                    ReportFactory.genDetailHTML(GlobalCons.PROJECT_NAME, GlobalCons.REPORT_OUTPUT_FOLDER, fullClassName, testMethodName, testMethodBody, stackDetail.toString());
                    SaveInfoService.insertIntoDetailInfo(testMethodName,stackDetail.toString(),testMethodBody,fullClassName,exceptionName);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
            // ---end: insert into detail_info;

            // ---start: insert into project_info;
            SaveInfoService.insertIntoPackageInfo(jUnitClass.getPackageName());
            // ---end: insert into project_info;
        } catch ( Exception e) {
//            e.printStackTrace();
        }
    }

    private static String copyHamcrestJarFile(){
        try {
            File destFolder = new File(GlobalCons.TEST_COMPILE_TEMP_FOLDER);
            if (!destFolder.exists()) {
                destFolder.mkdirs();
            }

            File junitJarFile = new File(GlobalCons.TEST_COMPILE_TEMP_FOLDER + File.separator + "hamcrest-core-1.3.jar");
            if (junitJarFile.exists()) {
                return junitJarFile.getPath();
            } else {
                junitJarFile.createNewFile();
            }

            InputStream inputStream = GenerationFactory.class.getResourceAsStream("/hamcrest-core-1.3.jar");
            FileUtils.copyInputStreamToFile(inputStream,junitJarFile);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        return GlobalCons.TEST_COMPILE_TEMP_FOLDER + File.separator + "hamcrest-core-1.3.jar";
    }

}
