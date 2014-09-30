package Armadillo.Analytics.Optimisation.Base.Delegates;

import java.util.List;

import Armadillo.Analytics.Optimisation.Base.DataStructures.ResultRow;
import Armadillo.Core.IDelegate;
import Armadillo.Core.NotImplementedException;

public class SolverFinishedDelegate implements IDelegate 
{
	public void invoke(List<ResultRow> resultList)
	{
		throw new NotImplementedException();
	}
}
