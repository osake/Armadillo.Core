package Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.DataStructures;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class NmVertexComparator implements Comparator<ANmVertex>
{
    /// <summary>
    ///   Sort vertex by values
    /// </summary>
    /// <param name = "o">
    ///   Vertext to compare with
    /// </param>
    /// <returns>
    ///   Compare value
    /// </returns>
	@Override
    public int compare(ANmVertex a, ANmVertex o)
    {
       double difference = a.Value - o.Value;
        if (difference < 0)
        {
            return 1;
        }
        if (difference > 0)
        {
            return -1;
        }
        return 0;
    }

	@Override
	public Comparator<ANmVertex> reversed() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<ANmVertex> thenComparing(
			Comparator<? super ANmVertex> other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> Comparator<ANmVertex> thenComparing(
			Function<? super ANmVertex, ? extends U> keyExtractor,
			Comparator<? super U> keyComparator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U extends Comparable<? super U>> Comparator<ANmVertex> thenComparing(
			Function<? super ANmVertex, ? extends U> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<ANmVertex> thenComparingInt(
			ToIntFunction<? super ANmVertex> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<ANmVertex> thenComparingLong(
			ToLongFunction<? super ANmVertex> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<ANmVertex> thenComparingDouble(
			ToDoubleFunction<? super ANmVertex> keyExtractor) {
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