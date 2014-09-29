package Armadillo.Core.Text;

public class TextHelper 
{
	public static String replaceCaseInsensitive(
			String original,
			String pattern, 
			String replacement) 
	{
		int position0, position1;
		int count = position0 = 0;
		String upperString = original.toUpperCase();
		String upperPattern = pattern.toUpperCase();
		int inc = (original.length() / pattern.length())
				* (replacement.length() - pattern.length());
		char[] chars = new char[original.length() + Math.max(0, inc)];
		while ((position1 = upperString.indexOf(upperPattern, position0)) != -1) {
			for (int i = position0; i < position1; ++i)
				chars[count++] = original.charAt(i);
			for (int i = 0; i < replacement.length(); ++i)
				chars[count++] = replacement.charAt(i);
			position0 = position1 + pattern.length();
		}
		if (position0 == 0)
			return original;
		for (int i = position0; i < original.length(); ++i)
			chars[count++] = original.charAt(i);
		return new String(chars, 0, count);
	}
}
