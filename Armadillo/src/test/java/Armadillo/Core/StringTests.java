package Armadillo.Core;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import Armadillo.Core.Console;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.Text.TextHelper;

public class StringTests 
{
    @Test
    public void testHashMap(){
    	Map<String, Object> map = new HashMap<String,Object>();
    	map.put("a", new Object());
    	map.clear();
    	Console.writeLine("size " + map.size());
    }
	
	@Test
	public void testReplaceCaseInsensitive2() {
		String original = "HELLo wOrLd";
		String pattern = "woRld";
		String replacement = "again";

		String result = TextHelper.replaceCaseInsensitive(original, pattern, replacement)
				.toLowerCase();
		assertEquals("invalid result", result, "hello again");
	}
	
	@Test
	public void testReplaceCaseInsensitive()
	{
		String str = "Der Tisch";
		String str2 = StringHelper.ReplaceCaseInsensitive(str, "der", "The");
		Assert.assertTrue(str2.equals("The Tisch"));
		
	}
	
	@Test
	public void testRemoveCommonSymbols()
	{
		String strTest = "$ hello%world";
		String strClean = StringHelper.RemoveCommonSymbols(strTest);
		Assert.assertTrue(strClean.equals("hello world"));
	}
}
