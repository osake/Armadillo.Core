package Armadillo.Core.Math;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Duration;;


public class TsRow2D extends ATsEvent
{
    public double Fx;

    /// <summary>
    ///   Used for serialization
    /// </summary>
    public TsRow2D()
    {
    }

    public TsRow2D(DateTime dblX, double dblFx)
    {
    	this(dblX.toDate(), dblFx);
    }
    
    public TsRow2D(Date dblX, double dblFx)
    {
        if (Double.isNaN(dblFx))
        {
            //Debugger.Break();
        }
        Time = dblX;
        Fx = dblFx;
    }

    /// <summary>
    ///   Sort by X values
    /// </summary>
    /// <param name = "o"></param>
    /// <returns></returns>
    public int CompareTo(TsRow2D o)
    {
    	Duration period = new Duration(new DateTime(o.Time), 
    			new DateTime(Time));
        double difference = period.getMillis();
        if (difference < 0)
        {
            return -1;
        }
        if (difference > 0)
        {
            return 1;
        }

        difference = (Fx - o.Fx);
        if (difference < 0)
        {
            return -1;
        }
        if (difference > 0)
        {
            return 1;
        }

        return 0;
    }

    public TsRow2D Clone()
    {
        return new TsRow2D(
            (Date)Time.clone(),
            Fx);
    }

    @Override
    public String toString()
    {
        return " Fx = " + Fx + ", x = " + Time;
    }

	@Override
	public DateTime getTime() {
		return new DateTime(Time);
	}

	@Override
	public void setTime(DateTime dateTime) {
		Time = dateTime.toDate();
	}
}
