package Armadillo.Analytics.Optimisation.Gp.GpOperators;

import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.GpOperatorsContainer;
import Armadillo.Analytics.Optimisation.Base.Operators.Crossover.ICrossover;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.Mutation.IMutation;
import Armadillo.Analytics.Optimisation.Base.Operators.Reproduction.AReproduction;
import Armadillo.Analytics.Optimisation.Base.Operators.Selection.ISelection;
import Armadillo.Analytics.Optimisation.Base.Operators.Selection.RankSelection;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.Console;

public class GpReproduction extends AReproduction
{
    private final ICrossover m_crossover;
    private final double m_dblCrossoverProbability;
    private final int m_intMaxTreeDepthMutation;
    private final IMutation m_mutation;
    private final ISelection m_selection;

    public GpReproduction(
        HeuristicProblem heuristicProblem,
        double dblCrossoverProbability,
        int intMaxTreeDepthMutation,
        int intMaxTreeSize,
        int intTournamentSize,
        GpOperatorsContainer gpOperatorsContainer)
    {
        super(heuristicProblem);
        m_dblCrossoverProbability = dblCrossoverProbability;
        m_intMaxTreeDepthMutation = intMaxTreeDepthMutation;
        m_heuristicProblem = heuristicProblem;

        m_selection = new RankSelection(
            heuristicProblem,
            intTournamentSize);

        m_crossover = new GpCrossover(
            heuristicProblem,
            intMaxTreeSize);

        m_mutation = new GpMutation(
            m_intMaxTreeDepthMutation,
            gpOperatorsContainer,
            m_heuristicProblem);
    }

    @Override
    public Individual DoReproduction()
    {
        RngWrapper rng = HeuristicProblem.CreateRandomGenerator();
        Individual ind1 = m_selection.DoSelection();
        Individual newIndividual = null;

        if (rng.nextDouble() <= m_dblCrossoverProbability)
        {
            Individual ind2 = m_selection.DoSelection();
            int intTrials = 0;

            boolean blnFailed = false;
            while (ind1.Equals(ind2))
            {
                ind2 = m_selection.DoSelection();

                //
                // avoid infinite loop
                // occurs when the population is the same.
                //
                if (intTrials > m_heuristicProblem.PopulationSize())
                {
                    blnFailed = true;
                    break;
                }
                intTrials++;
            }

            if (blnFailed)
            {
                newIndividual = ind1.Clone(m_heuristicProblem);
                m_mutation.DoMutation(newIndividual);
                Console.WriteLine("Failed");
            }
            else
            {
                newIndividual =
                    m_crossover.DoCrossover(
                        rng,
                        new Individual[] {ind1, ind2});
            }
        }
        else
        {
            newIndividual = ind1.Clone(m_heuristicProblem);
            m_mutation.DoMutation(newIndividual);
        }
        return newIndividual;
    }

    @Override
    public void ClusterInstance(
        Individual individual)
    {
    }
}
