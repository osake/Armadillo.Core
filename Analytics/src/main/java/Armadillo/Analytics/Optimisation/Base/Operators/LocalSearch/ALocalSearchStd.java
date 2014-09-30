package Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;

/// <summary>
///   Local search operator for continuous optimisation problems.
///   Implements the Nelder-Mead solver for "Expensive" local search
/// </summary>
public abstract class ALocalSearchStd extends ALocalSearch
{
    private final ILocalSearch m_localSearchExpensiveDbl;
    private final ILocalSearch m_localSearchSimpleDbl;

    /// <summary>
    ///   Constructors
    /// </summary>
    public ALocalSearchStd(
        HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
        m_localSearchExpensiveDbl = CreateExpensiveLocalSearch();

        m_localSearchSimpleDbl = CreateCheapLocalSearch();
    }

    /// <summary>
    ///   Do local search
    /// </summary>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
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
            m_localSearchExpensiveDbl.DoLocalSearch(
                individual);
        }
        else
        {
            m_localSearchSimpleDbl.DoLocalSearch(
                individual);
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

    public abstract ILocalSearch CreateCheapLocalSearch();
    public abstract ILocalSearch CreateExpensiveLocalSearch();
}
