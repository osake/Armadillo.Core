/* $Id: AbstractDoubleRVET.java,v 1.1 2010/12/07 14:44:34 NLMREE1 Exp $
 * 	
 * Created on Sep 3, 2010 
 *
 */
package Armadillo.Analytics.Stat.Tiltable;

import Armadillo.Analytics.Base.FastMath;


/**
 * Abstract class implementing rho in term of the cgf,
 * the mgf and its derivatives in terms of the cgf
 * and its derivatives. The mean equals cgf' at zero tilt.
 * The variance equals the cgf'' at zero tilt.
 * 
 * @author
 * @version CVS $Revision: 1.1 $
 * @since CVS $Date: 2010/12/07 14:44:34 $
 */
public abstract class ADoubleRVET extends ADoubleRVIS 
	implements IDoubleRVET {

	
	/**
	 * Returns rho as e^(cgf - t x).
	 * Returns 1.0 if t == 0.
	 */
	public double rho(double x, double t) {
		
		if(t == 0) {
			return 1.0;
		}
		
		return FastMath.exp(cgf(t) - t * x);
	}

	/**
	 * Returns the mgf as e^cgf.
	 */
	public double mgf(double t) {
		return FastMath.exp(cgf(t));
	}

	/**
	 * Returns mgf' as cgf' * e^cfg.
	 */
	public double mgfPrime(double t) {
		return cgfPrime(t) * FastMath.exp(cgf(t));
	}
	
	/**
	 * Returns mgf'' as (cgf'' + cgf' * cgf') * e^cfg.
	 */
	public double mgfPrimePrime(double t) {
		
		double cgfPrime_tmp = cgfPrime(t);
			
		return (cgfPrimePrime(t) + 
				cgfPrime_tmp * cgfPrime_tmp) * FastMath.exp(cgf(t));
		
	}

	/**
	 * Returns cgf'(0).
	 */
	public double mean() {
		return cgfPrime(0.0);
	}

	/**
	 * Returns cgf'(t).
	 */
	public double mean(double t) {
		return cgfPrime(t);
	}
	
	/**
	 * Returns cgf''(0).
	 */
	public double variance() {
		return cgfPrimePrime(0.0);
	}

	/**
	 * Returns cgf''(t).
	 */
	public double variance(double t) {
		return cgfPrimePrime(t);
	}
}
