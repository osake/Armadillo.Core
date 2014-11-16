package Web.Dashboard;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.primefaces.model.chart.BubbleChartSeries;
import org.primefaces.model.chart.ChartSeries;

import Armadillo.Core.Logger;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.UI.ColumnModel;
import Armadillo.Core.UI.TableRow;
import  Utils.Gui.AUiTableItem;
import Web.Base.WebHelper;
import Web.Chart.AUiChartItem;

public class DataExporterHelper 
{
	public static String getExportFileName(
			String strTabName) 
	{
		try
		{
			if(!StringHelper.IsNullOrEmpty(strTabName))
			{
				return WebHelper.getTableId(strTabName);
			}
		}
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return "empty";
	}
	
	public static String getOutputString(DynamicGuiInstanceWrapper tabInstanceWrapper) 
	{
		try
		{
			//DynamicGuiInstanceWrapper tabInstanceWrapper = m_dynamicGuiInstanceWrapper;
			if(tabInstanceWrapper == null)
			{
				return "";
			}
			
			AUiTableItem uiItem = tabInstanceWrapper.getUiTableItem();
			if(uiItem == null)
			{
				return "";
			}
			
			StringBuilder sb = new StringBuilder(); 
			if(AUiChartItem.class.isAssignableFrom(uiItem.getClass()))
			{
				getChartSb(uiItem, sb);
			}
			else if(AUiTableItem.class.isAssignableFrom(uiItem.getClass()))
			{
				getTableSb(uiItem, sb);
			}
			String str = sb.toString();
			
			return str;
		}
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		
		return "";
	}

	private static void getTableSb(AUiTableItem uiItem, StringBuilder sb) 
	{
		try
		{
			int intCols = uiItem.getColCount();
			synchronized(uiItem.getLockObj())
			{
				List<ColumnModel> cols = uiItem.getColumns();
				for(ColumnModel columnModel : cols)
				{
					for (int i = 0; i < intCols; i++) 
					{
						String strToken = columnModel.getHeader().replace(",", "");
						sb.append(strToken + ",");
					}
					sb.append("\n");
				}
				
				for(TableRow tableRow : uiItem.getTableRows())
				{
					String[] rows = tableRow.getRows();
					for (int i = 0; i < intCols; i++) 
					{
						String strToken = rows[i].replace(",", "");
						sb.append(strToken + ",");
					}
					sb.append("\n");
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	private static void getChartSb(
			AUiTableItem uiItem, 
			StringBuilder sb) 
	{
		try
		{
			AUiChartItem uiChartItem = (AUiChartItem)uiItem;
			Map<String, Serializable> chartSeries = uiChartItem.getChartSeries();
			if(chartSeries == null ||
					chartSeries.size() == 0)
			{
				return;
			}
			
			TreeMap<String, TreeMap<String,String>> outMap = 
					new TreeMap<String, TreeMap<String,String>>(); 
			TreeMap<String,String> xMap = new TreeMap<String,String>();
			synchronized(uiItem.getLockObj())
			{
				if(uiChartItem.getChartType() == EnumChartType.BubbleChart)
				{
					for(Serializable  serializable: chartSeries.values())
					{
						BubbleChartSeries currChartSeries = (BubbleChartSeries)serializable;
						TreeMap<String,String> seriesMap = new TreeMap<String,String>();
						outMap.put(currChartSeries.getLabel(), seriesMap);
						xMap.put(currChartSeries.getX() + "|" + currChartSeries.getRadius(), currChartSeries.getY() + "");
					}
				}
				else
				{
					for(Serializable  serializable: chartSeries.values())
					{
						ChartSeries currChartSeries = (ChartSeries)serializable;
						TreeMap<String,String> seriesMap = new TreeMap<String,String>();
						outMap.put(currChartSeries.getLabel(), seriesMap);
						for(Entry<Object, Number> kvp : currChartSeries.getData().entrySet())
						{
							String strKey = kvp.getKey().toString();
							seriesMap.put(strKey, kvp.getValue() + "");
							if(!xMap.containsKey(strKey))
							{
								xMap.put(strKey, "");
							}
						}
					}
				}
			}
			//
			// load titles
			//
			sb.append("xAxis," + StringHelper.join(outMap.keySet(), ",") + "\n");
			//
			// write text map
			//
			for(String strX: xMap.keySet())
			{
				String[] outArr = new String[outMap.size()];
				int i = 0;
				for(Entry<String, TreeMap<String, String>> kvp : outMap.entrySet())
				{
					String strCurrVal = "";
					if(kvp.getValue().containsKey(strX))
					{
						strCurrVal = kvp.getValue().get(strX);
					}
					outArr[i] = strCurrVal;
					i++;
				}
				sb.append(
						strX + "," +
						StringHelper.join(outArr, ",") + "\n");
			}
		}
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
}
