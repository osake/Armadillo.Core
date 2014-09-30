package Armadillo.Analytics.Stat.Random;

public interface IRanNumGenerator extends IRng{
	/**
	 * Returns a 64 bit uniformly distributed random number in the open unit interval 
	 * <tt>(0.0, 1.0)</tt>. Excluding 0 and 1.  
	 * 
	 * @return uniformly distributed random number in (0,1).
	 */
	double raw();
	
	/**
	 * Returns a 64 bit uniformly distributed random number in the open unit interval 
	 * <tt>(0.0, 1.0)</tt>. Excluding 0 and 1. 
	 * Should return the same as {@link #raw()}.
	 * 
	 * @return uniformly distributed random number in (0,1).
	 */
	double nextUniform();
	
	/**
	 * Returns a 64 bit Gaussian or N(0,1) distributed random number. 
	 * 
	 * @return a standard Gaussian or normally distributed random number
	 */
	double nextGaussian();
	
	/**
	 * Returns a 64 bit random number from the N(mu,sigma) normal or Gaussian
	 * distribution.
	 * @param mu the mean of the normal distribution
	 * @param sigma the standard deviation of the normal distribution 
	 * @return a normally distributed random number, with mean = mu, stdev = sigma.
	 */
	double nextGaussian(double mu, double sigma);
	
	/**
	 * Returns a 32 bit integer drawn from a Poisson distribution with mean <tt>lambda</tt>.
	 * 
	 * @param lambda the parameter or mean of the Poisson distribution (unchecked)
	 * @return a random integer, Poisson-distributed. 
	 */
	int nextPoisson(double lambda);

	/**
	 * Returns a 32 bit integer drawn from a Binomial distribution with
	 * parameters <tt>n</tt> (number of trials) and <tt>p</tt> 
	 * (probability of 'success' per trial).
	 * 
	 * @param n the trial parameter (total number of Bernoulli trials)
	 * @param p the probability parameter or Bernoulli parameter.
	 * @return a random integer, Binomial-distributed.
	 */
	int nextBinomial(int n, double p);
	
	/**
	 * Returns a 64 bit random number from a Gamma distribution with
	 * parameters <tt>alpha</tt> (the shape) and <tt>beta</tt> (the rate or inverse scale).
	 * 
	 * @param alpha the shape parameter
	 * @param beta the rate parameter (inverse scale)
	 * @return a random number from the Gamma distribution
	 */
	double nextGamma(double alpha, double beta);
	
	/**
	 * Returns a 32 bit integer drawn from a Bernoulli distribution 
	 * with probability <tt>p</tt> for returning one.
	 * 
	 * @param p the parameter of the Bernoulli distribution (unchecked)
	 * @return a random integer in <tt>{0,1}</tt>, Bernoulli-distributed. 
	 */
	int nextBernoulli(double p);

}
