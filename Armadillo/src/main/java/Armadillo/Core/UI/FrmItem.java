package Armadillo.Core.UI;

import java.util.Comparator;

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
}