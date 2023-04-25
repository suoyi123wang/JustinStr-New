package cn.ios.casegen.constraint.VO;

import cn.ios.casegen.util.TypeUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import soot.Type;

import java.util.Map;
import java.util.Set;


/**
 * @description: One parameter corresponds to one ParamConstraintVO class
 * @author: wangmiaomiao
 * @create: 2022-01-06 09:59
 **/
public class ParamConstraintVO {
    private int paramIndex = -1;
    private Type paramType = null;

    // 对于基本类型 + String + Collector:
    // 基本类型 + String 保存的是可用值; Collector类保存的是size大小
    private Set<String> possibleValuesForSimpleType = Sets.newHashSet();

    // 对于涉及成员变量的复杂对象：key是成员变量的名称， value List<String> 和上面的表示一样
    private Map<String, Set<String>> possibleValuesForObject = Maps.newHashMap();

    private int numsOfPossibleValues;

    public ParamConstraintVO(int paramIndex, Type paramType) {
        this.paramIndex = paramIndex;
        this.paramType = paramType;
    }

    public int getParamIndex() {
        return paramIndex;
    }

    public void setParamIndex(int paramIndex) {
        this.paramIndex = paramIndex;
    }

    public Type getParamType() {
        return paramType;
    }

    public void setParamType(Type paramType) {
        this.paramType = paramType;
    }

    public Set<String> getPossibleValuesForSimpleType() {
        return possibleValuesForSimpleType == null? Sets.newHashSet() : possibleValuesForSimpleType;
    }

    public void setPossibleValuesForSimpleType(Set<String> possibleValuesForSimpleType) {
        this.possibleValuesForSimpleType = possibleValuesForSimpleType;
    }

    public Map<String, Set<String>> getPossibleValuesForObject() {
        return possibleValuesForObject == null ? Maps.newHashMap() : possibleValuesForObject;
    }

    public void setPossibleValuesForObject(Map<String, Set<String>> possibleValuesForObject) {
        this.possibleValuesForObject = possibleValuesForObject;
    }

    public int getNumsOfPossibleValues() {
        return numsOfPossibleValues;
    }

    public void setNumsOfPossibleValues(int numsOfPossibleValues) {
        this.numsOfPossibleValues = numsOfPossibleValues;
    }

    public void setNumsOfPossibleValues(){
        if ((TypeUtil.isPrimType(paramType) || TypeUtil.isStringType(paramType) )
                && !possibleValuesForSimpleType.isEmpty()) {
            numsOfPossibleValues = possibleValuesForSimpleType.size();
        } else if (TypeUtil.isObjectType(paramType) && !possibleValuesForObject.isEmpty()) {
            for (Map.Entry<String, Set<String>> stringSetEntry : possibleValuesForObject.entrySet()) {
                numsOfPossibleValues = Math.max(numsOfPossibleValues, stringSetEntry.getValue().size());
            }
        }
    }
}
