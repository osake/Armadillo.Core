package Armadillo.Analytics.Optimisation.Base.Operators.Repair;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;

/// <summary>
///   Repair solution iterface
/// </summary>
public interface IRepair
{
 
    /// <summary>
    ///   Do repair
    /// </summary>
    /// <param name = "individual">
    ///   IIndividual to be repaired
    /// </param>
    boolean DoRepair(Individual individual);

    void AddRepairOperator(IRepair repair);

	void Dispose();
}