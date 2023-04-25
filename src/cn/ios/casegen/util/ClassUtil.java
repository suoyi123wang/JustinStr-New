package cn.ios.casegen.util;

import cn.ios.casegen.variable.FullClassType;
import com.google.common.collect.Lists;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.tagkit.SignatureTag;
import soot.tagkit.Tag;

import java.util.*;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2022-01-11 10:18
 **/

public class ClassUtil {

    public static boolean isIgnoredClass(SootClass sootClass) {
        return !sootClass.isPublic() || sootClass.isAbstract() || sootClass.isInterface() || sootClass.isInnerClass();
    }

    public static boolean isIgnoredMethod(SootMethod sootMethod) {
        if (!sootMethod.isPublic() || sootMethod.isAbstract() || sootMethod.isConstructor() || sootMethod.getName().contains("$")) return true;
        for (Type paramType : new ArrayList<>(sootMethod.getParameterTypes())) {
            if (TypeUtil.isCanDealType(paramType)) {
                return false;
            }
        }
        return true;
    }

    public static SootClass getSootClassByName(String className) {
        try {
            return Scene.v().getSootClass(className);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static List<FullClassType> getGenericInfoOfMethod(SootMethod sootMethod){
        List<FullClassType> result = Lists.newArrayList();
        if (sootMethod.getParameterCount() == 0) return result;

        List<Tag> tags = sootMethod.getTags();
        if (tags == null || tags.isEmpty()){
            for (Type type : new ArrayList<>(sootMethod.getParameterTypes())) {
                result.add(new FullClassType(type,""));
            }
            return result;
        }

        Tag tag = tags.get(0);
        if (!(tag instanceof SignatureTag)) return result;

        SignatureTag signatureTag = (SignatureTag) tag;
        String signature = signatureTag.getSignature();

        int i1 = signature.indexOf('(');
        int i2 = signature.indexOf(')');

        String genericInfo = signature.substring(i1+1, i2).replaceAll("/",".");

        List<Type> parameterTypes = sootMethod.getParameterTypes();
        for (Type parameterType : parameterTypes) {
            if (TypeUtil.isCollectionType(parameterType) || TypeUtil.isMapType(parameterType)) {
                String oneGenericInfo = spliceGenericInfo(genericInfo);
                result.add(new FullClassType(parameterType, oneGenericInfo));
                genericInfo = BaseUtil.substringFromStr(genericInfo, oneGenericInfo);
            } else {
                result.add(new FullClassType(parameterType, ""));
                if (!TypeUtil.isPrimType(parameterType)) {
                    genericInfo = BaseUtil.substringFromStr(genericInfo, parameterType.toString());
                }
            }
        }
        return result;
    }

    public static String spliceGenericInfo(String genericInfo){
        Deque<Integer> deque = new LinkedList<>();

        char[] chars = genericInfo.toCharArray();
        for (int i = 0; i < genericInfo.length(); ++i) {
            if (chars[i] == '<'){
                deque.push(i);
            } else if (chars[i] == '>') {
                if (!deque.isEmpty()) {
                    Integer pop = deque.pop();
                    if (deque.isEmpty()) {
                        return genericInfo.substring(pop + 1, i - 1);
                    }
                }
            }
        }
        return "";
    }

    private static List<String> spliceGenericInfo2(String genericInfo, String parameterName){
        List<String> genericList = Lists.newArrayList();
        char[] chars = genericInfo.toCharArray();
        int left = -1;
        int right = -1;
        for (int i = 0; i < genericInfo.length(); ++i) {
            if (chars[i] == '<'){
                left = i;
            } else if (chars[i] == '>' && left > -1) {
                right = i;
                String oneGeneric = genericInfo.substring(left + 1, right - 1);
                if (oneGeneric.startsWith("L")) {
                    oneGeneric = oneGeneric.substring(1);
                }
                oneGeneric = oneGeneric.replaceAll("/", ".");
                genericList.add(oneGeneric);
                left = -1;
            }
        }
        return genericList;
    }

    public static SootMethod findSetSootMethod(String sootClassName, String setMethodName){
        SootClass sootClass = Scene.v().getSootClass(sootClassName);
        if (sootClass == null) return null;
        HashSet<SootMethod> sootMethods = new HashSet<>(sootClass.getMethods());
        for (SootMethod sootMethod : sootMethods) {
            if (sootMethod.getName().equals(setMethodName) && sootMethod.getParameterCount() == 1) {
                return sootMethod;
            }
        }
        return null;
    }

    public static boolean isInheritedFromGivenClass(SootClass sootClass, String classNameUnderMatch){
        if (sootClass == null || classNameUnderMatch == null || classNameUnderMatch.isEmpty()) {
            return false;
        }
        List<Boolean> booleans = Lists.newArrayList();
        booleans.add(false);
        inheritedFromGivenClass(sootClass, classNameUnderMatch, booleans);
        return booleans.get(0);
    }

    private static void inheritedFromGivenClass(SootClass sootClass, String classNameUnderMatch, List<Boolean> booleans) {
        if (sootClass.getName().equals(classNameUnderMatch)) {
            booleans.set(0,true);
            return;
        }else if (sootClass.getInterfaces() != null) {
            ArrayList<SootClass> interfaces = new ArrayList<>(sootClass.getInterfaces());
            if (!interfaces.isEmpty()) {
                for (SootClass anInterface : interfaces) {
                    inheritedFromGivenClass(anInterface, classNameUnderMatch, booleans);
                }
            }

        } else if (sootClass.hasSuperclass()) {
            isInheritedFromGivenClass(sootClass.getSuperclass(), classNameUnderMatch);
        }
    }
}