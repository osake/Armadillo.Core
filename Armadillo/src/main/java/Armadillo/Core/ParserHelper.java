package Armadillo.Core;

import java.lang.reflect.Type;
import java.sql.Date;
import java.text.NumberFormat;
import java.text.ParsePosition;

import org.joda.time.DateTime;

import Armadillo.Core.Text.StringHelper;

public class ParserHelper 
{
    public static boolean IsNumeric(Object obj)
    {
        double[] result = new double[1];
        return IsNumeric(obj, result);
    }
    
	public static boolean IsNumeric(Object obj, double[] result) {
        
        try
        {
            if(obj == null)
            {
                return false;
            }
            String strNum = obj.toString();
			
            if (tryParseDoubleValue(strNum, result))
            {
                return true;
            }
            if (!StringHelper.IsNullOrEmpty((strNum)))
            {
                strNum = StringHelper.RemoveCommonSymbols(strNum)
                		.replace(" ", "");

                if (!StringHelper.IsNullOrEmpty(strNum))
                {
                    if (tryParseDoubleValue(strNum, result))
                    {
                        return true;
                    }

                }

            }
            return false;
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return false;
	}

	/**
	 * Parses the double value from string.
	 *
	 * @param strVal the string value
	 * @return the parsed double value
	 */
	public static double parseDouble(String strVal){
		try{
			if(strVal == null ||
					strVal.equals("")){
				return Double.NaN;
			}
			strVal = strVal.trim();
			
			if(strVal.equals("-")){
				return 0;
			}
			
			return Double.parseDouble(strVal
					.replace(",", "")
					.replace('"' + "", "").trim());
		}
		catch(Exception ex){
			Logger.log(ex);
		}
		
		return Double.NaN;
	}

	public static boolean isANullDouble(String strVal){
		try{
			if(StringHelper.IsNullOrEmpty(strVal)){
				return true;
			}
			strVal = strVal.trim();
			if(StringHelper.IsNullOrEmpty(strVal)){
				return true;
			}
			
			if(strVal.equals("-")){
				return true;
			}
			
			if(strVal.equals("NA")){
				return true;
			}
			
			return false;
		}
		catch(Exception ex){
			Logger.log(ex);
		}
		
		return true;
	}
	
	/**
	 * Parses a integer value from a string value
	 *
	 * @param string value
	 * @return the parsed integer value
	 */
	public static int parseInt(String strVal) {
		try{
			
			if(strVal == null ||
					strVal.equals("")){
				
				return 0;
			}
			
			return Integer.parseInt(strVal.replace(",", "").trim());
		}
		catch(Exception ex){
			Logger.log(ex);
		}
		
		return 0;
	}

	/**
	 * Parses a double value from string as a percentage
	 *
	 * @param strVal the str val
	 * @return the double
	 */
	public static double parseDoublePrc(String strVal) {
		double dblPrc = parseDouble(strVal.replace("%", "")) / 100;
		return dblPrc;
	}
	
	
	/**
	 * Parses the integer value avoiding throwing exception when a failure occurs
	 *
	 * @param strVal the string value
	 * @return the parsed integer value
	 */
	public static int parseIntSafe(String strVal) {
		try{
			int[] result = new int[1];
			if(tryParseIntegerValue(strVal.replace(",", "").trim(), result)){
				return result[0];
			}
		}
		catch(Exception ex){
			Logger.log(ex);
		}
		
		return 0;
	}

	
	/**
	 * Try parse integer value from a string value
	 *
	 * @param s the string value
	 * @param result the result wrapped as an array of integers
	 * @return true, if successful
	 */
	public static boolean tryParseIntegerValue(String s, int[] result)
	{
	    NumberFormat format = NumberFormat.getIntegerInstance();
	    ParsePosition position = new ParsePosition(0);
	    Object parsedValue = format.parseObject(s, position);

	    if (position.getErrorIndex() > -1)
	    {
	        return false;
	    }

	    if (position.getIndex() < s.length())
	    {
	        return false;
	    }

	    result[0] = ((Long) parsedValue).intValue();
	    return true;
	}

	/**
	 * Try parse integer value from a string value
	 *
	 * @param s the string value
	 * @param result the result wrapped as an array of integers
	 * @return true, if successful
	 */
	public static boolean tryParseDoubleValue(String s, double[] result)
	{
	    NumberFormat format = NumberFormat.getNumberInstance();
	    ParsePosition position = new ParsePosition(0);
	    Object parsedValue = format.parseObject(s, position);

	    if (position.getErrorIndex() > -1)
	    {
	        return false;
	    }

	    if (position.getIndex() < s.length())
	    {
	        return false;
	    }

	    if(parsedValue instanceof Double){
	    	result[0] = ((Double) parsedValue).doubleValue();
	    }
	    else if(parsedValue instanceof Long){
	    	result[0] = ((Long) parsedValue).doubleValue();
	    }
	    return true;
	}
	
	/**
	 * Clean string. Makes sure is suitable for parsing
	 *
	 * @param string the input string
	 * @return the cleaned string
	 */
	public static String cleanString(String string) {
		if(string == null){
			return "";
		}
		String strMatch = '"' + "";
		return string.trim().replace(strMatch, "");
	}	

@SuppressWarnings({ "rawtypes", "unchecked" })
public static Object ParseString(
        String strInput,
        Type type)
    {
        try
        {
        	if(StringHelper.IsNullOrEmpty(strInput))
        	{
        		return com.google.common.base.Defaults.defaultValue((Class) type);
        	}
            if (type == null)
            {
                return null;
            }
            if (type == String.class)
            {
                return strInput;
            }
            if (type == double.class ||
            		type == Double.class)
            {
                double dblValue = 
                		Double.parseDouble(
                                strInput);
                return dblValue;
            }
            if (type == Integer.class ||
            		type == int.class)
            {
                int intValue = Integer.parseInt(strInput);
                return intValue;
            }
            if (type == boolean.class)
            {
                boolean blnValue =
                		Boolean.parseBoolean(strInput);
                return blnValue;
            }
            if (type == long.class ||
            		type == Long.class)
            {
                long lngValue = Long.parseLong(strInput);
                return lngValue;
            }
            if (type == Date.class)
            {
                Date dateTime = 
                		(Date) DateHelper.m_defaultDateParser.parse(strInput);
                return dateTime;
            }
            if (type == java.util.Date.class)
            {
            	java.util.Date dateTime = DateTime.parse(strInput).toDate();
                return dateTime;
            }
            if (type == DateTime.class)
            {
            	
                DateTime dateTime = 
                		DateTime.parse(strInput);
                return dateTime;
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        return null;
    }
}
