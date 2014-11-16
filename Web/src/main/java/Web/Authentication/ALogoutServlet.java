package Web.Authentication;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import Web.Base.AWebConstants;

public abstract class ALogoutServlet extends HttpServlet 
{
	protected static final long serialVersionUID = 1L;
    
    protected void doPost(
    		HttpServletRequest request, 
    		HttpServletResponse response) throws ServletException, IOException 
    {
    	response.setContentType("text/html");
    	Cookie[] cookies = request.getCookies();
    	if(cookies != null)
    	{
	    	for(Cookie cookie : cookies)
	    	{
	    		if(cookie.getName().equals("JSESSIONID"))
	    		{
	    			System.out.println("JSESSIONID="+cookie.getValue());
	    			break;
	    		}
	    	}
    	}
    	//invalidate the session if exists
    	HttpSession session = request.getSession(false);
    	
    	if(session != null)
    	{
    		System.out.println("User="+session.getAttribute("user"));
    	
	    	LoggingSessionHelper.removeSession(session.getId());
    	}
    	response.sendRedirect(AWebConstants.getOwnInstance().getMainPageName());
    }

}
