package Armadillo.Analytics.SpecialFunctions;

import Armadillo.Analytics.Base.FastMath;

public class BetaFunct {
    // Beta function
    public static double betaFunction(double z, double w)
    {
        return FastMath.exp(LogGammaFunct.logGamma2(z) +
                        LogGammaFunct.logGamma2(w) -
                        LogGammaFunct.logGamma2(z + w));
    }
}
