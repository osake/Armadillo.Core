package Armadillo.Analytics.Stat.Sampling;

import Armadillo.Core.IDuplicable;


/**
 * This interface encapsulates a (value, likelihoodRatio) pair 
 * that can represent the results of (Monte-Carlo) 
 * stochastic simulations. 
 * Besides this basic pair, an implementation can hold a reference 
 * to the <tt>identifier</tt> of the sample.
 * The identifier references the source of the sample; it does not
 * have to be unique. The same source can generates
 * different samples, where each sample's identifier will point to the
 * identifier of the source.
 * Each sample should be able to create an exact duplicate of itself. 
 * In case that the sample itself is immutable, 
 * the duplicate can be the sample.
 * <p>
 * Moreover samples are assumed to be comparable amongst each other.
 * For samples of doubles or integers this ordering is natural.
 * For samples of vectors ordering could be based on the vector norm.
 * In cases where an appropriate ordering cannot be defined, make
 * sure that the {@link Comparable#compareTo(Object)} method throw
 * {@link UnsupportedOperationException}.
 * 
 * @author 
 * @version CVS $Revision: 1.2 $
 * @since CVS $Date: 2011/08/16 12:37:39 $s
 * @param <V> type of the value instance 
 * that this <tt>Sample</tt> holds. 
 */
public interface ISample<V> extends Comparable<ISample<V>>, IDuplicable<ISample<V>> {
	
	 /**
     * Returns the sample value.
     * @return the sample value
     */
	V getValue();
	
	/**
	 * Returns the sample likelihood ratio.
	 * @return the sample likelihood ratio
	 */
	double getLikelihoodRatio();

}
