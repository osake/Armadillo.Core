package Armadillo.Analytics.Optimisation.Binary.Operators.LocalSearch;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ALocalSearch;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ILocalSearch;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.LocalSearchHelper;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;

/// <summary>
///   Local search operator for binary solvers.
///   Find a solution in the neighbourhood of a given individual.
///   The neighbour solution is sometimes better than the prvided individual.
/// </summary>
public class LocalSearchStdBln extends ALocalSearch
{
    private final ILocalSearch m_localSearchExpensiveBln;
    private final ILocalSearch m_localSearchSimpleBln;

    /// <summary>
    ///   Constructor
    /// </summary>
    public LocalSearchStdBln(
        HeuristicProblem heuristicProblem)
            
    {
        super(heuristicProblem);
        // initialize inner local search operators
        m_localSearchExpensiveBln = new LocalSearchExpensiveBln(heuristicProblem);
        m_localSearchSimpleBln = new LocalSearchSimpleBln(heuristicProblem);
    }

    @Override
    public void DoLocalSearch(
        Individual individual)
    {
    	RngWrapper rng = new RngWrapper();

        CheckIterations();

        if (LocalSearchHelper.CalculateExtensiveLocalSearch(
            rng,
            m_heuristicProblem,
            m_intLocaSearchIterations))
        {
            m_localSearchExpensiveBln.DoLocalSearch(individual);
        }
        else
        {
            m_localSearchSimpleBln.DoLocalSearch(individual);
        }
    }


    private void CheckIterations()
    {
        m_intLocaSearchIterations++;
        Individual bestIndividual =
            m_heuristicProblem.getPopulation().GetIndividualFromPopulation(
                m_heuristicProblem,
                0);

        if (bestIndividual != null)
        {
            if (bestIndividual.getFitness() > m_dblBestFitness)
            {
                m_dblBestFitness =
                    bestIndividual.getFitness();
                m_intLocaSearchIterations = 0;
            }
        }
    }
}
