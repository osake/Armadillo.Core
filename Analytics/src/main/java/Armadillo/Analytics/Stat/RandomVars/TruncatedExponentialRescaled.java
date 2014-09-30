package Armadillo.Analytics.Stat.RandomVars;

import Armadillo.Analytics.Base.FastMath;
import Armadillo.Analytics.Base.IScalarFunctionX;
import Armadillo.Analytics.Base.NewtonRaphson;
import Armadillo.Analytics.Stat.Random.IRanNumGenerator;
import Armadillo.Analytics.Stat.Tiltable.AContExpTiltable;
import Armadillo.Core.Logger;
import Armadillo.Core.Math.DoubleFormat;
import Armadillo.Core.Math.MachinePrecision;

	/**
	 * The rescaled truncated exponential or censored exponential
	 * has a distribution function given by
	 * <p>
	 * G(x;\lambda, L) = \frac{G(x)}{G(L)} = \frac{1 - exp(-\frac{\lambda x}{L})}{1 - exp(-\lambda)}, 0 \le x < L.
	 * <p>
	 * The probability density function and mean are given by
	 * <p>
	 * g(x;\lambda, L) = 
	 * \frac{\lambda}{1 - exp(-\lambda)}\frac{exp(-\frac{\lambda x}{L})}{L}, , 0 \le x < L,
	 * <p>
	 * and
	 * <p>
	 * E[X] = \frac{L}{\lambda} - L\frac{e^{-\lambda}}{1 - e^{-\lambda}}.
	 * 
	 * @author 
	 * @version CVS $Revision: 1.9 $
	 * @since CVS $Date: 2013/11/12 14:50:58 $
	 */
	public class TruncatedExponentialRescaled  extends AContExpTiltable{

		/**
		 * The maximum exponent of e
		 */
		public static final double MAXEXPE = Armadillo.Core.Math.MachinePrecision.m_dblMaxExpE;
		
		/** 
		 * The <tt>\lambda</tt> parameter.
		 */
		protected final double lambda;

		/**
		 * The cutoff parameter.
		 */
		protected final double cutoffL;

		/**
		 * The rescaled (by cutoff) <tt>lambda</tt>.
		 */
		protected final double lambdaDivL;

		/**
		 * For internal use.
		 */
		protected static final double EXPFIETHRESHOLD = 1.0e-3;

		//	 after constructor completes, state of object is 'fixed'.
	    // It is immutable, with the exception of the uniformDeviate.
		
		/**
		 * It sets the identifier, value of lambda, 
		 * the value of the cutoff.
		 * @param id the identifier
		 * @param lambda the value of lambda
		 * @param cutoffL the value of the upper cutoff
		 * @throws IllegalArgumentException If lambda or the cutoff is not positive, NaN or infinite
		 */
		public TruncatedExponentialRescaled(double lambda, double cutoffL) {

			if(lambda < EXPFIETHRESHOLD * EXPFIETHRESHOLD) {
				throw new IllegalArgumentException("This distribution is not to supported " +
						"with values of lambda < " + EXPFIETHRESHOLD * EXPFIETHRESHOLD);
			}

			if (lambda <= 0.0){
				throw new IllegalArgumentException("The parameter lambda should be positive: " + lambda);
			}        
				
			if (Double.isNaN(lambda) || Double.isInfinite(lambda)){
				throw new IllegalArgumentException("The parameter " +
						"lambda is NaN or Infinite: " + lambda);
			}
			
			if (cutoffL <= 0.0){
				throw new IllegalArgumentException("Cutoff should be positive: "
						+ cutoffL);
			}
			
			if (Double.isNaN(cutoffL) || Double.isInfinite(cutoffL)){
				throw new IllegalArgumentException("Cutoff is NaN or Infinite: "
						+ cutoffL);
			}
			
			this.expFie_lambda = expFie(lambda);
			this.lambda = lambda;
			this.cutoffL = cutoffL;
			this.lambdaDivL = lambda / cutoffL;
		}
		
		 /**
		 * Gets the upper cutoff.
	     * @return Returns the cutoff
	     */
	    public double getCutoffL() {
	        
	    	return cutoffL;
	    }
		
	    /**
	     * Gets the lambda parameter.
	     * @return Returns the lambda
	     */
	    
	    public double getLambda() {
	        
	    	return lambda;
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
	    	double maxTilt = 0.5 * MAXEXPE / cutoffL;
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
			return cutoffL;
		}
		
		/**
		 * An accuracy parameter/threshold for computing lambda parameter from
		 * statistics info see {@link #lambda(double, double)}.
		 */
		public final static double ACCURACY = 1.0e-5;
		
		/**
		 * expFie(lambda)
		 */
		protected final double expFie_lambda;
		
		
		
		/**
		 * Computes and returns the <tt>\lambda</tt> parameter as function of the mean and the max (=cutoff).
		 * <pre>
		 *   mean / max = 1/\lambda - e^{-\lambda}/ (1 - e^{-\lambda}).
		 * </pre>
		 * Above equation is solved numerically, using a Newton-Raphson root-finding routine.
		 * 
		 * @param mean the mean
		 * @param sup the maximum, cutoff or supremum 
		 * @return the lambda parameter
		 */
		public static double lambda(double mean, double sup) {
		
			final double meanOverMax = mean / sup;
		 	
	        /* compute lambda: */
	        IScalarFunctionX fLambda = new IScalarFunctionX() {
	        	
	        	public double apply(double lambda) {
	        		
	        		return (-(expFiePrime(lambda) / expFie(lambda))- meanOverMax);
	        		
	        		//return ((1.0 / lambda) * (1.0 - FastMath.exp(-lambda) / expFie(lambda)) - meanOverMax);
	        	}
	        };

	        /* derivative of fLambda */
	        IScalarFunctionX fLambdaDerivative = new IScalarFunctionX() {
	        	
	        	public double apply(double lambda) {
	        		
	        		double fiePrimePrime = expFiePrimePrime(lambda);
	        		double fiePrime = expFiePrime(lambda);
	        		double fie = expFie(lambda);
	        		
	        		return (Math.pow(fiePrime/fie, 2.0) - fiePrimePrime / fie);
	        		
//	        		double denom = expFie(lambda);  /* use expFie to make sure that lambda values close to one are taken care of properly */
//	        		
//	        		return ( - (1.0 / (lambda * lambda)) * (1.0 - FastMath.exp(-lambda)/(denom * denom)));
	        	}	
	        };

			NewtonRaphson findRoot = new NewtonRaphson(fLambda, fLambdaDerivative);
			
//			Result lambdaSolution = findRoot.rtsafe(0.0, MAXEXPE, 
//					1000.0 * MachinePrecision.getMachinePrecisionDouble());
			
			// NLPRES1: set better upper bound
			Armadillo.Analytics.Base.NewtonRaphson.Result lambdaSolution = null;
			try {
				lambdaSolution = findRoot.rtsafe(0.0, Math.min(sup / mean, MAXEXPE), 
						1000.0 * MachinePrecision.m_dblMachineEps);
			} catch (Exception ex) {
				Logger.log(ex);
			}
	        
			return lambdaSolution.getRoot();
		}
		
		
		public double pdf(double x) {
			
			if (x < 0.0)
				throw new IllegalArgumentException("x cannot be less than zero!");

			if (x <= cutoffL)
				return (FastMath.exp(-lambdaDivL * x) / (cutoffL * expFie_lambda));

			return 0.0;
		}

		public double next(IRanNumGenerator rng) {
			
			/* recall that uniformDeviate.nextDouble() returns a random uniform number 
			 * in the open interval (0, 1). So zero and one are excluded.
			 */
			double uniform = rng.raw();
			
			double uf = cutoffL * expFie_lambda;
			
			return inverseExponentialForm(uniform, uf, lambdaDivL);
		}
		
		public double cdf(double x) {
			
			if (x < 0.0)
				throw new IllegalArgumentException("x cannot be less than zero!");

			if (x >= cutoffL)
				return 1.0;

			return (x * expFie(lambdaDivL * x) / (cutoffL * expFie_lambda));
		}
		
		public double mean() {
			
			return firstMoment();
			
		//	return (1.0 / lambdaDivL) * (1.0 - (FastMath.exp(-lambda) / expFie_lambda));
		}

		/**
		 * Returns the first moment or mean.
		 * @return the first moment
		 */
		public double firstMoment() {
			
			return (-cutoffL) * (expFiePrime(lambda) / expFie_lambda);
		}
		
		/**
		 * Returns the second moment.
		 * @return the second moment
		 */
		public double secondMoment() {
			return (cutoffL * cutoffL) * (expFiePrimePrime(lambda) / expFie_lambda);
		}
		
		public double variance() {
			
			double mean = firstMoment();
			
			return (secondMoment() - mean * mean);
		}
		
		public double mgf(double t) {

	        if(t == 0.0) {
	            return 1.0;
	        }
	        
	        return (expFie(lambda - t * cutoffL) / expFie_lambda);
	    }
		
		 public double mgfPrime(double t) {
		        
			 return (-cutoffL * expFiePrime(lambda - t * cutoffL) / expFie_lambda);
		}

		public double mgfPrimePrime(double t) {
		        
			 return (cutoffL * cutoffL
		                * expFiePrimePrime(lambda - t * cutoffL) / expFie_lambda);
		 }

		 
		 
		 public double next(IRanNumGenerator rng, double t) {

			 double uniform = rng.raw();

			 double alpha = (lambdaDivL - t);
		        
			 double uf = cutoffL * expFie(alpha * cutoffL);

			 return inverseExponentialForm(uniform, uf, alpha);
		        
		 }
		 
	    public double next(IRanNumGenerator rng, double t, int n) {
		    	
	    	double result = 0.0;
				
			for(int i = 0; i < n; i++) {
				result += next(rng, t);
			}
				
			return result;
	    }	 
		 
		 @Override
		 public String toString() {
		        return this.getClass().getSimpleName() 
		        + "(lambda=" + DoubleFormat.SCIENTIFICSHORT.format(lambda) + ", cutoffL="
		                + DoubleFormat.SCIENTIFICSHORT.format(cutoffL) + ")";
		    }


}
