package Armadillo.Analytics.Optimisation.Integer.Operators.LocalSearch;

import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ALocalSearchExpensive;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ALocalSearchNm;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ILocalSearch;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Integer.Operators.LocalSearch.NmLocalSearch.LocalSearchNmInt;

public class LocalSearchExpensiveInt extends ALocalSearchExpensive
{
    public LocalSearchExpensiveInt(
        HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
    }

    @Override
    public ILocalSearch CreateLocalSearchNearN()
    {
        return new LocalSearchNerestNeighbourInt(m_heuristicProblem,
                                                 EXPENSIVE_NEIGHBOURHOOD);
    }

    @Override
    public ALocalSearchNm CreateLocalSearchNM()
    {
        return new LocalSearchNmInt(m_heuristicProblem);
    }
}
