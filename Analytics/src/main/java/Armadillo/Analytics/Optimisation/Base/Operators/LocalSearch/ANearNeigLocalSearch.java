package Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Base.MathConstants;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.HCException;

/// <summary>
///   Local search nearest neighbourhood algorithm.
///   Search values close to the provided solution.
///   Iterates an specified number of times. 
///   Uses binary search as the search operator
/// </summary>
public abstract class ANearNeigLocalSearch extends ANearNeigLocalSearch0
{
    /// <summary>
    ///   Mutation factor applied to chormosome
    /// </summary>
    protected static final double MUTATION_FACTOR = 0.05;

    /// <summary>
    ///   Proportion of chromosomes to be mutated by the nearest neighbourhood
    /// </summary>
    private static final double MUTATION_RATE_NN = 0.5;

    /// <summary>
    ///   Factor applied to chromosome in order to seach in the neigbourhood
    /// </summary>
    private static final double NEIGHBOURHOOD_FACTOR = 0.1;

    public ANearNeigLocalSearch(
        HeuristicProblem heuristicProblem,
        int intSearchIterations)
    {
    	super(heuristicProblem);
        m_intSearchIterations = intSearchIterations;
    }

    @Override
    public void DoLocalSearch(
        Individual individual)
    {
        boolean blnCheckConstraint = m_heuristicProblem.CheckConstraints(individual);
        if (!blnCheckConstraint)
        {
            //Debugger.Break();
            throw new HCException("Error. Local search not valid.");
        }

        RngWrapper rng = HeuristicProblem.CreateRandomGenerator();
        //
        // get initial conditions
        //
        double[] dblInitialChromosomeArray = GetChromosomeCopy(individual);
        double[] dblInitialReturn = new double[1];
        dblInitialReturn[0] = m_heuristicProblem.getObjectiveFunction().Evaluate(individual);

        //
        // get shuffled index list
        //
        List<Integer> numberList = GetChromosomeIndexList(rng);

        // itereate each element of the list
        for (int i = 0;
             i < Math.min(numberList.size(), m_heuristicProblem.PopulationSize()*0.3);
             i++)
        {
            int intIndex = numberList.get(i);
            //
            // decide to go either forwards or backwards
            // for the knapsack problem, it should go forwards
            // however, for portfolio management it can go in either direction
            //
            boolean blnGoForward = m_searchDirectionOperator.CheckGoForward(
                intIndex,
                rng);

            //
            // Randomly mutate the weights in the chromosome in the opposite direction
            // this will allow the nearest neighbourhood algorithm to use the extra
            // available weight in order to explore in the neighbourhood.
            //
            if (rng.nextDouble() >= MUTATION_RATE_NN)
            {
                MutateChromosomeWeight(
                    individual,
                    intIndex,
                    rng,
                    !blnGoForward);
            }

            IterateNearestNeighbour(
                individual,
                dblInitialChromosomeArray,
                dblInitialReturn,
                intIndex,
                rng,
                blnGoForward);
        }

        blnCheckConstraint = m_heuristicProblem.CheckConstraints(individual);
        if (!blnCheckConstraint)
        {
            //Debugger.Break();
            throw new HCException("Error. Local search not valid.");
        }
    }

    private List<Integer> GetChromosomeIndexList(RngWrapper rng)
    {
    	List<Integer> numberList = new ArrayList<Integer>(
            m_heuristicProblem.VariableCount() + 1);
        for (int i = 0; i < m_heuristicProblem.VariableCount(); i++)
        {
            numberList.add(i);
        }
        // shuffle list
        rng.ShuffleList(numberList);
        return numberList;
    }

    private double GetUpperBoundWeight(
        double dblWeight,
        RngWrapper rng,
        int intIndex)
    {
        //
        // randomly decide if an upper bound is to be used
        // this allows either big or smal jumps by
        // the binary search
        //
        boolean blnUseUpperBound = rng.NextBln();

        //
        // set upper bound weight
        //
        double dblUpperBoundWeight;


        double dblMaxChromosomeValue = GetMaxChromosomeValue(intIndex);

        if (blnUseUpperBound)
        {
            dblUpperBoundWeight = (1 + NEIGHBOURHOOD_FACTOR)*dblWeight;

            if (dblUpperBoundWeight > dblMaxChromosomeValue)
            {
                dblUpperBoundWeight = dblMaxChromosomeValue;
            }
        }
        else
        {
            dblUpperBoundWeight = dblMaxChromosomeValue;
        }
        return dblUpperBoundWeight;
    }

    private static double GetLowerBoundWeight(
        double dblWeight,
        RngWrapper rng)
    {
        //
        // randomly decide if an upper bound is to be used
        //
        boolean blnUseLowerBound = rng.NextBln();

        //
        // set upper bound weight
        //
        double dblLowerBoundWeight;

        if (blnUseLowerBound)
        {
            dblLowerBoundWeight = (1.0 - NEIGHBOURHOOD_FACTOR)*dblWeight;
        }
        else
        {
            dblLowerBoundWeight = 0.0;
        }
        return dblLowerBoundWeight;
    }

    private void IterateNearestNeighbour(
        Individual individual,
        double[] dblInitialChromosomeArray,
        double[] dblBestReturn,
        int intIndex,
        RngWrapper rng,
        boolean blnGoForward)
    {
        //double[] dblInitialChromosomeArray = GetChromosomeCopy(individual);
        //
        // get weight bounds
        //
        double[] dblUpperBoundWeight = new double[1];
        double[] dblLowerBoundWeight = new double[1];
        GetLowerUpperBounds(
            individual,
            intIndex,
            rng,
            blnGoForward,
            dblUpperBoundWeight,
            dblLowerBoundWeight);

        if (Math.abs(dblUpperBoundWeight[0] - dblLowerBoundWeight[0]) <
            MathConstants.ROUND_ERROR)
        {
            //
            // Lower bound equals upper bound.
            // Therefore, not worth local search.
            //
            return;
        }

        // declare improvement flag
        boolean blnImprovement = false;
        boolean blnGlobalImprovement = false;

        //
        // iterate neighbourhood sarch
        //
        for (int intCounter = 0; intCounter < m_intSearchIterations; intCounter++)
        {
            // reset improvement
            blnImprovement = false;
            //
            // Calculate neighbour weight.
            // Similar to the binary sarch algorithm: 
            // Place the current weight at the middle of the two ranges
            //
            double dblNeighbourWeight =
                dblLowerBoundWeight[0] + (dblUpperBoundWeight[0] - dblLowerBoundWeight[0])/2.0;


            double dblWeight = dblNeighbourWeight -
                            GetChromosomeValue(individual, intIndex);

            if (Math.abs(dblWeight) >
                MathConstants.DBL_ROUNDING_FACTOR)
            {
                if (dblWeight > 0)
                {
                    AddChromosomeValue(
                        individual,
                        intIndex,
                        dblWeight);
                }
                else
                {
                    RemoveChromosomeValue(
                        individual,
                        intIndex,
                        Math.abs(dblWeight));
                }
            }

            //
            // check constraints
            //
            boolean blnCheckConstraint = m_heuristicProblem.CheckConstraints(individual);
            if (blnCheckConstraint)
            {
                double dblCurrentReturn =
                    m_heuristicProblem.getObjectiveFunction().Evaluate(
                        individual);
                if (dblCurrentReturn > dblBestReturn[0])
                {
                    //
                    // If improvement found then
                    // set the initial chromosome array to best neighbour found so far
                    //
                    dblInitialChromosomeArray = GetChromosomeCopy(individual);
                    dblBestReturn[0] = dblCurrentReturn;
                    blnImprovement = true;
                    blnGlobalImprovement = true;
                }
                else
                {
                    blnImprovement = false;
                }
            }

            // move to next point in the neighbourhood
            // depending if forward is flagged
            //
            if (blnImprovement)
            {
                if (blnGoForward)
                {
                    dblLowerBoundWeight[0] = dblNeighbourWeight;
                }
                else
                {
                    dblUpperBoundWeight[0] = dblNeighbourWeight;
                }
            }
            else
            {
                if (blnGoForward)
                {
                    dblUpperBoundWeight[0] = dblNeighbourWeight;
                }
                else
                {
                    dblLowerBoundWeight[0] = dblNeighbourWeight;
                }
            }
        }
        //
        // go back to the last successful state if no 
        // improvement is achieved in the last stages
        //
        if (!blnImprovement)
        {
            BackToInitialState(
                individual,
                dblInitialChromosomeArray);
        }
        if (blnGlobalImprovement)
        {
            m_searchDirectionOperator.SetImprovementCounter(
                intIndex,
                blnGoForward);
        }

        boolean blnCheckConstraint0 = m_heuristicProblem.CheckConstraints(individual);
        if (!blnCheckConstraint0)
        {
            //Debugger.Break();
            throw new HCException("Error. Local search not valid.");
        }
    }

    private void GetLowerUpperBounds(
        Individual individual,
        int intIndex,
        RngWrapper rng,
        boolean blnGoForward,
        double[] dblUpperBoundWeight,
        double[] dblLowerBoundWeight)
    {
        //
        // get upper and lower bound weights
        //
        if (blnGoForward)
        {
            dblUpperBoundWeight[0] =
                GetUpperBoundWeight(
                    GetChromosomeValue(individual, intIndex),
                    rng,
                    intIndex);

            dblLowerBoundWeight[0] =
                GetChromosomeValue(individual, intIndex);
        }
        else
        {
            dblUpperBoundWeight[0] =
                GetChromosomeValue(individual, intIndex);

            dblLowerBoundWeight[0] =
                GetLowerBoundWeight(
                    GetChromosomeValue(individual, intIndex),
                    rng);
        }
    }

    private void MutateChromosomeWeight(
        Individual individual,
        int intIndex,
        RngWrapper rng,
        boolean blnGoForward)
    {
        double[] dblInitialChromosomeArray = GetChromosomeCopy(individual);

        List<Integer> chromosomeIndexList =
            new ArrayList<Integer>(m_heuristicProblem.VariableCount() + 1);
        //
        // get non-zero chromosome indexes
        //
        for (int i = 0; i < m_heuristicProblem.VariableCount(); i++)
        {
            if (dblInitialChromosomeArray[i] > 0 && i != intIndex)
            {
                chromosomeIndexList.add(i);
            }
        }
        // pick one chromosome randomly and remove weight
        if (chromosomeIndexList.size() > 0)
        {
            int intRngSelectedIndex = rng.NextInt(0, chromosomeIndexList.size() - 1);
            // delete weight from current solution
            int intSelectedIndex = chromosomeIndexList.get(intRngSelectedIndex);
            // get a chromosome weight in the neighbourhood

            double dblChromosomeWeight =
                GetNearestNeighWeight(
                    GetChromosomeValue(individual, intSelectedIndex),
                    intIndex,
                    blnGoForward,
                    intSelectedIndex);

            //
            // add or remove weight to current chromosome
            //
            if (blnGoForward)
            {
                AddChromosomeValue(
                    individual,
                    intSelectedIndex,
                    dblChromosomeWeight);
            }
            else
            {
                RemoveChromosomeValue(
                    individual,
                    intSelectedIndex,
                    dblChromosomeWeight);
            }
        }
        //
        // check constraint. If constraint not satisfied, then return to 
        // origina state
        //
        boolean blnCheckConstraint = m_heuristicProblem.CheckConstraints(individual);
        if (!blnCheckConstraint)
        {
            BackToInitialState(
                individual,
                dblInitialChromosomeArray);
        }
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

    protected abstract double GetNearestNeighWeight(
        double dblChromosomeValue,
        int intIndex,
        boolean blnGoForward,
        int intScaleIndex);
}
