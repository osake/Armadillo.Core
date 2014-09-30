package Armadillo.Analytics.Optimisation.Continuous.Operators;

import Armadillo.Analytics.Optimisation.Base.OptimisationConstants;
import Armadillo.Analytics.Optimisation.Base.Operators.AGuidedConvergence;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.NotImplementedException;

/// <summary>
///   Guided convergence probabilities. New solutions are
///   generated
///   based on the probability array. A probability of
///   one means that the variable should be included in 
///   the solution. Zero means no likelihood to be included.
/// </summary>
public class GuidedConvergenceDbl extends AGuidedConvergence
{
    /// <summary>
    ///   Constructor
    /// </summary>
    public GuidedConvergenceDbl(HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
    }

    /// <summary>
    ///   Update guided convergence probabilities
    /// </summary>
    @Override
    public void UpdateGcProbabilities(HeuristicProblem heuristicProblem)
    {
        if (CheckInitialProbabilities())
        {
        	double[] dblGcProbabilityArray =
                new double[m_intVariableCount];
            for (int i = 0;
                 i < Math.min(
                     OptimisationConstants.INT_GM_POPULATION*
                     2,
                     m_heuristicProblem.getPopulation().LargePopulationSize());
                 i++)
            {
                for (int j = 0; j < m_intVariableCount; j++)
                {
                    dblGcProbabilityArray[j] +=
                        m_heuristicProblem.getPopulation().GetIndividualFromLargePopulation(
                            m_heuristicProblem,
                            i).GetChromosomeValueDbl(j);
                }
            }
            for (int j = 0; j < m_intVariableCount; j++)
            {
                m_gcProbabilityArray[j] =
                    ((1.0 - OptimisationConstants.DBL_GM_LAMBDA)*
                     m_gcProbabilityArray[j]) +
                    (OptimisationConstants.DBL_GM_LAMBDA*
                     dblGcProbabilityArray[j]/
                     ((double) OptimisationConstants.INT_GM_POPULATION*2));
            }
        }
    }

    @Override
    public double DrawGuidedConvergenceValue(
        int intIndex,
        RngWrapper rng)
    {
        throw new NotImplementedException();
    }

    /// <summary>
    ///   Initialize Guided Mutation probabilities
    /// </summary>
    @Override
    protected void InitializeGcProbabilities()
    {
        if (CheckPopulation())
        {
            m_gcProbabilityArray = new double[m_intVariableCount];
            for (int i = 0; i < m_heuristicProblem.getPopulation().LargePopulationSize(); i++)
            {
                for (int j = 0; j < m_intVariableCount; j++)
                {
                    m_gcProbabilityArray[j] +=
                        m_heuristicProblem.getPopulation().
                            GetIndividualFromLargePopulation(
                                m_heuristicProblem,
                                i).
                            GetChromosomeValueDbl(j);
                }
            }
            for (int j = 0; j < m_intVariableCount; j++)
            {
                m_gcProbabilityArray[j] /= m_heuristicProblem.getPopulation().LargePopulationSize();
            }
        }
    }

    private boolean CheckInitialProbabilities()
    {
        if (m_gcProbabilityArray == null)
        {
            InitializeGcProbabilities();
            return m_gcProbabilityArray != null;
        }

        return true;
    }


}