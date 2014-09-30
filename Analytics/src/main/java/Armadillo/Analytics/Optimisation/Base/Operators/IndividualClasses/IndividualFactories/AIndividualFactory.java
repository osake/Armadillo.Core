package Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.IndividualFactories;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public abstract class AIndividualFactory implements IIndividualFactory
{
    protected HeuristicProblem m_heuristicProblem;

    public AIndividualFactory(
        HeuristicProblem heuristicProblem)
    {
        m_heuristicProblem = heuristicProblem;
    }

    public abstract Individual BuildRandomIndividual();
}
