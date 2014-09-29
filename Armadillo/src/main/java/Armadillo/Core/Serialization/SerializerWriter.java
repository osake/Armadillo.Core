package Armadillo.Core.Serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import org.joda.time.DateTime;

import Armadillo.Core.Logger;

import com.google.protobuf.CodedOutputStream;

public class SerializerWriter implements ISerializerWriter {
    
	ByteArrayOutputStream m_ms;
	private CodedOutputStream m_output;
	private boolean m_blnIsFlushed;
	
	public SerializerWriter(){
		
		m_ms = new ByteArrayOutputStream();
		m_output = CodedOutputStream.newInstance(m_ms);		
	}
	
    /* (non-Javadoc)
	 * @see Armadillo.Core.ISerializerWriter2#Write(byte)
	 */
    @Override
	public void Write(byte value)
    {
        try {
			m_output.writeRawByte(value);
		} catch (IOException ex) {
			Logger.log(ex);
		}
    }

    /* (non-Javadoc)
	 * @see Armadillo.Core.ISerializerWriter2#Write(short)
	 */
    @Override
	public void Write(short value)
    {
        try {
			m_output.writeRawVarint32(value);
		} catch (IOException e) {
			Logger.log(e);
		}
    }

    /* (non-Javadoc)
	 * @see Armadillo.Core.ISerializerWriter2#Write(int)
	 */
    @Override
	public void Write(int value)
    {
        InternalWriteInt(value);
    }

    /* (non-Javadoc)
	 * @see Armadillo.Core.ISerializerWriter2#Write(long)
	 */
    @Override
	public void Write(long value)
    {
        InternalWriteLong(value);
    }

    /* (non-Javadoc)
	 * @see Armadillo.Core.ISerializerWriter2#Write(double)
	 */
    @Override
	public void Write(double value)
    {
        InternalWriteDouble(value);
    }

    /* (non-Javadoc)
	 * @see Armadillo.Core.ISerializerWriter2#Write(float)
	 */
    @Override
	public void Write(float value)
    {
        try {
			m_output.writeFloatNoTag(value);
		} catch (IOException e) {
			Logger.log(e);
		}
    }

    /* (non-Javadoc)
	 * @see Armadillo.Core.ISerializerWriter2#Write(boolean)
	 */
    @Override
	public void Write(boolean value)
    {
    	try {
			m_output.writeBoolNoTag(value);
		} catch (IOException e) {
			Logger.log(e);
		}
    }

    /* (non-Javadoc)
	 * @see Armadillo.Core.ISerializerWriter2#Write(java.util.Date)
	 */
    @Override
	public void Write(Date value)
    {
        Write(new DateTime(value).getMillis());
    }

    /* (non-Javadoc)
	 * @see Armadillo.Core.ISerializerWriter2#Write(java.lang.String)
	 */
    @Override
	public void Write(String value)
    {
        try {
	        if (value == null || value == "")
	        {
	            Write(true);
	            return;
	        }

	        Write(false);
			m_output.writeStringNoTag(value);
			
		} catch (IOException e) {
			Logger.log(e);
		}
    }

    /// <summary>
    /// Writes a byte to the buffer
    /// </summary>
    /// <param name="data">The byte to write</param>
    protected void InternalWriteByte(byte data)
    {
        try {
			m_output.writeRawByte(data);
		} catch (IOException e) {
			Logger.log(e);
		}
    }

    /// <summary>
    /// Writes an int to the buffer
    /// </summary>
    /// <param name="data">The int to write</param>
    protected void InternalWriteInt(int data)
    {
    	try {
			m_output.writeRawVarint32(data);
		} catch (IOException e) {
			Logger.log(e);
		}
    }

    protected void InternalWriteDouble(double data)
    {
        try {
			m_output.writeDoubleNoTag(data);
		} catch (IOException e) {
			Logger.log(e);
		}
    }

    protected void InternalWriteLong(long data)
    {
        try {
			m_output.writeRawVarint64(data);
		} catch (IOException e) {
			Logger.log(e);
		}
    }

	@Override
	public void Write(Object obj) {
		byte[] bytes = Serializer.getBytes(obj);
		Write(bytes);
	}    
	
	public void Write(byte[] value)
    {
        try {
        	Write(value.length);
			m_output.writeRawBytes(value);
		} catch (IOException e) {
			Logger.log(e);
		}
    }

	@Override
	public byte[] GetBytes() {
		if(m_blnIsFlushed){
			try {
				throw new Exception("Already flushed");
			} catch (Exception e) {
				Logger.log(e);
			}
		}
		
		try {
			m_output.flush();
			m_ms.flush();
			m_blnIsFlushed = true;
			m_output = null;
		} catch (IOException ex) {
			Logger.log(ex);
		}
		byte[] bytes = m_ms.toByteArray();
		m_ms = null;
		return bytes;
	}
	
}
