package Armadillo.Core;

import java.io.File;

import org.junit.Test;

import Armadillo.Core.Console;

public class TestFileHelper 
{
	@Test
	public void doTest(){
		String strName = "M:\\My Documents\\TravelDocs\\2013.11.06.xls".toLowerCase();
		String strPath = new File(strName).getParent();
		Console.writeLine(
				strPath);
	}
}
