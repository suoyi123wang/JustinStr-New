package cn.ios.casegen.util.random;

import cn.ios.casegen.config.GlobalCons;

public final class MagicValues {

	private static final Object[] INTEGERS = { 1, 0, -1, Integer.MAX_VALUE, Integer.MIN_VALUE };

	private static final Object[] BYTES = { (byte) 1, (byte) 0, (byte) -1, Byte.MAX_VALUE, Byte.MIN_VALUE };

	private static final Object[] SHORTS = { (short) 1, (short) 0, (short) -1, Short.MAX_VALUE, Short.MIN_VALUE };

	private static final Object[] LONGS = { 1l, 0l, -1l, Long.MAX_VALUE, Long.MIN_VALUE };

	private static final Object[] CHARS = { Character.MAX_VALUE, Character.MIN_VALUE };

	private static final Object[] DOUBLES = { 1d, 0d, -1d, Double.MAX_VALUE, Double.MIN_VALUE };

	private static final Object[] FLOATS = { 1.0f, 0.0f, -1.0f, Float.MAX_VALUE, Float.MIN_VALUE };

	private static final String MAIL_REGU = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";

	private static final String PHONE_REGU = "^[1]([3-9])[0-9]{9}$";

	private static final String JSON_1 = "[0,1]";

	private static final String JSON_2 = "{\"key\":2 }";

	private static final String JSON_3 = "{\"key\":null}";

	private static final String XML_1 = "<a>Hello World</a>";

	private static final String XML_2 = "<a> </a>";

	private static final String FILE = ".\\a.txt";

	private static final String URL_HTTP = "http://lcs.ios.ac.cn/";

	private static final Object[] STRINGS = { "", " ", "\\n", " #", "a ", MAIL_REGU, PHONE_REGU, JSON_1, JSON_2, JSON_3,
			XML_1, XML_2, FILE, URL_HTTP };

	private static Object getMagicValue(Object defaultValue, Object... magicValues) {
		if (magicValues == null) {
			return defaultValue;
		}
		int i = RandomUtil.nextInt(0, magicValues.length + 1);
		if (i < magicValues.length) {
			return magicValues[i];
		}
		return defaultValue;
	}

	public static boolean getBoolean() {
		return RandomUtil.nextBoolean();
	}

	public static int getInt() {
		int defaultValue = RandomUtil.nextInt();
		return (int) getMagicValue(defaultValue, INTEGERS);
	}

	public static byte getByte() {
		byte defaultValue = RandomUtil.nextByte();
		return (byte) getMagicValue(defaultValue, BYTES);
	}

	public static short getShort() {
		short defaultValue = RandomUtil.nextShort();
		return (short) getMagicValue(defaultValue, SHORTS);
	}

	public static char getChar() {
		char defaultValue = RandomUtil.nextChar();
		return (char) getMagicValue(defaultValue, CHARS);
	}

	public static long getLong() {
		long defaultValue = RandomUtil.nextLong();
		return (long) getMagicValue(defaultValue, LONGS);
	}

	public static float getFloat() {
		float defaultValue = RandomUtil.nextFloat();
		return (float) getMagicValue(defaultValue, FLOATS);
	}

	public static double getDouble() {
		double defaultValue = RandomUtil.nextDouble();
		return (double) getMagicValue(defaultValue, DOUBLES);
	}

	public static String getString() {
		int length = RandomUtil.nextInt(0, GlobalCons.STRING_MAX_LENGTH);
		String defaultValue = RandomUtil.nextString(length);
		return (String) getMagicValue(defaultValue, STRINGS);
		 
	}

}
