package Armadillo.Analytics.Optimisation.MixedSolvers.Operators;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ALocalSearch;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ILocalSearch;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Binary.Operators.LocalSearch.LocalSearchStdBln;
import Armadillo.Analytics.Optimisation.Continuous.Operators.LocalSearch.LocalSearchStdDbl;
import Armadillo.Analytics.Optimisation.Integer.Operators.LocalSearch.LocalSearchStdInt;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.NotImplementedException;


public class MixedLocalSearch extends ALocalSearch
{
    private final List<ILocalSearch> m_localSearchList;

    public MixedLocalSearch(
        HeuristicProblem heuristicProblem,
        List<HeuristicProblem> heuristicProblems)
    {
        super(heuristicProblem);
        m_localSearchList = new ArrayList<ILocalSearch>();

        for (HeuristicProblem problem : heuristicProblems)
        {
            if (problem != null)
            {
                ILocalSearch currentLocalSearch = null;
                switch (problem.EnumOptimimisationPoblemType())
                {
                    case INTEGER:
                        currentLocalSearch = new LocalSearchStdInt(problem);
                        break;
                    case BINARY:
                        currentLocalSearch = new LocalSearchStdBln(problem);
                        break;
                    case CONTINUOUS:
                        currentLocalSearch = new LocalSearchStdDbl(problem);
                        break;
                    case GENETIC_PROGRAMMING:
                        // do nothing
                        break;
                    default:
                        throw new NotImplementedException();
                }
                if (currentLocalSearch != null)
                {
                    m_localSearchList.add(currentLocalSearch);
                }
            }
        }
    }

    @Override
    public void DoLocalSearch(Individual individual)
    {
        RngWrapper rng = HeuristicProblem.CreateRandomGenerator();
        List<ILocalSearch> localSearchList = new ArrayList<ILocalSearch>(m_localSearchList);
        rng.ShuffleList(localSearchList);

        for (ILocalSearch localSearch : localSearchList)
        {
            localSearch.DoLocalSearch(individual);
        }
    }

}
