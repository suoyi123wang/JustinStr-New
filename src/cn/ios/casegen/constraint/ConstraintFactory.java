package cn.ios.casegen.constraint;

import cn.ios.casegen.constraint.generate.GenConstraints;
import cn.ios.casegen.config.GlobalCons;
import cn.ios.casegen.util.log.Log;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2022-01-17 21:09
 **/

public class ConstraintFactory {
    public static void processConstraints(){
        GlobalCons.PARAM_CONSTRAINTS_VOS = GenConstraints.getConstraintsFromDirectIf();
        if (GlobalCons.PARAM_CONSTRAINTS_VOS.isEmpty()) {
            Log.e("Can not obtain constraints, input value in test case is random.");
        }
    }
}