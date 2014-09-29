package Armadillo.Core;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Date;

import org.joda.time.DateTime;

import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.Text.StringHelper;

public class Foo extends AFoo implements Closeable 
{

	private String m_stringHidden;
	public String m_string;
	public int m_j;
	public double m_d;
	public DateTime m_jodaTime;
	public ArrayList<String> m_list;
	private boolean m_blnClosed;
	
	private static Object m_lock = new Object();
	private static int m_counter;
	private static int m_intFooCounter = 0; 
	
	public Foo() {}
	
	public Foo(String string, int j, double d) {
		this(string, j,d,DateTime.now().toDate(),
				DateTime.now());
	}

	public Foo(String string, 
			int j, 
			double d,
			Date date,
			DateTime jodaDate) {
		m_string = string;
		m_j = j;
		m_d = d;
		m_dateCol = date;
		m_jodaTime = jodaDate;
	}

	
	public void setHidden(String str){
		m_stringHidden = str;
	}
	
	public String getHidden(){
		return m_stringHidden;
	}

	public static ArrayList<Foo> getFooList(
			Integer intSize){
		return getFooList(intSize,true);
	}
	
	public static ArrayList<Foo> getFooList(Integer intSize,
			Boolean blnDoWork){
		ArrayList<Foo> fooList = new ArrayList<Foo>();
		
		try{
			for (int i = 0; i < intSize; i++) {
				
				String strRow = "str_" + new Integer(i).toString();
				Foo foo;
				synchronized(m_lock){
					m_counter++;
					foo = new Foo(
							strRow,
							i,
							m_counter,
							DateTime.now().toDate(),
							DateTime.now());
				}
				foo.setHidden(new Integer(i).toString() + "_hidden");
				foo.m_list = new ArrayList<String>();
				foo.m_list.add(strRow + "_foo_a");
				foo.m_list.add(strRow + "_foo_b");
				fooList.add(foo);
				if(blnDoWork){
					Console.writeLine("Foo [" + i + "/" + intSize + "] is working...");
					if(m_intFooCounter % 800.0 == 0){
						Thread.sleep(20000);
					}
				}
			}
			
			m_intFooCounter++;
			
			return fooList;
		}
		catch(Exception ex){
			Logger.log(ex);
		}
		return new ArrayList<Foo>();
	}

	public boolean compare(Foo foo) {
		
		try
		{
			
			if(foo == null ||
					StringHelper.IsNullOrEmpty(m_string) ||
					StringHelper.IsNullOrEmpty(foo.m_string) ||
							m_jodaTime == null){
				
				return false;
			}
			
			boolean blnEquals = m_string.equals(foo.m_string) &&
				m_j == foo.m_j &&
				m_d == foo.m_d &&
			    m_dateCol.equals(foo.m_dateCol) &&
				m_jodaTime.equals(foo.m_jodaTime) &&
				m_list.get(0).equals(foo.m_list.get(0)) &&
				m_list.get(1).equals(foo.m_list.get(1));
		
			if(!blnEquals)
			{
				System.out.println("Not equals");
			}
			return blnEquals;
		}
		catch(Exception ex){
			Logger.log(ex);
		}
		
		return false;
	}

	public String getKey() {
		return "key_" + m_j;
	}

	public boolean compareSimple(Foo foo) {
		try{
			boolean blnEquals = 
					m_j == foo.m_j;
			
			return blnEquals;
		}
		catch(Exception ex){
			Logger.log(ex);
		}
		return false;
	}
	
	@Override
	public String toString(){
		return m_string;
	}

	@Override
	public void close() {
		
		if(m_blnClosed){
			return;
		}
		m_blnClosed = true;
		m_stringHidden = null;
		m_string = null;
		m_jodaTime = null;
		
		if(m_list != null){
			m_list.clear();
			m_list = null;
		}
	}
}
