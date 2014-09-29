package Armadillo.Core.Cache;

import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.Verboser;
import Armadillo.Core.Concurrent.EfficientMemoryBuffer;
import Armadillo.Core.Concurrent.LockHelper;
import Armadillo.Core.Io.FileHelper;
import Armadillo.Core.Io.PathHelper;
import Armadillo.Core.Reflection.Reflector;

public class SqliteCacheConnectionPool {

	private static EfficientMemoryBuffer<String, ISqliteCacheBase> m_sqliteCacheWrapperBuffer;
	private final static Object m_dbCounterLock = new Object();
	private static int m_intDbCounter = 0;
	private final static Object m_dbLock = new Object();
	private final static Object m_lockBufferCahes = new Object();
	
	static{
		
		m_sqliteCacheWrapperBuffer =
				new EfficientMemoryBuffer<String, ISqliteCacheBase>(SqliteConstants.CONNECTIONS_SIZE)
				{
					@Override
					public void onItemRemoved(ISqliteCacheBase removedItem)
					{
						if(removedItem == null)
						{
							return;
						}
						
						try 
						{
							removedItem.close();
							Verboser.Talk("Disposed db connection [" + 
									removedItem.getFileName() + "]");
							synchronized(m_dbCounterLock)
							{
								m_intDbCounter--;
							}
							Verboser.Talk("Db connections[" + m_intDbCounter + "]");
							
						} 
						catch (Exception ex) 
						{
							Logger.log(ex);
						}
					}
				};
	}
	
	public static ISqliteCacheBase getDbWrapper(
			String strFileName,
			Reflector reflector,
			EnumDbType enumDbType){
		
		ISqliteCacheBase cacheWrapper = null;
		try 
		{
			String strWrapperKey = strFileName + "dbKey";
			cacheWrapper = m_sqliteCacheWrapperBuffer.get(strWrapperKey);
			
			if(cacheWrapper == null)
			{
				
				synchronized(LockHelper.GetLockObject(strWrapperKey))
				{
					cacheWrapper = m_sqliteCacheWrapperBuffer.get(strWrapperKey);
					if(cacheWrapper == null)
					{
						
						cacheWrapper = createDbWrapperInstance(
								strFileName,
								reflector,
								enumDbType);
						
						synchronized(m_lockBufferCahes )
						{
							m_sqliteCacheWrapperBuffer.put(strWrapperKey, cacheWrapper);
						}
						
						synchronized(m_dbCounterLock)
						{
							m_intDbCounter++;
						}
						Verboser.Talk("Db connections[" + m_intDbCounter + "]");
					}
				}
			}
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
			Logger.log(new Exception("Filed to load db [" + strFileName + "]"));
		}
		return cacheWrapper;
	}	

	private static <T> ISqliteCacheBase createDbWrapperInstance(
			String m_strFileName,
			Reflector m_reflector,
			EnumDbType enumDbType) {
		
		try
		{
			SqliteJdbcWrapper<T> dbWrapper = null;
			
			if(enumDbType == EnumDbType.SqLite)
			{
				String strDir = FileHelper.getDirectory(m_strFileName);
				
				if(!PathHelper.Exists(strDir)){
					PathHelper.createDir(strDir);
				}
				dbWrapper = new SqliteJdbcWrapper<T>(
						m_strFileName,
						m_reflector,
						SqliteConstants.DEFAULT_DRIVER,
						enumDbType);
				
				if(!FileHelper.exists(m_strFileName)){
					Thread.sleep(2000);
					synchronized(m_dbLock )
					{
						dbWrapper = new SqliteJdbcWrapper<>
						(
								m_strFileName,
								m_reflector,
								SqliteConstants.DEFAULT_DRIVER,
								enumDbType);
					}
					if(!FileHelper.exists(m_strFileName)){
						throw new Exception("Db file not found [" + m_strFileName + "]");
					}
					
				}
			}
			else if(enumDbType == EnumDbType.Oracle){
				
				dbWrapper = new GenericDbJdbcWrapper<T>(
						m_strFileName,
						m_reflector,
						"oracle.jdbc.OracleDriver",
						enumDbType);
			}
			else{
				throw new HCException("Not implemented");
			}
			
			String strMessage = "Loaded db file ["+ m_strFileName +"]";
			Verboser.Talk(strMessage);
			
			return dbWrapper;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
			Logger.log(new Exception("Filed to load db [" + m_strFileName + "]"));
		}
		return null;
	}	
}
