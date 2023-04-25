package cn.ios.casegen.expr.define.constant.string;

import cn.ios.casegen.config.GlobalCons;
import cn.ios.casegen.constraint.DTO.PossibleParamValueDTO;
import cn.ios.casegen.enums.GenerationEnum;
import cn.ios.casegen.util.random.MagicValues;
import com.github.curiousoddman.rgxgen.RgxGen;
import cn.ios.casegen.expr.define.constant.JUConstantExpr;
import com.github.curiousoddman.rgxgen.config.RgxGenProperties;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2022-01-13 16:37
 **/

public class JUStringConstantExpr extends JUConstantExpr {

    private PossibleParamValueDTO possibleParamValueDTO = null;

    public JUStringConstantExpr(PossibleParamValueDTO possibleParamValueDTO) {
        super();
        this.possibleParamValueDTO = possibleParamValueDTO;
    }

    @Override
    protected String createValue() {
        String value = MagicValues.getString();
        if (possibleParamValueDTO != null) {
            String regex = possibleParamValueDTO.getPossibleValue();
            try {
                RgxGenProperties properties = new RgxGenProperties();
                properties.put("generation.infinite.repeat", String.valueOf(GlobalCons.STRING_MAX_LENGTH));
                RgxGen rgxGen = new RgxGen(regex);
                rgxGen.setProperties(properties);
                String rgxString = rgxGen.generate();
//                value = rgxString.replaceAll(GenerationEnum.SPECIAL_REGEX_CHARS.getValue(), "" );
                value = rgxString.replaceAll(GenerationEnum.SPECIAL_REGEX_CHARS.getValue(), "" );
                if (value.length() > GlobalCons.STRING_MAX_LENGTH) {
                    value = value.substring(0, GlobalCons.STRING_MAX_LENGTH);
                }
            } catch (Exception | Error e){
//                Log.e("RgxGen exception");
//                Log.e(regex);
            }
        }
        value = value.replace("\\", "\\\\");
        value = value.replace("\"", "\\\"");
        return ("\"" + value + "\"");
    }
}
