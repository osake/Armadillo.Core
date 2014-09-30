package Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.IndividualFactories;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public class IndividualFactory extends AIndividualFactory
{
    public IndividualFactory(
        HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
    }

    @Override
    public Individual BuildRandomIndividual()
    {
        return new Individual(
            m_heuristicProblem);
    }
}