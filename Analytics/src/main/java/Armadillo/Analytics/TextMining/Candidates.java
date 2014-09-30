package Armadillo.Analytics.TextMining;

import java.util.Comparator;

import Armadillo.Core.DoubleHelper;
import Armadillo.Core.Logger;

public class Candidates implements Comparable<Candidates>,
	Comparator<Candidates>
{
    private  double dblScoreClass;
    private  int m_intTColumnClass;
    private  int m_intTPosClass;
    private  int m_intUColumnClass;
    private  int m_intUPosClass;

    public Candidates(
        int intTPos,
        int intUPos,
        int intTColumn,
        int intUColumn,
        double dblScore)
    {
        m_intTPosClass = intTPos;
        m_intUPosClass = intUPos;
        m_intTColumnClass = intTColumn;
        m_intUColumnClass = intUColumn;
        dblScoreClass = dblScore;
    }

    public Candidates(
        int intTPos,
        int intUPos,
        double dblScore)
    {
        m_intTPosClass = intTPos;
        m_intUPosClass = intUPos;
        dblScoreClass = dblScore;
    }

    public static int compareStatic(
    		Candidates obj0,
    		Candidates obj1)
    {
    	try{
	        Candidates Compare = obj1;
	        int intResult = DoubleHelper.compare(obj0.dblScoreClass, Compare.GetScore());
	        if (intResult == 0)
	        {
	            int intTrClass = -Math.abs(obj0.m_intTPosClass - obj0.m_intUPosClass);
	            int intTrCompare = -Math.abs(Compare.GetTPos() - Compare.GetUPos());
	            intResult = DoubleHelper.compare(intTrClass, intTrCompare);
	        }
	        return intResult;
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return 0;
    }

    public int GetTPos()
    {
        return m_intTPosClass;
    }

    public int GetUPos()
    {
        return m_intUPosClass;
    }

    public int GetTCol()
    {
        return m_intTColumnClass;
    }

    public int GetUCol()
    {
        return m_intUColumnClass;
    }

    public double GetScore()
    {
        return dblScoreClass;
    }

	@Override
	public int compare(Candidates arg0, Candidates arg1) {
		return arg0.compareTo(arg1);
	}

	@Override
	public int compareTo(Candidates arg0) {
		return compareStatic(this, arg0);
	}
}