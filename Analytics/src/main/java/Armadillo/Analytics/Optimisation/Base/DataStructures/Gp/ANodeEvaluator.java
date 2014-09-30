package Armadillo.Analytics.Optimisation.Base.DataStructures.Gp;

public abstract class ANodeEvaluator
{
    public abstract Object EvaluateVarNode(
        AGpVariable gpVariable,
        AGpNode gpOperatorNode);
}
