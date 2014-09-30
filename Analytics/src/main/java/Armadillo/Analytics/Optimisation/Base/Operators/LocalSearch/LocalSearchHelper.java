package Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch;

import Armadillo.Analytics.Optimisation.Base.OptimisationConstants;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;

/// <summary>
///   Local search helper
/// </summary>
public class LocalSearchHelper
{
    /// <summary>
    ///   Return true if extensive local search is to be calculated
    /// </summary>
    /// <param name = "rng"></param>
    /// <returns></returns>
    public static boolean CalculateExtensiveLocalSearch(
        RngWrapper rng,
        HeuristicProblem heuristicProblem,
        int intLocaSearchIterations)
    {
        if (intLocaSearchIterations < OptimisationConstants.EXPENSIVE_LOCAL_SERCH_ITERATIONS)
        {
            return false;
        }

        //
        // Ramdomly set an extensive/simple local search
        //
        if (rng.nextDouble() <= OptimisationConstants.DBL_EXTENSIVE_LOCAL_SEARCH ||
            heuristicProblem.getPopulation().GetIndividualFromPopulation(
                heuristicProblem,
                0) == null)
        {
            return false;
        }
        return true;
    }
}
