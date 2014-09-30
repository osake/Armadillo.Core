package Armadillo.Analytics.Optimisation.Binary.Operators.LocalSearch;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Optimisation.Base.DataStructures.VariableContribution;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ANearNeigLocalSearch0;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Binary.BinaryConstants;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.HCException;

public class LocalSearchExpensiveBln extends ANearNeigLocalSearch0
{
    private int m_intIterations = BinaryConstants.INT_LOCAL_SEARCH_ITERAIONS_BINARY;

    public LocalSearchExpensiveBln(HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
    }

    @Override
    public void DoLocalSearch(
        Individual individual)
    {
        RngWrapper rng = HeuristicProblem.CreateRandomGenerator();

        double[] bestReturn = new double[1];
        bestReturn[0] = m_heuristicProblem.getObjectiveFunction().Evaluate(
                individual);
        int intChromosomeLength = m_heuristicProblem.VariableCount();

        //
        // check if go forward
        //
        boolean blnGoForward = m_searchDirectionOperator.CheckGoForward(
            rng);

        List<Integer> indexList = new ArrayList<Integer>();
        List<VariableContribution> indexListRanked = new ArrayList<VariableContribution>();

        LocalSearchHelperBln.GetRankLists(
            individual,
            rng,
            intChromosomeLength,
            blnGoForward,
            m_heuristicProblem,
            indexList,
            indexListRanked);

        int intCurrentIteration = 0;

        for (VariableContribution variableContribution
            : indexListRanked)
        {
            int intIndex = variableContribution.Index;
            if (intCurrentIteration < m_intIterations)
            {
                IterateLocalSearch(
                    individual,
                    bestReturn,
                    indexList,
                    intIndex,
                    blnGoForward);
            }
            else
            {
                break;
            }
            intCurrentIteration++;
        }

        individual.Evaluate(
            false,
            false,
            false,
            m_heuristicProblem);

        if (bestReturn[0] > individual.getFitness())
        {
            throw new HCException("Error. Local search.");
        }
    }


    /// <summary>
    ///   Iterate extensive local search
    /// </summary>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    /// <param name = "constraints">
    ///   Constraints
    /// </param>
    /// <param name = "objectiveFunction">
    ///   Objective function
    /// </param>
    /// <param name = "bestReturn">
    ///   Best return
    /// </param>
    /// <param name = "oneList">
    ///   List with variables included in the solution
    /// </param>
    /// <param name = "intIndexZero">
    ///   List with variables not included in the solution
    /// </param>
    /// <param name = "cc">
    ///   Cluster class
    /// </param>
    /// <param name = "intIterations">
    ///   Number of iterations
    /// </param>
    private void IterateLocalSearch(
        Individual individual,
        double[] bestReturn,
        List<Integer> indexList,
        int intIndex,
        boolean blnGoForward)
    {
        double currentReturn = 0;
        boolean blnImprovementFound;
        boolean blnGlobalImprovement = false;
        //
        // swap the zero index by one
        //
        individual.SetChromosomeValueBln(intIndex, blnGoForward);
        int intBestIndex;
        //
        // iterate each of the one list and swap its values.
        // Then get the index which provides the best
        // fitness.
        //
        if (!m_heuristicProblem.CheckConstraints(individual))
        {
            blnImprovementFound = false;
            intBestIndex = -1;
            int intCurrentIteration = 0;
            for (int intIndexOne : indexList)
            {
                if (intCurrentIteration < m_intIterations)
                {
                    individual.SetChromosomeValueBln(intIndexOne, !blnGoForward);

                    if (m_heuristicProblem.CheckConstraints(individual))
                    {
                        currentReturn =
                            m_heuristicProblem.getObjectiveFunction().Evaluate(individual);
                        if (currentReturn > bestReturn[0])
                        {
                            intBestIndex = intIndexOne;
                            bestReturn[0] = currentReturn;
                            blnImprovementFound = true;
                            blnGlobalImprovement = true;
                        }
                    }
                    // back to original state
                    individual.SetChromosomeValueBln(intIndexOne, blnGoForward);
                }
                else
                {
                    break;
                }
                intCurrentIteration++;
            }

            if (blnImprovementFound)
            {
                // remove the one index from the list
                // and keep the zero index in the solution
                individual.SetChromosomeValueBln(intBestIndex, !blnGoForward);
                indexList.remove(intBestIndex);
            }
            else
            {
                // back to original state
                individual.SetChromosomeValueBln(intIndex, !blnGoForward);
            }
        }
        else
        {
            //
            // check if return has been improved
            //
            currentReturn =
                m_heuristicProblem.getObjectiveFunction().Evaluate(individual);
            if (currentReturn > bestReturn[0])
            {
                bestReturn[0] = currentReturn;
                blnGlobalImprovement = true;
            }
            else
            {
                // back to original state
                individual.SetChromosomeValueBln(intIndex, !blnGoForward);
            }
        }

        //
        // set global improvement
        //
        if (blnGlobalImprovement)
        {
            m_searchDirectionOperator.SetImprovementCounter(
                intIndex,
                blnGoForward);
        }
    }
}
