package Armadillo.Core;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.reflections.Reflections;

public class TestClassHelper 
{
	@Test
	public void doTest(){
		
	     Reflections reflections = new Reflections("HC");
	     Set<Class<? extends AFoo>> subTypes = 
	               reflections.getSubTypesOf(AFoo.class);
	     
	     for(Class<? extends AFoo> subType : subTypes)
	     {
	    	 Assert.assertTrue(AFoo.class.isAssignableFrom(subType));
	     }
	}

}
