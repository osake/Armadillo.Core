package Armadillo.Analytics.TimeSeries;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Core.HCException;

public class RollingWindowVolatility 
{
    public double Volatility()
    {
            double dblVolatility = Math.sqrt(m_dblSumSqReturns/m_returns.size());
            if (Double.isNaN(dblVolatility))
            {
                throw new HCException("Invalid volatility value");
            }
            return dblVolatility;
    }

    public int WindowSize;

    private List<Double> m_returns;
    private double m_dblPrevValue;
    private double m_dblSumSqReturns;

    public RollingWindowVolatility(int intWindowSize)
    {
        m_returns = new ArrayList<Double>();
        WindowSize = intWindowSize;
        m_dblPrevValue = Double.NaN;
    }

    public void Update(double dblValue)
    {
        if (Double.isNaN(dblValue))
        {
            throw new HCException("Invalid volatility value");
        }

        if (Double.isNaN(m_dblPrevValue))
        {
            m_dblPrevValue = dblValue;
            m_dblPrevValue = dblValue;
            return;
        }
        //
        // compute return
        //
        double dblReturn = Math.log(dblValue) - Math.log(m_dblPrevValue);
        
        if(Double.isNaN(dblReturn))
        {
            throw new HCException("Invalid return");
        }
        m_returns.add(dblReturn);
        m_dblSumSqReturns += Math.pow(dblReturn, 2);

        //
        // remove window values
        //
        if (m_returns.size() > WindowSize)
        {
            double dblOldReturn = m_returns.get(0);
            m_returns.remove(0);
            double dblOldSumSqReturn = Math.pow(dblOldReturn, 2);
            m_dblSumSqReturns -= dblOldSumSqReturn;

            if (m_dblSumSqReturns < 0)
            {
                if (Math.abs(m_dblSumSqReturns) > 1e-10)
                {
                    throw new HCException("Invalid sumSq return");
                }
                m_dblSumSqReturns = 0;
            }
        }

        m_dblPrevValue = dblValue;
    }
}
