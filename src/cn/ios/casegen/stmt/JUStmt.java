package cn.ios.casegen.stmt;

import cn.ios.casegen.expr.invoke.JUInvokeExpr;
import com.google.common.collect.Sets;
import soot.SootClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class JUStmt {
	
	protected List<JUStmt> previousStmts = new ArrayList<JUStmt>();
	protected List<JUStmt> succStmts = new ArrayList<JUStmt>();
	protected JUStmt nextStmt = null;

	public List<JUStmt> getPreviousStmts() {
		return previousStmts;
	}
	
	public List<JUStmt> getSuccStmts() {
		return succStmts;
	}

	public JUStmt getNextStmt() {
		return nextStmt;
	}

	public Set<SootClass> getExceptions(){
		return Sets.newHashSet();
	}
	
	public abstract boolean containsJUInvokeExpr();
	
	public abstract JUInvokeExpr getJUInvokeExpr();
	
}
