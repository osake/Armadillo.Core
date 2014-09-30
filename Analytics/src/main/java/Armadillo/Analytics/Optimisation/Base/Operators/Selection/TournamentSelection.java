package Armadillo.Analytics.Optimisation.Base.Operators.Selection;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public class TournamentSelection extends ASelection
{
    private static final int M_INT_K = 2; //tournament size

    public TournamentSelection(
        HeuristicProblem heuristicProblem)
            
    {
        super(heuristicProblem);
    }

    @Override
    public Individual DoSelection()
    {
        RngWrapper rng = HeuristicProblem.CreateRandomGenerator();
        int intPopSize = m_heuristicProblem.PopulationSize();

        int best = rng.NextInt(0, intPopSize - 1);
        double bestRank =
            m_heuristicProblem.getPopulation().GetIndividualFromPopulation(
                m_heuristicProblem,
                best).getFitness();

        for (int i = 1; i < M_INT_K; i++)
        {
            int competitor =
                rng.NextInt(0, intPopSize - 1);
            if (m_heuristicProblem.getPopulation().GetIndividualFromPopulation(
                m_heuristicProblem,
                competitor).getFitness() <
                bestRank)
            {
                best = competitor;
                bestRank = m_heuristicProblem.getPopulation().GetIndividualFromPopulation(
                    m_heuristicProblem,
                    competitor).getFitness();
            }
        }
        return m_heuristicProblem.getPopulation().GetIndividualFromPopulation(
            m_heuristicProblem,
            best);
    }
}
