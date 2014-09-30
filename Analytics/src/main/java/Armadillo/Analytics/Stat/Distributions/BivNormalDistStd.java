package Armadillo.Analytics.Stat.Distributions;

public class BivNormalDistStd {

    private final static double genzX1[] = { -0.932469514203152,
        -0.661209386466265, -0.238619186083197 };

	private final static double genzW1[] = { 0.17132449237917,
	        0.360761573048138, 0.46791393457269 };
	
	private final static double genzX2[] = { -0.981560634246719,
	        -0.904117256370475, -0.769902674194305, -0.587317954286617,
	        -0.36783149899818, -0.125233408511469 };
	
	private final static double genzW2[] = { 4.71753363865118E-02,
	        0.106939325995318, 0.160078328543346, 0.203167426723066,
	        0.233492536538355, 0.249147045813403 };
	
	private final static double genzX3[] = { -0.993128599185095,
	        -0.963971927277914, -0.912234428251326, -0.839116971822219,
	        -0.746331906460151, -0.636053680726515, -0.510867001950827,
	        -0.37370608871542, -0.227785851141645, -7.65265211334973E-02 };
	
	private final static double genzW3[] = { 1.76140071391521E-02,
	        4.06014298003869E-02, 6.26720483341091E-02, 8.32767415767048E-02,
	        0.10193011981724, 0.118194531961518, 0.131688638449177,
	        0.142096109318382, 0.149172986472604, 0.152753387130726 };
	
    /**
     * Calculates the Bivariate Gaussian Integral using Genz's double precision
     * algorithm
     *
     * Algorithm for bivariate gaussian Integral by Alan Genz (2004)
     * Fortran implementations canbe found at
     * http://www.math.wsu.edu/faculty/genz/homepage
     * Implementation taken from C++ implementation by Graeme West
     * Optimised for Java by Anthony Hams
     * @param x the first argument
     * @param y the second argument
     * @param rho the correlation coefficient
     * @return <code>N_2(x,y,rho)</code>
     */
    public final static double CdfStatic(
    		double x, 
    		double y,
            double rho) {
        double W[], X[], rhoAbs;

        /*
         * check if rho is in domain [-1,1]
         */
        if(rho < -1.0 || rho > 1.0 || 1.0 - rho* rho < 0 || Double.isNaN(rho)) {
        	throw new IllegalArgumentException(
        			"rho out of bounds, rho = " + rho + ", should be in [-1,1].");
        }
        
        /*
         * Special cases first.
         * rho = 1, rho = -1.
         * These special cases are discussed in Graeme West paper:
         * "Better Approximations to Cumulative Normal Functions"
         */
        if(rho == 1.0 || rho == -1.0 || Math.sqrt(1.0 - rho * rho) == 0.0) {
        	
        	if(rho > 0) {
        		return UnivNormalDistStd.CdfStatic(Math.min(x, y));
        	}
        	
        	if(rho < 0) {
        		if(y <= x) {
        			return 0.0;
        		} else {
        			return (UnivNormalDistStd.CdfStatic(x) + 
        					UnivNormalDistStd.CdfStatic(y) - 1.0);
        		}
        	}
        }
        
        
        rhoAbs = Math.abs(rho);

        if (rhoAbs < 0.3) {
            W = genzW1;
            X = genzX1;
        } else if (rhoAbs < 0.75) {
            W = genzW2;
            X = genzX2;
        } else {
            W = genzW3;
            X = genzX3;
        }

        double xy = x * y;
        double res = 0.0;

        if (rhoAbs < 0.925) {
            if (rhoAbs > 0) {
                double hs = (x * x + y * y) / 2.0;
                double asr = Math.asin(rho);
                double sn;

                for (int i = 0; i < X.length; ++i) {
                    sn = Math.sin(asr * (1.0 - X[i]) * 0.5);
                    res += W[i] * Math.exp((sn * xy - hs) / (1.0 - sn * sn));

                    sn = Math.sin(asr * (1.0 + X[i]) * 0.5);
                    res += W[i] * Math.exp((sn * xy - hs) / (1.0 - sn * sn));
                }
                
                /* Scale for upper integration boundary asin(rho) and 1/(4pi) */ 
                res *= asr * 1.0 / (4.0 * Math.PI);
            }
            res += UnivNormalDistStd.CdfStatic(x) * UnivNormalDistStd.CdfStatic(y);
        } else {
            if (rho < 0) {
                y *= -1;
                xy *= -1;
            }

            if (rhoAbs < 1) {
                double Ass = (1.0 - rho) * (1.0 + rho);
                double a = Math.sqrt(Ass);
                double bs = (x - y) * (x - y);
                double c = (4.0 - xy) / 8.0;
                double d = (12.0 - xy) / 16.0;
                double asr = -(bs / Ass + xy) / 2;
                double xs, rs;

                if (asr > -100) {
                    res = a
                            * Math.exp(asr)
                            * (1.0 - c * (bs - Ass) * (1 - d * bs / 5.0) / 3.0 + c
                                    * d * Ass * Ass / 5.0);
                }

                if (xy > -100.0) {
                    double B = Math.sqrt(bs);

                    res -= Math.exp(-xy / 2.0) * 2.506628274631
                            * UnivNormalDistStd.CdfStatic(-B / a) * B
                            * (1.0 - c * bs * (1 - d * bs / 5.0) / 3.0);
                }
                a /= 2.0;

                for (int i = 0; i < X.length; ++i) {
                    for (int iss = -1; iss <= 1; iss += 2) {
                        xs = a * (iss * X[i] + 1.0);
                        xs *= xs;
                        rs = Math.sqrt(1.0 - xs);
                        asr = -(bs / xs + xy) / 2.0;

                        if (asr > -100.0) {
                            res += a
                                    * W[i]
                                    * Math.exp(asr)
                                    * (Math.exp(-xy * (1.0 - rs)
                                            / (2.0 * (1.0 + rs)))
                                            / rs - (1.0 + c * xs
                                            * (1.0 + d * xs)));
                        }
                    }
                }
                
                /* Scale for 1/(2pi) */ 
                res *= -1.0 /(2.0 * Math.PI);
            }

            if (rho > 0) {
                res += UnivNormalDistStd.CdfStatic(Math.min(x, y));
            } else {
                res *= -1;

                if (y < x)
                    res += UnivNormalDistStd.CdfStatic(-y) - UnivNormalDistStd.CdfStatic(-x);
            }
        }
        return res;
    }	
}
