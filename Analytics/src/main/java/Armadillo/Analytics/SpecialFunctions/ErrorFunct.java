package Armadillo.Analytics.SpecialFunctions;

import Armadillo.Analytics.Base.FastMath;
import Armadillo.Analytics.Base.MathConstants;

public class ErrorFunct {

    public  double EvaluateFunction(double dblX)
    {
        return ErrorFunction(dblX);
    }
    
    private static final double[] T = {
            9.60497373987051638749E0,
            9.00260197203842689217E1,
            2.23200534594684319226E3,
            7.00332514112805075473E3,
            5.55923013010394962768E4
        };
    private static final double[] U = {
            //1.00000000000000000000E0,
            3.35617141647503099647E1,
            5.21357949780152679795E2,
            4.59432382970980127987E3,
            2.26290000613890934246E4,
            4.92673942608635921086E4
        };
    
    private static final double[] P = {
            2.46196981473530512524E-10,
            5.64189564831068821977E-1,
            7.46321056442269912687E0,
            4.86371970985681366614E1,
            1.96520832956077098242E2,
            5.26445194995477358631E2,
            9.34528527171957607540E2,
            1.02755188689515710272E3,
            5.57535335369399327526E2
        };
    private static final double[] Q = {
            //1.0
            1.32281951154744992508E1,
            8.67072140885989742329E1,
            3.54937778887819891062E2,
            9.75708501743205489753E2,
            1.82390916687909736289E3,
            2.24633760818710981792E3,
            1.65666309194161350182E3,
            5.57535340817727675546E2
        };

    private static final double[] R = {
            5.64189583547755073984E-1,
            1.27536670759978104416E0,
            5.01905042251180477414E0,
            6.16021097993053585195E0,
            7.40974269950448939160E0,
            2.97886665372100240670E0
        };
    
    private static final double[] S = {
            //1.00000000000000000000E0,
            2.26052863220117276590E0,
            9.39603524938001434673E0,
            1.20489539808096656605E1,
            1.70814450747565897222E1,
            9.60896809063285878198E0,
            3.36907645100081516050E0
        };

    /**
     * Returns the error function of the normal distribution; formerly named <tt>erf</tt>.
     * The integral is
     * <pre>
     *                           x
     *                            -
     *                 2         | |          2
     *   erf(x)  =  --------     |    Exp( - t  ) dt.
     *              sqrt(pi)   | |
     *                          -
     *                           0
     * </pre>
     * <b>Implementation:</b>
     * For <tt>0 <= |x| < 1, erf(x) = x * P4(x**2)/Q5(x**2)</tt>; otherwise
     * <tt>erf(x) = 1 - erfc(x)</tt>.
     * <p>
     * Code adapted from the <A HREF="http://www.sci.usq.taglink.com.dataStructures.mixtureGaussian.edu.au/staff/leighb/graph/Top.html">Java 2D Graph Package 2.4</A>,
     * which in turn is a port from the <A HREF="http://people.ne.mediaone.net/moshier/index.html#Cephes">Cephes 2.2</A> Math Library (C).
     *
     * @param a the argument to the function.
     */

    public static double ErrorFunction(double x)
    {
        double y, z;

        if (Math.abs(x) > 1.0)
        {
            return (1.0 - ErrorFunctionComplemented(x));
        }
        z = x*x;
        y = x*Polynomial.polevl(z, T, 4)/Polynomial.p1evl(z, U, 5);
        return y;
    }

    /**
     * Returns the complementary Error function of the normal distribution; formerly named <tt>erfc</tt>.
     * <pre>
     *  1 - erf(x) =
     *
     *                           inf.
     *                             -
     *                  2         | |          2
     *   erfc(x)  =  --------     |    Exp( - t  ) dt
     *               sqrt(pi)   | |
     *                           -
     *                            x
     * </pre>
     * <b>Implementation:</b>
     * For small x, <tt>erfc(x) = 1 - erf(x)</tt>; otherwise rational
     * approximations are computed.
     * <p>
     * Code adapted from the <A HREF="http://www.sci.usq.taglink.com.dataStructures.mixtureGaussian.edu.au/staff/leighb/graph/Top.html">Java 2D Graph Package 2.4</A>,
     * which in turn is a port from the <A HREF="http://people.ne.mediaone.net/moshier/index.html#Cephes">Cephes 2.2</A> Math Library (C).
     *
     * @param a the argument to the function.
     */

    public static double ErrorFunctionComplemented(double a)
    {
        double x, y, z, p, q;


        if (a < 0.0)
        {
            x = -a;
        }
        else
        {
            x = a;
        }

        if (x < 1.0)
        {
            return 1.0 - ErrorFunction(a);
        }

        z = -a*a;

        if (z < -MathConstants.MAXLOG)
        {
            if (a < 0)
            {
                return (2.0);
            }
            else
            {
                return (0.0);
            }
        }

        z = FastMath.exp(z);

        if (x < 8.0)
        {
            p = Polynomial.polevl(x, P, 8);
            q = Polynomial.p1evl(x, Q, 8);
        }
        else
        {
            p = Polynomial.polevl(x, R, 5);
            q = Polynomial.p1evl(x, S, 6);
        }

        y = (z*p)/q;

        if (a < 0)
        {
            y = 2.0 - y;
        }

        if (y == 0.0)
        {
            if (a < 0)
            {
                return 2.0;
            }
            else
            {
                return (0.0);
            }
        }

        return y;
    }

    public static int Sign(double dblValue)
    {
		if(dblValue < 0){
			return -1;
		}
		else if(dblValue > 0){
			return 1;
		}
		return 0;
    }
    
    private static double GetErrorFunction2(double x)
    {
        double result = 0;
        double xsq = 0;
        double s = 0;
        double p = 0;
        double q = 0;

        s = Sign(x);
        x = Math.abs(x);
        if (x < 0.5)
        {
            xsq = x*x;
            p = 0.007547728033418631287834;
            p = 0.288805137207594084924010 + xsq*p;
            p = 14.3383842191748205576712 + xsq*p;
            p = 38.0140318123903008244444 + xsq*p;
            p = 3017.82788536507577809226 + xsq*p;
            p = 7404.07142710151470082064 + xsq*p;
            p = 80437.3630960840172832162 + xsq*p;
            q = 0.0;
            q = 1.00000000000000000000000 + xsq*q;
            q = 38.0190713951939403753468 + xsq*q;
            q = 658.070155459240506326937 + xsq*q;
            q = 6379.60017324428279487120 + xsq*q;
            q = 34216.5257924628539769006 + xsq*q;
            q = 80437.3630960840172826266 + xsq*q;
            result = s*1.1283791670955125738961589031*x*p/q;
            return result;
        }
        if (x >= 10)
        {
            result = s;
            return result;
        }
        result = s*(1 - GetErrorFunctionComplemented2(x));
        return result;
    }


    private static double GetErrorFunctionComplemented2(double x)
    {
        double result = 0;
        double p = 0;
        double q = 0;

        if (x < 0)
        {
            result = 2 - GetErrorFunctionComplemented2(-x);
            return result;
        }
        if (x < 0.5)
        {
            result = 1.0 - GetErrorFunction2(x);
            return result;
        }
        if (x >= 10)
        {
            result = 0;
            return result;
        }
        p = 0.0;
        p = 0.5641877825507397413087057563 + x*p;
        p = 9.675807882987265400604202961 + x*p;
        p = 77.08161730368428609781633646 + x*p;
        p = 368.5196154710010637133875746 + x*p;
        p = 1143.262070703886173606073338 + x*p;
        p = 2320.439590251635247384768711 + x*p;
        p = 2898.0293292167655611275846 + x*p;
        p = 1826.3348842295112592168999 + x*p;
        q = 1.0;
        q = 17.14980943627607849376131193 + x*q;
        q = 137.1255960500622202878443578 + x*q;
        q = 661.7361207107653469211984771 + x*q;
        q = 2094.384367789539593790281779 + x*q;
        q = 4429.612803883682726711528526 + x*q;
        q = 6089.5424232724435504633068 + x*q;
        q = 4958.82756472114071495438422 + x*q;
        q = 1826.3348842295112595576438 + x*q;
        result = FastMath.exp(-Sqr(x))*p/q;
        return result;
    }

    public static double Sqr(double dblX)
    {
        return dblX*dblX;
    }

    public static double InverseErrorFunction(double e)
    {
        double result = 0;

        result = InverseNormaldistribution(0.5*(e + 1))/Math.sqrt(2);
        return result;
    }


    private static double InverseNormaldistribution(double y0)
    {
        double result = 0;
        double expm2 = 0;
        double s2pi = 0;
        double x = 0;
        double y = 0;
        double z = 0;
        double y2 = 0;
        double x0 = 0;
        double x1 = 0;
        int code = 0;
        double p0 = 0;
        double q0 = 0;
        double p1 = 0;
        double q1 = 0;
        double p2 = 0;
        double q2 = 0;

        expm2 = 0.13533528323661269189;
        s2pi = 2.50662827463100050242;
        if (y0 <= 0)
        {
            result = -MathConstants.MAX_REAL_NUMBER;
            return result;
        }
        if (y0 >= 1)
        {
            result = MathConstants.MAX_REAL_NUMBER;
            return result;
        }
        code = 1;
        y = y0;
        if (y > 1.0 - expm2)
        {
            y = 1.0 - y;
            code = 0;
        }
        if (y > expm2)
        {
            y = y - 0.5;
            y2 = y*y;
            p0 = -59.9633501014107895267;
            p0 = 98.0010754185999661536 + y2*p0;
            p0 = -56.6762857469070293439 + y2*p0;
            p0 = 13.9312609387279679503 + y2*p0;
            p0 = -1.23916583867381258016 + y2*p0;
            q0 = 1;
            q0 = 1.95448858338141759834 + y2*q0;
            q0 = 4.67627912898881538453 + y2*q0;
            q0 = 86.3602421390890590575 + y2*q0;
            q0 = -225.462687854119370527 + y2*q0;
            q0 = 200.260212380060660359 + y2*q0;
            q0 = -82.0372256168333339912 + y2*q0;
            q0 = 15.9056225126211695515 + y2*q0;
            q0 = -1.18331621121330003142 + y2*q0;
            x = y + y*y2*p0/q0;
            x = x*s2pi;
            result = x;
            return result;
        }
        x = Math.sqrt(-(2.0*Math.log(y)));
        x0 = x - Math.log(x)/x;
        z = 1.0/x;
        if (x < 8.0)
        {
            p1 = 4.05544892305962419923;
            p1 = 31.5251094599893866154 + z*p1;
            p1 = 57.1628192246421288162 + z*p1;
            p1 = 44.0805073893200834700 + z*p1;
            p1 = 14.6849561928858024014 + z*p1;
            p1 = 2.18663306850790267539 + z*p1;
            p1 = -(1.40256079171354495875*0.1) + z*p1;
            p1 = -(3.50424626827848203418*0.01) + z*p1;
            p1 = -(8.57456785154685413611*0.0001) + z*p1;
            q1 = 1;
            q1 = 15.7799883256466749731 + z*q1;
            q1 = 45.3907635128879210584 + z*q1;
            q1 = 41.3172038254672030440 + z*q1;
            q1 = 15.0425385692907503408 + z*q1;
            q1 = 2.50464946208309415979 + z*q1;
            q1 = -(1.42182922854787788574*0.1) + z*q1;
            q1 = -(3.80806407691578277194*0.01) + z*q1;
            q1 = -(9.33259480895457427372*0.0001) + z*q1;
            x1 = z*p1/q1;
        }
        else
        {
            p2 = 3.23774891776946035970;
            p2 = 6.91522889068984211695 + z*p2;
            p2 = 3.93881025292474443415 + z*p2;
            p2 = 1.33303460815807542389 + z*p2;
            p2 = 2.01485389549179081538*0.1 + z*p2;
            p2 = 1.23716634817820021358*0.01 + z*p2;
            p2 = 3.01581553508235416007*0.0001 + z*p2;
            p2 = 2.65806974686737550832*0.000001 + z*p2;
            p2 = 6.23974539184983293730*0.000000001 + z*p2;
            q2 = 1;
            q2 = 6.02427039364742014255 + z*q2;
            q2 = 3.67983563856160859403 + z*q2;
            q2 = 1.37702099489081330271 + z*q2;
            q2 = 2.16236993594496635890*0.1 + z*q2;
            q2 = 1.34204006088543189037*0.01 + z*q2;
            q2 = 3.28014464682127739104*0.0001 + z*q2;
            q2 = 2.89247864745380683936*0.000001 + z*q2;
            q2 = 6.79019408009981274425*0.000000001 + z*q2;
            x1 = z*p2/q2;
        }
        x = x0 - x1;
        if (code != 0)
        {
            x = -x;
        }
        result = x;
        return result;
    }


}
