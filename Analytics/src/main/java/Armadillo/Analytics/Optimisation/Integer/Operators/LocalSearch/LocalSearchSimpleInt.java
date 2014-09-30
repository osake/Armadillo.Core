package Armadillo.Analytics.Optimisation.Integer.Operators.LocalSearch;

import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ALocalSearchSimple;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ILocalSearch;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public class LocalSearchSimpleInt extends ALocalSearchSimple
{
    public LocalSearchSimpleInt(HeuristicProblem heuristicProblem)
        
    {
    	super(heuristicProblem);
    }

    @Override
    public ILocalSearch CreateLocalSearchNearN()
    {
        return new LocalSearchNerestNeighbourInt(
            m_heuristicProblem,
            SIMPLE_NEIGHBOURHOOD);
    }
}
