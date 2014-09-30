package Armadillo.Analytics.TextMining;

import java.util.Comparator;

import Armadillo.Core.DoubleHelper;
import Armadillo.Core.Logger;

public class MstDistanceObj implements Comparable<MstDistanceObj>,
		Comparator<MstDistanceObj>
{
    public int X;
    public int Y;
    public double Score;

    public MstDistanceObj(
        int x,
        int y,
        double score)
    {
        X = x;
        Y = y;
        Score = score;
    }


    public int GetX()
    {
        return X;
    }

    public int GetY()
    {
        return Y;
    }

    public double GetScore()
    {
        return Score;
    }

    public void SetScore(double score)
    {
        Score = score;
    }

	@Override
	public int compare(MstDistanceObj arg0, MstDistanceObj arg1) {
        
        return compareStatic(arg0, arg1);
	}

	public static int compareStatic(MstDistanceObj arg0, MstDistanceObj arg1) {
        
		try{
			MstDistanceObj compare = arg1;
	        double dblScore = arg0.GetScore();
	        double dblScoreCompare = compare.GetScore();
	        int intResult = DoubleHelper.compare(dblScore, dblScoreCompare);
	        if (intResult == 0)
	        {
	            intResult = DoubleHelper.compare(dblScore, dblScoreCompare);
	        }
	        return intResult;
		}
		catch(Exception ex){
			Logger.log(ex);
		}
		return 0;
	}
	
	@Override
	public int compareTo(MstDistanceObj arg0) {
		return compare(this, arg0);
	}
}