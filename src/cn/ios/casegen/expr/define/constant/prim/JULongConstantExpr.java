package cn.ios.casegen.expr.define.constant.prim;

import cn.ios.casegen.constraint.DTO.PossibleParamValueDTO;
import cn.ios.casegen.util.random.MagicValues;
import cn.ios.casegen.expr.define.constant.JUConstantExpr;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2022-01-14 19:45
 **/

public class JULongConstantExpr extends JUConstantExpr {
    private PossibleParamValueDTO possibleParamValueDTO = null;

    public JULongConstantExpr(PossibleParamValueDTO possibleParamValueDTO) {
        super();
        this.possibleParamValueDTO = possibleParamValueDTO;
    }

    @Override
    protected String createValue() {
        if (possibleParamValueDTO != null && !possibleParamValueDTO.getPossibleValue().isEmpty()) {
            return possibleParamValueDTO.getPossibleValue() + "L";
        }
        return MagicValues.getFloat() + "L";
    }
}
