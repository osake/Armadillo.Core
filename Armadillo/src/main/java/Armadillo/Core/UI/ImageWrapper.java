package Armadillo.Core.UI;

public class ImageWrapper 
{
	private byte[] m_bytes;

	public ImageWrapper()
	{
	}
	
	public ImageWrapper(byte[] bytes) 
	{
		m_bytes = bytes;
	}

	public byte[] getBytes() 
	{
		return m_bytes;
	}

	public void setBytes(byte[] bytes) 
	{
		m_bytes = bytes;
	}
}
