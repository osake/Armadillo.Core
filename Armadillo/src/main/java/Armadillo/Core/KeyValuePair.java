package Armadillo.Core;

import java.io.Closeable;
import java.io.IOException;

public class KeyValuePair<T1, T2> implements Closeable{
	
	private T1 m_key;
	private T2 m_value;
	
	public KeyValuePair(T1 key, T2 value)
	{
		m_key = key;
		m_value = value;
	}
	
	public KeyValuePair() {
	}

	public T1 getKey(){
		return m_key;
	}
	
	public T2 getValue(){
		return m_value;
	}

	public void close() throws IOException 
	{
		m_key = null;
		m_value = null;
	}
	
}