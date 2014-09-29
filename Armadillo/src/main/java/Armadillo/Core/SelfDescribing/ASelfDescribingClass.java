package Armadillo.Core.SelfDescribing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import Armadillo.Core.ClonerHelper;
import Armadillo.Core.HCException;
import Armadillo.Core.ICloneable;
import Armadillo.Core.Logger;
import Armadillo.Core.Concurrent.*;
import Armadillo.Core.Reflection.ReflectionCache;
import Armadillo.Core.Reflection.Reflector;
import Armadillo.Core.Serialization.EnumSerializedType;
import Armadillo.Core.Serialization.ISerializerReader;
import Armadillo.Core.Serialization.ISerializerWriter;
import Armadillo.Core.Serialization.Serializer;
import Armadillo.Core.Text.StringHelper;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

public class ASelfDescribingClass {

    protected final HashMap<String, Boolean> m_blnValues;
    protected final HashMap<String, Date> m_dateValues;
    protected final HashMap<String, Double> m_dblValues;
    protected final HashMap<String, Integer> m_intValues;
    protected final HashMap<String, Long> m_lngValues;
    private final HashMap<String, Object> m_objValues;
    protected final HashMap<String, String> m_strValues;
    protected Reflector m_reflector;
    protected String m_strClassName;

    public ASelfDescribingClass()
    {
    	this("");
    }

    public ASelfDescribingClass(
        String strClassName)
    {
        //
        // initialize lookup caches
        //
        m_strClassName = strClassName;
        m_intValues = new HashMap<String, Integer>();
        m_strValues = new HashMap<String, String>();
        m_blnValues = new HashMap<String, Boolean>();
        m_lngValues = new HashMap<String, Long>();
        m_dblValues = new HashMap<String, Double>();
        m_dateValues = new HashMap<String, Date>();
        m_objValues = new HashMap<String, Object>();
    }

    public void SaveToXml(String strXmlFileName)
    {
        try
        {
            synchronized (LockHelper.GetLockObject(strXmlFileName))
            {
            	StringBuilder sb =
                        new StringBuilder("<?xml version=" + '"' + "1.0" + '"' + " encoding=" + '"' + "utf-8" + '"' +
                                          "?>\n");
                sb.append("<constants>\n");
                for (String strPropertyName : GetHardPropertyNames())
                {
                    Object value = GetHardPropertyObjValue(strPropertyName);
                    sb.append("<" + strPropertyName + ">\n");
                    sb.append((value == null ? "" : value.toString().trim()) + " \n");
                    sb.append("</" + strPropertyName + ">\n");
                }

                for (Map.Entry<String, Boolean> kvp : m_blnValues.entrySet())
                {
                    String strPropertyName = kvp.getKey().trim();
                    String strPropertyValue = kvp.getValue().toString().trim();
                    sb.append("<" + strPropertyName + ">\n");
                    sb.append(strPropertyValue + "\n");
                    sb.append("</" + strPropertyName + ">\n");
                }
                for (Map.Entry<String, Double> kvp : m_dblValues.entrySet())
                {
                    String strPropertyName = kvp.getKey().trim();
                    String strPropertyValue = kvp.getValue().toString().trim();
                    sb.append("<" + strPropertyName + ">\n");
                    sb.append(strPropertyValue + "\n");
                    sb.append("</" + strPropertyName + ">\n");
                }
                for (Map.Entry<String, Integer> kvp : m_intValues.entrySet())
                {
                    String strPropertyName = kvp.getKey().trim();
                    String strPropertyValue = kvp.getValue().toString().trim();
                    sb.append("<" + strPropertyName + ">\n");
                    sb.append(strPropertyValue + "\n");
                    sb.append("</" + strPropertyName + ">\n");
                }
                for (Map.Entry<String, Date> kvp : m_dateValues.entrySet())
                {
                    String strPropertyName = kvp.getKey().trim();
                    String strPropertyValue = kvp.getValue().toString().trim();
                    sb.append("<" + strPropertyName + ">\n");
                    sb.append(strPropertyValue + "\n");
                    sb.append("</" + strPropertyName + ">\n");
                }
                for (Map.Entry<String, Long> kvp : m_lngValues.entrySet())
                {
                    String strPropertyName = kvp.getKey().trim();
                    String strPropertyValue = kvp.getValue().toString().trim();
                    sb.append("<" + strPropertyName + ">\n");
                    sb.append(strPropertyValue + "\n");
                    sb.append("</" + strPropertyName + ">\n");
                }
                for (Map.Entry<String, String> kvp : m_strValues.entrySet())
                {
                    String strPropertyName = kvp.getKey().trim();
                    String strPropertyValue = kvp.getValue().trim();
                    sb.append("<" + strPropertyName + ">\n");
                    sb.append(strPropertyValue + "\n");
                    sb.append("</" + strPropertyName + ">\n");
                }
                for (Map.Entry<String, Object> kvp : m_objValues.entrySet())
                {
                    String strPropertyName = kvp.getKey().trim();
                    String strPropertyValue = kvp.getValue().toString().trim();
                    sb.append("<" + strPropertyName + ">\n");
                    sb.append("<constants>\n");
                    sb.append(strPropertyValue + "\n");
                    sb.append("</constants>\n");
                    sb.append("</" + strPropertyName + ">\n");
                }
                sb.append("</constants>\n");
                String strDescr = sb.toString().trim();
                if (StringHelper.IsNullOrEmpty(strDescr))
                {
                    throw new HCException("Null description");
                }
                
                PrintWriter writer = new PrintWriter(
			    		new BufferedWriter(
			    				new FileWriter(strXmlFileName, true)));
            	writer.println(strDescr);
            	writer.flush();
            	writer.close();
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public  Object Clone()
    {
    	
    	ASelfDescribingClass obj = (ASelfDescribingClass)ReflectionCache.getReflector(getClass()).createInstance();
        obj.SetClassName(GetClassName());
        for (String strProperty : GetHardPropertyNames())
        {
            if (!m_reflector.CanWriteProperty(strProperty))
            {
                continue;
            }
            Object propertyValue = GetHardPropertyObjValue(strProperty);
            if(propertyValue instanceof Cloneable){
                propertyValue = ((ICloneable)propertyValue).clone();
            }
            else{
            	propertyValue = ClonerHelper.Clone(propertyValue);            	
            }

            obj.SetHardPropertyValue(strProperty, propertyValue);
        }

        for (Map.Entry<String, Boolean> kvp : m_blnValues.entrySet())
        {
            obj.SetBlnValue(
                kvp.getKey(),
                kvp.getValue());
        }
        for (Map.Entry<String, Double> kvp : m_dblValues.entrySet())
        {
            obj.SetDblValue(
                kvp.getKey(),
                kvp.getValue());
        }
        for (Map.Entry<String, Integer> kvp : m_intValues.entrySet())
        {
            obj.SetIntValue(
                kvp.getKey(),
                kvp.getValue());
        }
        for (Map.Entry<String, Date> kvp : m_dateValues.entrySet())
        {
            obj.SetDateValue(
                kvp.getKey(),
                kvp.getValue());
        }
        for (Map.Entry<String, Long> kvp : m_lngValues.entrySet())
        {
            obj.SetLngValue(
                kvp.getKey(),
                kvp.getValue());
        }
        for (Map.Entry<String, String> kvp : m_strValues.entrySet())
        {
            obj.SetStrValue(
                kvp.getKey(),
                kvp.getValue());
        }
        for (Map.Entry<String, Object> kvp : m_objValues.entrySet())
        {
            obj.SetObjValueToDict(
                kvp.getKey(),
                kvp.getValue());
        }

        return obj;
    }

    public String GetClassName()
    {
        return m_strClassName;
    }

    public void SetClassName(Enum<?> enumValue)
    {
        SetClassName(enumValue.toString());
    }

    public void SetClassName(String strClassName)
    {
        m_strClassName = strClassName;
    }

    public void CopyTo(
        ASelfDescribingClass otherSelfDescribingClass)
    {
        CopyTo(
            otherSelfDescribingClass,
            "");
    }

    public void CopyTo(
        ASelfDescribingClass otherSelfDescribingClass,
        String strKeyPrefix)
    {
        strKeyPrefix = (StringHelper.IsNullOrEmpty(strKeyPrefix)
                            ? ""
                            : strKeyPrefix + "_");
        //
        // note => We need to synchronized the dictionaries since these values can change
        // at runtime. Adding a concurrent dictionary would slow down the performance and add memmory footprint
        //
        //
        synchronized (m_blnValues)
        {
            for (Map.Entry<String, Boolean> kvp : m_blnValues.entrySet())
            {
                otherSelfDescribingClass.SetBlnValue(
                    strKeyPrefix + kvp.getKey(), kvp.getValue());
            }
        }
        synchronized (m_dblValues)
        {
            for (Map.Entry<String, Double> kvp : m_dblValues.entrySet())
            {
                otherSelfDescribingClass.SetDblValue(
                    strKeyPrefix + kvp.getKey(), kvp.getValue());
            }
        }
        synchronized (m_intValues)
        {
            for (Map.Entry<String, Integer> kvp : m_intValues.entrySet())
            {
                otherSelfDescribingClass.SetIntValue(
                    strKeyPrefix + kvp.getKey(), kvp.getValue());
            }
        }
        synchronized (m_dateValues)
        {
            for (Map.Entry<String, Date> kvp : m_dateValues.entrySet())
            {
                otherSelfDescribingClass.SetDateValue(
                    strKeyPrefix + kvp.getKey(), kvp.getValue());
            }
        }
        synchronized (m_lngValues)
        {
            for (Map.Entry<String, Long> kvp : m_lngValues.entrySet())
            {
                otherSelfDescribingClass.SetLngValue(
                    strKeyPrefix + kvp.getKey(), kvp.getValue());
            }
        }
        synchronized (m_strValues)
        {
            for (Map.Entry<String, String> kvp : m_strValues.entrySet())
            {
                otherSelfDescribingClass.SetStrValue(
                    strKeyPrefix + kvp.getKey(), kvp.getValue());
            }
        }
        synchronized (m_objValues)
        {
            for (Map.Entry<String, Object> kvp : m_objValues.entrySet())
            {
                otherSelfDescribingClass.SetObjValueToDict(
                    strKeyPrefix + kvp.getKey(), kvp.getValue());
            }
        }
        //
        // set property values
        //
        Class<?> thisDynamicType = getClass();
        Reflector thisClassBinderObj = ReflectionCache.getReflector(thisDynamicType);
        Class<?> otherDynamicType = otherSelfDescribingClass.getClass();
        Reflector otherClassBinderObj = ReflectionCache.getReflector(otherDynamicType);
        for (String strPropertyName : thisClassBinderObj.getColNames())
        {
            Object objValue = thisClassBinderObj.getPropValue(
                this,
                strPropertyName);
            if (otherClassBinderObj.ContainsProperty(
                strPropertyName) &&
                otherClassBinderObj.CanWriteProperty(strPropertyName))
            {
                otherClassBinderObj.SetPropertyValue(
                    otherSelfDescribingClass,
                    strPropertyName,
                    objValue);
            }
            else
            {
                Class<?> propertyType;
                if(objValue == null)
                {
                    propertyType = GetPropertyType(strPropertyName);
                    objValue = ReflectionCache.getReflector(propertyType).createInstance();
                }
                else
                {
                    propertyType = objValue.getClass();
                }
                //
                // set value as a map
                //
                if (propertyType == Integer.class)
                {
                    otherSelfDescribingClass.SetIntValue(strPropertyName,
                                                               (int)objValue);
                }
                else if (propertyType == Double.class)
                {
                    otherSelfDescribingClass.SetDblValue(strPropertyName,
                                                               (double)objValue);
                }
                else if (propertyType == String.class)
                {
                    otherSelfDescribingClass.SetStrValue(strPropertyName,
                                                               (String)objValue);
                }
                else if (propertyType == Date.class)
                {
                    otherSelfDescribingClass.SetDateValue(strPropertyName,
                                                                (Date)objValue);
                }
                else if (propertyType == Boolean.class)
                {
                    otherSelfDescribingClass.SetBlnValue(strPropertyName,
                                                               (boolean)objValue);
                }
                else if (propertyType == Long.class)
                {
                    otherSelfDescribingClass.SetLngValue(strPropertyName,
                                                               (long)objValue);
                }
                else
                {
                    otherSelfDescribingClass.SetObjValueToDict(strPropertyName,
                                                               objValue);
                }
            }
        }
    }

    public String[] TryGetStringArr(
        Enum<?> enumValue)
    {
        return TryGetStringArr(
            enumValue.toString());
    }

    public String[] TryGetStringArr(
        String strPropertyName)
    {
        if (ContainsHardProperty(strPropertyName))
        {
            return GetStringArr(strPropertyName);
        }
        return null;
    }

    public String[] GetStringArr(Enum<?> enumValue)
    {
        return GetStringArr(enumValue.toString());
    }

    @SuppressWarnings("unchecked")
	public <T> T GetHardPropertyValue(String strPropertyName)
    {
        ValidateReflector();
        return (T)m_reflector.getPropValue(this, strPropertyName);
    }

    public String[] GetStringArr(String strParamName)
    {
        String strValue = GetStrValue(strParamName);
        String[] tokens = strValue.split(", \n\t");
        ArrayList<String> selectedTokens = new ArrayList<String>();
        for (String strToken : tokens)
        {
            String strCleanToken = strToken.trim();
            if (!StringHelper.IsNullOrEmpty(strCleanToken))
            {
                selectedTokens.add(strCleanToken);
            }
        }
        return selectedTokens.toArray(new String[selectedTokens.size()]);
        //}
    }

    public HashMap<String, String> GetStringValues()
    {
        return m_strValues;
    }

    public HashMap<String, Double> GetDblValues()
    {
        return m_dblValues;
    }

    public HashMap<String, Integer> GetIntValues()
    {
        return m_intValues;
    }

    public HashMap<String, Boolean> GetBlnValues()
    {
        return m_blnValues;
    }

    public HashMap<String, Long> GetLngValues()
    {
        return m_lngValues;
    }

    public HashMap<String, Object> GetObjValues()
    {
        return m_objValues;
    }

    public HashMap<String, Date> GetDateValues()
    {
        return m_dateValues;
    }

    public String GetStrValue(
        Enum<?> enumValue)
    {
        return GetStrValue(
            enumValue.toString());
    }

    public String GetStrValue(
        String strPropertyName)
    {
        return TryGetStrValue(strPropertyName);
    }

    private String GetHardStrValue(String strPropertyName)
    {
        String strValue = (String) GetHardPropertyValue(strPropertyName);
        return strValue;
    }

    public int GetIntValue(
        Enum<?> enumValue)
    {
        return GetIntValue(enumValue.toString());
    }

    public int GetIntValue(
        String strPropertyName)
    {
        return TryGetIntValue(strPropertyName);
    }

    private int GetHardIntValue(String strPropertyName)
    {
        Object objValue = GetHardPropertyValue(strPropertyName);
        int intValue = (int)(objValue);
        return intValue;
    }

    private Object GetHardObjValue(String strPropertyName)
    {
        Object objValue = GetHardPropertyValue(strPropertyName);
        return objValue;
    }

    public boolean ContainsHardProperty(Enum<?> enumValue)
    {
        return ContainsHardProperty(enumValue.toString());
    }

    public boolean ContainsProperty(String strPropertyName)
    {
        if (ContainsHardProperty(strPropertyName))
        {
            return true;
        }
        if (m_blnValues.containsKey(strPropertyName) ||
            m_dateValues.containsKey(strPropertyName) ||
            m_dblValues.containsKey(strPropertyName) ||
            m_intValues.containsKey(strPropertyName) ||
            m_lngValues.containsKey(strPropertyName) ||
            m_objValues.containsKey(strPropertyName) ||
            m_strValues.containsKey(strPropertyName))
        {
            return true;
        }
        return false;
    }

    public boolean ContainsHardProperty(String strPropertyName)
    {
        ValidateReflector();
        if (m_reflector == null)
        {
            return false;
        }
        return m_reflector.ContainsProperty(strPropertyName);
    }

    public double GetDblValue(
        Enum<?> enumName)
    {
        return GetDblValue(enumName.toString());
    }

    public double GetDblValue(
        String strPropertyName)
    {
        return TryGetDblValue(strPropertyName);
    }

    private double GetHardDblValue(String strPropertyName)
    {
        Object objValue = GetHardPropertyValue(strPropertyName);
        if(objValue == null)
        {
            return Double.NaN;
        }
        double dblValue = (double)objValue;
        return dblValue;
    }

    public boolean GetBlnValue(
        Enum<?> enumValue)
    {
        return GetBlnValue(enumValue.toString());
    }

    public boolean GetBlnValue(
        String strPropertyName)
    {
        return TryGetBlnValue(strPropertyName);
    }

    private boolean GetHardBlnValue(String strPropertyName)
    {
        Object objValue = GetHardPropertyValue(strPropertyName);
        Boolean blnValue = (Boolean)objValue;
        return blnValue;
    }

    public long GetLngValue(
        Enum<?> enumValue)
    {
        return GetLngValue(
            enumValue.toString());
    }

    public long GetLngValue(
        String strPropertyName)
    {
        return TryGetIntValue(strPropertyName);
    }

    private long GetHardLngValue(String strPropertyName)
    {
        Object objValue = GetHardPropertyValue(strPropertyName);
        long lngValue = (long)objValue;
        return lngValue;
    }

    public Object GetObjValue(
        Enum<?> enumKey)
    {
        return GetObjValue(
            enumKey.toString());
    }

    public Object GetObjValue(
        String strPropertyName)
    {
        return TryGetObjValue(strPropertyName);
    }

    public void SetIntValue(
        String strKey,
        int intValue)
    {
        synchronized (m_intValues)
        {
            if (ContainsHardProperty(strKey))
            {
                SetHardPropertyValue(strKey, intValue);
            }
            else
            {
                m_intValues.put(strKey, intValue);
            }
        }
    }

    public void SetDateValue(
        Enum<?> enumKey,
        Date dateValue)
    {
        SetDateValue(enumKey.toString(),
                           dateValue);
    }

    public void SetDateValue(
        String strKey,
        Date dateValue)
    {
        synchronized (m_dateValues)
        {
            if (ContainsHardProperty(strKey))
            {
                SetHardPropertyValue(strKey, dateValue);
            }
            else
            {
                m_dateValues.put(strKey, dateValue);
            }
        }
    }


    public void SetLngValue(
        Enum<?> enumKey,
        long lngValue)
    {
        SetLngValue(
            enumKey.toString(),
            lngValue);
    }

    public void SetLngValue(
        String strKey,
        long lngValue)
    {
        synchronized (m_lngValues)
        {
            if (ContainsHardProperty(strKey))
            {
                SetHardPropertyValue(strKey, lngValue);
            }
            else
            {
                m_lngValues.put(strKey, lngValue);
            }
        }
    }


    public void SetIntValue(
        Enum<?> enumKey,
        int intValue)
    {
        SetIntValue(
            enumKey.toString(),
            intValue);
    }

    public void SetDblValue(
        String strKey,
        double dblValue)
    {
        synchronized (m_dblValues)
        {
            if (ContainsHardProperty(strKey))
            {
                SetHardPropertyValue(strKey, dblValue);
            }
            else
            {
                m_dblValues.put(strKey, dblValue);
            }
        }
    }

    public void SetBlnValue(
        Enum<?> enumKey,
        boolean blnValue)
    {
        SetBlnValue(
            enumKey.toString(),
            blnValue);
    }

    public void SetBlnValue(
        String strKey,
        boolean blnValue)
    {
        if (ContainsHardProperty(strKey))
        {
            SetHardPropertyValue(strKey, blnValue);
        }
        else
        {
            m_blnValues.put(strKey, blnValue);
        }
    }


    public void SetDblValue(
        Enum<?> enumKey,
        double dblValue)
    {
        SetDblValue(
            enumKey.toString(),
            dblValue);
    }

    public void SetStrValue(
        Enum<?> enumKey,
        String strValue)
    {
        SetStrValue(
            enumKey.toString(),
            strValue);
    }

    public void SetStrValue(
        String strKey,
        String strValue)
    {
        synchronized (m_strValues)
        {
            if (ContainsHardProperty(strKey))
            {
                SetHardPropertyValue(strKey, strValue);
            }
            else
            {
                m_strValues.put(strKey, strValue);
            }
        }
    }

    public void SetObjValueToDict(
        Enum<?> enumKey,
        Object oValue)
    {
        SetObjValueToDict(
            enumKey.toString(),
            oValue);
    }

    public void SetObjValueToDict(
        String strKey,
        Object oValue)
    {
        synchronized (m_objValues)
        {
            m_objValues.put(strKey, oValue);
        }
    }

    public List<String> GetAllPropertyNames()
    {

        List<String> properties = GetHardPropertyNames();
        synchronized (m_intValues)
        {
            if (m_intValues.size() > 0)
            {
                properties.addAll(m_intValues.keySet());
            }
        }
        synchronized (m_dblValues)
        {
            if (m_dblValues.size() > 0)
            {
                properties.addAll(m_dblValues.keySet());
            }
        }
        synchronized (m_blnValues)
        {
            if (m_blnValues.size() > 0)
            {
                properties.addAll(m_blnValues.keySet());
            }
        }
        synchronized (m_strValues)
        {
            if (m_strValues.size() > 0)
            {
                properties.addAll(m_strValues.keySet());
            }
        }
        synchronized (m_objValues)
        {
            if (m_objValues.size() > 0)
            {
                properties.addAll(m_objValues.keySet());
            }
        }
        synchronized (m_lngValues)
        {
            if (m_lngValues.size() > 0)
            {
                properties.addAll(m_lngValues.keySet());
            }
        }
        synchronized (m_dateValues)
        {
            if (m_dateValues.size() > 0)
            {
                properties.addAll(m_dateValues.keySet());
            }
        }
        return properties;
    }

    public List<String> GetHardPropertyNames()
    {
        ValidateReflector();
        if (m_reflector == null)
        {
            return new ArrayList<String>();
        }
        List<String> properties = new ArrayList<String>();
        String[] colNames = m_reflector.getColNames();
        for (int i = 0; i < colNames.length; i++) {
			properties.add(colNames[i]);
		}
        return properties;
    }

    public Class<?> GetPropertyType(String strPropertyName)
    {
        ValidateReflector();
        Type type = m_reflector.getPropertyType(strPropertyName);
        if(type == null)
        {
            Object objRes = TryGetValueFromAnyProperty(strPropertyName);
            if(objRes != null)
            {
                return objRes.getClass();
            }
        }
        return (Class<?>)type;
    }

    public void SetHardPropertyValue(
        String strPropertyName,
        Object objValue)
    {
        if (objValue == null)
        {
            return;
        }

        ValidateReflector();
        Class<?> propertyType = GetPropertyType(strPropertyName);
        if (objValue.getClass() != propertyType)
        {
            String strObjValue = objValue.toString();
            if (propertyType == String.class)
            {
                //
                // easy fix, just set a String value
                //
                m_reflector.SetPropertyValue(this, strPropertyName, strObjValue);
            }
            else
            {
                if (objValue.getClass() == propertyType)
                {
                    m_reflector.SetPropertyValue(this, strPropertyName, objValue);
                }
                else
                {
                    if (propertyType == Object.class)
                    {
                        m_reflector.SetPropertyValue(this, strPropertyName, objValue);
                    }
                    else
                    {
                        //
                        // Try to parse a String. Note that if the parser fails, the Object will be wasted
                        //
                        String obj = strObjValue;
                        if (obj != null)
                        {
                            m_reflector.SetPropertyValue(this, strPropertyName, obj);
                        }
                    }
                }
            }
        }
        else
        {
            //
            // set the property value
            //
            m_reflector.SetPropertyValue(this, strPropertyName, objValue);
        }
    }


    public Object ExecuteMethod(String strMethodName)
    {
        return ExecuteMethod(strMethodName,
                null);
    }

    public Object ExecuteMethod(String strMethodName,
        Object[] parameters)
    {
        try
        {
            Method methodInfo = getClass().getMethod(strMethodName);
            return methodInfo.invoke(
                this,
                parameters);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        return null;
    }


    public String ToCsvString()
    {
    	StringBuilder sb = new StringBuilder();
        boolean blnTitleAdded = false;
        HashMap<String, Object> validator =
            new HashMap<String, Object>();

        //
        // String values
        //
        for (Map.Entry<String, String> keyValuePair : m_strValues.entrySet())
        {
            if (blnTitleAdded)
            {
                sb.append(",");
            }
            else
            {
                blnTitleAdded = true;
            }
            String strColName = keyValuePair.getKey();
            try {
				HCException.ThrowIfTrue(validator.containsKey(strColName),
				    "Column already : event " + strColName);
			} catch (HCException e) {
				Logger.log(e);
			}

            validator.put(strColName, null);
            sb.append(keyValuePair.getValue().trim());
        }

        //
        // int values
        //
        for (Map.Entry<String, Integer> keyValuePair : m_intValues.entrySet())
        {
            if (blnTitleAdded)
            {
                sb.append(",");
            }
            else
            {
                blnTitleAdded = true;
            }
            String strColName = keyValuePair.getKey();
            try {
				HCException.ThrowIfTrue(validator.containsKey(strColName),
				    "Column already : event " + strColName);
			} catch (HCException e) {
				Logger.log(e);
			}

            validator.put(strColName, null);
            sb.append(keyValuePair.getValue());
        }

        //
        // double values
        //
        for (Map.Entry<String, Double> keyValuePair : m_dblValues.entrySet())
        {
            if (blnTitleAdded)
            {
                sb.append(",");
            }
            else
            {
                blnTitleAdded = true;
            }
            String strColName = keyValuePair.getKey();
            try {
				HCException.ThrowIfTrue(validator.containsKey(strColName),
				                               "Column already : event " + strColName);
			} catch (HCException e) {
				Logger.log(e);
			}
            validator.put(strColName, null);
            sb.append(keyValuePair.getValue());
        }

        //
        // boolean values
        //
        for (Map.Entry<String, Boolean> keyValuePair : m_blnValues.entrySet())
        {
            if (blnTitleAdded)
            {
                sb.append(",");
            }
            else
            {
                blnTitleAdded = true;
            }
            String strColName = keyValuePair.getKey();
            try {
				HCException.ThrowIfTrue(validator.containsKey(strColName),
				    "Column already : event " + strColName);
			} catch (HCException e) {
				Logger.log(e);
			}

            validator.put(strColName, null);
            sb.append(keyValuePair.getValue());
        }

        //
        // long values
        //
        for (Map.Entry<String, Long> keyValuePair : m_lngValues.entrySet())
        {
            if (blnTitleAdded)
            {
                sb.append(",");
            }
            else
            {
                blnTitleAdded = true;
            }
            String strColName = keyValuePair.getKey();
            try {
				HCException.ThrowIfTrue(validator.containsKey(strColName),
				    "Column already : event " + strColName);
			} catch (HCException e) {
				Logger.log(e);
			}

            validator.put(strColName, null);
            sb.append(keyValuePair.getValue());
        }

        //
        // date values
        //
        for (Map.Entry<String, Date> keyValuePair : m_dateValues.entrySet())
        {
            if (blnTitleAdded)
            {
                sb.append(",");
            }
            else
            {
                blnTitleAdded = true;
            }
            String strColName = keyValuePair.getKey();
            try {
				HCException.ThrowIfTrue(validator.containsKey(strColName),
				    "Column already : event " + strColName);
			} catch (HCException e) {
				Logger.log(e);
			}

            validator.put(strColName, null);
            sb.append(keyValuePair.getValue());
        }

        //
        // properties
        //
        if (m_reflector != null)
        {
            for (String strPropertyName : m_reflector.getColNames())
            {
                if (blnTitleAdded)
                {
                    sb.append(",");
                }
                else
                {
                    blnTitleAdded = true;
                }
                //String strColName = keyValuePair.getKey();
                try {
					HCException.ThrowIfTrue(validator.containsKey(strPropertyName),
					    "Column already : event " + strPropertyName);
				} catch (HCException e) {
					Logger.log(e);
				}

                validator.put(strPropertyName, null);
                Object oValue = GetHardPropertyValue(strPropertyName);
                sb.append(oValue == null ? "" : oValue.toString().trim() + "\n");
            }
        }
        return sb.toString();
    }
    
    @Override
    public String toString()
    {
    	StringBuilder sb =
            new StringBuilder();
        for (String strPropertyName : GetHardPropertyNames())
        {
            Object value = GetHardPropertyValue(strPropertyName);
            sb.append(
                strPropertyName + "=" +
                (value == null ? "" : value.toString().trim()) + " \n");
        }

        for (Map.Entry<String, Boolean> kvp : m_blnValues.entrySet())
        {
            sb.append(
                kvp.getKey().trim() + "=" +
                kvp.toString().trim() + " \n");
        }
        for (Map.Entry<String, Double> kvp : m_dblValues.entrySet())
        {
            sb.append(
                kvp.getKey().trim() + "=" +
                kvp.toString().trim() + " \n");
        }
        for (Map.Entry<String, Integer> kvp : m_intValues.entrySet())
        {
            sb.append(
                kvp.getKey().trim() + "=" +
                kvp.toString().trim() + " \n");
        }
        for (Map.Entry<String, Date> kvp : m_dateValues.entrySet())
        {
            sb.append(
                kvp.getKey().trim() + "=" +
                kvp.toString().trim() + " \n");
        }
        for (Map.Entry<String, Long> kvp : m_lngValues.entrySet())
        {
            sb.append(
                kvp.getKey().trim() + "=" +
                kvp.toString().trim() + " \n");
        }
        for (Map.Entry<String, String> kvp : m_strValues.entrySet())
        {
            sb.append(
                kvp.getKey().trim() + "=" +
                kvp.toString().trim() + " \n");
        }
        for (Map.Entry<String, Object> kvp : m_objValues.entrySet())
        {
            sb.append(
                kvp.getKey().trim() + "=" +
                kvp.toString().trim() + " \n");
        }

        String strDescr = sb.toString().trim();
        if (StringHelper.IsNullOrEmpty(strDescr))
        {
            try {
				throw new HCException("Null description");
			} catch (HCException e) {
				Logger.log(e);
			}
        }
        return strDescr;
    }

    public void Dispose()
    {
        ResetMaps();
    }

    public Date GetDateValue(Enum<?> enumValue)
    {
        return GetDateValue(enumValue.toString());
    }

    public Date GetDateValue(String strPropertyName)
    {
        return TryGetDateValue(strPropertyName);
    }

    private Date GetHardDateValue(String strPropertyName)
    {
        Object objValue = GetHardPropertyValue(strPropertyName);
        if (objValue == null)
        {
            return new Date();
        }
        Date dateValue = (Date)objValue;
        return dateValue;
    }

    public  byte[] GetByteArr()
    {
        ISerializerWriter writer = Serializer.GetWriter();
        try {
			Serialize(writer);
		} catch (HCException e) {
			Logger.log(e);
		}
        return writer.GetBytes();
    }

    public  Object Deserialize(byte[] bytes)
    {
        ISerializerReader serializationReader = Serializer.GetReader(bytes);
        return DeserializeStatic(serializationReader);
    }

    public  void Serialize(ISerializerWriter writerBase) throws HCException
    {
        if (StringHelper.IsNullOrEmpty(m_strClassName))
        {
            throw new HCException("Empty class name");
        }
        writerBase.Write(m_strClassName);
        writerBase.Write(SelfDescribingClass.class);
        ISerializerWriter serializer = SerializeProperties();
        writerBase.Write(serializer.GetBytes());
    }

    protected ISerializerWriter SerializeProperties()
    {
        try
        {
            ISerializerWriter serializer = Serializer.GetWriter();
            synchronized (m_blnValues)
            {
                for (Map.Entry<String, Boolean> kvp : m_blnValues.entrySet())
                {
                    serializer.Write(EnumSerializedType.BooleanType.m_byteItem);
                    serializer.Write(kvp.getKey());
                    serializer.Write(kvp.getValue());
                }
            }
            synchronized (m_dblValues)
            {
                for (Map.Entry<String, Double> kvp : m_dblValues.entrySet())
                {
                    serializer.Write(EnumSerializedType.DoubleType.m_byteItem);
                    serializer.Write(kvp.getKey());
                    serializer.Write(kvp.getValue());
                }
            }

            synchronized (m_intValues)
            {
                for (Map.Entry<String, Integer> kvp : m_intValues.entrySet())
                {
                    serializer.Write(EnumSerializedType.Int32Type.m_byteItem);
                    serializer.Write(kvp.getKey());
                    serializer.Write(kvp.getValue());
                }
            }
            synchronized (m_dateValues)
            {
                for (Map.Entry<String, Date> kvp : m_dateValues.entrySet())
                {
                    serializer.Write(EnumSerializedType.DateTimeType.m_byteItem);
                    serializer.Write(kvp.getKey());
                    serializer.Write(kvp.getValue());
                }
            }
            synchronized (m_lngValues)
            {
                for (Map.Entry<String, Long> kvp : m_lngValues.entrySet())
                {
                    serializer.Write(EnumSerializedType.Int64Type.m_byteItem);
                    serializer.Write(kvp.getKey());
                    serializer.Write(kvp.getValue());
                }
            }
            synchronized (m_strValues)
            {
                for (Map.Entry<String, String> kvp : m_strValues.entrySet())
                {
                    serializer.Write(EnumSerializedType.StringType.m_byteItem);
                    serializer.Write(kvp.getKey());
                    serializer.Write(kvp.getValue());
                }
            }
            synchronized (m_objValues)
            {
                for (Map.Entry<String, Object> kvp : m_objValues.entrySet())
                {
                    serializer.Write(EnumSerializedType.ObjectType.m_byteItem);
                    serializer.Write(kvp.getKey());
                    Object obj = kvp.getValue();

                    if (obj == null)
                    {
                        serializer.Write(EnumSerializedType.NullType.m_byteItem);
                    }
                    else
                    {
                        serializer.Write(EnumSerializedType.NonNullType.m_byteItem);
                        //Type type = obj.getClass();
                        serializer.Write(obj);
                    }
                }
            }

            //
            // set property values
            //
            Reflector expressionBinder = ReflectionCache.getReflector(getClass());

            //
            // map properties for the given Object
            //
            for (String strPropertyName : expressionBinder.getColNames())
            {
                if (ContainsHardProperty(strPropertyName))
                {
                    Object objValue = expressionBinder.getPropValue(
                        this,
                        strPropertyName);
                    //Type propertyType;
                    if (objValue == null)
                    {
                        if (StringHelper.IsNullOrEmpty(GetClassName()))
                        {
                            String strMessage = "Null Object value for property name [" +
                                                strPropertyName + "]. class: [" + GetClassName() +
                                                "]";
                            throw new HCException("Empty class name. " +
                                                  strMessage);
                        }
                        //propertyType = expressionBinder.getPropertyType(strPropertyName);
                        //objValue = ReflectionHelper.GetDefaltValue(propertyType);
                    }
                    else
                    {
                        //propertyType = objValue.getClass();
                    }

                    //serializer.Write((byte) PrimitiveTypesCache.GetSerializedPrimitiveType(propertyType));
                    serializer.Write(strPropertyName);
                    serializer.Write(objValue);
                }
            }

            serializer.Write(EnumSerializedType.EndOfProperties.m_byteItem);
            return serializer;
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return null;
    }

    /// <summary>
    /// Leave it public, and do not change the name of the method.
    /// We need it for serialization parsing
    /// </summary>
    /// <param name="serializationReader"></param>
    /// <returns></returns>
    public static Object DeserializeStatic(
        ISerializerReader serializationReader)
    {
        String strClassName = serializationReader.ReadString();
        Type type = serializationReader.readType();
        ASelfDescribingClass selfDescribingClass;
        if(type != SelfDescribingClass.class)
        {
            selfDescribingClass = (ASelfDescribingClass)
                ReflectionCache.getReflector((Class<?>)type).createInstance();
        }
        else
        {
            //
            // get factory
            //
            SelfDescribingClassFactory classFactory = SelfDescribingClassFactory.CreateFactory(
                strClassName);
            if(classFactory.Properties.size() > 0)
            {
                selfDescribingClass = classFactory.createInstance();
            }
            else
            {
                selfDescribingClass = new SelfDescribingClass();
            }
        }
         
        selfDescribingClass.SetClassName(strClassName);
        byte[] bytes = serializationReader.ReadByteArray();

        if (bytes != null)
        {
            ISerializerReader reader = Serializer.GetReader(
                bytes);

            DeserializeProperties(selfDescribingClass,
                                  reader);
        }
        return selfDescribingClass;
    }

    public static void DeserializeProperties(
        ASelfDescribingClass selfDescribingClass,
        ISerializerReader serializationReader)
    {
        while (serializationReader.getBytesRemaining() > 0)
        {
        	EnumSerializedType serializedType = 
                new EnumSerializedType(serializationReader.readByte());

            if(serializedType == EnumSerializedType.EndOfProperties)
            {
                break;
            }

            String strKey = serializationReader.ReadString();
            if (serializedType == EnumSerializedType.DoubleType)
            {
                double objValue = serializationReader.ReadDouble();
                selfDescribingClass.SetDblValue(strKey, objValue);
            }
            else if (serializedType == EnumSerializedType.Int32Type)
            {
                int objValue = serializationReader.ReadInt32();
                selfDescribingClass.SetIntValue(strKey, objValue);
            }
            else if (serializedType == EnumSerializedType.BooleanType)
            {
                boolean objValue = serializationReader.ReadBoolean();
                selfDescribingClass.SetBlnValue(strKey, objValue);
            }
            else if (serializedType == EnumSerializedType.DateTimeType)
            {
                Date objValue = serializationReader.ReadDateTime();
                selfDescribingClass.SetDateValue(strKey, objValue);
            }
            else if (serializedType == EnumSerializedType.StringType)
            {
                String objValue = serializationReader.ReadString();
                selfDescribingClass.SetStrValue(strKey, objValue);
            }
            else if (serializedType == EnumSerializedType.Int64Type)
            {
                long objValue = serializationReader.ReadInt64();
                selfDescribingClass.SetLngValue(strKey, objValue);
            }
            else if (serializedType == EnumSerializedType.ObjectType)
            {
            	EnumSerializedType serializedObjType = new EnumSerializedType(serializationReader.readByte());
                if (serializedObjType == EnumSerializedType.NullType)
                {
                    selfDescribingClass.SetObjValueToDict(strKey, null);
                }
                else
                {
                    Object objValue = serializationReader.ReadObject();
                    selfDescribingClass.SetObjValueToDict(strKey, objValue);
                }
            }
            else
            {
                throw new NotImplementedException();
            }
        }
    }

    public Object GetValueFromAnyProperty(
        String strPropertyName)
    {
        return TryGetValueFromAnyProperty(
            strPropertyName);
    }

    public Object TryGetValueFromAnyProperty(
        String strPropertyName)
    {
        //Object objResult = null;
        if(m_reflector.getPropertyType(strPropertyName) == boolean.class){
        	return TryGetBlnValue(strPropertyName);
        }
        
        if(m_reflector.getPropertyType(strPropertyName) == Date.class){
        	return TryGetDateValue(strPropertyName);
        }
        
        if(m_reflector.getPropertyType(strPropertyName) == double.class){
        	return TryGetDblValue(strPropertyName);
        }
        
        if(m_reflector.getPropertyType(strPropertyName) == int.class){
        	return TryGetIntValue(strPropertyName);
        }
        
        if(m_reflector.getPropertyType(strPropertyName) == long.class){
        	return TryGetLngValue(strPropertyName);
        }
        
        if(m_reflector.getPropertyType(strPropertyName) == String.class){
        	return TryGetStrValue(strPropertyName);
        }
        
        return TryGetObjValue(strPropertyName);
    }

    public Object GetHardPropertyObjValue(String strPropertyName)
    {
        ValidateReflector();
        return m_reflector.getPropValue(this, strPropertyName);
    }

    private void ValidateReflector()
    {
        if (m_reflector == null)
        {
            //
            // the binder will stay null, unless this class has been compiled
            //
            if (!StringHelper.IsNullOrEmpty(m_strClassName))
            {
                String strTypeName = getClass().getName();
                if (strTypeName.equals(m_strClassName) ||
                    (!strTypeName.equals(SelfDescribingClass.class.getName()) &&
                    !strTypeName.equals(ASelfDescribingClass.class.getName())))
                {
                    m_reflector = ReflectionCache.getReflector(getClass());
                }
            }
        }
    }

    public String TryGetStrValue(String strPropertyName)
    {
        synchronized (m_strValues)
        {
            if (ContainsHardProperty(strPropertyName))
            {
                String strValue = GetHardStrValue(strPropertyName);
                return strValue;
            }
            String strValue = m_strValues.get(
                strPropertyName);
            return strValue;
        }
    }

    public double TryGetDblValue(Enum<?> enumValue)
    {
        return TryGetDblValue(enumValue.toString());
    }

    public double TryGetDblValue(String strPropertyName)
    {
        synchronized (m_dblValues)
        {
            if (ContainsHardProperty(strPropertyName))
            {
                double dblValue = GetHardDblValue(strPropertyName);
                return dblValue;
            }
            return m_dblValues.get(strPropertyName);
        }
    }

    public Object TryGetObjValue(Enum<?> enumValue)
    {
        return TryGetObjValue(enumValue.toString());
    }

    public Date TryGetDateValue(Enum<?> enumValue)
    {
        return TryGetDateValue(enumValue.toString());
    }

    public long TryGetLngValue(Enum<?> enumValue)
    {
        return TryGetLngValue(enumValue.toString());
    }

    public boolean TryGetBlnValue(Enum<?> enumValue)
    {
        return TryGetBlnValue(enumValue.toString());
    }

    public String TryGetStrValue(Enum<?> enumValue)
    {
        return TryGetStrValue(enumValue.toString());
    }

    public int TryGetIntValue(Enum<?> enumValue)
    {
        return TryGetIntValue(enumValue.toString());
    }

    public int TryGetIntValue(String strPropertyName)
    {
        synchronized (m_intValues)
        {
            if (ContainsHardProperty(strPropertyName))
            {
                int intValue = GetHardIntValue(strPropertyName);
                return intValue;
            }
            return m_intValues.get(strPropertyName);
        }
    }

    public boolean TryGetBlnValue(String strPropertyName)
    {
        synchronized (m_blnValues)
        {
            if (ContainsHardProperty(strPropertyName))
            {
                boolean blnValue = GetHardBlnValue(strPropertyName);
                return blnValue;
            }
            if(m_blnValues.containsKey(strPropertyName)){
            	return m_blnValues.get(strPropertyName);
            }
        }
        return false;
    }

    public long TryGetLngValue(String strPropertyName)
    {
        synchronized (m_lngValues)
        {
            if (ContainsHardProperty(strPropertyName))
            {
                long lngValue = GetHardLngValue(strPropertyName);
                return lngValue;
            }
            return m_lngValues.get(strPropertyName);
        }
    }

    public Date TryGetDateValue(String strPropertyName)
    {
        synchronized (m_dateValues)
        {
            if (ContainsHardProperty(strPropertyName))
            {
                Date dateTime = GetHardDateValue(strPropertyName);
                return dateTime;
            }
            return m_dateValues.get(
                strPropertyName);
        }
    }

    public Object TryGetObjValue(String strPropertyName)
    {
        synchronized (m_objValues)
        {
            if (ContainsHardProperty(strPropertyName))
            {
                Object objValue = GetHardObjValue(strPropertyName);
                return objValue;
            }
            return m_objValues.get(
                strPropertyName);
        }
    }

    public Object GetPropertyValueByType(Type propertyType, String strPropertyName) throws Exception
    {
        if (propertyType == Integer.class)
        {
            return GetIntValue(strPropertyName);
        }
        if (propertyType == Double.class)
        {
            return GetDblValue(strPropertyName);
        }
        if (propertyType == Long.class)
        {
            return GetLngValue(strPropertyName);
        }
        if (propertyType == Boolean.class)
        {
            return GetBlnValue(strPropertyName);
        }
        if (propertyType == String.class)
        {
            return GetStrValue(strPropertyName);
        }
        if (propertyType == Object.class)
        {
            return GetObjValue(strPropertyName);
        }
        throw new Exception();
    }

    public void SetValueToDictByType(String strPropertyName, Object objProperty)
    {
        Type propertyType = objProperty.getClass();
        if (propertyType == Integer.class)
        {
            SetIntValue(strPropertyName, (int)objProperty);
        }
        else if (propertyType == Double.class)
        {
            SetDblValue(strPropertyName, (double)objProperty);
        }
        else if (propertyType == Long.class)
        {
            SetLngValue(strPropertyName, (long)objProperty);
        }
        else if (propertyType == Boolean.class)
        {
            SetBlnValue(strPropertyName, (Boolean)objProperty);
        }
        else if (propertyType == String.class)
        {
            SetStrValue(strPropertyName, objProperty.toString());
        }
        else if (propertyType == Object.class)
        {
            SetObjValueToDict(strPropertyName, objProperty);
        }
        else if (propertyType == Date.class)
        {
            SetDateValue(strPropertyName, (Date)objProperty);
        }
        else
        {
            SetObjValueToDict(strPropertyName, objProperty);
        }
    }

    public void ResetMaps()
    {
        m_blnValues.clear();
        m_dateValues.clear();
        m_dblValues.clear();
        m_intValues.clear();
        m_lngValues.clear();
        m_strValues.clear();
        m_objValues.clear();
    }

    //public void Dispose()
    //{
    //    ResetMaps();
    //}
}
