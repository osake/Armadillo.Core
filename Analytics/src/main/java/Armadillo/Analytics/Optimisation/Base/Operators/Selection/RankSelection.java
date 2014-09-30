package Armadillo.Analytics.Optimisation.Base.Operators.Selection;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public class RankSelection extends ASelection
{
    /// <summary>
    ///   Default tournament size
    /// </summary>
    private static final int TOURNAMENT_SIZE = 2;

    /// <summary>
    ///   Tournament size
    /// </summary>
    private final int m_intTournamentSize;

    public RankSelection(
        HeuristicProblem heuristicProblem)
    {
    	this(heuristicProblem,
                TOURNAMENT_SIZE);
    }

    public RankSelection(
        HeuristicProblem heuristicProblem,
        int intTournamentSize)
            
    {
    	super(heuristicProblem);
        m_intTournamentSize = intTournamentSize;
    }

    @Override
    public Individual DoSelection()
    {
        RngWrapper rng = HeuristicProblem.CreateRandomGenerator();

        int intNumb = rng.NextInt(0, m_heuristicProblem.PopulationSize() - 1);

        for (int i = 1; i < m_intTournamentSize; i++)
        {
            intNumb = Math.min(
                intNumb,
                rng.NextInt(0, m_heuristicProblem.PopulationSize() - 1));
        }
        return m_heuristicProblem.getPopulation().GetIndividualFromPopulation(
            m_heuristicProblem,
            intNumb);
    }
}
