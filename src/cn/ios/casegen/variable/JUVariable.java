package cn.ios.casegen.variable;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2022-01-13 15:57
 **/

public class JUVariable {
    private String variableName;
    private FullClassType variableFullType;

    public JUVariable(String variableName, FullClassType variableFullType) {
        this.variableName = variableName;
        this.variableFullType = variableFullType;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public FullClassType getVariableType() {
        return variableFullType;
    }

    public void setVariableType(FullClassType variableFullType) {
        this.variableFullType = variableFullType;
    }

    @Override
    public String toString() {
        return variableName;
    }
}
