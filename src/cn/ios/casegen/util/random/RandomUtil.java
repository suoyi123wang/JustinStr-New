package cn.ios.casegen.util.random;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class RandomUtil {
	
	private static long seed = 0;
	
	private static Random random = null;
	
	static {
		seed = System.currentTimeMillis();
		random = new MersenneTwister(seed);
	}

	/**
	 * <p>
	 * nextInt
	 * </p>
	 * 
	 * @param min
	 *            a int.
	 * @param max
	 *            a int.
	 * @return a int.
	 */
	public static int nextInt(int min, int max) {
		return random.nextInt(max - min) + min;
	}

	/**
	 * <p>
	 * nextInt
	 * </p>
	 * 
	 * @return a int.
	 */
	public static int nextInt() {
		return random.nextInt();
	}
	
	/**
	 * <p>
	 * nextBoolean
	 * </p>
	 * 
	 * @return a boolean.
	 */
	public static boolean nextBoolean() {
		return random.nextBoolean();
	}
	
	/**
	 * <p>
	 * nextChar
	 * </p>
	 * 
	 * @return a char.
	 */
	public static char nextChar() {
		return (char) (nextInt(32, 128));
		//return random.nextChar();
	}
	/**
	 * <p>
	 * nextShort
	 * </p>
	 * 
	 * @return a short.
	 */
	public static short nextShort() {
		return (short) (random.nextInt(2 * 32767) - 32767);
	}

	/**
	 * <p>
	 * nextLong
	 * </p>
	 * 
	 * @return a long.
	 */
	public static long nextLong() {
		return random.nextLong();
	}

	/**
	 * <p>
	 * nextByte
	 * </p>
	 * 
	 * @return a byte.
	 */
	public static byte nextByte() {
		return (byte) (random.nextInt(256) - 128);
	}

	/**
	 * <p>
	 * returns a randomly generated double in the range [0,1]
	 * </p>
	 * 
	 * @return a double between 0.0 and 1.0
	 */
	public static double nextDouble() {
		return random.nextDouble();
	}

	/**
	 * <p>
	 * nextDouble
	 * </p>
	 * 
	 * @param min
	 *            a double.
	 * @param max
	 *            a double.
	 * @return a double.
	 */
	public static double nextDouble(double min, double max) {
		return min + (random.nextDouble() * (max - min));
	}

	/**
	 * <p>
	 * nextFloat
	 * </p>
	 * 
	 * @return a float.
	 */
	public static float nextFloat() {
		return random.nextFloat();
	}
	
	/**
	 * <p>
	 * choice
	 * </p>
	 * 
	 * @param list
	 *            a {@link java.util.List} object.
	 * @param <T>
	 *            a T object.
	 * @return a T object or <code>null</code> if <code>list</code> is empty.
	 */
	public static <T> T choice(List<T> list) {
		if (list.isEmpty())
			return null;

		int position = random.nextInt(list.size());
		return list.get(position);
	}

	/**
	 * <p>
	 * choice
	 * </p>
	 * 
	 * @param set
	 *            a {@link java.util.Collection} object.
	 * @param <T>
	 *            a T object.
	 * @return a T object or <code>null</code> if <code>set</code> is empty.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T choice(Collection<T> set) {
		if (set.isEmpty())
			return null;

		int position = random.nextInt(set.size());
		return (T) set.toArray()[position];
	}

	/**
	 * <p>
	 * choice
	 * </p>
	 * 
	 * @param elements
	 *            a T object.
	 * @param <T>
	 *            a T object.
	 * @return a T object or <code>null</code> if <code>elements.length</code> is zero.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T choice(T... elements) {
		if (elements.length == 0)
			return null;

		int position = random.nextInt(elements.length);
		return elements[position];
	}
	
	/**
	 * <p>
	 * nextString
	 * </p>
	 * 
	 * @param length
	 *            a int.
	 * @return a {@link java.lang.String} object.
	 */
	public static String nextString(int length) {
		char[] characters = new char[length];
		for (int i = 0; i < length; i++) {
			characters[i] = nextChar();
		}
		String result = new String(characters);
		return result.replace("\"", "\\\"");
	}

}