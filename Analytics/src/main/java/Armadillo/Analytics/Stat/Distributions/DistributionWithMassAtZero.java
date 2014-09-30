/* $Id: DistributionWithMassAtZero.java,v 1.4 2013/05/27 08:49:12 NLIFIS1 Exp $
 * 	
 * Created on May 6, 2008 
 *
 */
package Armadillo.Analytics.Stat.Distributions;
import Armadillo.Analytics.Stat.IStats;
import Armadillo.Analytics.Stat.Random.IRanNumGenerator;
import Armadillo.Analytics.Stat.RandomVars.ADoubleRV;
import Armadillo.Core.Math.DoubleFormat;


/**
 * Wrapper or Decorator class that wraps around an instance of <tt>ContinuousDistribution</tt> 
 * and 'adds mass at zero'. In mathematical terms adding mass at zero to a density function
 * <tt>p(x)</tt> is given by:
 * <pre>
 * p_m(x) = m \delta(x) + (1-m) p(x),  
 *</pre>
 * where <tt>m</tt> is the mass at zero and <tt>p_m(x)</tt> is the rescaled version of  
 * <tt>p(x)</tt> with mass-at-zero. (<tt>delta(x)</tt> is the Dirac delta distribution).
 * <p>
 * WARNING!!! This class expects the <tt>ContinuousDistribution</tt> to be aware
 * that is will be adjusted for mass at zero. With other words, the statistics
 * of the <tt>ContinuousDistribution</tt> need to be adjusted so that the mass
 * at zero can be added to it. Rather than constructing instances of this class
 * directly, consider using {@link DistributionFactory#newWithMassAtZero(
 * math.stat.distr.factory.DistributionFactory.DistrWithMassAtZeroFactory,
 * ContinuousDistrFactory, IStats, double) newWithMassAtZero()} and corresponding
 * factory classes to create an instance of this class.
 * 
 * @author 
 * @version CVS $Revision: 1.4 $
 * @since CVS $Date: 2013/05/27 08:49:12 $
 */
public class DistributionWithMassAtZero extends ADoubleRV implements ContinuousDistribution {

	/**
	 * The distribution to be wrapped and rescaled.
	 */
	protected final ContinuousDistribution distribution;
	
	/**
	 * The mass (weight or probability) at x=0.
	 */
	protected final double massAtZero;
	
	/**
	 * Adjust the statistics in the <tt>inputStats</tt> so that the return
	 * value can be used as statistics for the underlying distribution.
	 * <p>
	 * When a <tt>ContinuousDistribution</tt> has statistics same as the return
	 * value of this method, and <tt>DistributionWithMassAtZero</tt> is created
	 * with such <tt>ContinuousDistribution</tt>, then the
	 * <tt>DistributionWithMassAtZero</tt> will have the statistics as defined
	 * in <tt>inputStats</tt>.
	 * 
	 * @param inputStats the statistics the resulting distribution will have.
	 * 			These statistics will be adjusted so that the an arbitrary
	 * 			distribution that needs to be combined with mass at zero can be
	 * 			created with the adjusted statistics and can be combined with
	 * 			the mass at zero.
	 * @param massAtZero the mass (weight or probability) at x=0
	 * @return adjusted statistics so so that the distribution with resulting
	 * 			statistics can be combined with mass at zero and that such
	 * 			combination has statistics as specified in <tt>inputStats</tt>
	 * @throws IllegalArgumentException if <tt>massAtZero</tt> is not in [0, 1)
	 */
	public static IStats adjustForMassAtZero(final IStats inputStats, final double massAtZero) {

		if(massAtZero < 0.0 || massAtZero >= 1.0) {
			throw new IllegalArgumentException("massAtZero must be in [0,1) but it is " + massAtZero);
		}

		return new IStats() {

			@Override
			public double mean() {

				return inputStats.mean() / (1 - massAtZero);
			}

			@Override
			public double stdev() {

				double mean = mean();
				double inputStdev = inputStats.stdev(); 
				
				return  Math.sqrt((inputStdev * inputStdev) / (1 - massAtZero) - massAtZero * mean * mean);
			}

			@Override
			public double inf() {

				return inputStats.inf();
			}

			@Override
			public double sup() {

				return inputStats.sup();
			}
		};
	}
	
	/**
	 * Constructor taking a <tt>ContinuousDistribution</tt>
	 * to wrap with the mass at zero (<tt>massAtZero</tt>) in [0,1).
	 * 
	 * @param id the identifier
	 * @param distribution a distribution 
	 * @param massAtZero the mass at zero
	 * @throws IllegalArgumentException if massAtZero not in [0,1).
	 */
	public DistributionWithMassAtZero(ContinuousDistribution distribution, double massAtZero) {
		
		if(distribution.inf() < 0.0) {
			throw new IllegalArgumentException(
					"Only defined for nonnegative Random Variable. " +
					"distr.inf() = " + distribution.inf() + " for " + distribution);
		}
		
		this.distribution = distribution;
		
		if(massAtZero < 0.0 || massAtZero >= 1.0) {
			throw new IllegalArgumentException("massAtZero must be in [0,1) but it is " + massAtZero);
		}
		
		this.massAtZero = massAtZero;
	}
	
	
	public double pdf(double x) {
		
		// In this pdf the delta at zero returns 1 instead of infinity.
		// if(x == 0) {
		//	return massAtZero;
		//} 
		
		// don't show the value at zero.. as it will distorts graphs that plot the pdf.
		
		return (1.0 - massAtZero) * distribution.pdf(x);
	}
	
	/**
	 * Returns infinity if x == 0. For x != 0, this functions is equal to pdf(x).
	 * 
	 * @param x the argument x to the probability density function
	 * @return the probability density function at x
	 */
	public double pdfAbstract(double x) {
		
		// In this pdf the delta at zero returns +infinity.
		if(x == 0) {
			return massAtZero * Double.POSITIVE_INFINITY;
		} 
		
		return (1.0 - massAtZero) * distribution.pdf(x);
	}
	
	public double cdf(double x) {
		
		return massAtZero + (1.0 - massAtZero) * distribution.cdf(x);
	}
	
	public double mean() {
		
		return (1.0 - massAtZero) * distribution.mean();
	}
	
	public double variance() {
		
		double distrMean = distribution.mean();
		
		return (1.0 - massAtZero) * (distribution.variance() + massAtZero * distrMean * distrMean);
	}
	
	public double next(IRanNumGenerator rng) {
		
		return (rng.raw() <= massAtZero) ? 0.0 : distribution.next(rng);
	}
	
	/**
	 * Returns zero.
	 */
	public double inf() {
		return 0.0;
	}	
	
	public double sup() {
		return distribution.sup();
	}
	
	/**
	 * Returns the mass at zero.
	 * 
	 * @return the massAtZero
	 */
	public double getMassAtZero() {
		return massAtZero;
	}
	
	/**
	 * Returns the wrapped distribution (the original unrescaled distribution)
	 * @return  the wrapped distribution (the original unrescaled distribution)
	 */
	public ContinuousDistribution getWrappedDistribution() {
		return distribution;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName()+ 
			"(distr=" + distribution +
			", massAtZero=" + DoubleFormat.SCIENTIFICSHORT.format(massAtZero) + ")";
	}
}
