package cn.ios.casegen.expr.define;

import cn.ios.casegen.expr.JUExpr;
import cn.ios.casegen.variable.JUVariable;
import soot.SootClass;

import java.util.List;
import java.util.Set;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2022-01-16 15:20
 **/

public abstract class JUDefineExpr implements JUExpr {
    @Override
    public List<JUVariable> getDependentVariableList() {
        return null;
    }

    @Override
    public int getArgCount() {
        return 0;
    }

    @Override
    public JUVariable getCaller() {
        return null;
    }

    @Override
    public Set<SootClass> getExceptions() {
        return null;
    }
}
