package Armadillo.Core.UI;

import java.util.List;

import Armadillo.Core.Logger;

public abstract class AComboList 
{
	private List<String> m_allItems;
	private Object m_lockObject = new Object();
	
	protected abstract List<String> generateComboItems();
	
	public List<String> getComboItemsList()
	{
		try
		{
			if(m_allItems == null)
			{
				synchronized(m_lockObject)
				{
					if(m_allItems == null)
					{
						m_allItems = generateComboItems();
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_allItems;
	}
}
