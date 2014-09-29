package Armadillo.Core.UI;

import java.io.Serializable;
import java.lang.reflect.Type;

@SuppressWarnings("serial")
public class ColumnModel implements Serializable 
{
	private String m_strHeader;
	private String m_strProperty;
	private Object m_value;
	private String m_strWidth;
	private Type m_dataType;

	public ColumnModel(
			String strHeader, 
			String strProperty,
			Type dataType) 
	{
		m_strHeader = strHeader;
		m_strProperty = strProperty; // dummy column name
		m_value = "unknown";
		m_dataType = dataType;
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

	public boolean isImageType() 
	{
		return m_dataType == ImageWrapper.class;
	}

}