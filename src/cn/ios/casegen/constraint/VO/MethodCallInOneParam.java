package cn.ios.casegen.constraint.VO;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2021-09-03 15:55
 **/

public class MethodCallInOneParam {
    private String methodName = "";
    private List<String> paramList = Lists.newArrayList();
    private int arrayIndex = Integer.MAX_VALUE;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<String> getParamList() {
        return paramList;
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
