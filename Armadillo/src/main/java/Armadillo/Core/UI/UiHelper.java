package Armadillo.Core.UI;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import Armadillo.Core.Logger;
import Armadillo.Core.Concurrent.ProducerConsumerQueue;
import Armadillo.Core.Concurrent.Task;
import Armadillo.Core.Reflection.ReflectionCache;
import Armadillo.Core.Reflection.Reflector;
import Armadillo.Core.Text.StringHelper;

public class UiHelper 
{
	private static Reflector m_tableRowReflector;
	private static ProducerConsumerQueue<AUiWorker> m_queue;
	private static int m_intMaxNumCols;
	
	static
	{
		try
		{
			loadUiQueue();
			m_tableRowReflector = ReflectionCache.getReflector(TableRow.class);
			Method[] methods = m_tableRowReflector.getMethods();
			Hashtable<String, Object> methodsNameMap = new Hashtable<String, Object>(); 
			if(methods != null && methods.length > 0)
			{
				for (int i = 0; i < methods.length; i++) 
				{
					String strMethodName = methods[i].getName();
					methodsNameMap.put(strMethodName, new Object());
				}
			}
			int i = 0;
			for (; i < 1000000; i++) 
			{
				String strMethodName = "getCol" + (i + 1);
				if(!methodsNameMap.containsKey(strMethodName))
				{
					break;
				}
			}
			m_intMaxNumCols = i;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public static void doLoadColumnItems(
			Reflector reflector,
			HashMap<String, LabelClass> m_labelClassesMap,
			ArrayList<LabelClass> m_lblClassess) 
	{
		try
		{
	    	String[] colNames = reflector.getColNames();
			
			for(String strCol : colNames)
			{
				LabelClass labelClass = new LabelClass(
						strCol, 
						"",
						((Class<?>)reflector.getPropertyType(strCol)));
				m_labelClassesMap.put(strCol, labelClass);
				m_lblClassess.add(labelClass);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public static void doLoadColumnItems(
			List<ColumnModel> columns,
			HashMap<String, LabelClass> m_labelClassesMap,
			ArrayList<LabelClass> m_lblClassess) 
	{
		try
		{
			for(ColumnModel columnModel : columns)
			{
				Object objVal = columnModel.getValue();
				Class<?> type = null;
				if(objVal == null)
				{
					type = String.class;
				}
				else
				{
					type = objVal.getClass();
				}
				String strCol = columnModel.getHeader();
				LabelClass labelClass = new LabelClass(
						strCol, 
						"",
						type);
				m_labelClassesMap.put(strCol, labelClass);
				m_lblClassess.add(labelClass);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	

	public static int getMaxNumCols()
	{
		return m_intMaxNumCols;		
	}

	private static void loadUiQueue() 
	{
		try
		{
			m_queue = new ProducerConsumerQueue<AUiWorker>(4)
						{
							@Override
							public void runTask(AUiWorker uiWorker) 
							{
								try 
								{
									uiWorker.Work();
								} 
								catch (Exception ex) 
								{
									Logger.log(ex);
								}
							}
				
						};
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public static Task enqueueGuiTask(AUiWorker uiWorker)
	{
		try
		{
			Task task = m_queue.add(uiWorker);
			uiWorker.setTask(task);
			return task;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
	
	public static String getStdKey(TableRow tableRow)
	{
		try
		{
			if(tableRow == null)
			{
				return "";
			}
			String[] cols = m_tableRowReflector.getColNames();
			Object obj = m_tableRowReflector.getPropValue(tableRow, cols[0]);
			StringBuilder sb = new StringBuilder(); 
			if(obj != null)
			{
				sb.append(obj.toString());
			}
			else
			{
				sb.append("");
			}
			for (int i = 1; i < cols.length; i++) 
			{
				obj = m_tableRowReflector.getPropValue(tableRow, cols[i]);
				if(obj != null)
				{
					sb.append("|" + obj.toString());
				}
				else
				{
					sb.append("|");
				}
			}
			String strResult = sb.toString().intern();
			return strResult;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
	}
	
	public static List<String[]> generateStringList(
			Collection<TableRow> collection,
			int intCols)
	{
		try
		{
			if(collection == null)
			{
				return new ArrayList<String[]>();
			}
			String[] colNames = m_tableRowReflector.getColNames();
			List<String[]> results = new ArrayList<String[]>();
			for(TableRow tableRow : collection)
			{
				String[] currRowStr = new String[intCols]; 
				for(int i = 0; i < intCols; i++)
				{
					String strColName = colNames[i];
					Object objVal = m_tableRowReflector.getPropValue(tableRow, strColName);
					if(objVal != null)
					{
						currRowStr[i] = objVal.toString();
					}
				}
				results.add(currRowStr);
			}
			return results;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ArrayList<String[]>();
	}


	public static TableRow generateTableRow(
			Reflector reflector,
			Object obj,
			String strKey) 
	{
		try
		{
			if(obj == null)
			{
				return null;
			}
			String[] tableRowCols = m_tableRowReflector.getColNames();
			TableRow tableRow = null;
			Object[] objs = reflector.getPropValues(obj);
			if(objs != null)
			{
				tableRow = new TableRow();
				for (int i = 1; i <= objs.length; i++) // zero is the key index 
				{
					Object currObj = objs[i-1];
					if(currObj != null)
					{
						m_tableRowReflector.SetPropertyValue(
								tableRow, 
								tableRowCols[i], 
								currObj);
					}
				}
			}
			tableRow.setKey(strKey);
			return tableRow;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}

	public static TableRow generateTableRow(
			Reflector reflector,
			String[] objs,
			String strKey) 
	{
		try
		{
			if(objs == null)
			{
				return null;
			}
			TableRow tableRow = null;
			String[] tableRowCols = ReflectionCache.getReflector(TableRow.class).getColNames();
			if(objs != null)
			{
				tableRow = new TableRow();
				for (int i = 0; i < Math.min(tableRowCols.length-1, objs.length); i++) 
				{
					Object currObj = objs[i];
					if(currObj != null)
					{
						m_tableRowReflector.SetPropertyValue(
								tableRow, 
								tableRowCols[i+1], // zero is the key column 
								currObj);
					}
				}
			}
			
			tableRow.setKey(strKey);
			return tableRow;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
	
	public static ArrayList<TableRow> generateTableRowList(
			List<String[]> dataList)
	{
		try
		{
			ArrayList<TableRow> tableRows = new ArrayList<TableRow>(); 
			for (String[] stringValues : dataList) 
			{
	
				TableRow dummyRow = 
						(TableRow) m_tableRowReflector.createInstance();
				dummyRow.key = UUID.randomUUID().toString();
				
				for (int i = 0; i < stringValues.length; i++) 
				{
					String strCurrVal = stringValues[i];
					if(!StringHelper.IsNullOrEmpty(strCurrVal))
					{
						String strDummyCol = "col" + (i + 1);
						m_tableRowReflector.SetPropertyValue(
								dummyRow, 
								strDummyCol,
								strCurrVal.intern());
					}
				}
	
				tableRows.add(dummyRow);
			}
			return tableRows;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ArrayList<TableRow>();
	}
	
	public static String getNodeKey(
		String str1,
		String str2,
		String str3)
	{

		try
		{
			if(StringHelper.IsNullOrEmpty(str1) ||
			   StringHelper.IsNullOrEmpty(str2) ||
			   StringHelper.IsNullOrEmpty(str3))
			{
				return "";
			}
			return str1 + "|" +
					str2 + "|" +
					str3;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
	}

	public static void replaceValues(
			Object obj, 
			TableRow tableRow, 
			Reflector reflector) 
	{
		try
		{
			if(obj == null)
			{
				return;
			}
			Object[] propValues = reflector.getPropValues(obj);
			String[] colNames = m_tableRowReflector.getColNames();
			for (int i = 0; i < propValues.length; i++) 
			{
				m_tableRowReflector.SetPropertyValue(
						tableRow, 
						colNames[i], 
						propValues[i]);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public static void replaceValues(
			String[] propValues, 
			TableRow tableRow, 
			Reflector reflector) 
	{
		try
		{
			if(propValues == null)
			{
				return;
			}
			String[] colNames = m_tableRowReflector.getColNames();
			for (int i = 0; i < Math.min(propValues.length, colNames.length - 1); i++) 
			{
				String strCurrVal = propValues[i];
				if(StringHelper.IsNullOrEmpty(strCurrVal))
				{
					strCurrVal = "";
				}
				m_tableRowReflector.SetPropertyValue(
						tableRow, 
						colNames[i + 1], // the first column should be the key 
						strCurrVal);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
}
