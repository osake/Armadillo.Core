package Armadillo.Core;

public class Console {

	public static void writeLine(String strMessage) {
		System.out.println(strMessage);
	}

	public static void writeLine(Exception ex) {
		ex.printStackTrace();
		
	}

	public static void writeLine(Object panel) 
	{
		writeLine(panel.toString());
	}

	public static void WriteLine() 
	{
		WriteLine("");
	}

	public static void WriteLine(Object obj) 
	{
		if(obj == null)
		{
			WriteLine("null");
			return;
		}
		WriteLine(obj.toString());
	}
	
	public static void WriteLine(String string) 
	{
		writeLine(string);
	}

	public static void WriteLine(Exception ex) 
	{
		writeLine(ex);
	}

	public static void Write(String string) 
	{
		System.out.print(string);
	}
}
