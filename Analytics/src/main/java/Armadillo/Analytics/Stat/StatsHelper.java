package Armadillo.Analytics.Stat;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.Math.TsRow2D;

public class StatsHelper 
{
    // SKEWNESS
    // Static Methods
    // Moment skewness of a 1D array of doubles
    public static double momentSkewness(double[] aa)
    {
    	try
    	{
	        int n = aa.length;
	        double denom = (n - 1);
	        double sum = 0.0D;
	        double mean = getMean(aa);
	        for (int i = 0; i < n; i++)
	        {
	            sum += Math.pow((aa[i] - mean), 3);
	        }
	        sum = sum/denom;
	        return sum/Math.pow(
	                       getStdDev(aa), 3);
	    }
	    catch (Exception ex)
	    {
	        ex.printStackTrace();
	    }
		return 0;
    }
    
	
	public static double getStdDev(List<TsRow2D> tsRows)
	{
		try
		{
			if(tsRows == null || tsRows.size() == 0)
			{
				return 0;
			}
			double dblMean = getMean(tsRows);
			return getStdDev(tsRows, dblMean);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
		return 0;
	}

	public static double getStdDevFromList(List<Double> tsRows)
	{
		try
		{
			if(tsRows == null || tsRows.size() == 0)
			{
				return 0;
			}
			double dblMean = getMeanFromList(tsRows);
			return getStdDevFromList(tsRows, dblMean);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
		return 0;
	}
	
	public static double getStdDev(double[] tsRows)
	{
		try
		{
			if(tsRows == null || tsRows.length == 0)
			{
				return 0;
			}
			double dblMean = getMean(tsRows);
			return getStdDev(tsRows, dblMean);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
		return 0;
	}
	
	public static double getVarianceFromList(List<Double> tsRows)
	{
		try
		{
			if(tsRows == null || tsRows.size() == 0)
			{
				return 0;
			}
			double dblMean = getMeanFromList(tsRows);
			return getVar(tsRows, dblMean);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
		return 0;
	}
	
	public static double getVar(List<Double> tsRows, double dblMean) 
	{
		try
		{
			double dblSumSq = 0;
			for(double tsRow : tsRows)
			{
				dblSumSq += Math.pow(tsRow - dblMean, 2);
			}
			return dblSumSq / (tsRows.size() - 1.0);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
		return 0;
	}
	
	public static double getStdDev(
			List<TsRow2D> tsRows, 
			double dblMean) 
	{
		try
		{
			double dblSumSq = 0;
			for(TsRow2D tsRow : tsRows)
			{
				dblSumSq += Math.pow(tsRow.Fx - dblMean, 2);
			}
			return Math.sqrt(dblSumSq / (tsRows.size() - 1.0));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
		return 0;
	}

	public static double getStdDevFromList(
			List<Double> tsRows, 
			double dblMean) 
	{
		try
		{
			double dblSumSq = 0;
			for(double tsRow : tsRows)
			{
				dblSumSq += Math.pow(tsRow - dblMean, 2);
			}
			return Math.sqrt(dblSumSq / (tsRows.size() - 1.0));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
		return 0;
	}
	
	public static double getStdDev(
			double[] tsRows, 
			double dblMean) 
	{
		try
		{
			double dblSumSq = 0;
			for(double tsRow : tsRows)
			{
				dblSumSq += Math.pow(tsRow - dblMean, 2);
			}
			return Math.sqrt(dblSumSq / (tsRows.length - 1.0));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
		return 0;
	}
	
	public static double getMean(List<TsRow2D> tsRows)
	{
		try
		{
			if(tsRows == null || tsRows.size() == 0)
			{
				return 0;
			}
			double dblSum = 0;
			for(TsRow2D tsRow : tsRows)
			{
				dblSum += tsRow.Fx;
			}
			return dblSum / tsRows.size();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
		return 0;
	}

	public static double getMeanFromList(List<Double> list) 
	{
		if(list == null || list.size() == 0)
		{
			return 0;
		}
		double dblMean = 0;
		for(double dbl : list)
		{
			dblMean += dbl;
		}
		return dblMean / list.size();
	}
	
    public static double GetCorrelation(
            List<Double> list1,
            List<Double> list2)
    {
    	try
    	{
            double dblSx = 0;
            double dblSx2 = 0;
            double dblSxy = 0;
            double dblSy = 0;
            double dblSy2 = 0;
            int intN = 0;

            if (list1.size() != list2.size())
            {
                throw new HCException("Invalid list size");
            }

            for (int i = 0; i < list1.size(); i++)
            {
                double dblVal1 = list1.get(i);
                double dblVal2 = list2.get(i);
                dblSx += dblVal1;
                dblSx2 += dblVal1 * dblVal1;
                dblSxy += dblVal1 * dblVal2;
                dblSy += dblVal2;
                dblSy2 += dblVal2 * dblVal2;
                intN++;
            }

            double dblCorrelation = 0;

            if (!(dblSxy == 0 && (dblSx == 0 || dblSy == 0)))
            {
                dblCorrelation = (intN * dblSxy - dblSx * dblSy) /
                    Math.sqrt((intN * dblSx2 - dblSx * dblSx) * (intN * dblSy2 - dblSy * dblSy));
            }

            if (Double.isNaN(dblCorrelation) || Double.isInfinite(dblCorrelation))
            {
                return 0;
            }

            if (dblCorrelation < -1.0)
            {
                return -1;
            }
            if (dblCorrelation > 1.0)
            {
                return 1;
            }
            return dblCorrelation;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return 0;
    }

    public static double GetCorrelationFromTs(
            List<TsRow2D> list1,
            List<TsRow2D> list2)
    {
    	try
    	{
            double dblSx = 0;
            double dblSx2 = 0;
            double dblSxy = 0;
            double dblSy = 0;
            double dblSy2 = 0;
            int intN = 0;

            if (list1.size() != list2.size())
            {
                throw new HCException("Invalid list size");
            }

            for (int i = 0; i < list1.size(); i++)
            {
            	if(list1.get(i).getTime().getMillis() != list2.get(i).getTime().getMillis())
            	{
            		throw new HCException("Invalid date");
            	}
            	
                double dblVal1 = list1.get(i).Fx;
                double dblVal2 = list2.get(i).Fx;
                dblSx += dblVal1;
                dblSx2 += dblVal1 * dblVal1;
                dblSxy += dblVal1 * dblVal2;
                dblSy += dblVal2;
                dblSy2 += dblVal2 * dblVal2;
                intN++;
            }

            double dblCorrelation = 0;

            if (!(dblSxy == 0 && (dblSx == 0 || dblSy == 0)))
            {
                dblCorrelation = (intN * dblSxy - dblSx * dblSy) /
                    Math.sqrt((intN * dblSx2 - dblSx * dblSx) * (intN * dblSy2 - dblSy * dblSy));
            }

            if (Double.isNaN(dblCorrelation) || Double.isInfinite(dblCorrelation))
            {
                return 0;
            }

            if (dblCorrelation < -1.0)
            {
                return -1;
            }
            if (dblCorrelation > 1.0)
            {
                return 1;
            }
            return dblCorrelation;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return 0;
    }
    
	public static List<double[]> resample(
			List<double[]> xData,
			int intNumberSamples) 
	{
		try
		{
			if(xData == null || xData.size() == 0 )
			{
				return new ArrayList<double[]>();
			}
			List<double[]> result = new ArrayList<double[]>(xData);
			RngWrapper rng = new RngWrapper();
			int intN = xData.size();
			for (int i = 0; i < intNumberSamples; i++) 
			{
				int intIndex = rng.NextInt(0, intN - 1);
				result.add(xData.get(intIndex).clone());
			}
			return result;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ArrayList<double[]>();
	}

	public static List<Double> resample2(
			List<Double> xData, 
			int intNumberSamples) 
	{
		try
		{
			if(xData == null || xData.size() == 0 )
			{
				return new ArrayList<Double>();
			}
			List<Double> result = new ArrayList<Double>(xData);
			RngWrapper rng = new RngWrapper();
			int intN = xData.size();
			for (int i = 0; i < intNumberSamples; i++) 
			{
				int intIndex = rng.NextInt(0, intN - 1);
				result.add(xData.get(intIndex));
			}
			return result;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ArrayList<Double>();
	}

	public static double getVariance(double[] tsRows) 
	{
		try
		{
			if(tsRows == null || tsRows.length == 0)
			{
				return 0;
			}
			double dblMean = getMean0(tsRows);
			return getVar(tsRows, dblMean);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
		return 0;
	}

	private static double getVar(double[] tsRows, double dblMean) 
	{
		try
		{
			double dblSumSq = 0;
			for(double tsRow : tsRows)
			{
				dblSumSq += Math.pow(tsRow - dblMean, 2);
			}
			return dblSumSq / (tsRows.length - 1.0);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
		return 0;
	}

	private static double getMean0(double[] tsRows) 
	{
		try
		{
			if(tsRows == null || tsRows.length == 0)
			{
				return 0;
			}
			double dblSum = 0;
			for(double tsRow : tsRows)
			{
				dblSum += tsRow;
			}
			return dblSum / tsRows.length;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
		return 0;
	}

	public static double getMean(double[] y) 
	{
		try
		{
			if(y == null || y.length == 0)
			{
				return 0;
			}
			double dblSum = 0;
			for (int i = 0; i < y.length; i++) 
			{
				dblSum+= y[i];
			}
			return dblSum / y.length;
	    }
	    catch (Exception ex)
	    {
	        ex.printStackTrace();
	    }
		return 0;
	}


	public static double momentSkewness(List<Double> aa) 
	{
    	try
    	{
	        int n = aa.size();
	        double denom = (n - 1);
	        double sum = 0.0D;
	        double mean = getMeanFromList(aa);
	        for (int i = 0; i < n; i++)
	        {
	            sum += Math.pow((aa.get(i) - mean), 3);
	        }
	        sum = sum/denom;
	        return sum/Math.pow(
	                       getStdDevFromList(aa), 3);
	    }
	    catch (Exception ex)
	    {
	        ex.printStackTrace();
	    }
		return 0;
	}


	public static double kurtosis(List<Double> aa) 
	{
		try
		{
            int n = aa.size();
            double denom = (n - 1);
            double sum = 0.0D;
            double mean = getMeanFromList(aa);
            for (int i = 0; i < n; i++)
            {
                sum += Math.pow((aa.get(i) - mean), 4);
            }
            sum = sum/denom;
            return sum/Math.pow(getVarianceFromList(aa), 2);
	    }
	    catch (Exception ex)
	    {
	        ex.printStackTrace();
	    }
		return 0;
	}	
}
