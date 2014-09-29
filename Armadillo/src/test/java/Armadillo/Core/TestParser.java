package Armadillo.Core;

import org.junit.Assert;
import org.junit.Test;

import Armadillo.Core.ParserHelper;

public class TestParser 
{
    @Test
    public void DoTest()
    {
        Assert.assertTrue(ParserHelper.IsNumeric("1.2324532"));
        Assert.assertTrue(ParserHelper.IsNumeric("1.2324532%"));
        Assert.assertTrue(ParserHelper.IsNumeric("1E10"));
        Assert.assertTrue(ParserHelper.IsNumeric("1E10%"));
        Assert.assertTrue(!ParserHelper.IsNumeric("a1.2324532"));
        Assert.assertTrue(!ParserHelper.IsNumeric("1.2324532b"));
        Assert.assertTrue(ParserHelper.IsNumeric("$1'123,421.2324532"));
        Assert.assertTrue(ParserHelper.IsNumeric("�1.2324532"));
        Assert.assertTrue(ParserHelper.IsNumeric("1.2324532%"));
    }

}
