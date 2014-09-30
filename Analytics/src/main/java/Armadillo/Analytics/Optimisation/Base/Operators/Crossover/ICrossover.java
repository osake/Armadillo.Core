package Armadillo.Analytics.Optimisation.Base.Operators.Crossover;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public interface ICrossover 
{
    Individual DoCrossover(
            RngWrapper rng,
            Individual[] individuals);
}
