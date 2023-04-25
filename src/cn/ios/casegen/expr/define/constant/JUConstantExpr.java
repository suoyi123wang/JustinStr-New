package cn.ios.casegen.expr.define.constant;

import cn.ios.casegen.expr.define.JUDefineExpr;

public abstract class JUConstantExpr extends JUDefineExpr {
    protected abstract String createValue();

    @Override
    public String toString() {
        return createValue();
    }
}


