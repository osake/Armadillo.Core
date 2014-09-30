package Armadillo.Analytics.Optimisation.Base.Operators.PopulationClasses;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Optimisation.Base.Delegates.ExceptionOccurredDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.UpdateProgressDelegate;

public abstract class AInitialPopulation  implements IInitialPopulation
{
	List<UpdateProgressDelegate> m_updateProgressBarDelegates;
	List<ExceptionOccurredDelegate> m_exceptionOccurredDelegates;
	
	public AInitialPopulation()
	{
		m_updateProgressBarDelegates = new ArrayList<UpdateProgressDelegate>(); 
		m_exceptionOccurredDelegates = new ArrayList<ExceptionOccurredDelegate>();
	}
    /// <summary>
    ///   Method called when exception is thrown
    /// </summary>
    /// <param name = "e">
    ///   HCException
    /// </param>
    public void ExceptionOccurred(Exception e)
    {
    	for (ExceptionOccurredDelegate exceptionOccurredDelegate : m_exceptionOccurredDelegates) 
    	{
    		exceptionOccurredDelegate.invoke(e);
		}
    }

    /// <summary>
    ///   Progress bar method
    /// </summary>
    /// <param name = "intProgress">
    ///   Progress value
    /// </param>
    /// <param name = "strMessage">
    ///   Progress message
    /// </param>
    public void invokeUpdateProgress(int intProgress, String strMessage)
    {
    	for (UpdateProgressDelegate updateProgressBarDelegate : m_updateProgressBarDelegates) 
    	{
    		updateProgressBarDelegate.invoke(intProgress, strMessage);
		}
    }

	@Override
	public void AddUpdateProgressEvent(
			UpdateProgressDelegate updateProgressBarDelegate) 
	{
		m_updateProgressBarDelegates.add(updateProgressBarDelegate);
		
	}

	@Override
	public void AddExceptionOccurredEvent(
			ExceptionOccurredDelegate exceptionOccurredDelegate) 
	{
		m_exceptionOccurredDelegates.add(exceptionOccurredDelegate);
	}
}
