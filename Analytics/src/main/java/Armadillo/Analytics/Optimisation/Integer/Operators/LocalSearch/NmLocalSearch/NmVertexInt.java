package Armadillo.Analytics.Optimisation.Integer.Operators.LocalSearch.NmLocalSearch;

import org.apache.commons.math3.util.Precision;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.DataStructures.ANmVertex;

public class NmVertexInt extends ANmVertex
{
    public NmVertexInt(
        Individual individual,
        HeuristicProblem heuristicProblem)
    {
        super(
            individual,
            heuristicProblem);
    }

    @Override
    protected ANmVertex CreateNmVertex()
    {
        return new NmVertexInt(
            m_individual.Clone(m_heuristicProblem),
            m_heuristicProblem);
    }

    @Override
    public void SetVertexValue(
        int intIndex,
        double dblValue)
    {
        m_dblCoordinatesArr[intIndex] = dblValue;
        SetChromosomeValue(intIndex, dblValue);
    }

    @Override
    protected void SetChromosomeValue(int intIndex, double dblValue)
    {
        Individual individual = m_individual;
        if (individual.getIndividualList() != null &&
            individual.getIndividualList().size() > 0)
        {
            individual = individual.GetIndividual(
                m_heuristicProblem.getProblemName());
        }

        int intValue =
            Math.max(0,
                     Math.min(
                         (int) m_heuristicProblem.getVariableRangesIntegerProbl()[intIndex],
                         (int) Precision.round(dblValue, 0)));

        individual.SetChromosomeValueInt(intIndex, intValue);
    }

    @Override
    protected double[] GetChromosomeCopy(Individual individual)
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
}
