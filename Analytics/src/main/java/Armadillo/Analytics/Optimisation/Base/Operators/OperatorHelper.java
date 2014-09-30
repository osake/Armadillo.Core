package Armadillo.Analytics.Optimisation.Base.Operators;

import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public class OperatorHelper
{
    public static void UpgradeSolverOperators(
        HeuristicProblem heuristicProblem)
    {
        if (heuristicProblem.IsMultiObjective())
        {
            heuristicProblem.MultiObjectiveRanking().Rank();
        }
        else
        {
            //
            // update gc probabilities
            //
            if (heuristicProblem.getGuidedConvergence() != null)
            {
                heuristicProblem.getGuidedConvergence().UpdateGcProbabilities(
                    heuristicProblem);
            }
            //
            // Upgrade population
            //
            heuristicProblem.getPopulation().LoadPopulation();
        }
    }
}
