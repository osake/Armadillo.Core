package Armadillo.Analytics.Optimisation.Binary.Operators.Reproduction;

import Armadillo.Analytics.Optimisation.Base.OptimisationConstants;
import Armadillo.Analytics.Optimisation.Base.Operators.Crossover.ICrossover;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.Mutation.IMutation;
import Armadillo.Analytics.Optimisation.Base.Operators.Reproduction.AReproduction;
import Armadillo.Analytics.Optimisation.Base.Operators.Selection.ISelection;
import Armadillo.Analytics.Optimisation.Base.Operators.Selection.MixedSelection;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Binary.Operators.MutationBln;
import Armadillo.Analytics.Optimisation.Binary.Operators.TwoPointsCrossoverBln;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public class ReproductionBlnStd extends AReproduction
{
    /// <summary>
    ///   Crossover operator
    /// </summary>
    private final ICrossover m_crossover;

    /// <summary>
    ///   Crossover rate
    /// </summary>
    private final double m_dblCrossoverProb;

    /// <summary>
    ///   Mutation probability
    /// </summary>
    //private final double m_dblMutationProbability;

    private final IMutation m_mutation;

    /// <summary>
    ///   Selection operator
    /// </summary>
    private final ISelection m_selection;

    public ReproductionBlnStd(
        HeuristicProblem heuristicProblem)
    {
    	this(heuristicProblem,
                OptimisationConstants.CROSSOVER_PROB,
                OptimisationConstants.MUTATION_PROBABILITY);
    }

    /// <summary>
    ///   Constructor
    /// </summary>
    /// <param name = "dblXrate">
    ///   Crossover rate
    /// </param>
    /// <param name = "dblMutationProbability">
    ///   Mutation probability
    /// </param>
    public ReproductionBlnStd(
        HeuristicProblem heuristicProblem,
        double dblCrossoverProb,
        double dblMutationProbability)
            
    {
        super(heuristicProblem);
        m_selection = new MixedSelection(heuristicProblem);
        m_crossover = new TwoPointsCrossoverBln(heuristicProblem);
        m_mutation = new MutationBln(heuristicProblem);

        m_heuristicProblem = heuristicProblem;
        m_dblCrossoverProb = dblCrossoverProb;
        //m_dblMutationProbability = dblMutationProbability;
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
                parent2 = m_selection.DoSelection();

                //
                // avoid infinite loop
                // occurs when the population is the same.
                //
                if (intTrials > m_heuristicProblem.PopulationSize())
                {
                    break;
                }
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
