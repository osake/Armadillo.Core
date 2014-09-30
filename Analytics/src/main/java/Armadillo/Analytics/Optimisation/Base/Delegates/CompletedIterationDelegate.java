package Armadillo.Analytics.Optimisation.Base.Delegates;

import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Core.IDelegate;
import Armadillo.Core.NotImplementedException;

public class CompletedIterationDelegate implements IDelegate
{
	public void invoke(HeuristicProblem heuristicProblem)
	{
		throw new NotImplementedException();
	}
}
