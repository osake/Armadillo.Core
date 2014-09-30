package Armadillo.Analytics.Stat.RandomVars;

import Armadillo.Analytics.Stat.Random.IRanNumGenerator;


public interface IDoubleRV extends Armadillo.Analytics.Stat.Sampling.IDoubleSampler {
	
	double next(IRanNumGenerator rng);
	double mean();
	double variance();
	double stdev();
	double sup();
	double inf();
}
