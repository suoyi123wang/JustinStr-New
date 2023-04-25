package cn.ios.casegen.expr.invoke.impl;

import cn.ios.casegen.expr.invoke.JUInvokeExpr;
import cn.ios.casegen.variable.FullClassType;
import cn.ios.casegen.variable.JUVariable;
import cn.ios.casegen.variable.JUVariableFactory;
import soot.SootMethod;

public class JUDynamicInvokeExprImpl extends JUInvokeExpr {

	private JUVariable caller = null;

	public JUDynamicInvokeExprImpl(JUVariable caller, SootMethod method, FullClassType callerFullClassType) {
		super(method, caller.getVariableType());
		this.caller = caller;
	}

	public JUVariable getCaller() {
		return caller;
	}

	@Override
	public String toString() {
		return caller + "." + super.toString();
	}

	public static JUDynamicInvokeExprImpl v(JUVariable caller, SootMethod method, FullClassType fullClassType) {
		if (caller == null) {
			String name = JUVariableFactory.getName(fullClassType.getType());
			caller = new JUVariable(name, fullClassType);
		}
		return new JUDynamicInvokeExprImpl(caller, method, fullClassType);
	}
}
