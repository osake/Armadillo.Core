package Armadillo.Core;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

public class Verboser 
{
	private static DateTime m_prevTalk = DateTime.now();
	
	public static void Talk(String strMessage)
	{
		try
		{
			int intSeconds = Seconds.secondsBetween(m_prevTalk, DateTime.now()).getSeconds();
			if(intSeconds > 3)
			{
				Logger.log(strMessage);
				m_prevTalk = DateTime.now();
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
}
