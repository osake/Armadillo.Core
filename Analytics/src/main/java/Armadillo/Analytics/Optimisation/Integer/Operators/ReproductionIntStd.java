package Armadillo.Analytics.Optimisation.Integer.Operators;

import Armadillo.Analytics.Optimisation.Base.OptimisationConstants;
import Armadillo.Analytics.Optimisation.Base.Operators.Crossover.ICrossover;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.Mutation.IMutation;
import Armadillo.Analytics.Optimisation.Base.Operators.Reproduction.AReproduction;
import Armadillo.Analytics.Optimisation.Base.Operators.Selection.ISelection;
import Armadillo.Analytics.Optimisation.Base.Operators.Selection.MixedSelection;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public class ReproductionIntStd extends AReproduction
{
    /// <summary>
    ///   Crossover operator
    /// </summary>
    private ICrossover m_crossover;

    /// <summary>
    ///   Crossover rate
    /// </summary>
    private final double m_dblCrossoverProb;

    private IMutation m_mutation;

    /// <summary>
    ///   Selection operator
    /// </summary>
    private ISelection m_selection;

    public ReproductionIntStd(
        HeuristicProblem heuristicProblem)
    {
        this(
                heuristicProblem,
                OptimisationConstants.CROSSOVER_PROB);
        m_selection = new MixedSelection(heuristicProblem);
        m_crossover = new TwoPointsCrossoverInt(heuristicProblem);
        m_mutation = new MutationInt(heuristicProblem);
    }

    public ReproductionIntStd(
        HeuristicProblem heuristicProblem,
        double dblCrossoverProb)
    {
        super(heuristicProblem);
        m_dblCrossoverProb = dblCrossoverProb;
        setReproductionProb(OptimisationConstants.STD_REPRODUCTION_PROB);
    }

    /// <summary>
    ///   Cluster instance
    /// </summary>
    /// <param name = "individual"></param>
    @Override
    public void ClusterInstance(
        Individual individual)
    {
        // do not cluster instance
    }


    /// <summary>
    ///   Reproduce individual via Genetic algorithm
    /// </summary>
    /// <param name = "repairIndividual">
    ///   Repair operator
    /// </param>
    /// <param name = "localSearch">
    ///   Local search
    /// </param>
    /// <returns>
    ///   New individual
    /// </returns>
    @Override
    public Individual DoReproduction()
    {
        RngWrapper rng = HeuristicProblem.CreateRandomGenerator();

        Individual parent1 = m_selection.DoSelection();
        Individual parent2;
        Individual newIndividual;
        if (rng.nextDouble() < m_dblCrossoverProb)
        {
            // do crossover
            parent2 = m_selection.DoSelection();
            int intTrials = 0;
            while (parent2.Equals(parent1))
            {
                //
                // avoid infinite loop
                // occurs when the population is the same.
                //
                if (intTrials > m_heuristicProblem.PopulationSize())
                {
                    break;
                }
                parent2 = m_selection.DoSelection();
                intTrials++;
            }
            newIndividual = m_crossover.DoCrossover(
                rng,
                new Individual[]
                    {
                        parent1,
                        parent2
                    });
        }
        else
        {
            // do mutation
            newIndividual = m_mutation.DoMutation(parent1);
        }
        return newIndividual;
    }
}
