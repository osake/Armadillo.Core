package Armadillo.Core.Math;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import Armadillo.Core.Console;
import Armadillo.Core.DateHelper;
import Armadillo.Core.Logger;

public class RollingWindowStdDev 
{
	private final Object m_lockObject = new Object();

    public double Cv()
    {
        return StdDev()/Mean();
    }

    public double Variance()
    {
        return Numerator/Denominator;
    }

    public double StdDev()
    {
        return Math.sqrt(Variance());
    }

    public double Mean()
    {
        return SumOfValues/CurrCounter;
    }

    public double LastValue;

    public List<TsRow2D> Data;

    public double SumOfValues;
    public DateTime LastUpdateTime = DateHelper.MIN_DATE_JODA;

    public double Denominator;
    public double Numerator;

    public double DefaultDenominator;
    public int WindowSize;
    public double CurrCounter;
    public double SumSqOfValues;

    public double Max()
    {
    	if(Data.size() == 0){
    		return 0;
    	}
    	
    	double dblMax = -Double.MAX_VALUE;
    	for(TsRow2D value : Data){
    		dblMax = Math.max(dblMax, value.Fx);
    	}
        return dblMax;
    }

    public double Min()
    {
    	if(Data.size() == 0){
    		return 0;
    	}
    	
    	double dblMin = -Double.MAX_VALUE;
    	for(TsRow2D value : Data){
    		dblMin = Math.min(dblMin, value.Fx);
    	}
        return dblMin;
    }

    public RollingWindowStdDev() { 
    	this(Integer.MAX_VALUE);
    }

    public RollingWindowStdDev(
        int intWindowSize)
    {
        if (intWindowSize <= 2)
        {
            try {
				throw new Exception("Invalid window size: " +
				                    intWindowSize);
			} catch (Exception e) {
				Logger.log(e);
			}
        }
        WindowSize = intWindowSize;
        Data = new ArrayList<TsRow2D>();
        DefaultDenominator = WindowSize*(WindowSize - 1);
    }

    public boolean IsReady()
    {
        return Data.size() >= WindowSize;
    }

    public void Update(
            Date dateTime,
            double dblValue)
    {
    	Update(new DateTime(dateTime),
    			dblValue);
    }
    
    public void Update(
        DateTime dateTime,
        double dblValue)
    {
        synchronized (m_lockObject)
        {
            if (LastUpdateTime.getMillis() > dateTime.getMillis() &&
                dateTime != new DateTime())
            {
                try 
                {
					throw new Exception("Invalid time");
				} 
                catch (Exception e) 
                {
					Logger.log(e);
				}
            }

            if (Double.isNaN(dblValue) ||
                Double.isInfinite(dblValue))
            {
                try 
                {
					throw new Exception("Invalid value");
				} 
                catch (Exception e) 
                {
					Logger.log(e);
				}
            }

            LastValue = dblValue;

            Data.add(
                new TsRow2D(dateTime.toDate(), dblValue));

            //
            // add to values
            //
            SumOfValues += dblValue;
            SumSqOfValues += dblValue*dblValue;
            CurrCounter = Data.size();


            if (CurrCounter > WindowSize)
            {
                //
                // remove old values
                //
                double dblOldValue = Data.get(0).Fx;
                SumOfValues -= dblOldValue;
                SumSqOfValues -= (dblOldValue*dblOldValue);
                Data.remove(0);
                CurrCounter = WindowSize;
            }


            if (CurrCounter < WindowSize)
            {
                Denominator = CurrCounter*(CurrCounter - 1);
            }
            else
            {
                Denominator = DefaultDenominator;
            }


            Numerator = (SumSqOfValues*CurrCounter -
                         Math.pow(SumOfValues, 2));
            if (Numerator < 0)
            {
                if (Math.abs(Numerator) < 1e-5)
                {
                    Numerator = 0;
                }
            }
            LastUpdateTime = dateTime;
        }
    }


    public static void DoTest()
    {
        double data[] = new double[]
                       {
                           0.3136649,
                           0.340137912,
                           0.66962385,
                           0.693365458,
                           0.747020564,
                           0.075946156,
                           0.639564259,
                           0.086856861,
                           0.522924034,
                           0.574736478,
                           0.668914313,
                           0.841301719,
                           0.431452231,
                           0.823558576,
                           0.530728641,
                           0.625912979,
                           0.617261918,
                           0.247430901,
                           0.706435352,
                           0.444944501,
                           0.073941027,
                           0.462320561,
                           0.038049327,
                           0.544860359,
                           0.001164442,
                           0.040927214,
                           0.959685337,
                           0.506014396,
                           0.854543686,
                           0.724056741,
                           0.651440546,
                           0.019357499,
                           0.835840975,
                           0.366760571,
                           0.370995047,
                           0.279957782,
                           0.257752914,
                           0.010635459,
                           0.465879657,
                           0.487012252,
                           0.283204804,
                           0.483345893,
                           0.732101943,
                           0.024790537,
                           0.795215301,
                           0.924795424,
                           0.683081081,
                           0.316911604,
                           0.1243507,
                           0.983052084,
                           0.284739058,
                           0.968872378,
                           0.434701251,
                           0.272833844,
                           0.070588556,
                           0.748747599,
                           0.914695343,
                           0.661913272,
                           0.860957181,
                           0.667487045,
                       };


        final int intWindowSize = 10;
        RollingWindowStdDev incrBasicRollingWindow =
            new RollingWindowStdDev(intWindowSize);


        for (int i = 0; i < data.length; i++)
        {
            incrBasicRollingWindow.Update(
                new DateTime(),
                data[i]);


            if (i >= intWindowSize - 1)
            {
                Console.writeLine(
                    "StDevValue = " +
                    incrBasicRollingWindow.StdDev());
            }
            else
            {
                Console.writeLine(
                    "StDevValue = " +
                    incrBasicRollingWindow.StdDev());
            }
        }
    }

    public void Update(double dblCurrFitness)
    {
        Update(new DateTime(DateHelper.MIN_DATE), dblCurrFitness);
    }

    public RollingWindowStdDev Clone()
    {
        RollingWindowStdDev clone = (RollingWindowStdDev)Clone();
        clone.Data = new ArrayList<TsRow2D>(Data);
        return clone;
    }


    public void Dispose()
    {
        if(Data != null)
        {
            Data.clear();
            Data = null;
        }
    }	
}
