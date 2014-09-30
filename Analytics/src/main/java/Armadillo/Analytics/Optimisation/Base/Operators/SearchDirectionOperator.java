package Armadillo.Analytics.Optimisation.Base.Operators;

import Armadillo.Analytics.Optimisation.Base.OptimisationConstants;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Analytics.TimeSeries.ListHelper;

/// <summary>
///   This class provides the likelihood of the search direction the algorithm
///   should aim for.
/// 
///   The search direction, depending on the constraints and the objective function,
///   allows the algorithm to find promissing solutions in the neighbourhood or
///   repair solutions for contrained problems.
/// 
///   The algorithm counts the number of time an improvement has been found
///   in a certain direction. The frequency is used for the calculation of
///   a likelihood value. The method then draws a random number which is used 
///   for the decision.
/// </summary>
public class SearchDirectionOperator
{
    private final int[] m_intBackwardCounterArr;

    /// <summary>
    ///   Forward counter. Add weights to chormosomes in order to improve the
    ///   fitness
    /// </summary>
    private final int[] m_intForwardCounterArr;

    protected double m_dblSearchDirectionLikelihood;

    //private HeuristicProblem m_heuristicProblem;

    public SearchDirectionOperator(HeuristicProblem heuristicProblem)
    {
        //m_heuristicProblem = heuristicProblem;
        m_dblSearchDirectionLikelihood = OptimisationConstants.SEARCH_DIRECTION_LIKELIHOOD;
        m_intForwardCounterArr = new int[heuristicProblem.VariableCount()];
        m_intBackwardCounterArr = new int[heuristicProblem.VariableCount()];
    }

    public boolean CheckGoForward(
        int intIndex,
        RngWrapper rng)
    {
        double dblTotalWeight =
            m_intBackwardCounterArr[intIndex] + m_intForwardCounterArr[intIndex];
        double dblForwardCount = m_intForwardCounterArr[intIndex];
        double dblBackwardCount = m_intBackwardCounterArr[intIndex];

        // if there are not enough statistics, 
        // then check the overall likelihood
        if (dblForwardCount == 0 && dblBackwardCount == 0)
        {
            return CheckGoForward(rng);
        }

        return CheckGoForward0(
            rng,
            dblTotalWeight,
            dblForwardCount);
    }

    public boolean CheckGoForward(
        RngWrapper rng)
    {
        double dblForwardCount = ListHelper.sum(m_intForwardCounterArr);
        double dblTotalWeight =
        		ListHelper.sum(m_intBackwardCounterArr) + dblForwardCount;

        return CheckGoForward0(
            rng,
            dblTotalWeight,
            dblForwardCount);
    }

    public void SetImprovementCounter(
        int intIndex,
        boolean blnGoForward)
    {
        //
        // set global improvement counters
        //

        if (blnGoForward)
        {
            m_intForwardCounterArr[intIndex]++;
        }
        else
        {
            m_intBackwardCounterArr[intIndex]++;
        }
    }

    private boolean CheckGoForward0(
        RngWrapper rng,
        double dblTotalWeight,
        double dblForwardCount)
    {
        if (dblTotalWeight == 0)
        {
            //
            // select randomly 50/50 chance to go forward or backward
            //
            return rng.NextBln();
        }
        double dblRandom = rng.nextDouble();
        //
        // calculate forward likelihood
        //
        double dblForwardLikelihood = dblForwardCount/dblTotalWeight;

        dblForwardLikelihood =
            Math.min(
                m_dblSearchDirectionLikelihood,
                dblForwardLikelihood);
        dblForwardLikelihood =
            Math.max(
                1.0 - m_dblSearchDirectionLikelihood,
                dblForwardLikelihood);

        return dblRandom < dblForwardLikelihood;
    }
}
