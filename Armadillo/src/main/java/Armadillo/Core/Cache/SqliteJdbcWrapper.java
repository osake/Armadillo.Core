package Armadillo.Core.Cache;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.zip.GZIPOutputStream;

import org.joda.time.DateTime;

import com.esotericsoftware.reflectasm.FieldAccess;

import Armadillo.Core.HCException;
import Armadillo.Core.KeyValuePair;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Core.Verboser;
import Armadillo.Core.Concurrent.ProducerConsumerQueue;
import Armadillo.Core.Concurrent.Task;
import Armadillo.Core.Io.FileHelper;
import Armadillo.Core.Reflection.Reflector;
import Armadillo.Core.Serialization.Serializer;
import Armadillo.Core.Text.StringHelper;

public class SqliteJdbcWrapper<T> implements ISqliteCacheBase 
{

	protected String m_strFileName;
	private boolean m_blnIsClosed;
	protected Connection m_dbConnRaw;
	private ReentrantReadWriteLock m_readWriteLock;
	protected String m_strDriver;

	public SqliteJdbcWrapper(
			String strFileName,
			Reflector reflector) {
		this(strFileName,
			reflector,
			SqliteConstants.DEFAULT_DRIVER,
			EnumDbType.SqLite);
	}
	
	public SqliteJdbcWrapper(
			String strFileName,
			Reflector reflector,
			EnumDbType enumDbType) {
		this(strFileName,
			reflector,
			SqliteConstants.DEFAULT_DRIVER,
			enumDbType);
	}
	
	public SqliteJdbcWrapper(
			String strFileName,
			Reflector reflector,
			String strDriver,
			EnumDbType enumDbType) {
		
		m_strDriver = strDriver;
		m_readWriteLock = new ReentrantReadWriteLock();
		if(enumDbType == EnumDbType.SqLite){
			strFileName = FileHelper.cleanFileName(strFileName);
		}
		m_strFileName = strFileName;
		
		if(StringHelper.IsNullOrEmpty(m_strFileName)){
			try {
				throw new HCException("Empty file name");
			} catch (HCException e) {
				Logger.log(e);
			}
		}
		openDb();
	}

	public SqliteJdbcWrapper() { }
	
	protected void openDb() 
	{
		try 
		{
			FileHelper.checkDirectory(m_strFileName);
			// load the sqlite-JDBC driver using the current class loader
			Class.forName(m_strDriver);
			// create a database connection
			m_dbConnRaw = DriverManager
					.getConnection("jdbc:sqlite:" + m_strFileName);
			Verboser.Talk("Loaded Sqlite DB. File [" + m_strFileName + "]");
		} 
		catch (Exception ex) 
		{
			// if the error message is "out of memory",
			// it probably means no database file is found
			Logger.log(ex);
		}
	}
	
    public static <T> Task bulkInsert(
    		String strTableName,
    		String[] cols,
    		List<KeyValuePair<String,T>> kvpList,
    		Reflector reflector,
    		String strFileName,
    		boolean blnUseCompression,
    		EnumDbType enumDbType)
    {
    	try
    	{
	    	if(kvpList == null || kvpList.size() == 0)
	    	{
				Task fakeTask = new Task(null);
				//fakeTask.setTaskDoneUnsafe();
				fakeTask.setTaskDone();
				return fakeTask;
	    	}
	    	
	    	KeyValuePair<String, Type>[] colToTypeArr = reflector.getColToType();
	    	FieldAccess fieldAccess = reflector.getAccess();
	    	Object[][] importArr = new Object[kvpList.size()][];
	        	
	    	for (int i = 0; i < importArr.length; i++) 
	    	{
	    		importArr[i] = getObjArray(
	    				kvpList.get(i),
	    	    		colToTypeArr,
	    	    		fieldAccess,
	    	    		blnUseCompression);
			}
	    	return SqliteTaskQueues.enqueueImport(
	    			strTableName,
	    			cols,
	    			importArr,
	    			false,
	    			strFileName,
	    			reflector,
	    			enumDbType);
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	finally
    	{
        	kvpList.clear();
    	}
    	return null;
    }
    



    private static <T> Object[] getObjArray(
    		KeyValuePair<String,T> kvp,
    		KeyValuePair<String, Type>[] colToTypeArr,
    		FieldAccess fieldAccess,
    		boolean blnUseCompression)
    {
    	try 
    	{
			T obj = kvp.getValue();
			Object[] objs = new Object[colToTypeArr.length + 1];
			objs[0] = kvp.getKey();
			
			for (int i = 0; i < colToTypeArr.length; i++) 
			{
				Type propType = colToTypeArr[i].getValue();		
				Object currObj = fieldAccess.get(obj, i);
				if(currObj == null)
				{
					if(propType == String.class)
					{
						currObj = "";
					}
				}
				if(currObj != null)
				{
					if(currObj instanceof Date)
					{
						long mills = new DateTime((Date)currObj).getMillis();
						currObj = mills;
					}
					else if(currObj instanceof DateTime)
					{
						long mills = ((DateTime)currObj).getMillis();
						currObj = mills;
					}
					else if(currObj.getClass().isEnum())
					{
						currObj = currObj.toString();
					}
					else if(isBlob(currObj.getClass()))
					{
						
						byte[] bytes = Serializer.getBytes(new ObjectWrapper(currObj));
						
						if(blnUseCompression)
						{
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
							ObjectOutputStream objectOut = new ObjectOutputStream(gzipOut);
							objectOut.writeObject(bytes);
							objectOut.close();
							bytes = baos.toByteArray();
						}
						currObj = bytes;
					}
				}
				objs[i+1] = currObj;
			}
			
			return objs;
		} 
    	catch (Exception e) 
    	{
			Logger.log(e);
		}
    	
    	return null;
    }
    
    private static boolean isBlob(Class<?> classObj){
    		
    	return !classObj.equals(Boolean.class) && 
    			!classObj.equals(Integer.class) &&
    			!classObj.equals(Character.class) &&
    			!classObj.equals(Byte.class) &&
    			!classObj.equals(Short.class) &&
    			!classObj.equals(Double.class) &&
    			!classObj.equals(Long.class) &&
    			!classObj.equals(Float.class) &&
    			!classObj.equals(String.class);
    }

	public void close() 
	{
		WriteLock writeLock = null;
		
		try 
		{
			writeLock = m_readWriteLock.writeLock();
			writeLock.lock();
			m_blnIsClosed = true;
			
			if (m_dbConnRaw != null) 
			{
				onClose(this);
				m_dbConnRaw.close();
				m_dbConnRaw = null;
				m_strFileName = null;
				Verboser.Talk("Closed connection [" +
						m_strFileName + "]");
			}
		} catch (Exception ex) {
			
			Logger.log(ex);
		}
		finally{
			
			if(writeLock != null){
				
				writeLock.unlock();
			}
			m_readWriteLock = null;
		}
	}
	
	public void onClose(SqliteJdbcWrapper<T> db) {
	}

	public static void enqueueLoadData(
			String strSql,
			ArrayList<Object[]> data,
			Reflector reflector,
			String strFileName,
			EnumDbType enumDbType){
		
		SqliteReadJob readJob = null;
		try {
			
			ProducerConsumerQueue<SqliteReadJob> queue = SqliteTaskQueues.getReadQueue(
					strFileName,
					enumDbType);
			readJob = new SqliteReadJob(
					queue, 
					strSql, 
					reflector.getColNames().length, 
					strFileName,
					reflector,
					enumDbType);
			
			ArrayList<Object[]> dataRes = new ArrayList<Object[]>();
			readJob.setData(dataRes);
			queue.add(readJob).waitTask();
			data.addAll(dataRes);
			dataRes.clear();
			
		} catch (Exception ex) {
			Logger.log(ex);
		}
		finally{
			if(readJob != null){
				
				readJob.close();
				readJob = null;
			}
		}
	}

	@Override
	public String getFileName() {
		return m_strFileName;
	}

	public boolean isClosed() {
		return m_blnIsClosed;
	}

	@Override
	public Connection getDbConnection() {
		return m_dbConnRaw;
	}
	
    public ReentrantReadWriteLock getReadWriteLock(){
    	return m_readWriteLock;
    }
}