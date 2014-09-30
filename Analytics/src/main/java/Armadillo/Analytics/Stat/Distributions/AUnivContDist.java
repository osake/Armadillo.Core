package Armadillo.Analytics.Stat.Distributions;

import Armadillo.Analytics.Stat.Random.IRng;

public abstract class AUnivContDist extends AUnivDist
{
    public AUnivContDist(IRng rng)
    {
    	super(rng);
    }

    public double[] NextDoubleArr(int n)
    {
        double[] x = new double[n];
        for (int i = 0; i < n; i++)
        {
            x[i] = NextDouble();
        }
        return x;
    }


//    public Vector NextDoubleVector(int n)
//    {
//        double[] dblRandomArr = NextDoubleArr(n);
//        return new Vector(dblRandomArr);
//    }

    public double Cdf(
        double dblLowLimit,
        double dblHighLimit)
    {
        return Cdf(dblHighLimit) - Cdf(dblLowLimit);
    }

    /// <summary>
    /// Probability density function
    /// </summary>
    /// <param name="x"></param>
    /// <returns></returns>
    public abstract double Pdf(double dblX);

    /// <summary>
    /// Returns a random number from the distribution.
    /// </summary>
    /// <returns></returns>
    public abstract double NextDouble();

    /// <summary>
    /// Cumulative distribution function
    /// </summary>
    /// <param name="dblX"></param>
    /// <returns></returns>
    public abstract double Cdf(double dblX);

    /// <summary>
    /// Inverse Cdf
    /// </summary>
    /// <param name="dblProbability"></param>
    /// <returns></returns>
    public abstract double CdfInv(double dblProbability);

}