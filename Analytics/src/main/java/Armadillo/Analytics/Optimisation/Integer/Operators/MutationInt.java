package Armadillo.Analytics.Optimisation.Integer.Operators;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.Mutation.AMutation;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public class MutationInt extends AMutation
{
    private static final double MUTATION_PROBABILITY = 0.5;
    private static final double MUTATION_RATE = 0.2;

    private final double m_dblMutationProbability;
    private final double m_dblMutationRate;

    /// <summary>
    ///   Default constructor
    /// </summary>
    /// <param name = "heuristicProblem"></param>
    public MutationInt(HeuristicProblem heuristicProblem)
    {
        this(heuristicProblem,
                MUTATION_RATE,
                MUTATION_PROBABILITY);
    }

    public MutationInt(
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

        int[] intNewChromosome = null;
        if (m_heuristicProblem.VariableCount() > 0)
        {
            //copy chromosome from parent
            intNewChromosome = individual.GetChromosomeCopyInt();
            //number of points to swap
            int ps = (int) (m_heuristicProblem.VariableCount()*
                            m_dblMutationRate + 1.0);
            int index;

            // swap ps randomly selected points
            for (int i = 0; i < ps; i++)
            {
                //point to be swapped
                index = rng.NextInt(0, m_heuristicProblem.VariableCount() - 1);

                //PrintToScreen.WriteLine(rng.NextDouble());

                if (rng.nextDouble() > m_dblMutationProbability)
                {
                    // do simple mutation
                    intNewChromosome[index] = rng.NextInt(
                        0, (int) m_heuristicProblem.getVariableRangesIntegerProbl()[index]);
                }
                else
                {
                    // do guided-mutation
                    intNewChromosome[index] =
                        (int) m_heuristicProblem.getGuidedConvergence().DrawGuidedConvergenceValue(
                            index,
                            rng);
                }
            }
        }

        //create new individual and return
        Individual newIndividual =
            new Individual(
                intNewChromosome,
                m_heuristicProblem);
        return newIndividual;
    }
}
