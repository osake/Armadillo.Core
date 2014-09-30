package Armadillo.Analytics.Optimisation.Base.Constraints;

import java.util.List;
import java.util.ArrayList;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;

public class ConstraintClass
{
    /// <summary>
    ///   list of constraints
    /// </summary>
    public List<IConstraint> ListConstraints;

    /// <summary>
    ///   Constructor
    /// </summary>
    public ConstraintClass()
    {
        // initialize constraint list
        ListConstraints = new ArrayList<IConstraint>();
    }

    /// <summary>
    ///   Add new constraint
    /// </summary>
    /// <param name = "constraint">
    ///   Constraint
    /// </param>
    public void AddConstraint(IConstraint constraint)
    {
        ListConstraints.add(constraint);
    }

    /// <summary>
    ///   Evaluate constraints
    /// </summary>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    /// <returns></returns>
    public boolean CheckConstraints(
        Individual individual)
    {
        // evaluate constraints
        for (IConstraint constraint : ListConstraints)
        {
            if (!constraint.CheckConstraint(individual))
            {
                return false;
            }
        }
        return true;
    }

    /// <summary>
    ///   Evaluate constraints.
    ///   Sum up constraint values
    /// </summary>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    /// <returns>
    ///   Constraint value
    /// </returns>
    public double EvaluateConstraints(Individual individual)
    {
        double dblTotalValue = 0.0;
        // evaluate constraints
        for (IConstraint constraint : ListConstraints)
        {
            dblTotalValue += constraint.EvaluateConstraint(individual);
        }
        return dblTotalValue;
    }

    public void Dispose()
    {
        if(ListConstraints != null)
        {
            for (int i = 0; i < ListConstraints.size(); i++)
            {
                ListConstraints.get(i).Dispose();
            }
        }
    }
}
