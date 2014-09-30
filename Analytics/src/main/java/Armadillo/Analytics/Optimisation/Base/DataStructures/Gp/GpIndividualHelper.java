package Armadillo.Analytics.Optimisation.Base.DataStructures.Gp;

public class GpIndividualHelper
{
    private int m_intCount;

    public static AGpNode ReturnNodeNumber(
        int numb,
        AGpNode gpNodes)
    {
        return (new GpIndividualHelper()).
            ReturnNodeNumber1(numb, gpNodes);
    }

    private AGpNode ReturnNodeNumber1(
        int numb,
        AGpNode gpNodes)
    {
        m_intCount = 0;
        return ReturnNodeNumber2(numb, gpNodes);
    }

    private AGpNode ReturnNodeNumber2(
        int numb,
        AGpNode gpNodes)
    {
        m_intCount++;
        if (m_intCount == numb)
        {
            return gpNodes;
        }
        else
        {
            if (gpNodes.IsOperatorNode)
            {
            	AGpNode[] children = ((GpOperatorNode) gpNodes).ChildrenArr();
                for (int i = 0; i < children.length; i++)
                {
                    //
                    // recursive call
                    //
                	AGpNode temp = ReturnNodeNumber2(numb, children[i]);
                    if (temp != null)
                    {
                        return temp;
                    }
                }
            }
            else
            {
                return null;
            }
        }
        return null;
    }

    public static int CountNodes(
        AGpNode gpNodes)
    {
        if (gpNodes.IsOperatorNode)
        {
            int count = 1;
            for (int i = 0; i < ((GpOperatorNode) gpNodes).ChildrenArr().length; i++)
            {
                count += CountNodes(((GpOperatorNode) gpNodes).ChildrenArr()[i]);
            }
            return count;
        }
        return 1;
    }
}
