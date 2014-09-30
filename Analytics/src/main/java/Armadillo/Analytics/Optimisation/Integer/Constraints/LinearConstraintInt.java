package Armadillo.Analytics.Optimisation.Integer.Constraints;

import org.apache.commons.math3.util.Precision;

import Armadillo.Analytics.Mathematics.InequalityType;
import Armadillo.Analytics.Optimisation.Base.Constraints.ALinearConstraint;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Core.NotImplementedException;

public class LinearConstraintInt extends ALinearConstraint
{
    public LinearConstraintInt(
        double[] dblCoefficients,
        int[] intIndexes,
        InequalityType inequality,
        double dblBoundary)
    {
            super(
            dblCoefficients,
            intIndexes,
            inequality,
            dblBoundary);
    }

    public LinearConstraintInt(
        double[] dblCoefficients,
        double[] dblScaleArr,
        int[] intIndexes,
        InequalityType inequality,
        double dblBoundary,
        HeuristicProblem heuristicProblem)
    {
    	super(
                dblCoefficients,
                dblScaleArr,
                intIndexes,
                inequality,
                dblBoundary,
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
        throw new NotImplementedException();
    }
    
	@Override
	public InequalityType getInequality() 
	{
		return this.Inequality;
	}

	@Override
	public double getBoundary() 
	{
		return this.Boundary;
	}
}
