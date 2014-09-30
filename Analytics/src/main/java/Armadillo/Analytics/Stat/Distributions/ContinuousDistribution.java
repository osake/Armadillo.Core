/* $Id: ContinuousDistribution.java,v 1.2 2012/11/21 12:22:54 deonau1 Exp $
 * 
 * Created on 12-Jun-2007
 *
 */
package Armadillo.Analytics.Stat.Distributions;

import Armadillo.Analytics.Stat.IStats;
import Armadillo.Analytics.Stat.RandomVars.IDoubleRV;

/**
 * Basic interface for continuous distributions.
 * 
 * @author 
 * @version CVS $Revision: 1.2 $
 * @since CVS $Date: 2012/11/21 12:22:54 $
 */
public interface ContinuousDistribution extends IDoubleRV, IStats {
    
	/**
	 * Returns the cumulative distribution function (cdf) at <tt>x</tt>.
	 * 
	 * @param x the argument to the cumulative distribution function
	 * @return the value of the cumulative distribution function at x
	 */
	double cdf(double x);

	/**
	 * Return the value of the probability density function (pdf) at <tt>x</tt> 
	 * of this <tt>Distribution</tt>. 
	 * 
	 * @param x the argument to the probability density function
	 * @return the probability density function at x
	 */
	double pdf(double x);
    
}