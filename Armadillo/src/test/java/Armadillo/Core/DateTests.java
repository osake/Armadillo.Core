package Armadillo.Core;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.junit.Assert;
import org.junit.Test;

import Armadillo.Core.DateHelper;

public class DateTests 
{
    @Test
    public void testSecondsBetween()
    {
    	DateTime dateTime1 = new DateTime(1900,1,1,1,1);
    	DateTime dateTime2 = new DateTime(1901,1,1,1,1);
    	Seconds seconds = Seconds.secondsBetween(dateTime1, dateTime2);
    	double dblSeconds = seconds.getSeconds();
    	double dblDiff = Math.abs(dblSeconds - 3.1536E7);
    	Assert.assertTrue(dblDiff < 10);
    }

    @Test
    public void testDateToString()
    {
    	Date dateTimeTest0 = new DateTime(1979,2,23,7,0,0).toDate();
    	String strDateTimeTest = DateHelper.ToDateTimeString(dateTimeTest0);
    	Date dateTimeTest1 = DateHelper.ParseDateTime(strDateTimeTest);
    	

		int intSeconds = Seconds.secondsBetween(
				new DateTime(dateTimeTest0),
				new DateTime(dateTimeTest1)).getSeconds();
    	
    	Assert.assertTrue(intSeconds == 0);
    	String strDateTest = DateHelper.ToDateString(dateTimeTest0);
    	Date dateTest0 = DateHelper.GetStartOfDay(dateTimeTest0);
    	Date dateTest1 = DateHelper.ParseDate(strDateTest);
		intSeconds = Seconds.secondsBetween(
				new DateTime(dateTest0),
				new DateTime(dateTest1)).getSeconds();
    	Assert.assertTrue(intSeconds == 0);
    }
    
}
