package cn.ios.casegen.constraint.DTO;

import com.google.common.collect.Lists;
import soot.Type;

import java.util.List;

/**
 * @description: One parameter corresponds to many ParamConstraintInfo classes
 * @author: wangmiaomiao
 * @create: 2021-10-11 10:36
 **/
public class ParamConstraintDTO {
    private int paramIndex = -1;
    private Type paramType = null;

    // 一条完整的API组合，API参数可能有多种情况
    private List<MethodCallDTO> methodCallList = Lists.newArrayList();

    /* 对于string类型的参数,compareValue 只代表一个值; 对于基本类型来说,以#分隔,代表多个取值 */
    private String compareValue = "";
    private String operator = "";

    private boolean hasMemberField = false;
    private String fieldName = "";
    private Type fieldType = null;


    // 正在处理函数参数
    private boolean isDealingMethodArg = false;
    // 正在处理函数调用者
    private boolean isDealingMethodCaller = false;
    private boolean paramAsMethodCaller = true;


    public int getParamIndex() {
        return paramIndex;
    }

    public void setParamIndex(int paramIndex) {
        this.paramIndex = paramIndex;
    }

    public List<MethodCallDTO> getMethodCallList() {
        return methodCallList == null ? Lists.newArrayList() : methodCallList;
    }

    public void setMethodCallList(List<MethodCallDTO> methodCallList) {
        this.methodCallList = methodCallList;
    }

    public Type getParamType() {
        return paramType;
    }

    public void setParamType(Type paramType) {
        this.paramType = paramType;
    }

    public String getCompareValue() {
        return compareValue;
    }

    public void setCompareValue(String compareValue) {
        this.compareValue = compareValue;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public boolean isParamAsMethodCaller() {
        return paramAsMethodCaller;
    }

    public void setParamAsMethodCaller(boolean paramAsMethodCaller) {
        this.paramAsMethodCaller = paramAsMethodCaller;
    }

    public boolean isDealingMethodCaller() {
        return isDealingMethodCaller;
    }

    public void setDealingMethodCaller(boolean dealingMethodCaller) {
        isDealingMethodCaller = dealingMethodCaller;
    }

    public boolean isDealingMethodArg() {
        return isDealingMethodArg;
    }

    public void setDealingMethodArg(boolean dealingMethodArg) {
        isDealingMethodArg = dealingMethodArg;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public boolean isHasMemberField() {
        return hasMemberField;
    }

    public void setHasMemberField(boolean hasMemberField) {
        this.hasMemberField = hasMemberField;
    }

    public Type getFieldType() {
        return fieldType;
    }

    public void setFieldType(Type fieldType) {
        this.fieldType = fieldType;
    }
}
