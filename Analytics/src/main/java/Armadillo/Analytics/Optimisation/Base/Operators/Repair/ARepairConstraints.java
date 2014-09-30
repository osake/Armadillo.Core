package Armadillo.Analytics.Optimisation.Base.Operators.Repair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Armadillo.Analytics.Base.MathConstants;
import Armadillo.Analytics.Optimisation.Base.OptimisationConstants;
import Armadillo.Analytics.Optimisation.Base.DataStructures.VariableContribution;
import Armadillo.Analytics.Optimisation.Base.DataStructures.VariableContributionComparator;
import Armadillo.Analytics.Optimisation.Base.Operators.SearchDirectionOperator;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.HCException;
import Armadillo.Core.NotImplementedException;

/// <summary>
///   Repair continuous solution
/// </summary>
public abstract class ARepairConstraints extends ARepair
{
    /// <summary>
    ///   Number of default iterations
    /// </summary>
    private static final int REPAIR_ITERATIONS = 3;

    private final int m_intIterations;

    private final SearchDirectionOperator m_searchDirectionOperator;

    /// <summary>
    ///   Constructor
    /// </summary>
    public ARepairConstraints(
        HeuristicProblem heuristicProblem)
    {
        super(heuristicProblem);
        m_intIterations = REPAIR_ITERATIONS;
        m_searchDirectionOperator = new SearchDirectionOperator(
            heuristicProblem);
    }

    /// <summary>
    ///   Repair solution
    /// </summary>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    /// <param name = "reproduction">
    ///   Reproduction
    /// </param>
    /// <param name = "constratints">
    ///   Constraints
    /// </param>
    /// <param name = "objectiveFunction">
    ///   Objective function
    /// </param>
    @Override
    public boolean DoRepair(
        Individual individual)
    {
        //
        // return if the constraints are satisfied
        //
        boolean blnSatisfyConstraint = m_heuristicProblem.CheckConstraints(
            individual);

        if (blnSatisfyConstraint)
        {
            return true;
        }

        double[] dblInitialChromosomeArr = GetChromosomeCopy(individual);

        RngWrapper rng = HeuristicProblem.CreateRandomGenerator();
        //
        // get array with flags indicating going either 
        // forwards or backwards in the repair
        // 
        boolean[] blnForwardList = GetForwardArr(rng);

        // get candidate list
        List<VariableContribution> candidateList =
            GetCandidateList(
                individual,
                blnForwardList,
                rng);

        // set the number of trials
        // after that the weights will be removed
        int intCurrentIteration = 0;

        Individual nestedIndividual;
        if (individual.getIndividualList() != null &&
            individual.getIndividualList().size() > 0)
        {
            nestedIndividual = individual.GetIndividual(
                m_heuristicProblem.getProblemName());
        }
        else
        {
            nestedIndividual = individual;
        }

        while (!blnSatisfyConstraint)
        {
            if (candidateList.size() > 0)
            {
                // remove a continuous ELT
                int intIndex = candidateList.get(0).Index;

                IterateRepair(
                    nestedIndividual,
                    m_intIterations,
                    intIndex,
                    intCurrentIteration,
                    blnForwardList[intIndex],
                    rng);

                blnSatisfyConstraint =
                    m_heuristicProblem.CheckConstraints(
                        individual);
                //
                // end loop if constraint is validated
                //
                if (blnSatisfyConstraint)
                {
                    //
                    // set direcion counter
                    //
                    m_searchDirectionOperator.SetImprovementCounter(
                        intIndex,
                        blnForwardList[intIndex]);
                    break;
                }
                intCurrentIteration++;
            }

            if (candidateList.size() == 0)
            {
                //
                // There are no more variables to remove from current solution.
                // Therefore, go back to original state
                //
                if (rng.nextDouble() > 0.2)
                {
                    BackToInitialState(
                        nestedIndividual,
                        dblInitialChromosomeArr);
                }

                return false;
            }

            //
            // new number list.
            // Use guided convergence in order to 
            // move to anohter less-likely variable which will be used
            // by the repair operator.
            //
            candidateList =
                GetCandidateList(
                    individual,
                    blnForwardList,
                    rng);
        }
        return true;
    }

    private boolean[] GetForwardArr(
        RngWrapper rng)
    {
        boolean[] blnForwardArr = new boolean[m_heuristicProblem.VariableCount()];
        for (int i = 0; i < m_heuristicProblem.VariableCount(); i++)
        {
            boolean blnGoForward =
                m_searchDirectionOperator.CheckGoForward(
                    i,
                    rng);
            blnForwardArr[i] = blnGoForward;
        }
        return blnForwardArr;
    }
    
    @Override
    public void AddRepairOperator(IRepair repair)
    {
        throw new NotImplementedException();
    }

    private void IterateRepair(
        Individual individual,
        int intIterations,
        int intIndex,
        int intCurrentIteration,
        boolean blnGoForward,
        RngWrapper rng)
    {
        double dblCurrentWeight = GetWeight(
            individual,
            intIterations,
            intIndex,
            intCurrentIteration,
            blnGoForward,
            rng);

        if (blnGoForward)
        {
            // add weight
            AddChromosomeValue(
                individual,
                intIndex,
                dblCurrentWeight);
        }
        else
        {
            // remove weight
            RemoveChromosomeValue(
                individual,
                intIndex,
                dblCurrentWeight);
        }
    }

    private double GetWeight(
        Individual individual,
        int intIterations,
        int intIndex,
        int intCurrentIteration,
        boolean blnGoForward,
        RngWrapper rng)
    {
        try
        {
            double dblCurrentWeight;
            //
            // Load weight.
            // after a set of trials, load the whole weight
            //
            if (intCurrentIteration >= intIterations)
            {
                dblCurrentWeight =
                    GetChromosomeValue(individual, intIndex);
                double dblMaxWeight = GetMaxChromosomeValue(intIndex);

                if (blnGoForward)
                {
                    dblCurrentWeight = dblMaxWeight - dblCurrentWeight;
                }
            }
            else
            {
                //
                // set same random weight for forward or backward
                //
                double dblCurrentChromosomeValue =
                    GetChromosomeValue(individual, intIndex);
                dblCurrentWeight = rng.nextDouble()*
                                   dblCurrentChromosomeValue;
                double dblMaxChromosomeValue = GetMaxChromosomeValue(intIndex);
                if (dblCurrentWeight + dblCurrentChromosomeValue > dblMaxChromosomeValue)
                {
                    dblCurrentWeight = dblMaxChromosomeValue - dblCurrentChromosomeValue;
                }
            }
            //
            // rounding error
            //
            if (dblCurrentWeight < MathConstants.DBL_ROUNDING_FACTOR)
            {
                dblCurrentWeight = Math.min(
                    MathConstants.DBL_ROUNDING_FACTOR,
                    GetChromosomeValue(individual, intIndex));
            }
            return dblCurrentWeight;
        }
        catch (HCException e)
        {
            //Debugger.Break();
            throw e;
        }
    }

    /// <summary>
    ///   Get candidate list of variables by removing variables
    ///   until the constraint is satisfied
    /// </summary>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    /// <param name = "reproduction">
    ///   Reproduciton
    /// </param>
    /// <returns>
    ///   Candidate list
    /// </returns>
    private List<VariableContribution> GetCandidateList(
        Individual individual,
        boolean[] blnForwardList,
        RngWrapper rng)
    {
        boolean blnUseGm = true;
        if (rng.nextDouble() >
            OptimisationConstants.DBL_USE_GM)
        {
            blnUseGm = false;
        }

        List<VariableContribution> numberList =
            new ArrayList<VariableContribution>(
                m_heuristicProblem.VariableCount() + 1);

        for (int i = 0; i < m_heuristicProblem.VariableCount(); i++)
        {
            boolean blnAddVariable = false;
            if (blnForwardList[i])
            {
                //
                // validate values to be added
                //
                blnAddVariable = ValidateAddVariable(i, individual);
            }
            else
            {
                //
                // validate values to be removed
                //
                blnAddVariable = ValidateRemoveVariable(i, individual);
            }

            if (blnAddVariable)
            {
                numberList.add(
                    new VariableContribution(i,
                                             !blnUseGm
                                                 ? rng.nextDouble()
                                                 : m_heuristicProblem.getGuidedConvergence().GetGcProb(i) -
                                                   rng.nextDouble()));
            }
        }
        Collections.sort(numberList, new VariableContributionComparator());
        // reverse the list because we are interested in 
        // removing less probable variables
        Collections.reverse(numberList);
        return numberList;
    }

    /// <summary>
    ///   Return current solution to its best state found so far
    /// </summary>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    /// <param name = "dblBestChromosomeArray">
    ///   Best chromosome
    /// </param>
    private void BackToInitialState(
        Individual individual,
        double[] dbInitialChromosomeArray)
    {
        for (int i = 0;
             i <
             m_heuristicProblem.VariableCount();
             i++)
        {
            double dblCurrentWeight = dbInitialChromosomeArray[i] -
                                   GetChromosomeValue(individual, i);
            if (dblCurrentWeight > 0)
            {
                AddChromosomeValue(
                    individual,
                    i,
                    dblCurrentWeight);
            }
            else
            {
                RemoveChromosomeValue(
                    individual,
                    i,
                    Math.abs(dblCurrentWeight));
            }
        }
    }

    protected abstract double GetChromosomeValue(
        Individual individual,
        int intIndex);

    protected abstract void AddChromosomeValue(
        Individual individual,
        int intIndex,
        double dblWeight);

    protected abstract void RemoveChromosomeValue(
        Individual individual,
        int intIndex,
        double dblWeight);

    protected abstract double[] GetChromosomeCopy(
        Individual individual);

    protected abstract double GetMaxChromosomeValue(int intIndex);

    protected abstract boolean ValidateAddVariable(int intIndex, Individual individual);

    protected abstract boolean ValidateRemoveVariable(int intIndex, Individual individual);
}
