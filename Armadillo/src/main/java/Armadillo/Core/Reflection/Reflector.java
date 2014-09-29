package Armadillo.Core.Reflection;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import Armadillo.Core.Environment;
import Armadillo.Core.KeyValuePair;
import Armadillo.Core.Logger;
import Armadillo.Core.Text.StringHelper;

import com.esotericsoftware.reflectasm.FieldAccess;

public class Reflector {
	
	private FieldAccess m_access;
	private Hashtable<String, Type> m_colToTypeMap;
	private KeyValuePair<String, Type>[] m_colToTypeArr;
	private String[] m_colNames;
	private Object m_lockObj = new Object();
	private Class<?> m_classObj;
	private HashMap<String, Field> m_fields;
	
	public Reflector(Class<?> classObj)
	{
		m_access = FieldAccess.get(classObj);
		m_classObj = classObj;
	}
	
	public Method[] getMethods()
	{
		try
		{
			if(m_classObj == null)
			{
				return null;
			}
			Method[] methods = m_classObj.getMethods();
			return methods;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
	
	public static String getClassName(Class<?> classObj)
	{
		try
		{
			String strRawName = classObj.getName();
			if(!strRawName.contains("."))
			{
				return strRawName;
			}
			String[] tokens = strRawName.split("\\.");
			return tokens[tokens.length - 1];
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
	}
	
    /**
     * A common method for all enums since they can't have another base class
     * @param <T> Enum type
     * @param c enum type. All enums must be all caps.
     * @param string case insensitive
     * @return corresponding enum, or null
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static Enum<?> getEnumFromString(Type type, String string)
    {
        if( type != null && string != null )
        {
            try
            {
            	return Enum.valueOf((Class<Enum>) type, string);
            }
            catch(IllegalArgumentException ex)
            {
            	Logger.log(ex);
            }
        }
        return null;
    }   
    
    public Object getPropValue(
    		Object obj,
    		String strPropName)
    {
    	validateFields();
    	try {
			return m_fields.get(strPropName).get(obj);
		} catch (Exception ex) {
			Logger.log(ex);
		}
    	return null;
    }
    
    private void validateFields()
    {
    	if(m_fields != null){
    		return;
    	}
       HashMap<String, Field> fieldList = new HashMap<String, Field>(); 
	   for (Class<?> c = m_classObj; c != null; c = c.getSuperclass())
	    {
	        Field[] fields = c.getDeclaredFields();
	        for (Field classField : fields)
	        {
	        	fieldList.put(classField.getName(), classField);
	        }
	    }    
	   m_fields = fieldList;
    }
    
    public static boolean isEnum(Type propertyType)
    {
    	return propertyType instanceof Class && ((Class<?>)propertyType).isEnum();
    }

	public Type[] getColTypes()
	{
		try
		{
			KeyValuePair<String, Type>[] kvpArr = getColToType();
			Type[] typeArr = new Type[kvpArr.length];
			for (int i = 0; i < typeArr.length; i++) 
			{
				typeArr[i] = kvpArr[i].getValue();
			}
			return typeArr;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
    
	public KeyValuePair<String, Type>[] getColToType()
	{
		try
		{
			if(m_colToTypeArr != null)
			{
				return m_colToTypeArr;
			}
			synchronized(m_lockObj)
			{
				
				if(m_colToTypeArr != null)
				{
					return m_colToTypeArr;
				}
				String[] fieldNames0 = m_access.getFieldNames();
				Hashtable<String, Type> colToTypeMap = new Hashtable<String, Type>();
				@SuppressWarnings({ "unchecked", "resource" })
				KeyValuePair<String, Type>[] colToTypeArr = (KeyValuePair<String, Type>[]) 
						Array.newInstance(new KeyValuePair<String, Type>().getClass(), 
								fieldNames0.length);
				List<String> fieldNames = new ArrayList<String>();
				for (int i = 0; i < fieldNames0.length; i++) 
				{
					try
					{
						String strFieldName = fieldNames0[i];
						Field f = m_classObj.getField(strFieldName);
						Type type = f.getType();
						colToTypeArr[i] = new KeyValuePair<String, Type>(strFieldName, 
								type);
						colToTypeMap.put(strFieldName, type);
						fieldNames.add(strFieldName);
					}
					catch(Exception ex)
					{
						//
						// swallow the exception. Some fields are not public
						//
					}
				}
				
				m_colToTypeArr = colToTypeArr;
				m_colNames = fieldNames.toArray(new String[0]);
				m_colToTypeMap = colToTypeMap;
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_colToTypeArr;
	}
	
	public String[] getColNames()
	{
		getColToType();
		return m_colNames;
	}
	
	public FieldAccess getAccess(){
		return m_access;
	}

	public Type getPropertyType(String strPropertyName) {
		
		getColToType();
		return m_colToTypeMap.get(strPropertyName);
	}

	public Object createInstance() 
	{
		try 
		{
			return m_classObj.newInstance();
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return null;
	}

	public Type getPropertyType(int intCol) 
	{
		try
		{
			return m_colToTypeArr[intCol].getValue();
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return null;
	}

	public boolean isEnum(int intCol) {
		Type propertyType = getPropertyType(intCol);
		return isEnum(propertyType);
	}

	public boolean CanWriteProperty(String strProperty) 
	{
		return Modifier.isFinal(
				m_fields.get(strProperty).getModifiers());
	}

	public boolean ContainsProperty(String strPropertyName) 
	{
		validateFields();
		return m_fields.containsKey(strPropertyName);
	}

	public void SetPropertyValue(Object obj,
			String strPropertyName, 
			Object value) 
	{
		try 
		{
			if(obj == null ||
			   value == null)
			{
				return;
			}
			validateFields();
			Field field = m_fields.get(strPropertyName);
			
			if(field == null)
			{
				return;
			}
			
			field.set(obj, value);
		} 
		catch (IllegalArgumentException | IllegalAccessException ex) 
		{
			Logger.log(ex);
		}
	}

	public Object[] getPropValues(Object obj) 
	{
		try
		{
			if(obj == null)
			{
				return null;
			}
			String[] colNames = getColNames();
			if(colNames == null || colNames.length == 0)
			{
				return null;
			}
			Object[] objs = new Object[colNames.length];
			for (int i = 0; i < colNames.length; i++) 
			{
				Object objValue = getPropValue(obj, colNames[i]);
				objs[i] = objValue;
			}
			return objs;
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return null;
	}

	public String getStringRepresentation(Object obj) 
	{
		try
		{
			if(obj == null)
			{
				return "";
			}
			
			Object[] propValues = getPropValues(obj);
			if(propValues == null || propValues.length == 0)
			{
				return "";
			}
			String[] strArray = new String[propValues.length];
			for (int i = 0; i < propValues.length; i++) 
			{
				Object currObj = propValues[i];
				if(currObj != null)
				{
					strArray[i] = currObj.toString();
				}
			}
			return StringHelper.join(strArray, ",");
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return "";
	}
	
	public String getLongStringRepresentation(Object obj) 
	{
		try
		{
			if(obj == null)
			{
				return "";
			}
			
			Object[] propValues = getPropValues(obj);
			if(propValues == null || propValues.length == 0)
			{
				return "";
			}
			String[] strArray = new String[propValues.length];
			String[] colNames = getColNames();
			for (int i = 0; i < propValues.length; i++) 
			{
				Object currObj = propValues[i];
				if(currObj != null)
				{
					strArray[i] = colNames[i] + "=" + currObj.toString();
				}
			}
			return StringHelper.join(strArray, Environment.NewLine);
		}
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return "";
	}
	
}