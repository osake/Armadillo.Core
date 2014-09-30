package Armadillo.Analytics.Optimisation.MixedSolvers.Operators;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.IndividualFactories.AIndividualFactory;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public class MixedIndividualFactoryGeneric extends AIndividualFactory
{
    private final List<HeuristicProblem> m_heuristicProblemList;

    public MixedIndividualFactoryGeneric(
        HeuristicProblem heuristicProblem,
        List<HeuristicProblem> heuristicProblemList)
    {
        super(heuristicProblem);
        m_heuristicProblemList = heuristicProblemList;
    }

    @Override
    public Individual BuildRandomIndividual()
    {
    	Individual finalIndividual = new Individual(
            null,
            null,
            null,
            0,
            m_heuristicProblem);
        finalIndividual.setIndividualList(new ArrayList<Individual>());
        for (HeuristicProblem heuristicProblem : m_heuristicProblemList)
        {
            Individual individual = heuristicProblem.getIndividualFactory().BuildRandomIndividual();
            individual.setProblemName(heuristicProblem.getProblemName());

            finalIndividual.getIndividualList().add(
                individual);
        }
        return finalIndividual;
    }
}
