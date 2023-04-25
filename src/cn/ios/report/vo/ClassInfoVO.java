package cn.ios.report.vo;

/**
 * @author: wangmiaomiao
 * @create: 2022/7/22 10:58
 **/
public class ClassInfoVO {
    private String fullClassName;
    private int methodNum;
    private int testMethodNum;
    private int testCaseNum;
    private int executeTestCaseNum;
    private int successTestCaseNum;
    private int failTestCaseNum;
    private int skipTestCaseNum;
    private int executeTime;

    public String getFullClassName() {
        return fullClassName;
    }

    public void setFullClassName(String fullClassName) {
        this.fullClassName = fullClassName;
    }

    public int getMethodNum() {
        return methodNum;
    }

    public void setMethodNum(int methodNum) {
        this.methodNum = methodNum;
    }

    public int getTestMethodNum() {
        return testMethodNum;
    }

    public void setTestMethodNum(int testMethodNum) {
        this.testMethodNum = testMethodNum;
    }

    public int getTestCaseNum() {
        return testCaseNum;
    }

    public void setTestCaseNum(int testCaseNum) {
        this.testCaseNum = testCaseNum;
    }

    public int getExecuteTestCaseNum() {
        return executeTestCaseNum;
    }

    public void setExecuteTestCaseNum(int executeTestCaseNum) {
        this.executeTestCaseNum = executeTestCaseNum;
    }

    public int getSuccessTestCaseNum() {
        return successTestCaseNum;
    }

    public void setSuccessTestCaseNum(int successTestCaseNum) {
        this.successTestCaseNum = successTestCaseNum;
    }

    public int getFailTestCaseNum() {
        return failTestCaseNum;
    }

    public void setFailTestCaseNum(int failTestCaseNum) {
        this.failTestCaseNum = failTestCaseNum;
    }

    public int getSkipTestCaseNum() {
        return skipTestCaseNum;
    }

    public void setSkipTestCaseNum(int skipTestCaseNum) {
        this.skipTestCaseNum = skipTestCaseNum;
    }

    public int getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(int  executeTime) {
        this.executeTime = executeTime;
    }
}
