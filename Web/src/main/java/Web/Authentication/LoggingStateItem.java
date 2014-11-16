package Web.Authentication;

public class LoggingStateItem {

	private boolean m_blnIsLogged;
	private String m_strUserName;
	private String m_strSessionName;
	
	
	public boolean getIsLogged(){
		return m_blnIsLogged;
	}
	
	public void setIsLogged(boolean blnIsLogged){
		m_blnIsLogged = blnIsLogged;
	}

	public String getUserName() {
		return m_strUserName;
	}

	public void setUserName(String m_strUserName) {
		this.m_strUserName = m_strUserName;
	}

	public String getSessionName() {
		return m_strSessionName;
	}

	public void setSessionName(String m_strSessionName) {
		this.m_strSessionName = m_strSessionName;
	}

	public void removeSession() {
		m_blnIsLogged = false;
		m_strSessionName = "";
		m_strUserName = "";
	}
}
