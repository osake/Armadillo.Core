package Web.Catalogue;

import java.util.Map.Entry;

import Armadillo.Core.Logger;
import  Utils.Gui.AUiItem;
import Web.Dashboard.ATreeBean;

public abstract class ACatalogueTreeBean extends ATreeBean 
{
	@Override
	protected void loadTreeNodes() 
	{
		try
		{
			synchronized(m_lockObj)
			{
				for (Entry<String, AUiItem> kvp : 
					CatalogueHelper.getGuiItems().entrySet()) 
				{
					String strKey = kvp.getKey();
					addToLeafNodes(strKey);
				}
			}
		}
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
}
