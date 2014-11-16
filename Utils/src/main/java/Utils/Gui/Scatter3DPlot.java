package Utils.Gui;

import java.awt.Dimension;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.math.plot.Plot3DPanel;
import org.math.plot.plots.Plot;

import Armadillo.Core.Logger;

public class Scatter3DPlot 
{
	private Plot3DPanel m_plot;
	private Map<String, Plot> m_mapNameToSeries = new ConcurrentHashMap<String, Plot>(); 
	private double[] m_x; 
	private double[] m_y;
	private double[] m_z;
	private String m_strSeriesName;
	private Object m_seriesLock = new Object(); 
	
	public Scatter3DPlot(
			String strTitle,
			String strXLabel,
			String strYLabel,
			String strZLabel)
	{
		try
		{
			// create your PlotPanel (you can use it as a JPanel)
			m_plot = new Plot3DPanel();
			
			m_plot.setAxeLabel(1, strXLabel);
			m_plot.setAxeLabel(2, strYLabel);
			m_plot.setAxeLabel(3, strZLabel);
		    // put the PlotPanel in a JFrame, as a JPanel
		    JFrame frame = new JFrame(strTitle);
		    frame.setContentPane(m_plot);
		    frame.setSize(new Dimension(640, 480));
		    frame.setVisible(true);		
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public void addValuesToSeries(
			double[] x, 
			double[] y,
			double[] z,
	    	String strSeriesName) 
	{
		synchronized(m_seriesLock)
		{
			try
			{
				m_x = x;
				m_y = y;
				m_z = z;
				m_strSeriesName = strSeriesName;
				
				SwingUtilities.invokeLater(new Runnable() 
				{
				    public void run() 
				    {
				      // Here, we can safely update the GUI
				      // because we'll be called from the
				      // event dispatch thread
						if(!m_mapNameToSeries.containsKey(m_strSeriesName))
						{
							addPlot(m_x, m_y, m_z, m_strSeriesName);
						}
						else
						{
							m_plot.removePlot(
									m_mapNameToSeries.get(m_strSeriesName));
							addPlot(m_x, m_y, m_z, m_strSeriesName);
						}
				    }
				  });			
			}
			catch(Exception ex)
			{
				Logger.log(ex);
			}
		}
	}
	
	
	private void addPlot(double[] x, double[] y, double[] z,
			String strSeriesName) 
	{
		try
		{
			m_plot.addScatterPlot(strSeriesName, x, y, z);
			Plot plot = m_plot.getPlot(m_plot.getPlots().length - 1);
			m_mapNameToSeries.put(strSeriesName, plot);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public static void main(String[] arg)
	{
		try
		{
			  Scatter3DPlot scatter3DPlot = new Scatter3DPlot("test", "xx", "yy", "zz");
			  while(true)
			  {
				  //m_plot.removePlot(0);
				  double[][] rngData = generateRandomData();
				  scatter3DPlot.addValuesToSeries(
						  rngData[0], 
						  rngData[1], 
						  rngData[2], 
						  "test_series");
				  // add a line plot to the PlotPanel
				  //m_plot.addScatterPlot("my plot", rngData[0], rngData[1], rngData[2]);
				  Thread.sleep(1000);
			  }
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public static double[][] generateRandomData() 
	{
		try
		{
			int intSamples = 200;
	        double[][] values = new double[3][intSamples];
	        Random rand = new Random();
	        for (int i = 0; i < intSamples; i++) 
	        {
	                double x = rand.nextGaussian();
	                double y = rand.nextGaussian();
	                double z = rand.nextGaussian();
	                values[0][i] = x; 
	                values[1][i] = y; 
	                values[2][i] = z; 
	        }
			return values;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
	
}
