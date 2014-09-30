package Armadillo.Analytics.Stat.Random;

import java.util.Random;

import Armadillo.Core.Logger;

public class RngBase implements IRng
{
	private Random m_random;
	
	public RngBase()
	{
		this(RandomFactory.getRandomSeed());
	}
	
	public RngBase(int intSeed)
	{
        m_random = new Random(intSeed);
	}
	
	@Override
	public double nextDouble() 
	{
		try
		{
			return m_random.nextDouble();
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
		return 0;
	}

	public int nextInt(int hi) 
	{
		try
		{
            return NextInt(1, hi); // _WH_,
			//return m_random.nextInt(hi);
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
		return 0;
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
            Logger.Log(ex);
        }
        return 0;
    }
}
