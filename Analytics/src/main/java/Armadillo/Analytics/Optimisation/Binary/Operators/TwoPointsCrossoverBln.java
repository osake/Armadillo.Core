package Armadillo.Analytics.Optimisation.Binary.Operators;

import org.apache.commons.math3.util.Precision;

import Armadillo.Analytics.Optimisation.Base.Operators.Crossover.ATwoPointsCrossover;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Core.NotImplementedException;

public class TwoPointsCrossoverBln extends ATwoPointsCrossover
{
    public TwoPointsCrossoverBln(
        HeuristicProblem heuristicProblem)
            
    {
    	super(heuristicProblem);
    }

    @Override
    protected double GetChromosomeValue(
        Individual individual,
        int intIndex)
    {
        return individual.GetChromosomeValueBln(intIndex) ? 1 : 0;
    }

    @Override
    protected void AddChromosomeValue(
        Individual individual,
        int intIndex,
        double dblWeight)
    {
        throw new NotImplementedException();
    }

    @Override
    protected void RemoveChromosomeValue(
        Individual individual,
        int intIndex,
        double dblWeight)
    {
        throw new NotImplementedException();
    }

    @Override
    protected double[] GetChromosomeCopy(
        Individual individual)
    {
        boolean[] blnChromosomeArr = individual.GetChromosomeCopyBln();
        double[] dblChromosomeArr = new double[blnChromosomeArr.length];


        for (int i = 0; i < blnChromosomeArr.length; i++)
        {
            dblChromosomeArr[i] = blnChromosomeArr[i] ? 1 : 0;
        }

        return dblChromosomeArr;
    }

    @Override
    protected double GetMaxChromosomeValue(int intIndex)
    {
        return 1.0;
    }

    @Override
    protected  Individual CreateIndividual(double[] dblChromosomeArr)
    {
        boolean[] blnChromosomeArr = new boolean[dblChromosomeArr.length];
        for (int i = 0; i < dblChromosomeArr.length; i++)
        {
            blnChromosomeArr[i] = Precision.round(dblChromosomeArr[i], 0) > 0;
        }

        return new Individual(blnChromosomeArr,
                              m_heuristicProblem);
    }
}
