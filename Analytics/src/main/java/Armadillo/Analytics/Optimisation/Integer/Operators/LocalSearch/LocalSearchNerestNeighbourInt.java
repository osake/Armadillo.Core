package Armadillo.Analytics.Optimisation.Integer.Operators.LocalSearch;

import org.apache.commons.math3.util.Precision;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ANearNeigLocalSearch;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Core.HCException;

public class LocalSearchNerestNeighbourInt extends ANearNeigLocalSearch
{
    public LocalSearchNerestNeighbourInt(
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
        return individual.GetChromosomeValueInt(intIndex);
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
        individual.AddChromosomeValueInt(
            intIndex,
            (int) Precision.round(dblWeight, 0),
            m_heuristicProblem);
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
        individual.RemoveChromosomeValueInt(
            intIndex,
            (int) Precision.round(dblWeight, 0),
            m_heuristicProblem);
    }

    @Override
    protected double[] GetChromosomeCopy(
        Individual individual)
    {
        if (individual.getIndividualList() != null &&
            individual.getIndividualList().size() > 0)
        {
            individual = individual.GetIndividual(
                m_heuristicProblem.getProblemName());
        }

        int[] intChromosomeArr = individual.GetChromosomeCopyInt();
        double[] dblChromosomeArr = new double[intChromosomeArr.length];

        for (int i = 0; i < intChromosomeArr.length; i++)
        {
            dblChromosomeArr[i] = intChromosomeArr[i];
        }

        return dblChromosomeArr;
    }

    @Override
    protected double GetMaxChromosomeValue(int intIndex)
    {
        return m_heuristicProblem.getVariableRangesIntegerProbl()[intIndex];
    }

    @Override
    protected double GetNearestNeighWeight(
        double dblChromosomeValue,
        int intIndex,
        boolean blnGoForward,
        int intScaleIndex)
    {
        double dblWeight = Math.max(
            Precision.round(MUTATION_FACTOR*dblChromosomeValue, 0),
            1);

        if (blnGoForward)
        {
            dblWeight = Math.min(
                dblWeight,
                m_heuristicProblem.getVariableRangesIntegerProbl()[intScaleIndex] - dblChromosomeValue);

            //
            // check that value is in the specified range
            //
            if (dblWeight > m_heuristicProblem.getVariableRangesIntegerProbl()[intScaleIndex] ||
                dblWeight < 0)
            {
                //Debugger.Break();
                throw new HCException("Error. Value not valid: " + dblWeight);
            }
        }


        return dblWeight;
    }
}
