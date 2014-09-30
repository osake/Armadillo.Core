/* $Id: DoubleSampler1Double.java,v 1.1 2010/12/07 14:44:36 NLMREE1 Exp $
 * 	
 * Created on Sep 1, 2010 
 *
 */
package Armadillo.Analytics.Stat.Sampling;

import Armadillo.Analytics.Stat.Random.IRanNumGenerator;

/**
 * An interface for samplers 
 * that generates samples of Doubles (the output Double value) 
 * from input t where t is a double, 
 * besides using a random number generator.
 * 
 * @author
 * @version CVS $Revision: 1.1 $
 * @since CVS $Date: 2010/12/07 14:44:36 $
 */
public interface IDoubleSampler1Double {

	/**
	 * Returns the next <tt>Double</tt> sample for specified input t.
	 * where t is a double, using the specified random number generator.
	 * @param rng the random number generator
	 * @param t the input double 
	 * @return the next <tt>Double</tt> sample from the input t, 
	 * using the specified random number generator
	 */
	ISample<Double> nextSample(IRanNumGenerator rng, double t);
}
