package Armadillo.Analytics.Optimisation.Base.ObjectiveFunctions;

import java.util.List;

import Armadillo.Analytics.Optimisation.Base.ObjectiveFunctionType;
import Armadillo.Analytics.Optimisation.Base.DataStructures.ResultRow;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Core.Logger;
import Armadillo.Core.NotImplementedException;

public class HeuristicMultiObjectiveFunction extends AHeuristicObjectiveFunction
{
    public String ObjectiveName()
	{
		return "HeuristicMultiObjectiveFunction";
	}

    public int ObjectiveCount()
    {
        return m_objectiveFunctions.size();
    }

    public List<IHeuristicObjectiveFunction> ObjectiveFunctions()
    {
        return m_objectiveFunctions;
    }

    private List<IHeuristicObjectiveFunction> m_objectiveFunctions;

    public HeuristicMultiObjectiveFunction(
        List<IHeuristicObjectiveFunction> objectiveFunctions)
    {
        m_objectiveFunctions = objectiveFunctions;
    }

    public ObjectiveFunctionType ObjectiveFunctionType()
    {
        return ObjectiveFunctionType.MULTI_OBJECTIVE_FUNCT;
    }

    public double Evaluate()
    {
        throw new NotImplementedException();
    }

    public int VariableCount()
    {
        return m_objectiveFunctions.get(0).VariableCount();
    }

    public String GetVariableDescription(int intIndex)
    {
        throw new NotImplementedException();
    }

    public double Evaluate(Individual individual)
    {
        double[] dblFitnessArr =
            EvaluateMultiObjective(individual);
        for (int i = 0; i < dblFitnessArr.length; i++)
        {
            individual.SetFitnessValue(
                dblFitnessArr[i],
                i);
        }

        return 0.0; // throw new NotImplementedException();
    }

    public double[] EvaluateMultiObjective(Individual individual)
    {
    	try
    	{
	        double[] dblObjectivesArray =
	            new double[ObjectiveCount()];
	
	        for (int i = 0; i < ObjectiveCount(); i++)
	        {
	        	IHeuristicObjectiveFunction objectiveFunction = m_objectiveFunctions.get(i);
	        	double dblCurrVal = objectiveFunction.Evaluate(individual);
	        	dblObjectivesArray[i] = dblCurrVal;
	        }
	        return dblObjectivesArray;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }

    public ResultRow GetResultObject(
        boolean blnGetFinalResults)
    {
        throw new NotImplementedException();
    }

    public void AddConstraint(
        IHeuristicObjectiveFunction objectiveFunction)
    {
        m_objectiveFunctions.add(objectiveFunction);
    }

    public void Dispose()
    {
        if (m_objectiveFunctions != null)
        {
            for (int i = 0; i < m_objectiveFunctions.size(); i++)
            {
                m_objectiveFunctions.get(i).Dispose();
            }
        }
    }

}