package Armadillo.Core.Cache;

import java.sql.DriverManager;
import Armadillo.Core.Logger;
import Armadillo.Core.Reflection.Reflector;

public class GenericDbJdbcWrapper<T> extends SqliteJdbcWrapper<T>{

	public GenericDbJdbcWrapper(
			String strFileName,
			Reflector reflector,
			String strDriver,
			EnumDbType enumDbType) {
		super(strFileName,
			reflector,
			strDriver,
			enumDbType);
	}
		
	@Override
	protected void openDb() 
	{
		try 
		{
			Class.forName(m_strDriver).newInstance();
			String strUrl = m_strFileName;
			String strMessage ="Loading connection ["+ strUrl + "]..."; 
			Logger.log(strMessage);
			DriverManager.setLoginTimeout(180);
			
			m_dbConnRaw = DriverManager
					.getConnection(strUrl);
			
			strMessage ="Loaded connection ["+ strUrl + "]"; 
			Logger.log(strMessage);
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
	
}
