package Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public abstract class ALocalSearchSimple extends ALocalSearch
{
    /// <summary>
    ///   Number of iterations to be computed by the neighbourhood algorithm
    /// </summary>
    protected static final int SIMPLE_NEIGHBOURHOOD = 2;

    private final ILocalSearch m_localSearchNearestNeigh;

    public ALocalSearchSimple(HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
        m_heuristicProblem = heuristicProblem;
        m_localSearchNearestNeigh = CreateLocalSearchNearN();
    }

    @Override
    public void DoLocalSearch(
        Individual individual)
    {
        m_localSearchNearestNeigh.DoLocalSearch(individual);
    }

    public abstract ILocalSearch CreateLocalSearchNearN();
}
