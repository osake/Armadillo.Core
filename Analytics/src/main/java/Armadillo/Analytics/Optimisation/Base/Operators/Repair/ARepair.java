package Armadillo.Analytics.Optimisation.Base.Operators.Repair;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public abstract class ARepair implements IRepair
{
    protected HeuristicProblem m_heuristicProblem;

    public ARepair(HeuristicProblem heuristicProblem)
    {
        m_heuristicProblem = heuristicProblem;
    }

    public abstract boolean DoRepair(Individual individual);

    public abstract void AddRepairOperator(IRepair repair);

    public void Dispose()
    {
        
    }
}
