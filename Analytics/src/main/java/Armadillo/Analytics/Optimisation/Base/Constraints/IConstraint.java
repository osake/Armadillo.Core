package Armadillo.Analytics.Optimisation.Base.Constraints;

import Armadillo.Analytics.Mathematics.InequalityType;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;

public interface IConstraint
{
    /// <summary>
    ///   The type of inequality
    /// </summary>
    public InequalityType getInequality();

    /// <summary>
    ///   Cosntraint boundary (limit)
    /// </summary>
    public double getBoundary();

    /// <summary>
    ///   Check if the constraint is in bounds
    ///   Returns true if the constraint is in bound. 
    ///   False otherwise
    /// </summary>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    /// <returns>
    ///   Returns true if the constraint is in bound. 
    ///   False otherwise
    /// </returns>
    boolean CheckConstraint(Individual individual);

    /// <summary>
    ///   Evaluate constraint value.
    /// </summary>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    /// <returns>
    ///   Constraint value
    /// </returns>
    double EvaluateConstraint(Individual individual);

	public void Dispose();
}