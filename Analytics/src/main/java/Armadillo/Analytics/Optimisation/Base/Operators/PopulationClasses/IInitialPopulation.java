package Armadillo.Analytics.Optimisation.Base.Operators.PopulationClasses;

import Armadillo.Analytics.Optimisation.Base.Delegates.ExceptionOccurredDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.UpdateProgressDelegate;


public interface IInitialPopulation
{
    boolean DoLocalSearch();
    void setDoLocalSearch(boolean bln);
    void ExceptionOccurred(Exception ex);
    void invokeUpdateProgress(int intProgress, String strMessage);
    void GetInitialPopulation();
	void AddUpdateProgressEvent(
			UpdateProgressDelegate updateProgressBarDelegate);
	void AddExceptionOccurredEvent(
			ExceptionOccurredDelegate exceptionOccurredDelegate);
	
}