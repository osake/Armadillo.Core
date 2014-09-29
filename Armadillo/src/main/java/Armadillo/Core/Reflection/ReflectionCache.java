package Armadillo.Core.Reflection;

import java.util.concurrent.ConcurrentHashMap;

public class ReflectionCache 
{
	private static ConcurrentHashMap<String,Reflector> m_reflectorMap = new ConcurrentHashMap<String, Reflector>();
	private static Object m_lockObj = new Object();
	
	public static Reflector getReflector(Class<?> classObj)
	{
		String strClassName = classObj.getName();
		if(m_reflectorMap.containsKey(strClassName))
		{
			return m_reflectorMap.get(strClassName);
		}
		synchronized(m_lockObj)
		{
			if(m_reflectorMap.containsKey(strClassName))
			{
				return m_reflectorMap.get(strClassName);
			}
			Reflector reflector = new Reflector(classObj);
			m_reflectorMap.put(strClassName, reflector);
			return reflector;
		}
	}
}
