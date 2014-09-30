/* $Id: MGFandCGF.java,v 1.1 2010/12/07 14:44:34 NLMREE1 Exp $
 * 	
 * Created on Sep 1, 2010 
 *
 */
package Armadillo.Analytics.Stat.Tiltable;

/**
 * Interface for a random variable <tt>X</tt> or a distribution thereof for which
 * the cumulant generating function cgf(and moment generating function mgf) 
 * is defined. Moreover it is assumed that 
 * the first and second derivatives of the cgf and mgf exist.
 * 
 * @author 
 * @version CVS $Revision: 1.1 $
 * @since CVS $Date: 2010/12/07 14:44:34 $
 */
public interface IMGFandCGF {
	
	/**
	 * Returns the moment generating function <tt>mgf(t)</tt>. 
	 * 
	 * @param t argument of the mgf.
	 * @return the moment generating function mgf.
	 */
	double mgf(double t);
	
	/**
	 * Returns the cumulant generating function <tt>cgf(t)</tt>. 
	 * 
	 * @param t argument of the cgf.
	 * @return the cumulant generating function cgf.
	 */
	double cgf(double t);
	
	/**
	 * Returns the first derivative of moment generating function:
	 *  <tt>dmgf(t)/dt= mgf'(t)</tt>. 
	 * 
	 * @param t argument of the mgf'.
	 * @return the first derivative of the moment generating function mgf.
	 */
	double mgfPrime(double t);
	
	/**
	 * Returns the first derivative of cumulant generating function:
	 *  <tt>dcgf(t)/dt= cgf'(t)</tt>. 
	 * 
	 * @param t argument of the cgf'.
	 * @return the first derivative of the cumulant generating function cgf.
	 */
	double cgfPrime(double t);
	
	/**
	 * Returns the second derivative of moment generating function:
	 *  <tt>d^2 mgf(t)/dt^2= mgf''(t)</tt>. 
	 * 
	 * @param t argument of the mgf''.
	 * @return the second derivative of the moment generating function mgf.
	 */
	double mgfPrimePrime(double t);
	
	/**
	 * Returns the second derivative of cumulant generating function:
	 *  <tt>d^2 cgf(t)/dt^2= cgf''(t)</tt>. 
	 * 
	 * @param t argument of the cgf''.
	 * @return the second derivative of the cumulant generating function cgf.
	 */
	double cgfPrimePrime(double t);
	
	
}
