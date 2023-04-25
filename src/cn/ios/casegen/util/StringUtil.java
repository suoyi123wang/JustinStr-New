package cn.ios.casegen.util;

import cn.ios.casegen.enums.GenerationEnum;
import java.text.DecimalFormat;
import java.util.List;

public class StringUtil {

	public static String splicingParameterParentheses(List<? extends Object> data) {
		return splicingParameterParentheses(data, 0, data.size());
	}

	public static String splicingParameterParentheses(List<? extends Object> data, int start, int length) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("(");
		stringBuffer.append(splicingParameter(data, start, length));
		stringBuffer.append(")");
		return stringBuffer.toString();
	}

	public static String splicingParameterParentheses(Object[] data) {
		return splicingParameterParentheses(data, 0, data.length);
	}

	public static String splicingParameterParentheses(Object[] data, int start, int length) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("(");
		stringBuffer.append(splicingParameter(data, start, length));
		stringBuffer.append(")");
		return stringBuffer.toString();
	}

	public static String splicingParameterBrace(List<? extends Object> data, int start, int length) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("{");
		stringBuffer.append(splicingParameter(data, start, length));
		stringBuffer.append("}");
		return stringBuffer.toString();
	}

	public static String splicingParameter(Object[] data) {
		if (data != null) {
			return splicingParameter(data, 0, data.length);
		}
		return "";
	}

	public static String splicingParameter(List<? extends Object> data) {
		if (data != null) {
			return splicingParameter(data, 0, data.size());
		}
		return "";
	}

	public static String splicingParameter(Object[] data, int start, int length) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < length - 1; i++) {
			stringBuffer.append(data[start + i]);
			stringBuffer.append(", ");
		}
		if (length != 0) {
			stringBuffer.append(data[start + length - 1]);
		}
		return stringBuffer.toString();
	}

	public static String splicingParameter(List<? extends Object> data, int start, int length) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < length - 1; i++) {
			stringBuffer.append(data.get(start + i));
			stringBuffer.append(", ");
		}
		if (length != 0) {
			Object o = data.get(start + length - 1);
			stringBuffer.append(o);
		}
		return stringBuffer.toString();
	}

	/**
	 * comma splicing
	 */
	public static String splicingStrList(List<String> strList){
		StringBuffer stringBuffer = new StringBuffer();
		if (strList == null || strList.isEmpty()) return stringBuffer.toString();
		stringBuffer.append(strList.get(0));
		for (int i = 1; i < strList.size(); i++) {
			stringBuffer.append(GenerationEnum.COMMA.getValue()).append(strList.get(i));
		}

		return stringBuffer.toString();
	}

	/**
	 * 1234 --> 1,234
	 * @param data
	 * @return
	 */
	public static String formatString(String data) {
		if (data == null || data.isEmpty()) return data;
		try {
			float dataf=Float.parseFloat(data);
			DecimalFormat df = new DecimalFormat("#,###");
			return df.format(dataf);
		} catch (Exception e) {
			return data;
		}
	}

	public static String getDividePercentage(int num1, int num2){
		DecimalFormat df=new DecimalFormat("0");
		float n = (float)(num1)/(num2);
		return df.format(((float)n*100))+"%";
	}
}
