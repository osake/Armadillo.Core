package Armadillo.Analytics.SpecialFunctions;

import Armadillo.Analytics.Base.Arithmetic;
import Armadillo.Core.Logger;

public class LogGammaFunct
{
    //  Lanczos Gamma Function approximation - small gamma
    //  Lanczos Gamma Function approximation - Coefficients
    private static final double[] lgfCoeff = {
                                                    1.000000000190015, 76.18009172947146, -86.50532032941677,
                                                    24.01409824083091, -1.231739572450155, 0.1208650973866179E-2,
                                                    -0.5395239384953E-5
                                                };

    private static double lgfGamma = 5.0;

    // GAMMA FUNCTIONS
    //  Lanczos Gamma Function approximation - N (number of coefficients -1)
    private static int lgfN = 6;

    public static double LnB(
        double d,
        double d1)
    {
        return (LogGamma(d) + LogGamma(d1)) - LogGamma(d + d1);
    }


    // this method is from Flanagan's
    // to do: Compare to actual approximation method
    // log to base e of the Gamma function
    // Lanczos approximation (6 terms)
    // Retained for backward compatibility
    public static double logGamma2(double x)
    {
    	try{
        double xcopy = x;
        double fg = 0.0D;
        double first = x + lgfGamma + 0.5;
        double second = lgfCoeff[0];

        if (x >= 0.0)
        {
            if (x >= 1.0 && x - (int) x == 0.0)
            {
                fg = Arithmetic.LogFactorial((int) x) - Math.log(x);
            }
            else
            {
                first -= (x + 0.5)*Math.log(first);
                for (int i = 1; i <= lgfN; i++)
                {
                    second += lgfCoeff[i]/++xcopy;
                }
                fg = Math.log(Math.sqrt(2.0*Math.PI)*second/x) - first;
            }
        }
        else
        {
            fg = Math.PI/(GammaFunct.Gamma(1.0D - x)*Math.sin(Math.PI*x));

            if (fg != 1.0/0.0 && fg != -1.0/0.0)
            {
                if (fg < 0)
                {
                    throw new Exception("\nThe gamma function is negative");
                }
                else
                {
                    fg = Math.log(fg);
                }
            }
        }
        return fg;
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return Double.NaN;
    }


    /**
     * Returns a quick approximation of <tt>Log(gamma(x))</tt>.
     */

    public static double LogGamma(double x)
    {
        double c0 = 9.1893853320467274e-01,
               c1 = 8.3333333333333333e-02,
               c2 = -2.7777777777777777e-03,
               c3 = 7.9365079365079365e-04,
               c4 = -5.9523809523809524e-04,
               c5 = 8.4175084175084175e-04,
               c6 = -1.9175269175269175e-03;
        double g, r, z;

        if (x <= 0.0 /* || x > 1.3e19 */)
        {
            return -999;
        }

        for (z = 1.0; x < 11.0; x++)
        {
            z *= x;
        }

        r = 1.0/(x*x);
        g = c1 + r*(c2 + r*(c3 + r*(c4 + r*(c5 + r + c6))));
        g = (x - 0.5)*Math.log(x) - x + c0 + g/x;
        if (z == 1.0)
        {
            return g;
        }
        return g - Math.log(z);
    }
}
