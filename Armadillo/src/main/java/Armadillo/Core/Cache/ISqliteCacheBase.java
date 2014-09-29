package Armadillo.Core.Cache;

import java.io.Closeable;
import java.sql.Connection;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public interface ISqliteCacheBase extends Closeable {


	String getFileName();

	void close();
	
	boolean isClosed();

	Connection getDbConnection();

	ReentrantReadWriteLock getReadWriteLock();	

}
