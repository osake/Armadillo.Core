package Armadillo.Analytics.Optimisation.Binary.Operators;

import Armadillo.Analytics.Optimisation.Base.OptimisationConstants;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.Mutation.AMutation;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public class MutationBln extends AMutation
{
    private final double m_dblMutationProbability;
    private final double m_dblMutationRate;

    /// <summary>
    ///   Default constructor
    /// </summary>
    /// <param name = "heuristicProblem"></param>
    public MutationBln(HeuristicProblem heuristicProblem)
    {
        this(heuristicProblem,
                OptimisationConstants.MUTATION_RATE,
                OptimisationConstants.MUTATION_PROBABILITY);
    }

    public MutationBln(
        HeuristicProblem heuristicProblem,
        double dblMutationRate,
        double dblMutationProbability)
    {
    	super(heuristicProblem);
        m_dblMutationRate = dblMutationRate;
        m_dblMutationProbability = dblMutationProbability;
    }

    @Override
    public Individual DoMutation(
        Individual individual)
    {
        RngWrapper rng = HeuristicProblem.CreateRandomGenerator();

        boolean[] blnChromosomeArr = null;
        if (m_heuristicProblem.VariableCount() > 0)
        {
            //copy chromosome from parent
            blnChromosomeArr = individual.GetChromosomeCopyBln();
            //number of points to swap
            int ps = (int) (m_heuristicProblem.VariableCount()*
                            m_dblMutationRate + 1.0);
            int index;

            // swap ps randomly selected points
            for (int i = 0; i < ps; i++)
            {
                //point to be swapped
                index = (int) ((m_heuristicProblem.VariableCount())*
                               rng.nextDouble());

                if (rng.nextDouble() > m_dblMutationProbability)
                {
                    // do normal mutation
                    if (blnChromosomeArr[index] == false)
                    {
                        blnChromosomeArr[index] = true;
                    }
                    else
                    {
                        blnChromosomeArr[index] = false;
                    }
                }
                else
                {
                    // do guided mutation
                    if (rng.nextDouble() <=
                        m_heuristicProblem.getGuidedConvergence().GetGcProb(index))
                    {
                        blnChromosomeArr[index] = true;
                    }
                    else
                    {
                        blnChromosomeArr[index] = false;
                    }
                }
            }
        }

        //create new individual and return
        Individual newIndividual = new Individual(
            blnChromosomeArr,
            m_heuristicProblem);
        return newIndividual;
    }
}

