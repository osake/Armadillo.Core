package Armadillo.Analytics.Optimisation.Base.Delegates;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.NmSolver;
import Armadillo.Core.NotImplementedException;

public class NmImprovementFoundDelegate 
{
	public void invoke(NmSolver nmSolver, Individual bestIndividual)
	{
		throw new NotImplementedException();
	}
}
