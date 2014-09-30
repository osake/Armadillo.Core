package Armadillo.Analytics.Stat.Random;

import Armadillo.Analytics.Stat.Distributions.BinomialDist;
import Armadillo.Analytics.Stat.Distributions.GammaDist;
import Armadillo.Analytics.Stat.Distributions.PoissonDist;


public class RngMersenneTwister implements IRanNumGenerator {

	// counter for counting 'roughly' the number of MersenneTwisters used by a program.
    private static volatile int m_counter = 0;
    
    /**
     * The random number generator or engine of this class.
     */
    private final MersenneTwister m_mt64;

    /**
     * Constructor specifying the seed to be used by the internal/private math.statistics.random generator.
     * @param seed seed for the internal math.statistics.random generator
     */
    public RngMersenneTwister(int seed) {
        
    	m_mt64 = new MersenneTwister(seed);
        //poissonFromColt = new PoissonDist(1.0, mt64); /* lambda set to 1.0 (Doesn't really matter) */
        //binomialFromColt = new BinomialDist(1, 0.5, mt64); /* n set to 1.0 and p set to 0.5 (Doesn't really matter) */
        //gammaFromColt = new GammaDist(1.0, 1.0, mt64); /* alpha set to 1.0 and lambda set to 1.0 (Doesn't really matter) */
        m_counter++;
    }

    /**
     * Constructor. Seed is based on <tt>System.currentTimeMillis()</tt>.
     *
     */
    public RngMersenneTwister() {
        this((int) System.currentTimeMillis());
    }
    
    public double raw() {
    	return m_mt64.raw();
    }
    
    public double nextUniform() {
        return m_mt64.raw();
    }
    
    public int nextBernoulli(double p) {
    	
    	return m_mt64.raw() < p ? 1 : 0;
    }
    
    public static int getCounter() {
        return m_counter;
    }
    
    /**
     * Uses the polar Box-Mueller transformation (algorithm) with cache.
     */
    public double nextGaussian() {
       

    	// Uses polar Box-Muller transformation.
        double x, y, r, z;
        do {
            x = 2.0 * raw() - 1.0;
            y = 2.0 * raw() - 1.0;
            r = x * x + y * y;
        } while (r >= 1.0);

        z = Math.sqrt(-2.0 * Math.log(r) / r);
        
        return y * z;
    }

    public double nextGaussian(double mu, double sigma)	{
    	return (nextGaussian() * sigma + mu);
    }
    
    public int nextPoisson(double lambda) {
 
    	return PoissonDist.NextInt(lambda, m_mt64);
    }
    
    public int nextBinomial(int n, double p) {
    	
    	if(n == 1) {
    		return nextBernoulli(p);
    	} else {
    		return BinomialDist.NextInt(m_mt64, p, n);
    	}
    }
    
    public static void main(String[] args) {
        RngMersenneTwister rand = new RngMersenneTwister();

        int n = 100000000;
        
        double[] vals = new double[n];
        for (int i = 0; i < n; i++) {
            
        	//vals[i]= rand.nextBernoulli(0.02);
        	vals[i] = rand.nextPoisson(2.0);
        	//vals[i] = rand.nextBinomial(100, 0.02);
        	
        	//vals[i] = rand.nextGaussian();
          //  System.out.println("i = " + i + ", rand = " + vals[i]);
        }
    }

	@Override
	public double nextGamma(double dblAlpha, 
			double dblBeta) {
		
		return GammaDist.NextDoubleStatic(dblAlpha, dblBeta, m_mt64);
	}

	@Override
	public double nextDouble() {
		return nextUniform();
	}
}
