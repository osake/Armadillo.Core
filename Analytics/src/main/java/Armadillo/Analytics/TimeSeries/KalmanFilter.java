package Armadillo.Analytics.TimeSeries;

import Armadillo.Core.Logger;

public class KalmanFilter
{
    private static final int MIN_DATA_SIZE = 10;
    private static final double SMOOTH_FACTOR = 0.05;
    private Kalman1D m_kalman1D;
    private int m_intSize;

    public KalmanFilter() 
    {
    	this(SMOOTH_FACTOR);
    }

    public KalmanFilter(double dblSmothFactor)
    {
        m_kalman1D = new Kalman1D();
        m_kalman1D.Reset(
            0.1,
            0.1,
            dblSmothFactor,
            400,
            0);
    }

    public boolean IsReady()
    {
        return m_intSize > MIN_DATA_SIZE;
    }

    public double Predict()
    {
        return m_kalman1D.Predicition(1);
    }

    public boolean Filter(
        double  dblValue,
        double[] dblFilteredValue,
        double[] dblNoise)
    {
        dblFilteredValue[0] = 0;
        dblNoise[0] = 0;
        try
        {
            double dblPrediction = m_kalman1D.Update(dblValue, 1);
            dblFilteredValue[0] = dblValue;
            dblNoise[0] = 0;
            m_intSize++;

            if (m_intSize < MIN_DATA_SIZE)
            {
                return false;
            }

            if (!Double.isNaN(dblValue))
            {
                dblFilteredValue[0] = dblPrediction;
                dblNoise[0] = dblValue - dblPrediction;
            }
            return true;
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return false;
    }
}