/* $Id: SampleFactory.java,v 1.1 2011/08/16 12:37:39 NLMREE1 Exp $
 * 	
 * Created on Aug 9, 2011 
 *
 */
package Armadillo.Analytics.Stat.Sampling;


/**
 * 
 * Interface for factories, creating <tt>Sample</tt> 
 * with generic type <tt>V</tt> instances
 * or subclasses thereof.
 * Each factory holds an identifier.
 * 
 * @author 
 * @version CVS $Revision: 1.1 $
 * @since CVS $Date: 2011/08/16 12:37:39 $
 * @param <V> type of the value instance of the produced samples.
 */
public interface ISampleFactory<V> {

	/**
	 * Returns a new sample of generic type <V>.
	 * @param value the value instance
	 * @param likelihoodRatio the likelihood ratio
	 * @return a new sample of type <V>.
	 */
	ISample<V> newSample(V value, double likelihoodRatio);
	
	/**
	 * Returns a new sample (think of copy) from the input sample.
	 * @param input the input sample
	 * @return a new sample from (or copy of) the input sample. 
	 */
	ISample<V> newSample(ISample<V> input);
	
	
}
