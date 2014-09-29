package Armadillo.Core.Cache;

import java.io.Closeable;
import Armadillo.Core.Concurrent.EfficientProducerConsumerQueue;
import Armadillo.Core.Concurrent.Task;
import Armadillo.Core.Reflection.Reflector;

public class SqliteWriteJob implements Closeable {

	private String[] m_columns;
	private EfficientProducerConsumerQueue<SqliteWriteJob> m_writerQueue;
	private Object m_lockObj;
	private String m_strFileName;
	private Task m_task;
	private boolean m_blnIsConsumed;
	private Object[][] m_importArr;
	private String m_strTableName;
	private Reflector m_reflector;
	private String m_strSql;
	private boolean m_blnIsOnlyWrite;
	private boolean m_blnIsClosed;
	private EnumDbType m_enumDbType;

	public SqliteWriteJob(
			String[] columns, 
			Object[][] importArr,
			EfficientProducerConsumerQueue<SqliteWriteJob> writerQueue,
			String strFileName, 
			String strTableName,
			Reflector reflector,
			EnumDbType enumDbType) {

		m_lockObj = new Object();
		m_columns = columns;
		m_importArr = importArr;
		m_writerQueue = writerQueue;
		m_strFileName = strFileName;
		m_strTableName = strTableName;
		m_reflector = reflector;
		m_enumDbType = enumDbType;
	}

	public String[] getColumns() {
		return m_columns;
	}

	public EfficientProducerConsumerQueue<SqliteWriteJob> getWriterQueue() {
		return m_writerQueue;
	}

	public Object getLockObj() {
		return m_lockObj;
	}

	public String getFileName() {
		return m_strFileName;
	}

	public void setTask(Task task) {
		m_task = task;
	}

	public Task getTask() {
		return m_task;
	}

	public boolean getIsConsumed() 
	{
		return m_blnIsConsumed;
	}

	public void setIsConsumed(boolean blnIsConsumed) 
	{
		m_blnIsConsumed = blnIsConsumed;
	}

	public Object[][] getImportArr() {
		return m_importArr;
	}

	public void setImportArr(Object[][] importArr) {
		m_importArr = importArr;
	}

	public String getTableName() {
		return m_strTableName;
	}

	@Override
	public void close() {
		
		if(m_blnIsClosed){
			return;
		}
		
		m_blnIsClosed = true;
		m_columns = null;
		m_writerQueue = null;
		m_lockObj = null;
		m_strFileName = null;
		m_task = null;
		m_importArr = null;
		m_strTableName = null;
		m_reflector = null;
		m_strSql = null;
	}
	
	public Reflector getReflector() {
		
		return m_reflector;
	}

	public void setSql(String strSql) {
		m_strSql = strSql;
	}
	
	public String getSql() {
		return m_strSql;
	}

	public boolean getIsOnlyWrite() {
		return m_blnIsOnlyWrite;
	}
	
	public boolean getIsClosed(){
		return m_blnIsClosed;
	}
	
	public void setIsOnlyWrite(boolean blnVal) {
		m_blnIsOnlyWrite = blnVal;
	}

	public EnumDbType getEnumDbType() {
		// TODO Auto-generated method stub
		return m_enumDbType;
	}
}