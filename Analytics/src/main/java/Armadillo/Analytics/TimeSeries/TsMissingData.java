package Armadillo.Analytics.TimeSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.DateTime;

import Armadillo.Core.DateHelper;
import Armadillo.Core.Logger;
import Armadillo.Core.Math.TsRow2D;

public class TsMissingData
{
    private static final int MISSING_LIMIT = 3;

    public static <T> SortedMap<Long, T> ProcessEvents(
        SortedMap<Long, T> tsEvents)
    {
        if (tsEvents == null ||
            tsEvents.size() == 0)
        {
            return new TreeMap<Long, T>();
        }
        ArrayList<DateTime> keySet = new ArrayList<DateTime>();
        for(long lngVal : tsEvents.keySet())
        {
        	keySet.add(new DateTime(lngVal));
        }
        List<DateTime> dateSet = DateHelper.GetDailyWorkingDates(keySet.get(0),
        											keySet.get(keySet.size() - 1));
        
        TreeMap<Long, T> outEvents = new TreeMap<Long, T>();
        T lastValue = null;
        for (int i = 0; i < dateSet.size(); i++)
        {
            DateTime currDate = dateSet.get(i);
            T currValue;
            if(tsEvents.containsKey(currDate))
            {
            	currValue = tsEvents.get(currDate);
                lastValue = currValue;
            }
            else
            {
                currValue = lastValue;
            }
            outEvents.put(currDate.getMillis(), currValue);
        }
        return outEvents;
    }

    public static List<TsRow2D> processEvents(
            List<DateTime> dateSet,
            TsRow2D[] tsEvents)
    {
        try
        {
        	SortedMap<Long, Double> tsEventsMap = new TreeMap<Long, Double>();
        	for(TsRow2D tsRow2D : tsEvents)
        	{
        		try
        		{
        			if(tsRow2D != null && tsRow2D.Time != null)
        			{
        				tsEventsMap.put(tsRow2D.getTime().getMillis(), tsRow2D.Fx);
        			}
	            }
	            catch(Exception ex)
	            {
	                Logger.Log(ex);
	            }
        	}
        	SortedMap<Long, Double> tsEventsMap1 = ProcessEvents(dateSet, tsEventsMap);
        	List<TsRow2D> results = new ArrayList<TsRow2D>();
        	for(Entry<Long, Double> kvp : tsEventsMap1.entrySet())
        	{
        		results.add(new TsRow2D(
        				new DateTime(kvp.getKey()), 
        				kvp.getValue()));
        	}
        	return results;
        }
        catch(Exception ex)
        {
            Logger.Log(ex);
        }
        return new ArrayList<TsRow2D>();
    }
    
    public static SortedMap<Long, Double> ProcessEvents(
        List<DateTime> dateSet,
        SortedMap<Long, Double> tsEvents)
    {
        try
        {
            if (tsEvents.size() == 0)
            {
                return new TreeMap<Long, Double>();
            }

            TreeMap<Long, Double> filteredEvents = new TreeMap<Long, Double>();
            KalmanFilter[] filterArr = new KalmanFilter[1];
            filterArr[0] = new KalmanFilter();
            int[] intMissingArr = new int[1];

            for (int i = 0; i < dateSet.size(); i++)
            {
                DateTime dateTime = dateSet.get(i);
                double dblCleanValue;
                double dblValue;
                if (!tsEvents.containsKey(dateTime.getMillis()))
                {
                    dblCleanValue = Math.max(
                        Predict(filterArr, intMissingArr), 0);
                }
                else
                {
                	dblValue = tsEvents.get(dateTime.getMillis());
                    if (!IsValid(dblValue))
                    {
                        dblCleanValue = Math.max(
                            Predict(filterArr, intMissingArr),
                            0);
                    }
                    else
                    {
                        dblCleanValue = dblValue;
                        double[] dblFilteredValue = new double[1];
                        double[] dblNoise = new double[1];
                        filterArr[0].Filter(dblCleanValue,
                                          dblFilteredValue,
                                          dblNoise);
                        intMissingArr[0] = 0;
                    }
                }

                dblCleanValue = Math.max(0, dblCleanValue);
                filteredEvents.put(dateTime.getMillis(), dblCleanValue);
            }
            //
            // remove zeros
            //
            TreeMap<Long, Double> filteredEvents1 = new TreeMap<Long, Double>();
            for(Entry<Long, Double> kvp : filteredEvents.entrySet())
            {
            	if(kvp.getValue() > 0)
            	{
            		filteredEvents1.put(kvp.getKey(), kvp.getValue());
            	}
            }
            
            return filteredEvents1;
        }
        catch(Exception ex)
        {
            Logger.Log(ex);
        }
        return tsEvents;
    }

    private static double Predict(
        KalmanFilter[] kalmanFilter,
        int[] intMissingCounter)
    {
    	try
    	{
	        double dblOpen = 0;
	        if (kalmanFilter[0].IsReady())
	        {
	            dblOpen = kalmanFilter[0].Predict();
	            intMissingCounter[0]++;
	        }
	        if (intMissingCounter[0] >= MISSING_LIMIT)
	        {
	            kalmanFilter[0] = new KalmanFilter();
	            intMissingCounter[0] = 0;
	        }
	        return dblOpen;
        }
        catch(Exception ex)
        {
            Logger.Log(ex);
        }
    	return 0;
    }

    public static boolean IsValid(double dblValue)
    {
        return !Double.isNaN(dblValue) &&
                 !Double.isInfinite(dblValue) &&
                 Math.abs(dblValue) > 1e-6;
    }
}
