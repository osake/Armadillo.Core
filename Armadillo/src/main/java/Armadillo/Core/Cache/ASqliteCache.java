package Armadillo.Core.Cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Armadillo.Core.KeyValuePair;
import Armadillo.Core.Logger;
import Armadillo.Core.Verboser;
import Armadillo.Core.Concurrent.Task;
import Armadillo.Core.Io.FileHelper;
import Armadillo.Core.Reflection.ReflectionCache;
import Armadillo.Core.Reflection.Reflector;

public abstract class ASqliteCache<T> implements ISqliteCache {
	
	private static ConcurrentHashMap<String, Object> m_validatedTables = 
			new ConcurrentHashMap<String, Object>();
	protected String m_strFileName;
	protected String m_strDefaultIndex;
	protected String m_strTableName;
	protected Reflector m_reflector;
	protected Class<?> m_genericClass;
	protected String[] m_allCols;
	private boolean m_blnIsClosed;
	private boolean m_blnUseCompression;
	protected EnumDbType m_enumDbType;
	protected String m_strDriver;
	
	public ASqliteCache(
			String strFileName,
			String strTableName,
			String strDefaultIndex,
			Class<T> item,
			EnumDbType enumDbType,
			String strDriver)
	{
		try
		{
			m_strDriver = strDriver;
			m_genericClass = item;
			m_enumDbType = enumDbType;
    		m_reflector = ReflectionCache.getReflector(m_genericClass);
    		String[] cols = m_reflector.getColNames();
    		m_allCols = new String[cols.length + 1];
    		m_allCols[0] = strDefaultIndex;
    		
    		for (int i = 0; i < cols.length; i++) {
    			m_allCols[i+1] =cols[i];
			}
    		 
    		if(enumDbType == EnumDbType.SqLite)
    		{
    			m_strFileName = FileHelper.cleanFileName(strFileName);
    		}
    		else
    		{
    			m_strFileName = strFileName;
    		}
			m_strDefaultIndex = strDefaultIndex;
			m_strTableName = strTableName.toUpperCase();
			String strTableKey = strFileName + "_" + strTableName;
			if(!m_validatedTables.containsKey(strTableKey))
			{
				m_validatedTables.put(strTableKey, new Object());
				validateTable();
				createDefaultIndex();
			}
		}
		catch(Exception ex){
			Logger.log(ex);
		}
	}
	
	private void createDefaultIndex() 
	{
		if(m_strDefaultIndex ==  null)
		{
			return;
		}
		
        try 
        {
        	// no write access
		} 
        catch (Exception ex) 
        {
			Logger.log(ex);
		}            
	}
	
	protected abstract String getCreateIndexStatement(); 

    public StringBuilder getSelectFrom()
    {
        try
        {
            StringBuilder sb = new StringBuilder(
                "select * ");
            sb.append(" from " + m_strTableName + " ");
            return sb;
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return null;
    }

    private static String getInStatement(List<String> list)
    {
        try
        {
            StringBuilder sb = new StringBuilder();
            boolean blnIsTitle = true;
            for (int i = 0 ; i < list.size(); i++)
            {
            	String str = list.get(i);
                if (!blnIsTitle)
                {
                    sb.append(",");
                }
                blnIsTitle = false;
                sb.append("'" + str + "'");
            }
            return sb.toString();
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        return null;
    }
	
	protected abstract void validateTable();

	
    public Map<String, List<T>> loadAllDataMap() throws Exception
    {
        if (m_strDefaultIndex == null)
        {
            throw new Exception("Index not found");
        }
        StringBuilder sb = getSelectFrom();
        return loadDataMap(sb.toString());
    }

    public T[] loadAllData()
    {
    	try
    	{
	        if (m_strDefaultIndex == null)
	        {
	            throw new Exception("Index not found");
	        }
	        StringBuilder sb = getSelectFrom();
	        return loadData(sb.toString());
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }
    
    public T[] loadDataFromWhere(String strWhere) throws Exception
    {
        if (m_strDefaultIndex == null)
        {
            throw new Exception("Index not found");
        }
        StringBuilder sb = getSelectFrom();
        sb.append(" where " + strWhere);
        return loadData(sb.toString());
    }

    @SuppressWarnings("unchecked")
	public Task insert(Object obj, String strKey)
    {
    	try
    	{
	    	if(obj == null)
	    	{
	    		return null;
	    	}
	    	return insert(strKey, (T)obj);
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }

    public Task insert(String strKey, T obj)
    {
    	try
    	{
	    	List<T> arrayList = new ArrayList<T>();
	    	arrayList.add(obj);
	        return insert(strKey, 
	        			 arrayList);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
    	return null;
    }

    public T[] loadDataFromKeys(List<String> strKeys) throws Exception
    {
        if (m_strDefaultIndex == null)
        {
            throw new Exception("Index not found");
        }
        StringBuilder sb = getSelectFrom();
        sb.append(" where " + m_strDefaultIndex + " IN (" +
                  getInStatement(strKeys) + ")");
        return loadData(sb.toString());
    }

    public T[] loadDataFromKey(String strKey)
    {
        try
        {
            if (m_strDefaultIndex == null)
            {
                throw new Exception("Index not found");
            }
            StringBuilder sb = getSelectFrom();

            sb.append("where " + m_strDefaultIndex + " = '" +
                      strKey + "'");
            return loadData(sb.toString());
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return null;
    }

    public void dropDefaultIndex()
    {
        String strSql = "drop index if exists " +
                SqliteConstants.KEY_COL_NAME + "_INDEX ";    	
        try {
        	
			SqliteTaskQueues.enqueueWrite(
					m_strTableName, 
					strSql,
					m_strFileName,
					m_reflector,
					m_enumDbType,
					m_strDriver).waitTask();
			
		} catch (Exception ex) {
			Logger.log(ex);
		}            
    }
	
    public void createIndex(String strIndex)
    {
        try {
        	
            String strSql = "create index if not exists " +
                    m_strTableName + "_" + strIndex + "_INDEX ON " +
                    m_strTableName + " (" + strIndex + ")";
        	
			SqliteTaskQueues.enqueueWrite(
					m_strTableName, 
					strSql,
					m_strFileName,
					m_reflector,
					m_enumDbType,
					m_strDriver).waitTask();
			
		} catch (Exception ex) {
			Logger.log(ex);
		}            
    }

    public void shrinkDb()
    {
        try {
        	
            String strSql = "vacuum";
        	
			SqliteTaskQueues.enqueueWrite(
					m_strTableName, 
					strSql,
					m_strFileName,
					m_reflector,
					m_enumDbType,
					m_strDriver).waitTask();
			
		} catch (Exception ex) {
			Logger.log(ex);
		}            
    }

    public void trunkateTable(String strTableName)
    {
    	 try {
         	
             String strSql = "delete from " + strTableName;
         	
 			SqliteTaskQueues.enqueueWrite(
 					m_strTableName, 
 					strSql,
 					m_strFileName,
 					m_reflector,
 					m_enumDbType,
 					m_strDriver).waitTask();
 			
 		} catch (Exception ex) {
 			Logger.log(ex);
 		}            
    }

    public void dropTable(String strTableName)
    {
   	 try {
      	
         String strSql = "drop table if exists " + strTableName;
     	
			SqliteTaskQueues.enqueueWrite(
					m_strTableName, 
					strSql,
					m_strFileName,
					m_reflector,
					m_enumDbType,
					m_strDriver).waitTask();
			
		} catch (Exception ex) {
			Logger.log(ex);
		}            
    }

    public List<String> loadAllKeys()
    {
    	try
    	{
	        String strQuery = "select " + m_strDefaultIndex + " from [" + m_strTableName +
	        "] ";
	
	        List<Object[]> data = new ArrayList<Object[]>();
	        execute(strQuery, data);
	        List<String> outList = new ArrayList<String>();
	        
	        for (int i = 0; i < data.size(); i++)
	        {
	            outList.add((String)data.get(i)[0]);
	        }
	
	        return outList;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return new ArrayList<String>();
    }

	public void clear() {
     	 try {
            	
             String strSql = "DELETE FROM [" + m_strTableName + "]";
         	
    			SqliteTaskQueues.enqueueWrite(
    					m_strTableName, 
    					strSql,
    					m_strFileName,
    					m_reflector,
    					m_enumDbType,
    					m_strDriver).waitTask();
    			
    		} catch (Exception ex) {
    			Logger.log(ex);
    		} 
	}
    
	public int getSize() {
        
		SqliteReadJob sqliteReadJob = null;
		
		try{
			String strQuery = "SELECT count(*) FROM [" + m_strTableName + "]";
			sqliteReadJob = new SqliteReadJob(
					null, 
					strQuery, 
					0, 
					m_strFileName, 
					m_reflector,
					m_enumDbType,
					m_strDriver);
			int intVal = SqliteTaskQueues.executeScalar(
					sqliteReadJob);
			
			return intVal;
		}
		catch(Exception ex){
			Logger.log(ex);
		}
		finally{
			
			if(sqliteReadJob != null){
				sqliteReadJob.close();
			}
		}
		return 0;
	}
	
    public void delete(String strKey)
    {
      	 try 
      	 {
             String strSql = "DELETE FROM [" + m_strTableName + "] WHERE " +
                     m_strDefaultIndex + " = '" +
                     strKey + "'";
         	
			SqliteTaskQueues.enqueueWrite(
					m_strTableName, 
					strSql,
					m_strFileName,
					m_reflector,
					m_enumDbType,
					m_strDriver).waitTask();
    			
    	} 
      	catch (Exception ex) 
      	{
      		Logger.log(ex);
    	}            
    }

    public void delete(List<String> strKeys)
    {
    	 try {
            	
             String strSql = "DELETE FROM [" + m_strTableName + "] WHERE " +
                     m_strDefaultIndex + " IN (" +
                     getInStatement(strKeys) + ")";
         	
    			SqliteTaskQueues.enqueueWrite(
    					m_strTableName, 
    					strSql,
    					m_strFileName,
    					m_reflector,
    					m_enumDbType,
    					m_strDriver).waitTask();
    			
    		} catch (Exception ex) {
    			Logger.log(ex);
    		}         
    }
    
    public abstract void execute(String strQuery, 
    		List<Object[]> data);

    public abstract T[] loadData(String strQuery);

    public abstract boolean containsKey(String strKey);

    public abstract Map<String, List<T>> loadDataMap(String strQuery);

    public Task insert(
    		Map<String, List<T>> objs)
    {
    
    	try
    	{
	    	if(objs == null || objs.size() == 0)
	        {
	            return null;
	        }
	        
	        int intObjCounter = 0;
	        for(List<T> val : objs.values())
	        {
	        	intObjCounter += val.size();
	        }
	
	        String strMessage = "Inserting [" + intObjCounter + "] rows. File: " +
	                         m_strFileName;
	        Verboser.Talk(strMessage);        
	        
	        List<KeyValuePair<String, T>> keyValuePairs = new ArrayList<KeyValuePair<String, T>>();
	        
	        for (Map.Entry<String, List<T>> keyValuePair : objs.entrySet())
	        {
	            for (T tObj : keyValuePair.getValue())
	            {
	                keyValuePairs.add(new KeyValuePair<String, T>(
	                    keyValuePair.getKey(),
	                    tObj));
	            }
	            keyValuePair.getValue().clear();
	        }
	        
	        Task task = SqliteJdbcWrapper.bulkInsert(
	                m_strTableName,
	                m_allCols,
	                keyValuePairs,
	        		m_reflector,
	        		m_strFileName,
	        		m_blnUseCompression,
	        		m_enumDbType,
	        		m_strDriver);
			objs.clear();
			
			return task;    	
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }

    public Task insertMap(
    		Map<String, T> objs)
    {
    	try
    	{
	        int intObjCounter = objs.size();
	        String strMessage = "Inserting [" + intObjCounter + "] rows. File: " +
	                         m_strFileName;
	        Verboser.Talk(strMessage);        
	        
	        List<KeyValuePair<String, T>> keyValuePairs = new ArrayList<KeyValuePair<String, T>>();
	        
	        for (Map.Entry<String, T> keyValuePair : objs.entrySet())
	        {
	                keyValuePairs.add(new KeyValuePair<String, T>(
	                    keyValuePair.getKey(),
	                    keyValuePair.getValue()));
	        }
	        Task task = SqliteJdbcWrapper.bulkInsert(
	                m_strTableName,
	                m_allCols,
	                keyValuePairs,
	        		m_reflector,
	        		m_strFileName,
	        		m_blnUseCompression,
	        		m_enumDbType,
	        		m_strDriver);
			//objs.clear();
			
			return task;    	
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }
    
    public Task insert(
    		String strKey,
    		List<T> objs)
    {
    	try
    	{
	        if(objs == null || objs.size() == 0)
	        {
	            return null;
	        }
	        
	        objs = new ArrayList<T>(objs);
	        List<KeyValuePair<String, T>> keyValuePairs = new ArrayList<KeyValuePair<String, T>>();
	        
	        for (T tObj : objs)
	        {
	            keyValuePairs.add(new KeyValuePair<String, T>(
	                strKey,
	                tObj));
	        }
	        
	        int intObjCounter = objs.size();
	
	        String strMessage = "Inserting [" + intObjCounter + "] rows. File: " +
	                         m_strFileName;
	        Verboser.Talk(strMessage);        
	        
	        Task task = SqliteJdbcWrapper.bulkInsert(
	                                m_strTableName,
	                                m_allCols,
	                                keyValuePairs,
	                        		m_reflector,
	                        		m_strFileName,
	                        		m_blnUseCompression,
	                        		m_enumDbType,
	                        		m_strDriver);
	        objs.clear();
	        objs = null;
	        return task;    	
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }
    
    public boolean getIsClosed(){
    	return m_blnIsClosed;
    }
    
    public void close(){
    	
    	if(m_blnIsClosed){
    		return;
    	}
    	
    	m_blnIsClosed = true;
    	m_strFileName = null;
    	m_strDefaultIndex = null;
    	m_strTableName = null;
    	m_reflector = null;
    	m_genericClass = null;
    	m_allCols = null;
    }

	public abstract void executeNonQuery(String strSql);

	public boolean getUseCompression() {
		return m_blnUseCompression;
	}

	public void setUseCompression(boolean useCompression) {
		m_blnUseCompression = useCompression;
	}
}