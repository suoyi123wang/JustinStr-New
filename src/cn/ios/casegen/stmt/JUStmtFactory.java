package cn.ios.casegen.stmt;

import cn.ios.casegen.constraint.DTO.PossibleParamValueDTO;
import cn.ios.casegen.stmt.impl.JUDefinitionStmt;
import cn.ios.casegen.stmt.impl.JUInvokeStmt;
import cn.ios.casegen.variable.FullClassType;
import cn.ios.casegen.variable.JUVariable;
import soot.SootMethod;

import java.util.Map;

public class JUStmtFactory {

	public static JUInvokeStmt createInvokeStmt(Map<Integer, PossibleParamValueDTO> possibleValues,
												SootMethod method, JUVariable caller, FullClassType fullClassType) {
		return new JUInvokeStmt(possibleValues, method, caller, fullClassType);
	}


	public static JUDefinitionStmt createDefinitionStmt(PossibleParamValueDTO possibleParamValueDTO,
														JUVariable JUVariable, JUStmt stmt) {
		return new JUDefinitionStmt(possibleParamValueDTO, JUVariable, stmt);
	}
}
