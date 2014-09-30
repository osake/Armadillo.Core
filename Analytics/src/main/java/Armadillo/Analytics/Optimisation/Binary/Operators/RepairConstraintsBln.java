package Armadillo.Analytics.Optimisation.Binary.Operators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Armadillo.Analytics.Base.MathConstants;
import Armadillo.Analytics.Optimisation.Base.DataStructures.VariableContribution;
import Armadillo.Analytics.Optimisation.Base.DataStructures.VariableContributionComparator;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.Repair.IRepair;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Binary.BinaryConstants;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.HCException;
import Armadillo.Core.NotImplementedException;

/// <summary>
///   Repair solution:
///   Sets the current solution into the predefined bounds by 
///   excluding elts from the given portfolio
/// </summary>
public class RepairConstraintsBln implements IRepair
{
    private final HeuristicProblem m_heuristicProblem;

    private final RngWrapper m_rngWrapper;

    /// <summary>
    ///   Repair solution
    /// </summary>
    public RepairConstraintsBln(
        HeuristicProblem heuristicProblem)
    {
        m_heuristicProblem = heuristicProblem;
        m_rngWrapper = HeuristicProblem.CreateRandomGenerator();
    }

    public HeuristicProblem HeuristicOptmizationProblem_()
    {
        return m_heuristicProblem; 
    }

    /// <summary>
    ///   Do repair
    /// </summary>
    /// <param name = "individual">
    ///   IIndividual
    public boolean DoRepair(
        Individual individual)
    {
        // do binary repair
        DoRepairBinary(false,
                       individual);
        if (!m_heuristicProblem.CheckConstraints(individual))
        {
            throw new HCException("Error. Repair operation failure.");
        }
        return true;
    }

    public void AddRepairOperator(IRepair repair)
    {
        throw new NotImplementedException();
    }

    /// <summary>
    ///   Repair binary Distributions
    /// </summary>
    /// <param name = "blnSecondProcess">
    ///   Check if it is the second process
    /// </param>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    /// <param name = "reproduction">
    ///   Reproduction
    /// </param>
    /// <param name = "constraints">
    ///   Constraints
    /// </param>
    /// <param name = "objectiveFunction">
    ///   Objective function
    /// </param>
    public void DoRepairBinary(boolean blnSecondProcess,
                               Individual individual)
    {
        if (individual.getIndividualList() != null &&
            individual.getIndividualList().size() > 0)
        {
            individual = individual.GetIndividual(
                m_heuristicProblem.getProblemName());
        }
        List<Integer> numbers = new ArrayList<Integer>(
            m_heuristicProblem.VariableCount());

        for (int i = 0; i < m_heuristicProblem.VariableCount(); i++)
        {
            numbers.add(i);
        }

        int q;
        //boolean accept = true;
        int rn;
        int numbersCount = numbers.size();

        List<Integer> listQ = new ArrayList<Integer>(numbersCount);
        for (int i = 0; i < numbersCount; i++)
        {
            rn = m_rngWrapper.NextInt(
                numbers.size());
            q = numbers.get(rn);
            numbers.remove(rn);
            if (individual.GetChromosomeValueDbl(q) >= 1.0 -
                MathConstants.DBL_ROUNDING_FACTOR)
            {
                listQ.add(q);
            }
        }
        List<Integer> removeList = new ArrayList<Integer>(); // TODO, fill this list with something, copy logic from other repair operator

        boolean blnBuildForward = false;

        // fix chromosome
        for (int index : removeList)
        {
            individual.SetChromosomeValueDbl(index, 1);
        }

        boolean blnGreedyRepair = false;
        if (m_rngWrapper.nextDouble() >=
            BinaryConstants.DBL_GREEDY_REPAIR)
        {
            blnGreedyRepair = true;
        }

        if (blnBuildForward)
        {
            //
            // Add elts until the limit is exhausted
            //
            if (blnGreedyRepair)
            {
                BuildForwardsGreedy(listQ,
                                    individual);

                if (!m_heuristicProblem.CheckConstraints(individual))
                {
                    throw new HCException("Error. Repair operation failure.");
                }
            }
            else
            {
                BuildForwards(listQ,
                              individual);

                if (!m_heuristicProblem.CheckConstraints(individual))
                {
                    throw new HCException("Error. Repair operation failure.");
                }
            }
        }
        else
        {
            if (m_heuristicProblem.CheckConstraints(individual))
            {
                // add remaining policies
                if (blnGreedyRepair)
                {
                    BuildForwardsGreedy(listQ,
                                        individual);

                    if (!m_heuristicProblem.CheckConstraints(individual))
                    {
                        throw new HCException("Error. Repair operation failure.");
                    }
                }
                else
                {
                    BuildForwards(listQ,
                                  individual);
                    if (!m_heuristicProblem.CheckConstraints(individual))
                    {
                        throw new HCException("Error. Repair operation failure.");
                    }
                }
            }
            else
            {
                //
                // Remove elts until the portfolio is in bounds
                //
                if (blnGreedyRepair)
                {
                    BuildBackwardsGreedy(removeList,
                                         individual);
                    if (!m_heuristicProblem.CheckConstraints(individual))
                    {
                        throw new HCException("Error. Repair operation failure.");
                    }
                }
                else
                {
                    BuildBackwards(removeList,
                                   individual);
                    if (!m_heuristicProblem.CheckConstraints(individual))
                    {
                        throw new HCException("Error. Repair operation failure.");
                    }
                }
            }
        }
    }

    /// <summary>
    ///   Remove Elts until the portfolio within the predefined bounds
    /// </summary>
    /// <param name = "qList">
    ///   List with candidate variables
    /// </param>
    /// <param name = "individual">
    ///   IIndividual
    ///   <param name = "reproduction">
    ///     Reproduction operator
    ///   </param>
    ///   <param name = "constraints">
    ///     Constraints
    ///   </param>
    private void BuildBackwards(
        List<Integer> qList,
        Individual individual)
    {
    	List<VariableContribution> buildList = new ArrayList<VariableContribution>();
        double dblContribution;
        for (int intIndex : qList)
        {
            if (m_heuristicProblem.getReproduction() != null)
            {
                dblContribution =
                    m_heuristicProblem.getGuidedConvergence().GetGcProb(intIndex) -
                    m_rngWrapper.nextDouble();
            }
            else
            {
                dblContribution = 1.0;
            }
            VariableContribution currentVariable = new VariableContribution(
                intIndex,
                dblContribution);
            buildList.add(currentVariable);
        }
        Collections.sort(buildList, new VariableContributionComparator());
        // reverse the list, we are interested on removing elts which are less likely
        Collections.reverse(buildList);
        for (VariableContribution currentVariable : buildList)
        {
            int intIndex = currentVariable.Index;
            individual.SetChromosomeValueDbl(intIndex, 0);

            if (m_heuristicProblem.CheckConstraints(individual))
            {
                break;
            }
        }
    }

    /// <summary>
    ///   Build backwards.
    ///   The solution is out of bounds and variables have to 
    ///   be removed from the current solution
    /// </summary>
    /// <param name = "qList">
    ///   Candidate list
    /// </param>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    /// <param name = "constraints">
    ///   Constraints
    /// </param>
    /// <param name = "objectiveFunction">
    ///   Objective function
    /// </param>
    private void BuildBackwardsGreedy(
        List<Integer> qList,
        Individual individual)
    {
        //foreach (int intIndex in qList)
        while (qList.size() > 0)
        {
            int intIndex1 = qList.get(0);
            int intIndex2 = -1;
            double dblReturn1 = CheckBackwardsIndex(intIndex1,
                                                 individual);
            double dblReturn2 = -1;
            if (qList.size() > 1)
            {
                // go to next index
                intIndex2 = qList.get(1);
                dblReturn2 = CheckBackwardsIndex(intIndex2,
                                                 individual);
            }
            if (dblReturn1 > dblReturn2 && dblReturn1 >= 0 && dblReturn2 >= 0)
            {
                individual.SetChromosomeValueDbl(intIndex2, 0);
                qList.remove(1);
            }
            else if (dblReturn2 >= dblReturn1 && dblReturn2 >= 0)
            {
                individual.SetChromosomeValueDbl(intIndex1, 0);
                qList.remove(0);
            }
            else
            {
                // neither of the two passed the test
                individual.SetChromosomeValueDbl(intIndex1, 0);
                qList.remove(0);
                if (qList.size() > 0)
                {
                    qList.remove(0);
                }
            }
            if (m_heuristicProblem.CheckConstraints(individual))
            {
                return;
            }
        }
    }

    /// <summary>
    ///   Add Elts to current portfolio until it is out of bounds
    /// </summary>
    /// <param name = "qList">
    ///   candidate list
    /// </param>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    /// <param name = "reproduction">
    ///   reproduction
    /// </param>
    /// <param name = "constraints">
    ///   Constraints
    /// </param>
    private void BuildForwards(
        List<Integer> qList,
        Individual individual)
    {
    	List<VariableContribution> buildList = new ArrayList<VariableContribution>();
        double dblContribution;
        for (int intIndex : qList)
        {
            if (m_heuristicProblem.getReproduction() != null)
            {
                dblContribution =
                    m_heuristicProblem.getGuidedConvergence().GetGcProb(intIndex) -
                    m_rngWrapper.nextDouble();
            }
            else
            {
                dblContribution = 1.0;
            }
            VariableContribution currentVariable = new VariableContribution(
                intIndex,
                dblContribution);
            buildList.add(currentVariable);
        }
        Collections.sort(buildList, new VariableContributionComparator());
        int intTrial = 0;
        int intMaxTrials = 10;

        for (VariableContribution currentVariable : buildList)
        {
            int intIndex = currentVariable.Index;
            individual.SetChromosomeValueDbl(intIndex, 1);

            if (!m_heuristicProblem.CheckConstraints(individual))
            {
                individual.SetChromosomeValueDbl(intIndex, 0);

                intTrial++;
                if (intTrial >= intMaxTrials)
                {
                    return;
                }
            }
            else
            {
                intTrial = 0;
            }
        }
    }

    /// <summary>
    ///   Build forwards. Add variables until the 
    ///   constraint is not satisfied.
    ///   The variables are added in a greedy fashion.
    /// </summary>
    /// <param name = "qList">
    ///   Candidate list
    /// </param>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    /// <param name = "constratint">
    ///   Constraint
    /// </param>
    /// <param name = "objectiveFunction">
    ///   Objective function
    /// </param>
    private void BuildForwardsGreedy(
        List<Integer> qList,
        Individual individual)
    {
        int intTrials = 0;
        int intMaxTrials = 10;
        //foreach (int intIndex in qList)
        while (qList.size() > 0)
        {
            int intIndex1 = qList.get(0);
            int intIndex2 = -1;
            double dblReturn1 = CheckForwardsIndex(intIndex1,
                                                individual);

            double dblReturn2 = -1;
            if (qList.size() > 1)
            {
                // go to next index
                intIndex2 = qList.get(1);
                dblReturn2 = CheckForwardsIndex(intIndex2,
                                                individual);
            }
            if (dblReturn1 > dblReturn2 && dblReturn1 != -1)
            {
                individual.SetChromosomeValueDbl(intIndex1, 1);
                qList.remove(0);
                intTrials = 0;
            }
            else if (dblReturn2 >= dblReturn1 && dblReturn2 != -1)
            {
                individual.SetChromosomeValueDbl(intIndex2, 1);
                qList.remove(1);
                intTrials = 0;
            }
            else
            {
                // neither of the two passed the test
                qList.remove(0);
                if (qList.size() > 0)
                {
                    qList.remove(0);
                }
                intTrials++;
                if (intTrials >= intMaxTrials)
                {
                    break;
                }
            }
        }
    }

    /// <summary>
    ///   Check the fitness by adding an index to 
    ///   a solutions
    /// </summary>
    /// <param name = "intIndex">
    ///   index
    /// </param>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    /// <param name = "constraints">
    ///   Constraints
    /// </param>
    /// <param name = "objectiveFunction">
    ///   Objective function
    /// </param>
    /// <returns>
    ///   Fitness value
    /// </returns>
    private double CheckForwardsIndex(
        int intIndex,
        Individual individual)
    {
        double dblFitness = -1;
        individual.SetChromosomeValueDbl(intIndex, 1);
        if (m_heuristicProblem.CheckConstraints(individual))
        {
            dblFitness =
                m_heuristicProblem.getObjectiveFunction().Evaluate(individual);
        }
        // remove elt from current portfolio
        individual.SetChromosomeValueDbl(intIndex, 0);
        return dblFitness;
    }

    /// <summary>
    ///   Check the fitness by removing an index to
    ///   a given individual
    /// </summary>
    /// <param name = "intIndex">
    ///   Index
    /// </param>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    /// <param name = "objectiveFunction">
    ///   Objective function
    /// </param>
    /// <returns></returns>
    private double CheckBackwardsIndex(
        int intIndex,
        Individual individual)
    {
        double dblReturn;
        individual.SetChromosomeValueDbl(intIndex, 0);
        dblReturn = m_heuristicProblem.getObjectiveFunction().Evaluate(individual);
        individual.SetChromosomeValueDbl(intIndex, 1);
        return dblReturn;
    }

    public void Dispose()
    {
        
    }
}
