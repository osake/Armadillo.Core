package Armadillo.Analytics.Stat.Distributions;

import Armadillo.Analytics.Base.FastMath;
import Armadillo.Analytics.SpecialFunctions.BetaFunct;
import Armadillo.Analytics.SpecialFunctions.IncompleteBetaFunct;
import Armadillo.Analytics.SpecialFunctions.LogGammaFunct;
import Armadillo.Analytics.SpecialFunctions.RegularizedBetaFunction;
import Armadillo.Analytics.Stat.Random.IRng;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.Console;
import Armadillo.Core.DoubleHelper;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;

/**
* getBeta() distribution; <A HREF="http://www.taglink.com.dataStructures.mixtureGaussian.cern.ch/RD11/rkb/AN16pp/node15.html#SECTION000150000000000000000"> math definition</A>
* and <A HREF="http://www.statsoft.com/textbook/glosb.html#getBeta() Distribution"> animated definition</A>.
* <p>
* <tt>p(x) = k * x^(alpha-1) * (1-x)^(beta-1)</tt> with <tt>k = g(alpha+beta)/(g(alpha)*g(beta))</tt> and <tt>g(a)</tt> being the gamma function.
* <p>
* Valid parameter ranges: <tt>alpha &gt; 0</tt> and <tt>beta &gt; 0</tt>.
* <p>
* Instance methods operate on a user supplied uniform random number generator; they are unsynchronized.
* <dt>
* Static methods operate on a default uniform random number generator; they are synchronized.
* <p>
* <b>Implementation:</b>
* <dt>Method: Stratified Rejection/Patchwork Rejection.
* High performance implementation.
* <dt>This is a port of <tt>bsprc.c</tt> from the <A HREF="http://www.cis.tu-graz.ac.at/stat/stadl/random.html">C-RAND / WIN-RAND</A> library.
* C-RAND's implementation, in turn, is based upon
* <p>
* H. Sakasegawa (1983): Stratified rejection and squeeze method for generating beta random numbers,
* Ann. Inst. Statist. Math. 35 B, 291-302.
* <p>
* and
* <p>
* Stadlober E., H. Zechner (1993), <A HREF="http://www.cis.tu-graz.ac.at/stat/stadl/random.html"> Generating beta variates via patchwork rejection,</A>,
* Computing 50, 1-18.
*
* @author wolfgang.hoschek@taglink.com.dataStructures.mixtureGaussian.cern.ch
* @version 1.0, 09/24/99
*/

/// <summary>
/// getBeta() distribution class.
/// Quick calculation of getBeta() probabilities.
/// getBeta() values are stored in memory in order to speedup computation.
/// The data is loaded via serialization when the classs is initialized.
/// Note: This class is not threadsafe
/// </summary>
public class BetaDist extends AUnivContDist
{
    private static final int INT_RND_SEED = 1;
	
    /// <summary>
    /// Own instance
    /// </summary>
    private static final BetaDist m_ownInstance = new BetaDist(
        1, 1, new RngWrapper(INT_RND_SEED));

    private double m_dblAlpha;
    private double m_dblBeta;

	private double m_dblPdfConst;



    public BetaDist(
            double dblAlpha,
            double dblBeta)
        {
        	this(dblAlpha,dblBeta, null);
        }
	
    public BetaDist(
        double dblAlpha,
        double dblBeta,
        RngWrapper rng)
    {
    	super(rng);
        SetState(
            dblAlpha,
            dblBeta);
    }

    public double getAlpha()
    {
        return m_dblAlpha; 
    }

    public void setAlpha(double dblValue)
    {
        m_dblAlpha = dblValue;
        SetState(
            m_dblAlpha,
            m_dblBeta);
    }

    public double getBeta()
    {
        return m_dblBeta; 
    }
    
    public void setBeta(double dblValue){
    	
            m_dblBeta = dblValue;
            SetState(
                m_dblAlpha,
                m_dblBeta);
    }

    private void SetState(
        double dblAlpha,
        double dblBeta)
    {
        m_dblAlpha = dblAlpha;
        m_dblBeta = dblBeta;
        m_dblPdfConst = LogGammaFunct.LogGamma(getAlpha() + getBeta()) -
                        LogGammaFunct.LogGamma(getAlpha()) -
                        LogGammaFunct.LogGamma(getBeta());
    }

    /// <summary>
    /// Calculates the beta distribution. This method is expensive.
    /// </summary>
    /// <param name="dblAlpha">
    /// getAlpha() parameter for beta distribution
    /// </param>
    /// <param name="dblBeta">
    /// getBeta() parameter for getBeta() distribution
    /// </param>
    /// <param name="dblCurrentLoss">
    /// Loss threshold
    /// </param>
    /// <returns>
    /// getBeta() distribution value
    /// </returns>
    /**
     * Returns the area from zero to <tt>x</tt> under the beta density
     * function.
     * <pre>
     *                          x
     *            -             -
     *           | (a+b)       | |  a-1      b-1
     * P(x)  =  ----------     |   t    (1-t)    dt
     *           -     -     | |
     *          | (a) | (b)   -
     *                         0
     * </pre>
     * This function is identical to the incomplete beta
     * integral function <tt>Gamma.incompleteBeta(a, b, x)</tt>.
     *
     * The complemented function is
     *
     * <tt>1 - P(1-x)  =  Gamma.incompleteBeta( b, a, x )</tt>;
     *
     */
    public  double Cdf(
        double dblX)
    {
        return IncompleteBetaFunct.IncompleteBeta(
            getAlpha(),
            getBeta(),
            dblX);
    }

    /**
     * Returns the area under the right hand tail (from <tt>x</tt> to
     * infinity) of the beta density function.
     *
     * This function is identical to the incomplete beta
     * integral function <tt>Gamma.incompleteBeta(b, a, x)</tt>.
     */

    public double CdfComplemented(
        double dblX)
    {
        return IncompleteBetaFunct.IncompleteBeta(
            getBeta(),
            getAlpha(),
            dblX);
    }

    public  double CdfInv(
        double dblProb)
    {
        return CdfInvStatic(
            getAlpha(),
            getBeta(),
            dblProb);
    }

    /**
     * Returns the PDF function.
     */

    public double Pdf(double dblX)
    {
        if (dblX < 0 || dblX > 1)
        {
            return 0.0;
        }
        return FastMath.exp(m_dblPdfConst)*
               Math.pow(dblX, getAlpha() - 1)*
               Math.pow(1 - dblX, getBeta() - 1);
    }

    public double NextDouble()
    {
    	return NextDouble(m_rng);
    }
    

    /**
     * Returns a beta distributed random number; bypasses the internal state.
     */

    public double NextDouble(IRng rng)
    {
		double dblGamma11 = GammaDist.NextDoubleStatic(m_dblAlpha, 1, rng);
		double dblGamma2 = GammaDist.NextDoubleStatic(m_dblBeta, 1, rng);
		double dblCurrBeta = dblGamma11 / (dblGamma11  + dblGamma2);
		return dblCurrBeta;
    }
    
    // old method. This used to be used. The other one is faster
    public double NextDoubleOld(IRng rng)
    {
        double[] m_dblA = new double[1];
        double[] m_dblA_ = new double[1];
        double[] m_dblA_last = new double[1];
        double[] m_dblB = new double[1];
        double[] m_dblB_ = new double[1];
        double[] m_dblB_last = new double[1];
        double[] m_dblC = new double[1];
        double[] m_dblD = new double[1];
        double[] m_dblD1 = new double[1];
        double[] m_dblDl = new double[1];
        double[] m_dblF2 = new double[1];
        double[] m_dblF4 = new double[1];
        double[] m_dblF5 = new double[1];
        double[] m_dblFa = new double[1];
        double[] m_dblFb = new double[1];
        double[] m_dblLl = new double[1];
        double[] m_dblLr = new double[1];
        double[] m_dblM = new double[1];
        double[] m_dblMl = new double[1];
        double[] m_dblMu = new double[1];
        double[] m_dblP1 = new double[1];
        double[] m_dblP2 = new double[1];
        double[] m_dblP3 = new double[1];
        double[] m_dblP4 = new double[1];
        double[] m_dblPLast = new double[1];
        double[] m_dblQLast = new double[1];
        double[] m_dblS = new double[1];
        double[] m_dblT = new double[1];
        double[] m_dblX1 = new double[1];
        double[] m_dblX2 = new double[1];
        double[] m_dblX4 = new double[1];
        double[] m_dblX5 = new double[1];
        double[] m_dblZ2 = new double[1];
        double[] m_dblZ4 = new double[1];
        
    	try{
        /******************************************************************
         *                                                                *
         * getBeta() Distribution - Stratified Rejection/Patchwork Rejection   *
         *                                                                *
         ******************************************************************
         * For parameters a < 1 , b < 1  and  a < 1 < b   or  b < 1 < a   *
         * the stratified rejection methods b00 and b01 of Sakasegawa are *
         * used. Both procedures employ suitable two-part power functions *
         * from which samples can be obtained by inversion.               *
         * If a > 1 , b > 1 (unimodal case) the patchwork rejection       *
         * method b1prs of Zechner/Stadlober is utilized:                 *
         * The area below the density function f(x) in its body is        *
         * rearranged by certain point reflections. Within a large center *
         * interval variates are sampled efficiently by rejection from    *
         * uniform hats. Rectangular immediate acceptance regions speed   *
         * up the generation. The remaining tails are covered by          *
         * exponential functions.                                         *
         * If (a-1)(b-1) = 0  sampling is done by inversion if either a   *
         * or b are not equal to one. If  a = b = 1  a uniform random     *
         * variate is delivered.                                          *
         *                                                                *
         ******************************************************************
         *                                                                *
         * FUNCTION :   - bsprc samples a random variate from the beta    *
         *                distribution with parameters  a > 0, b > 0.     *
         * REFERENCES : - H. Sakasegawa (1983): Stratified rejection and  *
         *                squeeze method for generating beta random       *
         *                numbers, Ann. Inst. Statist. Math. 35 B,        *
         *                291-302.                                        *
         *              - H. Zechner, E. Stadlober (1993): Generating     *
         *                beta variates via patchwork rejection,          *
         *                Computing 50, 1-18.                             *
         *                                                                *
         * SUBPROGRAMS: - drand(seed) ... (0,1)-Uniform generator with    *
         *                unsigned long integer *seed.                    *
         *              - b00(seed,a,b) ... getBeta() generator for a<1, b<1   *
         *              - b01(seed,a,b) ... getBeta() generator for a<1<b or   *
         *                                  b<1<a                         *
         *              - b1prs(seed,a,b) ... getBeta() generator for a>1, b>1 *
         *                with unsigned long integer *seed, double a, b.  *
         *                                                                *
         ******************************************************************/
        double a = getAlpha();
        double b = getBeta();
        if (a > 1.0)
        {
            if (b > 1.0)
            {
                double dblResult = b1prs(a, b, rng, 
                        m_dblPLast, 
                        m_dblQLast, 
                        m_dblA, 
                        m_dblB, 
                        m_dblS, 
                        m_dblM, 
                        m_dblD, 
                        m_dblX2, 
                        m_dblX1, 
                        m_dblD1, 
                        m_dblZ2, 
                        m_dblDl, 
                        m_dblLl, 
                        m_dblF2, 
                        m_dblX4, 
                        m_dblX5, 
                        m_dblF5, 
                        m_dblLr, 
                        m_dblZ4, 
                        m_dblF4, 
                        m_dblP1, 
                        m_dblP2, 
                        m_dblP3, 
                        m_dblP4);
            	if(!DoubleHelper.isAValidNumber(dblResult)){
            		throw new HCException("Invalid value");
            	}
                return dblResult;
            }
            if (b < 1.0)
            {
                double dblResult = (1.0 - b01(b, a, rng, 
                        m_dblA_last, 
                        m_dblB_last, 
                        m_dblA_, 
                        m_dblB_, 
                        m_dblT, 
                        m_dblFb, 
                        m_dblFa, 
                        m_dblMl, 
                        m_dblMu, 
                        m_dblP1, 
                        m_dblP2));
            	if(!DoubleHelper.isAValidNumber(dblResult)){
            		throw new HCException("Invalid value");
            	}
                return dblResult;
            }
            if (b == 1.0)
            {
                double dblResult = (FastMath.exp(Math.log(rng.nextDouble())/a));
            	if(!DoubleHelper.isAValidNumber(dblResult)){
            		throw new HCException("Invalid value");
            	}
                return dblResult;
            }
        }

        if (a < 1.0)
        {
            if (b > 1.0)
            {
                double dblResult = b01(a, b, rng, 
                        m_dblA_last, 
                        m_dblB_last, 
                        m_dblA_, 
                        m_dblB_, 
                        m_dblT, 
                        m_dblFb, 
                        m_dblFa, 
                        m_dblMl, 
                        m_dblMu, 
                        m_dblP1, 
                        m_dblP2);
            	if(!DoubleHelper.isAValidNumber(dblResult)){
            		throw new HCException("Invalid value");
            	}
                return dblResult;
            }
            if (b < 1.0)
            {
                double dblResult = (b00(
                		a, 
                		b, 
                		rng, 
                        m_dblA_last, 
                        m_dblB_last, 
                        m_dblA_, 
                        m_dblB_, 
                        m_dblT, 
                        m_dblC, 
                        m_dblFa, 
                        m_dblFb, 
                        m_dblP2, 
                        m_dblP1));
            	if(!DoubleHelper.isAValidNumber(dblResult)){
            		throw new HCException("Invalid value");
            	}
                return dblResult;
                
            }
            if (b == 1.0)
            {
                double dblResult = (FastMath.exp(Math.log(rng.nextDouble())/a));
            	if(!DoubleHelper.isAValidNumber(dblResult)){
            		throw new HCException("Invalid value");
            	}
                return dblResult;
            }
        }

        if (a == 1.0)
        {
            if (b != 1.0)
            {
            	double dblResult = (1.0 - FastMath.exp(Math.log(rng.nextDouble())/b));
            	if(!DoubleHelper.isAValidNumber(dblResult)){
            		throw new HCException("Invalid value");
            	}
                return dblResult;
            }
            if (b == 1.0)
            {
                return (rng.nextDouble());
            }
        }
    	}
        catch(Exception ex){
        	Logger.log(ex);
        }


        return 0.0;
    }

    /**
     *  Be(alpha1, alpha2)
     *
     *@param  alpha1  alpha1
     *@param  alpha2  alpha2
     *@return         Be(alpha1, alpha2)
     *@author:        <Vadum Kutsyy, kutsyy@hotmail.com>
     */

    public double NextDouble2()
    {
        // to do : compare performance for this method

        GammaDist univariateGammaDistribution1 =
            new GammaDist(getAlpha(), 1, m_rng);
        GammaDist univariateGammaDistribution2 =
            new GammaDist(getAlpha(), 1, m_rng);

        if (getAlpha() == 1 && getBeta() == 1)
        {
            return m_rng.nextDouble();
        }
        double y1 = univariateGammaDistribution1.NextDouble();
        return y1/(y1 + univariateGammaDistribution2.NextDouble());
    }

    /**
     * Returns a string representation of the receiver.
     */
    @Override
    public String toString()
    {
        return "BetaDist(" + getAlpha() + "," + getBeta() + ")";
    }

    // beta distribution mean
    public static double betaMean(double alpha, double beta)
    {
        return betaMean(0.0D, 1.0D, alpha, beta);
    }

    // beta distribution mean
    public static double betaMean(double min, double max, double alpha, double beta)
    {
    	try{
        if (alpha <= 0.0D)
        {
            throw new Exception("The shape parameter, alpha, " + alpha + "must be greater than zero");
        }
        if (beta <= 0.0D)
        {
            throw new Exception("The shape parameter, beta, " + beta + "must be greater than zero");
        }
        return min + alpha*(max - min)/(alpha + beta);
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return Double.NaN;
    }

    // beta distribution mode
    public static double betaMode(double alpha, double beta)
    {
        return betaMode(0.0D, 1.0D, alpha, beta);
    }

    // beta distribution mode
    public static double betaMode(double min, double max, double alpha, double beta)
    {
    	try{
        if (alpha <= 0.0D)
        {
            throw new Exception("The shape parameter, alpha, " + alpha + "must be greater than zero");
        }
        if (beta <= 0.0D)
        {
            throw new Exception("The shape parameter, beta, " + beta + "must be greater than zero");
        }

        double mode = Double.NaN;
        if (alpha > 1)
        {
            if (beta > 1)
            {
                mode = min + (alpha + beta)*(max - min)/(alpha + beta - 2);
            }
            else
            {
                mode = max;
            }
        }
        else
        {
            if (alpha == 1)
            {
                if (beta > 1)
                {
                    mode = min;
                }
                else
                {
                    if (beta == 1)
                    {
                        mode = Double.NaN;
                    }
                    else
                    {
                        mode = max;
                    }
                }
            }
            else
            {
                if (beta >= 1)
                {
                    mode = min;
                }
                else
                {
                	Console.writeLine("Class Stat; method betaMode; distribution is bimodal wirh modes at " +
                                            min + " and " + max);
                    Console.writeLine("NaN returned");
                }
            }
        }
        return mode;
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return Double.NaN;
    }

    // beta distribution standard deviation
    public static double betaStandardDeviation(double alpha, double beta)
    {
        return betaStandDev(alpha, beta);
    }

    // beta distribution standard deviation
    public static double betaStandDev(double alpha, double beta)
    {
        return betaStandDev(0.0D, 1.0D, alpha, beta);
    }

    // beta distribution standard deviation
    public static double betaStandardDeviation(double min, double max, double alpha, double beta)
    {
        return betaStandDev(min, max, alpha, beta);
    }

    // beta distribution standard deviation
    public static double betaStandDev(double min, double max, double alpha, double beta)
    {
    	try{
        if (alpha <= 0.0D)
        {
            throw new Exception("The shape parameter, alpha, " + alpha + "must be greater than zero");
        }
        if (beta <= 0.0D)
        {
            throw new Exception("The shape parameter, beta, " + beta + "must be greater than zero");
        }
        return ((max - min)/(alpha + beta))*Math.sqrt(alpha*beta/(alpha + beta + 1));
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return Double.NaN;
    }

    // beta distribution pdf
    public static double betaPDF(double min, double max, double alpha, double beta, double x)
    {
    	try{
        if (alpha <= 0.0D)
        {
            throw new Exception("The shape parameter, alpha, " + alpha + "must be greater than zero");
        }
        if (beta <= 0.0D)
        {
            throw new Exception("The shape parameter, beta, " + beta + "must be greater than zero");
        }
        if (x < min)
        {
            throw new Exception("x, " + x + ", must be greater than or equal to the minimum value, " + min);
        }
        if (x > max)
        {
            throw new Exception("x, " + x + ", must be less than or equal to the maximum value, " + max);
        }
        double pdf = Math.pow(x - min, alpha - 1)*Math.pow(max - x, beta - 1)/Math.pow(max - min, alpha + beta - 1);
        return pdf/BetaFunct.betaFunction(alpha, beta);
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return Double.NaN;
    }

    public static double PdfStatic(
        double dblAlpha,
        double dblBeta,
        double dblX)
    {
        m_ownInstance.SetState(
            dblAlpha,
            dblBeta);
        return m_ownInstance.Pdf(dblX);
    }

    public static double CdfStatic(
        double dblAlpha,
        double dblBeta,
        double dblX)
    {
        double dblCdf =
            IncompleteBetaFunct.IncompleteBeta(
                dblAlpha,
                dblBeta,
                dblX);
        //
        // validate small negative value
        //
        if (dblCdf < 0.0 && dblCdf > -1E-25)
        {
            dblCdf = 0.0;
        }
        return dblCdf;
    }

    // beta distribution pdf
    public static double betaCDF(double min, double max, double alpha, double beta, double limit)
    {
    	try{
        if (alpha <= 0.0D)
        {
            throw new Exception("The shape parameter, alpha, " + alpha + "must be greater than zero");
        }
        if (beta <= 0.0D)
        {
            throw new Exception("The shape parameter, beta, " + beta + "must be greater than zero");
        }
        if (limit < min)
        {
            throw new Exception("limit, " + limit + ", must be greater than or equal to the minimum value, " +
                                        min);
        }
        if (limit > max)
        {
            throw new Exception("limit, " + limit + ", must be less than or equal to the maximum value, " +
                                        max);
        }
        return RegularizedBetaFunction.regularisedBetaFunction(alpha, beta, (limit - min)/(max - min));
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return Double.NaN;
    }


    public static double CdfInvStatic(
        double dblAlpha,
        double dblBeta,
        double dblProbability)
    {
        // If there is a CV less than 10^-10 then we have an
        // effective delta, so just return the mean.
        if ((dblBeta/Math.pow(dblAlpha + dblBeta, 2)) < Math.pow(10, -10))
        {
            return (dblAlpha/(dblAlpha + dblBeta));
        }

        double inverseValue =
            IncompleteBetaFunct.InvIncompleteBeta(
                dblAlpha,
                dblBeta,
                dblProbability);
        if (inverseValue < 10E-100)
        {
            inverseValue = 0.0;
        }
        return inverseValue;
    }

    public static double NextDoubleStatic(
        double dblAlpha,
        double dblBeta)
    {
        m_ownInstance.SetState(
            dblAlpha,
            dblBeta);
        return m_ownInstance.NextDouble();
    }

    public static double[] NextDoubleArrStatic(
        double dblAlpha,
        double dblBeta,
        int intSampleSize)
    {
        m_ownInstance.SetState(
            dblAlpha,
            dblBeta);

        return m_ownInstance.NextDoubleArr(intSampleSize);
    }

//    public static Vector NextDoubleVectorStatic(
//        double dblAlpha,
//        double dblBeta,
//        int intSampleSize)
//    {
//        m_ownInstance.SetState(
//            dblAlpha,
//            dblBeta);
//
//        return m_ownInstance.NextDoubleVector(intSampleSize);
//    }

    private double b00(
        double a,
        double b,
        IRng m_rng, 
        double[] m_dblA_last, 
        double[] m_dblB_last, 
        double[] m_dblA_, 
        double[] m_dblB_, 
        double[] m_dblT, 
        double[] m_dblC, 
        double[] m_dblFa, 
        double[] m_dblFb, 
        double[] m_dblP2, 
        double[] m_dblP1)
    {
        double U, V, X, Z;

        if (a != m_dblA_last[0] || b != m_dblB_last[0])
        {
            m_dblA_last[0] = a;
            m_dblB_last[0] = b;

            m_dblA_[0] = a - 1.0;
            m_dblB_[0] = b - 1.0;
            m_dblC[0] = (b*m_dblB_[0])/(a*m_dblA_[0]); // b(1-b) / a(1-a)
            m_dblT[0] = (m_dblC[0] == 1.0) ? 0.5 : (1.0 - Math.sqrt(m_dblC[0]))/
            		(1.0 - m_dblC[0]); // t = t_opt
            m_dblFa[0] = FastMath.exp(m_dblA_[0]*Math.log(m_dblT[0]));
            m_dblFb[0] = FastMath.exp(m_dblB_[0]*Math.log(1.0 - m_dblT[0])); // f(t) = fa * fb

            m_dblP1[0] = m_dblT[0]/a; // 0 < X < t
            m_dblP2[0] = (1.0 - m_dblT[0])/b + m_dblP1[0]; // t < X < 1
        }

        for (;;)
        {
            if ((U = m_rng.nextDouble()*m_dblP2[0]) <= m_dblP1[0])
            {
                //  X < t
                Z = FastMath.exp(Math.log(U/m_dblP1[0])/a);
                X = m_dblT[0]*Z;
                // squeeze accept:   L(x) = 1 + (1 - b)x
                if ((V = m_rng.nextDouble()*m_dblFb[0]) <= 1.0 - m_dblB_[0]*X)
                {
                    break;
                }
                // squeeze reject:   U(x) = 1 + ((1 - t)^(b-1) - 1)/t * x
                if (V <= 1.0 + (m_dblFb[0] - 1.0)*Z)
                {
                    // quotient accept:  q(x) = (1 - x)^(b-1) / fb
                    if (Math.log(V) <= m_dblB_[0]*Math.log(1.0 - X))
                    {
                        break;
                    }
                }
            }
            else
            {
                //  X > t
                Z = FastMath.exp(Math.log((U - m_dblP1[0])/(m_dblP2[0] - m_dblP1[0]))/b);
                X = 1.0 - (1.0 - m_dblT[0])*Z;
                // squeeze accept:   L(x) = 1 + (1 - a)(1 - x)
                if ((V = m_rng.nextDouble()*m_dblFa[0]) <= 1.0 - m_dblA_[0]*(1.0 - X))
                {
                    break;
                }
                // squeeze reject:   U(x) = 1 + (t^(a-1) - 1)/(1 - t) * (1 - x)
                if (V <= 1.0 + (m_dblFa[0] - 1.0)*Z)
                {
                    // quotient accept:  q(x) = x^(a-1) / fa
                    if (Math.log(V) <= m_dblA_[0]*Math.log(X))
                    {
                        break;
                    }
                }
            }
        }
        return (X);
    }

    /**
     * @param m_dblA_last 
     * @param m_dblB_last 
     * @param m_dblA_ 
     * @param m_dblB_ 
     * @param m_dblT 
     * @param m_dblFb 
     * @param m_dblFa 
     * @param m_dblMl 
     * @param m_dblMu 
     * @param m_dblP1 
     * @param m_dblP2 
     *
     */

    protected double b01(
        double a,
        double b,
        IRng m_rng, 
        double[] m_dblA_last, 
        double[] m_dblB_last, 
        double[] m_dblA_, 
        double[] m_dblB_, 
        double[] m_dblT, 
        double[] m_dblFb, 
        double[] m_dblFa, 
        double[] m_dblMl, 
        double[] m_dblMu, 
        double[] m_dblP1, 
        double[] m_dblP2)
    {
        double U, V, X, Z;

        if (a != m_dblA_last[0] || b != m_dblB_last[0])
        {
            m_dblA_last[0] = a;
            m_dblB_last[0] = b;

            m_dblA_[0] = a - 1.0;
            m_dblB_[0] = b - 1.0;
            m_dblT[0] = m_dblA_[0]/(a - b); // one step Newton * start value t
            m_dblFb[0] = FastMath.exp((m_dblB_[0] - 1.0)*Math.log(1.0 - m_dblT[0]));
            m_dblFa[0] = a - (a + m_dblB_[0])*m_dblT[0];
            m_dblT[0] -= (m_dblT[0] - (1.0 - m_dblFa[0])*(1.0 - m_dblT[0])*
            		m_dblFb[0]/b)/(1.0 - m_dblFa[0]*m_dblFb[0]);
            m_dblFa[0] = FastMath.exp(m_dblA_[0]*Math.log(m_dblT[0]));
            m_dblFb[0] = FastMath.exp(m_dblB_[0]*Math.log(1.0 - m_dblT[0])); // f(t) = fa * fb
            if (m_dblB_[0] <= 1.0)
            {
                m_dblMl[0] = (1.0 - m_dblFb[0])/m_dblT[0]; //   ml = -m1
                m_dblMu[0] = m_dblB_[0]*m_dblT[0]; //   mu = -m2 * t
            }
            else
            {
                m_dblMl[0] = m_dblB_[0];
                m_dblMu[0] = 1.0 - m_dblFb[0];
            }
            m_dblP1[0] = m_dblT[0]/a; //  0 < X < t
            m_dblP2[0] = m_dblFb[0]*(1.0 - m_dblT[0])/b + m_dblP1[0]; //  t < X < 1
        }

        for (;;)
        {
            if ((U = m_rng.nextDouble()*m_dblP2[0]) <= m_dblP1[0])
            {
                //  X < t
                Z = FastMath.exp(Math.log(U/m_dblP1[0])/a);
                X = m_dblT[0]*Z;
                // squeeze accept:   L(x) = 1 + m1*x,  ml = -m1
                if ((V = m_rng.nextDouble()) <= 1.0 - m_dblMl[0]*X)
                {
                    break;
                }
                // squeeze reject:   U(x) = 1 + m2*x,  mu = -m2 * t
                if (V <= 1.0 - m_dblMu[0]*Z)
                {
                    // quotient accept:  q(x) = (1 - x)^(b-1)
                    if (Math.log(V) <= m_dblB_[0]*Math.log(1.0 - X))
                    {
                        break;
                    }
                }
            }
            else
            {
                //  X > t
                Z = FastMath.exp(Math.log((U - m_dblP1[0])/(m_dblP2[0] - m_dblP1[0]))/b);
                X = 1.0 - (1.0 - m_dblT[0])*Z;
                // squeeze accept:   L(x) = 1 + (1 - a)(1 - x)
                if ((V = m_rng.nextDouble()*m_dblFa[0]) <= 1.0 - m_dblA_[0]*(1.0 - X))
                {
                    break;
                }
                // squeeze reject:   U(x) = 1 + (t^(a-1) - 1)/(1 - t) * (1 - x)
                if (V <= 1.0 + (m_dblFa[0] - 1.0)*Z)
                {
                    // quotient accept:  q(x) = (x)^(a-1) / fa
                    if (Math.log(V) <= m_dblA_[0]*Math.log(X))
                    {
                        break;
                    }
                }
            }
        }
        return (X);
    }

    protected double b1prs(
        double p,
        double q,
        IRng m_rng, 
        double[] m_dblPLast, 
        double[] m_dblQLast, 
        double[] m_dblA, 
        double[] m_dblB, 
        double[] m_dblS, 
        double[] m_dblM, 
        double[] m_dblD, 
        double[] m_dblX2, 
        double[] m_dblX1, 
        double[] m_dblD1, 
        double[] m_dblZ2, 
        double[] m_dblDl, 
        double[] m_dblLl, 
        double[] m_dblF2, 
        double[] m_dblX4, 
        double[] m_dblX5, 
        double[] m_dblF5, 
        double[] m_dblLr, 
        double[] m_dblZ4, 
        double[] m_dblF4, 
        double[] m_dblP1, 
        double[] m_dblP2, 
        double[] m_dblP3, 
        double[] m_dblP4)
    {
        double U, V, W, X, Y;

        if (p != m_dblPLast[0] || q != m_dblQLast[0])
        {
            m_dblPLast[0] = p;
            m_dblQLast[0] = q;

            m_dblA[0] = p - 1.0;
            m_dblB[0] = q - 1.0;
            m_dblS[0] = m_dblA[0] + m_dblB[0];
            m_dblM[0] = m_dblA[0]/m_dblS[0];
            if (m_dblA[0] > 1.0 || m_dblB[0] > 1.0)
            {
                m_dblD[0] = Math.sqrt(m_dblM[0]*(1.0 - m_dblM[0])/(m_dblS[0] - 1.0));
            }

            if (m_dblA[0] <= 1.0)
            {
                m_dblX2[0] = (m_dblDl[0] = m_dblM[0]*0.5);
                m_dblX1[0] = m_dblZ2[0] = 0.0;
                m_dblD1[0] = m_dblLl[0] = 0.0;
            }
            else
            {
                m_dblX2[0] = m_dblM[0] - m_dblD[0];
                m_dblX1[0] = m_dblX2[0] - m_dblD[0];
                m_dblZ2[0] = m_dblX2[0]*(1.0 - (1.0 - m_dblX2[0])/(m_dblS[0]*m_dblD[0]));
                if (m_dblX1[0] <= 0.0 || (m_dblS[0] - 6.0)*m_dblX2[0] - m_dblA[0] + 3.0 > 0.0)
                {
                    m_dblX1[0] = m_dblZ2[0];
                    m_dblX2[0] = (m_dblX1[0] + m_dblM[0])*0.5;
                    m_dblDl[0] = m_dblM[0] - m_dblX2[0];
                }
                else
                {
                    m_dblDl[0] = m_dblD[0];
                }
                m_dblD1[0] = f(m_dblX1[0], m_dblA[0], m_dblB[0], m_dblM[0]);
                m_dblLl[0] = m_dblX1[0]*(1.0 - m_dblX1[0])/(m_dblS[0]*
                		(m_dblM[0] - m_dblX1[0])); // z1 = x1 - ll
            }
            m_dblF2[0] = f(m_dblX2[0], m_dblA[0], m_dblB[0], m_dblM[0]);

            if (m_dblB[0] <= 1.0)
            {
                m_dblX4[0] = 1.0 - (m_dblD[0] = (1.0 - m_dblM[0])*0.5);
                m_dblX5[0] = m_dblZ4[0] = 1.0;
                m_dblF5[0] = m_dblLr[0] = 0.0;
            }
            else
            {
                m_dblX4[0] = m_dblM[0] + m_dblD[0];
                m_dblX5[0] = m_dblX4[0] + m_dblD[0];
                m_dblZ4[0] = m_dblX4[0]*(1.0 + (1.0 - m_dblX4[0])/(m_dblS[0]*m_dblD[0]));
                if (m_dblX5[0] >= 1.0 || (m_dblS[0] - 6.0)*m_dblX4[0] - m_dblA[0] + 3.0 < 0.0)
                {
                    m_dblX5[0] = m_dblZ4[0];
                    m_dblX4[0] = (m_dblM[0] + m_dblX5[0])*0.5;
                    m_dblD[0] = m_dblX4[0] - m_dblM[0];
                }
                m_dblF5[0] = f(m_dblX5[0], m_dblA[0], m_dblB[0], m_dblM[0]);
                m_dblLr[0] = m_dblX5[0]*(1.0 - m_dblX5[0])/(m_dblS[0]*(m_dblX5[0] - m_dblM[0])); // z5 = x5 + lr
            }
            m_dblF4[0] = f(m_dblX4[0], m_dblA[0], m_dblB[0], m_dblM[0]);

            m_dblP1[0] = m_dblF2[0]*(m_dblDl[0] + m_dblDl[0]); //  x1 < X < m
            m_dblP2[0] = m_dblF4[0]*(m_dblD[0] + m_dblD[0]) + m_dblP1[0]; //  m  < X < x5
            m_dblP3[0] = m_dblD1[0]*m_dblLl[0] + m_dblP2[0]; //       X < x1
            m_dblP4[0] = m_dblF5[0]*m_dblLr[0] + m_dblP3[0]; //  x5 < X
        }

        for (;;)
        {
            if ((U = m_rng.nextDouble()*m_dblP4[0]) <= m_dblP1[0])
            {
                // immediate accept:  x2 < X < m, - f(x2) < W < 0
                if ((W = U/m_dblDl[0] - m_dblF2[0]) <= 0.0)
                {
                    return (m_dblM[0] - U/m_dblF2[0]);
                }
                // immediate accept:  x1 < X < x2, 0 < W < f(x1)
                if (W <= m_dblD1[0])
                {
                    return (m_dblX2[0] - W/m_dblD1[0]*m_dblDl[0]);
                }
                // candidates for acceptance-rejection-test
                V = m_dblDl[0]*(U = m_rng.nextDouble());
                X = m_dblX2[0] - V;
                Y = m_dblX2[0] + V;
                // squeeze accept:    L(x) = f(x2) (x - z2) / (x2 - z2)
                if (W*(m_dblX2[0] - m_dblZ2[0]) <= m_dblF2[0]*(X - m_dblZ2[0]))
                {
                    return (X);
                }
                if ((V = m_dblF2[0] + m_dblF2[0] - W) < 1.0)
                {
                    // squeeze accept:    L(x) = f(x2) + (1 - f(x2))(x - x2)/(m - x2)
                    if (V <= m_dblF2[0] + (1.0 - m_dblF2[0])*U)
                    {
                        return (Y);
                    }
                    // quotient accept:   x2 < Y < m,   W >= 2f2 - f(Y)
                    if (V <= f(Y, m_dblA[0], m_dblB[0], m_dblM[0]))
                    {
                        return (Y);
                    }
                }
            }
            else if (U <= m_dblP2[0])
            {
                U -= m_dblP1[0];
                // immediate accept:  m < X < x4, - f(x4) < W < 0
                if ((W = U/m_dblD[0] - m_dblF4[0]) <= 0.0)
                {
                    return (m_dblM[0] + U/m_dblF4[0]);
                }
                // immediate accept:  x4 < X < x5, 0 < W < f(x5)
                if (W <= m_dblF5[0])
                {
                    return (m_dblX4[0] + W/m_dblF5[0]*m_dblD[0]);
                }
                // candidates for acceptance-rejection-test
                V = m_dblD[0]*(U = m_rng.nextDouble());
                X = m_dblX4[0] + V;
                Y = m_dblX4[0] - V;
                // squeeze accept:    L(x) = f(x4) (z4 - x) / (z4 - x4)
                if (W*(m_dblZ4[0] - m_dblX4[0]) <= m_dblF4[0]*(m_dblZ4[0] - X))
                {
                    return (X);
                }
                if ((V = m_dblF4[0] + m_dblF4[0] - W) < 1.0)
                {
                    // squeeze accept:    L(x) = f(x4) + (1 - f(x4))(x4 - x)/(x4 - m)
                    if (V <= m_dblF4[0] + (1.0 - m_dblF4[0])*U)
                    {
                        return (Y);
                    }
                    // quotient accept:   m < Y < x4,   W >= 2f4 - f(Y)
                    if (V <= f(Y, m_dblA[0], m_dblB[0], m_dblM[0]))
                    {
                        return (Y);
                    }
                }
            }
            else if (U <= m_dblP3[0])
            {
                // X < x1
                Y = Math.log(U = (U - m_dblP2[0])/(m_dblP3[0] - m_dblP2[0]));
                if ((X = m_dblX1[0] + m_dblLl[0]*Y) <= 0.0)
                {
                    continue; // X > 0!!
                }
                W = m_rng.nextDouble()*U;
                // squeeze accept:    L(x) = f(x1) (x - z1) / (x1 - z1)
                //                    z1 = x1 - ll,   W <= 1 + (X - x1)/ll
                if (W <= 1.0 + Y)
                {
                    return (X);
                }
                W *= m_dblD1[0];
            }
            else
            {
                // x5 < X
                Y = Math.log(U = (U - m_dblP3[0])/(m_dblP4[0] - m_dblP3[0]));
                if ((X = m_dblX5[0] - m_dblLr[0]*Y) >= 1.0)
                {
                    continue; // X < 1!!
                }
                W = m_rng.nextDouble()*U;
                // squeeze accept:    L(x) = f(x5) (z5 - x) / (z5 - x5)
                //                    z5 = x5 + lr,   W <= 1 + (x5 - X)/lr
                if (W <= 1.0 + Y)
                {
                    return (X);
                }
                W *= m_dblF5[0];
            }
            // density accept:  f(x) = (x/m)^a ((1 - x)/(1 - m))^b
            if (Math.log(W) <= m_dblA[0]*Math.log(X/m_dblM[0]) + m_dblB[0]*Math.log((1.0 - X)/(1.0 - m_dblM[0])))
            {
                return (X);
            }
        }
    }

    private double f(double x, double a, double b, double m)
    {
        return FastMath.exp(a*Math.log(x/m) + b*Math.log((1.0 - x)/(1.0 - m)));
    }
}
