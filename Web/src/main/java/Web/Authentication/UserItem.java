package Web.Authentication;

public class UserItem 
{
	public String m_strUser;
	public String m_strPsw;
	public String m_strEmail;

	public String getUser() 
	{
		return m_strUser;
	}
	
	public void setUser(String strUser) 
	{
		this.m_strUser = strUser;
	}

	public String getPsw() 
	{
		return m_strPsw;
	}
	public void setPsw(String strPsw) 
	{
		m_strPsw = strPsw;
	}

	public String getEmail() 
	{
		return m_strEmail;
	}
	
	public void setEmail(String strEmail) 
	{
		m_strEmail = strEmail;
	}
}
