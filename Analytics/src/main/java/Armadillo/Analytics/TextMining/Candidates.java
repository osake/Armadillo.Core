package Armadillo.Analytics.TextMining;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

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

	@Override
	public Comparator<Candidates> reversed() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<Candidates> thenComparing(
			Comparator<? super Candidates> other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> Comparator<Candidates> thenComparing(
			Function<? super Candidates, ? extends U> keyExtractor,
			Comparator<? super U> keyComparator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U extends Comparable<? super U>> Comparator<Candidates> thenComparing(
			Function<? super Candidates, ? extends U> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<Candidates> thenComparingInt(
			ToIntFunction<? super Candidates> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<Candidates> thenComparingLong(
			ToLongFunction<? super Candidates> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<Candidates> thenComparingDouble(
			ToDoubleFunction<? super Candidates> keyExtractor) {
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