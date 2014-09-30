package Armadillo.Analytics.SpecialFunctions;

import Armadillo.Analytics.Base.FastMath;
import Armadillo.Analytics.Base.MathConstants;

public class IncompleteGamaFunct {
	
    public static double IncompleteGamma(double a, double x)
    {
        double ans, ax, c, r;

        if (x <= 0 || a <= 0)
        {
            return 0.0;
        }

        if (x > 1.0 && x > a)
        {
            return 1.0 - IncompleteGammaComplement(a, x);
        }

        /* Compute  x**a * Exp(-x) / gamma(a)  */
        ax = a*Math.log(x) - x - LogGammaFunct.LogGamma(a);
        if (ax < -MathConstants.MAXLOG)
        {
            return (0.0);
        }

        ax = FastMath.exp(ax);

        /* power series */
        r = a;
        c = 1.0;
        ans = 1.0;

        do
        {
            r += 1.0;
            c *= x/r;
            ans += c;
        }
        while (c/ans > MathConstants.MACHEP);

        return (ans*ax/a);
    }

    /**
     * Returns the Complemented Incomplete Gamma function; formerly named <tt>igamc</tt>.
     * @param a the parameter of the gamma distribution.
     * @param x the integration start point.
     */

    public static double IncompleteGammaComplement(double a, double x)
    {
        double ans, ax, c, yc, r, t, y, z;
        double pk, pkm1, pkm2, qk, qkm1, qkm2;

        if (x <= 0 || a <= 0)
        {
            return 1.0;
        }

        if (x < 1.0 || x < a)
        {
            return 1.0 - IncompleteGamma(a, x);
        }

        ax = a*Math.log(x) - x - LogGammaFunct.LogGamma(a);
        if (ax < -MathConstants.MAXLOG)
        {
            return 0.0;
        }

        ax = FastMath.exp(ax);

        /* continued fraction */
        y = 1.0 - a;
        z = x + y + 1.0;
        c = 0.0;
        pkm2 = 1.0;
        qkm2 = x;
        pkm1 = x + 1.0;
        qkm1 = z*x;
        ans = pkm1/qkm1;

        do
        {
            c += 1.0;
            y += 1.0;
            z += 2.0;
            yc = y*c;
            pk = pkm1*z - pkm2*yc;
            qk = qkm1*z - qkm2*yc;
            if (qk != 0)
            {
                r = pk/qk;
                t = Math.abs((ans - r)/r);
                ans = r;
            }
            else
            {
                t = 1.0;
            }

            pkm2 = pkm1;
            pkm1 = pk;
            qkm2 = qkm1;
            qkm1 = qk;
            if (Math.abs(pk) > MathConstants.BIG)
            {
                pkm2 *= MathConstants.BIG_INV;
                pkm1 *= MathConstants.BIG_INV;
                qkm2 *= MathConstants.BIG_INV;
                qkm1 *= MathConstants.BIG_INV;
            }
        }
        while (t > MathConstants.MACHEP);

        return ans*ax;
    }
}

