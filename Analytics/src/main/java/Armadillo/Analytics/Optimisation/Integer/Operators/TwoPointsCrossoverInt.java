package Armadillo.Analytics.Optimisation.Integer.Operators;

import org.apache.commons.math3.util.Precision;

import Armadillo.Analytics.Optimisation.Base.Operators.Crossover.ATwoPointsCrossover;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public class TwoPointsCrossoverInt extends ATwoPointsCrossover
{
    public TwoPointsCrossoverInt(
        HeuristicProblem heuristicProblem) 
    {
    	super(heuristicProblem);
    }

    @Override
    protected double GetChromosomeValue(
        Individual individual,
        int intIndex)
    {
        return individual.GetChromosomeValueInt(intIndex);
    }

    @Override
    protected void AddChromosomeValue(
        Individual individual,
        int intIndex,
        double dblWeight)
    {
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
        individual.RemoveChromosomeValueInt(
            intIndex,
            (int) Precision.round(dblWeight, 0),
            m_heuristicProblem);
    }

    protected double[] GetChromosomeCopy(
        Individual individual)
    {
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
    protected Individual CreateIndividual(double[] dblChromosomeArr)
    {
        int[] intChromosomeArr = new int[dblChromosomeArr.length];
        for (int i = 0; i < dblChromosomeArr.length; i++)
        {
            intChromosomeArr[i] = (int) dblChromosomeArr[i];
        }

        return new Individual(intChromosomeArr,
                              m_heuristicProblem);
    }
}
