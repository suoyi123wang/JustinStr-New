package cn.ios.casegen.constraint.DTO;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2022-01-05 10:24
 **/

public class SimpleMethodCallDTO {
    private String methodName;
    private List<String> paramList = Lists.newArrayList();
    private int arrayIndex = Integer.MAX_VALUE;


    public SimpleMethodCallDTO(String methodName, List<String> paramList, int arrayIndex) {
        this.methodName = methodName;
        this.paramList = paramList;
        this.arrayIndex = arrayIndex;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<String> getParamList() {
        return paramList == null ? Lists.newArrayList() : paramList;
    }

    public void setParamList(List<String> paramList) {
        this.paramList = paramList;
    }

    public int getArrayIndex() {
        return arrayIndex;
    }

    public void setArrayIndex(int arrayIndex) {
        this.arrayIndex = arrayIndex;
    }
}