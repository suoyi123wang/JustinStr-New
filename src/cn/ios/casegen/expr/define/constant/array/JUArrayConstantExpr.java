package cn.ios.casegen.expr.define.constant.array;

import cn.ios.casegen.config.GlobalCons;
import cn.ios.casegen.constraint.DTO.PossibleParamValueDTO;
import cn.ios.casegen.expr.JUExprFactory;
import cn.ios.casegen.expr.define.JUDefineExpr;
import cn.ios.casegen.expr.define.constant.JUConstantExpr;
import cn.ios.casegen.util.ListUtil;
import cn.ios.casegen.util.random.RandomUtil;
import cn.ios.casegen.util.StringUtil;
import cn.ios.casegen.variable.FullClassType;
import com.google.common.collect.Lists;
import soot.ArrayType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2022-01-16 17:48
 **/

public class JUArrayConstantExpr extends JUConstantExpr {
    private ArrayType arrayType = null;
    FullClassType baseFullClassType = null;
    private List<JUDefineExpr> parameters = Lists.newArrayList();
    private List<Integer> dimensionsLength = Lists.newArrayList();

    public JUArrayConstantExpr(FullClassType arrayFullClassType, PossibleParamValueDTO possibleParamValueDTO) {
        if (!(arrayFullClassType.getType() instanceof ArrayType)) {
            throw new IllegalArgumentException("Exception in JUArrayConstantExpr.init");
        }
        this.arrayType = (ArrayType) arrayFullClassType.getType();
        this.baseFullClassType = new FullClassType(this.arrayType.baseType, "");
        this.dimensionsLength = createDimensionLength(possibleParamValueDTO);
        List<Integer> dimensionsLength = this.dimensionsLength;
        createArrayValue(dimensionsLength);
    }

    private List<Integer> createDimensionLength(PossibleParamValueDTO possibleParamValueDTO){
        List<Integer> dimensionSizeList = Lists.newArrayList();
        // 目前只会限制数组最外层的长度
        int i = 0;
        if (possibleParamValueDTO != null && !possibleParamValueDTO.getPossibleValue().isEmpty()) {
            try {
                String size = possibleParamValueDTO.getPossibleValue();
                dimensionSizeList.add(Integer.parseInt(size));
                i = 1;
            } catch (NumberFormatException ignored) { }
        }
        for (; i < arrayType.numDimensions; i++) {
            dimensionSizeList.add(RandomUtil.nextInt(GlobalCons.ARRAY_MIN_SIZE, GlobalCons.ARRAY_MAX_SIZE));
        }
        return dimensionSizeList;
    }

    private void createArrayValue(List<Integer> dimensionsLength){
        int size = 1;
        for (Integer integer : dimensionsLength) {
            size *= integer;
        }
        for (int i = 0; i < size; i++) {
            this.parameters.add(JUExprFactory.createDefineExpr(null, baseFullClassType));
        }
    }

    private String arrayValueToString(){
        ArrayList<Integer> cloneDimensionList = new ArrayList<>(dimensionsLength);
        if (cloneDimensionList.size() == 1) {
            return StringUtil.splicingParameterBrace(parameters,0, parameters.size());
        } else {
            List<String> toSplicing = parameters.stream().map(JUDefineExpr::toString).collect(Collectors.toList());
            while (cloneDimensionList.size() > 0) {
                int groupLength = ListUtil.getLastElement(cloneDimensionList);
                List<String> temp= Lists.newArrayList();
                for (int i = 0; i < toSplicing.size();){
                    temp.add(StringUtil.splicingParameterBrace(toSplicing, 0, groupLength));
                    i += groupLength;
                }
                toSplicing = temp;
                ListUtil.removeLastElement(cloneDimensionList);
            }

            // at last, toSplicing.size() == 1
            return toSplicing.get(0);
        }
    }

    @Override
    public String toString() {
        return arrayValueToString();
    }

    @Override
    protected String createValue() {
        // do nothing
        return null;
    }
}
