package cn.ios.casegen.enums;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2022-01-06 16:54
 **/

public enum StringTypeEnum {
    STRING("java.lang.String");

    private String stringTypeName;

    StringTypeEnum(String stringTypeName) {
        this.stringTypeName = stringTypeName;
    }

    public String getStringTypeName() {
        return stringTypeName;
    }

    public void setStringTypeName(String stringTypeName) {
        this.stringTypeName = stringTypeName;
    }
}