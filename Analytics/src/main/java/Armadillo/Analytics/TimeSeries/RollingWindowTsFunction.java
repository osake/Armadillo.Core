package Armadillo.Analytics.TimeSeries;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import Armadillo.Core.DateHelper;
import Armadillo.Core.HCException;
import Armadillo.Core.Math.TsRow2D;

public class RollingWindowTsFunction 
{
    public double LastValue;
    public DateTime LastUpdateTime;
    public List<TsRow2D> Data;

    private double m_dblCurrCounter;
    private final int m_intWindowSize;

    public RollingWindowTsFunction(
        int intWindowSize)
    {
    	LastUpdateTime = DateHelper.MIN_DATE_JODA;
        if (intWindowSize == 0)
        {
            throw new HCException("Invalid window size: " +
                                intWindowSize);
        }
        m_intWindowSize = intWindowSize;
        Data = new ArrayList<TsRow2D>();
    }

    public boolean IsReady()
    {
        return Data.size() >= m_intWindowSize;
    }

    public void Update(
            Date dateTime,
            double dblVal)
    {
    	Update(new DateTime(dateTime), dblVal);
    }
    
    public void Update(
        DateTime dateTime,
        double dblVal)
    {
        if (LastUpdateTime.getMillis() >= dateTime.getMillis() &&
            dateTime != DateHelper.MIN_DATE_JODA)
        {
            throw new HCException("Invalid time");
        }

        LastValue = dblVal;
        LastUpdateTime = dateTime;
        Data.add(new TsRow2D(dateTime,dblVal));
        m_dblCurrCounter = Data.size();


        if (m_dblCurrCounter > m_intWindowSize)
        {
            //
            // remove old values
            //
            Data.remove(0);
            m_dblCurrCounter = m_intWindowSize;
        }
    }

//    public RollingWindowTsFunction Clone()
//    {
//    	RollingWindowTsFunction clone = (RollingWindowTsFunction)clone();
//        clone.Data = new ArrayList<TsRow2D>(Data);
//        return clone;
//    }

}
