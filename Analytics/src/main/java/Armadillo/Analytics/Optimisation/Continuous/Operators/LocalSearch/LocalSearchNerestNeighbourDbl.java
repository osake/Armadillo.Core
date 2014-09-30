package Armadillo.Analytics.Optimisation.Continuous.Operators.LocalSearch;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ANearNeigLocalSearch;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public class LocalSearchNerestNeighbourDbl extends ANearNeigLocalSearch
{
    public LocalSearchNerestNeighbourDbl(
        HeuristicProblem heuristicProblem,
        int intSearchIterations)
    {
        	super(heuristicProblem,
                    intSearchIterations);
    }

    @Override
    protected double GetChromosomeValue(
        Individual individual,
        int intIndex)
    {
        if (individual.getIndividualList() != null &&
            individual.getIndividualList().size() > 0)
        {
            individual = individual.GetIndividual(
                m_heuristicProblem.getProblemName());
        }

        return individual.GetChromosomeValueDbl(intIndex);
    }

    @Override
    protected void AddChromosomeValue(
        Individual individual,
        int intIndex,
        double dblWeight)
    {
        if (individual.getIndividualList() != null &&
            individual.getIndividualList().size() > 0)
        {
            individual = individual.GetIndividual(
                m_heuristicProblem.getProblemName());
        }
        individual.AddChromosomeValueDbl(
            intIndex,
            dblWeight);
    }

    @Override
    protected void RemoveChromosomeValue(
        Individual individual,
        int intIndex,
        double dblWeight)
    {
        if (individual.getIndividualList() != null &&
            individual.getIndividualList().size() > 0)
        {
            individual = individual.GetIndividual(
                m_heuristicProblem.getProblemName());
        }
        individual.RemoveChromosomeValueDbl(
            intIndex,
            dblWeight);
    }

    @Override
    protected  double[] GetChromosomeCopy(
        Individual individual)
    {
        if (individual.getIndividualList() != null &&
            individual.getIndividualList().size() > 0)
        {
            individual = individual.GetIndividual(
                m_heuristicProblem.getProblemName());
        }

        return individual.GetChromosomeCopyDbl();
    }

    @Override
    protected double GetMaxChromosomeValue(int intIndex)
    {
        return 1.0;
    }

    @Override
    protected double GetNearestNeighWeight(
        double dblChromosomeValue,
        int intIndex,
        boolean blnGoForward,
        int intScaleIndex)
    {
        double dblWeight = MUTATION_FACTOR*dblChromosomeValue;

        if (blnGoForward)
        {
            dblWeight = Math.min(
                dblWeight,
                1.0 - dblChromosomeValue);
        }

        return dblWeight;
    }
}
