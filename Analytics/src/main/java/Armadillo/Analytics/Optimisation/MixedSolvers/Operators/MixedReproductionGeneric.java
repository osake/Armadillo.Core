package Armadillo.Analytics.Optimisation.MixedSolvers.Operators;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Optimisation.Base.EnumOptimimisationPoblemType;
import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.GpOperatorsContainer;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.Reproduction.IReproduction;
import Armadillo.Analytics.Optimisation.Base.Operators.Reproduction.ReproductionClass;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Binary.Operators.Reproduction.ReproductionBlnStd;
import Armadillo.Analytics.Optimisation.Continuous.Operators.ReproductionDblStd;
import Armadillo.Analytics.Optimisation.Gp.GpOperators.GpReproduction;
import Armadillo.Analytics.Optimisation.Integer.Operators.ReproductionIntStd;
import Armadillo.Core.Console;
import Armadillo.Core.NotImplementedException;

/// <summary>
///   Combines double and integer reproduction operators
/// </summary>
public class MixedReproductionGeneric extends ReproductionClass
{
    private final GpOperatorsContainer m_gpOperatorsContainer;

    public MixedReproductionGeneric(
        HeuristicProblem heuristicProblem,
        List<HeuristicProblem> heuristicProblems,
        GpOperatorsContainer gpOperatorsContainer)
    {
        super(heuristicProblem);
        m_gpOperatorsContainer = gpOperatorsContainer;
        LoadReproductionOperators(heuristicProblems);
    }

    @Override
    public Individual DoReproduction()
    {
    	List<IReproduction> reproductionList = new ArrayList<IReproduction>(m_reproductionList);
        //
        // create new individual
        //
    	Individual finalIndividual = new Individual(
            null,
            null,
            null,
            0,
            m_heuristicProblem);
        finalIndividual.setIndividualList(new ArrayList<Individual>());

        for (IReproduction reproduction : reproductionList)
        {
            Individual individual = reproduction.DoReproduction();
            individual.setProblemName(reproduction.HeuristicProblem().getProblemName());

            finalIndividual.getIndividualList().add(individual);
        }

        if (finalIndividual.getIndividualList().size() == 0)
        {
            Console.writeLine("IndividualList count == 0");
        }

        return finalIndividual;
    }

    private void LoadReproductionOperators(List<HeuristicProblem> heuristicProblems)
    {
        m_reproductionList = new ArrayList<IReproduction>();


        for (HeuristicProblem problem : heuristicProblems)
        {
            if (problem != null)
            {
                IReproduction currentReproduction;
                if (problem.EnumOptimimisationPoblemType() == EnumOptimimisationPoblemType.INTEGER)
                {
                    currentReproduction = new ReproductionIntStd(problem);
                }
                else if (problem.EnumOptimimisationPoblemType() == EnumOptimimisationPoblemType.BINARY)
                {
                    currentReproduction = new ReproductionBlnStd(problem);
                }
                else if (problem.EnumOptimimisationPoblemType() == EnumOptimimisationPoblemType.CONTINUOUS)
                {
                    currentReproduction = new ReproductionDblStd(problem);
                }
                else if (problem.EnumOptimimisationPoblemType() == EnumOptimimisationPoblemType.GENETIC_PROGRAMMING)
                {
                    currentReproduction = new GpReproduction(
                        problem,
                        m_gpOperatorsContainer.CrossoverProbability,
                        m_gpOperatorsContainer.MaxTreeDepthMutation,
                        m_gpOperatorsContainer.MaxTreeSize,
                        m_gpOperatorsContainer.TournamentSize,
                        m_gpOperatorsContainer);
                }
                else if (problem.EnumOptimimisationPoblemType() ==
                         EnumOptimimisationPoblemType.MIXED)
                {
                    currentReproduction = new MixedReproductionGeneric(
                        problem,
                        problem.getInnerProblemList(),
                        m_gpOperatorsContainer);
                }
                else
                {
                    throw new NotImplementedException();
                }

                m_reproductionList.add(currentReproduction);
            }
        }
    }
}
