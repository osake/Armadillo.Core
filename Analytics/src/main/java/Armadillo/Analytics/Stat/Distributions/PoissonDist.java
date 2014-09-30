package Armadillo.Analytics.Stat.Distributions;

import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;

import Armadillo.Analytics.Base.Arithmetic;
import Armadillo.Analytics.Base.FastMath;
import Armadillo.Analytics.SpecialFunctions.IncompleteGamaFunct;
import Armadillo.Analytics.Stat.Random.IRng;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.Logger;

public class PoissonDist //extends AbstractUnivDiscrDist
{

    private final static int INT_RND_SEED = 33;

    /// <summary>
    /// Own instance
    /// </summary>
    private final static PoissonDist m_ownInstance = new PoissonDist(
        1, new RngWrapper(INT_RND_SEED));

    private double m_dblLambda;

    private static final double m_dblMeanMax = Integer.MAX_VALUE;

    private static final double m_dblSwitchMean = 10.0; // switch from method A to method B
    IRng m_rng;
    
    public PoissonDist(
        double dblLambda,
        IRng rng)
         
    {
    	m_rng = rng;
    	//base(new RngWrapper(INT_RND_SEED))
        SetState(dblLambda);
    }

    public PoissonDist(
        double dblLambda,
        RngWrapper rng) //: base(rng)
    {
        SetState(dblLambda);
    }


    public double Lambda()
    {
        return m_dblLambda;
    }

    private void SetState(
        double dblLambda)
    {
        m_dblLambda = dblLambda;
    }


    /**
    * Returns the probability distribution function.
    */

    public double Pdf(int k)
    {
        return FastMath.exp(k*Math.log(Lambda()) -
                        Arithmetic.LogFactorial(k) - Lambda());
    }


    /**
     * Returns the sum of the first <tt>k</tt> terms of the Poisson distribution.
     * <pre>
     *   k         j
     *   --   -m  m
     *   >   e    --
     *   --       j!
     *  j=0
     * </pre>
     * The terms are not summed directly; instead the incomplete
     * gamma integral is employed, according to the relation
     * <p>
     * <tt>y = poisson( k, m ) = Gamma.incompleteGammaComplement( k+1, m )</tt>.
     *
     * The arguments must both be positive.
     *
     * @param k number of terms.
     * @param mean the mean of the poisson distribution.
     */

    public double Cdf(int k)
    {
        if (Lambda() < 0)
        {
            try {
				throw new Exception();
			} catch (Exception e) {
				Logger.log(e);
			}
        }
        if (k < 0)
        {
            return 0.0;
        }
        return IncompleteGamaFunct.IncompleteGammaComplement((k + 1), Lambda());
    }

    /**
     * Returns the sum of the terms <tt>k+1</tt> to <tt>Infinity</tt> of the Poisson distribution.
     * <pre>
     *  inf.       j
     *   --   -m  m
     *   >   e    --
     *   --       j!
     *  j=k+1
     * </pre>
     * The terms are not summed directly; instead the incomplete
     * gamma integral is employed, according to the formula
     * <p>
     * <tt>y = poissonComplemented( k, m ) = Gamma.incompleteGamma( k+1, m )</tt>.
     *
     * The arguments must both be positive.
     *
     * @param k start term.
     * @param mean the mean of the poisson distribution.
     */

    public double CdfComplemented(int k)
    {
        if (Lambda() < 0)
        {
            try {
				throw new Exception();
			} catch (Exception e) {
				Logger.log(e);
			}
        }
        if (k < -1)
        {
            return 0.0;
        }
        return IncompleteGamaFunct.IncompleteGamma((k + 1), Lambda());
    }


    /**
     *  Insert the method's description here. Creation date: (1/26/00 12:47:13 PM)
     *
     *@param  lambda  double
     *@return         int
     *@author:        <Vadum Kutsyy, kutsyy@hotmail.com>
     */

    public int NextInt2()
    {
        // to do: check performance
        double a = FastMath.exp(-Lambda());
        double b = 1;
        int i = 0;
        do
        {
            b *= m_rng.nextDouble();
            if (b < a)
            {
                return i;
            }
            i++;
        }
        while (true);
    }


    /**
     * Returns a random number from the distribution; bypasses the internal state.
     */

    public static int NextInt(
    		double dblLambda,
    		IRng rng)
    {
    	if(dblLambda == 0){
    		return 0;
    	}
    	
        /******************************************************************
         *                                                                *
         * Poisson Distribution - Patchwork Rejection/Inversion           *
         *                                                                *
         ******************************************************************
         *                                                                *
         * For parameter  my < 10  Tabulated Inversion is applied.        *
         * For my >= 10  Patchwork Rejection is employed:                 *
         * The area below the histogram function f(x) is rearranged in    *
         * its body by certain point reflections. Within a large center   *
         * interval variates are sampled efficiently by rejection from    *
         * uniform hats. Rectangular immediate acceptance regions speed   *
         * up the generation. The remaining tails are covered by          *
         * exponential functions.                                         *
         *                                                                *
         *****************************************************************/
        double my = dblLambda;
        double[] m_intPpArr = new double[36];
        double m_dblP = 0;
        int m_intM;
        int m_intLlll = 0;
        double m_dblQ = 0;
        double m_dblP0 = 0;
        int m_intK1 = 0;
        int m_intK2 = 0;
        double m_dblCPm = 0;
        double m_dblDl = 0;
        double m_dblDr = 0;
        double m_dblF1 = 0;
        double m_dblF2 = 0;
        double m_dblF4 = 0;
        double m_dblF5 = 0;
        double m_dblLl = 0;
        double m_dblLMy = 0;
        double m_dblLr = 0;
        double m_dblP1 = 0;
        double m_dblP2 = 0;
        double m_dblP3 = 0;
        double m_dblP4 = 0;
        double m_dblP5 = 0;
        double m_dblP6 = 0;
        double m_dblR1 = 0;
        double m_dblR2 = 0;
        double m_dblR4 = 0;
        double m_dblR5 = 0;
        double m_dblMyOld =  0;
        int m_intK4 = 0;
        int m_intK5 = 0;
        double m_dblMyLast = -1.0;
        
        //double t, g, my_k;

        //double gx, gy, px, py, e, x, xx, delta, v;
        //int sign;

        //static double p,q,p0,pp[36];
        //static long ll,m;
        double u;
        int k, i;

        if (my < m_dblSwitchMean)
        {
            // CASE B: Inversion- start new table and calculate p0
            if (my != m_dblMyOld)
            {
                m_dblMyOld = my;
                m_intLlll = 0;
                m_dblP = FastMath.exp(-my);
                m_dblQ = m_dblP;
                m_dblP0 = m_dblP;
                //for (k=pp.length; --k >=0; ) pp[k] = 0;
            }
            m_intM = (my > 1.0) ? (int) my : 1;
            for (;;)
            {
                u = rng.nextDouble(); // Step U. Uniform sample
                k = 0;
                if (u <= m_dblP0)
                {
                    return (k);
                }
                if (m_intLlll != 0)
                {
                    // Step T. Table comparison
                    i = (u > 0.458) ? Math.min(m_intLlll, m_intM) : 1;
                    for (k = i; k <= m_intLlll; k++)
                    {
                        if (u <= m_intPpArr[k])
                        {
                            return (k);
                        }
                    }
                    if (m_intLlll == 35)
                    {
                        continue;
                    }
                }
                for (k = m_intLlll + 1; k <= 35; k++)
                {
                    // Step C. Creation of new prob.
                    m_dblP *= my/k;
                    m_dblQ += m_dblP;
                    m_intPpArr[k] = m_dblQ;
                    if (u <= m_dblQ)
                    {
                        m_intLlll = k;
                        return (k);
                    }
                }
                m_intLlll = 35;
            }
        } // end my < SWITCH_MEAN
        else if (my < m_dblMeanMax)
        {
            // CASE A: acceptance complement
            //static double        my_last = -1.0;
            //static long int      m,  k2, k4, k1, k5;
            //static double        dl, dr, r1, r2, r4, r5, ll, lr, l_my, c_pm,
            //  					 f1, f2, f4, f5, p1, p2, p3, p4, p5, p6;
            int Dk, X, Y;
            double Ds, U, V, W;

            m_intM = (int) my;
            if (my != m_dblMyLast)
            {
                //  set-up
                m_dblMyLast = my;

                // approximate deviation of reflection points k2, k4 from my - 1/2
                Ds = Math.sqrt(my + 0.25);

                // mode m, reflection points k2 and k4, and points k1 and k5, which
                // delimit the centre region of h(x)
                m_intK2 = (int) Math.ceil(my - 0.5 - Ds);
                m_intK4 = (int) (my - 0.5 + Ds);
                m_intK1 = m_intK2 + m_intK2 - m_intM + 1;
                m_intK5 = m_intK4 + m_intK4 - m_intM;

                // range width of the critical left and right centre region
                m_dblDl = (m_intK2 - m_intK1);
                m_dblDr = (m_intK5 - m_intK4);

                // recurrence constants r(k) = p(k)/p(k-1) at k = k1, k2, k4+1, k5+1
                m_dblR1 = my/m_intK1;
                m_dblR2 = my/m_intK2;
                m_dblR4 = my/(m_intK4 + 1);
                m_dblR5 = my/(m_intK5 + 1);

                // reciprocal values of the scale parameters of expon. tail envelopes
                m_dblLl = Math.log(m_dblR1); // expon. tail left
                m_dblLr = -Math.log(m_dblR5); // expon. tail right

                // Poisson constants, necessary for computing function values f(k)
                m_dblLMy = Math.log(my);
                m_dblCPm = m_intM*m_dblLMy - Arithmetic.LogFactorial(m_intM);

                // function values f(k) = p(k)/p(m) at k = k2, k4, k1, k5
                m_dblF2 = f(m_intK2, m_dblLMy, m_dblCPm);
                m_dblF4 = f(m_intK4, m_dblLMy, m_dblCPm);
                m_dblF1 = f(m_intK1, m_dblLMy, m_dblCPm);
                m_dblF5 = f(m_intK5, m_dblLMy, m_dblCPm);

                // area of the two centre and the two exponential tail regions
                // area of the two immediate acceptance regions between k2, k4
                m_dblP1 = m_dblF2*(m_dblDl + 1.0); // immed. left
                m_dblP2 = m_dblF2*m_dblDl + m_dblP1; // centre left
                m_dblP3 = m_dblF4*(m_dblDr + 1.0) + m_dblP2; // immed. right
                m_dblP4 = m_dblF4*m_dblDr + m_dblP3; // centre right
                m_dblP5 = m_dblF1/m_dblLl + m_dblP4; // expon. tail left
                m_dblP6 = m_dblF5/m_dblLr + m_dblP5; // expon. tail right
            } // end set-up

            for (;;)
            {
                // generate uniform number U -- U(0, p6)
                // case distinction corresponding to U
                if ((U = rng.nextDouble()*m_dblP6) < m_dblP2)
                {
                    // centre left

                    // immediate acceptance region R2 = [k2, m) *[0, f2),  X = k2, ... m -1
                    if ((V = U - m_dblP1) < 0.0)
                    {
                        return (m_intK2 + (int) (U/m_dblF2));
                    }
                    // immediate acceptance region R1 = [k1, k2)*[0, f1),  X = k1, ... k2-1
                    if ((W = V/m_dblDl) < m_dblF1)
                    {
                        return (m_intK1 + (int) (V/m_dblF1));
                    }

                    // computation of candidate X < k2, and its counterpart Y > k2
                    // either squeeze-acceptance of X or acceptance-rejection of Y
                    Dk = (int) (m_dblDl*rng.nextDouble()) + 1;
                    if (W <= m_dblF2 - Dk*(m_dblF2 - m_dblF2/m_dblR2))
                    {
                        // quick accept of
                        return (m_intK2 - Dk); // X = k2 - Dk
                    }
                    if ((V = m_dblF2 + m_dblF2 - W) < 1.0)
                    {
                        // quick reject of Y
                        Y = m_intK2 + Dk;
                        if (V <= m_dblF2 + Dk*(1.0 - m_dblF2)/(m_dblDl + 1.0))
                        {
//quick accept of
                            return (Y); // Y = k2 + Dk
                        }
                        if (V <= f(Y, m_dblLMy, m_dblCPm))
                        {
                            return (Y); // accept of Y
                        }
                    }
                    X = m_intK2 - Dk;
                }
                else if (U < m_dblP4)
                {
                    // centre right
                    // immediate acceptance region R3 = [m, k4+1)*[0, f4), X = m, ... k4
                    if ((V = U - m_dblP3) < 0.0)
                    {
                        return (m_intK4 - (int) ((U - m_dblP2)/m_dblF4));
                    }
                    // immediate acceptance region R4 = [k4+1, k5+1)*[0, f5)
                    if ((W = V/m_dblDr) < m_dblF5)
                    {
                        return (m_intK5 - (int) (V/m_dblF5));
                    }

                    // computation of candidate X > k4, and its counterpart Y < k4
                    // either squeeze-acceptance of X or acceptance-rejection of Y
                    Dk = (int) (m_dblDr*rng.nextDouble()) + 1;
                    if (W <= m_dblF4 - Dk*(m_dblF4 - m_dblF4*m_dblR4))
                    {
                        // quick accept of
                        return (m_intK4 + Dk); // X = k4 + Dk
                    }
                    if ((V = m_dblF4 + m_dblF4 - W) < 1.0)
                    {
                        // quick reject of Y
                        Y = m_intK4 - Dk;
                        if (V <= m_dblF4 + Dk*(1.0 - m_dblF4)/m_dblDr)
                        {
                            // quick accept of
                            return (Y); // Y = k4 - Dk
                        }
                        if (V <= f(Y, m_dblLMy, m_dblCPm))
                        {
                            return (Y); // accept of Y
                        }
                    }
                    X = m_intK4 + Dk;
                }
                else
                {
                    W = rng.nextDouble();
                    if (U < m_dblP5)
                    {
                        // expon. tail left
                        Dk = (int) (1.0 - Math.log(W)/m_dblLl);
                        if ((X = m_intK1 - Dk) < 0)
                        {
                            continue; // 0 <= X <= k1 - 1
                        }
                        W *= (U - m_dblP4)*m_dblLl; // W -- U(0, h(x))
                        if (W <= m_dblF1 - Dk*(m_dblF1 - m_dblF1/m_dblR1))
                        {
                            return (X); // quick accept of X
                        }
                    }
                    else
                    {
                        // expon. tail right
                        Dk = (int) (1.0 - Math.log(W)/m_dblLr);
                        X = m_intK5 + Dk; // X >= k5 + 1
                        W *= (U - m_dblP5)*m_dblLr; // W -- U(0, h(x))
                        if (W <= m_dblF5 - Dk*(m_dblF5 - m_dblF5*m_dblR5))
                        {
                            return (X); // quick accept of X
                        }
                    }
                }

                // acceptance-rejection test of candidate X from the original area
                // test, whether  W <= f(k),    with  W = U*h(x)  and  U -- U(0, 1)
                // log f(X) = (X - m)*Log(my) - log X! + log m!
                if (Math.log(W) <= X*m_dblLMy - Arithmetic.LogFactorial(X) - m_dblCPm)
                {
                    return (X);
                }
            }
        }
        else
        {
            // mean is too large
            return (int) my;
        }
    }

    /**
     * Returns a string representation of the receiver.
     */

    public String ToString()
    {
        return "Poisson(" + Lambda() + ")";
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is positive infinity,
     * regardless of the parameter values. There is no integer infinity,
     * so this method returns {@code Integer.MAX_VALUE}.
     *
     * @return upper bound of the support (always {@code Integer.MAX_VALUE} for
     * positive infinity)
     */
    public int getSupportUpperBound() {
        return Integer.MAX_VALUE;
    }
    
    /**
     * Computes the cumulative probability function and checks for {@code NaN}
     * values returned. Throws {@code MathInternalError} if the value is
     * {@code NaN}. Rethrows any exception encountered evaluating the cumulative
     * probability function. Throws {@code MathInternalError} if the cumulative
     * probability function returns {@code NaN}.
     *
     * @param argument input value
     * @return the cumulative probability
     * @throws MathInternalError if the cumulative probability is {@code NaN}
     */
    private double checkedCumulativeProbability(int argument)
        throws MathInternalError {
        double result = Double.NaN;
        
        result = Cdf(argument);
        if (Double.isNaN(result)) {
            throw new MathInternalError(LocalizedFormats
                    .DISCRETE_CUMULATIVE_PROBABILITY_RETURNED_NAN, argument);
        }
        return result;
    }
    
    public int CdfInv(double p)
    {
        if (p < 0.0 || p > 1.0) {
            throw new OutOfRangeException(p, 0, 1);
        }

        int lower = getSupportLowerBound();
        if (p == 0.0) {
            return lower;
        }
        if (lower == Integer.MIN_VALUE) {
            if (checkedCumulativeProbability(lower) >= p) {
                return lower;
            }
        } else {
            lower -= 1; // this ensures cumulativeProbability(lower) < p, which
                        // is important for the solving step
        }

        int upper = getSupportUpperBound();
        if (p == 1.0) {
            return upper;
        }

        // use the one-sided Chebyshev inequality to narrow the bracket
        // cf. AbstractRealDistribution.inverseCumulativeProbability(double)
        final double mu = getNumericalMean();
        final double sigma = FastMath.sqrt(getNumericalVariance());
        final boolean chebyshevApplies = !(Double.isInfinite(mu) || Double.isNaN(mu) ||
                Double.isInfinite(sigma) || Double.isNaN(sigma) || sigma == 0.0);
        if (chebyshevApplies) {
            double k = FastMath.sqrt((1.0 - p) / p);
            double tmp = mu - k * sigma;
            if (tmp > lower) {
                lower = ((int) Math.ceil(tmp)) - 1;
            }
            k = 1.0 / k;
            tmp = mu + k * sigma;
            if (tmp < upper) {
                upper = ((int) Math.ceil(tmp)) - 1;
            }
        }

        return solveInverseCumulativeProbability(p, lower, upper);
    }
    
	    /**
	    * This is a utility function used by {@link
        * #inverseCumulativeProbability(double)}. It assumes {@code 0 < p < 1} and
        * that the inverse cumulative probability lies in the bracket {@code
        * (lower, upper]}. The implementation does simple bisection to find the
        * smallest {@code p}-quantile <code>inf{x in Z | P(X<=x) >= p}</code>.
        *
        * @param p the cumulative probability
        * @param lower a value satisfying {@code cumulativeProbability(lower) < p}
        * @param upper a value satisfying {@code p <= cumulativeProbability(upper)}
        * @return the smallest {@code p}-quantile of this distribution
        */
       protected int solveInverseCumulativeProbability(final double p, int lower, int upper) {
           while (lower + 1 < upper) {
               int xm = (lower + upper) / 2;
               if (xm < lower || xm > upper) {
                   /*
                    * Overflow.
                    * There will never be an overflow in both calculation methods
                    * for xm at the same time
                    */
                   xm = lower + (upper - lower) / 2;
               }

               double pm = checkedCumulativeProbability(xm);
               if (pm >= p) {
                   upper = xm;
               } else {
                   lower = xm;
               }
           }
           return upper;
       }
        
    /**
     * {@inheritDoc}
     *
     * For mean parameter {@code p}, the mean is {@code p}.
     */
    public double getNumericalMean() {
    	
        return getMean();
    }

    private double getMean() {
		return m_dblLambda;
	}

	/**
     * {@inheritDoc}
     *
     * For mean parameter {@code p}, the variance is {@code p}.
     */
    public double getNumericalVariance() {
        return getMean();
    }
    
    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is always 0 no matter the mean parameter.
     *
     * @return lower bound of the support (always 0)
     */
    public int getSupportLowerBound() {
        return 0;
    }
    
    private static double f(int k, double l_nu, double c_pm)
    {
        return FastMath.exp(k*l_nu - Arithmetic.LogFactorial(k) - c_pm);
    }

    public static double PdfStatic(
        double dblLambda,
        int intX)
    {
        m_ownInstance.SetState(
            dblLambda);

        return m_ownInstance.Pdf(intX);
    }

    public static double CdfStatic(
        double dblLambda,
        int intX)
    {
        m_ownInstance.SetState(
            dblLambda);

        return m_ownInstance.Cdf(intX);
    }

    public static double CdfInvStatic(
        double dblLambda,
        double dblProbability)
    {
        m_ownInstance.SetState(
            dblLambda);

        try {
			return m_ownInstance.CdfInv(dblProbability);
		} catch (Exception e) {
			Logger.log(e);
		}
        return Double.NaN;
    }
}
