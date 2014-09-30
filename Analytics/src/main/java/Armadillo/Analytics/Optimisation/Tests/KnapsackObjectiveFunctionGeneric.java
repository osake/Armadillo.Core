package Armadillo.Analytics.Optimisation.Tests;

import Armadillo.Analytics.Optimisation.Base.ObjectiveFunctions.AHeuristicObjectiveFunction;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.ObjectiveFunctionType;
import Armadillo.Core.Console;
import Armadillo.Core.NotImplementedException;


public class KnapsackObjectiveFunctionGeneric extends AHeuristicObjectiveFunction
{
    public ObjectiveFunctionType ObjectiveFunctionType()
    {
        return ObjectiveFunctionType.STD_OBJECTIVE_FUNCT;
    }

    private final double[] m_dblReturnArrayContinuous;
    private final double[] m_dblReturnArrayInteger;
    private final double[] m_dblScaleArrayContinuous;
    private final double[] m_dblScaleArrayInteger;

    public KnapsackObjectiveFunctionGeneric(
        double[] dblReturnArrayInteger,
        double[] dblReturnArrayContinuous,
        double[] dblScaleArrayInteger,
        double[] dblScaleArrayContinuous)
    {
        m_dblReturnArrayInteger = dblReturnArrayInteger;
        m_dblReturnArrayContinuous = dblReturnArrayContinuous;
        m_dblScaleArrayInteger = dblScaleArrayInteger;
        m_dblScaleArrayContinuous = dblScaleArrayContinuous;
    }

    public String ObjectiveName;

    public double Evaluate()
    {
        throw new NotImplementedException();
    }

    public int VariableCount()
    {
            return m_dblReturnArrayInteger.length +
                   m_dblScaleArrayContinuous.length;
    }

    public String GetVariableDescription(int intIndex)
    {
        throw new NotImplementedException();
    }

    public double Evaluate(Individual individual)
    {
        double dblTotal = 0;
        for (Individual nestedInvidual :
            individual.getIndividualList())
        {
            //
            // integer part
            //
            if (nestedInvidual.ContainsChromosomeInt())
            {
                for (int i = 0; i < m_dblReturnArrayInteger.length; i++)
                {
                    dblTotal += nestedInvidual.GetChromosomeValueInt(i)*
                                m_dblReturnArrayInteger[i]*
                                m_dblScaleArrayInteger[i];
                }
            }

            //
            // continuous part
            //
            if (nestedInvidual.ContainsChromosomeDbl())
            {
                for (int i = 0; i < m_dblReturnArrayContinuous.length; i++)
                {
                    dblTotal += nestedInvidual.GetChromosomeValueDbl(i)*
                                m_dblReturnArrayContinuous[i]*
                                m_dblScaleArrayContinuous[i];
                }
            }
        }
        if (Double.isNaN(dblTotal) ||
            Double.isInfinite(dblTotal))
        {
            Console.writeLine("Invalid objective");
        }
        return dblTotal;
    }

    public void Dispose()
    {
        
    }

	@Override
	public String ObjectiveName() 
	{
		return ObjectiveName;
	}
}
