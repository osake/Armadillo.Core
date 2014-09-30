package Armadillo.Analytics;

import java.util.Hashtable;

public class OptimisationSandBox implements Comparable<OptimisationSandBox> 
{
	public String str;

	public static void main(String[] args)
	{
		test();
	}
	
	public static void test()
	{
		Hashtable<OptimisationSandBox, String> map = new Hashtable<OptimisationSandBox, String>();
		OptimisationSandBox sandBox1 = new OptimisationSandBox();
		sandBox1.str = "1";
		map.put(sandBox1, "");
		
		OptimisationSandBox sandBox2 = new OptimisationSandBox();
		sandBox2.str = "2";
		map.put(sandBox2, "");
		
		OptimisationSandBox sandBox3 = new OptimisationSandBox();
		sandBox3.str = "2";
		boolean blnContains = map.containsKey(sandBox3);
		System.out.println(blnContains + "");
	}
	
	@Override
	public int hashCode() 
	{
		if(str == null)
		{
			return 0;
		}
		
		return str.hashCode();
	}
	
	@Override
	public boolean equals(Object arg0) 
	{
		return this.str.equals(((OptimisationSandBox)arg0).str);
	}
	
	public int compareTo(OptimisationSandBox arg0) 
	{
		return this.str.compareTo(((OptimisationSandBox)arg0).str);
	}
}
