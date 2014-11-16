package Web.Authentication;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import Armadillo.Core.Logger;
import Armadillo.Core.Text.StringHelper;
import Web.Base.AWebConstants;

public abstract class ALoginServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException 
	{
		try
		{
			// get request parameters for userID and password
			String strUser = request.getParameter("user");
			String strPwd = request.getParameter("pwd");
			
			UserItem userItem;
			if(!StringHelper.IsNullOrEmpty(strUser) &&
			   !StringHelper.IsNullOrEmpty(strPwd) &&
			    UserCacheHelper.containsKey(strUser) &&
			   (userItem = UserCacheHelper.getUserFromCache(strUser)) != null &&
			   !StringHelper.IsNullOrEmpty(userItem.getPsw()) &&
				userItem.getPsw().equals(strPwd))
			{
				loginSuccess(request, response, strUser);
			}
			else
			{
				loginFailure(request, response);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
			loginFailure(request, response);
		}
	}

	private void loginSuccess(HttpServletRequest request,
			HttpServletResponse response, 
			String strUser) throws IOException 
	{
		try
		{
			HttpSession session = request.getSession();
			
			LoggingStateItem loggingStateBean = new LoggingStateItem();
			loggingStateBean.setIsLogged(true);
			loggingStateBean.setSessionName(session.getId());
			loggingStateBean.setUserName(strUser);
			
			LoggingSessionHelper.addUserToSession(
					session.getId(),
					loggingStateBean);
			session.setAttribute("user", strUser);
			//setting session to expiry in x*60 mins
			session.setMaxInactiveInterval(AWebConstants.getOwnInstance().getMaxInactiveSessionInterval());
			Cookie userName = new Cookie("user", strUser);
			userName.setMaxAge(AWebConstants.getOwnInstance().getMaxInactiveSessionInterval());
			response.addCookie(userName);
	
			response.sendRedirect(
					AWebConstants.getOwnInstance().getMainPageName());
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	private void loginFailure(HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException 
	{
		try
		{
			RequestDispatcher rd = getServletContext().getRequestDispatcher("/" + 
						AWebConstants.getOwnInstance().getLoginPageName());
			if(rd != null)
			{
				PrintWriter out= response.getWriter();
				out.println("<font color=red>Either user name or password is wrong.</font>");
				rd.include(request, response);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
}
