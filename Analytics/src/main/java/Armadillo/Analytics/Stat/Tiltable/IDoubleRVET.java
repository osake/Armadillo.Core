/* $Id: DoubleRVET.java,v 1.1 2010/12/07 14:44:34 NLMREE1 Exp $
 * 	
 * Created on Sep 2, 2010 
 *
 */
package Armadillo.Analytics.Stat.Tiltable;

/**
 * Interface for continuous random variables, 
 * whose distribution can be exponentially tilted.
 * The <tt>t</tt>-exponentially tiltable importance sampling 
 * transformation for the RV <tt>X</tt> has density:
 * <pre>
 * [ETDEF]
 * f_{X}(x)\rightarrow 
 * f^{(ET)}_{X}(x;t)=f_{X}(x)\rho_{X}^{-1}(x,t),
 * </pre>
 * with likelihood ratio <tt>\rho_{X}(x,t)</tt> given by
 * <pre>
 * [RHODEF] \rho_{X}(x,t) = \mbox{mgf}_{X}(t) \exp\left(-t x\right),
 * </pre>
 * see {@link #rho(double, double)}.
 * 
 * @author 
 * @version CVS $Revision: 1.1 $
 * @since CVS $Date: 2010/12/07 14:44:34 $
 */
public interface IDoubleRVET extends IDoubleRVIS, IMGFandCGF {}
