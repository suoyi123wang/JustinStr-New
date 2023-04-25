package cn.ios.casegen.constraint.VO;

import soot.Type;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2021-12-31 11:37
 **/

public class MemberFieldVO {
    private String memberFieldName;
    private Type memberFieldType;
    private boolean publicModifier;
    private String nameOfGetMethod;
    private String nameOfSetMethod;

    public MemberFieldVO(String memberFieldName, Type memberFieldType, boolean publicModifier) {
        this.memberFieldName = memberFieldName;
        this.memberFieldType = memberFieldType;
        this.publicModifier = publicModifier;
    }

    public boolean isPublicModifier() {
        return publicModifier;
    }

    public void setPublicModifier(boolean publicModifier) {
        this.publicModifier = publicModifier;
    }

    public Type getMemberFieldType() {
        return memberFieldType;
    }

    public void setMemberFieldType(Type memberFieldType) {
        this.memberFieldType = memberFieldType;
    }

    public String getMemberFieldName() {
        return memberFieldName;
    }

    public void setMemberFieldName(String memberFieldName) {
        this.memberFieldName = memberFieldName;
    }

    public boolean hasPublicGetMethod() {
        return nameOfGetMethod != null && !nameOfGetMethod.isEmpty();
    }

    public boolean hasPublicSetMethod() {
        return nameOfSetMethod != null && !nameOfSetMethod.isEmpty();
    }

    public String getNameOfGetMethod() {
        return nameOfGetMethod;
    }

    public void setNameOfGetMethod(String nameOfGetMethod) {
        this.nameOfGetMethod = nameOfGetMethod;
    }

    public String getNameOfSetMethod() {
        return nameOfSetMethod;
    }

    public void setNameOfSetMethod(String nameOfSetMethod) {
        this.nameOfSetMethod = nameOfSetMethod;
    }
}
