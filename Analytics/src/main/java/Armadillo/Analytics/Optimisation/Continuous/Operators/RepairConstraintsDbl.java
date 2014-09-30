package Armadillo.Analytics.Optimisation.Continuous.Operators;

import Armadillo.Analytics.Base.MathConstants;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.Repair.ARepairConstraints;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public class RepairConstraintsDbl extends ARepairConstraints
{
    public RepairConstraintsDbl(
        HeuristicProblem heuristicProblem)
    {
        super(heuristicProblem);
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
    protected boolean ValidateAddVariable(int intIndex, Individual individual)
    {
        //
        // validate values to be added
        //
        boolean blnAddVariable = false;
        if (GetChromosomeValue(individual, intIndex) <
            GetMaxChromosomeValue(intIndex) -
            MathConstants.DBL_ROUNDING_FACTOR)
        {
            blnAddVariable = true;
        }

        return blnAddVariable;
    }

    @Override
    protected boolean ValidateRemoveVariable(int intIndex, Individual individual)
    {
        boolean blnAddVariable = false;
        if (GetChromosomeValue(individual, intIndex) >
            MathConstants.DBL_ROUNDING_FACTOR)
        {
            blnAddVariable = true;
        }

        return blnAddVariable;
    }
}
