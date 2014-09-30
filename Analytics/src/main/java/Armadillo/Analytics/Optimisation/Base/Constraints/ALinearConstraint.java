package Armadillo.Analytics.Optimisation.Base.Constraints;

import java.util.List;

import Armadillo.Analytics.Mathematics.InequalityType;
import Armadillo.Analytics.Mathematics.MathHelper;
import Armadillo.Analytics.Optimisation.Base.DataStructures.VariableContribution;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Core.NotImplementedException;

public abstract class ALinearConstraint implements IHeuristicConstraint
{
    public double[] Coefficients()
    {
        return m_dblCoefficients;
    }

    public int[] Indexes()
    {
        return m_intIndexes;
    }

    public double Boundary;

    /// <summary>
    ///   The type of inequality
    /// </summary>
    public InequalityType Inequality;

    private double[] m_dblCoefficients;
    private double[] m_dblScaleArr;
    protected HeuristicProblem m_heuristicProblem;
    private int[] m_intIndexes;

    public ALinearConstraint(
        double[] dblCoefficients,
        int[] intIndexes,
        InequalityType inequality,
        double dblBoundary)
    {
    }

    public ALinearConstraint(
        double[] dblCoefficients,
        double[] dblScaleArr,
        int[] intIndexes,
        InequalityType inequality,
        double dblBoundary,
        HeuristicProblem heuristicProblem)
    {
        SetState(
            dblCoefficients,
            dblScaleArr,
            intIndexes,
            inequality,
            dblBoundary,
            heuristicProblem);
    }

    public List<VariableContribution> GetRankList()
    {
        throw new NotImplementedException();
    }

    public boolean CheckConstraint(Individual individual)
    {
        double dblSum = EvaluateConstraint(individual);
        return MathHelper.CheckInequality(Inequality, dblSum, Boundary);
    }

    public double EvaluateConstraint(Individual individual)
    {
        if (individual.getIndividualList() != null &&
            individual.getIndividualList().size() > 0)
        {
            individual = individual.GetIndividual(
                m_heuristicProblem.getProblemName());
        }

        double dblSum = 0;
        for (int i = 0; i < m_dblCoefficients.length; i++)
        {
            dblSum +=
                GetChromosomeValue(individual, i)*m_dblCoefficients[i]*m_dblScaleArr[i];
        }

        return dblSum;
    }

    private void SetState(
        double[] dblCoefficients,
        double[] dblScaleArr,
        int[] intIndexes,
        InequalityType inequality,
        double dblBoundary,
        HeuristicProblem heuristicProblem)
    {
        m_heuristicProblem = heuristicProblem;
        m_dblCoefficients = dblCoefficients;
        m_intIndexes = intIndexes;
        m_dblScaleArr = dblScaleArr;
        Inequality = inequality;
        Boundary = dblBoundary;

        if (m_dblScaleArr == null)
        {
            m_dblScaleArr = new double[dblCoefficients.length];
            for (int i = 0; i < dblCoefficients.length; i++)
            {
                m_dblScaleArr[i] = 1.0;
            }
        }
    }

    protected abstract double GetChromosomeValue(
        Individual individual,
        int intIndex);

    protected abstract void AddChromosomeValue(
        Individual individual,
        int intIndex,
        double dblWeight);

    protected abstract void RemoveChromosomeValue(
        Individual individual,
        int intIndex,
        double dblWeight);

    protected abstract double[] GetChromosomeCopy(
        Individual individual);

    protected abstract double GetMaxChromosomeValue(int intIndex);

    public void Dispose()
    {
        m_dblCoefficients = null;
        m_dblScaleArr = null;
        m_intIndexes = null;
        m_heuristicProblem = null;
    }
}

