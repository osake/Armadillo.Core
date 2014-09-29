package Armadillo.Core;

public class DoubleHelper 
{
	public static int compare(double p1, double p2) 
	{
		if (p1 < p2)
			return -1;
		if (p1 > p2)
			return 1;
		return 0;
	}

	public static boolean isAValidNumber(double dbl) 
	{
		if (Double.isNaN(dbl) || Double.isInfinite(dbl)) 
		{
			return false;
		}
		return true;
	}
}
