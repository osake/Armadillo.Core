/* $Id: AbstractDoubleRVIS.java,v 1.1 2010/12/07 14:44:36 NLMREE1 Exp $
 * 	
 * Created on Sep 3, 2010 
 *
 */
package Armadillo.Analytics.Stat.Tiltable;

import Armadillo.Analytics.Stat.Random.IRanNumGenerator;
import Armadillo.Analytics.Stat.RandomVars.ADoubleRV;
import Armadillo.Analytics.Stat.Sampling.ISample;

/**
 * Abstract class implementing some methods.
 * 
 * rho in term of the cgf.
 * @author
 * @version CVS $Revision: 1.1 $
 * @since CVS $Date: 2010/12/07 14:44:36 $
 */
public abstract class ADoubleRVIS extends ADoubleRV 
	implements IDoubleRVIS {

	/**
	 * Value given by next(rng, t) and likelihood-ratio by rho(x,t).
	 */
	public ISample<Double> nextSample(IRanNumGenerator rng, double t) {

		final double x = next(rng, t);
			
		final double likelihood = rho(x, t);
			
		return sampleFactory.newSample(x, likelihood);
	}

	/**
	 * Tilted standard deviation as the square root of the variance.
	 */
	public double stdev(double t) {
		return Math.sqrt(variance(t));
	}
}
