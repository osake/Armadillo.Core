package Armadillo.Analytics.TimeSeries;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import Armadillo.Core.DateHelper;
import Armadillo.Core.HCException;
import Armadillo.Core.Math.TsRow2D;

public class RollingWindowRegression 
{
    public double LastX;
    public double LastY;
    public double Slope;
    public double Intercept;
    public List<Double> XList;
    public List<Double> YList;
    public List<TsRow2D> Predictions;
    public int WindowSize;
    public DateTime LastUpdateTime;

    private double m_dblCurrCounter;
    private double m_dblSumOfXValues;
    private double m_dblSumOfXxValues;
    private double m_dblSumOfXyValues;
    private double m_dblSumOfYValues;
    private List<Double> m_xxList;
    private List<Double> m_xyList;
    private int m_intCounter;

    public RollingWindowRegression()
    {
    }
    
    public RollingWindowRegression(int intWindowSize)
    {
        Reset();
        WindowSize = intWindowSize;
    }

    public void Update(double dblX, double dblY)
    {
        Update(dblX, dblY, DateHelper.MIN_DATE_JODA);
    }

    public void Update(Date dateTime, double dblY)
    {
    	Update(new DateTime(dateTime), dblY);
    }
    
    public void Update(DateTime dateTime, double dblY)
    {
        m_intCounter++;
        Update(m_intCounter, dblY, dateTime);
    }

    public void Update(double dblX, double dblY, DateTime dateTime)
    {
        IsValid(dblX);
        IsValid(dblY);

        AddValues(dblX, dblY);
        m_dblCurrCounter = m_xyList.size();
        RemoveValues();
        GetRegressionWeights();
        UpdatePredictions(dateTime, dblX);
    }

    private void IsValid(double dblX)
    {
        if (Double.isNaN(dblX) ||
            Double.isInfinite(dblX))
        {
            throw new HCException("Invalid value");
        }
    }

    public double Predict(double dblX)
    {
        double dblPrediction = Slope * dblX + Intercept;
        return dblPrediction;
    }

    public boolean IsReady()
    {
        return m_xyList.size() >= WindowSize;
    }

    public double PredictLastValue()
    {
        return Predict(XList.get(XList.size() - 1));
    }

    private void Reset()
    {
        m_xxList = new ArrayList<Double>();
        m_xyList = new ArrayList<Double>();
        XList = new ArrayList<Double>();
        YList = new ArrayList<Double>();
        Predictions = new ArrayList<TsRow2D>();
        m_dblCurrCounter = 0;
        m_dblSumOfXyValues = 0;
        m_dblSumOfXxValues = 0;
        m_dblSumOfXValues = 0;
        m_dblSumOfYValues = 0;
    }

    private void GetRegressionWeights()
    {
        if (m_dblCurrCounter > 2)
        {
            double dblNumerator =
                (m_dblCurrCounter * m_dblSumOfXyValues) -
                (m_dblSumOfXValues * m_dblSumOfYValues);
            double dblDenominator =
                (m_dblCurrCounter * m_dblSumOfXxValues) -
                Math.pow(m_dblSumOfXValues, 2);
            Slope =
                (Math.abs(dblNumerator) < 1e-6 || Math.abs(dblDenominator) < 1e-6) ? 0
                : (dblNumerator / dblDenominator);
            Intercept = (m_dblSumOfYValues - (Slope * m_dblSumOfXValues)) / m_dblCurrCounter;

            //if ((Math.Abs(dblNumerator) < 1e-6 || Math.Abs(dblDenominator) < 1e-6))
            //{
            //    for (int i = 0; i < XList.size(); i++)
            //    {
            //        Console.WriteLine(XList[i] + "," + YList[i]);
            //    }
            //}

            if (Double.isNaN(Intercept) ||
            		Double.isInfinite(Intercept))
            {
                throw new HCException("Invalid intercept");
            }
        }
        else
        {
            Slope = Double.NaN;
            Intercept = Double.NaN;
        }
    }

    private void UpdatePredictions(DateTime dateTime, double dblX)
    {
        LastUpdateTime = dateTime;
        double dblPrediciton = Predict(dblX);
        Predictions.add(
            new TsRow2D(dateTime, dblPrediciton));
        if (Predictions.size() > WindowSize)
        {
            Predictions.remove(0);
        }
    }

    private void RemoveValues()
    {
        if (m_dblCurrCounter > WindowSize)
        {
            //
            // remove old values
            //
            RemoveXy();
            RemoveXx();
            RemoveX();
            RemoveY();
            m_dblCurrCounter = WindowSize;
        }
    }

    private void RemoveXy()
    {
        double dblOldValue = m_xyList.get(0);
        m_dblSumOfXyValues -= dblOldValue;
        m_xyList.remove(0);
    }

    private void RemoveXx()
    {
        double dblOldValue = m_xxList.get(0);
        m_dblSumOfXxValues -= dblOldValue;
        m_xxList.remove(0);
    }

    private void RemoveX()
    {
        double dblOldValue = XList.get(0);
        m_dblSumOfXValues -= dblOldValue;
        XList.remove(0);
    }

    private void RemoveY()
    {
        double dblOldValue = YList.get(0);
        m_dblSumOfYValues -= dblOldValue;
        YList.remove(0);
    }

    private void AddValues(double dblX, double dblY)
    {
        LastX = dblX;
        LastY = dblY;
        AddXy(dblX, dblY);
        AddXx(dblX);
        AddX(dblX);
        AddY(dblY);
    }

    private void AddXy(double dblX, double dblY)
    {
        double dblXy = dblX * dblY;
        m_dblSumOfXyValues += dblXy;
        m_xyList.add(dblXy);
    }

    private void AddXx(double dblX)
    {
        double dblXx = dblX * dblX;
        m_dblSumOfXxValues += dblXx;
        m_xxList.add(dblXx);
    }

    private void AddX(double dblX)
    {
        m_dblSumOfXValues += dblX;
        XList.add(dblX);
    }

    private void AddY(double dblY)
    {
        m_dblSumOfYValues += dblY;
        YList.add(dblY);
    }

//    ~RollingWindowRegression()
//    {
//        Dispose();
//    }

    public void Dispose()
    {
        if (m_xxList != null)
        {
            m_xxList.clear();
        }
        if (m_xyList != null)
        {
            m_xyList.clear();
        }
        if (XList != null)
        {
            XList.clear();
        }
        if (YList != null)
        {
            YList.clear();
        }
        if (Predictions != null)
        {
            Predictions.clear();
        }
    }

}
