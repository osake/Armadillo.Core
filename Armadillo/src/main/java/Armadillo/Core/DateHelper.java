package Armadillo.Core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

public class DateHelper 
{

    public static final String TODAY_START_OF_DAY = "@TodayStartOfDay";
    public static final String TODAY_END_OF_DAY = "@TodayEndOfDay";
	public static final Date MIN_DATE = new DateTime(1900,1,1,0,0,0).toDate();
	public static final DateTime MIN_DATE_JODA = new DateTime(1900,1,1,0,0,0);
    private static final String DATE_FORMAT = "yyyy_MM_dd";
    private static final String DATE_TIME_FORMAT = "yyyy_MM_dd_HH.mm.ss.SSS";
    public static final SimpleDateFormat m_defaultDateParser = new SimpleDateFormat();
    
    public static SimpleDateFormat m_dateParser;
    public static SimpleDateFormat m_dateTimeParser;

    static 
    {
    	try
    	{
	        m_dateParser = new SimpleDateFormat(DATE_FORMAT);
	        m_dateTimeParser = new SimpleDateFormat(DATE_TIME_FORMAT);
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }
    

    public static String ToDateTimeString(Date dateTime) 
    {
    	try
    	{
			return m_dateTimeParser.format(dateTime);
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return "";
	}

	public static Date ParseDate(String strDate) 
    {
		try 
		{
			return m_dateParser.parse(strDate);
		} 
		catch (ParseException ex) 
		{
			Logger.log(ex);
		}
		return MIN_DATE;
	}

	public static Date ParseDateTime(String strDate) 
    {
		try 
		{
			return m_dateTimeParser.parse(strDate);
		} 
		catch (ParseException ex) 
		{
			Logger.log(ex);
		}
		return MIN_DATE;
	}
	
	public static String ToDateString(Date date) 
    {
		return m_dateParser.format(date);
	}

    public static Date ParseDefaultDateTimeString(String strDate)
    {
    	try
    	{
	        Date dateTime = ParseWildCardDate(
	                strDate);
	        if(dateTime != MIN_DATE)
	        {
	            return dateTime;
	        }
	        
	        try 
	        {
				return m_defaultDateParser.parse(strDate);
			} 
	        catch (ParseException e) 
	        {
				Logger.log(e);
			}
		} 
        catch (Exception e) 
        {
			Logger.log(e);
		}
        return null;
    }

    private static Date ParseWildCardDate(
            String token)
    {
    	try
    	{
			Date dateTime = MIN_DATE;
	        if(token.equals(TODAY_START_OF_DAY))
	        {
	            dateTime = DateTime.now().toDate();
	        }
	        if (token.equals(TODAY_END_OF_DAY))
	        {
	            dateTime = GetEndOfDay(DateTime.now().toDate());
	        }
	        return dateTime;
		} 
	    catch (Exception e) 
	    {
			Logger.log(e);
		}
    	return MIN_DATE;
    }
    
    public static Date GetEndOfDay(Date dateTime)
    {
        dateTime = GetStartOfDay(dateTime);
        Date eod = new DateTime(dateTime).plusDays(1).plusSeconds(-1).toDate();
        return eod;
    }
    
    public static Date GetStartOfDay(Date dateTime)
    {
    	try
    	{
    		DateTime dateTimeVal = new DateTime(dateTime);
	        return new DateTime(
	        		dateTimeVal.getYear(),
	        		dateTimeVal.getMonthOfYear(),
	        		dateTimeVal.getDayOfMonth(),
	            0,
	            0,
	            0).toDate();
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return MIN_DATE;
    }

	public static DateTime getStartOfYear(DateTime start) 
	{
		return new DateTime(start.getYear(), 1, 1, 0,0,0);
	}

	public static DateTime getEndOfYear(DateTime start) 
	{
		return new DateTime(start.getYear()+1, 1, 1, 0,0,0).minusDays(1);
	}

    public static List<DateTime> GetDailyWorkingDates(
            DateTime startTime,
            DateTime endTime)
    {
		List<DateTime> dateList = new ArrayList<DateTime>();
        DateTime currDate = startTime;
        while (!currDate.isAfter(endTime))
        {
            if (!IsAWeekendDay(currDate))
            {
                dateList.add(new DateTime(GetStartOfDay(currDate.toDate())));
            }
            currDate = currDate.plusDays(1);
        }
        return dateList;
    }

	private static boolean IsAWeekendDay(DateTime currDate) 
	{
		 return currDate.getDayOfWeek() == DateTimeConstants.SATURDAY ||
				 currDate.getDayOfWeek() == DateTimeConstants.SUNDAY;
	}

	public static List<DateTime> getDailyDates(
			DateTime startTime, 
			DateTime endTime) 
	{
		List<DateTime> dateList = new ArrayList<DateTime>();
        DateTime currDate = startTime;
        while (currDate.isBefore(endTime))
        {
            dateList.add(new DateTime(currDate.toDate()));
            currDate = currDate.plusDays(1);
        }
        return dateList;
	}
	
	public static List<DateTime> getWeeklyDates(
			DateTime startTime, 
			DateTime endTime) 
	{
		List<DateTime> dateList = new ArrayList<DateTime>();
        DateTime currDate = startTime;
        while (currDate.isBefore(endTime))
        {
            dateList.add(new DateTime(currDate.toDate()));
            currDate = currDate.plusWeeks(1);
        }
        return dateList;
	}	
	
	public static List<DateTime> getMonthlyDates(
			DateTime startTime, 
			DateTime endTime) 
	{
		List<DateTime> dateList = new ArrayList<DateTime>();
        DateTime currDate = startTime;
        while (currDate.isBefore(endTime))
        {
            dateList.add(new DateTime(currDate.toDate()));
            currDate = currDate.plusMonths(1);
        }
        return dateList;
	}	

	public static List<DateTime> getYearlyDates(
			DateTime startTime, 
			DateTime endTime)
	{
		List<DateTime> dateList = new ArrayList<DateTime>();
        DateTime currDate = startTime;
        while (currDate.isBefore(endTime))
        {
            dateList.add(new DateTime(currDate.toDate()));
            currDate = currDate.plusYears(1);
        }
        return dateList;
	}

	public static DateTime moveToNextDayOfWeek(
			DateTime date, 
			int intDayOfWeek) 
	{
		try
		{
			while(date.getDayOfWeek() != intDayOfWeek)
			{
				date = date.plusDays(1);
			}
			return date;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
	
}