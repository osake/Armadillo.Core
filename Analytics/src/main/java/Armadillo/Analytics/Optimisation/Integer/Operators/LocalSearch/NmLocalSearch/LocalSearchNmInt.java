package Armadillo.Analytics.Optimisation.Integer.Operators.LocalSearch.NmLocalSearch;

import org.apache.commons.math3.util.Precision;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ALocalSearchNm;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.NmSolver;

public class LocalSearchNmInt extends ALocalSearchNm
{
    public LocalSearchNmInt(HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
        m_nmPopulationGenerator = new NmPopulationGeneratorInt(
            heuristicProblem);
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

    @Override
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
    protected NmSolver LoadNmSolver()
    {
        return new NmSolver(
            m_heuristicProblem,
            m_nmPopulationGenerator);
    }

    @Override
    protected Individual BuildIndividual(double[] dblChromosomeArr, double dblFitness)
    {
        //
        // create int chromosome
        //
        int[] intChromosomeArr = new int[dblChromosomeArr.length];
        for (int i = 0; i < dblChromosomeArr.length; i++)
        {
            intChromosomeArr[i] = (int) Precision.round(dblChromosomeArr[i], 0);
        }

        return new Individual(
            intChromosomeArr,
            dblFitness,
            m_heuristicProblem);
    }
}
