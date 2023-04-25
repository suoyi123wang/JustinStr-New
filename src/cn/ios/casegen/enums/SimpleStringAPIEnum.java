package cn.ios.casegen.enums;

public enum SimpleStringAPIEnum {
    LENGTH("length"),
    ISEMPTY("isEmpty"),
    CHARAT("charAt"),
    CODEPOINTAT("codePointAt"),
    CODEPOINTBEFORE("codePointBefore"),
    CODERPOINTCOUNT("codePointCount"),
    OFFSETBYCODEPOINTS("offsetByCodePoints"),
    EQUALS("equals"),
    COTENTEQUALS("contentEquals"),
    EQUALSIGNORECASE("equalsIgnoreCase"),
    COMPARETO("compareTo"),
    COMPARETOIGNORECASE("compareToIgnoreCase"),
    REGIONMATCHES("regionMatches"),
    STARTSWITH("startsWith"),
    ENDSWITH("endsWith"),
    HASHCODE("hashCode"),
    INDEXOF("indexOf"),
    LASTINDEXOF("lastIndexOf"),
    MATCHES("matches"),
    CONTAINS("contains");

    private String methodName;

    SimpleStringAPIEnum(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public static boolean isSimpleAPI(String methodName){
        for (SimpleStringAPIEnum simpleAPI : SimpleStringAPIEnum.values()) {
            if (simpleAPI.getMethodName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    public static SimpleStringAPIEnum getByValue(String methodName){
        for(SimpleStringAPIEnum simpleAPIs : values()){
            if (simpleAPIs.getMethodName().equals(methodName)) {
                return simpleAPIs;
            }
        }
        return null;
    }
}
