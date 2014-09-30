package Armadillo.Analytics.Optimisation.Base.ObjectiveFunctions;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;

public abstract class AEqualityConstrainObjFunc
{
    public double LowValue;
    public double HighValue;
    public double TargetValue;
    public String ObjectiveName;

    public AEqualityConstrainObjFunc()
    {
        LowValue = -Double.MAX_VALUE;
        HighValue = Double.MAX_VALUE;
    }

    public abstract boolean CheckConstraint(Individual individual);
}
