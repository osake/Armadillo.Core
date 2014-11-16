package Armadillo.Core.UI;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class FrmItem implements Comparable<FrmItem>, Comparator<FrmItem>
{
	private String m_strKey;
	private Object m_obj;
	
	public FrmItem()
	{
		m_strKey = "";
	}
	
	public String getKey() 
	{
		return m_strKey;
	}
	
	public void setKey(String strKey) 
	{
		m_strKey = strKey;
	}
	
	public Object getObj() 
	{
		return m_obj;
	}
	
	public void setObj(Object obj) 
	{
		m_obj = obj;
	}
	
	@Override
	public int compare(FrmItem arg0, FrmItem arg1) 
	{
		return arg0.m_strKey.compareTo(arg1.m_strKey);
	}
	
	@Override
	public int compareTo(FrmItem arg0) 
	{
		return compare(this, arg0);
	}
	
    @Override
    public int hashCode()
    {
        return m_strKey.hashCode();
    }

    @Override
    public boolean equals(Object other)
    {
    	if(other == null)
    	{
    		return false;
    	}
    	String strKey;
    	if(other instanceof String)
    	{
    		strKey = (String)other;
    	}
    	else
    	{
    		strKey = ((FrmItem)other).m_strKey;
    	}
    	
        return m_strKey.equals(strKey);
    }

    @Override
    public String toString()
    {
        return m_strKey;
    }

	@Override
	public Comparator<FrmItem> reversed() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<FrmItem> thenComparing(Comparator<? super FrmItem> other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> Comparator<FrmItem> thenComparing(
			Function<? super FrmItem, ? extends U> keyExtractor,
			Comparator<? super U> keyComparator) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U extends Comparable<? super U>> Comparator<FrmItem> thenComparing(
			Function<? super FrmItem, ? extends U> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<FrmItem> thenComparingInt(
			ToIntFunction<? super FrmItem> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<FrmItem> thenComparingLong(
			ToLongFunction<? super FrmItem> keyExtractor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Comparator<FrmItem> thenComparingDouble(
			ToDoubleFunction<? super FrmItem> keyExtractor) {
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