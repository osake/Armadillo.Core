package Web.InputData;

import java.util.Hashtable;
import java.util.Map;

import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Core.Cache.SqliteCacheFullSchema;
import  Utils.Gui.Frm.AUiFrmItem;

public class InputDataCacheHelper 
{
	private static SqliteCacheFullSchema<ObjectWrapper> m_cache;

	static 
	{
		try
		{
			m_cache = new SqliteCacheFullSchema<ObjectWrapper>(
					ObjectWrapper.class);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public static void cacheParams(
			Map<String, String> map, 
			AUiFrmItem uiFrmItem)
	{
		try
		{
			String strKey = uiFrmItem.toString();
			if(m_cache.containsKey(strKey))
			{
				m_cache.delete(strKey);
			}
			m_cache.insert(strKey, new ObjectWrapper(map));
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, String> getCacheParams(
			AUiFrmItem uiFrmItem)
	{
		try
		{
			String strKey = uiFrmItem.toString();
			if(m_cache.containsKey(strKey))
			{
				ObjectWrapper[] items = m_cache.loadDataFromKey(strKey);
				if(items != null && items.length > 0)
				{
					Object obj = items[0].m_obj;
					if(obj != null)
					{
						return (Map<String, String>) obj;
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new Hashtable<String, String>();
	}
}
