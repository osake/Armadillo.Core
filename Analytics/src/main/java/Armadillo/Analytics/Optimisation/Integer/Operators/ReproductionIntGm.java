package Armadillo.Analytics.Optimisation.Integer.Operators;

import Armadillo.Analytics.Optimisation.Base.OptimisationConstants;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.Reproduction.AReproduction;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.NotImplementedException;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public class ReproductionIntGm extends AReproduction
{
    public ReproductionIntGm(HeuristicProblem heuristicProblem) 
    {
        super(heuristicProblem);
        m_heuristicProblem = heuristicProblem;
        setReproductionProb(OptimisationConstants.GUIDED_MUTATION_REPRODUCTION_PROB);
    }

    /// <summary>
    ///   Reproduce individual via guided mutation.
    ///   This operator allows quicker convergence.
    /// </summary>
    /// <param name = "repairIndividual">
    ///   Repair operator
    /// </param>
    /// <param name = "intBestChromosome">
    ///   Best chromosome found so far
    /// </param>
    /// <param name = "localSearch">
    ///   Local search operator
    /// </param>
    /// <returns>
    ///   New individual
    /// </returns>
    @Override
    public Individual DoReproduction()
    {
        RngWrapper rngWrapper = HeuristicProblem.CreateRandomGenerator();

        int[] intBestChromosome =
            m_heuristicProblem.getPopulation().GetIndividualFromPopulation(
                m_heuristicProblem,
                0).GetChromosomeCopyInt();

        int[] intNewChrosmosomeArr = new int[m_heuristicProblem.VariableCount()];

        for (int j = 0; j < m_heuristicProblem.VariableCount(); j++)
        {
            if (rngWrapper.nextDouble() <= OptimisationConstants.DBL_GM_BETA)
            {
                intNewChrosmosomeArr[j] = (int) m_heuristicProblem.getGuidedConvergence().DrawGuidedConvergenceValue(
                    j,
                    rngWrapper);
            }
            else
            {
                intNewChrosmosomeArr[j] = intBestChromosome[j];
            }
        }
        return new Individual(
            intNewChrosmosomeArr,
            m_heuristicProblem);
    }

    @Override
    public void ClusterInstance(Individual individual)
    {
        throw new NotImplementedException();
    }
}
