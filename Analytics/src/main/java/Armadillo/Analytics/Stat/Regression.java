package Armadillo.Analytics.Stat;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Mathematics.MathHelper;
import Armadillo.Analytics.Stat.StatsHelper;
import Armadillo.Analytics.TimeSeries.IForecasterWrapper;
import Armadillo.Analytics.TimeSeries.ListHelper;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

	public class Regression implements IForecasterWrapper
	{
	    public double[] Weights;

	    private final Object m_errorLockObjects = new Object();
	    private final Object m_forecastLockObjects = new Object();
	    private List<Double> m_erorrs;
	    private List<Double> m_forecasts;
	    protected List<double[]> m_xData;
	    protected List<Double> m_yData;

	    public Regression(){} // used for serialization

	    public Regression(
	        List<double[]> xData,
	        List<Double> yData)
	    {
	        m_xData = xData;
	        m_yData = yData;
	        DoRegression();
	    }

	    public Regression(double[] data)
	    {
	        GetXyData(data);
	        DoRegression();
	    }

		private void GetXyData(double[] data)
	    {
	    	try
	    	{
		        m_xData = new ArrayList<double[]>();
		        m_yData = new ArrayList<Double>();
		        for (int i = 0; i < data.length; i++)
		        {
		            double dblVal = data[i];
		            m_yData.add(dblVal);
		            m_xData.add(new double[] {i + 1.0});
		        }
			}
			catch(Exception ex)
			{
				Logger.log(ex);
			}
	    }

	    protected void DoRegression()
	    {
	        try
	        {
	            if (m_xData.get(0).length >= m_xData.size() - 1)
	            {
	                Logger.log(new HCException("Number of variables [" + m_xData.get(0).length +
	                                           "] are too large in comparison to num of samples [" + m_xData.size() + "]"));
	                return;
	            }

				OLSMultipleLinearRegression simpleRegression = 
						new OLSMultipleLinearRegression();

				double[] yArr = ArrayUtils.toPrimitive(m_yData.toArray(new Double[0]));
				double[][] xArr = m_xData.toArray(new double[0][]);
				
				simpleRegression.newSampleData(
						yArr,
						xArr);
				
	            Weights = simpleRegression.estimateRegressionParameters();
	        }
	        catch(Exception ex)
	        {
	            Logger.log(ex);
	        }
	    }

	    
	    public static double getRSquared(double[] residuals, double[] y)
	    {
	    	try
	    	{
		    	double dblMean = StatsHelper.getMean(y);
		    	double dblRss = 0;
		    	double dblTss = 0;
		    	for (int i = 0; i < residuals.length; i++) 
		    	{
		    		dblRss += Math.pow(residuals[i], 2);
		    		dblTss += Math.pow(dblMean - y[i], 2);
				}
		    	double dblRSquared = 1.0 - (dblRss / dblTss);
		    	if(!MathHelper.isAValidNumber(dblRSquared))
		    	{
		    		throw new HCException("Invalid number");
		    	}
		    	return dblRSquared;
	    	}
	    	catch(Exception ex)
	    	{
	    		Logger.log(ex);
	    	}
	    	return 0;
	    }
	    
	    public double GetRss()
	    {
	    	try
	    	{
		    	List<Double> errors = GetErrors();
		    	double dblRss = 0;
		    	for (int i = 0; i < errors.size(); i++) 
		    	{
		    		dblRss += Math.pow(errors.get(i), 2);
				}
		        return dblRss;
			}
			catch(Exception ex)
			{
				Logger.log(ex);
			}
			return 0;
		}

	    public double Forecast(double dblX)
	    {
	        return Forecast(new double[] { dblX });
	    }

	    public int length()
	    {
	    	try
	    	{
		        if(Weights == null)
		        {
		            return 0;
		        }
		        return Weights.length;
			}
			catch(Exception ex)
			{
				Logger.log(ex);
			}
			return 0;
	    }

	    public double Forecast(double[] dblX)
	    {
	        try
	        {
	            double dblIntercept = 0;
	            for (int i = 0; i < Weights.length - 1; i++)
	            {
	                dblIntercept += Weights[i]*dblX[i];
	            }

	            double dblYPrediction = dblIntercept + Weights[Weights.length - 1];
	            return dblYPrediction;
	        }
	        catch(Exception ex)
	        {
	            Logger.log(ex);
	        }
	        return 0;
	    }

	    /// <summary>
	    /// MRSE = Mean root squared error
	    /// </summary>
	    /// <returns></returns>
	    public double GetMrse()
	    {
	        List<Double> errors = GetErrors();
	        double dblSumSq =0;
	        for (int i = 0; i < errors.size(); i++) 
	        {
	        	double n = errors.get(i);
	        	dblSumSq += n * n;
			}
	        return Math.sqrt(dblSumSq/(errors.size() - 1));
	    }

	    public List<Double> GetErrors()
	    {
	    	try
	    	{
		        if (m_erorrs == null)
		        {
		            synchronized (m_errorLockObjects)
		            {
		                if (m_erorrs == null)
		                {
		                    GetForecasts();
		                    m_erorrs = new ArrayList<Double>();
		                    for (int i = 0; i < m_xData.size(); i++)
		                    {
		                        double dblYForecast = m_forecasts.get(i);
		                        double dblY = m_yData.get(i);
		                        double dblError = dblYForecast - dblY;
		                        m_erorrs.add(dblError);
		                    }
		                }
		            }
		        }
		        return m_erorrs;
			}
			catch(Exception ex)
			{
				Logger.log(ex);
			}
			return new ArrayList<Double>();
	    }

	    public List<Double> GetForecasts()
	    {
	    	try
	    	{
		        if (m_forecasts == null)
		        {
		            synchronized (m_forecastLockObjects)
		            {
		                if (m_forecasts == null)
		                {
		                	ArrayList<Double> forecasts = new ArrayList<Double>();
		                    for (int i = 0; i < m_xData.size(); i++)
		                    {
		                        double[] xRow = m_xData.get(i);
		                        double dblYForecast = Forecast(xRow);
		                        forecasts.add(dblYForecast);
		                    }
		                    m_forecasts = forecasts;
		                }
		            }
		        }
		        return m_forecasts;
			}
			catch(Exception ex)
			{
				Logger.log(ex);
			}
			return new ArrayList<Double>();
	    }

	    public double GetAdjustedCoeffDeterm()
	    {
	    	try
	    	{
		        double dblCoeffDeterm = GetCoeffDeterm();
		        int intVars = Weights.length - 1;
		        return dblCoeffDeterm - ((1.0 - dblCoeffDeterm)*(intVars/(m_yData.size() - intVars - 1.0)));
			}
			catch(Exception ex)
			{
				Logger.log(ex);
			}
			return 0;
	    }

	    public double GetCoeffDeterm()
	    {
	    	try
	    	{
		        double dblYAvg = ListHelper.average(m_yData);
		        double dblSsTot = 0;
		        for (int i = 0; i < m_yData.size(); i++) 
		        {
					dblSsTot += Math.pow(m_yData.get(i) - dblYAvg, 2);
				}
		        List<Double> errors = GetErrors();
		        double dblSsErr = 0;
		        for (int i = 0; i < errors.size(); i++)
		        {
		        	double dblCurrErr = errors.get(i);
		        	dblSsErr += Math.pow(dblCurrErr, 2);
				}
		        return 1.0 - (dblSsErr/dblSsTot);
	    	}
	    	catch(Exception ex)
	    	{
	    		Logger.log(ex);
	    	}
	    	return 0;
	    }
	}
