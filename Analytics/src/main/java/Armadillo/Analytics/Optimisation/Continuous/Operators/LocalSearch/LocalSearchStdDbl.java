package Armadillo.Analytics.Optimisation.Continuous.Operators.LocalSearch;

import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ALocalSearchStd;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ILocalSearch;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public class LocalSearchStdDbl extends ALocalSearchStd
{
    /// <summary>
    ///   Constructors
    /// </summary>
    public LocalSearchStdDbl(
        HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
    }

    @Override
    public ILocalSearch CreateCheapLocalSearch()
    {
        return new LocalSearchSimpleDbl(m_heuristicProblem);
    }

    @Override
    public ILocalSearch CreateExpensiveLocalSearch()
    {
        return new LocalSearchExpensiveDbl(m_heuristicProblem);
    }
}
