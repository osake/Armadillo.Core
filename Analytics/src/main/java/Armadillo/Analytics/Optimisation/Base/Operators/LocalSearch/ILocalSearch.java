package Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch;

import Armadillo.Analytics.Optimisation.Base.Delegates.ImprovementFoundDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.UpdateProgressDelegate;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;

/// <summary>
///   Local serach.
///   Find better solutions in the neighbourhood of a 
///   given individual
/// </summary>
public interface ILocalSearch
{
    /// <summary>
    ///   Update progress
    /// </summary>
	void InvokeUpdateProgress(int intProgress, String strMessage);

	

    /// <summary>
    ///   Call this event if an improvement is found
    /// </summary>
	void InvokeImprovementFoundEvent();
    
    /// <summary>
    ///   Likelihood of selecting current reproduction operator
    /// </summary>
    double LocalSearchProb();

    /// <summary>
    ///   Do local search
    /// </summary>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    void DoLocalSearch(Individual individual);

	void Dispose();

	void AddImprovementFoundEvent(
			ImprovementFoundDelegate improvementFoundDelegate);
	
	void AddUpdateProgressDelegate(UpdateProgressDelegate updateProgressDelegate);	
}
