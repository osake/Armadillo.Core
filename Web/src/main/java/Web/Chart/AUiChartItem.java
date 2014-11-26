package Web.Chart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.primefaces.component.chart.Chart;
import org.primefaces.model.chart.BubbleChartSeries;
import org.primefaces.model.chart.ChartModel;
import org.primefaces.model.chart.LineChartSeries;

import Armadillo.Analytics.TextMining.Searcher;
import Armadillo.Core.Logger;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.UI.TableRow;
import  Utils.Gui.AUiTableItem;
import Web.Dashboard.EnumChartType;

public abstract class AUiChartItem  extends AUiTableItem
{
	private Map<String, Serializable> m_chartSeriesMap;
	private Map<String, Double> m_lastValueSeries = new ConcurrentHashMap<String, Double>();
	private ChartModel m_chartModel;
	private Chart m_lineChart;
	//private double m_dblMaxXVal;
	//private double m_dblMinXVal;
	private EnumChartType m_chartType;

	public void setChartSeries(Map<String, Serializable> chartSeries)
	{
		m_chartSeriesMap = chartSeries;
	}
	
	public Map<String, Serializable> getChartSeries()
	{
		try
		{
			if(m_chartSeriesMap == null)
			{
				synchronized (m_lockObj) 
				{
					if(m_chartSeriesMap == null)
					{
						m_chartSeriesMap = generateChartSeries();
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_chartSeriesMap;
	}
	
	@Override
	public String[] getFieldNames() {
		return null;
	}

	@Override
	protected String getObjKey(TableRow obj) 
	{
		return null;
	}
	
	
	@Override
	public List<TableRow> generateTableRows() {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected abstract Map<String, Serializable> generateChartSeries();


	@Override
	protected Class<?> getParamsClass() 
	{
		return null;
	}

	@Override
	protected Searcher generateSearcher() 
	{
		return null;
	}

	@Override
	protected List<String> generateKeys() 
	{
		return new ArrayList<String>();
	}

	public void setChartModel(ChartModel cartesianChartModel)
	{
		m_chartModel = cartesianChartModel;
	}
	
	public ChartModel getChartModel() 
	{
		return m_chartModel;
	}

	public void populateBubbleChartRow(
			String strSeriesName, 
			int intX, 
			int intY,
			int intRadius,
			String strLabel) 
	{
		try
		{
			if(m_chartSeriesMap == null)
			{
				m_chartSeriesMap = new ConcurrentHashMap<String, Serializable>();
			}
			
			BubbleChartSeries chartSeries;
			if(!m_chartSeriesMap.containsKey(strSeriesName))
			{
				chartSeries = new BubbleChartSeries(strSeriesName);
				chartSeries.setLabel(strSeriesName);
				m_chartSeriesMap.put(strSeriesName, chartSeries);
			}
			else
			{
				chartSeries = (BubbleChartSeries)m_chartSeriesMap.get(strSeriesName);
			}
			chartSeries.setX(intX);
			chartSeries.setY(intY);
			chartSeries.setRadius(intRadius);
			if(!StringHelper.IsNullOrEmpty(strLabel))
			{
				chartSeries.setLabel(strLabel);
			}
			m_blnHasChanged = true;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public void populateLineChartRow(
			String strSeriesName, 
			String strX, 
			double dblY) 
	{
		try
		{
			if(m_chartSeriesMap == null)
			{
				m_chartSeriesMap = new ConcurrentHashMap<String, Serializable>();
			}
			
			LineChartSeries chartSeries;
			if(!m_chartSeriesMap.containsKey(strSeriesName))
			{
				chartSeries = new LineChartSeries(strSeriesName);
				chartSeries.setLabel(strSeriesName);
				m_chartSeriesMap.put(strSeriesName, chartSeries);
			}
			else
			{
				chartSeries = (LineChartSeries)m_chartSeriesMap.get(strSeriesName);
			}
			chartSeries.set(strX, dblY);
			m_lastValueSeries.put(strSeriesName, dblY);
			
			//
			// make sure each series contain the same value
			//
			for(Entry<String, Serializable> kvp : m_chartSeriesMap.entrySet())
			{
				LineChartSeries currChartSeries = (LineChartSeries)kvp.getValue();
				if(!currChartSeries.getData().containsKey(strX))
				{
					double dblLastValue = 0;
					if(m_lastValueSeries.containsKey(strSeriesName))
					{
						dblLastValue = m_lastValueSeries.get(strSeriesName);
					}
					currChartSeries.set(strX, dblLastValue);
				}
			}
			
			m_blnHasChanged = true;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public void setChart(Chart lineChart) 
	{
		m_lineChart = lineChart;
	}
	
//	public void SetXMaxVal(double dblXVal)
//	{
//		try
//		{
//			m_dblMaxXVal = Math.max(m_dblMaxXVal, dblXVal);
//			m_dblMinXVal = Math.min(m_dblMinXVal, dblXVal);
//			if(m_lineChart != null)
//			{
//				m_lineChart.setMaxX(m_dblMaxXVal);
//				m_lineChart.setMinX(m_dblMaxXVal);
//			}
//		}
//		catch(Exception ex)
//		{
//			Logger.log(ex);
//		}
//	}
	
	public Chart getChart()
	{
		return m_lineChart;
	}

	public EnumChartType getChartType() 
	{
		return m_chartType;
	}
	
	public void setChartType(EnumChartType enumChartType) 
	{
		m_chartType = enumChartType;
	}
	
}
