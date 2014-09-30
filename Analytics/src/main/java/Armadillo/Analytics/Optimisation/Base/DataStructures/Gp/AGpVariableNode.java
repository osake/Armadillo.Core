package Armadillo.Analytics.Optimisation.Base.DataStructures.Gp;

public abstract class AGpVariableNode extends AGpNode
{
    /// <summary>
    ///   Constructor used for serialization
    /// </summary>
    public AGpVariableNode()
    {
    }

    public AGpVariableNode(
        int depth,
        GpOperatorNode parent,
        GpOperatorsContainer gpOperatorsContainer) 
    {
    	super(gpOperatorsContainer);
        IsOperatorNode = false;
        Parent = parent;
        Depth = depth;
    }
}
