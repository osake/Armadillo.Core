package Armadillo.Analytics.Optimisation.Integer.Operators.LocalSearch;

import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ALocalSearchStd;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ILocalSearch;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public class LocalSearchStdInt extends ALocalSearchStd
{
    /// <summary>
    ///   Constructors
    /// </summary>
    public LocalSearchStdInt(
        HeuristicProblem heuristicProblem)
    {
        	super(heuristicProblem);
    }

    @Override
    public ILocalSearch CreateCheapLocalSearch()
    {
        return new LocalSearchSimpleInt(m_heuristicProblem);
    }

    @Override
    public ILocalSearch CreateExpensiveLocalSearch()
    {
        return new LocalSearchExpensiveInt(m_heuristicProblem);
    }
}
