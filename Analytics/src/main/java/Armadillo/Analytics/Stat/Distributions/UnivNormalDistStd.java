package Armadillo.Analytics.Stat.Distributions;

import Armadillo.Analytics.Base.FastMath;
import Armadillo.Analytics.SpecialFunctions.ErrorFunct;
import Armadillo.Analytics.SpecialFunctions.InvNormalFunct;
import Armadillo.Analytics.Stat.Random.IRng;
import Armadillo.Analytics.Stat.Random.RandomFactory;

public class UnivNormalDistStd {
    //private static final int INT_RND_SEED = 26;

    public UnivNormalDistStd(IRng rng)
    {
    }

    private static final double SQRT2 = FastMath.sqrt(2.0);
    private static double m_blnMoutput; // constant needed by Box-Mueller algorithm
    private static boolean m_blnMoutputAvailable; // constant needed by Box-Mueller algorithm

    //private static final double m_dblSqrth = 7.07106781186547524401E-1;
    /*************************************************
     *    COEFFICIENTS FOR METHOD  normalInverse()   *
     *************************************************/


    /**
         * Pdf of standard normal
         * @return pdf
         * @param x  x
         */
    private static final double m_dblDenomilator = Math.sqrt(2.0*Math.PI);
    
    public static double Pdf(double dblX)
    {
//        if (Double.isInfinite(dblX))
//        {
//            return 0;
//        }
        return FastMath.exp(-dblX*dblX/2.0)/m_dblDenomilator;
    }

    public  double Cdf(
        double dblX)
    {
        return CdfStatic(dblX);
    }

    private static final double SQRT_OF_TWO = Math.sqrt(2.0);
    
    public static double CdfStatic(
        double dblX)
    {
        if (dblX > 0)
        {
            return 0.5 * (1 + ErrorFunct.ErrorFunction(dblX/
            		SQRT_OF_TWO));
        }
        else
        {
            return 0.5 * (1 - ErrorFunct.ErrorFunction(-dblX/
            		SQRT_OF_TWO));
        }
    }

    /**
       *  Compute Cdf of standard normal
       *  
       *  Deprecated: Slow method, use CdfStatic
       *
       *@param  x  upper limit
       *@return    probability
       */
    @Deprecated
    public static double CdfStatic2(
        double x)
    {
        //return VisualNumerics.math.Statistics.normalCdf(x);
        if (Double.isNaN(x))
        {
            return 0;
        }
        if (Double.isInfinite(x))
        {
            return x > 0 ? 1 : 0;
        }

        double zabs = Math.abs(x);
        if (zabs > 37)
        {
            return x > 0 ? 1 : 0;
        }
        double expntl = FastMath.exp(-(zabs*zabs)/2);
        double p = 0;
        if (zabs < 7.071067811865475)
        {
            p = expntl*
                ((((((zabs*.03526249659989109 + .7003830644436881)*zabs
                     + 6.37396220353165)*zabs + 33.912866078383)*zabs +
                   112.0792914978709)*zabs
                  + 221.2135961699311)*zabs + 220.2068679123761)/
                (((((((zabs*.08838834764831844
                       + 1.755667163182642)*zabs + 16.06417757920695)*zabs +
                     86.78073220294608)*zabs
                    + 296.5642487796737)*zabs + 637.3336333788311)*zabs +
                  793.8265125199484)*zabs
                 + 440.4137358247522);
        }
        else
        {
            p = expntl/(zabs + 1/(zabs + 2/(zabs + 3/(zabs + 4/(zabs + .65)))))/
                2.506628274631001;
        }
        return x > 0 ? 1 - p : p;
    }

    /**
     * {@inheritDoc}
     *
     * If {@code x} is more than 40 standard deviations from the mean, 0 or 1
     * is returned, as in these cases the actual value is within
     * {@code Double.MIN_VALUE} of 0 or 1.
       *  Deprecated: Slow method, use CdfStatic
     */
    @Deprecated
    public static double CdfStatic3(double x)  {
        final double dev = x;
        if (FastMath.abs(dev) > 40 * 1) {
            return dev < 0 ? 0.0d : 1.0d;
        }
        return 0.5 * (1 + ErrorFunct.ErrorFunction(dev / (1 * SQRT2)));
    }
    
    public  double CdfInv(double dblP)
    {
        return CdfInvStatic(dblP);
    }

    /**
     * Returns the value, <tt>x</tt>, for which the area under the
     * Normal (Gaussian) probability density function (integrated from
     * minus infinity to <tt>x</tt>) is equal to the argument <tt>y</tt> (assumes mean is zero, variance is one); formerly named <tt>ndtri</tt>.
     * <p>
     * For small arguments <tt>0 < y < Exp(-2)</tt>, the program computes
     * <tt>z = sqrt( -2.0 * Log(y) )</tt>;  then the approximation is
     * <tt>x = z - Log(z)/z  - (1/z) P(1/z) / Q(1/z)</tt>.
     * There are two rational functions P/Q, one for <tt>0 < y < Exp(-32)</tt>
     * and the other for <tt>y</tt> up to <tt>Exp(-2)</tt>.
     * For larger arguments,
     * <tt>w = y - 0.5</tt>, and  <tt>x/sqrt(2pi) = w + w**3 R(w**2)/S(w**2))</tt>.
     *
     */
    public static double CdfInvStatic(double dblP)
    {
    	return InvNormalFunct.InvNormal(dblP);
    }


    public  double NextDouble()
    {
        return NextDouble_static();
    }

    /**
    gaussian() uses the Box-Muller algorithm to transform raw()'s into
    gaussian deviates.

    @return a random real with a gaussian distribution,  standard deviation

    */
    public static double NextDouble_static()
    {
        IRng rng = RandomFactory.Create();
        return NextDouble_static(rng);
    }
    
    public static double NextDouble_static(IRng rng)
    {
        if (m_blnMoutputAvailable)
        {
            m_blnMoutputAvailable = false; // _WH_, March 12,1999
            return (m_blnMoutput);
        }

        double x, y, r, z;
        do
        {
            x = 2*rng.nextDouble() - 1; //x=uniform(-1,1); // _WH_, March 12,1999
            y = 2*rng.nextDouble() - 1; //y=uniform(-1,1);
            r = x*x + y*y;
        }
        while (r >= 1);

        z = Math.sqrt(-2*Math.log(r)/r);
        m_blnMoutput = x*z;
        m_blnMoutputAvailable = true;
        return y*z;
    }

    /**
     *  Insert the method's description here. Creation date: (3/6/00 11:42:39 AM)
     *
     *@param  n  number of variables to generate
     *@return    double[]
     */

    /**
     * Returns a String representation of the receiver.
     */

    public  String ToString()
    {
        return "NormalStd";
    }

}
