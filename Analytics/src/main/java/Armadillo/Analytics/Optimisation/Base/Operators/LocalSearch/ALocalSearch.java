package Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Optimisation.Base.Delegates.ImprovementFoundDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.UpdateProgressDelegate;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Core.Logger;

public abstract class ALocalSearch implements ILocalSearch
{
    public double LocalSearchProb;

    protected double m_dblBestFitness;
    protected HeuristicProblem m_heuristicProblem;
    protected int m_intLocaSearchIterations;
    private List<ImprovementFoundDelegate> m_improvementFoundDelegates;
    private List<UpdateProgressDelegate> m_updateProgressDelegates;
    
    public ALocalSearch()
    {
    	m_improvementFoundDelegates = new ArrayList<ImprovementFoundDelegate>();
    	m_updateProgressDelegates = new ArrayList<UpdateProgressDelegate>(); 
    }
    
    public ALocalSearch(HeuristicProblem heuristicProblem)
    {
    	this();
        m_heuristicProblem = heuristicProblem;
    }

    public abstract void DoLocalSearch(Individual individual);

    /// <summary>
    ///   Update progress bar
    /// </summary>
    /// <param name = "intProgress">
    ///   Progress value
    /// </param>
    /// <param name = "strMessage">
    ///   Progress message
    /// </param>
	@Override
	public void InvokeUpdateProgress(int intProgress, String strMessage) 
	{
		try
		{
			for(UpdateProgressDelegate progressDelegate : m_updateProgressDelegates)
			{
				progressDelegate.invoke(intProgress, strMessage);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

    /// <summary>
    ///   Call this method if an improvent is found
    /// </summary>
	@Override
	public void InvokeImprovementFoundEvent() 
	{
		for(ImprovementFoundDelegate improvementFoundDelegate : m_improvementFoundDelegates)
		{
			improvementFoundDelegate.invoke(null);
		}
	}

	@Override
	public double LocalSearchProb() 
	{
		return 0;
	}

	@Override
	public void Dispose() 
	{
	}

	@Override
	public void AddImprovementFoundEvent(
			ImprovementFoundDelegate improvementFoundDelegate) 
	{
		m_improvementFoundDelegates.add(improvementFoundDelegate);
	}
	
	@Override
	public void AddUpdateProgressDelegate(UpdateProgressDelegate updateProgressDelegate)
	{
		m_updateProgressDelegates.add(updateProgressDelegate);
	}
}
