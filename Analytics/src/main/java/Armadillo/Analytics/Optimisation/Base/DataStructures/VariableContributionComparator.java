package Armadillo.Analytics.Optimisation.Base.DataStructures;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import Armadillo.Core.Logger;

public class VariableContributionComparator implements Comparator<VariableContribution> 
{

    /// <summary>
    ///   Sort object by their contribution.
    /// </summary>
    /// <param name = "obj">
    ///   Object to compare with.
    /// </param>
    /// <returns>
    ///   Compare value.
    /// </returns>
	@Override
	public int compare(VariableContribution arg0, VariableContribution arg1) 
	{
		try
		{
	    	VariableContribution Compare = arg1;
	        double difference = arg0.Contribution - Compare.Contribution;
	        if (difference < 0)
	        {
	            return 1;
	        }
	        if (difference > 0)
	        {
	            return -1;
	        }
	        difference = arg0.Index - Compare.Index;
	        if (difference < 0)
	        {
	            return 1;
	        }
	        if (difference > 0)
	        {
	            return -1;
	        }
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
        return 0;
	}

	@Override
	public Comparator<VariableContribution> reversed() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<VariableContribution> thenComparing(
			Comparator<? super VariableContribution> other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> Comparator<VariableContribution> thenComparing(
			Function<? super VariableContribution, ? extends U> keyExtractor,
			Comparator<? super U> keyComparator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U extends Comparable<? super U>> Comparator<VariableContribution> thenComparing(
			Function<? super VariableContribution, ? extends U> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<VariableContribution> thenComparingInt(
			ToIntFunction<? super VariableContribution> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<VariableContribution> thenComparingLong(
			ToLongFunction<? super VariableContribution> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<VariableContribution> thenComparingDouble(
			ToDoubleFunction<? super VariableContribution> keyExtractor) {
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
