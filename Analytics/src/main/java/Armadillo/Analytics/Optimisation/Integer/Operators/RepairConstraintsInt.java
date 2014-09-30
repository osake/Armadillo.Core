package Armadillo.Analytics.Optimisation.Integer.Operators;

import org.apache.commons.math3.util.Precision;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.Repair.ARepairConstraints;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public class RepairConstraintsInt extends ARepairConstraints
{
    public RepairConstraintsInt(
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
    protected boolean ValidateAddVariable(int intIndex, Individual individual)
    {
        //
        // validate values to be added
        //
        boolean blnAddVariable = false;
        if (GetChromosomeValue(individual, intIndex) <
            (int) GetMaxChromosomeValue(intIndex))
        {
            blnAddVariable = true;
        }

        return blnAddVariable;
    }

    @Override
    protected boolean ValidateRemoveVariable(int intIndex, Individual individual)
    {
        boolean blnAddVariable = false;
        if ((int) GetChromosomeValue(individual, intIndex) >
            0)
        {
            blnAddVariable = true;
        }

        return blnAddVariable;
    }
}
