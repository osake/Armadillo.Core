package Armadillo.Analytics.Optimisation.Base.DataStructures.Gp;

public abstract class AGpVarNodeFactory
{
    public abstract AGpVariableNode BuildVariable(
        int intDepth,
        GpOperatorNode parent);
}