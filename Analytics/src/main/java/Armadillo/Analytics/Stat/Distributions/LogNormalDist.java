package Armadillo.Analytics.Stat.Distributions;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.UnivariateSolverUtils;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.special.Erf;
import Armadillo.Analytics.Base.FastMath;
import Armadillo.Analytics.Stat.Random.IRng;

public class LogNormalDist extends AUnivContDist{

    /// <summary>
    /// Own instance
    /// </summary>
    private double m_dblMu; // scale parameter
    private double m_dblSigma;
    public static final double DEFAULT_INVERSE_ABSOLUTE_ACCURACY = 1e-9;    
    /** Inverse cumulative probability accuracy. */
    private final double solverAbsoluteAccuracy = DEFAULT_INVERSE_ABSOLUTE_ACCURACY;
    /** &radic;(2) */
    private static final double SQRT2 = FastMath.sqrt(2.0);
    /** &radic;(2 &pi;) */
    private static final double SQRT2PI = FastMath.sqrt(2 * FastMath.PI);

    public LogNormalDist(
            double dblMu,
            double dblSigma){
    	this(dblMu, dblSigma, null);
    }
    
    public LogNormalDist(
        double dblMu,
        double dblSigma,
        IRng rng) 
    {
    	super(rng);
        SetState(
            dblMu,
            dblSigma);
    }

    private void SetState(
        double dblMu,
        double dblSigma)
    {
        m_dblMu = dblMu;
        m_dblSigma = dblSigma;
    }

    @Override
    public double NextDouble()
    {
    	return NextDouble(m_rng);
    }
    
    /**
     *  Insert the method's description here. Creation date: (1/26/00 11:45:20 AM)
     *
     *@param  mu     double
     *@param  sigma  double
     *@return        double
     *@author:       <Vadum Kutsyy, kutsyy@hotmail.com>
     */
    public double NextDouble(IRng m_rng)
    {
        final double n = UnivNormalDistStd.NextDouble_static(m_rng);
        return FastMath.exp(m_dblMu + m_dblSigma * n);
    }

    @Override
    public double Cdf(double x)
    {
        if (x <= 0) {
            return 0;
        }
        final double dev = FastMath.log(x) - m_dblMu;
        if (FastMath.abs(dev) > 40 * m_dblSigma) {
            return dev < 0 ? 0.0d : 1.0d;
        }
        return 0.5 + 0.5 * Erf.erf(dev / (m_dblSigma * SQRT2));
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
     * @return upper bound of the support (always
     * {@code Double.POSITIVE_INFINITY})
     */
    public double getSupportUpperBound() {
        return Double.POSITIVE_INFINITY;
    }
    
    /** {@inheritDoc} */
    protected double getSolverAbsoluteAccuracy() {
        return solverAbsoluteAccuracy;
    }
    
    @Override
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
     * {@inheritDoc}
     *
     * For scale {@code m} and shape {@code s}, the mean is
     * {@code exp(m + s^2 / 2)}.
     */
    public double getNumericalMean() {
        double s = m_dblSigma;
        return FastMath.exp(m_dblMu + (s * s / 2));
    }

    /**
     * {@inheritDoc}
     *
     * For scale {@code m} and shape {@code s}, the variance is
     * {@code (exp(s^2) - 1) * exp(2 * m + s^2)}.
     */
    public double getNumericalVariance() {
        final double s = m_dblSigma;
        final double ss = s * s;
        return (FastMath.exp(ss) - 1) * FastMath.exp(2 * m_dblMu + ss);
    }
    
    @Override
    public double Pdf(double x)
    {
        if (x <= 0) {
            return 0;
        }
        final double x0 = FastMath.log(x) - m_dblMu;
        final double x1 = x0 / m_dblSigma;
        return FastMath.exp(-0.5 * x1 * x1) / (m_dblSigma * SQRT2PI * x);

    }
}
