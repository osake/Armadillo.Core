package Armadillo.Analytics.Optimisation.Continuous.Operators;

import Armadillo.Analytics.Optimisation.Base.Operators.Crossover.ICrossover;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.Reproduction.AReproduction;
import Armadillo.Analytics.Optimisation.Base.Operators.Selection.ISelection;
import Armadillo.Analytics.Optimisation.Base.Operators.Selection.MixedSelection;
import Armadillo.Analytics.Optimisation.Continuous.ContinuousConstants;
import Armadillo.Analytics.Optimisation.Continuous.Operators.Crossover.DeCrossoverDbl;
import Armadillo.Analytics.Optimisation.Continuous.Operators.Crossover.EdaCrossoverDbl;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;;

public class ReproductionDblStd extends AReproduction
{
    private final ICrossover m_deCrossover;
    private final ICrossover m_edaCrossover;
    private final ISelection m_mixedSelection;

    public ReproductionDblStd(
        Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
        m_heuristicProblem = heuristicProblem;
        m_mixedSelection = new MixedSelection(heuristicProblem);
        m_deCrossover = new DeCrossoverDbl(heuristicProblem);
        m_edaCrossover = new EdaCrossoverDbl(heuristicProblem);
    }

    @Override
    public Individual DoReproduction()
    {
        RngWrapper rng = HeuristicProblem.CreateRandomGenerator();
        //
        // decide the type of reproduction. Reproduction types are
        // DE or EDA
        //
        if (rng.nextDouble() <=
            ContinuousConstants.DBL_DE_REPRODUCTION)
        {
        	Individual[] parents = SelectParents();
            return m_deCrossover.DoCrossover(rng, parents);
        }
        return m_edaCrossover.DoCrossover(rng, null);
    }

    @Override
    public void ClusterInstance(Individual individual)
    {
    }

    private Individual[] SelectParents()
    {
    	Individual[] parents = new Individual[4];

        parents[0] = m_mixedSelection.DoSelection();
        parents[1] = m_mixedSelection.DoSelection();
        parents[2] = m_mixedSelection.DoSelection();
        parents[3] = m_mixedSelection.DoSelection();

        return parents;
    }
}
