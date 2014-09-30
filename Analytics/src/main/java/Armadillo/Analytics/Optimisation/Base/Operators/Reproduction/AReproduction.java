package Armadillo.Analytics.Optimisation.Base.Operators.Reproduction;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public abstract class AReproduction implements IReproduction
{
    public double ReproductionProb()
    {
        return m_dblReproductionProb;
    }
    
    public void setReproductionProb(double value)
    {
        m_dblReproductionProb = value;
    }

    public HeuristicProblem HeuristicProblem()
    {
        return m_heuristicProblem;
    }

    protected double m_dblReproductionProb;
    protected HeuristicProblem m_heuristicProblem;


    public AReproduction(HeuristicProblem heuristicProblem)
    {
        m_heuristicProblem = heuristicProblem;
    }

    public abstract Individual DoReproduction();

    public abstract void ClusterInstance(Individual individual);

    public void Dispose()
    {
    }
}
