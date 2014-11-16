package Armadillo.Core.Concurrent;

import java.util.Comparator;
import java.util.Date;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class BufferItem implements Comparable<BufferItem>,
		Comparator<BufferItem> {
	private Date m_date;
	private long m_lngAge;

	@Override
	public int compareTo(BufferItem arg0) {
		return compare(this, arg0);
	}

	@Override
	public int compare(BufferItem arg0, BufferItem arg1) {
		if (arg0.getAge() > arg1.getAge()) {
			return 1;
		}
		if (arg0.getAge() < arg1.getAge()) {
			return -1;
		}
		return 0;
	}

	@Override
	public int hashCode() {
		return (int) (m_lngAge ^ (m_lngAge >>> 32));
	}

	@Override
	public boolean equals(Object arg0) {
		return m_lngAge == ((BufferItem) arg0).m_lngAge;
	}

	public Date getDate() {
		return m_date;
	}

	public void setDate(Date m_date) {
		this.m_date = m_date;
	}

	public long getAge() {
		return m_lngAge;
	}

	public void setAge(long m_lngAge) {
		this.m_lngAge = m_lngAge;
	}

	@Override
	public Comparator<BufferItem> reversed() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<BufferItem> thenComparing(
			Comparator<? super BufferItem> other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> Comparator<BufferItem> thenComparing(
			Function<? super BufferItem, ? extends U> keyExtractor,
			Comparator<? super U> keyComparator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U extends Comparable<? super U>> Comparator<BufferItem> thenComparing(
			Function<? super BufferItem, ? extends U> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<BufferItem> thenComparingInt(
			ToIntFunction<? super BufferItem> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<BufferItem> thenComparingLong(
			ToLongFunction<? super BufferItem> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<BufferItem> thenComparingDouble(
			ToDoubleFunction<? super BufferItem> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	public static <T extends Comparable<? super T>> Comparator<T> reverseOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	public static <T extends Comparable<? super T>> Comparator<T> naturalOrder() {
		// TODO Auto-generated method stub
		return null;
	}

	public static <T> Comparator<T> nullsFirst(Comparator<? super T> comparator) {
		// TODO Auto-generated method stub
		return null;
	}

	public static <T> Comparator<T> nullsLast(Comparator<? super T> comparator) {
		// TODO Auto-generated method stub
		return null;
	}

	public static <T, U> Comparator<T> comparing(
			Function<? super T, ? extends U> keyExtractor,
			Comparator<? super U> keyComparator) {
		// TODO Auto-generated method stub
		return null;
	}

	public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
			Function<? super T, ? extends U> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	public static <T> Comparator<T> comparingInt(
			ToIntFunction<? super T> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	public static <T> Comparator<T> comparingLong(
			ToLongFunction<? super T> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	public static <T> Comparator<T> comparingDouble(
			ToDoubleFunction<? super T> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}
}
