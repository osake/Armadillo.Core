package Armadillo.Core;


import org.junit.Assert;
import org.junit.Test;

import com.esotericsoftware.reflectasm.FieldAccess;

public class ReflectionTests {
	
	@Test
	public void testFoo()
	{
		Foo someObject = new Foo("a",1,2);
		FieldAccess access = FieldAccess.get(Foo.class);
		
		//String[] filedNames = access.getFieldNames();
		
		Object val1 = access.get(someObject, 0);
		
		System.out.println(val1);
		String strTestValue = "testValue";
		String strField = "m_string";
		access.set(someObject, strField, strTestValue);
		String strFieldSet = (String)access.get(someObject, strField);
		Assert.assertTrue("Values are not equal", strTestValue.equals(strFieldSet));
	}
}
