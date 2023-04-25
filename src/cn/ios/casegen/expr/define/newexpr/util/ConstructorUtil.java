package cn.ios.casegen.expr.define.newexpr.util;

import cn.ios.casegen.util.ClassUtil;
import cn.ios.casegen.util.TypeUtil;
import cn.ios.casegen.util.log.Log;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import soot.FastHierarchy;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2022-01-18 11:21
 **/

public class ConstructorUtil {
    public static Set<SootMethod> getConstructorMethodOfClass(SootClass sootClass){
        List<SootClass> classList = Lists.newArrayList();
        classList.add(sootClass);

        Set<SootClass> subClasses = getAllSubClassFromGivenClass(sootClass);
        if (!subClasses.isEmpty()) {
            classList.addAll(subClasses);
        }
        Set<SootMethod> constructorList = Sets.newHashSet();
        for (SootClass oneClass : classList) {
            if (!ClassUtil.isIgnoredClass(oneClass)) {
                if (TypeUtil.isMapType(sootClass.getType()) && oneClass.getSuperclass().getName().equals("java.lang.Object")) {
                    continue;
                }
                ArrayList<SootMethod> sootMethods = new ArrayList<>(oneClass.getMethods());
                for (SootMethod sootMethod : sootMethods) {
                    if (sootMethod.isConstructor()) {
                        constructorList.add(sootMethod);
                    }
                }
            }
        }
        return constructorList;
    }

    private static Set<SootClass> getAllSubClassFromGivenClass(SootClass sootClass){
        Set<SootClass> subClasses = Sets.newHashSet();
        if (sootClass == null) return subClasses;
        try {
            FastHierarchy fastHierarchy = Scene.v().getOrMakeFastHierarchy();
//            FastHierarchy fastHierarchy = Scene.v().getFastHierarchy();
            List<SootClass> subclassCollection = new ArrayList<>(fastHierarchy.getSubclassesOf(sootClass));
            List<SootClass> allImplementersOfInterface = new ArrayList<>(fastHierarchy.getAllImplementersOfInterface(sootClass));
            subClasses.addAll(subclassCollection);
            subClasses.addAll(allImplementersOfInterface);
        } catch (RuntimeException e) {
            e.printStackTrace();
            Log.i("Cannot get fastHierarchy: ");
        }

        return subClasses;
    }
}