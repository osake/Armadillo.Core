package Web.Tests;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.primefaces.model.chart.BubbleChartSeries;

import Armadillo.Core.Logger;
import Web.Chart.AUiChartItem;
import Web.Dashboard.EnumChartType;

public class UiBubbleChartTest extends AUiChartItem
{
	
	private HashMap<String, Serializable> m_map;
	private String m_strSeriesName = "testSerie";
	private int i = 0;
	
	public UiBubbleChartTest()
	{
		setChartType(EnumChartType.BubbleChart);
	}
	
	private void loadData()
	{
		try
		{
			m_map = new HashMap<String, Serializable>();
			
			for (; i < 50; i++) 
			{
		        BubbleChartSeries chartSeries = new BubbleChartSeries(m_strSeriesName);
				chartSeries.setLabel(m_strSeriesName);
				chartSeries.setX(i*20);
				chartSeries.setY(i*20);
				chartSeries.setRadius(5);
				chartSeries.setLabel(i + "_label");
				m_map.put(m_strSeriesName + "_" + i, chartSeries);
			}
			Thread.sleep(6000);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}		
	}

	@Override
	protected Map<String, Serializable> generateChartSeries() 
	{
		loadData();
		//loadThreadWorker();

		return m_map;
	}


	@Override
	public String getReportTitle() 
	{
		return null;
	}

	@Override
	public String[] getReportTreeLabels() 
	{
		return new String[]
				{
					"Test",
					"Chart",
					"BubbleChartTest"
				};
	}
}
