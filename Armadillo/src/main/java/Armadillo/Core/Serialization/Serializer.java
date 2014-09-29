package Armadillo.Core.Serialization;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import Armadillo.Core.Logger;
import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Serializer {

	static final int bufferSize = 20480; // Preferably the size of your largest
										// possible string if you are streaming.
	
    public static final int BYTES_LIMIT = (int) (0.5*1024f*1024f);
	
    private static final ThreadLocal<LinkedBuffer> localBuffer = new ThreadLocal<LinkedBuffer>() 
    {
		public LinkedBuffer initialValue() 
		{
			return LinkedBuffer.allocate(bufferSize);
		}
	};
	
    static 
    {
        System.setProperty("Dprotostuff.runtime.collection_schema_on_repeated_fields", "true");
        System.setProperty("Dprotostuff.runtime.morph_collection_interfaces", "true");
        System.setProperty("Dprotostuff.runtime.morph_map_interfaces", "true");
        
        System.setProperty("protostuff.runtime.collection_schema_on_repeated_fields", "true");
        System.setProperty("protostuff.runtime.morph_collection_interfaces", "true");
        System.setProperty("protostuff.runtime.morph_map_interfaces", "true");
        
    }

	public static LinkedBuffer getApplicationBuffer() 
	{
		return localBuffer.get();
	}

	@SuppressWarnings("unchecked")
	public static <T> void deserialize(byte[] bytes, Object obj) 
	{
		
		try 
		{
			@SuppressWarnings("rawtypes")
			Schema schema = RuntimeSchema.getSchema(obj.getClass());
			ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
		} 
		catch (Exception e) 
		{
			Logger.log(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> void deserialize(String strFileName, Object obj) 
	{
		try 
		{
			InputStream in = new FileInputStream(strFileName);
			@SuppressWarnings("rawtypes")
			Schema schema = RuntimeSchema.getSchema(obj.getClass());
			ProtostuffIOUtil.mergeFrom(in, obj, schema);
		} 
		catch (Exception e) 
		{
			Logger.log(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static void serialize(String strFileName0, Object obj) 
	{
		LinkedBuffer linkedBuffer = null;
		try 
		{
			String strFileName = strFileName0 + "tmp";
			FileOutputStream stream = new FileOutputStream(strFileName);
			@SuppressWarnings("rawtypes")
			Schema schema = RuntimeSchema.getSchema(obj.getClass());
			linkedBuffer = getApplicationBuffer();
			ProtostuffIOUtil.writeTo(stream, obj, schema, linkedBuffer);
			File file = new File(strFileName);
			file.renameTo(new File(strFileName0));
		} 
		catch (Exception e) 
		{
			Logger.log(e);
		}
		finally 
		{
			if(linkedBuffer != null)
			{
				linkedBuffer.clear();
			}
		}
	}

	public static byte[] getBytes(Object obj) 
	{
		try
		{
			// this is lazily created and cached by RuntimeSchema
			// so its safe to call RuntimeSchema.getSchema(Foo.class) over and over
			// The getSchema method is also thread-safe
			@SuppressWarnings("rawtypes")
			Schema schema = RuntimeSchema.getSchema(obj.getClass());
			LinkedBuffer buffer = getApplicationBuffer();
	
			try 
			{
				@SuppressWarnings("unchecked")
				byte[] protostuff = ProtostuffIOUtil.toByteArray(
						obj, 
						schema,
						buffer);
				
				return protostuff;
			} 
			finally 
			{
				buffer.clear();
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}

	public static ISerializerWriter GetWriter() 
	{
		return new SerializerWriter();
	}

	public static ISerializerReader GetReader(byte[] bytes) 
	{
		return new SerializerReader(bytes);
	}

    public static ArrayList<byte[]> GetByteArrList(byte[] bytes)
    {
        int intBytesLength = bytes.length;
        int intIndex = 0;
        ArrayList<byte[]> byteList = new ArrayList<byte[]>(2 + (intBytesLength / BYTES_LIMIT));
        while(intIndex < intBytesLength)
        {
            int intArraySize = Math.min(
                BYTES_LIMIT, 
                intBytesLength - intIndex);
            byte[] currArr = new byte[intArraySize];
            currArr = Arrays.copyOfRange(bytes, 
            		intIndex, 
            		intIndex + intArraySize);
            byteList.add(currArr);
            intIndex += intArraySize;
        }
        return byteList;
    }
}