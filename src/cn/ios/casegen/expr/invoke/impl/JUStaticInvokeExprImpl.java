package cn.ios.casegen.expr.invoke.impl;

import cn.ios.casegen.expr.invoke.JUInvokeExpr;
import cn.ios.casegen.variable.FullClassType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;

public class JUStaticInvokeExprImpl extends JUInvokeExpr {

	private SootClass callerClass = null;

	public JUStaticInvokeExprImpl(SootMethod constructorMethod, FullClassType fullClassType) {
		super(constructorMethod, fullClassType);
		this.callerClass = constructorMethod.getDeclaringClass();
	}

	@Override
	public String toString() {
		String typeInfo = ((RefType) (callerFullClassType.getType())).getSootClass().getName();
		String type = "";
		if (method.getName().contains("<init>") && typeInfo.contains("<")) {
			int start = typeInfo.indexOf("<");
			type = typeInfo.substring(start);
		}

		if (!method.isConstructor()) {
			type = typeInfo + ".";
		}
		return type.replace("$", ".") + super.toString();
	}

	public static JUStaticInvokeExprImpl v(SootMethod constructorMethod, FullClassType fullClassType) {
		return new JUStaticInvokeExprImpl(constructorMethod, fullClassType);
	}

	public SootClass getCallerClass() {
		return callerClass;
	}

}
