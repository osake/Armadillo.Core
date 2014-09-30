package Armadillo.Analytics.Optimisation.Continuous.Operators.LocalSearch;

import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ALocalSearchExpensive;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ALocalSearchNm;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ILocalSearch;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Continuous.Operators.LocalSearch.NmLocalSearch.LocalSearchNmDbl;

public class LocalSearchExpensiveDbl extends ALocalSearchExpensive
{
    public LocalSearchExpensiveDbl(
        HeuristicProblem heuristicProblem) 
    {
    	super(heuristicProblem);
    }

    @Override
    public ILocalSearch CreateLocalSearchNearN()
    {
        return new LocalSearchNerestNeighbourDbl(m_heuristicProblem,
                                                 EXPENSIVE_NEIGHBOURHOOD);
    }

    @Override
    public ALocalSearchNm CreateLocalSearchNM()
    {
        return new LocalSearchNmDbl(m_heuristicProblem);
    }
}
