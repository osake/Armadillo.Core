package Armadillo.Analytics.Optimisation.Continuous.Operators.LocalSearch;

import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ALocalSearchSimple;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ILocalSearch;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public class LocalSearchSimpleDbl extends ALocalSearchSimple
{
    public LocalSearchSimpleDbl(HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
    }

    @Override
    public ILocalSearch CreateLocalSearchNearN()
    {
        return new LocalSearchNerestNeighbourDbl(
            m_heuristicProblem,
            SIMPLE_NEIGHBOURHOOD);
    }
}
