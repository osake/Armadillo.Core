package Armadillo.Core.Concurrent;

import Armadillo.Core.Logger;

public class LockHelper 
{
	
//	public static ConcurrentHashMap<String, Object> m_map = 
//			new ConcurrentHashMap<String, Object>();
	private static Object m_lock = new Object();
	private static EfficientMemoryBuffer<String,Object> m_map;
	
	public static Object GetLockObject(String string) 
	{
		try
		{
			m_map = 
					new EfficientMemoryBuffer<String, Object>(500000);
			
			if(m_map.containsKey(string)) 
			{
				Object lockObj = m_map.get(string);
				if(lockObj != null){
					return lockObj;
				}
			}
			
			synchronized(m_lock)
			{
				
				if(m_map.containsKey(string))
				{
					Object lockObj = m_map.get(string);
					if(lockObj != null){
						return lockObj;
					}
				}
				Object newLock = new Object();
				m_map.put(string, newLock);
				return newLock;
			}
		}
		catch(Exception ex){
			Logger.log(ex);
		}
		return new Object();
	}
}
