package Armadillo.Analytics.Optimisation.Base.DataStructures.Gp;

public abstract class AGpOperator
{
    public int NumbParameters;

    public abstract Object Compute(Object[] parameters);
    public abstract String ComputeToString(String[] parameters);
}