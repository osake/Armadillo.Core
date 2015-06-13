package Armadillo.Analytics.Optimisation.MstImpl;

public class MstEdge 
{
    private int m_intBefore;
    private MSTParentObject m_MstParentObject;

    public MstEdge(
        int before0, 
        MSTParentObject mSTParentObject0)
    {
        m_intBefore = before0;
        m_MstParentObject = mSTParentObject0;
    }

    public int GetParent()
    {
        return m_MstParentObject.GetParent();
    }

    public MSTParentObject getParentObject()
    {
        return m_MstParentObject;
    }

    public int GetBefore()
    {
        return m_intBefore;
    }

    public void SetBefore(int before0)
    {
        m_intBefore = before0;
    }

    public void SetParentObject(MSTParentObject mSTParentObject0)
    {
        m_MstParentObject = mSTParentObject0;
    }

}
