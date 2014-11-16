package Armadillo.Core.Cache;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import Armadillo.Core.Console;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.Verboser;
import Armadillo.Core.Concurrent.EfficientProducerConsumerQueue;
import Armadillo.Core.Concurrent.LockHelper;
import Armadillo.Core.Concurrent.ProducerConsumerQueue;
import Armadillo.Core.Concurrent.Task;
import Armadillo.Core.Io.FileHelper;
import Armadillo.Core.Reflection.Reflector;

public class SqliteTaskQueues 
{
	private static HashMap<String,EfficientProducerConsumerQueue<SqliteWriteJob>> m_writerQueueMap;
	private static HashMap<String,ProducerConsumerQueue<SqliteReadJob>> m_readerQueueMap;
	private static HashMap<String,String> m_mapFileNameToDriveLetter;
	private static DateTime m_lastReadLogTime = DateTime.now();
	private static DateTime m_lastWriteTime = DateTime.now();
	private static Object m_lockObj = new Object();
	
	static
	{
		m_writerQueueMap = new HashMap<String,EfficientProducerConsumerQueue<SqliteWriteJob>>();
		m_readerQueueMap = new HashMap<String,ProducerConsumerQueue<SqliteReadJob>>();
		m_mapFileNameToDriveLetter = new HashMap<String,String>();
	}
	
	public static ProducerConsumerQueue<SqliteReadJob> getReadQueue(
			String strFileName,
			EnumDbType enumDbType)
	{
		ProducerConsumerQueue<SqliteReadJob> readerQueue;
		String strDriveLetter = GetDriveLtter(
				strFileName,
				enumDbType);
		synchronized(m_lockObj)
		{
			if(!m_readerQueueMap.containsKey(strDriveLetter)) 
			{
				readerQueue = new ProducerConsumerQueue<SqliteReadJob>(
						
						SqliteConstants.READ_THREAD_SIZE, 
						SqliteConstants.ITEMS_TO_READ_IN_MEMORY)
						{
								@Override
								public void runTask(SqliteReadJob sqliteReadJob) 
								{
									
									if(sqliteReadJob.getIsOnlyRead())
									{
										executeStatementWithReadLock(sqliteReadJob);
										sqliteReadJob.close();
									}
									else
									{
										executeLoad(sqliteReadJob);
									}
								}
						};
				m_readerQueueMap.put(strDriveLetter, readerQueue);
			}
			else
			{
				readerQueue = m_readerQueueMap.get(strDriveLetter);
			}
		}
		return readerQueue;
	}

    public static Task enqueueWrite(
    		String strTableName, 
			String strSql,
			String strFileName,
			Reflector reflector,
			EnumDbType enumDbType,
			String strDriver) 
    {
    	SqliteWriteJob newWriteJob = null;
    	try
    	{
	    	EfficientProducerConsumerQueue<SqliteWriteJob> queue = 
	    			SqliteTaskQueues.getWriteQueue(
	    					strFileName,
	    					enumDbType);
	    	String strKey = "IK_" + strFileName;
	    	
	    	synchronized(LockHelper.GetLockObject(strKey))
	    	{
	    		newWriteJob = new SqliteWriteJob(
	    				null,
	    				null,
	    				queue,
	    				strFileName,
	    				strTableName,
	    				reflector,
	    				enumDbType,
	    				strDriver);
	    		newWriteJob.setIsOnlyWrite(true);
	    		newWriteJob.setSql(strSql);
	    		Task newTask = queue.add(strKey, newWriteJob);
	    		newWriteJob.setTask(newTask);
	    		return newTask;
	    	}
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
		return null;
	}
    
    public static Task enqueueImport(
    		String strTableName, 
    		String[] columns,
    		Object[][] importArr,
			boolean b,
			String strFileName,
			Reflector reflector,
			EnumDbType enumDbType,
			String strDriver) 
    {
    	SqliteWriteJob newWriteJob = null;
    	try
    	{
	    	EfficientProducerConsumerQueue<SqliteWriteJob> queue = 
	    			SqliteTaskQueues.getWriteQueue(
	    					strFileName,
	    					enumDbType);
	    	String strQueueKey = "IK_" + strFileName;
	    	
	    	//synchronized(LockHelper.GetLockObject(strQueueKey))
	    	synchronized(queue)
	    	{
	    		SqliteWriteJob parentWriteJob = queue.tryGetValue(strQueueKey);
	    		if(parentWriteJob != null && !parentWriteJob.getIsOnlyWrite())
	    		{
	    			//
	    			// merge import
	    			//
	    			Object currLockObj = parentWriteJob.getLockObj();
	    			if(currLockObj != null)
	    			{
		    			synchronized(currLockObj)
		    			{
		    				
		    	    		if(parentWriteJob != null && 
	    	    				!parentWriteJob.getIsOnlyWrite() &&
	    	    				!parentWriteJob.getIsClosed() &&
	    	    				!parentWriteJob.getIsConsumed())
		    	    		{
			    				Task parentTask = parentWriteJob.getTask();
			    				
			    				if(parentTask != null)
			    				{
			    					
			    					Object[][] oldArr = parentWriteJob.getImportArr();
			    					
			    					if(oldArr != null)
			    					{
				    					ArrayList<Object[]> mergedList = null;
				    					
				    					try
				    					{
				    						mergedList = new ArrayList<Object[]>(Arrays.asList(oldArr));
				    					}
				    					catch(Exception ex)
				    					{
				    						Logger.log(ex);
				    					}
				    					mergedList.addAll(Arrays.asList(importArr));
				    					parentWriteJob.setImportArr(
				    							mergedList.toArray(
				    									new Object[0][]));
				    					if(parentWriteJob.getIsConsumed())
				    					{
				    						throw new HCException("Item already consumed");
				    					}
				    					String strMessage = "=> import merged [" +
				    							parentWriteJob.getImportArr().length + "]...";
				    					Console.WriteLine(strMessage);
				    					//Verboser.Talk(strMessage);
				    					
				    					Task mergedTask = new Task(null)
				    					{
				    						@Override
				    						public void runTask()
				    						{
				    	    					String strMessage = "=> merged task is completed";
				    	    					Verboser.Talk(strMessage);
				    						}
				    					};
				    					mergedTask.continueWhenAll(new Task[]{ parentTask });
				    					return mergedTask;
			    					}
			    				}
			    				else
			    				{
				    				// else LockObj came too late and the item has been already consumed
			    					Console.WriteLine("Too late merge");
			    				}
		    	    		}
		    			}
	    			}
	    		}
	    		
	    		//
	    		// enqueue one at a time!!
	    		//
	    		
	    		while(queue.containsKey(strQueueKey))
	    		{
	    			Thread.sleep(50);
	    		}
	    	
//	    		writeJob = queue.tryGetValue(strKey);
//	    		if(writeJob != null && !writeJob.getIsOnlyWrite()){
//	    			throw new Exception("Item already in the queue");
//	    		}
	    		
	    		newWriteJob = new SqliteWriteJob(
	    				columns,
	    				importArr,
	    				queue,
	    				strFileName,
	    				strTableName,
	    				reflector,
	    				enumDbType,
	    				strDriver);
	    		if(queue.containsKey(strQueueKey))
	    		{
	    			throw new HCException("Item already in queue!");
	    		}
	    		
	    		Task newTask = queue.add(strQueueKey, newWriteJob);
	    		newWriteJob.setTask(newTask);
	    		return newTask;
	    	}
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
		return null;
	}
    
	public static void enqueueLoadData(
			String strSql,
			ArrayList<Object[]> data,
			Reflector reflector,
			String strFileName,
			boolean blnIncludeKeyCol,
			EnumDbType enumDbType,
			String strDriver)
	{
		
		enqueueLoadData(
				strSql,
				data,
				reflector,
				strFileName,
				blnIncludeKeyCol,
				enumDbType,
				null,
				strDriver);
	}
    
	public static void enqueueLoadData(
			String strSql,
			ArrayList<Object[]> data,
			Reflector reflector,
			String strFileName,
			boolean blnIncludeKeyCol,
			EnumDbType enumDbType,
			ArrayList<String> schema,
			String strDriver)
	{
		
		SqliteReadJob readJob = null;
		try 
		{
			readJob = new SqliteReadJob(
					null, 
					strSql, 
					reflector.getColNames().length, 
					strFileName,
					reflector,
					enumDbType,
					strDriver);
			readJob.setIncludeKeyId(blnIncludeKeyCol);
			
			if(schema != null)
			{
				readJob.setLoadColNames(true);
			}
			
			enqueueLoadData(data, readJob, schema);
			
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		finally
		{
			if(readJob != null)
			{
				readJob.close();
				readJob = null;
			}
		}
	}

	public static void enqueueLoadData(
			List<Object[]> data, 
			SqliteReadJob readJob,
			List<String> schema) 
	{
		try
		{
			ProducerConsumerQueue<SqliteReadJob> queue = SqliteTaskQueues.getReadQueue(
					readJob.getFileName(),
					readJob.getEnumDbType());
			readJob.setQueue(queue);
			
			List<Object[]> dataRes = new ArrayList<Object[]>();
			readJob.setData(dataRes);
			queue.add(readJob).waitTask();
			data.addAll(dataRes);
			if(schema != null)
			{
				String[] colNames = readJob.getColNames();
				if(colNames != null &&
						colNames.length > 0)
				{
					for (int i = 0; i < colNames.length; i++) 
					{
						schema.add(colNames[i]);
					}
				}
			}
			dataRes.clear();
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}

	public static Task enqueueRead(
			String strSql,
			Reflector reflector,
			String strFileName,
			EnumDbType enumDbType,
			String strDriver)
	{
		
		SqliteReadJob readJob = null;
		try 
		{
			ProducerConsumerQueue<SqliteReadJob> queue = SqliteTaskQueues.getReadQueue(
					strFileName,
					enumDbType);
			readJob = new SqliteReadJob(
					queue, 
					strSql, 
					reflector.getColNames().length, 
					strFileName,
					reflector,
					enumDbType,
					strDriver);
			readJob.setIsOnlyRead(true);
			return queue.add(readJob);
			
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		finally
		{
			if(readJob != null)
			{
				
				//readJob.close();
				readJob = null;
			}
		}
		
		return null;
	}
	
	
	public static EfficientProducerConsumerQueue<SqliteWriteJob> getWriteQueue(
			String strFileName,
			EnumDbType enumDbType)
	{
		try
		{
			EfficientProducerConsumerQueue<SqliteWriteJob> queue;
			String strDriveLetter = GetDriveLtter(strFileName, enumDbType);
			
			synchronized(m_lockObj)
			{
				if(!m_writerQueueMap.containsKey(strDriveLetter))
				{
					queue = new EfficientProducerConsumerQueue<SqliteWriteJob>(1,
							SqliteConstants.ITEMS_TO_WRITE_IN_MEMORY)
							{
									@Override
									public void runTask(SqliteWriteJob sqliteWriteJob) 
									{
										try
										{
											if(sqliteWriteJob.getIsOnlyWrite())
											{
												executeStatementWithWriteLock(sqliteWriteJob);
											}
											else
											{
												bulkInsert(sqliteWriteJob);
											}
										}
										catch(Exception ex)
										{
											Logger.log(ex);
										}
										finally
										{
											sqliteWriteJob.close();
											sqliteWriteJob = null;
										}
									}
							};
					m_writerQueueMap.put(strDriveLetter, queue);
				}
				else
				{
					queue = m_writerQueueMap.get(strDriveLetter);
				}
			}
			return queue;
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return null;
	}

	private static void bulkInsert(SqliteWriteJob sqliteWriteJob) 
	{
		WriteLock writeLock = null;
		
		try
		{
			String strFileName = sqliteWriteJob.getFileName();
			Reflector reflector = sqliteWriteJob.getReflector();
			EnumDbType enumDbType = sqliteWriteJob.getEnumDbType();
			String strDriver = sqliteWriteJob.getDriver();
			
			boolean blnIsValid = false;
			
			while(!blnIsValid)
			{
				ISqliteCacheBase cache = SqliteCacheConnectionPool.getDbWrapper(
						strFileName, 
						reflector,
						strDriver,
						enumDbType);
				writeLock = cache.getReadWriteLock().writeLock();
				
				if(writeLock != null)
				{
					writeLock.lock();
					blnIsValid = !cache.isClosed();
				}
				
				if(blnIsValid) 
				{
					DateTime logTime = DateTime.now();
					Object lockObj = sqliteWriteJob.getLockObj();
					if(lockObj == null)
					{
						throw new HCException("Null lock");
					}
					synchronized(lockObj) // possible merge of imports 
					{ 
						sqliteWriteJob.setIsConsumed(true);
						String strMessage;
						if(Seconds.secondsBetween(
								m_lastWriteTime, DateTime.now()).getSeconds() > 5)
						{
							strMessage = "****Write queue size [" +
									sqliteWriteJob.getWriterQueue().getSize() + "]";
							Verboser.Talk(strMessage);
							m_lastWriteTime = DateTime.now();
						}
						
						synchronized(LockHelper.GetLockObject(
								strFileName + "_bulkInsert"))
						{
							Object[][] objArr = sqliteWriteJob.getImportArr();
							int intArrSize = objArr.length;
							SqliteBulkInsert.bulkInsert(
									sqliteWriteJob.getTableName(),
									sqliteWriteJob.getColumns(), 
									cache.getDbConnection(), 
									objArr);
							
							strMessage = "Inserted [" + intArrSize + 
									"] rows. File [" + 
									strFileName + "]. Time [" +
									Seconds.secondsBetween(
											logTime, DateTime.now()).getSeconds() + 
											"] seconds";
							objArr = null;
							sqliteWriteJob.setImportArr(null);
							Console.WriteLine(strMessage);
					        //Verboser.Talk(strMessage);        
						}
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		finally
		{
			try 
			{
				sqliteWriteJob.close();
				
				if(writeLock != null)
				{
					writeLock.unlock();
				}
			} 
			catch (Exception e) 
			{
				Logger.log(e);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <K> K executeScalar(
			SqliteReadJob sqliteReadJob) 
	{
		ResultSet rs = null;
		Statement statement = null;
		ReadLock readLock = null;
		ResultSet resultSet = null;
		try 
		{
			
			String strFileName = sqliteReadJob.getFileName();
			Reflector reflector = sqliteReadJob.getReflector();
			EnumDbType enumDbType = sqliteReadJob.getEnumDbType();
			String strDriver = sqliteReadJob.getDriver();
			boolean blnIsValid = false;
			
			while(!blnIsValid)
			{
				ISqliteCacheBase cache = SqliteCacheConnectionPool.getDbWrapper(
						strFileName, 
						reflector,
						strDriver,
						enumDbType);
				readLock = cache.getReadWriteLock().readLock();
				
				if(readLock != null)
				{
					
					readLock.lock();
					blnIsValid = !cache.isClosed();
				}
				
				blnIsValid = !cache.isClosed();
				
				if(blnIsValid) 
				{
					
					if(Seconds.secondsBetween(
							m_lastReadLogTime, DateTime.now()).getSeconds() > 5)
					{
						if(sqliteReadJob.getReaderQueue() != null)
						{
							
							String strMessage = "****Read queue size [" +
									sqliteReadJob.getReaderQueue().getSize() + "]";
							Verboser.Talk(strMessage);
							m_lastReadLogTime = DateTime.now();
						}
					}
					
					String strSql = sqliteReadJob.getSql();
					
					Connection dbConn = cache.getDbConnection();
					
					if(dbConn == null)
					{
						throw new Exception("null connection");
					}
					
					statement = dbConn.createStatement();
					resultSet = statement.executeQuery(strSql);
					return (K)resultSet.getObject(1);
				}
			}
		}
		catch(Exception ex){
			
			Logger.log("Exception thrown in file [" +
					sqliteReadJob.getFileName() + "]");
			Logger.log(ex);
		}
		finally{
			try{
				if(rs != null){
					rs.close();
				}
				
				if(statement != null){
					statement.close();
				}
				
				if(resultSet != null){
					resultSet.close();
				}
				
				if(readLock != null){
					readLock.unlock();
				}
				sqliteReadJob.close();
			}
			catch(Exception ex){
				
				Logger.log(ex);
			}
		}
		return null;
	}
	
	private static void executeLoad(
			SqliteReadJob sqliteReadJob) 
	{
		
		ResultSet rs = null;
		Statement statement = null;
		ReadLock readLock = null;
		
		try 
		{
			
			String strFileName = sqliteReadJob.getFileName();
			Reflector reflector = sqliteReadJob.getReflector();
			EnumDbType enumDbType = sqliteReadJob.getEnumDbType();
			boolean blnIsOnlyRead = sqliteReadJob.getIsOnlyRead();
			String strDriver = sqliteReadJob.getDriver();			
			boolean blnIsValid = false;
			
			while(!blnIsValid)
			{
				
				ISqliteCacheBase cache = SqliteCacheConnectionPool.getDbWrapper(
						strFileName, 
						reflector,
						strDriver,
						enumDbType);
				readLock = cache.getReadWriteLock().readLock();
				
				if(readLock != null)
				{
					
					readLock.lock();
					blnIsValid = !cache.isClosed();
				}
				
				blnIsValid = !cache.isClosed();
				
				if(blnIsValid) 
				{
					
					if(Seconds.secondsBetween(
							m_lastReadLogTime, DateTime.now()).getSeconds() > 5)
					{
						String strMessage = "****Read queue size [" +
								sqliteReadJob.getReaderQueue().getSize() + "]";
						Verboser.Talk(strMessage);
						m_lastReadLogTime = DateTime.now();
					}
					
					String strSql = sqliteReadJob.getSql();
					int intColCount = sqliteReadJob.getColumnCount();
					Connection dbConn = cache.getDbConnection();
					
					if(dbConn == null)
					{
						throw new Exception("null connection");
					}
					
					statement = dbConn.createStatement();
					rs = statement.executeQuery(strSql);
					
					if(sqliteReadJob.getLoadColNames())
					{
						ResultSetMetaData rsmd = rs.getMetaData();
						int intCols = rsmd.getColumnCount();
						String[] colNames = new String[intCols];
						for (int i = 0; i < intCols; i++) {
							String strColName = rsmd.getColumnName(i + 1);
							colNames[i] = strColName;
						}
						sqliteReadJob.setColNames(colNames);
					}
					
					int intBaseIndex; 
					if(intColCount == 0)
					{
						ResultSetMetaData rsmd = rs.getMetaData();
						intColCount = rsmd.getColumnCount();
						intBaseIndex = 1;
					}
					else
					{
						if(blnIsOnlyRead){
							intBaseIndex = 1;
						}
						else
						{
							intBaseIndex = 2; // remember that sqlite is a 1-based index
						}
						
						if(sqliteReadJob.getIncludeKeyId())
						{
							intBaseIndex = 1;
							intColCount++;
						}
					}
					
					List<Object[]> data = sqliteReadJob.getData();
					while(rs.next())
				    {
						Object[] rowList = new Object[intColCount];
						for(int i = 0; i<intColCount; i++)
						{
							rowList[i] = rs.getObject(i+intBaseIndex);
						}
						data.add(rowList);
				    }
				}
			}
		}
		catch(Exception ex){
			
			Logger.log("Exception thrown in file [" +
					sqliteReadJob.getFileName() + "][" +
					sqliteReadJob.getSql() + "]");
			Logger.log(ex);
		}
		finally{
			try{
				if(rs != null){
					rs.close();
				}
				
				if(statement != null){
					statement.close();
				}
				
				if(readLock != null){
					readLock.unlock();
				}
				sqliteReadJob.close();
			}
			catch(Exception ex){
				
				Logger.log(ex);
			}
		}
	}

	public static void executeStatementWithWriteLock(
			SqliteWriteJob sqliteWriteJob) {
		
		ResultSet rs = null;
		Statement statement = null;
		WriteLock writeLock = null;
		
		try {
			
			String strFileName = sqliteWriteJob.getFileName();
			Reflector reflector = sqliteWriteJob.getReflector();
			EnumDbType enumDbType = sqliteWriteJob.getEnumDbType();
			String strDriver = sqliteWriteJob.getDriver();			
			
			boolean blnIsValid = false;
			
			while(!blnIsValid){
				
				ISqliteCacheBase cache = SqliteCacheConnectionPool.getDbWrapper(
						strFileName, 
						reflector,
						strDriver,
						enumDbType);
				writeLock = cache.getReadWriteLock().writeLock();
				
				if(writeLock != null){
					
					writeLock.lock();
					blnIsValid = !cache.isClosed();
				}
				
				blnIsValid = !cache.isClosed();
				
				if(blnIsValid) 
				{
					
					if(Seconds.secondsBetween(m_lastReadLogTime, DateTime.now()).getSeconds() > 5) 
					{
						String strMessage = "****Read queue size [" +
								sqliteWriteJob.getWriterQueue().getSize() + "]";
						Verboser.Talk(strMessage);
						m_lastReadLogTime = DateTime.now();
					}
					
					String strSql = sqliteWriteJob.getSql();
					Connection dbConn = cache.getDbConnection();
					
					if(dbConn == null){
						throw new Exception("null connection");
					}
					
					statement = dbConn.createStatement();
					statement.executeUpdate(strSql);
					statement.close();

				}
			}
		}
		catch(Exception ex){
			
			Logger.log("Exception thrown in file [" +
					sqliteWriteJob.getFileName() + "]");
			Logger.log(ex);
		}
		finally{
			
			try{
				if(rs != null){
					rs.close();
				}
				
				if(statement != null){
					statement.close();
				}
				
				if(writeLock != null){
					writeLock.unlock();
				}
				sqliteWriteJob.close();
			}
			catch(Exception ex){
				
				Logger.log(ex);
			}
		}
	}	
	
	public static void executeStatementWithReadLock(
			SqliteReadJob sqliteReadJob) {
		
		ResultSet rs = null;
		Statement statement = null;
		ReadLock readLock = null;
		String strSql = null;
		
		try {
			
			String strFileName = sqliteReadJob.getFileName();
			Reflector reflector = sqliteReadJob.getReflector();
			EnumDbType enumDbType = sqliteReadJob.getEnumDbType();
			String strDriver = sqliteReadJob.getDriver();
			
			boolean blnIsValid = false;
			
			while(!blnIsValid){
				
				ISqliteCacheBase cache = SqliteCacheConnectionPool.getDbWrapper(
						strFileName, 
						reflector,
						strDriver,
						enumDbType);
				readLock = cache.getReadWriteLock().readLock();
				
				if(readLock != null){
					
					readLock.lock();
					blnIsValid = !cache.isClosed();
				}
				
				readLock.lock();
				
				blnIsValid = !cache.isClosed();
				
				if(blnIsValid) {
					
					if(Seconds.secondsBetween(m_lastReadLogTime, DateTime.now()).getSeconds() > 5) 
					{
						String strMessage = "****Read queue size [" +
								sqliteReadJob.getReaderQueue().getSize() + "]";
						Verboser.Talk(strMessage);
						m_lastReadLogTime = DateTime.now();
					}
					
					strSql = sqliteReadJob.getSql();
					Connection dbConn = cache.getDbConnection();
					
					if(dbConn == null){
						throw new Exception("null connection");
					}
					
					statement = dbConn.createStatement();
					statement.executeUpdate(strSql);
					statement.close();

				}
			}
		}
		catch(Exception ex){
			
			Logger.log("Exception thrown in file [" +
					sqliteReadJob.getFileName() + "][" +
					strSql + "]");
			Logger.log(ex);
		}
		finally{
			try{
				if(rs != null){
					rs.close();
				}
				
				if(statement != null){
					statement.close();
				}
				
				if(readLock != null){
					readLock.unlock();
				}
				sqliteReadJob.close();
			}
			catch(Exception ex){
				
				Logger.log(ex);
			}
		}
	}
	
	
	
	private static String GetDriveLtter(
			String strFileName,
			EnumDbType enumDtType) {
		String strDriveLetter;
		synchronized(m_lockObj){
			if(!m_mapFileNameToDriveLetter.containsKey(strFileName)){
				if(enumDtType == EnumDbType.SqLite){
					strDriveLetter = FileHelper.getDriveLetter(strFileName).toLowerCase();
				}
				else{
					strDriveLetter = strFileName;
				}
				m_mapFileNameToDriveLetter.put(strFileName, strDriveLetter);
			}
			else
			{
				strDriveLetter = m_mapFileNameToDriveLetter.get(strFileName);
			}
			return strDriveLetter;
		}
	}
}