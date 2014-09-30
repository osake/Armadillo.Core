package Armadillo.Analytics.Stat.Distributions;

import Armadillo.Core.HCException;
import Armadillo.Analytics.SpecialFunctions.IncompleteGamaFunct;

public class ChiSquareDist
{
    /*************************************************************************
    Chi-square distribution

    Returns the area under the left hand tail (from 0 to x)
    of the Chi square probability density function with
    v degrees of freedom.


                                      x
                                       -
                           1          | |  v/2-1  -t/2
     P( x | v )   =   -----------     |   t      e     dt
                       v/2  -       | |
                      2    | (v/2)   -
                                      0

    where x is the Chi-square variable.

    The incomplete gamma integral is used, according to the
    formula

    y = chdtr( v, x ) = igam( v/2.0, x/2.0 ).

    The arguments must both be positive.

    ACCURACY:

    See incomplete gamma function


    Cephes Math Library Release 2.8:  June, 2000
    Copyright 1984, 1987, 2000 by Stephen L. Moshier
    *************************************************************************/
    public static double chisquaredistribution(double v,
        double x)
    {
        double result = 0;

        HCException.doAssert((double)(x)>=(double)(0) && (double)(v)>=(double)(1), "Domain error in ChiSquareDistribution");
        result = IncompleteGamaFunct.IncompleteGamma(v/2.0, x/2.0);
        return result;
    }


    /*************************************************************************
    Complemented Chi-square distribution

    Returns the area under the right hand tail (from x to
    infinity) of the Chi square probability density function
    with v degrees of freedom:

                                     inf.
                                       -
                           1          | |  v/2-1  -t/2
     P( x | v )   =   -----------     |   t      e     dt
                       v/2  -       | |
                      2    | (v/2)   -
                                      x

    where x is the Chi-square variable.

    The incomplete gamma integral is used, according to the
    formula

    y = chdtr( v, x ) = igamc( v/2.0, x/2.0 ).

    The arguments must both be positive.

    ACCURACY:

    See incomplete gamma function

    Cephes Math Library Release 2.8:  June, 2000
    Copyright 1984, 1987, 2000 by Stephen L. Moshier
    *************************************************************************/
    public static double chisquarecdistribution(double v,
        double x)
    {
        double result = 0;

        HCException.doAssert((double)(x)>=(double)(0) && (double)(v)>=(double)(1), "Domain error in ChiSquareDistributionC");
        result = IncompleteGamaFunct.IncompleteGammaComplement(v/2.0, x/2.0);
        return result;
    }


    /*************************************************************************
    Inverse of complemented Chi-square distribution

    Finds the Chi-square argument x such that the integral
    from x to infinity of the Chi-square density is equal
    to the given cumulative probability y.

    This is accomplished using the inverse gamma integral
    function and the relation

       x/2 = igami( df/2, y );

    ACCURACY:

    See inverse incomplete gamma function


    Cephes Math Library Release 2.8:  June, 2000
    Copyright 1984, 1987, 2000 by Stephen L. Moshier
    *************************************************************************/
    public static double invchisquaredistribution(double v,
        double y)
    {
        double result = 0;

        HCException.doAssert(((double)(y)>=(double)(0) && (double)(y)<=(double)(1)) && (double)(v)>=(double)(1), "Domain error in InvChiSquareDistribution");
        result = 2*IncompleteGamaFunct.IncompleteGammaComplement(0.5*v, y);
        return result;
    }


}
