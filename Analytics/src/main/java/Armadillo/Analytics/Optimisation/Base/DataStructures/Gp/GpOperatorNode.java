package Armadillo.Analytics.Optimisation.Base.DataStructures.Gp;

import java.util.List;

import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public class GpOperatorNode extends AGpNode
{
    protected AGpNode[] m_childrenArr;

    public AGpNode[] ChildrenArr()
    {
        return m_childrenArr;
    }

    public void setChildrenArr(AGpNode[] value)
    {
    	m_childrenArr = value;    	
    }
    
    public Object[] ParameterValuesArr;

    public AGpOperator GpOperator;

    public String[] ParameterDescrArr;

    /// <summary>
    ///   Constructor used for xml serialization
    /// </summary>
    public GpOperatorNode()
    {
    }

    public GpOperatorNode(
        GpOperatorNode parent,
        int intDepth,
        int intMaxDepth,
        RngWrapper random,
        boolean blnFullTree,
        GpOperatorsContainer gpOperatorsContainer)
    {
        this(
        //
        // randomly pick up an operator (sum, product, etc)
        //
        gpOperatorsContainer.GpOperatorArr[
            random.NextInt(
                0,
                gpOperatorsContainer.GpOperatorArr.length - 1)],
        intDepth,
        parent,
        gpOperatorsContainer);
    	
    	
        m_childrenArr = new AGpNode[GpOperator.NumbParameters];
        m_gpOperatorsContainer = gpOperatorsContainer;

        if (intDepth == intMaxDepth - 1)
        {
            //
            // end of the tree
            // m_childrenArr have to be constants or
            // variables only
            //
            for (int i = 0; i < m_childrenArr.length; i++)
            {
                m_childrenArr[i] = CreateEndNodeChild(random, intDepth + 1);
            }
        }
        else
        {
            //
            // m_childrenArr can be constants, terminals or operators
            //
            for (int i = 0; i < m_childrenArr.length; i++)
            {
                //
                // check if it is a full size tree
                //
                if (blnFullTree)
                {
                    m_childrenArr[i] = new GpOperatorNode(
                        this,
                        intDepth + 1,
                        intMaxDepth,
                        random,
                        true,
                        gpOperatorsContainer);
                }
                else
                {
                    //
                    // decide if the node is an operator or a terminal one
                    //
                    if (random.nextDouble() < 0.5)
                    {
                        m_childrenArr[i] = new GpOperatorNode(
                            this,
                            intDepth + 1,
                            intMaxDepth,
                            random,
                            false,
                            gpOperatorsContainer);
                    }
                    else
                    {
                        m_childrenArr[i] = CreateEndNodeChild(
                            random,
                            intDepth + 1);
                    }
                }
            }
        }
    }

    protected GpOperatorNode(
        AGpOperator gpOperator,
        int intDepth,
        GpOperatorNode parent,
        GpOperatorsContainer gpOperatorsContainer)
            
    {
    	super(gpOperatorsContainer);
        IsOperatorNode = true;
        GpOperator = gpOperator;
        Parent = parent;
        Depth = intDepth;

        if (gpOperator != null)
        {
            ParameterValuesArr = new Object[gpOperator.NumbParameters];
            ParameterDescrArr = new String[gpOperator.NumbParameters];
        }
    }

    public GpOperatorNode(GpOperatorsContainer gpOperatorsContainer) 
    {
    	super(gpOperatorsContainer);
    }

    /// <summary>
    ///   Compute each of the nodes
    /// </summary>
    /// <returns></returns>
    @Override
    public Object Compute(AGpVariable gpVariable)
    {
        for (int i = 0; i < ParameterValuesArr.length; i++)
        {
            ParameterValuesArr[i] = m_childrenArr[i].Compute(gpVariable);
        }
        //
        // compute current operator node
        //
        return GpOperator.Compute(ParameterValuesArr);
    }

    @Override
    public  String ComputeToString()
    {
        for (int i = 0; i < ParameterValuesArr.length; i++)
        {
            ParameterDescrArr[i] = m_childrenArr[i].ComputeToString();
        }
        //
        // compute current operator node
        //
        return GpOperator.ComputeToString(ParameterDescrArr);
    }

    @Override
    public String toString()
    {
        return ComputeToString();
    }

    @Override
    public void ToStringB(StringBuilder sb)
    {
        sb.append(GpOperator + " ");
        for (int i = 0; i < m_childrenArr.length; i++)
        {
            m_childrenArr[i].ToStringB(sb);
        }
    }

    @Override
    public void GetNodeList(
        List<AGpNode> nodeList)
    {
        nodeList.add(this);
        for (int i = 0; i < m_childrenArr.length; i++)
        {
            m_childrenArr[i].GetNodeList(nodeList);
        }
    }

    /// <summary>
    ///   Creates a deep clone of the current node
    /// </summary>
    /// <returns></returns>
    //public AbstractGpNode Clone()
    //{
    //    GpOperatorNode newNode = new GpOperatorNode(m_gpOperatorsContainer);
    //    newNode.ParameterValuesArr = (double[]) ParameterValuesArr.Clone();
    //    newNode.ParameterDescrArr = (String[]) ParameterDescrArr.Clone();
    //    newNode.Depth = Depth;
    //    newNode.GpOperator = GpOperator;
    //    newNode.Parent = null;
    //    newNode.IsOperatorNode = true;
    //    AbstractGpNode[] children = new AbstractGpNode[m_childrenArr.length];
    //    for (int i = 0; i < children.length; i++)
    //    {
    //        children[i] = m_childrenArr[i].Clone(
    //            newNode);
    //    }
    //    newNode.m_childrenArr = children;
    //    return newNode;
    //}
    @Override
    public AGpNode Clone(
        GpOperatorNode parent,
        HeuristicProblem heuristicProblem)
    {
    	GpOperatorNode newNode = new GpOperatorNode(m_gpOperatorsContainer);
        newNode.ParameterValuesArr = (Object[]) ParameterValuesArr.clone();
        newNode.ParameterDescrArr = (String[]) ParameterDescrArr.clone();
        newNode.Depth = Depth;
        newNode.GpOperator = GpOperator;
        newNode.Parent = parent;
        newNode.IsOperatorNode = true;
        AGpNode[] children = new AGpNode[m_childrenArr.length];
        for (int i = 0; i < children.length; i++)
        {
            children[i] = m_childrenArr[i].Clone(
                newNode,
                heuristicProblem);
        }
        newNode.m_childrenArr = children;
        return newNode;
    }

    private AGpNode CreateEndNodeChild(
        RngWrapper rng,
        int depth)
    {
        //
        // decide if creating a time series variable or a constant node
        //
        if (rng.nextDouble() < 0.5)
        {
            //
            // generate a variable node from the provided factory
            // a variable node can be also used as a sub-function.
            // the node factory provides this flexibility.
            //
            return m_gpOperatorsContainer.GpVarNodeFactory.BuildVariable(
                depth,
                this);
        }
        int intChosenConstant = rng.NextInt(0,
                                           m_gpOperatorsContainer.GpConstantArr.length - 1);
        return new GpConstantNode(
            m_gpOperatorsContainer.GpConstantArr[intChosenConstant],
            depth,
            this,
            m_gpOperatorsContainer);
    }

}
