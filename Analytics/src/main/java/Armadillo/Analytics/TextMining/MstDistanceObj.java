package Armadillo.Analytics.TextMining;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import Armadillo.Core.DoubleHelper;
import Armadillo.Core.Logger;

public class MstDistanceObj implements Comparable<MstDistanceObj>,
		Comparator<MstDistanceObj>
{
    public int X;
    public int Y;
    public double Score;

    public MstDistanceObj(){}
    
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


	@Override
	public Comparator<MstDistanceObj> reversed() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Comparator<MstDistanceObj> thenComparing(
			Comparator<? super MstDistanceObj> other) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public <U> Comparator<MstDistanceObj> thenComparing(
			Function<? super MstDistanceObj, ? extends U> keyExtractor,
			Comparator<? super U> keyComparator) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public <U extends Comparable<? super U>> Comparator<MstDistanceObj> thenComparing(
			Function<? super MstDistanceObj, ? extends U> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Comparator<MstDistanceObj> thenComparingInt(
			ToIntFunction<? super MstDistanceObj> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Comparator<MstDistanceObj> thenComparingLong(
			ToLongFunction<? super MstDistanceObj> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Comparator<MstDistanceObj> thenComparingDouble(
			ToDoubleFunction<? super MstDistanceObj> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}


	public <T extends Comparable<? super T>> Comparator<T> reverseOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends Comparable<? super T>> Comparator<T> naturalOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> Comparator<T> nullsFirst(Comparator<? super T> comparator) {
		// TODO Auto-generated method stub
		return null;
	}


	public <T> Comparator<T> nullsLast(Comparator<? super T> comparator) {
		// TODO Auto-generated method stub
		return null;
	}


	public <T, U> Comparator<T> comparing(
			Function<? super T, ? extends U> keyExtractor,
			Comparator<? super U> keyComparator) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T, U extends Comparable<? super U>> Comparator<T> comparing(
			Function<? super T, ? extends U> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}


	public <T> Comparator<T> comparingInt(
			ToIntFunction<? super T> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> Comparator<T> comparingLong(
			ToLongFunction<? super T> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> Comparator<T> comparingDouble(
			ToDoubleFunction<? super T> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}
}