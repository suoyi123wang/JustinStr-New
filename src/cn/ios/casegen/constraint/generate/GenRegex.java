package cn.ios.casegen.constraint.generate;

import cn.ios.casegen.constraint.DTO.ParamConstraintDTO;
import cn.ios.casegen.constraint.DTO.SimpleMethodCallDTO;
import cn.ios.casegen.constraint.DTO.MethodCallDTO;
import cn.ios.casegen.enums.ComplexStringAPIEnum;
import cn.ios.casegen.enums.SimpleStringAPIEnum;
import cn.ios.casegen.util.BaseUtil;
import cn.ios.casegen.util.TypeUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import soot.Type;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2021-09-06 17:22
 **/
public class GenRegex {
    public static Set<String>  genRegexByParamConstraintInfo(ParamConstraintDTO paramConstraintInfo) {
        Set<String> paramRegexSet = Sets.newHashSet();
        if (paramConstraintInfo == null) return paramRegexSet;
        String compareValue = paramConstraintInfo.getCompareValue();
        Type paramType = paramConstraintInfo.getParamType();
        Type fieldType = paramConstraintInfo.getFieldType();

        List<String> suitableParamList = Lists.newArrayList();

        if ( (paramType== null || !paramType.toString().equals("java.lang.String")) &&
                (fieldType == null || !fieldType.toString().equals("java.lang.String")) ) {
            suitableParamList = paramConstraintInfo.isHasMemberField()?
                    getSuitableValuesForBasicTypes(compareValue, fieldType) : getSuitableValuesForBasicTypes(compareValue, paramType);
            if (!suitableParamList.isEmpty()){
                paramRegexSet.addAll(suitableParamList);
            }
        } else {
            List<MethodCallDTO> methodCallInfoList = paramConstraintInfo.getMethodCallList();
            List<List<SimpleMethodCallDTO>> simpleMethodCallLists = transferToSimpleMethodCall(methodCallInfoList);
            if (simpleMethodCallLists == null) return paramRegexSet;
            for (List<SimpleMethodCallDTO> simpleMethodCallList : simpleMethodCallLists) {
                // 一个参数可能有多个值
                suitableParamList = getRegexForString(compareValue,simpleMethodCallList);

                if (!suitableParamList.isEmpty()){
                    paramRegexSet.addAll(suitableParamList);
                }
            }
        }

        return paramRegexSet;
    }

    /**
     * 不是通过举特例的方式生成正则
     * @param simpleMethodCallList 一个paramConstraintInfo可以生成多个regex
     * @return 考虑到length，比如length>2, 会生成{2,},{3,},{2},{0,1},{0,2}，所以是list
     *
     * compareValue, List<methodCallName, List<param.toSTring>>
     */
    private static List<String> getRegexForString(String compareValue, List<SimpleMethodCallDTO> simpleMethodCallList){
        List<String> result = Lists.newArrayList();
        if (simpleMethodCallList == null || simpleMethodCallList.isEmpty()) {
            return result;
        }

        String anyLiteral = "";
        String[] regexArray = new String[]{"","","","",""};
        List<String[]> regexArrayList = Lists.newArrayList();

        for (SimpleMethodCallDTO simpleMethodCall : simpleMethodCallList) {
            String methodName = simpleMethodCall.getMethodName();
            List<String> paramList = simpleMethodCall.getParamList();
            int arrayIndex = simpleMethodCall.getArrayIndex();

            if (SimpleStringAPIEnum.isSimpleAPI(methodName)){
                List<String> regexFromSimpleMethod = getRegexFromSimpleMethod(methodName, paramList, compareValue, anyLiteral);
                for (String regex : regexFromSimpleMethod) {
                    if (regexArrayList.isEmpty()) {
                        result.add(regex);
                    }
                    for (String[] eachRegexArray : regexArrayList) {
                        result.add(eachRegexArray[0] + regex + eachRegexArray[2]);
                    }
                }
            } else if (ComplexStringAPIEnum.isComplexAPI(methodName)) {
                List<String[]> regexListFromComplexMethod = getRegexFromComplexMethod(methodName, paramList, compareValue, arrayIndex, regexArray[3]);
                Iterator<String[]> iterator = regexListFromComplexMethod.iterator();
                while (iterator.hasNext()) {
                    String[] regexFromComplexMethod = iterator.next();
                    anyLiteral = regexFromComplexMethod[3];
                    if (!regexFromComplexMethod[4].isEmpty()) {
                        // 单独生成正则，不合并
                        result.add(regexFromComplexMethod[0] + regexFromComplexMethod[1] + regexFromComplexMethod[2]);
                        iterator.remove();
                    }
                }
                regexArrayList = combineRegex(regexArrayList, regexListFromComplexMethod);
            }
        }

        // 特例：str.substring(2,5).endsWith(":")
        if (simpleMethodCallList.size() == 2 &&
                (simpleMethodCallList.get(0).getMethodName().equals("substring") || simpleMethodCallList.get(0).getMethodName().equals("subSequence")) &&
                simpleMethodCallList.get(1).getMethodName().equals("endsWith") &&
                simpleMethodCallList.get(0).getParamList().size() == 2) {
            int index = Integer.parseInt(simpleMethodCallList.get(0).getParamList().get(1)) - simpleMethodCallList.get(1).getParamList().get(0).length();
            result.add("[\\s\\S]{" + index + "}" + Pattern.quote(simpleMethodCallList.get(1).getParamList().get(0)) + "[\\s\\S]*");
        }

        // 每个String都要有""
        result.add(" ");
        return result;
    }

    /**
     * 对于不是String类型的参数的约束,生成合适的值
     * @param compareValue
     * @param type
     * @return
     */
    private static List<String> getSuitableValuesForBasicTypes(String compareValue, Type type) {
        List<String> result = Lists.newArrayList();
        if (compareValue.isEmpty()) {
            return result;
        }
        try {
            if (TypeUtil.isPrimType(type)) {
                if (TypeUtil.isBooleanType(type)) {
                    result.add("true");
                    result.add("false");
                } else if (TypeUtil.isIntType(type)) {
                    int intBound = Integer.parseInt(compareValue);
                    result.add(String.valueOf(intBound));
                    result.add(String.valueOf(intBound + 1));
                    result.add(String.valueOf(intBound - 1));
                    result.add("0");
                    result.add(String.valueOf(Integer.MAX_VALUE));
                    result.add(String.valueOf(Integer.MIN_VALUE));
                } else if (TypeUtil.isShortType(type)) {
                    short shortBound = Short.parseShort(compareValue);
                    result.add(String.valueOf(shortBound));
                    result.add(String.valueOf(shortBound + 1));
                    result.add(String.valueOf(shortBound - 1));
                    result.add(String.valueOf(Short.MAX_VALUE));
                    result.add(String.valueOf(Short.MIN_VALUE));
                } else if (TypeUtil.isLongType(type)) {
                    if (compareValue.endsWith("L")) {
                        compareValue = compareValue.split("L")[0];
                    }
                    long longBound = Long.parseLong(compareValue);
                    result.add(String.valueOf(longBound));
                    result.add(String.valueOf(longBound + 1));
                    result.add(String.valueOf(longBound - 1));
                    result.add(String.valueOf(Long.MAX_VALUE));
                    result.add(String.valueOf(Long.MIN_VALUE));
                } else if (TypeUtil.isCharType(type)) {
                    if (BaseUtil.isNumeric(compareValue)) {
                        int i = Integer.parseInt(compareValue);
                        result.add(String.valueOf((char) i));
                        result.add(String.valueOf((char) (i + 1)));
                        result.add(String.valueOf((char) (i - 1)));
                        result.add(String.valueOf(Character.MAX_VALUE));
                        result.add(String.valueOf(Character.MIN_VALUE));
                    }
                } else if (TypeUtil.isFloatType(type)) {
                    float floatBound = Float.parseFloat(compareValue);
                    result.add(String.valueOf(floatBound));
                    result.add(String.valueOf(floatBound + 0.1));
                    result.add(String.valueOf(floatBound - 0.1));
                    result.add(String.valueOf(Float.MAX_VALUE));
                    result.add(String.valueOf(Float.MIN_VALUE));
                } else if (TypeUtil.isDoubleType(type)) {
                    double doubleBound = Double.parseDouble(compareValue);
                    result.add(String.valueOf(doubleBound));
                    result.add(String.valueOf(doubleBound + 0.01));
                    result.add(String.valueOf(doubleBound - 0.01));
                    result.add(String.valueOf(Double.MAX_VALUE));
                    result.add(String.valueOf(Double.MIN_VALUE));
                } else if (TypeUtil.isByteType(type)) {
                    byte byteBound = Byte.parseByte(compareValue);
                    result.add(String.valueOf(byteBound));
                    result.add(String.valueOf(byteBound + 1));
                    result.add(String.valueOf(byteBound - 1));
                    result.add(String.valueOf(Byte.MAX_VALUE));
                    result.add(String.valueOf(Byte.MIN_VALUE));
                }
            } else {
                if (compareValue.equals("null")) {
                    result.add("null");
                    return result;
                } else if (!BaseUtil.isNumeric(compareValue) || Integer.parseInt(compareValue) < 0) {
                    return result;
                }

                if (TypeUtil.isArrayType(type) || TypeUtil.isCollectionType(type)) {
                    int length = Integer.parseInt(compareValue);
                    result.add(String.valueOf(length));
                    result.add(String.valueOf(length + 1));
                    if (length - 1 > 0) {
                        result.add(String.valueOf(length - 1));
                    }
                    result.add(String.valueOf(0));
                }
            }
        } catch (Exception e) {
//            System.out.println("exception In getSuitableValuesFromParamConstraintInfo: " + e.toString());
        }
       return result;
    }

    /**
     * 不是举特例的方式对SimpleMethod生成正则
     * @param methodName 这些api的返回值都是int,boolean,char
     * @param paraList
     * @param compareValue
     * @param anyLiteral 当api是split时，anyLiteral不再单纯是[\s\S]
     * @return
     */
    private static List<String> getRegexFromSimpleMethod(String methodName, List<String> paraList, String compareValue, String anyLiteral){
        List<String> regexList = Lists.newArrayList();
        if (anyLiteral.isEmpty()) {
            anyLiteral = "[\\s\\S]";
        }
        StringBuilder regex = new StringBuilder();
        try {
            switch (Objects.requireNonNull(SimpleStringAPIEnum.getByValue(methodName))){
                case ISEMPTY:
                    regexList.add("");
                    regexList.add("[\\s\\S]+");
                    break;
                case EQUALS:
                case COTENTEQUALS:
                case COMPARETO:
                case COMPARETOIGNORECASE:
                    regex = new StringBuilder();
                    regex.append(Pattern.quote(paraList.get(0)));
                    regexList.add(regex.toString());

                    regex = new StringBuilder();
                    regex.append("[^(").append(Pattern.quote(paraList.get(0))).append(")]{").append(paraList.get(0).length()).append("}");
                    regexList.add(regex.toString());

                    regex = new StringBuilder();
                    regex.append("[^(").append(Pattern.quote(paraList.get(0))).append(")]{*}");
                    regexList.add(regex.toString());
                    break;
                case EQUALSIGNORECASE:
                    regex = new StringBuilder();
                    regex.append("(?i)").append(Pattern.quote(paraList.get(0)));
                    regexList.add(regex.toString());

                    regex = new StringBuilder();
                    regex.append(Pattern.quote(paraList.get(0)));
                    regexList.add(regex.toString());

                    regex = new StringBuilder();
                    regex.append("[^(").append(Pattern.quote(paraList.get(0))).append(")]{*}");
                    regexList.add(regex.toString());
                    break;
                case REGIONMATCHES:
                    regex = new StringBuilder();
                    int offset = Integer.parseInt(paraList.get(2));
                    int len = Integer.parseInt(paraList.get(3));
                    String str = Pattern.quote(paraList.get(1).substring(offset, offset + len));
                    if (paraList.size() == 4) {
                        regex.append(anyLiteral).append("{").append(paraList.get(0)).append("}").
                                append(str).append(anyLiteral).append("*");
                    } else if (paraList.size() == 5) {
                        regex.append(anyLiteral).append("{").append(paraList.get(0)).append("}").
                                append("(?)(").append(str).append(")").append(anyLiteral).append("*");
                    }
                    regexList.add(regex.toString());
                    break;
                case STARTSWITH:
                    if (paraList.size() == 1){
                        regex = new StringBuilder();
                        regex.append(Pattern.quote(paraList.get(0))).append(anyLiteral).append("*");
                        regexList.add(regex.toString());

                        regex = new StringBuilder();
                        regex.append(anyLiteral).append("*");
                        regexList.add(regex.toString());
                    } else if (paraList.size() == 2) {
                        regex = new StringBuilder();
                        regex.append(anyLiteral).append("{").append(paraList.get(1)).append("}").
                                append(Pattern.quote(paraList.get(0))).append(anyLiteral).append("*");
                        regexList.add(regex.toString());

                        regex = new StringBuilder();
                        int startLen1 = Integer.parseInt(paraList.get(1)) - 1;
                        regex.append(anyLiteral).append("{").append(startLen1).append("}").
                                append(Pattern.quote(paraList.get(0))).append(anyLiteral).append("*");
                        regexList.add(regex.toString());

                        regex = new StringBuilder();
                        int startLen2 = Integer.parseInt(paraList.get(1)) + 1;
                        regex.append(anyLiteral).append("{").append(startLen2).append("}").
                                append(Pattern.quote(paraList.get(0))).append(anyLiteral).append("*");
                        regexList.add(regex.toString());

                        regex = new StringBuilder();
                        regex.append(anyLiteral).append("*");
                        regexList.add(regex.toString());
                    }
                    break;
                case ENDSWITH:
                    regex = new StringBuilder();
                    regex.append(anyLiteral).append("*").append(Pattern.quote(paraList.get(0)));
                    regexList.add(regex.toString());

                    regex = new StringBuilder();
                    regex.append(anyLiteral).append("*");
                    regexList.add(regex.toString());
                    break;
                case CONTAINS:
                    regex = new StringBuilder();
                    regex.append(Pattern.quote(paraList.get(0))).append(anyLiteral).append("*");
                    regexList.add(regex.toString());

                    regex = new StringBuilder();
                    regex.append(anyLiteral).append("*").append(Pattern.quote(paraList.get(0))).append(anyLiteral).append("*");
                    regexList.add(regex.toString());

                    regex = new StringBuilder();
                    regex.append(anyLiteral).append("*");
                    regexList.add(regex.toString());
                    break;
                case LENGTH:
                    // ==
                    regex.append(anyLiteral).append("{").append(compareValue).append("}");
                    regexList.add(regex.toString());
                    // >
                    regex = new StringBuilder();
                    int compareLength = Integer.parseInt(compareValue) + 1;
                    regex.append(anyLiteral).append("{").append(compareLength).append(",}");
                    regexList.add(regex.toString());
                    // >=
                    regex = new StringBuilder();
                    regex.append(anyLiteral).append("{").append(compareValue).append(",}");
                    regexList.add(regex.toString());
                    // <
                    regex = new StringBuilder();
                    compareLength = Integer.parseInt(compareValue) - 1;
                    regex.append(anyLiteral).append("{0,").append(compareLength).append("}");
                    regexList.add(regex.toString());
                    // <=
                    regex = new StringBuilder();
                    regex.append(anyLiteral).append("{0,").append(compareValue).append("}");
                    regexList.add(regex.toString());
                    break;
                case INDEXOF:
                    String para0 = paraList.get(0);
                    if (BaseUtil.isNumeric(para0)){
                        para0 = String.valueOf((char) Integer.parseInt(paraList.get(0)));
                    }
                    para0 = Pattern.quote(para0);
                    if (paraList.size() == 1){
                        regex.append("[^(").append(para0).append(")]{").append(compareValue).
                                append("}").append(para0).append(anyLiteral).append("*");
                        regexList.add(regex.toString());

                        // 故意不满足条件，进入到else分支
                        regex = new StringBuilder();
                        int indexLen1 = Integer.parseInt(compareValue) + 1;
                        regex.append("[^(").append(para0).append(")]{").append(indexLen1).
                                append("}").append(para0).append(anyLiteral).append("*");
                        regexList.add(regex.toString());

                        regex = new StringBuilder();
                        int indexLen2 = Integer.parseInt(compareValue) - 1;
                        regex.append("[^(").append(para0).append(")]{").append(indexLen2).
                                append("}").append(para0).append(anyLiteral).append("*");
                        regexList.add(regex.toString());
                    } else if (paraList.size() == 2) {
                        Integer integer = Integer.parseInt(compareValue) - Integer.parseInt(paraList.get(1));
                        regex.append(anyLiteral).append("{").append(paraList.get(1)).append("}").append("[^(").
                                append(para0).append(")]{").append(integer).append("}").
                                append(para0).append(anyLiteral).append("*");
                        regexList.add(regex.toString());

                        // 故意不满足条件，进入到else分支
                        regex = new StringBuilder();
                        int indexLen1 = integer + 1;
                        regex.append(anyLiteral).append("{").append(paraList.get(1)).append("}").append("[^(").
                                append(para0).append(")]{").append(indexLen1).append("}").append(para0).append(anyLiteral).append("*");
                        regexList.add(regex.toString());

                        regex = new StringBuilder();
                        int indexLen2 = integer - 1;
                        regex.append(anyLiteral).append("{").append(paraList.get(1)).append("}").append("[^(").
                                append(para0).append(")]{").append(indexLen2).append("}").append(para0).append(anyLiteral).append("*");
                        regexList.add(regex.toString());
                    }
                    break;
                case LASTINDEXOF:
                    String firstPara = paraList.get(0);
                    if (BaseUtil.isNumeric(firstPara)){
                        firstPara = String.valueOf((char) Integer.parseInt(paraList.get(0)));
                    }
                    firstPara = Pattern.quote(firstPara);
                    if (paraList.size() == 1){
                        regex.append(anyLiteral).append("{").append(compareValue).append("}").append(firstPara).
                                append("[^(").append(firstPara).append(")]*");
                        regexList.add(regex.toString());

                        // 故意不满足条件，进入到else分支
                        regex = new StringBuilder();
                        int lastIndexLen1 = Integer.parseInt(compareValue) + 1;
                        regex.append(anyLiteral).append("{").append(lastIndexLen1).append("}").append(firstPara).
                                append("[^(").append(firstPara).append(")]*");
                        regexList.add(regex.toString());

                        regex = new StringBuilder();
                        int lastIndexLen2 = Integer.parseInt(compareValue) - 1;
                        regex.append(anyLiteral).append("{").append(lastIndexLen2).append("}").append(firstPara).
                                append("[^(").append(firstPara).append(")]*");
                        regexList.add(regex.toString());

                    } else if (paraList.size() == 2){
                        Integer integer = Integer.parseInt(paraList.get(1)) - Integer.parseInt(compareValue);
                        regex.append("[^(").append(firstPara).append(")]{").append(compareValue).append("}").
                                append(firstPara).append("[^(").append(firstPara).append(")]{").
                                append(integer).append("}").append(anyLiteral).append("*");
                        regexList.add(regex.toString());

                        // 故意不满足条件，进入到else分支
                        regex = new StringBuilder();
                        int lastIndexLen1 = Integer.parseInt(compareValue) + 1;
                        regex.append("[^(").append(firstPara).append(")]{").append(lastIndexLen1).append("}").
                                append(firstPara).append("[^(").append(firstPara).append(")]{").
                                append(integer).append("}").append(anyLiteral).append("*");
                        regexList.add(regex.toString());

                        regex = new StringBuilder();
                        int lastIndexLen2 = Integer.parseInt(compareValue) - 1;
                        regex.append("[^(").append(firstPara).append(")]{").append(lastIndexLen2).append("}").
                                append(firstPara).append("[^(").append(firstPara).append(")]{").
                                append(integer).append("}").append(anyLiteral).append("*");
                        regexList.add(regex.toString());
                    }
                    break;
                case CHARAT:
                case CODEPOINTAT:
                case CODEPOINTBEFORE:
                    if (BaseUtil.isNumeric(compareValue)){
                        compareValue = String.valueOf((char) Integer.parseInt(compareValue));
                    }
                    String firstParam = methodName.equals("codePointBefore")? String.valueOf(Integer.parseInt(paraList.get(0)) - 1) : paraList.get(0);

                    regex.append(anyLiteral).append("{").append(firstParam).append("}").append(Pattern.quote(compareValue)).append(anyLiteral).append("*");
                    regexList.add(regex.toString());

                    // 长度小于charAt的长度
                    regex = new StringBuilder();
                    regex.append(anyLiteral).append("{").append(firstParam).append("}");
                    regexList.add(regex.toString());


                    regex = new StringBuilder();
                    int charAtLen1 = Integer.parseInt(firstParam) + 1;
                    regex.append(anyLiteral).append("{").append(charAtLen1).append("}").append(Pattern.quote(compareValue)).append(anyLiteral).append("*");
                    regexList.add(regex.toString());

                    regex = new StringBuilder();
                    int charAtLen2 = Integer.parseInt(firstParam) - 1;
                    regex.append(anyLiteral).append("{").append(charAtLen2).append("}").append(Pattern.quote(compareValue)).append(anyLiteral).append("*");
                    regexList.add(regex.toString());

                    break;
                case MATCHES:
                    regexList.add(paraList.get(0));
                    break;
            }
        } catch (Exception exception){
            return regexList;
        }
        return regexList;
    }

    /**
     * 不是举特例的方式对ComplexMethod生成正则
     * @param methodName
     * @param paraList
     * @param compareValue 主要用于getBytes这个api
     * @param arrayIndex 数组下标
     * @param anyLiteral
     * @return List<String[]> 根据一个api可以生成多个不同的正则
     * 对于每一个正则，都以String[]呈现
     * String[0] + String[1] + String[2] 是一个完整的正则
     * String[3] 表示接下来的anyLiteral是啥，若为空，则认为是[\s\S]
     * String[4] ComplexMethod后面肯定会调用其他函数，后面调用函数的正则会放在string[1]中，但是如果!String[4].isEmpty(),就不会合并后面函数的正则，单独生成一条regex
     */
    private static List<String[]> getRegexFromComplexMethod(String methodName, List<String> paraList, String compareValue, int arrayIndex, String anyLiteral){
        List<String[]> result = Lists.newArrayList();

        // toLowerCase / toUpperCase / trim  默认返回为空，目前生成的正则也可以满足
        String[] regexArray = new String[]{"","","","",""};
        result.add(regexArray);

        if (anyLiteral.isEmpty()) {
            anyLiteral = "[\\s\\S]";
        }
        try {
            switch (Objects.requireNonNull(ComplexStringAPIEnum.getByValue(methodName))){
                case SUBSTRING:
                case SUBSEQUENCE:
                    // paraList.size() == 1
                    String[] regexArray0 = new String[]{"","","","",""};
                    regexArray0[0] = anyLiteral + "{" + paraList.get(0) + "}";
                    result.add(regexArray0);

                    // paraList.size() == 2
                    String[] regexArray1 = Arrays.copyOf(regexArray0, 5);
                    if (paraList.size() == 2) {
                        int i = Integer.parseInt(paraList.get(1)) - Integer.parseInt(paraList.get(0));
                        regexArray1[2] = anyLiteral + "{" + i + ",}";
                        result.add(regexArray1);
                    }

                    // 故意进入else或exception
                    String[] regexArray2 = Arrays.copyOf(regexArray0, 5);
                    int len = Integer.parseInt(paraList.get(0)) - 1;
                    regexArray2[0] = anyLiteral + "{" + len + "}";
                    regexArray2[4] = "true";
                    result.add(regexArray2);
                    break;
                case SPLIT:
                    if (arrayIndex == Integer.MAX_VALUE){
                        return result;
                    }

                    String[] regexArray3 = new String[]{"","","","",""};
                    int i = 0;
                    while (i < arrayIndex){
                        regexArray3[0] = regexArray3[0] + "[^(" + Pattern.quote(paraList.get(0)) + ")]*" + Pattern.quote(paraList.get(0));
                        i++;
                    }
                    regexArray3[2] = Pattern.quote(paraList.get(0)) + anyLiteral + "*";
                    regexArray3[3] = "[^(" + Pattern.quote(paraList.get(0)) + ")]";
                    result.add(regexArray3);

                    // 故意进入else或exception
                    String[] regexArray4 = Arrays.copyOf(regexArray3, 5);
                    regexArray4[0] = regexArray3[0] + "[^(" + Pattern.quote(paraList.get(0)) + ")]*" + Pattern.quote(paraList.get(0));
                    regexArray4[4] = "true";
                    result.add(regexArray4);

                    // 导致数组越界
                    String[] regexArray5 = Arrays.copyOf(regexArray3, 5);
                    regexArray5[0] = regexArray3[0];
                    regexArray5[1] = "";
                    regexArray5[2] = "";
                    regexArray5[4] = "true";
                    result.add(regexArray5);
                    break;
                case REPLACEFIRST:
                case REPLACEALL:
                case REPLACE:
                    String[] regexArray6 = new String[]{"","","","",""};
                    regexArray6[3] = "[^(" + Pattern.quote(paraList.get(0)) + ")]";
                    result.add(regexArray6);

                    String[] regexArray7 = new String[]{"","","","",""};
                    regexArray7[3] = "[^(" + Pattern.quote(paraList.get(1)) + ")]";
                    result.add(regexArray7);
                    break;
                case GETBYTES:
                case TOCHARARRAY:
                    // 处理str.getBytes()[1] == 109 这种类似的形式
                    if (arrayIndex == Integer.MAX_VALUE){
                        return result;
                    }
                    if (BaseUtil.isNumeric(compareValue)){
                        compareValue = String.valueOf((char) Integer.parseInt(compareValue));
                    }

                    String[] regexArray8 = new String[]{"","","","",""};
                    regexArray8[0] = anyLiteral + "{" + arrayIndex + "}" + compareValue + anyLiteral + "*";
                    regexArray8[4] = "true";
                    result.add(regexArray8);

                    String[] regexArray9 = Arrays.copyOf(regexArray8, 5);
                    int getBytesLen1 = arrayIndex + 1;
                    regexArray9[0] = anyLiteral + "{" + getBytesLen1 + "}" + compareValue + anyLiteral + "*";
                    result.add(regexArray9);

                    String[] regexArray10 = Arrays.copyOf(regexArray8, 5);
                    int getBytesLen2 = arrayIndex - 1;
                    regexArray10[0] = anyLiteral + "{" + getBytesLen2 + "}" + compareValue + anyLiteral + "*";
                    result.add(regexArray10);
                    break;
            }
        } catch (Exception exception) {
            return result;
        }

        return result;
    }

    /**
     * @param methodCallCopyList
     * one MethodCall ----> List<SimpleMethodCall>
     * @return List<SimpleMethodCall> is one complete method call with complete param
     */
    private static List<List<SimpleMethodCallDTO>> transferToSimpleMethodCall(List<MethodCallDTO> methodCallCopyList){
        List<List<SimpleMethodCallDTO>> sameMethodCallLists = Lists.newArrayList();
        for (MethodCallDTO methodCallCopy : methodCallCopyList) {
            List<SimpleMethodCallDTO> sameMethodCallList = Lists.newArrayList();

            // for each method call API
            Map<Integer, List<String>> paramMap = methodCallCopy.getParamMap();
            String methodName = methodCallCopy.getMethodName();
            int arrayIndex = methodCallCopy.getArrayIndex();

            if (paramMap.isEmpty()) {
                SimpleMethodCallDTO simpleMethodCall = new SimpleMethodCallDTO(methodName, Lists.newArrayList(), arrayIndex);
                sameMethodCallList.add(simpleMethodCall);
            } else {
                //内层的List<String>是一个参数的所有可能取值情况
                List<List<String>> temp = Lists.newArrayList();
                for (int i = 0; i < paramMap.size(); i++) {
                    if (!paramMap.containsKey(i)) {
                        return null;
                    }
                    temp.add(paramMap.get(i));
                }

                if (!temp.isEmpty()) {
                    // 这个API的参数的所有组合情况  内层的List<String> 是这个API的参数的一种组合情况
                    List<List<String>> paramLists = Lists.cartesianProduct(temp);
                    for (List<String> paramList : paramLists) {
                        SimpleMethodCallDTO simpleMethodCall = new SimpleMethodCallDTO(methodName, paramList, arrayIndex);
                        sameMethodCallList.add(simpleMethodCall);
                    }
                }
            }
            if (!sameMethodCallList.isEmpty()) {
                sameMethodCallLists.add(sameMethodCallList);
            }
        }

        return Lists.cartesianProduct(sameMethodCallLists);
    }

    private static List<String[]> combineRegex(List<String[]> oldRegexArrayList, List<String[]> newRegexArrayList) {
        List<String[]> result = Lists.newArrayList();
        if (oldRegexArrayList.isEmpty()) {
            result.addAll(newRegexArrayList);
        }
        for (String[] newRegexArray : newRegexArrayList) {
            for (String[] oldRegexArray : oldRegexArrayList) {
                String[] combineRegexArray = Arrays.copyOf(oldRegexArray, 5);
                combineRegexArray[0] = combineRegexArray[0] + newRegexArray[0];
                combineRegexArray[1] = newRegexArray[1];
                combineRegexArray[2] = newRegexArray[2] + combineRegexArray[2];
                result.add(combineRegexArray);
            }
        }
        return result;

    }

}
