package Utils.Gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Shape;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JPanel;

import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.util.ShapeUtilities;

import Armadillo.Core.Logger;

public class Scatter2DPlot extends ApplicationFrame 
{
	private static final long serialVersionUID = 1L;
	private XYSeriesCollection m_seriesCollection;
	private String m_strTitle;
	private String m_strXLabel;
	private String m_strYLabel;
	private Map<String, XYSeries> m_mapNameToSeries = new ConcurrentHashMap<String, XYSeries>();
	private double[] m_x;
	private double[] m_y;
	private String m_strSeriesName;
	private Object m_seriesLock = new Object();
	private JPanel m_jpanel;

	public Scatter2DPlot(
			String strTitle,
			String strXLabel,
			String strYLabel) 
	{
        super(strTitle);
		try
		{
	        m_strTitle = strTitle;
	        m_strXLabel = strXLabel;
	        m_strYLabel = strYLabel;
	        m_jpanel = createDemoPanel();
	        m_jpanel.setPreferredSize(new Dimension(640, 480));
	        add(m_jpanel);
	        
	        pack();
	        RefineryUtilities.centerFrameOnScreen(this);
	        setVisible(true);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
    }

    public JPanel createDemoPanel() 
    {
    	try
    	{
	        m_seriesCollection = new XYSeriesCollection();
	        JFreeChart jfreechart = ChartFactory.createScatterPlot(
	        		m_strTitle, m_strXLabel, m_strYLabel, m_seriesCollection,
	            PlotOrientation.VERTICAL, true, true, false);
	        Shape cross = ShapeUtilities.createDiagonalCross(3, 1);
	        XYPlot xyPlot = (XYPlot) jfreechart.getPlot();
	        xyPlot.setDomainCrosshairVisible(true);
	        xyPlot.setRangeCrosshairVisible(true);
	        XYItemRenderer renderer = xyPlot.getRenderer();
	        
	        renderer.setSeriesShape(0, cross);
	        renderer.setSeriesPaint(0, Color.red);
	        return new ChartPanel(jfreechart);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
    	return null;
    }

    private void checkSeries(String strSeriesName)
    {
    	try
    	{
	    	if(!m_mapNameToSeries.containsKey(strSeriesName))
	    	{
	            XYSeries series = new XYSeries(strSeriesName);
	            m_seriesCollection.addSeries(series);
	            m_mapNameToSeries.put(strSeriesName, series);
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
			int intSamples = 20;
	        double[][] values = new double[2][intSamples];
	        Random rand = new Random();
	        for (int i = 0; i < intSamples; i++) 
	        {
	                double x = rand.nextGaussian();
	                double y = rand.nextGaussian();
	                values[0][i] = x; 
	                values[1][i] = y; 
	        }
			return values;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}

    public static void main(String args[]) 
    {
    	try
    	{
	        Scatter2DPlot scatterplotdemo4 = new Scatter2DPlot("Scatter Plot Demo", "x", "yy");
	        
	        while(true)
	        {
	        	double[][] randomData = generateRandomData();
	        	scatterplotdemo4.addValuesToSeries(randomData[0], randomData[1], "TestSeries");
	        	Thread.sleep(1000);
	        }
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

	public void addValuesToSeries(
			double[] x, 
			double[] y,
	    	String strSeriesName) 
	{
		try
		{
			synchronized(m_seriesLock)
			{
				m_x = x;
				m_y = y;
				m_strSeriesName = strSeriesName;
				
				//SwingUtilities.invokeLater(new Runnable() // todo, make this work properly
				{
				    //public void run() 
				    {
				    	checkSeries(m_strSeriesName);
						XYSeries series = m_mapNameToSeries.get(m_strSeriesName);
						series.clear();
				        for (int i = 0; i < m_x.length; i++) 
				        {
			                double x_ = m_x[i];
			                double y_ = m_y[i];
			                series.add(x_, y_);
				        }
				   }			
				}
				//);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
}