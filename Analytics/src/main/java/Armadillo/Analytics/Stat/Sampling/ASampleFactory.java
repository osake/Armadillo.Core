/* $Id: AbstractSampleFactory.java,v 1.3 2013/10/31 12:20:17 DEHCAM1 Exp $
 * 	
 * Created on Jan 19, 2010 
 *
 */
package Armadillo.Analytics.Stat.Sampling;


/**
 * Abstract class for factories, creating <tt>Sample</tt> 
 * with generic type <tt>V</tt> instances
 * or subclasses thereof.
 * Each factory holds an identifier.
 * 
 * @author
 * @version CVS $Revision: 1.3 $
 * @since CVS $Date: 2013/10/31 12:20:17 $
 */
public abstract class ASampleFactory<V> implements ISampleFactory<V> {

	
	/**
	 * Default constructor setting nothing.
	 */
	public ASampleFactory() {
	}
	
	/**
	 * Checks the input. 
	 * Implementation should define what to be checked if any.
	 * @param value the value to be checked
	 */
	protected abstract void checkValue(V value);
	
	/**
	 * Checks likelihoodRatio parameter input.
	 * @param likelihoodRatio the likelihoodRatio to be checked
	 * @throws IllegalArgumentException if <tt>likelihoodRatio</tt> 
	 * is non-positive or <tt>NaN</tt>. 
	 */
	protected void checkLikelihoodRatio(double likelihoodRatio) {
		
//		if (likelihoodRatio <= 0.0)
//	            throw new IllegalArgumentException(
//	                    "Must be positive 'likelihoodRatio': "
//	                            + likelihoodRatio + " in factory " + this);
		
		if (likelihoodRatio < 0.0)
            throw new IllegalArgumentException(
                    "Must not be negative 'likelihoodRatio': "
                            + likelihoodRatio + " in factory " + this);

		if (Double.isNaN(likelihoodRatio)) {
	            throw new IllegalArgumentException(
	                    "Can't be NaN 'likelihoodRatio': "
	                            + likelihoodRatio + " in factory " + this);
		}
	}
	
	/**
	 * Returns a new sample (think of copy) from the input sample.
	 * @param input the input sample
	 * @return a new sample from (or copy of) the input sample. 
	 */
	public ISample<V> newSample(ISample<V> input) {
		return newSample(input.getValue(), input.getLikelihoodRatio());
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
