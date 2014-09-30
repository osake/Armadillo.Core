/* $Id: AbstractDoubleRV.java,v 1.1 2010/12/07 14:44:36 NLMREE1 Exp $
 * 	
 * Created on Sep 3, 2010 
 *
 */
package Armadillo.Analytics.Stat.RandomVars;

import Armadillo.Analytics.Stat.Random.IRanNumGenerator;
import Armadillo.Analytics.Stat.Sampling.DoubleSampleFactory;
import Armadillo.Analytics.Stat.Sampling.ISample;


public abstract class ADoubleRV implements IDoubleRV {

	/**
	 * The double sample factory.
	 */
	protected final DoubleSampleFactory sampleFactory;
	
	public ADoubleRV() {
		this.sampleFactory = new DoubleSampleFactory();
	}
	
	/**
	 * Value given by next(rng).
	 */
	public ISample<Double> nextSample(IRanNumGenerator rng) {

		return sampleFactory.newSample(next(rng));
	}

	/**
	 * Standard deviation as the square root of the variance.
	 */
	public double stdev() {
		return Math.sqrt(variance());
	}
}
