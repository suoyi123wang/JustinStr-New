package cn.ios.casegen.config;

import cn.ios.casegen.constraint.VO.MemberFieldVO;
import cn.ios.casegen.constraint.VO.ParamConstraintVO;
import cn.ios.casegen.variable.FullClassType;
import com.google.common.collect.Maps;
import soot.SootClass;
import soot.SootMethod;

import java.io.File;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2022-01-17 20:32
 **/

public class GlobalCons {
    public static Map<String, Map<String, Map<Integer, ParamConstraintVO>>> PARAM_CONSTRAINTS_VOS = Maps.newHashMap();
    public static Map<String, List<MemberFieldVO>> MEMBER_FIELD_INFO = Maps.newHashMap();
    public static Map<SootMethod, List<FullClassType>> GENERIC_INFO_OF_METHOD = Maps.newHashMap();
    public static Map<SootClass, Set<SootMethod>> CONSTRUCTOR_MAP = Maps.newHashMap();

    public static String TEST_INPUT_FOLDER = "";
    public static String TEST_OUTPUT_FOLDER = "";
    public static String REPORT_OUTPUT_FOLDER = "";
    public static String TEST_COMPILE_TEMP_FOLDER = "";
    public static String LOCAL_JRE_PATH = "";
    public static String DB_PATH = "";

    public static boolean sootConfig = false;
    public static boolean pluginStart = false;

    public static Set<String> CLASS_NAME_UNDER_TEST = new HashSet<>();
    public static Connection connection = null;
    public static String PROJECT_NAME = "";
    public static String EXECUTE_TIME = "";
    public static long START_TIME = 0L;
    public static int TEST_CLASS_NUM = 0;
    public static int ALREADY_DEAL_CLASS_NUM = 0;

    public static int MAX_TIME_PER_CLASS = 15;
    public static int MAX_UNIT_METHOD = 5;
    public static int ARRAY_MIN_SIZE = 0;
    public static int ARRAY_MAX_SIZE = 5;
    public static int STRING_MAX_LENGTH = 50;

    public static int VARIABLE_INDEX = 0;
    public static int MAX_UNIT_VISIT_TIME = 5;
}
