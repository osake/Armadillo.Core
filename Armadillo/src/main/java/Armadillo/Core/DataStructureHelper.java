package Armadillo.Core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.joda.time.DateTime;

public class DataStructureHelper 
{
	
	public static SortedMap<DateTime, Double> aggregateMapAllLevels(
			SortedMap<DateTime, 
				SortedMap<String, 
					SortedMap<String, 
						SortedMap<String,Double>>>> map)
	{
		try
		{
			SortedMap<DateTime, Double> resultMap = 
					new TreeMap<DateTime, Double>();
			for(Entry<DateTime, 
					SortedMap<String, 
						SortedMap<String, 
							SortedMap<String, Double>>>> kvp : map.entrySet())
			{
				DateTime currDate = kvp.getKey();
				
				for(Entry<String, 
						SortedMap<String, 
							SortedMap<String, Double>>> kvp2 : kvp.getValue().entrySet())
				{
					for(Entry<String, 
							SortedMap<String, Double>> kvp3 : kvp2.getValue().entrySet())
					{
						for(Entry<String, Double> kvp4 : kvp3.getValue().entrySet())
						{
							double dblMappedValue = 0;
							if(resultMap.containsKey(currDate))
							{
								dblMappedValue = resultMap.get(currDate);
							}
							resultMap.put(currDate, dblMappedValue + kvp4.getValue());
						}
					}
				}
			}
			return resultMap;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new TreeMap<DateTime, Double>();
	}	
	
	public static SortedMap<DateTime, SortedMap<String,Double>> aggregateMapLevelOne(
			SortedMap<DateTime, 
				SortedMap<String, 
					SortedMap<String, 
						SortedMap<String,Double>>>> map)
	{
		try
		{
			SortedMap<DateTime, SortedMap<String,Double>> resultMap = 
					new TreeMap<DateTime, SortedMap<String,Double>>();
			for(Entry<DateTime, 
					SortedMap<String, 
						SortedMap<String, 
							SortedMap<String, Double>>>> kvp : map.entrySet())
			{
				DateTime currDate = kvp.getKey();
				SortedMap<String,Double> currMap = new TreeMap<String,Double>();
				resultMap.put(currDate, currMap);
				
				for(Entry<String, 
						SortedMap<String, 
							SortedMap<String, Double>>> kvp2 : kvp.getValue().entrySet())
				{
					String strKey = kvp2.getKey();
					for(Entry<String, SortedMap<String, Double>> kvp3 : kvp2.getValue().entrySet())
					{
						for(Entry<String, Double> kvp4 : kvp3.getValue().entrySet())
						{
							double dblMappedValue = 0;
							if(currMap.containsKey(strKey))
							{
								dblMappedValue = currMap.get(strKey);
							}
							currMap.put(strKey, dblMappedValue + kvp4.getValue());
						}
					}
				}
			}
			return resultMap;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new TreeMap<DateTime, SortedMap<String,Double>>();
	}
	
	public static SortedMap<DateTime, SortedMap<String,Double>> aggregateMapLevelTwo(
			SortedMap<DateTime, 
				SortedMap<String, 
					SortedMap<String, 
						SortedMap<String,Double>>>> map)
	{
		try
		{
			SortedMap<DateTime, SortedMap<String,Double>> resultMap = 
					new TreeMap<DateTime, SortedMap<String,Double>>();
			for(Entry<DateTime, 
					SortedMap<String, 
						SortedMap<String, 
							SortedMap<String, Double>>>> kvp : map.entrySet())
			{
				DateTime currDate = kvp.getKey();
				SortedMap<String,Double> currMap = new TreeMap<String,Double>();
				resultMap.put(currDate, currMap);
				
				for(Entry<String,
						SortedMap<String, 
							SortedMap<String, Double>>> kvp2 : kvp.getValue().entrySet())
				{
					for(Entry<String, 
							SortedMap<String, Double>> kvp3 : kvp2.getValue().entrySet())
					{
						String strKey = kvp3.getKey();
						for(Entry<String, Double> kvp4 : kvp3.getValue().entrySet())
						{
							double dblMappedValue = 0;
							if(currMap.containsKey(strKey))
							{
								dblMappedValue = currMap.get(strKey);
							}
							currMap.put(strKey, dblMappedValue + kvp4.getValue());
						}
					}
				}
			}
			return resultMap;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new TreeMap<DateTime, SortedMap<String,Double>>();
	}	

	public static SortedMap<DateTime, SortedMap<String,Double>> aggregateMapLevelThree(
			SortedMap<DateTime, 
				SortedMap<String, 
					SortedMap<String, 
						SortedMap<String,Double>>>> map)
	{
		try
		{
			SortedMap<DateTime, SortedMap<String,Double>> resultMap = 
					new TreeMap<DateTime, SortedMap<String,Double>>();
			for(Entry<DateTime, 
					SortedMap<String, 
						SortedMap<String, 
							SortedMap<String, Double>>>> kvp : map.entrySet())
			{
				DateTime currDate = kvp.getKey();
				SortedMap<String,Double> currMap = new TreeMap<String,Double>();
				resultMap.put(currDate, currMap);
				
				for(Entry<String,
						SortedMap<String, 
							SortedMap<String, Double>>> kvp2 : kvp.getValue().entrySet())
				{
					for(Entry<String, 
							SortedMap<String, Double>> kvp3 : kvp2.getValue().entrySet())
					{
						for(Entry<String, Double> kvp4 : kvp3.getValue().entrySet())
						{
							String strKey = kvp4.getKey();
							double dblMappedValue = 0;
							if(currMap.containsKey(strKey))
							{
								dblMappedValue = currMap.get(strKey);
							}
							currMap.put(strKey, dblMappedValue + kvp4.getValue());
						}
					}
				}
			}
			return resultMap;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new TreeMap<DateTime, SortedMap<String,Double>>();
	}	
	
	public static SortedMap<DateTime, SortedMap<String,Double>> flatToKeyedMap(
			SortedMap<DateTime, 
				SortedMap<String, 
					SortedMap<String, 
						SortedMap<String,Double>>>> map)
	{
		try
		{
			SortedMap<DateTime, SortedMap<String,Double>> resultMap = 
					new TreeMap<DateTime, SortedMap<String,Double>>();
			for(Entry<DateTime, 
					SortedMap<String, 
						SortedMap<String, 
							SortedMap<String, Double>>>> kvp : map.entrySet())
			{
				DateTime currDate = kvp.getKey();
				SortedMap<String,Double> currMap = new TreeMap<String,Double>();
				resultMap.put(currDate, currMap);
				
				for(Entry<String, 
						SortedMap<String, 
							SortedMap<String, Double>>> kvp2 : kvp.getValue().entrySet())
				{
					for(Entry<String, SortedMap<String, Double>> kvp3 : kvp2.getValue().entrySet())
					{
						for(Entry<String, Double> kvp4 : kvp3.getValue().entrySet())
						{
							String strKey = kvp2.getKey() + "%" + kvp3.getKey() + "%" + kvp4.getKey();
							currMap.put(strKey, kvp4.getValue());
						}
					}
				}
			}
			return resultMap;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new TreeMap<DateTime, SortedMap<String,Double>>();
	}
	
	public static HashMap<String, SortedMap<DateTime, Double>> getInvertedMap(
			SortedMap<DateTime, SortedMap<String, Double>> initialMap) 
	{
		try
		{
			HashMap<String, SortedMap<DateTime, Double>> invertedMap = new HashMap<String, SortedMap<DateTime, Double>>(); 
			for(Entry<DateTime, SortedMap<String, Double>> kvp : initialMap.entrySet())
			{
				DateTime currDate = kvp.getKey();
				for(Entry<String, Double> kvp2 : kvp.getValue().entrySet())
				{
					String strKey2 = kvp2.getKey();
					SortedMap<DateTime, Double> dateMap;
					if(!invertedMap.containsKey(strKey2))
					{
						dateMap = new TreeMap<DateTime, Double>(); 
						invertedMap.put(strKey2, dateMap);
					}
					else
					{
						dateMap = invertedMap.get(strKey2);
					}
					dateMap.put(currDate, kvp2.getValue());
				}
			}
			return invertedMap;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new HashMap<String, SortedMap<DateTime, Double>>();
	}	

	public static SortedMap<String, SortedMap<DateTime, Double>> getInvertedMap2(
			SortedMap<DateTime, SortedMap<String, Double>> initialMap) 
	{
		try
		{
			SortedMap<String, SortedMap<DateTime, Double>> invertedMap = new TreeMap<String, SortedMap<DateTime, Double>>(); 
			for(Entry<DateTime, SortedMap<String, Double>> kvp : initialMap.entrySet())
			{
				DateTime currDate = kvp.getKey();
				for(Entry<String, Double> kvp2 : kvp.getValue().entrySet())
				{
					String strKey2 = kvp2.getKey();
					SortedMap<DateTime, Double> dateMap;
					if(!invertedMap.containsKey(strKey2))
					{
						dateMap = new TreeMap<DateTime, Double>(); 
						invertedMap.put(strKey2, dateMap);
					}
					else
					{
						dateMap = invertedMap.get(strKey2);
					}
					dateMap.put(currDate, kvp2.getValue());
				}
			}
			return invertedMap;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new TreeMap<String, SortedMap<DateTime, Double>>();
	}	
	
	public static List<double[]> cloneList(List<double[]> xData) 
	{
		List<double[]> result = new ArrayList<double[]>();
		for(double[] currArr : xData)
		{
			result.add(currArr.clone());
		}
		return result;
	}

	public static SortedMap<String, SortedMap<DateTime, Double>> getInvertedMap1(
			SortedMap<DateTime, SortedMap<String, Double>> map) 
	{
		try
		{
			SortedMap<String, SortedMap<DateTime, Double>> invertedMap = 
					new TreeMap<String, SortedMap<DateTime, Double>>();
			for(Entry<DateTime, SortedMap<String, Double>> kvp : map.entrySet())
			{
				DateTime currDate = kvp.getKey();
				for(Entry<String, Double> kvp2 : kvp.getValue().entrySet())
				{
					String strCurrKey = kvp2.getKey();
					SortedMap<DateTime, Double> currMap;
					if(!invertedMap.containsKey(strCurrKey))
					{
						currMap = new TreeMap<DateTime, Double>();
						invertedMap.put(strCurrKey, currMap);
					}
					else
					{
						currMap = invertedMap.get(strCurrKey);
					}
					currMap.put(currDate, kvp2.getValue());
				}
			}
			return invertedMap;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new TreeMap<String, SortedMap<DateTime, Double>>();
	}
}
