package Armadillo.Analytics.Optimisation.Base.ObjectiveFunctions;

import Armadillo.Analytics.Optimisation.Base.Constraints.ConstraintClass;
import Armadillo.Analytics.Optimisation.Base.Constraints.IConstraint;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;

public abstract class ARangeConstrainedObjFunc implements IHeuristicObjectiveFunction
{
    public ConstraintClass Constraints;

    public ARangeConstrainedObjFunc()
    {
        Constraints = new ConstraintClass();
    }

    public boolean CheckConstraint(Individual individual)
    {
        return Constraints.CheckConstraints(individual);
    }

    public void AddConstraint(IConstraint constraint)
    {
        Constraints.AddConstraint(constraint);
    }
}