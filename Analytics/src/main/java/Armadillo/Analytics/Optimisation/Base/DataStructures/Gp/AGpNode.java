package Armadillo.Analytics.Optimisation.Base.DataStructures.Gp;

import java.util.List;

import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public abstract class AGpNode 
{
    protected GpOperatorsContainer m_gpOperatorsContainer;

    public int Depth;

    public GpOperatorNode Parent;

    public boolean IsOperatorNode;

    /// <summary>
    ///   Used for xml serialization
    /// </summary>
    public AGpNode()
    {
    }

    public AGpNode(
        GpOperatorsContainer gpOperatorsContainer)
    {
        m_gpOperatorsContainer = gpOperatorsContainer;
    }

    public abstract Object Compute(AGpVariable gpVariable);
    public abstract String ComputeToString();

    public void Dispose()
    {
        if (m_gpOperatorsContainer != null)
        {
            m_gpOperatorsContainer.Dispose();
            m_gpOperatorsContainer = null;
        }
        if(Parent != null)
        {
            Parent.Dispose();
            Parent = null;
        }
    }

    public abstract void ToStringB(StringBuilder sb);
    public abstract void GetNodeList(List<AGpNode> nodeList);

    public abstract AGpNode Clone(
        GpOperatorNode parent,
        HeuristicProblem heuristicProblem);

}
