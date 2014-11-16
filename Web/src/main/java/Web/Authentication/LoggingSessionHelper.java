package Web.Authentication;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import Armadillo.Core.Logger;
import Armadillo.Core.Text.StringHelper;
import Web.Base.AWebConstants;

public class LoggingSessionHelper 
{
	
	private static ConcurrentHashMap<String,LoggingStateItem> m_mapSessionToUser;
	private static HashMap<String,String> m_restrictedPages;
	
	static
	{
		try
		{
			m_mapSessionToUser = 
					new ConcurrentHashMap<String,LoggingStateItem>();
			m_restrictedPages = new HashMap<String,String>();
			if(AWebConstants.getOwnInstance() != null)
			{
				List<String> restrictedPages = AWebConstants.getOwnInstance().getRestrictedPages();
				for(String strPage : restrictedPages)
				{
					m_restrictedPages.put(strPage, "");
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public static LoggingStateItem getLogginStateBean(String strSessionId)
	{
		try
		{
			if(m_mapSessionToUser.containsKey(strSessionId))
			{
				return m_mapSessionToUser.get(strSessionId);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
	
	public static boolean isRestrictedUri(String strUri) 
	{
		try
		{		
			if(StringHelper.IsNullOrEmpty(strUri))
			{
				return false;
			}
			String[] tokens = strUri.split("/");
			
			return m_restrictedPages.containsKey(tokens[tokens.length - 1]);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return true;
	}

	public static boolean isUserLoggedIn(String strSessionId) 
	{
		try
		{
			return m_mapSessionToUser.containsKey(strSessionId);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return false;
	}
	
	public static void removeSession(String sessionId) 
	{
		try
		{
			if(m_mapSessionToUser.containsKey(sessionId))
			{
				m_mapSessionToUser.remove(sessionId);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public static void addUserToSession(String sessionId, LoggingStateItem logginStateBean) 
	{
		try
		{
			m_mapSessionToUser.put(sessionId, logginStateBean);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
}