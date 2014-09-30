package Armadillo.Analytics.Stat.Distributions;

import org.apache.commons.math3.exception.MathInternalError;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.util.FastMath;
import Armadillo.Analytics.Base.Arithmetic;
import Armadillo.Analytics.SpecialFunctions.IncompleteBetaFunct;
import Armadillo.Analytics.Stat.Random.IRng;
import Armadillo.Core.Logger;

public class BinomialDist
{

//    /// <summary>
//    /// Own instance
//    /// </summary>
//    private static final  BinomialDist m_ownInstance = new BinomialDist(
//        1, 1, new RngWrapper(INT_RND_SEED));
//


    public int m_intN;
	private double m_dblLogP;
	private double m_dblLogQ;
	private double m_dblP;
	private double m_dblLogN;
	private IRng m_rng;

    /**
* Constructs a binomial distribution.
* Example: n=1, p=0.5.
* @param n the number of trials (also known as <i>sample size</i>).
* @param p the probability of success.
* @param randomGenerator a uniform random number generator.
* @throws ArgumentException if <tt>n*Math.Min(p,1-p) &lt;= 0.0</tt>
*/

    public BinomialDist(
        int intN,
        double dblP,
        IRng rng)
        //: base(rng)
    {
    	m_rng = rng;
        SetState(
            dblP,
            intN);
    }

    public double P()
    {
        return m_dblP;
    }

    private void SetState(
        double dblP,
        int intN)
    {
        m_dblP = dblP;
        m_intN = intN;

        /**
         * Sets the parameters number of trials and the probability of success.
         * @param n the number of trials
         * @param p the probability of success.
         * @throws ArgumentException if <tt>n*Math.Min(p,1-p) &lt;= 0.0</tt>
         */

        if (m_intN*Math.min(m_dblP, 1 - m_dblP) <= 0.0)
        {
            try {
				throw new Exception();
			} catch (Exception e) {
				Logger.log(e);
			}
        }
        m_intN = intN;
        m_dblP = dblP;

        m_dblLogP = Math.log(m_dblP);
        m_dblLogQ = Math.log(1.0 - m_dblP);
        m_dblLogN = Arithmetic.LogFactorial(m_intN);
    }

    /**
     * Returns the sum of the terms <tt>0</tt> through <tt>k</tt> of the Binomial
     * probability density.
     * <pre>
     *   k
     *   --  ( n )   j      n-j
     *   >   (   )  p  (1-p)
     *   --  ( j )
     *  j=0
     * </pre>
     * The terms are not summed directly; instead the incomplete
     * beta integral is employed, according to the formula
     * <p>
     * <tt>y = binomial( k, n, p ) = Gamma.incompleteBeta( n-k, k+1, 1-p )</tt>.
     * <p>
     * All arguments must be positive,
     * @param k end term.
     * @param n the number of trials.
     * @param p the probability of success (must be in <tt>(0.0,1.0)</tt>).
     */

    public double Cdf(int k)
    {
    	try{
	        if ((m_dblP < 0.0) || (m_dblP > 1.0))
	        {
	            throw new Exception();
	        }
	        if ((k < 0) || (m_intN < k))
	        {
	            throw new Exception();
	        }
	
	        if (k == m_intN)
	        {
	            return (1.0);
	        }
	        if (k == 0)
	        {
	            return Math.pow(1.0 - m_dblP, m_intN - k);
	        }
	
	        return IncompleteBetaFunct.IncompleteBeta(m_intN - k, k + 1, 1.0 - m_dblP);
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return Double.NaN;
    }

    /**
     * Returns the probability distribution function.
     */

    public double Pdf(int k)
    {
        if (k < 0)
        {
            try {
				throw new Exception();
			} catch (Exception e) {
				Logger.log(e);
			}
        }
        int r = m_intN - k;
        return Math.exp(
            m_dblLogN -
            Arithmetic.LogFactorial(k) -
            Arithmetic.LogFactorial(r) +
            m_dblLogP*k + m_dblLogQ*r);
    }

    /**
     * Returns the sum of the terms <tt>k+1</tt> through <tt>n</tt> of the Binomial
     * probability density.
     * <pre>
     *   n
     *   --  ( n )   j      n-j
     *   >   (   )  p  (1-p)
     *   --  ( j )
     *  j=k+1
     * </pre>
     * The terms are not summed directly; instead the incomplete
     * beta integral is employed, according to the formula
     * <p>
     * <tt>y = binomialComplemented( k, n, p ) = Gamma.incompleteBeta( k+1, n-k, p )</tt>.
     * <p>
     * All arguments must be positive,
     * @param k end term.
     * @param n the number of trials.
     * @param p the probability of success (must be in <tt>(0.0,1.0)</tt>).
     */

    public double CdfCompl(int k)
    {
    	try{
        if ((m_dblP < 0.0) || (m_dblP > 1.0))
        {
            throw new Exception();
        }
        if ((k < 0) || (m_intN < k))
        {
            throw new Exception();
        }

        if (k == m_intN)
        {
            return (0.0);
        }
        if (k == 0)
        {
            return 1.0 - Math.pow(1.0 - m_dblP, m_intN - k);
        }

        return IncompleteBetaFunct.IncompleteBeta(k + 1, m_intN - k, m_dblP);
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return Double.NaN;
    }

    /**
     *  Binomial(p)
     *
     *@param  p   p
     *@return     Binomial(p)
     *@author:    <Vadum Kutsyy, kutsyy@hotmail.com>
     */

    public int NextInt2()
    {
        return m_rng.nextDouble() < m_dblP ? 0 : 1;
    }


    /******************************************************************
     *                                                                *
     *     Binomial-Distribution - Acceptance Rejection/Inversion     *
     *                                                                *
     ******************************************************************
     *                                                                *
     * Acceptance Rejection method combined with Inversion for        *
     * generating Binomial random numbers with parameters             *
     * n (number of trials) and p (probability of success).           *
     * For  Min(n*p,n*(1-p)) < 10  the Inversion method is applied:   *
     * The random numbers are generated via sequential search,        *
     * starting at the lowest index k=0. The cumulative probabilities *
     * are avoided by using the technique of chop-down.               *
     * For  Min(n*p,n*(1-p)) >= 10  Acceptance Rejection is used:     *
     * The algorithm is based on a hat-function which is uniform in   *
     * the centre region and exponential in the tails.                *
     * A triangular immediate acceptance region in the centre speeds  *
     * up the generation of binomial variates.                        *
     * If candidate k is near the mode, f(k) is computed recursively  *
     * starting at the mode m.                                        *
     * The acceptance test by Stirling's formula is modified          *
     * according to W. Hoermann (1992): The generation of binomial    *
     * random variates, to appear in J. Statist. Comput. Simul.       *
     * If  p < .5  the algorithm is applied to parameters n, p.       *
     * Otherwise p is replaced by 1-p, and k is replaced by n - k.    *
     *                                                                *
     ******************************************************************
     *                                                                *
     * FUNCTION:    - samples a random number from the binomial       *
     *                distribution with parameters n and p  and is    *
     *                valid for  n*Min(p,1-p)  >  0.                  *
     * REFERENCE:   - V. Kachitvichyanukul, B.W. Schmeiser (1988):    *
     *                Binomial random variate generation,             *
     *                Communications of the ACM 31, 216-222.          *
     * SUBPROGRAMS: - StirlingCorrection()                            *
     *                            ... Correction term of the Stirling *
     *                                approximation for Log(k!)       *
     *                                (series in 1/k or table values  *
     *                                for small k) with long int k    *
     *              - randomGenerator    ... (0,1)-Uniform engine     *
     *                                                                *
     ******************************************************************/

    public static int NextInt(IRng m_rng,
    		double m_dblP,
    		int m_intN)
    {
        double m_dblC = 0;
        double m_dblCh = 0;
        double m_dblLl = 0;
        double m_dblLr = 0;
        double m_dblNp = 0;
        double m_dblP0 = 0;
        double m_dblP1 = 0;
        double m_dblP2 = 0;
        double m_dblP3 = 0;
        double m_dblP4 = 0;
        double m_dblPar = 0;
        double m_dblPLast = -1.0;
        double m_dblPPrev = -1.0;
        double m_dblPq = 0;
        double m_dblQ = 0;
        double m_dblRc = 0;
        double m_dblSs = 0;
        double m_dblXl = 0;
        double m_dblXm = 0;
        double m_dblXr = 0;
        int m_intB = 0;
        int m_intM = 0;
        int m_intNLast = -1;
        int m_intNm = 0;
        int m_intNPrev = -1;
        
        double C1_3 = 0.33333333333333333;
        double C5_8 = 0.62500000000000000;
        double C1_6 = 0.16666666666666667;
        int DMAX_KM = 20;

        int bh, i, K, Km, nK;
        double f, rm, U, V, X, T, E;

        if (m_intN != m_intNLast || m_dblP != m_dblPLast)
        {
            // set-up
            m_intNLast = m_intN;
            m_dblPLast = m_dblP;
            m_dblPar = Math.min(m_dblP, 1.0 - m_dblP);
            m_dblQ = 1.0 - m_dblPar;
            m_dblNp = m_intN*m_dblPar;

            // Check for invalid input values

            if (m_dblNp <= 0.0)
            {
                return -1;
            }

            rm = m_dblNp + m_dblPar;
            m_intM = (int) rm; // mode, integer
            if (m_dblNp < 10)
            {
                m_dblP0 = Math.exp(m_intN*Math.log(m_dblQ)); // Chop-down
                bh = (int) (m_dblNp + 10.0*Math.sqrt(m_dblNp*m_dblQ));
                m_intB = Math.min(m_intN, bh);
            }
            else
            {
                m_dblRc = (m_intN + 1.0)*(m_dblPq = m_dblPar/m_dblQ); // recurr. relat.
                m_dblSs = m_dblNp*m_dblQ; // variance
                i = (int) (2.195*Math.sqrt(m_dblSs) - 4.6*m_dblQ); // i = p1 - 0.5
                m_dblXm = m_intM + 0.5;
                m_dblXl = (m_intM - i); // limit left
                m_dblXr = (m_intM + i + 1L); // limit right
                f = (rm - m_dblXl)/(rm - m_dblXl*m_dblPar);
                m_dblLl = f*(1.0 + 0.5*f);
                f = (m_dblXr - rm)/(m_dblXr*m_dblQ);
                m_dblLr = f*(1.0 + 0.5*f);
                m_dblC = 0.134 + 20.5/(15.3 + m_intM); // parallelogram
                // height
                m_dblP1 = i + 0.5;
                m_dblP2 = m_dblP1*(1.0 + m_dblC + m_dblC); // probabilities
                m_dblP3 = m_dblP2 + m_dblC/m_dblLl; // of regions 1-4
                m_dblP4 = m_dblP3 + m_dblC/m_dblLr;
            }
        }

        if (m_dblNp < 10)
        {
            //Inversion Chop-down
            double pk;

            K = 0;
            pk = m_dblP0;
            U = m_rng.nextDouble();
            while (U > pk)
            {
                ++K;
                if (K > m_intB)
                {
                    U = m_rng.nextDouble();
                    K = 0;
                    pk = m_dblP0;
                }
                else
                {
                    U -= pk;
                    pk = (((m_intN - K + 1)*m_dblPar*pk)/(K*m_dblQ));
                }
            }
            return ((m_dblP > 0.5) ? (m_intN - K) : K);
        }

        for (;;)
        {
            V = m_rng.nextDouble();
            if ((U = m_rng.nextDouble()*m_dblP4) <= m_dblP1)
            {
                // triangular region
                K = (int) (m_dblXm - U + m_dblP1*V);
                return (m_dblP > 0.5) ? (m_intN - K) : K; // immediate accept
            }
            if (U <= m_dblP2)
            {
                // parallelogram
                X = m_dblXl + (U - m_dblP1)/m_dblC;
                if ((V = V*m_dblC + 1.0 - Math.abs(m_dblXm - X)/m_dblP1) >= 1.0)
                {
                    continue;
                }
                K = (int) X;
            }
            else if (U <= m_dblP3)
            {
                // left tail
                if ((X = m_dblXl + Math.log(V)/m_dblLl) < 0.0)
                {
                    continue;
                }
                K = (int) X;
                V *= (U - m_dblP2)*m_dblLl;
            }
            else
            {
                // right tail
                if ((K = (int) (m_dblXr - Math.log(V)/m_dblLr)) > m_intN)
                {
                    continue;
                }
                V *= (U - m_dblP3)*m_dblLr;
            }

            // acceptance test :  two cases, depending on |K - m|
            if ((Km = Math.abs(K - m_intM)) <= DMAX_KM || Km + Km + 2L >= m_dblSs)
            {
                // computation of p(K) via recurrence relationship from the mode
                f = 1.0; // f(m)
                if (m_intM < K)
                {
                    for (i = m_intM; i < K;)
                    {
                        if ((f *= (m_dblRc/++i - m_dblPq)) < V)
                        {
                            break; // multiply  f
                        }
                    }
                }
                else
                {
                    for (i = K; i < m_intM;)
                    {
                        if ((V *= (m_dblRc/++i - m_dblPq)) > f)
                        {
                            break; // multiply  V
                        }
                    }
                }
                if (V <= f)
                {
                    break; // acceptance test
                }
            }
            else
            {
                // lower and upper squeeze tests, based on lower bounds for log p(K)
                V = Math.log(V);
                T = -Km*Km/(m_dblSs + m_dblSs);
                E = (Km/m_dblSs)*((Km*(Km*C1_3 + C5_8) + C1_6)/m_dblSs + 0.5);
                if (V <= T - E)
                {
                    break;
                }
                if (V <= T + E)
                {
                    if (m_intN != m_intNPrev || m_dblPar != m_dblPPrev)
                    {
                        m_intNPrev = m_intN;
                        m_dblPPrev = m_dblPar;

                        m_intNm = m_intN - m_intM + 1;
                        m_dblCh = m_dblXm*Math.log((m_intM + 1.0)/(m_dblPq*m_intNm)) +
                                  Arithmetic.stirlingCorrection(m_intM + 1) + Arithmetic.stirlingCorrection(m_intNm);
                    }
                    nK = m_intN - K + 1;

                    // computation of log f(K) via Stirling's formula
                    // acceptance-rejection test
                    if (V <= m_dblCh + (m_intN + 1.0)*Math.log(m_intNm/(double) nK) +
                             (K + 0.5)*Math.log(nK*m_dblPq/(K + 1.0)) -
                             Arithmetic.stirlingCorrection(K + 1) - Arithmetic.stirlingCorrection(nK))
                    {
                        break;
                    }
                }
            }
        }
        return (m_dblP > 0.5) ? (m_intN - K) : K;
    }


    /**
     * Returns a string representation of the receiver.
     */

    public String ToString()
    {
        return "BinomialDist(" + m_intN + "," + m_dblP + ")";
    }
    
    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is always 0 except for the probability
     * parameter {@code p = 1}.
     *
     * @return lower bound of the support (0 or the number of trials)
     */
    public int getSupportLowerBound() {
        return m_dblP < 1.0 ? 0 : m_intN;
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is the number of trials except for the
     * probability parameter {@code p = 0}.
     *
     * @return upper bound of the support (number of trials or 0)
     */
    public int getSupportUpperBound() {
        return m_dblP > 0.0 ? m_intN : 0;
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
     * For {@code n} trials and probability parameter {@code p}, the mean is
     * {@code n * p}.
     */
    public double getNumericalMean() {
    	return m_intN * m_dblLogP;
    }

    /**
     * {@inheritDoc}
     *
     * For {@code n} trials and probability parameter {@code p}, the variance is
     * {@code n * p * (1 - p)}.
     */
    public double getNumericalVariance() {
        final double p = m_dblLogP;
        return m_intN * p * (1 - p);
    }
}
