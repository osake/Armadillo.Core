package Web.Tests;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.primefaces.model.chart.LineChartSeries;

import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Core.Concurrent.ThreadWorker;
import Web.Base.LiveGuiPublisher;
import Web.Chart.AUiChartItem;

public class UiLineChartTest extends AUiChartItem
{
	
	private HashMap<String, Serializable> m_map;
	private String m_strSeriesName = "testSerie";
	private int i = 0;
	private ThreadWorker<ObjectWrapper> m_worker;
	
	public UiLineChartTest()
	{
		//loadData();
		loadThreadWorker();
	}
	
	private void loadData()
	{
		try
		{
			m_map = new HashMap<String, Serializable>();
			
	        LineChartSeries lineChartSeries = new LineChartSeries(m_strSeriesName);
			lineChartSeries.setLabel(m_strSeriesName);
			for (; i < 50; i++) 
			{
				lineChartSeries.set(i + "", i);
			}
			Thread.sleep(6000);
			m_map.put(m_strSeriesName, lineChartSeries);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}		
	}

	private void loadThreadWorker() 
	{
		m_worker = new ThreadWorker<ObjectWrapper>()
				{
						@Override
						public void runTask(ObjectWrapper item)  
						{
							while(true)
							{
								try
								{
									LiveGuiPublisher.PublishLineChartRow(
											"Test",
											"Chart",
											"LineChartTest",
											"LiveSeries", 
											(i++) + "", 
											i);
									
									//m_map.get(m_strSeriesName).set(, i++);
									//m_blnHasChanged = true;
									if(i > 1000)
									{
										break;
									}
									Thread.sleep(2000);
								}
								catch(Exception ex)
								{
									Logger.log(ex);
								}
							}
						}
				};
		m_worker.work();
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
					"LineChartTest"
				};
	}
}
