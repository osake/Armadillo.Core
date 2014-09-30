package Armadillo.Analytics.Optimisation.Base.Operators.Selection;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public class RandomSelection extends ASelection
{
    public RandomSelection(
        HeuristicProblem heuristicProblem) 
    {
    	super(heuristicProblem);
    }


    @Override
    public Individual DoSelection()
    {
        RngWrapper rng = HeuristicProblem.CreateRandomGenerator();
        int intIndividualIndex =
            rng.NextInt(0, m_heuristicProblem.PopulationSize() - 1);
        return m_heuristicProblem.getPopulation().GetIndividualFromPopulation(
            m_heuristicProblem,
            intIndividualIndex);
    }
}
