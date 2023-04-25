//package cn.ios.junit.zother.path;
//
//import cn.ios.junit.config.Config;
//import cn.ios.junit.util.ConstraintUtil;
//import cn.ios.junit.util.log.Log;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import com.google.common.collect.Sets;
//import soot.*;
//import soot.jimple.*;
//import soot.jimple.internal.JArrayRef;
//import soot.jimple.internal.JVirtualInvokeExpr;
//import soot.toolkits.graph.BriefUnitGraph;
//import soot.toolkits.graph.UnitGraph;
//import soot.toolkits.scalar.SimpleLocalDefs;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//
///**
// * @description: TODO
// * @author: wangmiaomiao
// * @create: 2021-08-28 15:02
// **/
//
//public class ConstraintsFromPath {
//    /**
//     * 将每一条路径转换成也Constraints
//     * @return
//     */
//    public static Map<String, Map<String, Set<Map<Integer, Set<ParamConstraintInfo>>>>>  getConstraintsFromPaths(){
//        Map<String, Map<String, Set<Map<Integer, Set<ParamConstraintInfo>>>>> constraintsForAllClasses = Maps.newHashMap();
//        for (SootClass sootClass : Scene.v().getApplicationClasses()) {
//            if (sootClass.isInterface()) {
//                continue;
//            }
//            Map<String, Set<Map<Integer, Set<ParamConstraintInfo>>>> constraintsForEachClass = Maps.newHashMap();
//            for (SootMethod sootMethod : sootClass.getMethods()) {
//                try {
//                    if (sootMethod.isAbstract() || sootMethod.isPrivate() ||
//                            sootMethod.getName().contains("<init>") || sootMethod.getName().contains("<clinit>")) {
//                        continue;
//                    }
//
//                    Body body = sootMethod.getActiveBody();
//                    if (body == null) {
//                        Log.e(sootMethod.getSignature() + " : body is null");
//                        continue;
//                    }
//
//                    if (body.getMethod().getParameterTypes().stream().anyMatch(type -> type.toString().equals("java.lang.String"))){
//                        List<List<Unit>> allPathInMethod = CallSequence.findPathsInMethod(new BriefUnitGraph(body));
//                        if (!allPathInMethod.isEmpty()){
//                            Set<Map<Integer, Set<ParamConstraintInfo>>> constrainsForEachMethod = getConstrainsFromPathsInMethod(allPathInMethod, new BriefUnitGraph(body));
//                            if (!constrainsForEachMethod.isEmpty()){
//                                String signature = ConstraintUtil.getSignatureFromSootToReflect(sootMethod);
//                                constraintsForEachClass.put(signature,constrainsForEachMethod);
//                            }
//                        }
//                    }
//                } catch (Exception | Error exception ){
//                    System.out.println("Exception | Error:::::" + exception.getMessage());
//                }
//            }
//            if (!constraintsForEachClass.isEmpty()){
//                constraintsForAllClasses.put(sootClass.getName(),constraintsForEachClass);
//            }
//        }
//        return constraintsForAllClasses;
//    }
//
//    private static Set<Map<Integer, Set<ParamConstraintInfo>>> getConstrainsFromPathsInMethod(List<List<Unit>> allPathsInMethod, UnitGraph unitGraph) throws InterruptedException {
//        Set<Map<Integer,Set<ParamConstraintInfo>>> constraintInOneMethod = Sets.newHashSet();
//        for (List<Unit> onePath : allPathsInMethod) {
//            Map<Integer, Set<ParamConstraintInfo>> constraintInOnePath = Maps.newHashMap();
//            List<Unit> currentPath = Lists.newArrayList();
//            for (Unit unit : onePath) {
//                currentPath.add(unit);
//                if (unit instanceof IfStmt) {
//                    // Jimple里面 一个If对应一个ParamConstraintInfo
//                    try {
//                        ParamConstraintInfo paramConstraintInfo = new ParamConstraintInfo();
//                        dealIfStmt(unitGraph, (IfStmt) unit, currentPath, onePath, paramConstraintInfo);
//
//                        // deal result
//                        Set<ParamConstraintInfo> paramConstraintInfoList = constraintInOnePath.get(paramConstraintInfo.getParamIndex());
//                        if (paramConstraintInfoList == null) {
//                            paramConstraintInfoList = Sets.newHashSet();
//                        }
//                        if (paramConstraintInfo.getParamIndex() != -1 && !paramConstraintInfo.getOperator().isEmpty() && !paramConstraintInfo.getCompareValue().isEmpty()) {
//                            List<MethodCallInOneParam> methodCallInfoList = ConstraintUtil.removeLastElement(paramConstraintInfo.getMethodCallInfoList());
//                            paramConstraintInfo.setMethodCallInfoList(methodCallInfoList);
//
//                            paramConstraintInfoList.add(paramConstraintInfo);
//                            constraintInOnePath.put(paramConstraintInfo.getParamIndex(), paramConstraintInfoList);
//                        }
//                    } catch (StackOverflowError stackOverflowError){
//                        System.out.println("stackOverflowError");
//                    }
//
//                }
//            }
//            if (!constraintInOnePath.isEmpty()){
//                constraintInOneMethod.add(constraintInOnePath);
//            }
//        }
//        return constraintInOneMethod;
//    }
//
//    private static void dealIfStmt(UnitGraph unitGraph, IfStmt ifStmt, List<Unit> currentPath, List<Unit> completePath, ParamConstraintInfo paramConstraintInfo){
//        Value conditionExpr = ifStmt.getCondition();
//        Value leftValue = ((BinopExpr) conditionExpr).getOp1();
//        Value rightValue = ((BinopExpr) conditionExpr).getOp2();
//
//        // dealLeft
//        if (leftValue instanceof Local){
//            traceLocalInPath(unitGraph, ifStmt,(Local) leftValue,currentPath, paramConstraintInfo);
//        }
//
//        // dealRight compareValue
//        if (rightValue instanceof Constant){
//            paramConstraintInfo.setCompareValue(rightValue.toString());
//        }
//        // TODO ForExample??
////        else if(rightValue instanceof Local){
////            traceLocalInPath(unitGraph,ifStmt,(Local) rightValue,currentPath, paramConstraintInfo);
////        }
//
//        // deal operation
//        // 如果if的goto语句恰好是路径的下一条语句，operation取原来的，否则operation取反
//        dealOperator(ifStmt,completePath,paramConstraintInfo);
//    }
//
//    private static void traceLocalInPath(UnitGraph unitGraph,IfStmt ifStmt, Local localTemp, List<Unit> currentPath, ParamConstraintInfo paramConstraintInfo){
//        List<Unit> defsOfOp = new SimpleLocalDefs(unitGraph).getDefsOfAt(localTemp, ifStmt);
//        for (int i = currentPath.size() - 1; i>= 0; i--) {
//            // 找最近的定义语句
//            if (defsOfOp.contains(currentPath.get(i))){
//                Unit defineUnit = currentPath.get(i);
//                if (defineUnit instanceof IdentityStmt){
//                    dealIdentityStmt((IdentityStmt) defineUnit,paramConstraintInfo);
//                } else if (defineUnit instanceof AssignStmt){
//                    dealAssignStmt(unitGraph, ifStmt, currentPath, (AssignStmt) defineUnit, paramConstraintInfo);
//                }
//                break;
//            }
//        }
//    }
//
//    private static void dealIdentityStmt(IdentityStmt identityStmt, ParamConstraintInfo paramConstraintInfo){
//        Value rightOp = identityStmt.getRightOp();
//        if (rightOp instanceof ParameterRef) {
//            // TODO deal other type
//            if (rightOp.getType().toString().equals("java.lang.String")){
//                int paraIndex = ((ParameterRef) rightOp).getIndex();
//                paramConstraintInfo.setParamIndex(paraIndex);
//            }
//        }
//    }
//
//    private static void dealAssignStmt(UnitGraph unitGraph, IfStmt ifStmt, List<Unit> currentPath, AssignStmt assignStmt, ParamConstraintInfo paramConstraintInfo){
//        Value expr = assignStmt.getRightOp();
//
//        if (expr instanceof JVirtualInvokeExpr){
//            dealVirtualInvokeExpr((JVirtualInvokeExpr) expr, unitGraph, ifStmt, currentPath, paramConstraintInfo);
//        } else if (expr instanceof JArrayRef){
//            Value base = ((JArrayRef) expr).getBase();
//            Value index = ((JArrayRef) expr).getIndex();
//            // 下面两个if的顺序不能变
//            if (index instanceof Constant){
//                // 数组下标应该放在MethodCallInOneParam里面
//                MethodCallInOneParam currentMethodCall = getCurrentMethodCall(paramConstraintInfo);
//                currentMethodCall.setArrayIndex(Integer.parseInt(index.toString()));
//
//                List<MethodCallInOneParam> methodCallInfoList = paramConstraintInfo.getMethodCallInfoList();
//                methodCallInfoList.add(currentMethodCall);
//                paramConstraintInfo.setMethodCallInfoList(methodCallInfoList);
//            }
//            if (base instanceof Local){
//                traceLocalInPath(unitGraph, ifStmt, (Local)base, currentPath, paramConstraintInfo);
//            }
//        } else if (expr instanceof Constant){
//            // 是函数参数
//            MethodCallInOneParam currentMethodCall = getCurrentMethodCall(paramConstraintInfo);
//            List<String> paramList = currentMethodCall.getParamList();
//            if (expr instanceof StringConstant){
//                paramList.add(((StringConstant) expr).value);
//            } else {
//                paramList.add(expr.toString());
//            }
//
//            currentMethodCall.setParamList(paramList);
//            List<MethodCallInOneParam> methodCallInfoList = paramConstraintInfo.getMethodCallInfoList();
//            methodCallInfoList.add(currentMethodCall);
//            paramConstraintInfo.setMethodCallInfoList(methodCallInfoList);
//        }
//    }
//
//    private static void dealVirtualInvokeExpr(JVirtualInvokeExpr jVirtualInvokeExpr, UnitGraph unitGraph, IfStmt ifStmt, List<Unit> currentPath, ParamConstraintInfo paramConstraintInfo){
//        String methodSignature = jVirtualInvokeExpr.getMethod().getName();
//        Value methodCaller = jVirtualInvokeExpr.getBase();
//        List<Value> methodArgs = jVirtualInvokeExpr.getArgs();
//
//        // dealMethodSignature
//        MethodCallInOneParam currentMethodCall = getCurrentMethodCall(paramConstraintInfo);
//        currentMethodCall.setMethodName(methodSignature);
//        List<MethodCallInOneParam> methodCallInfoList = paramConstraintInfo.getMethodCallInfoList();
//        methodCallInfoList.add(currentMethodCall);
//        paramConstraintInfo.setMethodCallInfoList(methodCallInfoList);
//
//        // dealMethodArgs
//        dealMethodArgs(methodArgs, unitGraph, ifStmt, currentPath, paramConstraintInfo);
//
//        // dealMethodCaller
//        if (methodCaller instanceof Local){
//            traceLocalInPath(unitGraph, ifStmt, (Local) methodCaller, currentPath, paramConstraintInfo);
//        }
//    }
//
//    private static void dealOperator(IfStmt ifStmt,List<Unit> completePath, ParamConstraintInfo paramConstraintInfo){
//        for (int i = 0; i< completePath.size(); ++i){
//            if (ifStmt.equals(completePath.get(i))){
//                Value condition = ifStmt.getCondition();
//                if (ifStmt.getTarget().equals(completePath.get(i+1))){
//                    paramConstraintInfo.setOperator(getOperator(condition,true));
//                } else {
//                    paramConstraintInfo.setOperator(getOperator(condition,false));
//                }
//                break;
//            }
//        }
//    }
//
//    private static String getOperator(Value conditionExpr,boolean flag){
//        if (conditionExpr instanceof NeExpr) {
//            return flag? Config.NE_EXPR : Config.EQ_EXPR;
//        } else if (conditionExpr instanceof EqExpr) {
//            return flag? Config.EQ_EXPR : Config.NE_EXPR;
//        } else if (conditionExpr instanceof GeExpr) {
//            return flag? Config.GE_EXPR : Config.LT_EXPR;
//        } else if (conditionExpr instanceof GtExpr) {
//            return flag? Config.GT_EXPR : Config.LE_EXPR;
//        } else if (conditionExpr instanceof LeExpr) {
//            return flag? Config.LE_EXPR : Config.GT_EXPR;
//        } else if (conditionExpr instanceof LtExpr) {
//            return flag? Config.LT_EXPR : Config.GE_EXPR;
//        }
//        return null;
//    }
//
//    // dealMethodArgs
//    private static void dealMethodArgs(List<Value> methodArgs,UnitGraph unitGraph, IfStmt ifStmt, List<Unit> currentPath, ParamConstraintInfo paramConstraintInfo){
//        for (Value methodArg : methodArgs) {
//            if (methodArg instanceof Local) {
//                paramConstraintInfo.setDealingMethodArg(true);
//                traceLocalInPath(unitGraph,ifStmt,(Local) methodArg,currentPath,paramConstraintInfo);
//            } else if (methodArg instanceof Constant) {
//                MethodCallInOneParam currentMethodCall = getCurrentMethodCall(paramConstraintInfo);
//
//                List<String> paramList = currentMethodCall.getParamList();
//                if (methodArg instanceof StringConstant){
//                    paramList.add(((StringConstant) methodArg).value);
//                } else {
//                    paramList.add(methodArg.toString());
//                }
//                currentMethodCall.setParamList(paramList);
//
//                List<MethodCallInOneParam> methodCallInfoList = paramConstraintInfo.getMethodCallInfoList();
//                methodCallInfoList.add(currentMethodCall);
//                paramConstraintInfo.setMethodCallInfoList(methodCallInfoList);
//            }
//        }
//
//        // 需要添加一个空的currentMethod
//        List<MethodCallInOneParam> methodCallInfoList = paramConstraintInfo.getMethodCallInfoList();
//        methodCallInfoList.add(new MethodCallInOneParam());
//        paramConstraintInfo.setMethodCallInfoList(methodCallInfoList);
//    }
//
//    // 帮助取出paramConstraintInfo 当前处理的方法，并从methodCallInfoList中除去当前的方法
//    private static MethodCallInOneParam getCurrentMethodCall(ParamConstraintInfo paramConstraintInfo){
//        List<MethodCallInOneParam> methodCallInfoList = paramConstraintInfo.getMethodCallInfoList();
//        MethodCallInOneParam currentMethodCall = ConstraintUtil.getLastElement(methodCallInfoList) == null ? new MethodCallInOneParam() : ConstraintUtil.getLastElement(methodCallInfoList);
//        List<MethodCallInOneParam> methodCallInOneParams = ConstraintUtil.removeLastElement(methodCallInfoList);
//        methodCallInfoList = methodCallInOneParams == null ? Lists.newArrayList() : methodCallInOneParams;
//
//        paramConstraintInfo.setMethodCallInfoList(methodCallInfoList);
//        return currentMethodCall;
//    }
//
//}
