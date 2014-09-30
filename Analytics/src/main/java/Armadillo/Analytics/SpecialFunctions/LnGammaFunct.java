package Armadillo.Analytics.SpecialFunctions;

public class LnGammaFunct
{
    public static double LnGamma(double x,
                                 double[] sgngam)
    {
        double result = 0;
        double a = 0;
        double b = 0;
        double c = 0;
        double p = 0;
        double q = 0;
        double u = 0;
        double w = 0;
        double z = 0;
        int i = 0;
        double logpi = 0;
        double ls2pi = 0;
        double[] tmp = new double[1];

        sgngam[0] = 1;
        logpi = 1.14472988584940017414;
        ls2pi = 0.91893853320467274178;
        if (x < -34.0)
        {
            q = -x;
            w = LnGamma(q, tmp);
            p = (int) Math.floor(q);
            i = (int) Math.round(p);
            if (i%2 == 0)
            {
                sgngam[0] = -1;
            }
            else
            {
                sgngam[0] = 1;
            }
            z = q - p;
            if (z > 0.5)
            {
                p = p + 1;
                z = p - q;
            }
            z = q*Math.sin(Math.PI*z);
            result = logpi - Math.log(z) - w;
            return result;
        }
        if (x < 13)
        {
            z = 1;
            p = 0;
            u = x;
            while (u >= 3)
            {
                p = p - 1;
                u = x + p;
                z = z*u;
            }
            while (u < 2)
            {
                z = z/u;
                p = p + 1;
                u = x + p;
            }
            if (z < 0)
            {
                sgngam[0] = -1;
                z = -z;
            }
            else
            {
                sgngam[0] = 1;
            }
            if (u == 2)
            {
                result = Math.log(z);
                return result;
            }
            p = p - 2;
            x = x + p;
            b = -1378.25152569120859100;
            b = -38801.6315134637840924 + x*b;
            b = -331612.992738871184744 + x*b;
            b = -1162370.97492762307383 + x*b;
            b = -1721737.00820839662146 + x*b;
            b = -853555.664245765465627 + x*b;
            c = 1;
            c = -351.815701436523470549 + x*c;
            c = -17064.2106651881159223 + x*c;
            c = -220528.590553854454839 + x*c;
            c = -1139334.44367982507207 + x*c;
            c = -2532523.07177582951285 + x*c;
            c = -2018891.41433532773231 + x*c;
            p = x*b/c;
            result = Math.log(z) + p;
            return result;
        }
        q = (x - 0.5)*Math.log(x) - x + ls2pi;
        if (x > 100000000)
        {
            result = q;
            return result;
        }
        p = 1/(x*x);
        if (x >= 1000.0)
        {
            q = q +
                ((7.9365079365079365079365*0.0001*p - 2.7777777777777777777778*0.001)*p + 0.0833333333333333333333)/
                x;
        }
        else
        {
            a = 8.11614167470508450300*0.0001;
            a = -(5.95061904284301438324*0.0001) + p*a;
            a = 7.93650340457716943945*0.0001 + p*a;
            a = -(2.77777777730099687205*0.001) + p*a;
            a = 8.33333333333331927722*0.01 + p*a;
            q = q + a/x;
        }
        result = q;
        return result;
    }
}
