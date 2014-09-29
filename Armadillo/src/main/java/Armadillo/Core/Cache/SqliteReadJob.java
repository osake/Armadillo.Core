package Armadillo.Core.Cache;

import java.io.Closeable;
import java.util.List;

import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.Concurrent.ProducerConsumerQueue;
import Armadillo.Core.Reflection.Reflector;
import Armadillo.Core.Text.StringHelper;

public class SqliteReadJob implements Closeable 
{
	private ProducerConsumerQueue<SqliteReadJob> m_readerQueue;
	private String m_strSql;
	private List<Object[]> m_data;
	private int m_intColCount;
	private String m_strFileName;
	private Reflector m_reflector;
	private boolean m_blnIsOnlyRead;
	private boolean m_blnIncludeKeyId;
	private EnumDbType m_enumDbType;
	private boolean m_blnIsDisposed;
	private boolean m_blnLoadColNames;
	private String[] m_colNames;
	
	public SqliteReadJob(
			ProducerConsumerQueue<SqliteReadJob> readerQueue,
			 String strSql,
			 int intColCount,
			 String strFileName,
			 Reflector reflector,
			 EnumDbType enumDbType) 
	{
		if(StringHelper.IsNullOrEmpty(strFileName))
		{
			try 
			{
				throw new HCException("Empty file name");
			} 
			catch (HCException e) 
			{
				Logger.log(e);
			}
		}
		
		m_readerQueue = readerQueue;
		m_strSql = strSql;
		m_intColCount = intColCount;
		m_strFileName = strFileName;
		m_reflector = reflector;
		m_enumDbType = enumDbType;
	}
	
	public ProducerConsumerQueue<SqliteReadJob> getReaderQueue() 
	{
		return m_readerQueue;
	}

	public String getSql() {
		return m_strSql;
	}

	public void setData(List<Object[]> data) 
	{
		m_data = data;
	}
	
	public List<Object[]> getData()
	{
		return m_data;
	}

	public int getColumnCount() 
	{
		return m_intColCount;
	}

	public String getFileName() 
	{
		return m_strFileName;
	}

	@Override
	public void close() {
		m_blnIsDisposed = true;
		m_readerQueue = null;
		m_strSql = null;
		m_data = null;
		m_strFileName = null;
		m_reflector = null;
	}
	
	public boolean getIsDisposed(){
		return m_blnIsDisposed;
	}

	public Reflector getReflector() {
		
		return m_reflector;
	}

	public void setIsOnlyRead(boolean blnVal) {
		m_blnIsOnlyRead = blnVal;
	}
	
	public boolean getIsOnlyRead() {
		return m_blnIsOnlyRead;
	}

	public void setIncludeKeyId(boolean blnValue) {
		m_blnIncludeKeyId = blnValue;
	}
	
	public boolean getIncludeKeyId() {
		return m_blnIncludeKeyId;
	}

	public EnumDbType getEnumDbType() {
		return m_enumDbType;
	}

	public void setQueue(ProducerConsumerQueue<SqliteReadJob> queue) {
		m_readerQueue = queue;
	}

	public void setLoadColNames(boolean blnLoadColNames) {
		m_blnLoadColNames = blnLoadColNames;
	}
	
	public boolean getLoadColNames() {
		return m_blnLoadColNames;
	}

	public String[] getColNames() {
		return m_colNames;
	}

	public void setColNames(String[] colNames) {
		m_colNames = colNames;
	}
}