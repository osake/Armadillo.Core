package Armadillo.Core.UI;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Core.Guid;
import Armadillo.Core.Logger;

public class LiveGuiPublisher 
{
	private static List<AGuiCallback> m_callbacks;
	private static Object m_callbacksLock;
	
	static
	{
		m_callbacksLock = new Object();
		m_callbacks = new ArrayList<AGuiCallback>();
	}
	
	public static void publishStrMessage(String strReceived) {
		
		synchronized(m_callbacksLock)
		{
			if(m_callbacks != null &&
			   m_callbacks.size() > 0)
			{
				for(AGuiCallback callback : m_callbacks)
				{
					callback.OnStr(strReceived);
					strReceived = null;
				}
			}
		}
	}

	public static void addCallback(AGuiCallback callBack)
	{
		try
		{
	        synchronized(m_callbacksLock)
	        {
				m_callbacks.add(callBack);
	        }
		} 
		catch (Exception e) 
		{
			Logger.log(e);
		}
	}
	
	public static boolean PublishLineChartRow(
			String str1, 
			String str2,
			String str3, 
			String strSeries, 
			String strX, 
			double dblY)
	{
		try
		{
			synchronized(m_callbacksLock)
			{
				if(m_callbacks != null &&
				   m_callbacks.size() > 0)
				{
					for(AGuiCallback callback : m_callbacks)
					{
						callback.PublishLineChartRow(
								str1, 
								str2,
								str3, 
								strSeries, 
								strX, 
								dblY);
					}
					return true;
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return false;
	}
	
	public static void PublishGui(
			String string, 
			String string2, 
			String string3,
			String strKey, 
			Object jobLog) 
	{
		try
		{
			synchronized(m_callbacksLock)
			{
				if(m_callbacks != null &&
				   m_callbacks.size() > 0)
				{
					for(AGuiCallback callback : m_callbacks)
					{
						callback.PublishTableRow(
								string, 
								string2, 
								string3,
								strKey, 
								jobLog);
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public static void PublishLog(
			String string, 
			String string2, 
			String string3,
			String string4, 
			String strLog) 
	{
		Logger.log(strLog);
		PublishGui(string, 
				string2, 
				string3, 
				Guid.NewGuid().toString(), 
				new PublishLogItem(strLog));
	}
}
