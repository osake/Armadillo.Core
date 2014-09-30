package Armadillo.Analytics.Optimisation.Base.DataStructures.Gp;

import java.util.List;

import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public class GpConstantNode extends AGpNode
{
    public GpConstants GpConstant_()
    {
        return m_gpConstant;
    }
    
    public void setGpConstant(GpConstants value)
    {
		m_gpConstant = value;
    }

    private GpConstants m_gpConstant;

    public GpConstantNode()
    {
    }

    public GpConstantNode(
        GpConstants constant,
        int depth,
        GpOperatorNode parent,
        GpOperatorsContainer gpOperatorsContainer)
         
    {
    	super(gpOperatorsContainer);
        Parent = parent;
        m_gpConstant = constant;
        Depth = depth;
        IsOperatorNode = false;
    }

    public GpConstantNode(
        GpOperatorsContainer gpOperatorsContainer)
            
    {
        	super(gpOperatorsContainer);
    }

    @Override
    public  Object Compute(AGpVariable gpVariable)
    {
        //
        // return the value of the constant
        //
        return m_gpConstant.GetValue();
    }

    @Override
    public String toString()
    {
        return m_gpConstant.toString();
    }

    @Override
    public void ToStringB(StringBuilder sb)
    {
        m_gpConstant.ToStringB(sb);
    }

    @Override
    public AGpNode Clone(
        GpOperatorNode parent,
        HeuristicProblem heuristicProblem)
    {
    	GpConstantNode newNode = new GpConstantNode(m_gpOperatorsContainer);
        newNode.Depth = Depth;
        newNode.Parent = parent;
        newNode.m_gpConstant = m_gpConstant;
        newNode.IsOperatorNode = false;
        return newNode;
    }

    @Override
    public void GetNodeList(List<AGpNode> nodeList)
    {
        if (nodeList.contains(this))
        {
            //Debugger.Break();
        }

        nodeList.add(this);
    }

    @Override
    public String ComputeToString()
    {
        return toString();
    }
}
