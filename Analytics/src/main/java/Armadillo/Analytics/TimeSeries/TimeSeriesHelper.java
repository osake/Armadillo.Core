package Armadillo.Analytics.TimeSeries;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.DateTime;

import Armadillo.Analytics.Stat.StatsHelper;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.Math.TsRow2D;

public class TimeSeriesHelper 
{
    public static int GetMostCorrelatedSymbol(
            Hashtable<Integer, Object> varSet,
            List<double[]> xData,
            List<Double> yData)
        {
            try
            {
                if (varSet == null ||
                    varSet.size() == 0)
                {
                    return -1;
                }
                double dblMaxCorrelation = -Double.MAX_VALUE;
                int intSelectVar = -1;
                for (int intVarId : varSet.keySet())
                {
                    List<Double> currVector = GetVector(xData, intVarId);
                    double dblAbsCorr = Math.abs(StatsHelper.GetCorrelation(
                        yData,
                        currVector));
                    if (dblMaxCorrelation < dblAbsCorr)
                    {
                        dblMaxCorrelation = dblAbsCorr;
                        intSelectVar = intVarId;
                    }
                }
                return intSelectVar;
            }
            catch (Exception ex)
            {
                Logger.Log(ex);
            }
            return -1;
        }

    public static List<Double> GetVector(
            SortedMap<DateTime, double[]> xVars,
            int intVarId)
    {
        try
        {
            if (xVars == null ||
                xVars.size() == 0)
            {
                return new ArrayList<Double>();
            }
            List<Double> result = new ArrayList<Double>();
            for(double[] currArr : xVars .values())
            {
            	result.add(currArr[intVarId]);
            }
            //return (from n in xVars select n.Value[intVarId]).ToList();
            return result;
        }
        catch (Exception ex)
        {
            Logger.Log(ex);
        }
        return new ArrayList<Double>();
    }    
    
    public static List<Double> GetVector(
            List<double[]> xVars,
            int intVarId)
    {
        try
        {
            if (xVars == null ||
                xVars.size() == 0)
            {
                return new ArrayList<Double>();
            }
            List<Double> result = new ArrayList<Double>();
            for(double[] currArr : xVars)
            {
            	result.add(currArr[intVarId]);
            }
            //return (from n in xVars select n.Value[intVarId]).ToList();
            return result;
        }
        catch (Exception ex)
        {
            Logger.Log(ex);
        }
        return new ArrayList<Double>();
    }    
    
    public static double GetAdjustedCoeffDeterm(
            List<Double> yVars,
            List<Double> residuals,
            int intVars)
        {
            try
            {
                double dblCoeffDeterm = GetCoeffDeterm(yVars, residuals);
                return dblCoeffDeterm - ((1.0 - dblCoeffDeterm)*(intVars/(yVars.size() - intVars - 1.0)));
            }
            catch (Exception ex)
            {
                Logger.Log(ex);
            }
            return Double.NaN;
        }
	
    public static double GetCoeffDeterm(
            List<Double> yData,
            List<Double> errors)
        {
            try
            {
                double dblYAvg = ListHelper.average(yData);
                double dblSsTot = 0;
                //double dblSsTot = (from n in yData select Math.Pow(n - dblYAvg, 2)).Sum();
                for(double dblVal : yData)
                {
                	dblSsTot += Math.pow(dblVal - dblYAvg, 2);
                }
                //double dblSsErr = (from n in errors select Math.Pow(n, 2)).Sum();
                double dblSsErr = 0;
                for(double dblVal : errors)
                {
                	dblSsErr += Math.pow(dblVal, 2);
                }
                
                return 1.0 - (dblSsErr/dblSsTot);
            }
            catch (Exception ex)
            {
                Logger.Log(ex);
            }
            return Double.NaN;
        }
    
    public static List<double[]> SelectVariables(
            List<double[]> xVars,
            List<Integer> selectedVariables)
        {
            try
            {
                if (xVars == null ||
                    xVars.size() == 0 ||
                    selectedVariables == null ||
                    selectedVariables.size() == 0)
                {
                    return new ArrayList<double[]>();
                }

                int intVars = xVars.get(0).length;
                if (intVars < selectedVariables.size())
                {
                    throw new HCException("Invalid number of vars");
                }

                List<double[]> listOut = new ArrayList<double[]>();
                for (double[] doublese : xVars)
                {
                    double[] currFeat = new double[selectedVariables.size()];
                    int i = 0;
                    for (int intSelectedVAr : selectedVariables)
                    {
                        currFeat[i] = doublese[intSelectedVAr];
                        i++;
                    }
                    listOut.add(currFeat);
                }
                return listOut;
            }
            catch (Exception ex)
            {
                Logger.Log(ex);
            }
            return new ArrayList<double[]>();
        }	
	
	public static List<TsRow2D> GetNormalizedLogReturns(
			int intSize, 
			List<TsRow2D> data) 
	{
		try
		{
			List<TsRow2D> stdLogReturns = new ArrayList<TsRow2D>(); 
			List<TsRow2D> tsRows = GetLogReturns(intSize, data);
			double dblMean = StatsHelper.getMean(tsRows);
			double dblStdDev = StatsHelper.getStdDev(tsRows, dblMean);
			for(TsRow2D tsRow2D : tsRows)
			{
				double dblX = (tsRow2D.Fx - dblMean) / dblStdDev;
				stdLogReturns.add(new TsRow2D(tsRow2D.Time, dblX));
			}
			return stdLogReturns;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return new ArrayList<TsRow2D>();
	}

    public static <T> SortedMap<T, Double> NormalizeFeatures(
            SortedMap<T, Double> features,
            double[] dblDelta,
            double[] dblMinVal)
        {
            dblMinVal[0] = Double.NaN;
            dblDelta[0] = Double.NaN;
            try
            {
                dblMinVal[0] = Double.MAX_VALUE;
                double dblMaxVal = -Double.MAX_VALUE;
                for (Entry<T, Double> keyValuePair : features.entrySet())
                {
                    dblMinVal[0] = Math.min(dblMinVal[0], keyValuePair.getValue());
                    dblMaxVal = Math.max(dblMaxVal, keyValuePair.getValue());
                }
                TreeMap<T, Double> newFeatMap = new TreeMap<T, Double>();
                dblDelta[0] = dblMaxVal - dblMinVal[0];
                for (Entry<T, Double> keyValuePair : features.entrySet())
                {
                    double currFeat = keyValuePair.getValue();
                    double newFeat;
                    if (dblDelta[0] == 0)
                    {
                        newFeat = 0;
                    }
                    else
                    {
                        newFeat = (currFeat - dblMinVal[0])/dblDelta[0];
                    }
                    if (!IsValid(newFeat))
                    {
                        throw new HCException("Invalid value");
                    }
                    newFeatMap.put(keyValuePair.getKey(), newFeat);
                }
                return newFeatMap;
            }
            catch (Exception ex)
            {
                Logger.Log(ex);
            }
            return new TreeMap<T, Double>();
        }	

    public static boolean IsValid(double dblVal)
    {
        try
        {
            return !Double.isNaN(dblVal) && 
            		!Double.isInfinite(dblVal);
        }
        catch (Exception ex)
        {
            Logger.Log(ex);
        }
        return false;
    }
    
	public static List<TsRow2D> GetLogReturns(
			int intSize, 
			List<TsRow2D> data) 
	{
        try
        {
            if (data == null ||
                data.size() == 0)
            {
                return new ArrayList<TsRow2D>();
            }
            List<TsRow2D> outData = new ArrayList<TsRow2D>();
            for (int i = intSize; i < data.size(); i++)
            {
                outData.add(
                    new TsRow2D(
                        data.get(i).Time,
                        Math.log(data.get(i).Fx / data.get(i - intSize).Fx)));
            }
            return outData;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return new ArrayList<TsRow2D>();
	}

	public static SortedMap<DateTime, Double> GetLogReturns(
			SortedMap<DateTime, Double> data,
			int intSize) 
	{
        try
        {
            if (data == null ||
                data.size() == 0)
            {
                return new TreeMap<DateTime, Double>();
            }
            TreeMap<DateTime, Double> outData = new TreeMap<DateTime, Double>();
            List<DateTime> dateList = new ArrayList<DateTime>(data.keySet());
            for (int i = intSize; i < data.size(); i++)
            {
            	DateTime currDate = dateList.get(i);
            	DateTime prevDate = dateList.get(i - intSize);
                outData.put(
                		currDate,
                        Math.log(data.get(currDate) / data.get(prevDate)));
            }
            return outData;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return new TreeMap<DateTime, Double>();
	}	
	
	public static List<TsRow2D> GetDerivative(
			int intSize, 
			List<TsRow2D> data) 
	{
        try
        {
            if (data == null ||
                data.size() == 0)
            {
                return new ArrayList<TsRow2D>();
            }
            List<TsRow2D> outData = new ArrayList<TsRow2D>();
            for (int i = intSize; i < data.size(); i++)
            {
                outData.add(
                    new TsRow2D(
                        data.get(i).Time,
                        data.get(i).Fx - data.get(i - intSize).Fx));
            }
            return outData;
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        return new ArrayList<TsRow2D>();
	}

	public static SortedMap<DateTime, TsRow2D> toSortedMap(
			List<TsRow2D> tsEventsList) 
	{
		try
		{
			SortedMap<DateTime, TsRow2D> sortedMap = new TreeMap<DateTime, TsRow2D>();
			for(TsRow2D tsRow2D : tsEventsList)
			{
				sortedMap.put(tsRow2D.getTime(), tsRow2D);
			}
			return sortedMap;
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        return new TreeMap<DateTime, TsRow2D>();
	}


	public static List<TsRow2D> cloneTsList(
			List<TsRow2D> tsList) 
	{
		try
		{
			if(tsList == null || 
			   tsList.size() == 0)
			{
				return new ArrayList<TsRow2D>();
			}
			List<TsRow2D> newList = new ArrayList<TsRow2D>();
			for(TsRow2D tsRow2D : tsList)
			{
				newList.add(tsRow2D.Clone());
			}
			return newList;
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        return new ArrayList<TsRow2D>();
	}


	public static List<TsRow2D> convertToTsList(
			SortedMap<DateTime, Double> sortedMap) 
	{
		try
		{
			List<TsRow2D> results = new ArrayList<TsRow2D>();
			for(Entry<DateTime, Double> kvp : sortedMap.entrySet())
			{
				results.add(new TsRow2D(kvp.getKey(), kvp.getValue()));
			}
			return results;
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        return new ArrayList<TsRow2D>();
	}


	public static double[] toDblArr(List<TsRow2D> currRawReturns) 
	{
		try
		{
			if(currRawReturns == null || 
					currRawReturns.size() == 0)
			{
				return null;
			}
			
			double[] results = new double[currRawReturns.size()];
			for (int i = 0; i < results.length; i++) 
			{
				results[i] = currRawReturns.get(i).Fx;
			}
			return results;
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
		return null;
	}


	public static double[][] toDblArr(
			SortedMap<DateTime, Double> compositeWeightsMap) 
	{
		try
		{
			if(compositeWeightsMap == null || 
					compositeWeightsMap.size() == 0)
			{
				return null;
			}
			
			double[][] result = new double[compositeWeightsMap.size()][1];
			int i = 0;
			for(Entry<DateTime, Double> kvp : compositeWeightsMap.entrySet())
			{
				result[i++][0] = kvp.getValue();
			}
			return result;
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
		return null;
	}


	public static SortedMap<DateTime, Double> CloneMap(
			SortedMap<DateTime, Double> map) 
	{
		try
		{
			SortedMap<DateTime, Double> newMap = 
					new TreeMap<DateTime, Double>();
			for(Entry<DateTime, Double> kvp : map.entrySet())
			{
				newMap.put(kvp.getKey(), kvp.getValue());
			}
			return newMap;
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
		return new TreeMap<DateTime, Double>();
	}

	public static <K,T> SortedMap<K, T> FilterMapByKeys(
			SortedMap<K, T> map, 
			List<K> dateSet) 
	{
        try
        {
            if (map == null ||
                map.size() == 0)
            {
                return new TreeMap<K, T>();
            }

            SortedMap<K, T> mapOut = new TreeMap<K, T>();
            for(Entry<K, T> kvp : map.entrySet())
            {
            	K currKey = kvp.getKey(); 
            	if(dateSet.contains(currKey))
            	{
            		mapOut.put(currKey, kvp.getValue());
            	}
            }
            return mapOut;
        }
        catch (Exception ex)
        {
            Logger.Log(ex);
        }
        return new TreeMap<K, T>();
	}
}
