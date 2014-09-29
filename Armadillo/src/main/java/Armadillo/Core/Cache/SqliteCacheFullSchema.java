package Armadillo.Core.Cache;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.zip.GZIPInputStream;

import org.joda.time.DateTime;

import Armadillo.Core.Config;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Core.Io.PathHelper;
import Armadillo.Core.Reflection.Reflector;
import Armadillo.Core.Serialization.Serializer;
import Armadillo.Core.Text.StringHelper;

import com.dyuproject.protostuff.parser.Field.Bool;
import com.esotericsoftware.reflectasm.FieldAccess;

public class SqliteCacheFullSchema<T> extends ASqliteCache<T>
{
	private static String m_strDefaultDataPath;

	static
	{
		try
		{
			m_strDefaultDataPath = Config.getConfig(Logger.class).getStr("DataPath");
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public SqliteCacheFullSchema(
			Class<T>item) 
	{
		this(getFileName(item), item);
	}
	
	private static String getFileName(Class<?> item) 
	{
		try
		{
			String strClassName = Reflector.getClassName(item);
			return PathHelper.combinePaths(m_strDefaultDataPath, strClassName);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
	}

	public SqliteCacheFullSchema(
			String strFileName, 
			Class<T>item) 
	{
		this(strFileName, 
				Reflector.getClassName(item), 
				SqliteConstants.KEY_COL_NAME,
				item);
	}

	public SqliteCacheFullSchema(
			String strFileName, 
			String strTableName,
			String strDefaultIndex,
			Class<T> item) 
	{
		this(strFileName, 
				strTableName, 
				strDefaultIndex,
				item,
				EnumDbType.SqLite);
	}
	
	public SqliteCacheFullSchema(
			String strFileName, 
			String strTableName,
			String strDefaultIndex,
			Class<T> classObj,
			EnumDbType enumDbType) 
	{
		super(strFileName, 
				strTableName, 
				strDefaultIndex,
				classObj,
				enumDbType);
	}

	@Override
    public void validateTable() 
	{
        try 
        {
            StringBuilder sb = new StringBuilder();
            boolean blnIsTitleCol = true;

            //
            // add default index as a column
            //
            if (m_strDefaultIndex != null)
            {
                blnIsTitleCol = false;
                sb.append(m_strDefaultIndex + " varchar(100)");
            }
            String[] propertyNames = m_reflector.getColNames();
            for (int i = 0; i < propertyNames.length; i++)
            {
            	String strPropertyName = propertyNames[i];
                Class<?> propertyType = (Class<?>)m_reflector.getPropertyType(strPropertyName);
                
                
                if (!blnIsTitleCol)
                {
                    sb.append(",");
                }

                if (propertyType == String.class)
                {
                    sb.append(strPropertyName + " varchar(100)");
                }
                else if (propertyType == int.class)
                {
                    sb.append(strPropertyName + " int");
                }
                else if (propertyType == long.class)
                {
                    sb.append(strPropertyName + " DOUBLE");
                }
                else if (propertyType == double.class)
                {
                    sb.append(strPropertyName + " DOUBLE");
                }
                else if (propertyType == Bool.class ||
                		propertyType == boolean.class)
                {
                    sb.append(strPropertyName + " BOOLEAN");
                }
                else if (propertyType == DateTime.class ||
                		propertyType == Date.class)
                {
                    sb.append(strPropertyName + " REAL");
                }
                else if (Reflector.isEnum(propertyType))
                {
                    sb.append(strPropertyName + " varchar(100)");
                }
                else
                {
                    sb.append(strPropertyName + " BLOB");
                }
                blnIsTitleCol = false;
            }
            String strColumnDef = sb.toString();
            String strSql = "create table if not exists [" +
                                 m_strTableName + "] (" +
                                 strColumnDef + ")";
            
            SqliteTaskQueues.enqueueWrite(
            		m_strTableName, 
        			strSql,
        			m_strFileName,
        			m_reflector,
        			m_enumDbType).waitTask();            
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

	@Override
	public void executeNonQuery(
			String strSql) {
		
		SqliteTaskQueues.enqueueRead(
				strSql, 
				m_reflector, 
				m_strFileName, 
				m_enumDbType).waitTask();
	}
	
	@Override
	public void execute(
			String strSql, 
			List<Object[]> data) 
	{
		
		SqliteReadJob sqliteReadJob = new SqliteReadJob(
				null, 
				strSql, 
				0, 
				m_strFileName, 
				m_reflector,
				m_enumDbType);
		
		SqliteTaskQueues.enqueueLoadData(data, sqliteReadJob, null);
	}

	public void execute(
			String strSql, 
			ArrayList<Object[]> data,
			ArrayList<String> schema) {
		
		SqliteReadJob sqliteReadJob = new SqliteReadJob(
				null, 
				strSql, 
				0, 
				m_strFileName, 
				m_reflector,
				m_enumDbType);
		
	    if(schema != null){
	    	sqliteReadJob.setLoadColNames(true);
	    }
		
		SqliteTaskQueues.enqueueLoadData(data, sqliteReadJob, schema);
	}
	
    public T getRow(
    		Object[] objects)
    {
    	return getRow(objects, 0);
    }
    
	
    private T getRow(
    		Object[] objects,
    		int intBaseIndex)
    {
        try
        {
            @SuppressWarnings("unchecked")
			T tobj = (T)m_reflector.createInstance();
            int intCols = m_reflector.getColNames().length;
            FieldAccess access = m_reflector.getAccess();
            for (int j = intBaseIndex; j < intCols + intBaseIndex; j++)
            {
            	Object obj;
            	try
            	{
	                obj = objects[j];
	                objects[j] = null;
	                if(obj != null)
	                {
		                obj = parseObject(obj, j- intBaseIndex);
	                	access.set(tobj, j - intBaseIndex, obj);
	                }
            	}
            	catch(Exception ex)
            	{
            		Logger.log(ex);
            	}
            }
            return tobj;
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return null;
    }

    
    protected Object parseObject(Object obj, int intCol)
    {
        try
        {
            Class<?> propertyType = (Class<?>)m_reflector.getPropertyType(intCol);
            if (m_reflector.isEnum(intCol))
            {
                obj = Reflector.getEnumFromString((Class<?>)propertyType, (String) obj);
            }
            else if (propertyType == Boolean.class ||
            		propertyType == boolean.class)
            {
            	int intVal = (Integer) obj;
                obj = intVal == 1;
            }
            else if (propertyType == Date.class)
            {
            	double dblDateVal = (Double) obj;
                obj = new DateTime((long)dblDateVal).toDate();
            }
            else if (propertyType == DateTime.class)
            {
            	double dblDateVal = (Double) obj;
                obj = new DateTime((long)dblDateVal);
            }
            else if (propertyType == double.class &&
                     obj.getClass() != double.class)
            {
                if (obj instanceof String)
                {
                    String strObj = (String)obj;
                    if (strObj == null ||
                    		strObj == "")
                    {
                        obj = Double.NaN;
                    }
                }
            }
            else if (obj instanceof byte[])
            {
                //
                // by default. This is a blob value
                //
            	byte[] bytes = (byte[]) obj;
            	
            	if(getUseCompression())
            	{
	            	ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
	            	GZIPInputStream gzipIn = new GZIPInputStream(bais);
	            	ObjectInputStream objectIn = new ObjectInputStream(gzipIn);
	            	bytes = (byte[]) objectIn.readObject();
	            	objectIn.close();            	
            	}
            	ObjectWrapper object2 = new ObjectWrapper();
        		Serializer.deserialize(
        				bytes,
        		object2);
        		obj = object2.getObj();
            }
            return obj;
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return null;
    }
    
	@Override
	public T[] loadData(String strSql) 
	{
		ArrayList<Object[]> data = new ArrayList<Object[]>();
		SqliteTaskQueues.enqueueLoadData(
				strSql, 
				data , 
				m_reflector, 
				m_strFileName, 
				false,
				m_enumDbType);
		
        try
        {
            //
            // build objects using reflection
            //
            @SuppressWarnings("unchecked")
			T[] dataArray = (T[]) Array.newInstance(m_genericClass, data.size());
            //
            // not worth parallel.For
            //
            for(int i= 0; i<data.size(); i++)
            {
                T tobj = getRow(data.get(i));
                dataArray[i] = tobj;
                data.set(i, null);
            }
            return dataArray;
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        finally
        {
        	if(data != null)
        	{
        		data.clear();
        	}
        }
        return null;
	}

	public <K> K executeScalar(String strQuery)
	{
		SqliteReadJob sqliteReadJob = null;
		try
		{
			sqliteReadJob = new SqliteReadJob(
					null, 
					strQuery, 
					0, 
					m_strFileName, 
					m_reflector,
					m_enumDbType);
			
			return SqliteTaskQueues.executeScalar(
					sqliteReadJob);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		finally
		{
			if(sqliteReadJob != null)
			{
				
				sqliteReadJob.close();
				sqliteReadJob = null;
			}
		}
		return null;
	}
	
	public ResultSet getResultSet(
			String strQuery)
	{
		Statement statement = null;
		ReadLock readLock = null;
		ResultSet resultSet = null;
		try 
		{
			String strFileName = m_strFileName;
			Reflector reflector = m_reflector;
			EnumDbType enumDbType = m_enumDbType;
			boolean blnIsValid = false;
			
			while(!blnIsValid)
			{
				ISqliteCacheBase cache = SqliteCacheConnectionPool.getDbWrapper(
						strFileName, 
						reflector,
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
					Connection dbConn = cache.getDbConnection();
					
					if(dbConn == null)
					{
						throw new Exception("null connection");
					}
					
					statement = dbConn.createStatement(
							ResultSet.TYPE_SCROLL_INSENSITIVE, 
							ResultSet.CONCUR_READ_ONLY);
					resultSet = statement.executeQuery(strQuery);
					return resultSet;
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log("Exception thrown in file [" +
					m_strFileName + "]");
			Logger.log(ex);
		}
		return null;		
	}
	
	@Override
	public boolean containsKey(String strKey) 
	{
		SqliteReadJob sqliteReadJob = null;
        try
        {
            if (m_strDefaultIndex == null)
            {
                throw new Exception("Key not found");
            }

            String strQuery = "SELECT EXISTS(select " + m_strDefaultIndex +
                            " from [" + m_strTableName +
                           "] where " + m_strDefaultIndex + " = '" + strKey + "' LIMIT 1)";

    		sqliteReadJob = new SqliteReadJob(
    				null, 
    				strQuery, 
    				0, 
    				m_strFileName, 
    				m_reflector,
    				m_enumDbType);
    		int intVal = SqliteTaskQueues.executeScalar(sqliteReadJob);
    		
            return intVal == 1;

        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
		finally
		{
			if(sqliteReadJob !=  null)
			{
				sqliteReadJob.close();
				sqliteReadJob = null;
			}
		}
        return false;
    }

	@Override
	public Map<String, List<T>> loadDataMap(String strQuery) 
	{
		
    	ArrayList<Object[]> data = new ArrayList<Object[]>();
        try
        {
    		SqliteTaskQueues.enqueueLoadData(
    				strQuery, 
    				data , 
    				m_reflector, 
    				m_strFileName,
    				true,
    				m_enumDbType);

            //
            // build objects using reflection
            //
            Hashtable<String, List<T>> dataMap = 
            		new Hashtable<String, List<T>>();
            Object currLockObj = new Object();
            int intdataSize = data.size();
            for(int i= 0; i<intdataSize; i++)
            {
				addToMap(dataMap, i, currLockObj, 0, data.get(i));
            	data.set(i, null);
            }
            return dataMap;
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        finally
        {
        	if(data != null)
        	{
        		
        		data.clear();
        	}
        }
			        
        return null;
    }

    private void addToMap(
            Map<String, List<T>> dataMap, 
            int i, 
            Object currLockObj, 
            int intKeyCol, 
            Object[] objects)
        {
	    	try
	    	{
	            T tobj = getRow(objects, 1);
	            String strKey = (String) objects[intKeyCol];
	            
	            if(!StringHelper.IsNullOrEmpty(strKey))
	            {
		            synchronized (currLockObj)
		            {
		            	List<T> currList;
		            	if(!dataMap.containsKey(strKey))
		            	{
		                    currList = new ArrayList<T>();
		                    dataMap.put(strKey, currList);
		            	}
		            	else
		            	{
		            		currList = dataMap.get(strKey);
		                }
		                currList.add(tobj);
		            }
	            }
	    	}
	    	catch(Exception ex)
	    	{
	    		Logger.log(ex);
	    	}
        }

	@Override
	protected String getCreateIndexStatement() 
	{
		return "create index if not exists " +
        		m_strTableName + "_" + m_strDefaultIndex + "_INDEX ON " +
        		m_strTableName + " (" + m_strDefaultIndex + ")";	
	}

	public String getTableName() 
	{
		return m_strTableName;
	}

	public String getKeyColName() 
	{
		return m_strDefaultIndex;
	}
}