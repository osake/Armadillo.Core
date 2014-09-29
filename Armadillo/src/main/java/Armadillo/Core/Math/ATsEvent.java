package Armadillo.Core.Math;

import java.util.Date;

import Armadillo.Core.Reflection.ReflectionCache;
import Armadillo.Core.Reflection.Reflector;

public abstract class ATsEvent implements ITsEvent
{
    public Date Time;
    private Reflector m_reflector;
    //public TsDataRequest TsDataRequest { get; set; }

    public ATsEvent ParseCsvString(
        String strLine)
    {
        LoadBinder();
        Object item = m_reflector.createInstance();
        TsEventHelper.ParseCsvString(
            strLine,
            item,
            m_reflector.getColNames());
        return (ATsEvent)item;
    }

    public String ToCsvString()
    {
        String strCsvString =
            TsEventHelper.ToCsvString(
                this,
                getClass());
        return strCsvString;
    }

    public Object GetHardPropertyValue(String strFieldName)
    {
        LoadBinder();
        return m_reflector.getPropValue(
            this,
            strFieldName);
    }

    private void LoadBinder()
    {
        if (m_reflector == null)
        {
            m_reflector = ReflectionCache.getReflector(getClass());
        }
    }

    @Override
    public String toString()
    {
        LoadBinder();
        StringBuilder sb = new StringBuilder();
        boolean blnIsTitle = true;
        for (String strPropertyName : m_reflector.getColNames())
        {
            if (!blnIsTitle)
            {
                sb.append(",\n");
            }
            else
            {
                blnIsTitle = false;
            }
            sb.append(strPropertyName + " = " +
                m_reflector.getPropValue(this, strPropertyName));
        }
        return sb.toString();
    }

    public void Dispose()
    {
//        if (TsDataRequest != null)
//        {
//            TsDataRequest.Dispose();
//            TsDataRequest = null;
//        }
        //m_reflector = null;
    }
}
