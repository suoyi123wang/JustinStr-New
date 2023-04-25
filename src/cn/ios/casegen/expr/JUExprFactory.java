package cn.ios.casegen.expr;

import cn.ios.casegen.constraint.DTO.PossibleParamValueDTO;
import cn.ios.casegen.expr.define.JUDefineExpr;
import cn.ios.casegen.expr.define.constant.array.JUArrayConstantExpr;
import cn.ios.casegen.expr.define.constant.prim.*;
import cn.ios.casegen.expr.define.constant.string.JUStringConstantExpr;
import cn.ios.casegen.expr.define.newexpr.JUNewExprImpl;
import cn.ios.casegen.expr.invoke.JUInvokeExpr;
import cn.ios.casegen.expr.invoke.impl.JUDynamicInvokeExprImpl;
import cn.ios.casegen.expr.invoke.impl.JUStaticInvokeExprImpl;
import cn.ios.casegen.util.TypeUtil;
import cn.ios.casegen.variable.FullClassType;
import cn.ios.casegen.variable.JUVariable;
import soot.SootMethod;
import soot.Type;

public class JUExprFactory {
	public static JUInvokeExpr createInvokeExpr(SootMethod method, JUVariable caller, FullClassType fullClassType) {
		if (method.isStatic()){
			return new JUStaticInvokeExprImpl(method,fullClassType);
		} else {
			return JUDynamicInvokeExprImpl.v(caller, method, fullClassType);
		}
	}

	public static JUDefineExpr createDefineExpr(PossibleParamValueDTO possibleParamValueDTO, FullClassType fullClassType){
		Type type = fullClassType.getType();
		if (TypeUtil.isPrimType(type)) {
			if (TypeUtil.isBooleanType(type)) {
				return new JUBooleanConstantExpr(possibleParamValueDTO);
			} else if (TypeUtil.isByteType(type)) {
				return new JUByteConstantExpr(possibleParamValueDTO);
			} else if (TypeUtil.isCharType(type)) {
				return new JUCharConstantExpr(possibleParamValueDTO);
			} else if (TypeUtil.isDoubleType(type)) {
				return new JUDoubleConstantExpr(possibleParamValueDTO);
			} else if (TypeUtil.isFloatType(type)) {
				return new JUFloatConstantExpr(possibleParamValueDTO);
			} else if (TypeUtil.isLongType(type)) {
				return new JULongConstantExpr(possibleParamValueDTO);
			} else if (TypeUtil.isIntType(type)) {
				return new JUIntConstantExpr(possibleParamValueDTO);
			} else {
				// (BaseUtil.isShortType(type))
				return new JUShortConstantExpr(possibleParamValueDTO);
			}
		} else if (TypeUtil.isStringType(type)) {
			return new JUStringConstantExpr(possibleParamValueDTO);
		} else if (TypeUtil.isArrayType(type)) {
			return new JUArrayConstantExpr(fullClassType, possibleParamValueDTO);
		} else {
			return new JUNewExprImpl(fullClassType, possibleParamValueDTO);
		}
	}
}