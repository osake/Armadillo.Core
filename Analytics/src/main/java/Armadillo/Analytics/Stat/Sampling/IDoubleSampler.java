package Armadillo.Analytics.Stat.Sampling;

import Armadillo.Analytics.Stat.Random.IRanNumGenerator;

/**
 * An interface for samplers 
 * that generates samples of Doubles, using
 * a random number generator.
 * @author 
 * @version CVS $Revision: 1.1 $
 * @since CVS $Date: 2010/12/07 14:44:36 $
 */
public interface IDoubleSampler {
	
	/**
	 * Returns the next <tt>Double</tt> sample, 
	 * using the specified random number generator.
	 * @param rng the random number generator
	 * @return the next <tt>Double</tt> sample, using the specified random number generator.
	 */
	ISample<Double> nextSample(IRanNumGenerator rng);
}
