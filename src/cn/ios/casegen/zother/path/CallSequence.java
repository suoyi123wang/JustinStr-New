//package cn.ios.junit.zother.path;
//
//import cn.ios.junit.util.log.Log;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import soot.*;
//import soot.jimple.IfStmt;
//import soot.jimple.internal.JAssignStmt;
//import soot.jimple.internal.JGotoStmt;
//import soot.jimple.internal.JIfStmt;
//import soot.jimple.internal.JInvokeStmt;
//import soot.toolkits.graph.BriefUnitGraph;
//import soot.toolkits.graph.UnitGraph;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Stack;
//import java.util.stream.Collectors;
//
///**
// * @description: 是为了找到api组合的特例，才会输出path
// * 这里的路径是一条完整的执行路径，但是并不需要完整的执行路径，因为不可能根据他们生成regex，变数太大；
// * @author: wangmiaomiao
// * @create: 2021-09-01 19:47
// **/
//
//public class CallSequence {
//    public static Map<String, Map<String, List<List<Unit>>>> getAllPathsFromSoot(){
//        Map<String,Map<String, List<List<Unit>>>> pathsForAllClasses = Maps.newHashMap();
//        for (SootClass sootClass : Scene.v().getApplicationClasses()) {
//            try {
//                if (sootClass.isInterface()) {
//                    continue;
//                }
//                Map<String, List<List<Unit>>> methodResultMap = Maps.newHashMap();
//                for (SootMethod sootMethod : sootClass.getMethods()) {
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
//                        List<List<Unit>> allPath = findPathsInMethod(new BriefUnitGraph(body));
//                        if (!allPath.isEmpty()){
//                            methodResultMap.put(sootMethod.getSignature(),allPath);
//                        }
//                    }
//                }
//                if (!methodResultMap.isEmpty()){
//                    pathsForAllClasses.put(sootClass.getName(),methodResultMap);
//                }
//            }catch (Exception e){
//                e.printStackTrace();
//            }
//        }
//        return pathsForAllClasses;
//    }
//
//    /**
//     * 对每一个方法，都寻找可能的路径
//     * @param unitGraph
//     * @return
//     */
//    public static List<List<Unit>> findPathsInMethod(UnitGraph unitGraph){
//        Stack<Unit> pathStack = new Stack<>();
//        Stack<Unit> succsOfStack = new Stack<>();
//        List<Unit> heads = unitGraph.getHeads();
//        List<List<Unit>> pathResult = Lists.newArrayList();
//        for (Unit head : heads) {
//            try {
//                // TODO this should be fix
//                pathStack.clear();
//                succsOfStack.clear();
//                pathStack.push(head);
//                tracePath(unitGraph,pathStack,succsOfStack,pathResult);
//            } catch (StackOverflowError stackOverflowError){
//                System.out.println("StackOverflowError happened:" + stackOverflowError.getMessage());
//            }
//        }
//        // 每条路径只保留与String相关的API
//        List<List<Unit>> oldPaths = filterPath(pathResult);
//        return InterceptPath(oldPaths);
//    }
//
//    private static void tracePath(UnitGraph unitGraph, Stack<Unit> pathStack, Stack<Unit> succsOfStack, List<List<Unit>> pathResult){
//        List<Unit> succsOf = unitGraph.getSuccsOf(pathStack.peek());
//        boolean flag = true;
//        for (Unit unit : succsOf) {
//            if (!judgeLoop(unit,pathStack)){
//                succsOfStack.push(unit);
//                flag = false;
//            }
//        }
//        if (succsOfStack.size() == 0){
//            // already find all path
//            List<Unit> onePath = Lists.newArrayList();
//            onePath.addAll(pathStack);
//            pathResult.add(onePath);
//            pathStack.clear();
//            succsOfStack.clear();
//        } else {
//            // do not add new unit to succsStack
//            if (flag){
//                if (succsOf.size() == 0){
//                    // find one path
//                    List<Unit> onePath = Lists.newArrayList();
//                    onePath.addAll(pathStack);
//                    pathResult.add(onePath);
//                }
//                pathStack.pop();
//                while (unitGraph.getSuccsOf(pathStack.peek()).stream().noneMatch(unit -> unit.equals(succsOfStack.peek()))){
//                    pathStack.pop();
//                }
//            }
//            pathStack.push(succsOfStack.pop());
//            tracePath(unitGraph,pathStack,succsOfStack, pathResult);
//        }
//    }
//
//    private static boolean judgeLoop(Unit succsOfUnit, Stack<Unit> pathStack){
//        Unit targetUnit = null;
//        if (succsOfUnit instanceof JGotoStmt){
//            targetUnit = ((JGotoStmt) succsOfUnit).getTarget();
//            return pathStack.contains(targetUnit);
//        } else if (succsOfUnit instanceof JIfStmt){
//            targetUnit = ((JIfStmt) succsOfUnit).getTarget();
//            return pathStack.contains(targetUnit);
//        }
//        return false;
//    }
//
//    private static List<List<Unit>> filterPath(List<List<Unit>> allPath){
//        List<List<Unit>> allFilterPath = Lists.newArrayList();
//        for (List<Unit> onePath : allPath) {
//            List<Unit> oneFilterPath = Lists.newArrayList();
//            // filterEachUnit
//            filterEachUnit(oneFilterPath,onePath);
//            // one filter path is the result
//            allFilterPath.add(oneFilterPath);
//        }
//
//
//        return allFilterPath;
//    }
//
//    private static void filterEachUnit(List<Unit> oneFilterPath,List<Unit> onePath){
//        for (Unit unit : onePath) {
//            if (judgeUnitCanBeAdded(unit)) {
//                oneFilterPath.add(unit);
//            }
//        }
//    }
//
//    private static boolean judgeUnitCanBeAdded(Unit toBeAddedUnit){
//        if (toBeAddedUnit instanceof JAssignStmt){
//            if ((((JAssignStmt) toBeAddedUnit).containsInvokeExpr())){
//                return ((JAssignStmt) toBeAddedUnit).getInvokeExpr().
//                        getMethodRef().getDeclaringClass().getName().equals("java.lang.String");
//
//            }
//        } else if (toBeAddedUnit instanceof JInvokeStmt){
//            return ((JInvokeStmt) toBeAddedUnit).getInvokeExpr().getMethodRef().
//                    getDeclaringClass().getName().equals("java.lang.String");
//        }
//        return true;
//    }
//
//    // path中必须有if语句，并且最后一条if语句后面只保留一个unit
//    private static List<List<Unit>> InterceptPath(List<List<Unit>> oldPaths){
//        List<List<Unit>> newPaths = Lists.newArrayList();
//        for (List<Unit> oldPath : oldPaths) {
//            if (oldPath.stream().anyMatch(unit -> unit instanceof IfStmt)){
//                // 去掉最后一个if后面多余的语句
//                for (int i = oldPath.size() - 1 ; i >= 0; i--){
//                    if (oldPath.get(i) instanceof IfStmt) {
//                        if (i == oldPath.size() - 1){
//                            newPaths.add(oldPath);
//                        } else {
//                            List<Unit> collect = oldPath.stream().limit(i + 2).collect(Collectors.toList());
//                            newPaths.add(collect);
//                        }
//                        break;
//                    }
//                }
//            }
//        }
//        return newPaths;
//    }
//}
