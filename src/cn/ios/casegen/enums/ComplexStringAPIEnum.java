package cn.ios.casegen.enums;

public enum ComplexStringAPIEnum {
    GETCHARS("getChars"),
    GETBYTES("getBytes"),
    SUBSTRING("substring"),
    SUBSEQUENCE("subSequence"),
    CONCAT("concat"),
    REPLACE("replace"),
    REPLACEFIRST("replaceFirst"),
    REPLACEALL("replaceAll"),
    SPLIT("split"),
    JOIN("join"),
    TOLOWERCASE("toLowerCase"),
    TOUPPERCASE("toUpperCase"),
    TRIM("trim"),
    TOSTRING("toString"),
    TOCHARARRAY("toCharArray"),
    FORMAT("format"),
    VALUEOF("valueOf"),
    COPYVALUEOF("copyValueOf");

    private String methodName;

    ComplexStringAPIEnum(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public static boolean isComplexAPI(String methodName){
        for (ComplexStringAPIEnum complexAPI : values()) {
            if (complexAPI.getMethodName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    public static ComplexStringAPIEnum getByValue(String methodName){
        for(ComplexStringAPIEnum complexAPIs : values()){
            if (complexAPIs.getMethodName().equals(methodName)) {
                return complexAPIs;
            }
        }
        return null;
    }

}
