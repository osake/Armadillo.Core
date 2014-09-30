package Armadillo.Analytics.Optimisation.MixedSolvers.DummyObjectiveFunctions;

import Armadillo.Analytics.Optimisation.Base.ObjectiveFunctionType;
import Armadillo.Analytics.Optimisation.Base.ObjectiveFunctions.AHeuristicObjectiveFunction;
import Armadillo.Core.HCException;

public class MixedObjectiveFunctionDummy extends AHeuristicObjectiveFunction
{
    private int m_intVariableCount;

    public String ObjectiveName;
    
    public MixedObjectiveFunctionDummy(int intVariableCount)
    {
        m_intVariableCount = intVariableCount;
    }

    public ObjectiveFunctionType ObjectiveFunctionType()
    {
        return ObjectiveFunctionType.MIXED;
    }

    public double Evaluate()
    {
        throw new HCException("");
    }

    public int VariableCount()
    {
        return m_intVariableCount;
    }

    public String GetVariableDescription(int intIndex)
    {
        throw new HCException("");
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
