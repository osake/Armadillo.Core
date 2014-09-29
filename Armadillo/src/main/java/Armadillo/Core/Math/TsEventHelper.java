package Armadillo.Core.Math;

import java.lang.reflect.Type;
import java.sql.Date;

import org.joda.time.DateTime;

import Armadillo.Core.DateHelper;
import Armadillo.Core.Logger;
import Armadillo.Core.ParserHelper;
import Armadillo.Core.Reflection.ReflectionCache;
import Armadillo.Core.Reflection.Reflector;

public class TsEventHelper {

    public static <T> void ParseCsvString(
            String strLine,
            T tEvent,
            String[] strTitles)
        {
            String[] strTokens = strLine.split(",");
            int intIndex = 0;

            Reflector expressionBinder = ReflectionCache.getReflector(
            		tEvent.getClass());

            for (String strTitle : strTitles)
            {
                if (!expressionBinder.ContainsProperty(strTitle))
                {
                    intIndex++;
                    continue;
                }
                if (!expressionBinder.CanWriteProperty(strTitle))
                {
                    continue;
                }
                if (intIndex > strTokens.length - 1)
                {
                    break;
                }
                String strToken = strTokens[intIndex];
                Type type = expressionBinder.getPropertyType(strTitle);
                if (type == Date.class)
                {
                    expressionBinder.SetPropertyValue(
                        tEvent,
                        strTitle,
                        DateHelper.ParseDefaultDateTimeString(strToken));
                }
                else
                {
                    expressionBinder.SetPropertyValue(
                        tEvent,
                        strTitle,
                        ParserHelper.ParseString(
                            strToken,
                            type));
                }
                intIndex++;
            }
        }

	public static java.lang.String ToCsvString(ATsEvent aTsEvent,
			Class<?> class1) {
		 return ToCsvString(aTsEvent, aTsEvent.getClass());
    }

    public static String ToCsvString(
        Object obj,
        Class<?> type)
    {
        try
        {
            Reflector binder = ReflectionCache.getReflector(type);
            String[] propertyNames = binder.getColNames();
            Object[] objArr = new Object[propertyNames.length]; 
            for (int i = 0; i < propertyNames.length; i++) {
            	objArr[i] = binder.getPropValue(obj, propertyNames[i]);
			}
            return ToCsvString(objArr);

        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        return "";
    }
    

    public static String ToCsvString(Object[] oArr)
    {
        StringBuilder sb = new StringBuilder();
        if (oArr[0] instanceof Date)
        {
            sb.append(
                ((DateTime) oArr[0]).toString());
        }
        else
        {
            if (oArr[0] != null)
            {
                sb.append(oArr[0].toString()
                              .replace(",", "_")
                              .replace("\n", "")
                              .replace("\t", "")
                              .trim());
            }
        }

        for (int i = 1; i < oArr.length; i++)
        {
            sb.append(",");
            if (oArr[i] instanceof DateTime)
            {
                sb.append(
                    ((DateTime) oArr[i]).toString());
            }
            else
            {
                if (oArr[i] != null)
                {
                    //
                    // remove invalid characters
                    //
                    sb.append(oArr[i].toString()
                                  .replace(",", "_")
                                  .replace("\n", "")
                                  .replace("\t", "")
                                  .trim());
                }
                else
                {
                    //
                    // add an empty dummy value
                    //
                    sb.append("");
                }
            }
        }
        return sb.toString();
    }    
}
