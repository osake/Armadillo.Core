package Web.Authentication;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import Armadillo.Core.Logger;
import Web.Base.AWebConstants;

public abstract class AauthenticationFilter implements Filter 
{
	private ServletContext m_context;
	
	public void init(FilterConfig fConfig) throws ServletException 
	{
		m_context = fConfig.getServletContext();
		m_context.log("AuthenticationFilter initialized");
	}
	
	public void doFilter(ServletRequest request, 
			ServletResponse response, 
			FilterChain chain) throws IOException, ServletException 
	{
		try
		{
			HttpServletRequest req = (HttpServletRequest) request;
			HttpServletResponse res = (HttpServletResponse) response;
			
			String strUri = req.getRequestURI();
			this.m_context.log("Requested Resource::"+strUri);
			
			HttpSession session = req.getSession(false);
			
			if(LoggingSessionHelper.isRestrictedUri(strUri)){
				
				if(session != null)
				{
					String strSessionId = session.getId();
					if(!LoggingSessionHelper.isUserLoggedIn(strSessionId))
					{
						this.m_context.log("Unauthorized access request. User not logged in.");
						res.sendRedirect(AWebConstants.getOwnInstance().getMainPageName());
						return;
					}
				}
				else
				{
					this.m_context.log("Unauthorized access request. Restricted url");
					res.sendRedirect(AWebConstants.getOwnInstance().getMainPageName());
					return;
				}
			}
			
			chain.doFilter(request, response);
		}
		catch(Exception ex){
			Logger.log(ex);
		}
	}

	public void destroy() 
	{
		//close any resources here
	}

}
