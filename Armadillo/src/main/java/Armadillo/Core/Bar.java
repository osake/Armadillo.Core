package Armadillo.Core;

import java.util.ArrayList;
import org.joda.time.DateTime;

public class Bar extends Foo 
{
	public DateTime m_dateFoo;
	private boolean m_blnClosed;
	
	public Bar(
			String string, 
			int j, 
			double d,
			DateTime dateFoo) {
		
		super(string, j,d,DateTime.now().toDate(),
				DateTime.now());
		
		m_dateFoo = dateFoo;
	}
	
	public Bar() 
	{
	}

	public static ArrayList<Bar> getBarList(int intSize)
	{
		ArrayList<Bar> list = new ArrayList<Bar>();
		
		for (int i = 0; i < intSize; i++) 
		{
			
			String strRow = "str_" + new Integer(i).toString();
			Bar item = new Bar(
					strRow,
					i,
					i+1,
					DateTime.now());
			item.setHidden(new Integer(i).toString() + "_hidden");
			item.m_list = new ArrayList<String>();
			item.m_list.add(strRow + "_bar_a");
			item.m_list.add(strRow + "_bar_b");
			list.add(item);
		}
		
		return list;
	}
	
	@Override
	public void close() 
	{
		
		if(m_blnClosed)
		{
			return;
		}
		
		super.close();
		m_blnClosed = true;
		m_dateFoo = null;
	}
}