package cn.ios.casegen.constraint.DTO;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2021-10-11 10:45
 **/

public class MethodCallDTO {
    private String methodName = "";
    // 获取约束的时候用这个变量
    private Map<Integer,List<String>> paramMap = Maps.newHashMap();
    private int currentMethodArgIndex = -1;
    private int arrayIndex = Integer.MAX_VALUE;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Map<Integer, List<String>> getParamMap() {
        return paramMap == null? Maps.newHashMap() : paramMap;
    }

    public void setParamMap(Map<Integer, List<String>> paramMap) {
        this.paramMap = paramMap;
    }

    public int getArrayIndex() {
        return arrayIndex;
    }

    public void setArrayIndex(int arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

    public int getCurrentMethodArgIndex() {
        return currentMethodArgIndex;
    }

    public void setCurrentMethodArgIndex(int currentMethodArgIndex) {
        this.currentMethodArgIndex = currentMethodArgIndex;
    }
}
