package Armadillo.Analytics.TimeSeries;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Core.Logger;

public class ListHelper 
{
	public static List<double[]> getListOfArrays(List<Double> list)
	{
		try
		{
			List<double[]> listOfArrays = new ArrayList<double[]>();
			for (int i = 0; i < list.size(); i++) 
			{
				listOfArrays.add(new double[] { list.get(i)});
			}
			return listOfArrays;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ArrayList<double[]>();
	}
	
	public static double average(List<Double> list)
	{
		try
		{
			if(list == null || list.size() == 0)
			{
				return 0;
			}
			double dblSum = 0;
			for (int i = 0; i < list.size(); i++) 
			{
				dblSum += list.get(i);
			}
			return dblSum / list.size();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return 0;
	}

	public static double max(List<Double> allObjValues) 
	{
		try
		{
			if(allObjValues == null || allObjValues.size() == 0)
			{
				return 0;
			}
			double dblMax = -Double.MAX_VALUE;
			for (Double double1 : allObjValues) 
			{
				dblMax = Math.max(double1, dblMax);
			}
			return dblMax;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return 0;
	}
	
	public static double min(List<Double> allObjValues) 
	{
		try
		{
			if(allObjValues == null || allObjValues.size() == 0)
			{
				return 0;
			}
			double dblMin = -Double.MAX_VALUE;
			for (Double double1 : allObjValues) 
			{
				dblMin = Math.min(double1, dblMin);
			}
			return dblMin;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return 0;
	}

	public static double sum(int[] arr) 
	{
		double dblSum = 0;
		for (int i = 0; i < arr.length; i++) 
		{
			dblSum+= arr[i];
		}
		return dblSum;
	}

	public static double sum(double[] arr) 
	{
		double dblSum = 0;
		for (int i = 0; i < arr.length; i++) 
		{
			dblSum += arr[i];
		}
		return dblSum;
	}
}
