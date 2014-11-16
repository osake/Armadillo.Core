package Armadillo.Analytics.Stat.Random;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Base.SearchUtilsClass;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;

public class RngWrapper implements IRng
{
    private final int m_intSeed;
    protected RngBase m_rng;

    public RngWrapper(int intSeed)
    {
        m_rng = RandomFactory.Create(intSeed);
        m_intSeed = intSeed;
    }

    public RngWrapper()
    {
    	m_intSeed = RandomFactory.getRandomSeed();
        m_rng = RandomFactory.Create();
    }
    
    /**
     *  Insert the method's description here. Creation date: (1/26/00 11:03:40 AM)
     *
     *@return     double
     *@author:    <Vadum Kutsyy, kutsyy@hotmail.com>
     */

    public Object Clone()
    {
        return new RngWrapper(m_intSeed);
    }


    public double nextDouble()
    {
    	try
    	{
    		return m_rng.nextDouble();
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return 0;
    }


    public List<Integer> GetShuffledList(
        int intSize)
    {
    	try
    	{
	        ArrayList<Integer> suffledList = new ArrayList<Integer>();
	        for (int i = 0; i < intSize; i++)
	        {
	            suffledList.add(i);
	        }
	        Shuffle(suffledList);
	        return suffledList;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return new ArrayList<Integer>();
    }

    /**
     *  Insert the method's description here. Creation date: (3/6/00 4:04:19 PM)
     *
     *@param  low   double[]
     *@param  high  double[]
     *@return       double
     */

    public double NextDouble(double[] low, double[] high)
    {
        int n = low.length;
        double[] cut = new double[n];
        cut[0] = Math.max(0, high[0] - low[0]);
        for (int i = 1; i < n; i++)
        {
            cut[i] = cut[i - 1] + Math.max(0, high[i] - low[i]);
        }
        double x = NextDouble(0, cut[n - 1]);
        if (x < cut[0])
        {
            return x + low[0];
        }
        for (int i = 0; i < n - 1; i++)
        {
            if (x < cut[i + 1])
            {
                return (x - cut[i]) + low[i + 1];
            }
        }
        return Double.NaN;
    }


    /**
     *  Insert the method's description here. Creation date: (1/26/00 11:06:43 AM)
     *
     *@param  a   double
     *@param  b   double
     *@return     double
     *@author:    <Vadum Kutsyy, kutsyy@hotmail.com>
     */

    public double NextDouble(double a, double b)
    {
        if (a > b)
        {
            double tmp = a;
            a = b;
            b = tmp;
        }
        if (a == b)
        {
            return a;
        }
        return (nextDouble())*(b - a) + a;
    }


    /**
     *  Insert the method's description here. Creation date: (1/26/00 11:08:33 AM)
     *
     *@param  n   number of variables to generate
     *@return     double[]
     *@author:    <Vadum Kutsyy, kutsyy@hotmail.com>
     */

    public double[] NextDouble(int n)
    {
        double[] x = new double[n];
        for (int i = 0; i < n; i++)
        {
            x[i] = nextDouble();
        }
        return x;
    }

    public boolean NextBln()
    {
        return NextInt(0, 1) == 1;
    }


    /**
     *  Insert the method's description here. Creation date: (1/26/00 11:09:26 AM)
     *
     *@param  n   number of variables to generate
     *@param  a   double
     *@param  b   double
     *@return     double[]
     *@author:    <Vadum Kutsyy, kutsyy@hotmail.com>
     */

    public double[] NextDouble(int n, double a, double b)
    {
        if (a > b)
        {
            double tmp = a;
            a = b;
            b = tmp;
        }
        double[] x = new double[n];
        for (int i = 0; i < n; i++)
        {
            x[i] = (nextDouble())*(b - a) + a;
        }
        return x;
    }

    public int[] NextInt(int n, int a, int b)
    {
        if (a > b)
        {
            int tmp = a;
            a = b;
            b = tmp;
        }
        int[] x = new int[n];
        for (int i = 0; i < n; i++)
        {
            x[i] = NextInt(a, b);
        }
        return x;
    }


    // _WH_, March 12,1999
    // Double BMoutput;                // constant needed by Box-Mueller algorithm
    /**
    @param hi upper limit of range
    @return a random integer in the range 1,2,... ,<STRONG>hi</STRONG>
    */

    public int NextInt(int hi)
    {
        return NextInt(1, hi); // _WH_,
        //return (int) (1+hi*raw()); // does not yield [1,hi]
    }

    /**
    @param lo lower limit of range
    @param hi upper limit of range
    @return a random integer in the range <STRONG>lo</STRONG>, <STRONG>lo</STRONG>+1, ... ,<STRONG>hi</STRONG>
    */

    public int NextInt(int lo, int hi)
    {
    	try
    	{
	        return (int) (lo + (long) ((1L + hi - lo)*nextDouble())); // _WH_, March 12,1999
	        //return (int) (lo+(hi-lo+1)*raw()); // does not yield [lo,hi]
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return 0;
    }

    // Returns an array, of length top+1, of unique pseudorandom integers between bottom and top
    // i.e. no integer is repeated and all integers between bottom and top inclusive are present
    public int[] uniqueIntegerArray(int bottom, int top)
    {
        int range = top - bottom;
        int[] array = uniqueIntegerArray(range);
        for (int i = 0; i < range + 1; i++)
        {
            array[i] += bottom;
        }
        return array;
    }


    // Returns an array, of length top+1, of unique pseudorandom integers between 0 and top
    // i.e. no integer is repeated and all integers between 0 and top inclusive are present
    public int[] uniqueIntegerArray(int top)
    {
        int numberOfIntegers = top + 1; // number of unique pseudorandom integers returned
        int[] array = new int[numberOfIntegers]; // array to contain returned unique pseudorandom integers
        //boolean allFound = false;                           // will equal true when all required integers found
        int nFound = 0; // number of required pseudorandom integers found
        boolean[] found = new boolean[numberOfIntegers]; // = true when integer corresponding to its index is found
        for (int i = 0; i < numberOfIntegers; i++)
        {
            found[i] = false;
        }

        boolean test0 = true;
        while (test0)
        {
            int ii = NextInt(top);
            if (!found[ii])
            {
                array[nFound] = ii;
                found[ii] = true;
                nFound++;
                if (nFound == numberOfIntegers)
                {
                    test0 = false;
                }
            }
        }
        return array;
    }

    /// <summary>
    /// Draws a random value from an array of cumumative probabilities.
    /// NB. Assumes that the values are valid cumumative probabilities.
    /// </summary>
    /// <param name="dblSearchSpace"></param>
    /// <returns>The drawn index.</returns>
    public int Draw(List<Double> dblSearchSpace)
    {
        double dblDraw = m_rng.nextDouble();
        return SearchUtilsClass.DoBinarySearch(dblSearchSpace, dblDraw);
    }

    public int Draw(double[] dblSearchSpace)
    {
        double dblDraw = m_rng.nextDouble();
        return Draw(
            dblSearchSpace,
            dblDraw);
    }

    public int Draw(
        double[] dblSearchSpace,
        double dblDraw)
    {
        return SearchUtilsClass.DoBinarySearch(dblSearchSpace, dblDraw);
    }

    public int Draw(
        List<Double> dblSearchSpace,
        double dblDraw)
    {
        return SearchUtilsClass.DoBinarySearch(dblSearchSpace, dblDraw);
    }

    public int NextSymbol()
    {
        return nextDouble() > 0.5 ? 1 : -1;
    }


    // swaps array elements i and j
    private <T> void Exch(List<T> a, int i, int j)
    {
        T swap = a.get(i);
        a.set(i, a.get(j));
        a.set(j, swap);
    }


    /*************************************************************************
     *  Reads in N lines, shuffles them
     *  Uses Knuth's shuffle.
     *************************************************************************/

    public <T> void Shuffle(List<T> a)
    {
    	try
    	{
	        int N = a.size();
	        for (int i = 0; i < N; i++)
	        {
	            int r = i + (int) (nextDouble()*(N - i)); // between i and N-1
	            Exch(a, i, r);
	        }
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }


    /// <summary>
    /// Shuffle list of numbers in a random order
    /// </summary>
    /// <param name="listNumbers">
    /// ArrayList of numbers
    /// </param>
    /// <returns>
    /// Shuffled list
    /// </returns>
    public <T> void ShuffleList(List<T> listNumbers)
    {
        int intNumbersCount = listNumbers.size();
        if (intNumbersCount == 1)
        {
            return;
        }
        ArrayList<T> listQ = new ArrayList<T>(intNumbersCount + 1);
        for (int i = 0; i < intNumbersCount; i++)
        {
            int rn = m_rng.NextInt(0, listNumbers.size()-1);
            T q = listNumbers.get(rn);
            listNumbers.remove(rn);
            listQ.add(q);
        }
        if (listNumbers.size() > 0)
        {
            //Debugger.Break();
            try {
				throw new HCException("Shuffle error.");
			} catch (HCException e) {
				Logger.log(e);
			}
        }
        listNumbers.addAll(listQ);
    }
}