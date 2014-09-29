package Armadillo.Core;

import org.junit.Assert;
import org.junit.Test;

import Armadillo.Core.Text.Tokeniser;
import Armadillo.Core.Text.TokeniserHelper;

public class TestTokeniserHelper 
{
	@Test
	public void testTokinser(){
		
		String str = " a\tab\babc\n abcd fgh";
		String[] tokensArr = TokeniserHelper.tokenise(str, "\t\b\n ");
		
		Assert.assertTrue("Invalid number of tokens", tokensArr.length == 5);
		Assert.assertTrue(tokensArr[0].equals("a"));
		Assert.assertTrue(tokensArr[1].equals("ab"));
		Assert.assertTrue(tokensArr[2].equals("abc"));
		Assert.assertTrue(tokensArr[3].equals("abcd"));
		Assert.assertTrue(tokensArr[4].equals("fgh"));
	}
	
	@Test
    public void DoTest()
    {
        final String strTextExample = "This �1.234, $1.5E6 is_a sentence-a Million    �%^� assdf word1 word2 /t";

        String[] substringsList = Tokeniser.Tokenise(strTextExample);
        Assert.assertTrue("this".equals(substringsList[0]));
        Assert.assertTrue(1.234 ==  Double.parseDouble(substringsList[1]));
        Assert.assertTrue(1.5E6 == Double.parseDouble(substringsList[2]));
        Assert.assertTrue("is".equals(substringsList[3]));
        Assert.assertTrue("a".equals(substringsList[4]));
        Assert.assertTrue("sentence".equals(substringsList[5]));
        Assert.assertTrue("a".equals(substringsList[6]));
        Assert.assertTrue("million".equals(substringsList[7]));
        Assert.assertTrue("assdf".equals(substringsList[8]));
        Assert.assertTrue("word".equals(substringsList[9]));
        Assert.assertTrue("1.0".equals(substringsList[10]));
        Assert.assertTrue("word".equals(substringsList[11]));
        Assert.assertTrue("2.0".equals(substringsList[12]));
    }

}
