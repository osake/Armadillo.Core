package Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import Armadillo.Core.Logger;

public class IndividualComparator implements Comparator<Individual> 
{

	public int compare(Individual arg0, Individual arg1) 
	{
		try
		{
			return compareTo(arg0, arg1);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return 0;
	}

	public int compareTo(
			Individual thisIndividual, 
			Individual o) 
	{
    	try
    	{
	        int intCompareToValue =
	            CompareToStd(thisIndividual, o);
	
	        if (intCompareToValue == 0)
	        {
	            intCompareToValue =
	                CompareToTree(thisIndividual, o);
	        }
	
	        return intCompareToValue;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return 0;
	}
	
    /// <summary>
    ///   Sort individuals by their fitness
    /// </summary>
    /// <param name = "o">
    ///   IIndividual
    /// </param>
    /// <returns>
    ///   Compare value
    /// </returns>
    public int CompareToStd(Individual thisIndividual, Individual o)
    {
    	try
    	{
	        if (thisIndividual.getFitness() < o.getFitness())
	        {
	            return 1;
	        }
	        if (thisIndividual.getFitness() > o.getFitness())
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

    public int CompareToTree(
    		Individual thisIndividual,
    		Individual other)
    {
    	try
    	{
	        return thisIndividual.getGpTreeSize() - other.getGpTreeSize();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return 0;
    }

	@Override
	public Comparator<Individual> reversed() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<Individual> thenComparing(
			Comparator<? super Individual> other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> Comparator<Individual> thenComparing(
			Function<? super Individual, ? extends U> keyExtractor,
			Comparator<? super U> keyComparator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U extends Comparable<? super U>> Comparator<Individual> thenComparing(
			Function<? super Individual, ? extends U> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<Individual> thenComparingInt(
			ToIntFunction<? super Individual> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<Individual> thenComparingLong(
			ToLongFunction<? super Individual> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<Individual> thenComparingDouble(
			ToDoubleFunction<? super Individual> keyExtractor) {
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
