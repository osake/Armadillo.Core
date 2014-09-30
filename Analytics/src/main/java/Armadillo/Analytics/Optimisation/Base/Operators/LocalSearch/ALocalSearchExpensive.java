package Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public abstract class ALocalSearchExpensive extends ALocalSearch
{
    /// <summary>
    ///   Number of iterations to be computed by the neighbourhood algorithm
    /// </summary>
    protected static final int EXPENSIVE_NEIGHBOURHOOD = 4;

    private final ILocalSearch m_localSearchNerestNeighbourDbl;
    private final ALocalSearchNm m_nmLocalSearch;

    public ALocalSearchExpensive(
        HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
        m_nmLocalSearch = CreateLocalSearchNM();
        m_localSearchNerestNeighbourDbl = CreateLocalSearchNearN();
    }

    @Override
    public void DoLocalSearch(
        Individual individual)
    {
        if (m_nmLocalSearch.ValidateNmSolver(m_intLocaSearchIterations))
        {
            m_nmLocalSearch.DoLocalSearch(individual);
            m_intLocaSearchIterations = 0;
        }
        else
        {
            m_localSearchNerestNeighbourDbl.DoLocalSearch(
                individual);
        }


        m_intLocaSearchIterations++;
    }

    public abstract ILocalSearch CreateLocalSearchNearN();
    public abstract ALocalSearchNm CreateLocalSearchNM();
}
