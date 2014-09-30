package Armadillo.Analytics.Stat.Tiltable;

import Armadillo.Analytics.Stat.Distributions.DistributionWithMassAtZero;
import Armadillo.Analytics.Stat.Distributions.TruncatedExponentialDeltaDist;
import Armadillo.Analytics.Stat.Random.IRanNumGenerator;
import Armadillo.Analytics.Stat.RandomVars.TruncatedExponentialRescaled;
import Armadillo.Core.Math.DoubleFormat;


/**
 * Wrapper or Decorator class that wraps around an instance of 
 * {@link IContExpTiltable} and 'adds mass at zero'. 
 * Standard problem with java is the fact that one cannot 
 * extends two (abstract) classes (in Scala we can extends multiple traits).
 * Here we need to make a choice whether we extend 
 * {@link AContExpTiltable} or {@link DistributionWithMassAtZero}.
 * 
 * @author
 * @version CVS $Revision: 1.2 $
 * @since CVS $Date: 2013/11/12 14:46:47 $
 */
public class TiltableWithMassAtZero extends AContExpTiltable {

	/**
	 * The exponentially tiltable distribution wrapped with a mass at zero.
	 */
	private final DistributionWithMassAtZero etDistrWithMassAtZero;

	/**
	 * The original exponenially tiltable distribution.
	 */
	private final IContExpTiltable etDistr;
	
	/**
	 * The mass (weight or probability) at x=0.
	 */
	protected final double massAtZero;
	
	/**
	 * Constructor specifying the identifier and
	 * taking a <tt>ExponentiallyTiltable</tt> to wrap with the mass at zero 
	 * (<tt>massAtZero</tt>) in [0,1).
	 * 
	 * @param id the identifier
	 * @param etDistr an exponentially tiltable distribution
	 * @param massAtZero the mass at zero
	 * @throws IllegalArgumentException if massAtZero not in [0,1)
	 */
	public TiltableWithMassAtZero(
			IContExpTiltable etDistr,
			double massAtZero) {

		this.etDistr = etDistr;
		
		this.etDistrWithMassAtZero = 
			new DistributionWithMassAtZero(etDistr, massAtZero);
		
		this.massAtZero = massAtZero;
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
	 * Returns the wrapped tiltable distribution (the original unrescaled distribution)
	 * @return  the wrapped tiltable distribution (the original unrescaled distribution)
	 */
	public IContExpTiltable getWrappedDistribution() {
		return etDistr;
	}
	
	public double pdf(double x) {
		return etDistrWithMassAtZero.pdf(x);
	}
	
	public double cdf(double x) {
		return etDistrWithMassAtZero.cdf(x);
	}
	
	public double next(IRanNumGenerator rng) {
		return etDistrWithMassAtZero.next(rng);
	}
	
	@Override
	public double mean() {
		return etDistrWithMassAtZero.mean();
	}
	
	@Override
	public double variance() {
		return etDistrWithMassAtZero.variance();
	}
	
	@Override
	public double mgf(double t) {
		return massAtZero + (1.0 - massAtZero) * etDistr.mgf(t);
	}

	@Override
	public double mgfPrime(double t) {
		return (1.0 - massAtZero) * etDistr.mgfPrime(t);
	}

	@Override
	public double mgfPrimePrime(double t) {
		return (1.0 - massAtZero) * etDistr.mgfPrimePrime(t);
	}

	
	public double next(IRanNumGenerator rng, double t) {

		/* Probability of tilted mass at zero:
		 * pdf_ET(0,t) = e^(t*0) pdf(0) / mgf(t)
		 */
		double threshold = massAtZero / mgf(t); 
		

		return (rng.raw() <= threshold) ? 0.0 : etDistr.next(rng, t);
	}
	
    public double next(IRanNumGenerator rng, double t, int n) {
    	
    	double result = 0.0;
		
		for(int i = 0; i < n; i++) {
			result += next(rng, t);
		}
		
		return result;
    }
	
	public double inf() {
		return etDistrWithMassAtZero.inf();
	}
	
	public double sup() {
		return etDistrWithMassAtZero.sup();
	}
	
	
	public double getMaxTilt() {
		return etDistr.getMaxTilt();
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(distr=" + etDistr + 
		", massAtZero=" + DoubleFormat.SCIENTIFICSHORT.format(massAtZero) + ")"; 
	}
	
	/**
	 * @param args
	 */
	
	public static void main(String[] args) {

		double cutoffL = 100000.0;
		double lambda = 5.9;
		double delta = 1.0;
		double weight = 1.0 / 3.0;
		//int nrOfBins = 50;
		// 
		TruncatedExponentialDeltaDist trExp = new TruncatedExponentialDeltaDist(lambda,
				cutoffL, delta);

		System.out.println(trExp);
		
		TruncatedExponentialRescaled trExpResc = new TruncatedExponentialRescaled(
				lambda, cutoffL);

		System.out.println(trExpResc);
		
		IContExpTiltable withMassAtZero = new TiltableWithMassAtZero(trExp, weight);

		System.out.println(withMassAtZero);
		
		IContExpTiltable withMassAtZeroResc = new TiltableWithMassAtZero(trExpResc, weight);

		System.out.println(withMassAtZeroResc);

		/*
		
		double theta = 4.0 / cutoffL;

		Distribution twisted = new TiltedDistribution(trExp, theta);

		System.out.println(twisted);
		
		Distribution twistedWithMassAtZero = new TiltedDistribution(
				withMassAtZero, theta);

		System.out.println(twisted);
		
		
		Distribution twistedResc = new TiltedDistribution(trExpResc, theta);

		System.out.println(twistedResc);
		
		
		Distribution twistedWithMassAtZeroResc = new TiltedDistribution(
				withMassAtZeroResc, theta);

		System.out.println(twistedWithMassAtZeroResc);
		
		
		System.out.println();

		for (int i = 0; i < 100; i++) {
			System.out.println("draw = "
					+ withMassAtZero.nextDoubleTilted(theta));
		}
		
		List<DistributionTester> testList = new ArrayList<DistributionTester>();

		testList.add(new DistributionTester(trExp, 0.0, cutoffL + 1.0, 
				nrOfBins));

		testList.add(new DistributionTester(withMassAtZero, 0.0, cutoffL + 1.0,
				nrOfBins));

		testList.add(new DistributionTester(twisted, 0.0, cutoffL + 1.0,
				nrOfBins));

		testList.add(new DistributionTester(trExpResc, 0.0, cutoffL + 1.0,
				nrOfBins));

		testList.add(new DistributionTester(withMassAtZeroResc, 0.0,
				cutoffL + 1.0, nrOfBins));

		testList.add(new DistributionTester(twistedWithMassAtZero, 0.0,
				cutoffL + 1.0, nrOfBins));

		testList.add(new DistributionTester(twistedResc, 0.0, cutoffL + 1.0,
				nrOfBins));

		testList.add(new DistributionTester(twistedWithMassAtZeroResc, 0.0,
				cutoffL + 1.0, nrOfBins));

		for (DistributionTester db : testList) {

			Thread th = new Thread(db);

			th.start();

		}
	*/

	}

}
