package Utils.Gui;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ColumnModel implements Serializable 
{
	
	private String m_strHeader;
	private String m_strProperty;
	private Object m_value;
	private String m_strWidth;

	public ColumnModel(String strHeader, String strProperty) 
	{
		m_strHeader = strHeader;
		m_strProperty = strProperty;
		m_value = "unknown";
	}

	public String getHeader() 
	{
		return m_strHeader;
	}

	public String getProperty() 
	{
		return m_strProperty;
	}

	public Object getValue() 
	{
		if(m_value == null)
		{
			return "";
		}
		return m_value.toString();
	}

	public void setValue(Object value) 
	{
		m_value = value;
	}

	public void setWidth(String strWidth) 
	{
		m_strWidth = strWidth;
	}
	
	public String getWidth() 
	{
		return m_strWidth;
	}

}