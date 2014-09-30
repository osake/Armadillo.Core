package Armadillo.Analytics.Stat.Distributions;

import Armadillo.Analytics.Base.FastMath;
import Armadillo.Analytics.Stat.Random.IRanNumGenerator;
import Armadillo.Analytics.Stat.Tiltable.AContExpTiltable;
import Armadillo.Core.Math.DoubleFormat;
import Armadillo.Core.Math.MachinePrecision;

/**
 * Concrete subclass of AbstractTruncatedExponential.
 * It is possible to simplify the expression for the mean of the truncated exponential distribution by choosing an additional
 * parameter <tt>\delta</tt> and setting
 * <p>
 * G_{\delta}(x; L; \lambda) = \int_{0}^{x} g_{\delta}(L, \lambda, t)dt =
 * \delta(1 - exp(-\frac{\lambda x}{L})), 0 \le x < L,
 * <p>
 * G_{\delta}(x; L; \lambda) = 1, x \ge L,
 * <p>
 * where <tt>0 \le \delta \le \frac{1}{1 - e^{-\lambda}}</tt>. The expression
 * for the mean is now
 * <p>
 * E[G_{\delta}(x; L; \lambda)] = -\delta L e^{-\lambda} + \frac{\delta
 * L}{\lambda}(1 - e^{-\lambda}) + [1 - \delta(1 - e^{-\lambda})].
 * <p>
 * Setting
 * <p>
 * \delta = \lambda\frac{\frac{1}{\lambda} - 1}{1 - lambda - e^{-\lambda}},
 * <p>
 * simplifies the mean to
 * <p>
 * E[G_{\delta}(x; L; \lambda)] = \frac{L}{\lambda}.
 * 
 * @author 
 * @version 
 * @since 
 */
public class TruncatedExponentialDeltaDist extends AContExpTiltable{

	/**
	 * The maximum exponent of e
	 */
	public static final double MAXEXPE = MachinePrecision.m_dblMaxExpE;
	
	/** 
	 * The <tt>\lambda</tt> parameter.
	 */
	protected final double m_dblLambda;

	/**
	 * The cutoff parameter.
	 */
	protected final double m_dblCutoffL;

	/**
	 * The rescaled (by cutoff) <tt>lambda</tt>.
	 */
	protected final double m_dblLambdaDivL;

	/**
	 * For internal use.
	 */
	protected static final double EXPFIETHRESHOLD = 1.0e-3;
	
	// after constructor completes, state of object is 'fixed'.
	// It is immutable, with the exception of the uniformDeviate.
	
	/**
	 * The <tt>\delta</tt> parameter.
	 */
	protected final double m_dblDelta;

	/**
	 * Some useful constants
	 */
	protected final double m_dblPrefactor;

	/**
	 * cdf at the x=cutoff or L.
	 */
	protected final double m_dblCdfAtCutoffL;

	/**
	 * expFie(lambda).
	 */
	protected final double m_dblExpFieLambda;
	
	/**
	 * Constructor with a specified identifier, 
	 * lambda, upper cutoff, delta.
	 * @param id the identifier
	 * @param lambda the value of lambda
	 * @param cutoffL the value of the upper cutoff
	 * @param delta the value of delta
	 * @throws IllegalArgumentException If delta less than zero or greater than <tt>\frac{1}{1 - e^{-\lambda}}</tt>
	 */
	public TruncatedExponentialDeltaDist(
			double lambda, 
			double cutoffL,
			double delta) {

		if (lambda <= 0.0)
			throw new IllegalArgumentException("The parameter lambda should be positive: " + lambda);
			        
		if (Double.isNaN(lambda) || Double.isInfinite(lambda))
			throw new IllegalArgumentException("The parameter " +
					"lambda is NaN or Infinite: " + lambda);
			     
		if (cutoffL <= 0.0)
			throw new IllegalArgumentException("Cutoff should be positive: "
					+ cutoffL);
			        
		if (Double.isNaN(cutoffL) || Double.isInfinite(cutoffL))
			throw new IllegalArgumentException("Cutoff is NaN or Infinite: "
					+ cutoffL);
		
		this.m_dblLambda = lambda;
		this.m_dblCutoffL = cutoffL;
		this.m_dblLambdaDivL = lambda / cutoffL;
		
		if (delta < 0.0 || delta > 1.0 / (1.0 - FastMath.exp(-lambda)))
			throw new IllegalArgumentException("Incorrect value of delta: "
					+ delta);

		this.m_dblDelta = delta;

		// set some useful factors...
		m_dblPrefactor = delta * m_dblLambdaDivL;
		// this is the value of the cdf at the discontinuous point x=cutoffL.
		m_dblCdfAtCutoffL = delta * (1.0 - FastMath.exp(-lambda));
		// this is the value of expFie at lambda:
		m_dblExpFieLambda = expFie(lambda);
	}
	
	 /**
	 * Gets the upper cutoff.
    * @return Returns the cutoff
    */
	
   public double getCutoffL() {
       
   	return m_dblCutoffL;
   }
	
   /**
    * Gets the lambda parameter.
    * @return Returns the lambda
    */
   
   public double getLambda() {
       
   	return m_dblLambda;
   }

   
   
   /**
    * Calculates <tt>\frac{1 - e^{-x}}{x}</tt> which is important in concrete subclasses of
    * AbstractTruncatedExponential. Notice that this function is present in the mean of the distribution
    * when <tt>x = \lambda</tt>.
    * @param x the argument of the function
    * @return Returns the value of the function
    */
   
	protected static double expFie(double x) {

       double xSqr = x * x;
       double xTri = x * xSqr;

       if (Math.abs(x) < EXPFIETHRESHOLD) {
           double series = (1.0 - x / 2.0 + xSqr / 6.0 - xTri / 24.0 + xSqr
                   * xSqr / 120.0 - xTri * xSqr / 720.0);
           return series;
       }
       return (1.0 - FastMath.exp(-x)) / x;
   }
	
	/**
	 * Calculates the derivative of <tt>\frac{1 - e^{-x}}{x}</tt>.
	 * @param x the argument of the function
	 * @return Returns the value of the function
	 */
	
   protected static double expFiePrime(double x) {

       double xSqr = x * x;
       double xTri = x * xSqr;

       if (Math.abs(x) < EXPFIETHRESHOLD) {
           double series = (-0.5 + x / 3.0 - xSqr / 8.0 + xTri / 30.0 - xSqr
                   * xSqr / 144.0 + xTri * xSqr / 840.0);
           return series;
       }
       return -(1.0 - (1.0 + x) * FastMath.exp(-x)) / xSqr;
   }
   /**
    * Calculates the second derivative of <tt>\frac{1 - e^{-x}}{x}</tt>.
    * @param x the argument of the function
    * @return Returns the value of the function
    */
   
   protected static double expFiePrimePrime(double x) {

       double xSqr = x * x;
       double xTri = x * xSqr;

       if (Math.abs(x) < EXPFIETHRESHOLD) {
           double series = (1.0 / 3.0 - x / 4.0 + xSqr / 10.0 - xTri / 36.0
                   + xSqr * xSqr / 168.0 - xTri * xSqr / 960.0);
           return series;
       }
       return (2.0 - (xSqr + 2.0 * (x + 1)) * FastMath.exp(-x)) / xTri;
   }
   /**
    * Returns  <tt>-\frac{log(1 - \alpha * uf * u)}{\alpha}</tt> which is the inverse of the exponential
    * function <tt>\frac{1-exp(-alpha * x)}{uf * alpha} which is used for random number \
    * creation in concrete subclasses.
    * @param u the exponential function value		
    * @param uf the first parameter of the exponential function
    * @param alpha the second parameter of the exponential function
    * @return the value of the inverse exponential form
    */
   
   protected static double inverseExponentialForm(double u, double uf, double alpha) {
   	/* This function returns 
   	 * x = -log(1-alpha * uf * u) / alpha
   	 * which is the inverse of the exponential function:
   	 * u = (1-exp(-alpha * x)) / (uf * alpha).
   	 */
   	if(alpha == 0.0) {
   		return uf * u;
   	}
   	
   	return -Math.log(1.0 - alpha * uf * u) / alpha;
   }
   
   /**
    * Truncated exponential distributions 
    * can be tilted by any <tt>t</tt>.
    * No bound. However, we do have take into account numerical accuracy, 
    * which dictates that e^(L * t) should be finite numerically.
    */
   public double getMaxTilt() {
   	double maxTilt = 0.5 * MAXEXPE / m_dblCutoffL;
		return maxTilt;
   }

   /**
	 * Returns zero.
	 */
	public double inf() {
		return 0.0;
	}
   
   /**
	 * Returns the cutoff L.
	 */
	public double sup() {
		return m_dblCutoffL;
	}	
	
	
	/**
	 * Sets the delta so that the mean of the distribution is equal to <tt>\frac{L}{\lambda}</tt>.
	 * @param lambda the value of lambda
	 * @return the revised delta
	 */
	public static double deltaToSetMeanAtLambdaOverCutoffL(double lambda) {

		/* delta set so that mean = cutoffL/lambda. */
		double delta = ((1.0 / lambda) - 1.0) / (expFie(lambda) - 1.0);

		return delta;
	}

	public double pdf(double x) {
		if (x < 0.0)
			throw new IllegalArgumentException("x cannot be less than zero!");

		if (x < m_dblCutoffL)
			return (m_dblPrefactor * FastMath.exp(-m_dblLambdaDivL * x));

		if (x == m_dblCutoffL)
			return (1.0 - m_dblCdfAtCutoffL);

		return 0.0;
	}

	public double cdf(double x) {
		if (x < 0.0)
			throw new IllegalArgumentException("x cannot be less than zero!");

		if (x >= m_dblCutoffL)
			return 1.0;

		return m_dblDelta * (1.0 - FastMath.exp(-m_dblLambdaDivL * x));
	}

	public double mean() {
		// return ((cutoffL / lambda) * delta * (1.0 - FastMath.exp(-lambda)) +
		// cutoffL * (1.0 - delta));
		return (m_dblCutoffL * (1.0 - m_dblDelta + m_dblDelta * m_dblExpFieLambda));
	}

	// first moment equals mean().
	public double firstMoment() {
		return m_dblCutoffL * (1.0 - m_dblDelta * m_dblLambda * (expFie(m_dblLambda) + expFiePrime(m_dblLambda)));
	}
	
	public double secondMoment() {
		
		return m_dblCutoffL * m_dblCutoffL * (m_dblDelta * m_dblLambda * expFiePrimePrime(m_dblLambda) + 1.0 - m_dblCdfAtCutoffL);
	}
	
	public double variance() {
		
		double mean = firstMoment();
		
		return (secondMoment() - mean * mean);
	}
	
	public double mgf(double t) {

		if (t == 0.0) {
			return 1.0;
		}

		return (m_dblDelta * m_dblLambda * expFie(m_dblLambda - t * m_dblCutoffL) + FastMath.exp(t
				* m_dblCutoffL)
				* (1.0 - m_dblCdfAtCutoffL));
	}

	public double mgfPrime(double t) {

		return m_dblCutoffL
				* (-m_dblDelta * m_dblLambda * expFiePrime(m_dblLambda - t * m_dblCutoffL) + Math
						.exp(t * m_dblCutoffL)
						* (1.0 - m_dblCdfAtCutoffL));
	}
	
	public double mgfPrimePrime(double t) {

		return m_dblCutoffL
				* m_dblCutoffL
				* (m_dblDelta * m_dblLambda * expFiePrimePrime(m_dblLambda - t * m_dblCutoffL) + Math
						.exp(t * m_dblCutoffL)
						* (1.0 - m_dblCdfAtCutoffL));
	}

	public double next(IRanNumGenerator rng) {
		
		double uniform = rng.raw();
		
		if (uniform >= m_dblCdfAtCutoffL)
			return m_dblCutoffL;
		else
			return -Math.log(1.0 - uniform / m_dblDelta) / m_dblLambdaDivL;
	}
	
	public double next(IRanNumGenerator rng, double t) {

		double uniform = rng.raw();

		double value = (m_dblLambda - t * m_dblCutoffL);

		double mgfT = mgf(t);

		double threshold = (m_dblDelta * m_dblLambda / mgfT) * expFie(value);

		if (uniform >= threshold) {

			return m_dblCutoffL;

		} else {

			double logArg = value * mgfT * uniform / (m_dblDelta * m_dblLambda);

			if (value == 0.0)
				return m_dblCutoffL * mgfT * uniform / (m_dblDelta * m_dblLambda);

			return -(m_dblCutoffL / value) * Math.log(1.0 - logArg);
		}
	}
	
    public double next(IRanNumGenerator rng, double t, int n) {
    	
    	double result = 0.0;
		
		for(int i = 0; i < n; i++) {
			result += next(rng, t);
		}
		
		return result;
    }
	
	public String toString() {
		return this.getClass().getSimpleName() + "(lambda=" + 
			DoubleFormat.SCIENTIFICSHORT.format(m_dblLambda) + ", cutoffL=" + 
			DoubleFormat.SCIENTIFICSHORT.format(m_dblCutoffL) + ", delta=" + 
			DoubleFormat.SCIENTIFICSHORT.format(m_dblDelta) + ")";
	}

	/**
	 * Gets the delta for this distribution.
	 * @return the delta
	 */
	public double getDelta() {
		return m_dblDelta;
	}
	
	public double getNumericalVar(){
		double dblMean = firstMoment();
		
		return (secondMoment() - dblMean * dblMean);
	}
	
	public double getNumericalMean(){
		return (m_dblCutoffL * (1.0 - m_dblDelta + m_dblDelta * m_dblExpFieLambda));		
	}
}
