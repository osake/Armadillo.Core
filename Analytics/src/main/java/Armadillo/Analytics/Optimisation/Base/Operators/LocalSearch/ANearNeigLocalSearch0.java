package Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch;

import Armadillo.Analytics.Optimisation.Base.Operators.SearchDirectionOperator;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public abstract class ANearNeigLocalSearch0 extends ALocalSearch
{
    protected int m_intSearchIterations;
    protected SearchDirectionOperator m_searchDirectionOperator;

    public ANearNeigLocalSearch0(
        HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
        m_searchDirectionOperator = new SearchDirectionOperator(
            heuristicProblem);
    }
}
