package Armadillo.Analytics.Stat.Tiltable;

public abstract class AContExpTiltable extends ADoubleRVET implements IContExpTiltable {

	/**
	 * Returns 
	 * <pre>
	 *  pdf_{(ET)}(x,t) = pdf(x) / rho(x,t),
	 * </pre>
	 * where rho is the likelihood ratio, 
	 * <pre>
	 * rho(x,t) = exp(-t x) mgf(t).
	 * </pre>
	 */
	public double pdfET(double x, double t) {
		return pdf(x) / rho(x, t);
	}
	
	  
    /**
	 * Returns log(mgf(t)).
	 */
	public double cgf(double t) {
		
		/* perhaps it is better to compute cgf explicity */
		
		return Math.log(mgf(t)); 
	}
	

	/**
	 * Returns cgf'=mgf'(t)/mgf(t)
	 */
	public double cgfPrime(double t) {
		
		/* perhaps it is better to compute cgf' explicity */
		
		return mgfPrime(t) / mgf(t);
	}
	
	/**
	 * Returns cgf''=mgf''/mgf - (mgf')^2/mgf^2
	 */
	public double cgfPrimePrime(double t) {

		/* perhaps it is better to compute cgf'' explicity */
		
		double mgf = mgf(t);
		
		double mgfPrime = mgfPrime(t);
		
		double mgfPrimePrime = mgfPrimePrime(t);
		
		return (mgfPrimePrime/mgf - Math.pow(mgfPrime/mgf, 2.0));
		
	}
}
