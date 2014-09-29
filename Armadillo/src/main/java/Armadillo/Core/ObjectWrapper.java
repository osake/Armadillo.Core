package Armadillo.Core;

import java.io.IOException;
import java.io.Closeable;

public class ObjectWrapper implements Closeable {
	
	public Object m_obj;
	
	public ObjectWrapper(Object str){
		m_obj = str;
	}
	
	public ObjectWrapper() {
	}

	public Object getObj(){
		return m_obj;
	}

	public void close() throws IOException {
		m_obj = null;
	}
}
