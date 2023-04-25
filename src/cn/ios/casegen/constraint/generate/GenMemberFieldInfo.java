package cn.ios.casegen.constraint.generate;

import cn.ios.casegen.constraint.VO.MemberFieldVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.Type;
import soot.util.Chain;

import java.util.*;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2021-12-31 13:14
 **/

public class GenMemberFieldInfo {
    /**
     * <className, memberVOList>
     */
    public static Map<String, List<MemberFieldVO>> getMemberVariableInfo(){
        Map<String, List<MemberFieldVO>> result = Maps.newHashMap();

        HashSet<SootClass> sootClasses = new HashSet<>(Scene.v().getApplicationClasses());
        for (SootClass sootClass : sootClasses) {
            if (sootClass.isInterface()) {
                continue;
            }

            String className = sootClass.getName();
            List<MemberFieldVO> memberVOList = Lists.newArrayList();

            Chain<SootField> fields = sootClass.getFields();
            for (SootField field : fields) {
                String fieldName = field.getName();
                Type type = field.getType();
                String getMethodName = "get" + upperFirstCase(fieldName);
                String setMethodName = "set" + upperFirstCase(fieldName);
                MemberFieldVO memberVO = new MemberFieldVO(fieldName, type, field.isPublic());

                try {
                    String getMethodSignature = constructGetMethodSignature(className, field);
                    if (Scene.v().getMethod(getMethodSignature).isPublic()) {
                        memberVO.setNameOfGetMethod(getMethodName);
                    }
                } catch (RuntimeException runtimeException) {
                    // there is no such get method for field
                }

                try {
                    String setMethodSignature = constructSetMethodSignature(className, field);
                    if (Scene.v().getMethod(setMethodSignature).isPublic()) {
                        memberVO.setNameOfSetMethod(setMethodName);
                    }
                } catch (RuntimeException runtimeException) {
                    // there is no such set method for field
                }

                if (memberVO.hasPublicSetMethod()) {
                    memberVOList.add(memberVO);
                }
            }

            if (!memberVOList.isEmpty()) {
                result.put(className,memberVOList);
            }
        }

        return result;
    }

    private static String constructGetMethodSignature(String className, SootField sootField){
        String type = sootField.getType().toString();
        String name = upperFirstCase(sootField.getName());

        return "<" + className + ": " + type + " get" + name + "()>";
    }

    private static String constructSetMethodSignature(String className, SootField sootField){
        String type = sootField.getType().toString();
        String name = upperFirstCase(sootField.getName());

        return "<" + className + ": void" + " set" + name + "(" + type + ")>";
    }

    private static String upperFirstCase(String name){
        char[] chars = name.toCharArray();
        chars[0] -= 32;
        return String.valueOf(chars);
    }
}
