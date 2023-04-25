package cn.ios;

import cn.ios.casegen.config.Config;
import cn.ios.casegen.config.GlobalCons;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import soot.*;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author: wangmiaomiao
 * @description: TODO
 * @date: 2023/3/16 15:01
 */
public class Benchmark {
    @Test
    public void debugQi03() {
        String jrePath = "/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre";
        String outputPath = "/Users/wangmiaomiao/coding/final-new/JustinStrPlugin/out/artifacts/JustinStrPlugin_jar/output";
//        String inputPath = "/Users/wangmiaomiao/coding/Demo/out/production/Demo";
        String inputPath = "/Users/wangmiaomiao/Downloads/commons-io-2.11.0.jar";
        GlobalCons.START_TIME = System.currentTimeMillis();
        Config.onceConfig(jrePath, outputPath, inputPath,-1, -1, -1, -1, -1);
        API.generateTestCaseAfterConfig();
    }

    @Test
    public void testCompile() {

        JUnitCore runner = new JUnitCore();

//        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            URLClassLoader urlClassLoader = new URLClassLoader(
                    new URL[]{
                            new URL("file:///Users/wangmiaomiao/coding/Demo/justinStr-result/compile-temp/"),
                            new URL("file:///Users/wangmiaomiao/coding/Demo/out/production/Demo/")

                    });
            String testClassName = "cn.iscas.DDD";
            Class<?> aClass = urlClassLoader.loadClass(testClassName + "_Test");
            urlClassLoader.loadClass(testClassName);
            Thread.currentThread().setContextClassLoader(urlClassLoader);

            ExecutorService executorService = Executors.newFixedThreadPool(1);
            Callable<Result> callable = new Callable<Result>() {

                @Override
                public Result call() throws Exception {
                    Result res = runner.run(aClass);
                    return res;
                }
            };

            try {
                Future<Result> future = executorService.submit(callable);
                Result runResult = future.get(2, TimeUnit.MINUTES);
                System.out.println("aaaa");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAllMethods(String target){
        String jrePath = "/Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre";
        Config.onceConfig(jrePath,null,target,-1,-1,-1,5,-1);

        SootClass sootClass = Scene.v().getSootClass("java.lang.StringBuffer");
        List<SootMethod> methods = sootClass.getMethods();
        int i = 0;
        for (SootMethod method : methods) {
            if (method.isPhantom() || method.isNative() || method.isStatic() || !method.isPublic() || method.isConstructor()) continue;

            Type returnType = method.getReturnType();
            if (returnType instanceof VoidType) continue;

            if (method.getName().contains("init")) continue;

            String modifier = Modifier.toString(method.getModifiers());
            System.out.println(modifier +"---" + method.getSignature());
            i++;
        }
        System.out.println(i);
    }
}
