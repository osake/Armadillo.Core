package Armadillo.Core.Serialization;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Date;
import org.joda.time.DateTime;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.google.protobuf.CodedInputStream;

public class SerializerReader implements ISerializerReader {
	
    protected CodedInputStream m_input;

    public SerializerReader(byte[] buf){
    	
    	m_input = CodedInputStream.newInstance(buf);
    }
    
    /* (non-Javadoc)
	 * @see Armadillo.Core.ISerializerReader#ReadInt16()
	 */
    @Override
	public short ReadInt16()
    {
        try {
			return (short)m_input.readRawVarint32();
		} catch (IOException e) {
			Logger.log(e);
		}
        return 0;
    }

    /* (non-Javadoc)
	 * @see Armadillo.Core.ISerializerReader#ReadInt32()
	 */
    @Override
	public int ReadInt32()
    {
        try {
			return (int)m_input.readRawVarint32();
		} catch (IOException e) {
			Logger.log(e);
		}
        return 0;
    }

    /* (non-Javadoc)
	 * @see Armadillo.Core.ISerializerReader#ReadInt64()
	 */
    @Override
	public long ReadInt64()
    {
        try {
			return (long) m_input.readRawVarint64();
		} catch (IOException e) {
			Logger.log(e);
		}
        return 0;
    }

    /* (non-Javadoc)
	 * @see Armadillo.Core.ISerializerReader#ReadSingle()
	 */
    @Override
	public float ReadSingle()
    {
        try {
			return m_input.readFloat();
		} catch (IOException e) {
			Logger.log(e);
		}
        return 0;
    }

    /* (non-Javadoc)
	 * @see Armadillo.Core.ISerializerReader#ReadDouble()
	 */
    @Override
	public double ReadDouble()
    {
        try {
			return m_input.readDouble();
		} catch (IOException e) {
			Logger.log(e);
		}
        return 0;
    }

    /* (non-Javadoc)
	 * @see Armadillo.Core.ISerializerReader#ReadBoolean()
	 */
    @Override
	public boolean ReadBoolean()
    {
        try {
			return m_input.readBool();
		} catch (IOException e) {
			Logger.log(e);
		}
        return false;
    }

    /* (non-Javadoc)
	 * @see Armadillo.Core.ISerializerReader#ReadDateTime()
	 */
    @Override
	public Date ReadDateTime()
    {
        return new DateTime(ReadInt64()).toDate();
    }

//    public Type ReadType()
//    {
//        var serializedType = (EnumSerializedType)ReadByte();
//        if (serializedType == EnumSerializedType.ValueType)
//        {
//            var intTypeIndex = ReadInt32();
//            return PrimitiveTypesCache.PrimitiveTypes[intTypeIndex];
//        }
//        if (serializedType == EnumSerializedType.ReferenceType)
//        {
//            var intTypeIndex = ReadInt32();
//            if(intTypeIndex < 0)
//            {
//                string strClass = ReadString();
//                if (!string.IsNullOrEmpty(strClass))
//                {
//                    string strAssembly = ReadString();
//                    if(!string.IsNullOrEmpty(strAssembly))
//                    {
//                        Type calcType = Type.GetType(
//                            strClass + "," +
//                            strAssembly);
//                        return calcType;
//                    }
//                }
//                return null;
//            }
//            return KnownTypesCache.GetTypeFromId(intTypeIndex);
//        }
//        return null;
//    }

    /* (non-Javadoc)
	 * @see Armadillo.Core.ISerializerReader#ReadString()
	 */
    @Override
	public String ReadString()
    {
        try {
	        if(ReadBoolean())
	        {
	            return "";
	        }
			return m_input.readString();
			
		} catch (IOException ex) {
			
			Logger.log(ex);
		}
        return "";
    }

	@SuppressWarnings("unchecked")
	@Override
	public Object ReadObject() {
		try {
			int intsize = m_input.readInt32();
			byte[] bytes = m_input.readRawBytes(intsize);
			ObjectWrapper obj = new ObjectWrapper();
			@SuppressWarnings("rawtypes")
			Schema schema = RuntimeSchema.getSchema(obj.getClass());
			ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
			return obj.m_obj;
		} catch (Exception ex) {
			Logger.log(ex);
		}
		return null;
	}

	@Override
	public byte[] ReadByteArray() {
		try {
			int intBytesSize = ReadInt32();
			byte[] bytes = m_input.readRawBytes(intBytesSize);
			return bytes;
		} catch (IOException e) {
			Logger.log(e);
		}
		return null;
	}

	@Override
	public int getBytesRemaining() {
		return m_input.getTotalBytesRead();
	}

	@Override
	public byte readByte() {
		try {
			return m_input.readRawByte();
		} catch (IOException ex) {
			Logger.log(ex);
		}
		return -1;
	}

	@Override
	public Type readType() {
		return null;
	}
}
