package Armadillo.Analytics.Optimisation.Binary.Operators.LocalSearch;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Optimisation.Base.DataStructures.VariableContribution;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ANearNeigLocalSearch0;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Binary.BinaryConstants;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public class LocalSearchSimpleBln extends ANearNeigLocalSearch0
{
    public LocalSearchSimpleBln(HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
    }

    @Override
    public void DoLocalSearch(Individual individual)
    {
        RngWrapper rng = HeuristicProblem.CreateRandomGenerator();
        double[] bestReturn = new double[1];
        bestReturn[0] =
            m_heuristicProblem.getObjectiveFunction().Evaluate(individual);
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


        int intOneCount = Math.min(indexList.size(), indexListRanked.size());
        int intTrialCount = 1;
        for (int intIndex = 0; intIndex < intOneCount; intIndex++)
        {
            IterateLocalSearch(
                individual,
                bestReturn,
                indexList,
                indexListRanked,
                intIndex,
                blnGoForward);

            if (intTrialCount >=
                BinaryConstants.INT_LOCAL_SEARCH_ITERAIONS_BINARY)
            {
                break;
            }
            //}
            intTrialCount++;
        }
    }


    /// <summary>
    ///   Iterate local search
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
    /// <param name = "oneListRanked">
    ///   List with variables in current solution
    /// </param>
    /// <param name = "zeroListRanked">
    ///   List with variables not included in the solution but
    ///   ranked by likelihood
    /// </param>
    /// <param name = "intIndex">
    ///   IIndividual index
    /// </param>
    /// <param name = "cc">
    ///   Cluster
    /// </param>
    private void IterateLocalSearch(
        Individual individual,
        double[] bestReturn,
        List<Integer> indexList,
        List<VariableContribution> indexListRanked,
        int intIndex,
        boolean blnGoForward)
    {
        boolean blnOriginalState;
        int intCurrentIndex;
        int intCurrentIndexRanked;
        double currentReturn;

        intCurrentIndex = indexList.get(intIndex);
        intCurrentIndexRanked = indexListRanked.get(intIndex).Index;

        individual.SetChromosomeValueBln(intCurrentIndexRanked, blnGoForward);

        boolean blnGlobalImprovement = false;

        if (!m_heuristicProblem.CheckConstraints(individual))
        {
            blnOriginalState = false;
            individual.SetChromosomeValueBln(intCurrentIndex, !blnGoForward);

            if (m_heuristicProblem.CheckConstraints(individual))
            {
                currentReturn =
                    m_heuristicProblem.getObjectiveFunction().Evaluate(
                        individual);

                if (currentReturn > bestReturn[0])
                {
                    bestReturn[0] = currentReturn;
                    blnGlobalImprovement = true;
                }
                else
                {
                    blnOriginalState = true;
                }
            }
            else
            {
                blnOriginalState = true;
            }
            if (blnOriginalState)
            {
                // back to original state
                individual.SetChromosomeValueBln(intCurrentIndexRanked, !blnGoForward);
                individual.SetChromosomeValueBln(intCurrentIndex, blnGoForward);
            }
        }
        else
        {
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
                individual.SetChromosomeValueBln(intCurrentIndexRanked, !blnGoForward);
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
