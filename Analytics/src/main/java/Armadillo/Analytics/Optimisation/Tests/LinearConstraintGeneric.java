package Armadillo.Analytics.Optimisation.Tests;

import Armadillo.Analytics.Mathematics.InequalityType;
import Armadillo.Analytics.Optimisation.Base.Constraints.ALinearConstraint;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Core.HCException;
import Armadillo.Core.NotImplementedException;

public class LinearConstraintGeneric extends ALinearConstraint
{
    public LinearConstraintGeneric(
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
        if(individual.getIndividualList() != null &&
            individual.getIndividualList().size() > 0)
        {
            individual = individual.GetIndividual(
                m_heuristicProblem.getProblemName());
        }

        if(individual.ContainsChromosomeDbl())
        {
            return individual.GetChromosomeValueDbl(intIndex);
        }
        if (individual.ContainsChromosomeInt())
        {
            return individual.GetChromosomeValueInt(intIndex);
        }

        throw new HCException("Chromosome type not found");
    }

    @Override
    protected void AddChromosomeValue(
        Individual individual,
        int intIndex,
        double dblWeight)
    {
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
        individual.RemoveChromosomeValueDbl(
            intIndex,
            dblWeight);
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
        throw  new NotImplementedException();
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
