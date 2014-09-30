/* $Id: DoubleRVIS.java,v 1.2 2013/11/12 14:09:00 nlpres1 Exp $
 * 	
 * Created on Sep 2, 2010 
 *
 */
package Armadillo.Analytics.Stat.Tiltable;

import Armadillo.Analytics.Stat.Random.IRanNumGenerator;
import Armadillo.Analytics.Stat.RandomVars.IDoubleRV;
import Armadillo.Analytics.Stat.Sampling.IDoubleSampler1Double;


/**
 * Interface for continuous random variables, 
 * that can be 'transformed' or 'tilted' using an importance sampling (IS) 
 * transformation by a single parameter.
 * In case of importance sampling, the IS density tilted by <tt>t</tt>
 * is defined as follows:
 * <pre>
 * [ISDEF] f_X(x) \rightarrow f^{(IS)}_X(x;t) = f_X(X) \rho^{-1}_X(x,t),
 * </pre>
 * where <tt>\rho</tt> is the likelihood ratio function, 
 * given by the method {@link #rho(double, double)}.
 * The method {@link #next(RanNumGenerator, double)} draws a new random value
 * for the <tt>t</tt>-tilted RV, 
 * defined by the distribution with density <tt>f^{(IS)}_{X}(x;t)</tt>.
 * The {@link #mean(double)} and {@link #variance(double)} are the mean and variance
 * of the <tt>t</tt>-tilted random variable.
 * 
 * @author 
 * @version CVS $Revision: 1.2 $
 * @since CVS $Date: 2013/11/12 14:09:00 $
 */
public interface IDoubleRVIS extends IDoubleRV, IDoubleSampler1Double {
	
	/**
	 * Returns the next random value drawn 
	 * from the <tt>t</tt>-tilted distribution <tt>f^{(IS)}_{X}(x;t)</tt>, 
	 * see [ISDEF], using the specified random number generator.
	 * @param rng the random number generator
	 * @param t the primary parameter or tilt
	 * @return the next draw from the <tt>t</tt>-tilted distribution of this RV
	 */
	double next(IRanNumGenerator rng, double t);
	
	/**
	 * Returns the sum of n random values drawn 
	 * from the <tt>t</tt>-tilted distribution <tt>f^{(IS)}_{X}(x;t)</tt>, 
	 * see [ISDEF], using the specified random number generator.
	 * @param rng the random number generator
	 * @param t the primary parameter or tilt
	 * @param n the number of draws
	 * @return the next draw from the <tt>t</tt>-tilted distribution of this RV
	 */
	double next(IRanNumGenerator rng, double t, int n);
	
	/**
	 * Returns the likelihood ratio or <tt>\rho</tt> function of the 
	 * RV value <tt>x</tt> at specified <tt>t</tt>, see [ISDEF].
	 * @param x the value of the RV
	 * @param t the primary parameter or tilt
	 * @return the likelihood ratio or <tt>\rho</tt> function of the 
	 * value <tt>x</tt> at specified <tt>t</tt>
	 */
	double rho(double x, double t);
	
	/**
	 * Returns the maximum allowed value for the tilt parameter.
	 * @return the maximum allowed value for the tilt parameter.
	 */
	double getMaxTilt();

	/**
	 * Returns the tilted mean or the mean of this t-tilted RV. 
	 * @param t the primary parameter or tilt
	 * @return the tilted mean
	 */
	double mean(double t);
	
	/**
	 * Returns the tilted variance or the variance of this t-tilted RV. 
	 * @param t the primary parameter or tilt
	 * @return the tilted variance
	 */
	double variance(double t);

	/**
	 * Returns the tilted standard deviation or the stdev of this t-tilted RV. 
	 * @param t the primary parameter or tilt
	 * @return the tilted standard deviation
	 */
	double stdev(double t);
}


