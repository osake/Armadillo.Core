package Armadillo.Core;

import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Core.Serialization.Serializer;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

public class TestSerializer 
{
	@Test
	public void testBytes() 
	{
		try
		{
			Foo foo = new Foo("foo", 1, 3.5);
			foo.m_list = new ArrayList<String>();
			foo.m_list.add("test");
	
				byte[] bytes = Serializer.getBytes(foo);
				Foo f = new Foo();
				Serializer.deserialize(bytes, f);
				System.out.println(f);
				
		}
		catch(Exception ex)
		{
			Logger.log(ex);
			assertTrue("Exception", false);
		}
	}
	
	@Test
	public void testFile2() 
	{
		String strFileName = "c:\\testSerializer";
		try 
		{
			Foo foo = new Foo("foo", 1, 3.5);
			Serializer.serialize(strFileName, foo);

			Foo f = new Foo();
			Serializer.deserialize(strFileName, f);
			System.out.println(f);
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
	
	@Test
	public void testFile() 
	{
		String strFileName = "c:\\testSerializer";
		LinkedBuffer linkedBuffer = null;
		try 
		{
			FileOutputStream stream = new FileOutputStream(strFileName);
			Schema<Foo> schema = RuntimeSchema.getSchema(Foo.class);
			linkedBuffer = Serializer.getApplicationBuffer();
			Foo foo = new Foo("foo", 1, 3.5);
			ProtostuffIOUtil.writeTo(stream, foo, schema, linkedBuffer);
			InputStream in = new FileInputStream(strFileName);

			Foo f = new Foo();
			ProtostuffIOUtil.mergeFrom(in, f, schema);
			System.out.println(f);
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		finally
		{
			if(linkedBuffer != null)
			{
				linkedBuffer.clear();
			}
		}
	}
	
	@Test
	public void testBytesList() 
	{
		try
		{
			for (int i = 0; i < 100; i++) 
			{
				ArrayList<String> m_list = new ArrayList<String>();
				String strTestValue = "test";
				m_list.add(strTestValue);
				ObjectWrapper objWrp = new ObjectWrapper(m_list);
				byte[] bytes = Serializer.getBytes(objWrp);
				ObjectWrapper objWrp2 = new ObjectWrapper();
				Serializer.deserialize(bytes, objWrp2);
				@SuppressWarnings("unchecked")
				ArrayList<String> list2 = (ArrayList<String>)objWrp2.getObj();
				Assert.assertTrue("Invalid number of rows", list2.size() == 1);
				Assert.assertTrue("Invalid value in lsit", list2.get(0).equals(strTestValue));
			}
		}
		catch(Exception ex){
			Logger.log(ex);
			assertTrue("Exception", false);
		}
	}
}
