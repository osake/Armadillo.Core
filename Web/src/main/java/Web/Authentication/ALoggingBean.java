package Web.Authentication;

import javax.faces.context.FacesContext;

import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Web.Base.AWebConstants;
import Web.Base.WebHelper;

public abstract class ALoggingBean 
{
	public ALoggingBean()
	{
		try
		{
			String strMessage = "Bean created [" + getClass().getName() + "]";
			Logger.log(strMessage);
			Console.writeLine(strMessage);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public void getLogOut()
	{
		logOut();
	}
	
	public void logOut()
	{
		try
		{
			String strSessionId = getSessionId();
			
			LoggingSessionHelper.removeSession(strSessionId);
			FacesContext.getCurrentInstance().getExternalContext().redirect(
					AWebConstants.getOwnInstance().getMainPageName());
			
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public boolean getIsLoggedIn()
	{
		try
		{
			String strSessionId = getSessionId();
			return LoggingSessionHelper.isUserLoggedIn(strSessionId);
		}
		catch(Exception ex) 
		{
			Logger.log(ex);
		}
		return false;
	}
	
	private String getSessionId()
	{
		try
		{
			return WebHelper.getSessionIdStatic();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
	}
	

	public String getUserDetails()
	{
	
		try
		{
			String strSessionId = getSessionId();
			LoggingStateItem logginStateBean = LoggingSessionHelper.getLogginStateBean(strSessionId);
			String strUser = "User: [" + logginStateBean.getUserName() + "]";
			return strUser;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
	}

}
