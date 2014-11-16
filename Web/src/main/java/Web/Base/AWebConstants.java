package Web.Base;

import java.util.List;

public abstract class AWebConstants 
{
	private String m_strMainPageName;
	private String m_strLogingPageName;
	private String m_strUsersDbFileName;
	private List<String> m_restrictedPages;
	private int m_intMaxInactiveSessionInterval;
	protected static AWebConstants m_ownInstance;

	public AWebConstants(
			String strMainPageName,
			String strLogingPageName,
			String strUsersDbFileName,
			int intMaxInactiveSessionInterval,
			List<String> restrictedPages)
	{
		m_intMaxInactiveSessionInterval = intMaxInactiveSessionInterval;
		m_strMainPageName = strMainPageName;
		m_strLogingPageName = strLogingPageName;
		m_strUsersDbFileName = strUsersDbFileName;
		m_restrictedPages = restrictedPages;
	}
	
	public String getMainPageName() 
	{
		return m_strMainPageName;
	}

	public String getLoginPageName() 
	{
		return m_strLogingPageName;
	}

	public String getUsersDbFileName() 
	{
		return m_strUsersDbFileName;
	}

	public static AWebConstants getOwnInstance() 
	{
		return m_ownInstance;
	}

	public static void SetOwnInstance(AWebConstants webConstants) 
	{
		m_ownInstance = webConstants;
	}

	public List<String> getRestrictedPages() 
	{
		return m_restrictedPages;
	}

	public int getMaxInactiveSessionInterval() 
	{
		return m_intMaxInactiveSessionInterval;
	}
}