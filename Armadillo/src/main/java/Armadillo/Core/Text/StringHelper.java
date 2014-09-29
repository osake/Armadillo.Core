package Armadillo.Core.Text;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import Armadillo.Core.Logger;

public class StringHelper 
{
    public static String ReplaceCaseInsensitive(
            String strOriginal,
            String strPattern,
            String strReplacement)
        {
            try
            {
                int position0, position1;
                int count = position0 = 0;
                String upperString = strOriginal.toUpperCase();
                String upperPattern = strPattern.toUpperCase();
                int inc = (strOriginal.length()/strPattern.length())*
                          (strReplacement.length() - strPattern.length());
                char[] chars = new char[strOriginal.length() + Math.max(0, inc)];
                while ((position1 = upperString.indexOf(upperPattern,
                                                        position0)) != -1)
                {
                    for (int i = position0; i < position1; ++i)
                        chars[count++] = strOriginal.charAt(i);
                    for (int i = 0; i < strReplacement.length(); ++i)
                        chars[count++] = strReplacement.charAt(i);
                    position0 = position1 + strPattern.length();
                }
                if (position0 == 0) return strOriginal;
                for (int i = position0; i < strOriginal.length(); ++i)
                    chars[count++] = strOriginal.charAt(i);
                return new String(chars, 0, count).trim();
            }
            catch (Exception ex)
            {
                Logger.Log(ex);
            }
            return strOriginal;
        }

	public static boolean IsNullOrEmpty(String str) 
	{
		return str == null || str.equals("");
	}

	public static String toTitleCase(String input) {
	    StringBuilder titleCase = new StringBuilder();
	    boolean nextTitleCase = true;

	    for (char c : input.toCharArray()) {
	        if (Character.isSpaceChar(c)) {
	            nextTitleCase = true;
	        } else if (nextTitleCase) {
	            c = Character.toTitleCase(c);
	            nextTitleCase = false;
	        }

	        titleCase.append(c);
	    }

	    return titleCase.toString();
	}

	public static String join(String delimiter, String[] s) 
	{
		return join(s, delimiter);
	}
	
	public static String join(String[] s, String delimiter) 
	{
	     StringBuilder builder = new StringBuilder();
	     
	     if(s == null || s.length  == 0){
	    	 return "";	    
	     }
	     builder.append(s[0]);
	     
	     for (int i = 1; i < s.length; i++) {
	         builder.append(delimiter + s[i]);
	     }
	     return builder.toString();
	 }
	
	public static String join(Collection<?> s, String delimiter) 
	{
		try
		{
			if(delimiter == null || s.size() == 0)
			{
				return "";
			}
			
		     StringBuilder builder = new StringBuilder();
		     Iterator<?> iter = s.iterator();
		     while (iter.hasNext()) 
		     {
		         builder.append(iter.next());
		         if (!iter.hasNext()) 
		         {
		           break;                  
		         }
		         builder.append(delimiter);
		     }
		     return builder.toString();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
	 }

	public static String join(Enumeration<String> keys, String delimiter) {
	    StringBuilder builder = new StringBuilder();
		while (keys.hasMoreElements()){
	         builder.append(keys.nextElement());
	         if (!keys.hasMoreElements()) {
	           break;                  
	         }
	         builder.append(delimiter);
			
		}
	     return builder.toString();
	}
	
    public static String RemoveCommonSymbols(String str)
    {
        str = str
            .replace("�", " ")
            .replace("$", " ")
            .replace("�", " ")
            .replace("%", " ")
            .replace("'", " ")
            .replace("#", " ")
            .replace("#", " ")
            .replace("=", " ")
            .replace("+", " ")
            .replace("_", " ")
            .replace("`", " ")
            .replace("@", " ")
            .replace("[", " ")
            .replace("]", " ")
            .replace("{", " ")
            .replace("}", " ")
            .replace("&", " ")
            .replace("^", " ")
            .replace("<", " ")
            .replace(">", " ")
            .replace("?", " ")
            .replace("~", " ")
            .replace("|", " ")
            .replace("\\", " ")
            .replace("/", " ")
            .replace("*", " ")
            .replace("!", " ")
            .replace("(", " ")
            .replace(")", " ")
            .trim();
        if(StringHelper.IsNullOrEmpty(str))
        {
            return "";
        }
        char firstChar = str.charAt(0);
        if(!Character.isLetterOrDigit(firstChar) &&
            !(firstChar != '-' &&
            firstChar != '+' &&
            firstChar != '.'))
        {
            str = str.substring(1);
        }
        int intStrSize = str.length();
        if (intStrSize > 1)
        {
            char lastChar = str.charAt(intStrSize - 1);
            if (!Character.isLetterOrDigit(lastChar))
            {
                str = str.substring(0, str.length() - 1);
            }
        }
        return str;
    }

    public static boolean AllLetters(String token)
    {
        try
        {
            for (int i = 0; i < token.length(); i++)
            {
                if (!Character.isLetter(token.charAt(i)))
                    return false;
            }
            return true;
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return true;
    }
    
    public static String CleanString(
            String strInput)
        {
            return CleanString(strInput, false);
        }

        public static String CleanString(
            String strInput,
            boolean blnIgnoreNumbers)
        {
            return CleanString(strInput,
                        null,
                        blnIgnoreNumbers);
        }
    
    public static String CleanString(
            String strInput,
            String[] stopWords,
            boolean blnIgnoreNumbers)
        {
            try
            {
                if (IsNullOrEmpty(strInput))
                {
                    return "";
                }
                String[] tokens = Tokeniser.Tokenise(
                    strInput,
                    stopWords,
                    blnIgnoreNumbers);

                if (tokens == null || tokens.length == 0)
                {
                    return "";
                }
                StringBuilder sb = new StringBuilder();
                sb.append(tokens[0]);
                for (int i = 1; i < tokens.length; i++)
                {
                    sb.append(" " + tokens[i]);
                }
                return sb.toString();
            }
            catch (Exception ex)
            {
                Logger.log(ex);
            }
            return "";
        }

    
}
