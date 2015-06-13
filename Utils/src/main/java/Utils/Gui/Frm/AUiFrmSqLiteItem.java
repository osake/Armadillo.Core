package Utils.Gui.Frm;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Armadillo.Analytics.TextMining.DataWrapper;
import Armadillo.Analytics.TextMining.Searcher;
import Armadillo.Core.Config;
import Armadillo.Core.Logger;
import Armadillo.Core.Cache.SqliteCacheFullSchema;
import Armadillo.Core.Reflection.ReflectionCache;
import Armadillo.Core.Reflection.Reflector;
import Armadillo.Core.UI.EnumActions;
import Armadillo.Core.UI.FrmItem;
import Utils.Gui.ACustomMenuItem;

public abstract class AUiFrmSqLiteItem extends AUiFrmItem
{
	private static final String DEFAULT_CACHE_LOCATION; 
	protected SqliteCacheFullSchema<?> m_db;
	protected String m_strDbFileName;
	private Object m_lockObject = new Object();
	private Class<?> m_classObj;
	
	static
	{
		DEFAULT_CACHE_LOCATION = Config.getConfig(Logger.class).getStr("DataPath");		
	}
	
	public AUiFrmSqLiteItem(Class<?> classObj)
	{
		try
		{
			m_classObj = classObj;
			m_strDbFileName = getDbFileName(classObj);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public static String getDbFileName(Class<?> classObj) 
	{
		try
		{
			String strClassName = Reflector.getClassName(classObj);
			String strDbFileName = DEFAULT_CACHE_LOCATION + "\\" +
					strClassName + ".db";
			return strDbFileName;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
	}
	
	protected abstract SqliteCacheFullSchema<?> generateDb();
	
	@Override
	public String getObjKey(Object obj) 
	{
		try
		{
			Reflector reflector = getReflector();
			return reflector.getStringRepresentation(obj);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
	}

	@Override
	public String getReportTitle() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected Class<?> getParamsClass() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ACustomMenuItem> getMenuItems() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public SqliteCacheFullSchema<?> getDb() 
	{
		try
		{
			if(m_db == null)
			{
				synchronized(m_lockObject)
				{
					if(m_db == null)
					{
						m_db = generateDb();
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_db;
	}
	
	@Override
	public HashMap<EnumActions, FrmAction> generateActionsMap() 
	{
		try
		{
			SqliteCacheFullSchema<?> db = getDb();
			if(db == null)
			{
				return new HashMap<EnumActions, FrmAction>();
			}
			HashMap<EnumActions, FrmAction> actionsMap = new HashMap<EnumActions, FrmAction>();
			actionsMap.put(EnumActions.Delete, new SqLiteDeleteAction<>(db));
			actionsMap.put(EnumActions.Save, new SqLiteSaveAction<>(db));
			
			return actionsMap;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new HashMap<EnumActions, FrmAction>();
	}

	@Override
	public void moveToNext() 
	{
		try
		{
			List<String> keys = getKeys();
			if(keys == null)
			{
				return;
			}
			
			int intIndex = getIndex() + 1;
			setIndex(intIndex);
			
			if(getIndex() >= keys.size())
			{
				setIndex(keys.size() - 1);
			}
			
			FrmItem currFrmItem = getCurrFrmItem();
			publishObject(currFrmItem);
			
			HashMap<EnumActions, FrmAction> actionsMap = getActionsMap();
			
			if(actionsMap == null)
			{
				return;
			}
			if(!actionsMap.containsKey(EnumActions.Next.toString()))
			{
				return;
			}
			FrmAction action = actionsMap.get(EnumActions.Next.toString());
			
			action.invokeAction(null, null);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	private FrmItem getFrmItem(String strKey)
	{
		try
		{
			Object rawItem = getRawObj(strKey);
			if(rawItem == null)
			{
				return null;
			}
			FrmItem currFrmItem = new FrmItem();
			currFrmItem.setObj(rawItem);
			currFrmItem.setKey(strKey);
			return currFrmItem;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}

	protected Object getRawObj(String strKey) 
	{
		try
		{
			SqliteCacheFullSchema<?> db = getDb();
			if(db == null)
			{
				return null;
			}
			
			Object itemsObj = db.loadDataFromKey(strKey);
			if(itemsObj == null || Array.getLength(itemsObj) == 0)
			{
				// HACK in order to load keys with apostrophe's
				itemsObj = db.loadDataFromKey(strKey.replace("'", "''"));
				if(itemsObj == null || Array.getLength(itemsObj) == 0)
				{
					return null;
				}
			}
			
			Object rawItem = Array.get(itemsObj, 0);
			return rawItem;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}

	@Override
	public int getSize() 
	{
		try
		{
			List<String> keys = getKeys();
			if(keys != null)
			{
				return keys.size();
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return 0;
	}

	@Override
	public Reflector getReflector() 
	{
		try
		{
			if(m_reflector == null)
			{
				synchronized(m_lockObj)
				{
					if(m_reflector == null)
					{
						if(m_classObj == null)
						{
							return null;
						}
						m_reflector = ReflectionCache.getReflector(m_classObj);
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_reflector;
	}

	@Override
	public void moveToIndex(int intIndex) 
	{
		try
		{
			List<String> keys = getKeys();
			if(keys == null || keys.size() == 0)
			{
				return;
			}
			if(intIndex < 0 || intIndex >= keys.size())
			{
				return;
			}
			setIndex(intIndex);
			
			FrmItem currFrmItem = getCurrFrmItem();
			publishObject(currFrmItem);
			
			HashMap<EnumActions, FrmAction> actionsMap = getActionsMap();
			
			if(actionsMap == null)
			{
				return;
			}
			if(!actionsMap.containsKey(EnumActions.GoToIndex.toString()))
			{
				return;
			}
			FrmAction action = actionsMap.get(EnumActions.GoToIndex.toString());
			
			action.invokeAction(null, null);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	@Override
	public boolean save(Object callingBean) 
	{
		try
		{
			// the sqlite save action does the job
			return true;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return false;
	}

	@Override
	public void moveToPrev() 
	{
		try
		{
			List<String> keys = getKeys();
			if(keys == null)
			{
				return;
			}
			
			setIndex(getIndex() - 1);
			if(getIndex() < 0)
			{
				setIndex(0);
				return;
			}
			
			FrmItem currFrmItem = getCurrFrmItem();
			publishObject(currFrmItem);
			
			HashMap<EnumActions, FrmAction> actionsMap = getActionsMap();
			
			if(actionsMap == null)
			{
				return;
			}
			if(!actionsMap.containsKey(EnumActions.Prev.toString()))
			{
				return;
			}
			FrmAction action = actionsMap.get(EnumActions.Prev.toString());
			
			action.invokeAction(null, null);
			
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	@Override
	public void moveToLast() 
	{
		try
		{
			List<String> keys = getKeys();
			if(keys == null || keys.size() == 0)
			{
				return;
			}
			setIndex(keys.size() - 1);
			FrmItem currFrmItem = getCurrFrmItem();
			publishObject(currFrmItem);
			
			HashMap<EnumActions, FrmAction> actionsMap = getActionsMap();
			
			if(actionsMap == null)
			{
				return;
			}
			if(!actionsMap.containsKey(EnumActions.MoveToLast.toString()))
			{
				return;
			}
			FrmAction action = actionsMap.get(EnumActions.MoveToLast.toString());
			
			action.invokeAction(null, null);
			
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	@Override
	public boolean delete(Object callingBean) 
	{
		try
		{
			// the sqlite delete action does the job
			return true;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return false;
	}

	@Override
	public void moveToFirst() 
	{
		try
		{
			setIndex(0);
			FrmItem currFrmItem = getCurrFrmItem();
			publishObject(currFrmItem);
			
			publishObject(currFrmItem);
			
			HashMap<EnumActions, FrmAction> actionsMap = getActionsMap();
			
			if(actionsMap == null)
			{
				return;
			}
			if(!actionsMap.containsKey(EnumActions.MoveToFirst.toString()))
			{
				return;
			}
			FrmAction action = actionsMap.get(EnumActions.MoveToFirst.toString());
			
			action.invokeAction(null, null);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	@Override
	public FrmItem getCurrFrmItem() 
	{
		try
		{
			List<String> keys = getKeys();
			if(keys == null || keys.size() == 0)
			{
				return null;
			}
			
			if(getIndex() >= keys.size())
			{
				setIndex(keys.size() - 1);
			}
			if(getIndex() < 0)
			{
				//
				// we are in a new item state
				//
				return null;
			}
			
			String strKey = keys.get(getIndex());
			FrmItem currFrmItem = getFrmItem(strKey);
			
			return currFrmItem;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		
		return null;
	}

	@Override
	protected Searcher generateSearcher() 
	{
		try
		{
			List<String> keys = getKeys();
			if(keys == null || keys.size() == 0)
			{
				return null;
			}
			DataWrapper dataWrapper = new DataWrapper(
					 new ArrayList<String>(keys)); // HACK: clone the list, otherwise the items would be added twice
			m_searcher = new Searcher(dataWrapper);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_searcher;
	}

	@Override
	protected List<String> generateKeys() 
	{
		try
		{
			SqliteCacheFullSchema<?> db = getDb();
			if(db == null)
			{
				return new ArrayList<String>();
			}
			List<String> keys = db.loadAllKeys();
			return keys;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		
		return new ArrayList<String>();
	}
	
	@Override
	public void onClose() 
	{
		m_keys = null;
	}
}
