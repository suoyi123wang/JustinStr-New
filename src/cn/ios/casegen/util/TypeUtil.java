package cn.ios.casegen.util;

import cn.ios.casegen.config.GlobalCons;
import cn.ios.casegen.enums.ComplexStringAPIEnum;
import cn.ios.casegen.enums.GenerationEnum;
import cn.ios.casegen.enums.SimpleStringAPIEnum;
import soot.*;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2022-01-11 20:18
 **/

public class TypeUtil {

    public static boolean isPrimType(Type type){
        return type instanceof PrimType;
    }

    public static boolean isStringType(Type type){
        return type.toString().contains("java.lang.String");
    }

    public static boolean isIntType(Type type){
        return type instanceof IntType;
    }

    public static boolean isShortType(Type type){
        return type instanceof ShortType;
    }

    public static boolean isLongType(Type type){
        return type instanceof LongType;
    }

    public static boolean isFloatType(Type type){
        return type instanceof FloatType;
    }

    public static boolean isDoubleType(Type type){
        return type instanceof DoubleType;
    }

    public static boolean isByteType(Type type){
        return type instanceof ByteType;
    }

    public static boolean isCharType(Type type){
        return type instanceof CharType;
    }

    public static boolean isBooleanType(Type type){
        return type instanceof BooleanType;
    }

    public static boolean isArrayType(Type type){
        return type instanceof ArrayType;
    }

    public static boolean isCollectionType(Type type){
        SootClass sootClass = Scene.v().getSootClass(type.toString());
        return ClassUtil.isInheritedFromGivenClass(sootClass, GenerationEnum.JAVA_UTIL_COLLECTION.getValue());
    }

    public static boolean isObjectType(Type type){
        return GlobalCons.MEMBER_FIELD_INFO.containsKey(type.toString());
    }

    public static boolean isMapType(Type type){
        SootClass sootClass = Scene.v().getSootClass(type.toString());
        return ClassUtil.isInheritedFromGivenClass(sootClass, GenerationEnum.JAVA_UTIL_MAP.getValue());
    }

    public static boolean isCanDealType(Type type){
        return TypeUtil.isObjectType(type) || TypeUtil.isPrimType(type) ||
                TypeUtil.isStringType(type) || TypeUtil.isMapType(type) ||
                TypeUtil.isCollectionType(type) || TypeUtil.isArrayType(type);
    }

    public static boolean isCanDealMethod(String methodName) {
        return SimpleStringAPIEnum.isSimpleAPI(methodName) ||
                ComplexStringAPIEnum.isComplexAPI(methodName) ||
                methodName.startsWith("get");
    }
}
