package Armadillo.Analytics.Optimisation.Base.ObjectiveFunctions;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Optimisation.Base.Delegates.EvaluateObjectiveDelegate;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Core.HCException;

public abstract class AHeuristicObjectiveFunction implements IHeuristicObjectiveFunction
{
	private List<EvaluateObjectiveDelegate> m_evaluateObjectiveDelegates;
	
	public AHeuristicObjectiveFunction()
	{
		m_evaluateObjectiveDelegates = new ArrayList<EvaluateObjectiveDelegate>();
	}
	
	
	@Override
	public void addEvaluateDelegate(
			EvaluateObjectiveDelegate evaluateObjectiveDelegate) 
	{
		m_evaluateObjectiveDelegates.add(evaluateObjectiveDelegate);
	}
	
	@Override
	public double Evaluate(Individual individual) 
	{
		if(m_evaluateObjectiveDelegates.size() > 1)
		{
			throw new HCException("To many objective evaluators!");
		}
		return m_evaluateObjectiveDelegates.get(0).invoke(individual);
	}
	
}
