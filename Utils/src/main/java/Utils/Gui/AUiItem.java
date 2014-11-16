package Utils.Gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Armadillo.Analytics.TextMining.MstDistanceObj;
import Armadillo.Analytics.TextMining.Searcher;
import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.Reflection.ReflectionCache;
import Armadillo.Core.Reflection.Reflector;
import Armadillo.Core.UI.IUiItem;
import Armadillo.Core.UI.UiHelper;

public abstract class AUiItem implements IUiItem
{
	private static final int QUERY_RESULTS_SIZE = 15;
	
	protected Object m_lockObj = new Object();
	private AUiParam m_params;
	protected Searcher m_searcher;
	protected List<String> m_keys;
	private Map<String, String> m_paramsMap;
	//private ABusyNotification m_busyNotification;
	protected boolean m_blnHasChanged;
	
	public abstract String getReportTitle();
	public abstract String[] getReportTreeLabels();
	protected abstract Class<?> getParamsClass();
	protected abstract Searcher generateSearcher();
	protected abstract List<String> generateKeys();
	
	public AUiItem()
	{
		generateParams();
	}
	
	protected void generateParams()
	{
		try
		{
			Class<?> cls = getParamsClass();
			
			if(cls == null)
			{
				return;
			}
			
			Reflector reflector = ReflectionCache.getReflector(cls);
			AUiParam paramInstance = (AUiParam)reflector.createInstance();
			m_params = paramInstance;
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}

	public String getTreeKey() 
	{
		try
		{
			String[] reportTreeLabels = getReportTreeLabels();
			
			String strLabel1 = "";
			String strLabel2 = "";
			String strLabel3 = getClass().getName();
			
			if(reportTreeLabels == null ||
					reportTreeLabels.length == 0)
			{
				strLabel1 = "Unknown";
				strLabel2 = "Unknown";
				strLabel3 = getClass().getName();
			}
			else
			{
				strLabel1 = reportTreeLabels[0];
				strLabel2 = reportTreeLabels.length > 1 ? 
						reportTreeLabels[1] :
							"Unknown";
				strLabel3 = reportTreeLabels.length > 2 ? 
						reportTreeLabels[2] :
							getClass().getName();
			}
			
			return UiHelper.getNodeKey(
					strLabel1, 
					strLabel2,
					strLabel3);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
	}
	
	public AUiParam getParams()
	{
		return m_params;
	}
	
	public List<String> search(String strQuery)
	{
		try
		{
			Searcher m_searcher = getSearcher();
			
			if(m_searcher == null)
			{
				return new ArrayList<String>(); 
			}
			List<String> keys = getKeys();
			if(keys == null)
			{
				return new ArrayList<String>();
			}
			
			List<MstDistanceObj> results = m_searcher.Search(strQuery);
			List<String> queryResults = new ArrayList<String>(); 
			if(results != null && results.size() > 0)
			{
				for(int i = 0; i < Math.min(results.size(), QUERY_RESULTS_SIZE); i++)
				{
					MstDistanceObj mstDistanceObj = results.get(i);
					int intPosition = mstDistanceObj.Y;
					if(intPosition >= 0 && intPosition < keys.size())
					{
						queryResults.add(keys.get(intPosition));
					}
				}
			}
			return queryResults;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ArrayList<String>(); 
	}
	
	public Searcher getSearcher()
	{
		try
		{
			if(m_searcher == null)
			{
				synchronized(m_lockObj)
				{
					if(m_searcher == null)
					{
						Console.WriteLine("Generating searcher...");
						m_searcher = generateSearcher();
						Console.WriteLine("FInish generating searcher");
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_searcher;
	}
	
	public List<String> getKeys()
	{
		try
		{
			if(m_keys == null)
			{
				synchronized(m_lockObj)
				{
					if(m_keys == null)
					{
						m_keys = generateKeys();
						getSearcher();
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_keys;
	}
	
	public void setParamsMap(Map<String, String> paramsMap) 
	{
		m_paramsMap = paramsMap;
	}
	
	public Map<String, String> getParamsMap() 
	{
		return m_paramsMap;
	}
	public void resetSearcher() 
	{
		m_searcher = null;
		m_keys = null;		
	}
	
//	public ABusyNotification getBusyNotification() 
//	{
//		return m_busyNotification;
//	}
//	
//	public void setBusyNotification(ABusyNotification busyNotification) 
//	{
//		m_busyNotification = busyNotification;
//	}
	
	public Object getLockObj()
	{
		return m_lockObj;
	}
	
	public boolean getHasChanged() 
	{
		return m_blnHasChanged;
	}
	
	public void setHasChanged(boolean blnHasChanged) 
	{
		m_blnHasChanged = blnHasChanged;
	}
}
