package Armadillo.Analytics.Optimisation.Continuous.Operators.Crossover;

import Armadillo.Analytics.Optimisation.Base.Operators.Crossover.ACrossover;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Continuous.Operators.Mutation.DeMutationHelper;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public class DeCrossoverDbl extends ACrossover
{
    private static final double CR = 0.5; // Crossover probability
    private static final double DE_CR_RATE = 0.5;

    public DeCrossoverDbl(HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
    }

    /// <summary>
    ///   Receives four solutions and returns a new one generated through
    ///   differential evoluation crossover.
    /// </summary>
    /// <param name = "individuals"></param>
    /// <returns></returns>
    @Override
    public Individual DoCrossover(
        RngWrapper rng,
        Individual[] individuals)
    {
        /* Representations of the parents */
    	double[] p0 = individuals[0].GetChromosomeCopyDbl();
    	double[] p1 = individuals[1].GetChromosomeCopyDbl();
    	double[] p2 = individuals[2].GetChromosomeCopyDbl();
    	double[] p3 = individuals[3].GetChromosomeCopyDbl();

        int nVar = p0.length;

        double[] offspring = new double[nVar];
        /* random in [0, nVar - 1] */
        int iRand = rng.NextInt(nVar);

        for (int i = 0; i < nVar; i++)
        {
            /* differential crossover */
            if (rng.nextDouble() < CR || i == iRand)
            {
                double dblMutation = p3[i] - p2[i];
                dblMutation = DeMutationHelper.ValidateMutationFactor(
                    rng,
                    dblMutation);

                offspring[i] = p1[i] + DE_CR_RATE*dblMutation;
            }
            else
            {
                offspring[i] = p0[i];
            }

            /*
             * Validate crossover
             */
            if (offspring[i] > 1.0)
            {
                offspring[i] = 1.0;
            }

            if (offspring[i] < 0.0)
            {
                offspring[i] = 0.0;
            }
        }

        return new Individual(
            offspring,
            m_heuristicProblem);
    }
}
