package Armadillo.Analytics.Optimisation.Binary.Operators.Reproduction;

import Armadillo.Analytics.Optimisation.Base.OptimisationConstants;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.Reproduction.AReproduction;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.NotImplementedException;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;;


public class ReproductionBlnGm extends AReproduction
{
    public ReproductionBlnGm(HeuristicProblem heuristicProblem)
    {
        super(heuristicProblem);
        setReproductionProb(OptimisationConstants.GUIDED_MUTATION_REPRODUCTION_PROB);
    }

    @Override
    public Individual DoReproduction()
    {
        RngWrapper rng = HeuristicProblem.CreateRandomGenerator();

        boolean[] blnNewChrosmosome = new boolean[
            m_heuristicProblem.VariableCount()];
        boolean[] blnBestChromosomeArr = m_heuristicProblem.getPopulation().
            GetIndividualFromPopulation(
                m_heuristicProblem,
                0).
            GetChromosomeCopyBln();
        for (int j = 0; j < m_heuristicProblem.VariableCount(); j++)
        {
            if (rng.nextDouble() <= OptimisationConstants.DBL_GM_BETA)
            {
                if (rng.nextDouble() <=
                    m_heuristicProblem.getGuidedConvergence().GetGcProb(j))
                {
                    blnNewChrosmosome[j] = true;
                }
                else
                {
                    blnNewChrosmosome[j] = false;
                }
            }
            else
            {
                blnNewChrosmosome[j] =
                    blnBestChromosomeArr[j];
            }
        }
        return new Individual(
            blnNewChrosmosome,
            m_heuristicProblem);
    }

    @Override
    public void ClusterInstance(
        Individual individual)
    {
        throw new NotImplementedException();
    }
}
