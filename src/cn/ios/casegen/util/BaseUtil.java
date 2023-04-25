package cn.ios.casegen.util;

/**
 * @description: TODO
 * @author: wangmiaomiao
 * @create: 2022-01-17 20:15
 **/

public class BaseUtil {
    /**
     * each char of str isNumeric
     */
    public static boolean isNumeric(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param charInt "97",实际是char a
     * @return "a"
     */
    public static String convertCharIntToString(String charInt) {
        if (isNumeric(charInt)) {
            return String.valueOf((char) Integer.parseInt(charInt));
        }
        return null;
    }

    public static String lowerCaseFirstLetter(String str){
        char[] chars = str.toCharArray();
        if ('A' <= chars[0] && chars[0] <= 'Z') {
            chars[0] += 32;
        }
        return String.valueOf(chars);
    }

    public static String substringFromStr(String allStr, String subStr){
        if (allStr.contains(subStr)) {
            return allStr.substring(allStr.indexOf(subStr) + subStr.length());
        }
        return allStr;
    }

    public static String substringToStr(String allStr, String subStr){
        if (allStr.contains(subStr)) {
            return allStr.substring(0, allStr.indexOf(subStr));
        }
        return allStr;
    }
}
