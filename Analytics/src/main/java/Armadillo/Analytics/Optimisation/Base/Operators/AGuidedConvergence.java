package Armadillo.Analytics.Optimisation.Base.Operators;

import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public abstract class AGuidedConvergence
{
    protected final int m_intVariableCount;
    protected double[] m_gcProbabilityArray;
    protected HeuristicProblem m_heuristicProblem;

    public AGuidedConvergence(
        HeuristicProblem heuristicProblem)
    {
        m_heuristicProblem = heuristicProblem;
        m_heuristicProblem = heuristicProblem;
        m_intVariableCount = m_heuristicProblem.VariableCount();
        InitializeGcProbabilities();
    }

    protected boolean CheckPopulation()
    {
        if (m_heuristicProblem.getPopulation().LargePopulationSize() == 0)
        {
            return false;
        }
        return m_heuristicProblem.getPopulation().GetIndividualFromLargePopulation(
            m_heuristicProblem,
            m_heuristicProblem.getPopulation().LargePopulationSize() - 1) != null;
    }

    public double GetGcProb(int intIndex)
    {
        if (m_gcProbabilityArray == null)
        {
            InitializeGcProbabilities();
        }

        if (m_gcProbabilityArray == null)
        {
            return 0.0;
        }

        return m_gcProbabilityArray[intIndex];
    }

    public abstract void UpdateGcProbabilities(HeuristicProblem heuristicProblem);

    /// <summary>
    ///   This method generates a random number value according 
    ///   to the guided convergence probabilities
    /// </summary>
    /// <param name = "intIndex"></param>
    /// <returns></returns>
    public abstract double DrawGuidedConvergenceValue(
        int intIndex,
        RngWrapper rng);

    protected abstract void InitializeGcProbabilities();

    public void Dispose()
    {
        m_gcProbabilityArray = null;
    }

}
