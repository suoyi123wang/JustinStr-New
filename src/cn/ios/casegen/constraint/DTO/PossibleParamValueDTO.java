package cn.ios.casegen.constraint.DTO;

import com.google.common.collect.Maps;
import soot.Type;

import java.util.Map;

/**
 * @description: 在生成测试用例时用到
 * @author: wangmiaomiao
 * @create: 2022-01-11 14:18
 **/

public class PossibleParamValueDTO {
    int paramIndex;
    String possibleValue;
    Type paramType;
    Map<String, String> possibleFieldValueMap = Maps.newHashMap();

    public PossibleParamValueDTO(int paramIndex, Type paramType, String possibleValue) {
        this.paramIndex = paramIndex;
        this.paramType = paramType;
        this.possibleValue = possibleValue;
    }

    public PossibleParamValueDTO(int paramIndex, Type paramType, Map<String, String> possibleFieldValueMap) {
        this.paramIndex = paramIndex;
        this.paramType = paramType;
        this.possibleFieldValueMap = possibleFieldValueMap;
    }

    public int getParamIndex() {
        return paramIndex;
    }

    public void setParamIndex(int paramIndex) {
        this.paramIndex = paramIndex;
    }

    public String getPossibleValue() {
        return possibleValue;
    }

    public void setPossibleValue(String possibleValue) {
        this.possibleValue = possibleValue;
    }

    public Map<String, String> getPossibleFieldValueMap() {
        return possibleFieldValueMap;
    }

    public void setPossibleFieldValueMap(Map<String, String> possibleFieldValueMap) {
        this.possibleFieldValueMap = possibleFieldValueMap;
    }

    public Type getParamType() {
        return paramType;
    }

    public void setParamType(Type paramType) {
        this.paramType = paramType;
    }
}
