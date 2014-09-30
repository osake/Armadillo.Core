package Armadillo.Analytics.Stat.Distributions;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.UnivariateSolverUtils;
import org.apache.commons.math3.exception.OutOfRangeException;
import Armadillo.Analytics.Base.FastMath;
import Armadillo.Analytics.SpecialFunctions.IncompleteGamaFunct;
import Armadillo.Analytics.SpecialFunctions.LogGammaFunct;
import Armadillo.Analytics.Stat.Random.IRng;
import Armadillo.Core.Logger;

public class GammaDist extends AUnivContDist
{
    /** Default accuracy. */
    public static final double SOLVER_DEFAULT_ABSOLUTE_ACCURACY = 1e-6;
    /** Solver absolute accuracy for inverse cumulative computation */
    private double solverAbsoluteAccuracy = SOLVER_DEFAULT_ABSOLUTE_ACCURACY;

    private double m_dblAlpha;
    private double m_dblBeta;// shape parameter



    public GammaDist(
        double dblAlpha,
        double dblBeta,
        IRng rng)
        
    {
    	super(rng);
        SetState(
            dblAlpha,
            dblBeta);
    }

    public double getAlpha(){
    	return m_dblAlpha;
    }
    
    public void setAlpha(double value)
    {
            m_dblAlpha = value;
            SetState(
                m_dblAlpha,
                m_dblBeta);
    }

    public double getBeta()
    {
        return m_dblBeta;
    }
    
    public void setBeta(double value)
        {
            m_dblBeta = value;
            SetState(
                m_dblAlpha,
                m_dblBeta);
        }

    private void SetState(
        double dblAlpha,
        double dblBeta)
    {
    	try{
	        if (dblAlpha <= 0.0)
	        {
	            throw new Exception();
	        }
	        if (dblBeta <= 0.0)
	        {
	            throw new Exception();
	        }
	
	        m_dblAlpha = dblAlpha;
	        m_dblBeta = dblBeta;
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    }

    /**
     * Returns the integral from zero to <tt>x</tt> of the gamma probability
     * density function.
     * <pre>
     *                x
     *        b       -
     *       a       | |   b-1  -at
     * y =  -----    |    t    e    dt
     *       -     | |
     *      | (b)   -
     *               0
     * </pre>
     * The incomplete gamma integral is used, according to the
     * relation
     *
     * <tt>y = Gamma.incompleteGamma( b, a*x )</tt>.
     *
     * @param a the paramater a (alpha) of the gamma distribution.
     * @param b the paramater b (beta, lambda) of the gamma distribution.
     * @param x integration end point.
     */

    public double Cdf(
        double x)
    {
        if (x < 0.0)
        {
            return 0.0;
        }
        return IncompleteGamaFunct.IncompleteGamma(getBeta(), getAlpha()*x);
    }

    /**
     * Returns the integral from <tt>x</tt> to infinity of the gamma
     * probability density function:
     * <pre>
     *               inf.
     *        b       -
     *       a       | |   b-1  -at
     * y =  -----    |    t    e    dt
     *       -     | |
     *      | (b)   -
     *               x
     * </pre>
     * The incomplete gamma integral is used, according to the
     * relation
     * <p>
     * y = Gamma.incompleteGammaComplement( b, a*x ).
     *
     * @param a the paramater a (alpha) of the gamma distribution.
     * @param b the paramater b (beta, lambda) of the gamma distribution.
     * @param x integration end point.
     */

    public double CdfCompl(double x)
    {
        if (x < 0.0)
        {
            return 0.0;
        }
        return IncompleteGamaFunct.IncompleteGammaComplement(getBeta(), getAlpha()*x);
    }

    /**
     * Returns the probability distribution function.
     */
    public double NextDouble()
    {
    	return NextDoubleStatic(m_dblAlpha, m_dblBeta, m_rng);
    }

    public static double NextDoubleStatic(
    		double dblAlpha,
    		double dblBeta,
    		IRng rng)
    {
    	try{
        /******************************************************************
         *                                                                *
         *    Gamma Distribution - Acceptance Rejection combined with     *
         *                         Acceptance Complement                  *
         *                                                                *
         ******************************************************************
         *                                                                *
         * FUNCTION:    - gds samples a random number from the standard   *
         *                gamma distribution with parameter  a > 0.       *
         *                Acceptance Rejection  gs  for  a < 1 ,          *
         *                Acceptance Complement gd  for  a >= 1 .         *
         * REFERENCES:  - J.H. Ahrens, U. Dieter (1974): Computer methods *
         *                for sampling from gamma, beta, Poisson and      *
         *                binomial distributions, Computing 12, 223-246.  *
         *              - J.H. Ahrens, U. Dieter (1982): Generating gamma *
         *                variates by a modified rejection technique,     *
         *                Communications of the ACM 25, 47-54.            *
         * SUBPROGRAMS: - drand(seed) ... (0,1)-Uniform generator with    *
         *                unsigned long integer *seed                     *
         *              - NORMAL(seed) ... Normal generator N(0,1).       *
         *                                                                *
         ******************************************************************/
        double a = dblAlpha;
        double aa = -1.0,
               aaa = -1.0,
               b = 0.0,
               c = 0.0,
               d = 0.0,
               e,
               r,
               s = 0.0,
               si = 0.0,
               ss = 0.0,
               q0 = 0.0,
               q1 = 0.0416666664,
               q2 = 0.0208333723,
               q3 = 0.0079849875,
               q4 = 0.0015746717,
               q5 = -0.0003349403,
               q6 = 0.0003340332,
               q7 = 0.0006053049,
               q8 = -0.0004701849,
               q9 = 0.0001710320,
               a1 = 0.333333333,
               a2 = -0.249999949,
               a3 = 0.199999867,
               a4 = -0.166677482,
               a5 = 0.142873973,
               a6 = -0.124385581,
               a7 = 0.110368310,
               a8 = -0.112750886,
               a9 = 0.104089866,
               e1 = 1.000000000,
               e2 = 0.499999994,
               e3 = 0.166666848,
               e4 = 0.041664508,
               e5 = 0.008345522,
               e6 = 0.001353826,
               e7 = 0.000247453;

        double gds, p, q, t, sign_u, u, v, w, x;
        double v1, v2, v12;

        // Check for invalid input values

        if (a <= 0.0)
        {
            throw new Exception();
        }
        if (dblBeta <= 0.0)
        {
            new Exception();
        }

        if (a < 1.0)
        {
            // CASE A: Acceptance rejection algorithm gs
            b = 1.0 + 0.36788794412*a; // Step 1
            for (;;)
            {
                p = b*rng.nextDouble();
                if (p <= 1.0)
                {
                    // Step 2. Case gds <= 1
                    gds = FastMath.exp(Math.log(p)/a);
                    if (Math.log(rng.nextDouble()) <= -gds)
                    {
                        return (gds/dblBeta);
                    }
                }
                else
                {
                    // Step 3. Case gds > 1
                    gds = -Math.log((b - p)/a);
                    if (Math.log(rng.nextDouble()) <= ((a - 1.0)*Math.log(gds)))
                    {
                        return (gds/dblBeta);
                    }
                }
            }
        }

        else
        {
            // CASE B: Acceptance complement algorithm gd (gaussian distribution, box muller transformation)
            if (a != aa)
            {
                // Step 1. Preparations
                aa = a;
                ss = a - 0.5;
                s = Math.sqrt(ss);
                d = 5.656854249 - 12.0*s;
            }
            // Step 2. Normal deviate
            do
            {
                v1 = 2.0*rng.nextDouble() - 1.0;
                v2 = 2.0*rng.nextDouble() - 1.0;
                v12 = v1*v1 + v2*v2;
            }
            while (v12 > 1.0);
            t = v1*Math.sqrt(-2.0*Math.log(v12)/v12);
            x = s + 0.5*t;
            gds = x*x;
            if (t >= 0.0)
            {
                return (gds/dblBeta); // Immediate acceptance
            }

            u = rng.nextDouble(); // Step 3. Uniform random number
            if (d*u <= t*t*t)
            {
                return (gds/dblBeta); // Squeeze acceptance
            }

            if (a != aaa)
            {
                // Step 4. Set-up for hat case
                aaa = a;
                r = 1.0/a;
                q0 = ((((((((q9*r + q8)*r + q7)*r + q6)*r + q5)*r + q4)*
                        r + q3)*r + q2)*r + q1)*r;
                if (a > 3.686)
                {
                    if (a > 13.022)
                    {
                        b = 1.77;
                        si = 0.75;
                        c = 0.1515/s;
                    }
                    else
                    {
                        b = 1.654 + 0.0076*ss;
                        si = 1.68/s + 0.275;
                        c = 0.062/s + 0.024;
                    }
                }
                else
                {
                    b = 0.463 + s - 0.178*ss;
                    si = 1.235;
                    c = 0.195/s - 0.079 + 0.016*s;
                }
            }
            if (x > 0.0)
            {
                // Step 5. Calculation of q
                v = t/(s + s); // Step 6.
                if (Math.abs(v) > 0.25)
                {
                    q = q0 - s*t + 0.25*t*t + (ss + ss)*Math.log(1.0 + v);
                }
                else
                {
                    q = q0 + 0.5*t*t*((((((((a9*v + a8)*v + a7)*v + a6)*
                                          v + a5)*v + a4)*v + a3)*v + a2)*v + a1)*v;
                } // Step 7. Quotient acceptance
                if (Math.log(1.0 - u) <= q)
                {
                    return (gds/dblBeta);
                }
            }

            for (;;)
            {
                // Step 8. Double exponential deviate t
                do
                {
                    e = -Math.log(rng.nextDouble());
                    u = rng.nextDouble();
                    u = u + u - 1.0;
                    sign_u = (u > 0) ? 1.0 : -1.0;
                    t = b + (e*si)*sign_u;
                }
                while (t <= -0.71874483771719); // Step 9. Rejection of t
                v = t/(s + s); // Step 10. New q(t)
                if (Math.abs(v) > 0.25)
                {
                    q = q0 - s*t + 0.25*t*t + (ss + ss)*Math.log(1.0 + v);
                }
                else
                {
                    q = q0 + 0.5*t*t*((((((((a9*v + a8)*v + a7)*v + a6)*
                                          v + a5)*v + a4)*v + a3)*v + a2)*v + a1)*v;
                }
                if (q <= 0.0)
                {
                    continue; // Step 11.
                }
                if (q > 0.5)
                {
                    w = FastMath.exp(q) - 1.0;
                }
                else
                {
                    w = ((((((e7*q + e6)*q + e5)*q + e4)*q + e3)*q + e2)*
                         q + e1)*q;
                } // Step 12. Hat acceptance
                if (c*u*sign_u <= w*FastMath.exp(e - 0.5*t*t))
                {
                    x = s + 0.5*t;
                    return (x*x/dblBeta);
                }
            }
        }
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return Double.NaN;
    }

    public double Pdf(double dblX)
    {
    	try{
        if (dblX < 0)
        {
            throw new Exception();
        }
        if (dblX == 0)
        {
            if (getAlpha() == 1.0)
            {
                return 1.0/getBeta();
            }
            else
            {
                return 0.0;
            }
        }
        if (getAlpha() == 1.0)
        {
            return FastMath.exp(-dblX/getBeta())/getBeta();
        }

        return FastMath.exp((getAlpha() - 1.0)*Math.log(dblX/getBeta()) - dblX/getBeta() -
                        LogGammaFunct.LogGamma(getAlpha()))/getBeta();
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return Double.NaN;
    }

    /**
     *  Gamma(alpha,beta)
     *
     *@param  alpha  alpha
     *@param  beta   beta
     *@return        Gamma(alpha,beta)
     *@author:       <Vadum Kutsyy, kutsyy@hotmail.com>
     */

    public double NextRandom2()
    {
        if (getAlpha() < 1)
        {
            double b = (Math.E + getAlpha())/Math.E;
            double y;
            double p;
            do
            {
                do
                {
                    double u1 = m_rng.nextDouble();
                    p = b*u1;
                    if (p > 1)
                    {
                        break;
                    }
                    y = Math.pow(p, 1/getAlpha());
                    double u2 = m_rng.nextDouble();
                    if (u2 <= FastMath.exp(-y))
                    {
                        return getBeta()*y;
                    }
                }
                while (true);
                y = -Math.log((getBeta() - p)/getAlpha());
                double u2_ = m_rng.nextDouble();
                if (u2_ <= Math.pow(y, getAlpha() - 1))
                {
                    return getBeta()*y;
                }
            }
            while (true);
        }
        else
        {
            double a = 1/Math.sqrt(2*getAlpha() - 1);
            double b = getAlpha() - Math.log(4);
            double q = getAlpha() + 1/a;
            double theta = 4.5;
            double d = 1 + Math.log(theta);
            do
            {
                double u1 = m_rng.nextDouble();
                double u2 = m_rng.nextDouble();
                double v = a*Math.log(u1/(1 - u2));
                double y = getAlpha()*FastMath.exp(v);
                double z = u1*u1*u2;
                double w = b + q*v + y;
                if (w + d - theta*z >= 0 && w >= Math.log(z))
                {
                    return getBeta()*y;
                }
            }
            while (true);
        }
    }

    /**
     * Returns a string representation of the receiver.
     */

    public String ToString()
    {
        return "GammaDist(" + getAlpha() + "," + getBeta() + ")";
    }

    /**
     * {@inheritDoc}
     *
     * For shape parameter {@code alpha} and scale parameter {@code beta}, the
     * mean is {@code alpha * beta}.
     */
    public double getNumericalMean() {
        return m_dblAlpha * m_dblBeta;
    }

    /**
     * {@inheritDoc}
     *
     * For shape parameter {@code alpha} and scale parameter {@code beta}, the
     * variance is {@code alpha * beta^2}.
     *
     * @return {@inheritDoc}
     */
    public double getNumericalVariance() {
        return m_dblBeta * m_dblAlpha * m_dblAlpha;
    }
    
    public double CdfInv(final double p)
    {
        /*
         * IMPLEMENTATION NOTES
         * --------------------
         * Where applicable, use is made of the one-sided Chebyshev inequality
         * to bracket the root. This inequality states that
         * P(X - mu >= k * sig) <= 1 / (1 + k^2),
         * mu: mean, sig: standard deviation. Equivalently
         * 1 - P(X < mu + k * sig) <= 1 / (1 + k^2),
         * F(mu + k * sig) >= k^2 / (1 + k^2).
         *
         * For k = sqrt(p / (1 - p)), we find
         * F(mu + k * sig) >= p,
         * and (mu + k * sig) is an upper-bound for the root.
         *
         * Then, introducing Y = -X, mean(Y) = -mu, sd(Y) = sig, and
         * P(Y >= -mu + k * sig) <= 1 / (1 + k^2),
         * P(-X >= -mu + k * sig) <= 1 / (1 + k^2),
         * P(X <= mu - k * sig) <= 1 / (1 + k^2),
         * F(mu - k * sig) <= 1 / (1 + k^2).
         *
         * For k = sqrt((1 - p) / p), we find
         * F(mu - k * sig) <= p,
         * and (mu - k * sig) is a lower-bound for the root.
         *
         * In cases where the Chebyshev inequality does not apply, geometric
         * progressions 1, 2, 4, ... and -1, -2, -4, ... are used to bracket
         * the root.
         */
        if (p < 0.0 || p > 1.0) {
            throw new OutOfRangeException(p, 0, 1);
        }

        double lowerBound = getSupportLowerBound();
        if (p == 0.0) {
            return lowerBound;
        }

        double upperBound = getSupportUpperBound();
        if (p == 1.0) {
            return upperBound;
        }

        final double mu = getNumericalMean();
        final double sig = FastMath.sqrt(getNumericalVariance());
        final boolean chebyshevApplies;
        chebyshevApplies = !(Double.isInfinite(mu) || Double.isNaN(mu) ||
                             Double.isInfinite(sig) || Double.isNaN(sig));

        if (lowerBound == Double.NEGATIVE_INFINITY) {
            if (chebyshevApplies) {
                lowerBound = mu - sig * FastMath.sqrt((1. - p) / p);
            } else {
                lowerBound = -1.0;
                while (Cdf(lowerBound) >= p) {
                    lowerBound *= 2.0;
                }
            }
        }

        if (upperBound == Double.POSITIVE_INFINITY) {
            if (chebyshevApplies) {
                upperBound = mu + sig * FastMath.sqrt(p / (1. - p));
            } else {
                upperBound = 1.0;
                while (Cdf(upperBound) < p) {
                    upperBound *= 2.0;
                }
            }
        }

        final UnivariateFunction toSolve = new UnivariateFunction() {

            public double value(final double x) {
                return Cdf(x) - p;
            }
        };

        double x = UnivariateSolverUtils.solve(toSolve,
                                                   lowerBound,
                                                   upperBound,
                                                   getSolverAbsoluteAccuracy());

        if (!isSupportConnected()) {
            /* Test for plateau. */
            final double dx = getSolverAbsoluteAccuracy();
            if (x - dx >= getSupportLowerBound()) {
                double px = Cdf(x);
                if (Cdf(x - dx) == px) {
                    upperBound = x;
                    while (upperBound - lowerBound > dx) {
                        final double midPoint = 0.5 * (lowerBound + upperBound);
                        if (Cdf(midPoint) < px) {
                            lowerBound = midPoint;
                        } else {
                            upperBound = midPoint;
                        }
                    }
                    return upperBound;
                }
            }
        }
        return x;    	
    }

    /**
     * {@inheritDoc}
     *
     * The support of this distribution is connected.
     *
     * @return {@code true}
     */
    public boolean isSupportConnected() {
        return true;
    }
    
    /**
     * Returns the solver absolute accuracy for inverse cumulative computation.
     * You can override this method in order to use a Brent solver with an
     * absolute accuracy different from the default.
     *
     * @return the maximum absolute error in inverse cumulative probability estimates
     */
    protected double getSolverAbsoluteAccuracy() {
        return solverAbsoluteAccuracy;
    }
    
    /**
     * {@inheritDoc}
     *
     * The lower bound of the support is always 0 no matter the parameters.
     *
     * @return lower bound of the support (always 0)
     */
    public double getSupportLowerBound() {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * The upper bound of the support is always positive infinity
     * no matter the parameters.
     *
     * @return upper bound of the support (always Double.POSITIVE_INFINITY)
     */
    public double getSupportUpperBound() {
        return Double.POSITIVE_INFINITY;
    }
}