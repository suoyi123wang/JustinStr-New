package cn.ios.casegen.enums;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2022-01-11 10:45
 **/

public enum GenerationEnum {
    TEST_ANNOTATION("@Test(timeout = 5000)"),
    BEFORE_ANNOTATION("@Before"),
    AFTER_ANNOTATION("@After"),
    BEFORE_CLASS_ANNOTATION("@BeforeClass"),
    AFTER_CLASS_ANNOTATION("@AfterClass"),
    BEFORE_EACH_ANNOTATION("@BeforeEach"),
    AFTER_EACH_ANNOTATION("@AfterEach"),
    BEFORE_ALL_ANNOTATION("@BeforeAll"),
    AFTER_ALL_ANNOTATION("@AfterAll"),

    ONE_SPACE(" "),
    TWO_SPACE("  "),
    FOUR_SPACE("    "),
    SIX_SPACE("      "),
    SEMICOLON(";"),
    LEFT_CURLY_BRACE("{"),
    RIGHT_CURLY_BRACE("}"),
    LEFT_PARENTHESES("("),
    RIGHT_PARENTHESES(")"),
    LEFT_ANGLE_BRACKET("<"),
    RIGHT_ANGLE_BRACKET(">"),
    UNDERSCORE("_"),
    COMMA(","),
    EQUAL("="),
    ONE_NEW_LINE("\n"),
    TWO_NEW_LINES("\n\n"),

    PACKAGE("package"),
    IMPORT_TEST("import org.junit.Test;\n\n"),
    PUBLIC_CLASS("public class "),
    PUBLIC_TEST("public void test_"),

    THROWS("throws"),
    ADD_METHOD("add"),
    PUT_METHOD("put"),


    JUNIT4("JUNIT4"),
    JUNIT5("JUNIT5"),

    NE_EXPR("NeExpr"),
    EQ_EXPR("EqExpr"),
    GE_EXPR("GeExpr"),
    GT_EXPR("GtExpr"),
    LE_EXPR("LeExpr"),
    LT_EXPR("LtExpr"),

    JAVA_UTIL_COLLECTION("java.util.Collection"),
    JAVA_UTIL_MAP("java.util.Map"),
    SUB_SIG_ADD("boolean add(java.lang.Object)"),
    SUB_SIG_PUT("java.lang.Object put(java.lang.Object,java.lang.Object)"),
//    SPECIAL_REGEX_CHARS("\\s*|\t|\n|\n"),
    SPECIAL_REGEX_CHARS("[\t\n\r]"),
    OUTPUT_FOLDER("Justin"),
    NULL_OBJECT("null"),

    ;



    private String value;

    GenerationEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
