/* $Id: ContExpTiltable.java,v 1.1 2010/12/07 14:44:37 NLMREE1 Exp $
 * 	
 * Created on May 7, 2008 
 *
 */
package Armadillo.Analytics.Stat.Tiltable;

import Armadillo.Analytics.Stat.Distributions.ContinuousDistribution;

/**
 * Interface for a continuous (double) random variable <tt>X</tt> or a distribution thereof for which
 * the cumulant generating function cgf(and moment generating function mgf) 
 * is defined. Moreover it is assumed that 
 * the first and second derivatives of the cgf and mgf exist.
 * In this way the random variable is exponenially tiltable and
 * the exponentially tilted density is given by:
 * <pre>
 * pdf_{(ET)}(x,t)= pdf(x) e^{tx} / mgf(t).
 * </pre>
 * 
 * @author Manuel Reenders
 * @version CVS $Revision: 1.1 $
 * @since CVS $Date: 2010/12/07 14:44:37 $
 */
public interface IContExpTiltable extends ContinuousDistribution, IDoubleRVET {
	
	/**
	 * Returns the exponentially tilted (ET) density function <tt> pdf_{(ET)}(x,t)= pdf(x) e^{tx} / mgf(t)</tt>.
	 * 
	 * @param x the basic argument of the density function
	 * @param t the tilt argument
	 * @return the tilted density function 
	 */
	double pdfET(double x, double t);
	
}
