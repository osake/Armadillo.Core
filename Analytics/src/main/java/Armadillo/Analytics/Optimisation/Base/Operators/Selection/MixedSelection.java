package Armadillo.Analytics.Optimisation.Base.Operators.Selection;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public class MixedSelection extends ASelection
{
    private final ISelection m_randomSelection;
    private final ISelection m_rankSelection;
    private final ISelection m_tournamentSelection;

    public MixedSelection(HeuristicProblem heuristicProblem)
        
    {
    	super(heuristicProblem);
        m_tournamentSelection = new TournamentSelection(
            m_heuristicProblem);
        m_randomSelection = new RandomSelection(
            m_heuristicProblem);
        m_rankSelection = new RankSelection(
            m_heuristicProblem);
    }

    @Override
    public Individual DoSelection()
    {
        RngWrapper rng = HeuristicProblem.CreateRandomGenerator();
        ISelection selection = null;

        if (rng.nextDouble() > 0.8)
        {
            selection = m_randomSelection;
        }
        else if (rng.nextDouble() > 0.5)
        {
            selection = m_tournamentSelection;
        }
        else
        {
            selection = m_rankSelection;
        }

        return selection.DoSelection();
    }
}
