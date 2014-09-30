package Armadillo.Analytics.Optimisation.Base.Operators.Crossover;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public abstract class ACrossover implements ICrossover
{
     protected HeuristicProblem m_heuristicProblem;

    public ACrossover(
        HeuristicProblem heuristicProblem)
    {
        m_heuristicProblem = heuristicProblem;
    }

    public abstract Individual DoCrossover(
        RngWrapper rng,
        Individual[] individuals);
}
