package Armadillo.Analytics.SpecialFunctions;

import Armadillo.Analytics.Base.FastMath;

public class GammaFunct extends AFunction2D
{
    //  Lanczos Gamma Function approximation - small gamma
    //private static double lgfGamma = 5.0;

    // beta array
    //
    // The parameters: m_intInitialIndex and  m_intInitialIndex2 are used for 
    // retrieving probabilities from the beta distribution array.
    //
    // Recall: The beta distribution is subdivided in three parts: a continuous part, 
    // a continuous-integer part and an integer part.
    // We used two bounds to split the array (INT_PARAMETER_UPPER_BOUND and 
    // INT_PARAMETER_INTEGER_UPPER_BOUND)
    //
    // If beta is > than continuous bound and alpha < than the continuous bound then 
    // look into the second part of the beta array.+
//    private static final int m_intInitialIndex =
//        (int) ((MathConstants.INT_PARAMETER_UPPER_BOUND)/
//               MathConstants.DBL_BETA_DELTA);

    // If alpha and beta are both > than the integer bound then we look into the third part of the array
//    private static final int m_intInitialIndex2 = m_intInitialIndex +
//                                           MathConstants.INT_PARAMETER_INTEGER_UPPER_BOUND;

    public static final String STR_PRECOMPILED_DATA_FILE_NAME = "precompiledBeta";

    // beta distribution

    //private static readonly Dictionary<double, double> m_gammaCache =
    //    new Dictionary<double, double>(
    //        Constants.INT_GAMMA_CASHED_SIZE + 100);

    //private static final Object m_lockObject = new Object();
    //private static bool m_blnCalculateRealProb;
    //private static bool m_blnLoadingData;
    //private static double[,,] m_dblBetaDistributionArray;
    //private static Object m_lockObject2 = new Object();

    public GammaFunct()
    {
        YLabel = "gamma(x)";
    }

    public double EvaluateFunction(double dblX)
    {
        return Gamma(dblX);
    }

    /**
     * Returns the gamma function <tt>gamma(x)</tt>.
     */

    public static double GammaQuick(double x)
    {
        // to do: Compare performance with gamma function
        x = LogGammaFunct.LogGamma(x);
        //if (x > Math.Log(Double.MaxValue)) return Double.MaxValue;
        return FastMath.exp(x);
    }

    public static double Gamma(double x)
    {
        double dblGammaValue = Gamma2(x);
        return Double.isInfinite(dblGammaValue) ? Double.MAX_VALUE : dblGammaValue;
    }

    private static double Gamma2(double x)
    {
        double result = 0;
        double p = 0;
        double pp = 0;
        double q = 0;
        double qq = 0;
        double z = 0;
        int i = 0;
        double sgngam = 0;

        sgngam = 1;
        q = Math.abs(x);
        if (q > 33.0)
        {
            if (x < 0.0)
            {
                p = (int) Math.floor(q);
                i = (int) Math.round(p);
                if (i%2 == 0)
                {
                    sgngam = -1;
                }
                z = q - p;
                if (z > 0.5)
                {
                    p = p + 1;
                    z = q - p;
                }
                z = q*Math.sin(Math.PI*z);
                z = Math.abs(z);
                z = Math.PI/(z*gammastirf(q));
            }
            else
            {
                z = gammastirf(x);
            }
            result = sgngam*z;
            return result;
        }
        z = 1;
        while (x >= 3)
        {
            x = x - 1;
            z = z*x;
        }
        while (x < 0)
        {
            if (x > -0.000000001)
            {
                result = z/((1 + 0.5772156649015329*x)*x);
                return result;
            }
            z = z/x;
            x = x + 1;
        }
        while (x < 2)
        {
            if (x < 0.000000001)
            {
                result = z/((1 + 0.5772156649015329*x)*x);
                return result;
            }
            z = z/x;
            x = x + 1.0;
        }
        if (x == 2)
        {
            result = z;
            return result;
        }
        x = x - 2.0;
        pp = 1.60119522476751861407E-4;
        pp = 1.19135147006586384913E-3 + x*pp;
        pp = 1.04213797561761569935E-2 + x*pp;
        pp = 4.76367800457137231464E-2 + x*pp;
        pp = 2.07448227648435975150E-1 + x*pp;
        pp = 4.94214826801497100753E-1 + x*pp;
        pp = 9.99999999999999996796E-1 + x*pp;
        qq = -2.31581873324120129819E-5;
        qq = 5.39605580493303397842E-4 + x*qq;
        qq = -4.45641913851797240494E-3 + x*qq;
        qq = 1.18139785222060435552E-2 + x*qq;
        qq = 3.58236398605498653373E-2 + x*qq;
        qq = -2.34591795718243348568E-1 + x*qq;
        qq = 7.14304917030273074085E-2 + x*qq;
        qq = 1.00000000000000000320 + x*qq;
        result = z*pp/qq;
        return result;
    }

    private static double gammastirf(double x)
    {
        double result = 0;
        double y = 0;
        double w = 0;
        double v = 0;
        double stir = 0;

        w = 1/x;
        stir = 7.87311395793093628397E-4;
        stir = -2.29549961613378126380E-4 + w*stir;
        stir = -2.68132617805781232825E-3 + w*stir;
        stir = 3.47222221605458667310E-3 + w*stir;
        stir = 8.33333333333482257126E-2 + w*stir;
        w = 1 + w*stir;
        y = FastMath.exp(x);
        if (x > 143.01608)
        {
            v = Math.pow(x, 0.5*x - 0.25);
            y = v*(v/y);
        }
        else
        {
            y = Math.pow(x, x - 0.5)/y;
        }
        result = 2.50662827463100050242*y*w;
        return result;
    }

    @Override
    public void SetFunctionLimits()
    {
        XMin = -10;
        XMax = 10;
        YMin = -10;
        YMax = 10;
    }
}