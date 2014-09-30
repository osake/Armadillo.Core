package Armadillo.Analytics.Optimisation.Base.DataStructures.Gp;

import Armadillo.Analytics.Stat.Random.RngWrapper;

public abstract class AGpOperatorNodeFactory
{
    protected final double m_dblFullTreeProb;
    protected final GpOperatorsContainer m_gpOperatorsContainer;


    public AGpOperatorNodeFactory(
        double dblFullTreeProb,
        GpOperatorsContainer gpOperatorsContainer)
    {
        m_dblFullTreeProb = dblFullTreeProb;
        m_gpOperatorsContainer = gpOperatorsContainer;
    }

    public AGpNode BuildOperator(
        GpOperatorNode parent,
        int intMaxDepth,
        int intDepth,
        RngWrapper rngWrapper)
    {
        return BuildOperator(
            parent,
            intMaxDepth,
            intDepth,
            rngWrapper.nextDouble() < m_dblFullTreeProb,
            rngWrapper);
    }

    protected abstract AGpNode BuildOperator(
        GpOperatorNode parent,
        int intMaxDepth,
        int intDepth,
        boolean blnFullTree,
        RngWrapper rngWrapper);

}
