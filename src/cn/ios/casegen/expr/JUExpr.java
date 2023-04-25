package cn.ios.casegen.expr;

import cn.ios.casegen.variable.JUVariable;
import soot.SootClass;

import java.util.List;
import java.util.Set;

public interface JUExpr {
    List<JUVariable> getDependentVariableList();

    int getArgCount();

    // this expr has variable caller for DynamicInvoke
    JUVariable getCaller();

//    SootMethod getMethod();
    Set<SootClass> getExceptions();
}
