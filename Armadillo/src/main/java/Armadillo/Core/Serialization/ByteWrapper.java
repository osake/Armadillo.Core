package Armadillo.Core.Serialization;

import java.io.Closeable;

public class ByteWrapper implements Closeable {

	public byte[] m_bytes;

	public ByteWrapper(byte[] bytes){
		m_bytes = bytes;
	}
	
	@Override
	public void close() {
		m_bytes = null;
	}
}
