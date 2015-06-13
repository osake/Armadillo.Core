package Armadillo.Core.UI;

import java.lang.reflect.Type;
import java.util.Date;

import org.joda.time.DateTime;

import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.Text.StringHelper;

public class LabelClass 
{
    private String m_strLbl;
    private String m_strValue;
    private Type m_dataType;
    private AComboList m_autoComplete;
	private Object m_objVal;

    public LabelClass(
    		String strLbl, 
    		String strValue,
    		Type dataType) 
    {
        super();
        m_strLbl = strLbl;
        m_strValue = strValue;
        m_dataType = dataType;
        
    }

    public String getLbl() 
    {
    	if(StringHelper.IsNullOrEmpty(m_strLbl))
    	{
    		Console.writeLine("Empty label");
    	}
    	
        return m_strLbl;
    }

    public void setLbl(String lbl) 
    {
        m_strLbl = lbl;
    }

    public String getValue() 
    {
        return m_strValue;
    }

    public void setValue(String value) 
    {
        m_strValue = value;
    }
    
    public void setDateValue(Date dateTime)
    {
    	try
    	{
    		m_strValue = new DateTime(dateTime).toString();
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    public boolean getIsTextType()
    {
    	return !getIsDateType() && 
    			!getIsImageType() &&
    			!getIsBooleanType();
    }
    
    public boolean getIsDateType()
    {
    	try
    	{
	    	return m_dataType == Date.class ||
	    		   m_dataType == DateTime.class;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return false;
    }
    
	public Type getDataType() 
	{
		return m_dataType;
	}

	public void setDataType(Type dataType) 
	{
		m_dataType = dataType;
	}
	
	public Date getDateValue()
	{
		try
		{
			if(!StringHelper.IsNullOrEmpty(m_strValue))
			{
				return DateTime.parse(m_strValue).toDate();
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return DateTime.now().toDate();
	}

	public boolean getIsCombo() 
	{
		return m_autoComplete != null;
	}
	
	public void setComboList(AComboList autoComplete) 
	{
		m_autoComplete = autoComplete;
	}

	public AComboList getComboItems() 
	{
		return m_autoComplete;
	}

	public boolean isAValidType() 
	{
		return m_dataType == double.class ||
				m_dataType == Double.class ||
				m_dataType == String.class ||
				m_dataType == int.class ||
				m_dataType == Integer.class ||
				m_dataType == boolean.class ||
				m_dataType == Boolean.class ||
				m_dataType == long.class ||
				m_dataType == float.class ||
				m_dataType == Date.class ||
				m_dataType == DateTime.class ||
				m_dataType == ImageWrapper.class;
	}

	public boolean getIsImageType() 
	{
		return m_dataType == ImageWrapper.class;
	}

	public Object getObjValue() 
	{
		return m_objVal;
	}
	
	public void setObjValue(Object objVal) 
	{
		m_objVal = objVal;
	}

	public boolean getIsBooleanType() 
	{
		return m_dataType == boolean.class;
	}
}