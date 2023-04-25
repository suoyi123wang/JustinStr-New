package cn.ios.casegen.variable;

import cn.ios.casegen.config.GlobalCons;
import cn.ios.casegen.util.BaseUtil;
import cn.ios.casegen.util.ClassUtil;
import soot.SootClass;
import soot.Type;

public class JUVariableFactory {
	public static String getName(Type type) {
		SootClass sootClass = ClassUtil.getSootClassByName(type.toString());

		if (sootClass == null) {
			throw new IllegalArgumentException("Exception in VariableNameFactory.getName");
		}

		String variableName = BaseUtil.lowerCaseFirstLetter(sootClass.getShortName());

		if (type.toString().contains("[")) {
			variableName = variableName.substring(0, variableName.indexOf("["));
			variableName += "Array";
		}

		variableName += GlobalCons.VARIABLE_INDEX++;
		return variableName;
	}
}
