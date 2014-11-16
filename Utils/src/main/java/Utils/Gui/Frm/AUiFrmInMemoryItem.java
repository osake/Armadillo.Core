package Utils.Gui.Frm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import Armadillo.Analytics.TextMining.DataWrapper;
import Armadillo.Analytics.TextMining.Searcher;
import Armadillo.Core.Logger;
import Armadillo.Core.Reflection.ReflectionCache;
import Armadillo.Core.Reflection.Reflector;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.UI.FrmItem;

public abstract class AUiFrmInMemoryItem extends AUiFrmItem
{
	private List<FrmItem> m_frmItemsList;
	private HashMap<String, FrmItem> m_frmItemsMap;
	private HashMap<String, Object> m_objectsMap;
	
	protected abstract HashMap<String, Object> generateObjMap();
	
	@Override
	public int getSize()
	{
		try
		{
			List<FrmItem> frmItemsList = getFrmItemsList();
			if(frmItemsList == null)
			{
				return 0;
			}
			return frmItemsList.size();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return 0;
	}
	
	@Override
	public boolean save(Object callingBean)
	{
		try
		{
			List<FrmItem> items = getFrmItemsList();
			
			FrmItem selectedItem = items.get(getIndex());
			
			boolean blnSuccess = true;
			
			boolean blnIsReplacement = items.contains(selectedItem);
			String strkey0 = "";
			if(blnIsReplacement)
			{
				strkey0 = selectedItem.getKey();
			}
	
			if(blnSuccess)
			{
				if(!blnIsReplacement)
				{
					//
					// this is a new item
					//
					Object obj = createNewInstanceItem();
					selectedItem = new FrmItem();
					selectedItem.setObj(obj);
					String strSyntheticKey = getSyntheticKey0(strkey0, "");
					getFrmItemsMap().put(strSyntheticKey, selectedItem);
					items.add(selectedItem);
				}
				else
				{
					//
					// set values to current object
					//
					setPropertyValues(items.get(getIndex()).getObj());
				}
			}
			return true;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return false;
	}
	
	@Override
	public FrmItem getCurrFrmItem()
	{
		try
		{
			if(m_frmItemsList == null ||
				getIndex() >= m_frmItemsList.size() ||
				getIndex() < 0)
			{
				return null;
			}
			return m_frmItemsList.get(getIndex());
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
	
	@Override
	public void moveToIndex(int intIndex)
	{
		try
		{
			List<FrmItem> frmItemsList = getFrmItemsList();
			
			if(frmItemsList == null)
			{
				return;
			}
			FrmItem frmItem = frmItemsList.get(intIndex);
	    	publishObject(frmItem);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
    public HashMap<String, Object> getObjectsMap() 
	{
    	try
    	{
			if(m_objectsMap == null)
			{
				synchronized(m_lockObj)
				{
					if(m_objectsMap == null)
					{
						m_objectsMap = generateObjMap();
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_objectsMap;
	}
	
    @Override
	protected Searcher generateSearcher() 
	{
		try
		{
			HashMap<String, Object> objectsMap = getObjectsMap();
			
			if(objectsMap == null ||
			   objectsMap.size() == 0)
			{
				return null;
			}
			List<String> keys = getKeys();
			
			DataWrapper dataWrapper = new DataWrapper(keys);
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
			HashMap<String, Object> objectsMap = getObjectsMap();
			
			if(objectsMap == null || 
			   objectsMap.size() == 0)
			{
				return new ArrayList<String>();
			}
			
			List<String> keys = new ArrayList<String>();
			
			for(Object obj : objectsMap.values())
			{
				String strKey = getObjKey(obj);
				if(!StringHelper.IsNullOrEmpty(strKey))
				{
					keys.add(strKey);
				}
			}
			return keys;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ArrayList<String>();
	}

	@Override
	public void moveToNext()
	{
		try
		{
			List<FrmItem> items = getFrmItemsList();
			
			if(items == null || items.size() == 0)
			{
				return;
			}
			
			setIndex(getIndex() + 1);
			if(getIndex() >= items.size())
			{
				setIndex(items.size() - 1);
				return;
			}
			FrmItem frmItem = items.get(getIndex());
			publishObject(frmItem);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	@Override
	public void moveToPrev()
	{
		try
		{
			List<FrmItem> items = getFrmItemsList();
			
			if(items == null || items.size() == 0)
			{
				return;
			}
			
			setIndex(getIndex() - 1);
			if(getIndex() < 0)
			{
				setIndex(0);
				return;
			}
			FrmItem frmItem = items.get(getIndex());
			publishObject(frmItem);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	@Override
	public void moveToFirst()
	{
		try
		{
			List<FrmItem> items = getFrmItemsList();
			
			if(items == null || items.size() == 0)
			{
				return;
			}
			
			setIndex(0);
			FrmItem frmItem = items.get(getIndex());
			publishObject(frmItem);
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
			List<FrmItem> items = getFrmItemsList();
			
			if(items == null || items.size() == 0)
			{
				return;
			}
			
			setIndex(items.size() - 1);
			FrmItem frmItem = items.get(getIndex());
			publishObject(frmItem);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
    private void generateItems()
    {
    	try
    	{
    		HashMap<String, Object> objectsMap = getObjectsMap();
    		if(objectsMap == null || 
    				objectsMap.size() == 0)
    		{
    			return;
    		}
    		
	    	m_frmItemsList = new ArrayList<FrmItem>();
	    	m_frmItemsMap = new HashMap<String, FrmItem>();
	    	
	    	for(Entry<String, Object> kvp : objectsMap.entrySet())
	    	{
	    		Object obj = kvp.getValue();
	    		FrmItem currFrmItem = new FrmItem();
	    		//String strVal = getObjVal(obj);
	    		String strKey = kvp.getKey();
	    		currFrmItem.setObj(obj);
	    		currFrmItem.setKey(strKey);
	    		m_frmItemsMap.put(strKey, currFrmItem);
	    		m_frmItemsList.add(currFrmItem);
	    	}
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

	public List<FrmItem> getFrmItemsList() 
	{
		try
		{
			if(m_frmItemsList == null)
			{
				synchronized(m_lockObj)
				{
					if(m_frmItemsList == null)
					{
						generateItems();
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_frmItemsList;
	}

	public HashMap<String, FrmItem> getFrmItemsMap() 
	{
		try
		{
			if(m_frmItemsMap == null)
			{
				synchronized(m_lockObj)
				{
					if(m_frmItemsMap == null)
					{
						generateItems();
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_frmItemsMap;
	}
    
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
						HashMap<String, Object> objectsMap = getObjectsMap();
						if(objectsMap == null)
						{
							return null;
						}
				    	m_reflector = ReflectionCache.getReflector(
				    			objectsMap.entrySet().iterator().next().getValue().getClass());
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
	
	private String getSyntheticKey0(String strKey, String strVal) 
	{
		return strVal + "|" + strKey;
	}
	
	@Override
	public boolean delete(Object callingBean)
	{
		try
		{
			List<FrmItem> items = getFrmItemsList();
			HashMap<String, FrmItem> frmItemsMap = getFrmItemsMap();
				FrmItem obj = items.get(getIndex());
				items.remove(obj);
				frmItemsMap.remove(obj.getKey());
			return true;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return false;
	}
}
