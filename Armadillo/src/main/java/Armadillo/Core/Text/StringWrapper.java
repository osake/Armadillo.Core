package Armadillo.Core.Text;
import java.io.Closeable;
import java.io.IOException;

public class StringWrapper implements Closeable {
	
	private String m_str;
	
	public StringWrapper(String str){
		m_str = str;
	}
	
	public String getStr(){
		return m_str;
	}

	@Override
	public void close() throws IOException {
		m_str = null;
	}

}
