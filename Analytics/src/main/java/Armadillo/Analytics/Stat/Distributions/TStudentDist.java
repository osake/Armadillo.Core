package Armadillo.Analytics.Stat.Distributions;

import Armadillo.Analytics.SpecialFunctions.IncompleteBetaFunct;

public class TStudentDist 
{
	public static double CdfInvStatic(
            double dblV,
            double dblCumProb)
	{	
            double f1, f2, f3;
            double x1, x2, x3;
            double g, s12;

            x1 = UnivNormalDistStd.CdfInvStatic(dblCumProb);

            // Return inverse of normal for large size
            if (dblV > 200)
            {
                return x1;
            }

            // Find a pair of x1,x2 that braket zero
            f1 = CdfStatic(
                     x1,
                     dblV) - dblCumProb;
            x2 = x1;
            f2 = f1;
            do
            {
                if (f2 > 0)
                {
                    x2 = x2/2;
                }
                else
                {
                    x2 = x2 + x1;
                }
                f2 = CdfStatic(x2,
                               dblV) - dblCumProb;
            }
            while (f1*f2 > 0);

            // Find better approximation
            // Pegasus-method
            do
            {
                // Calculate slope of secant and t value for which it is 0.
                s12 = (f2 - f1)/(x2 - x1);
                x3 = x2 - f2/s12;

                // Calculate function value at x3
                f3 = CdfStatic(x3,
                               dblV) - dblCumProb;
                if (Math.abs(f3) < 1e-8)
                {
                    // This criteria needs to be very tight!
                    // We found a perfect value -> return
                    return x3;
                }

                if (f3*f2 < 0)
                {
                    x1 = x2;
                    f1 = f2;
                    x2 = x3;
                    f2 = f3;
                }
                else
                {
                    g = f2/(f2 + f3);
                    f1 = g*f1;
                    x2 = x3;
                    f2 = f3;
                }
            }
            while (Math.abs(x2 - x1) > 0.001);

            if (Math.abs(f2) <= Math.abs(f1))
            {
                return x2;
            }
            else
            {
                return x1;
            }
        }

public static double CdfStatic(
        double t,
        double dblV)
    {
        if (dblV <= 0)
        {
            try {
				throw new Exception();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
        if (t == 0)
        {
            return (0.5);
        }

        double cdf = 0.5*IncompleteBetaFunct.IncompleteBeta(
                             0.5*dblV, 0.5, dblV/(dblV + t*t));

        if (t >= 0)
        {
            cdf = 1.0 - cdf; // fixes bug reported by stefan.bentink@molgen.mpg.de
        }

        return cdf;
    }

}
