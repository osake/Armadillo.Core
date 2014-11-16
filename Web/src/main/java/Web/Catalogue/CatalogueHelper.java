package Web.Catalogue;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javassist.Modifier;
import Armadillo.Core.Logger;
import Armadillo.Core.Reflection.ClassHelper;
import Armadillo.Core.Reflection.ReflectionCache;
import  Utils.Gui.AUiItem;
import  Utils.Gui.AUiParam;

public class CatalogueHelper 
{
	private static ConcurrentHashMap<String,AUiItem> m_guiItems;
	
	static 
	{
		try
		{
			m_guiItems = 
					new ConcurrentHashMap<String,AUiItem>();
			loadUiItemsViaReflection();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	private static void loadUiItemsViaReflection()
	{
		try
		{
			//
			// find all reports in the framework
			//
			Set<Class<? extends AUiItem>> reports = 
					ClassHelper.getSubTypes(AUiItem.class);
			
			for(Class<? extends AUiItem> reportClass : reports)
			{
				if(reportClass.isInterface() ||
				   Modifier.isAbstract(reportClass.getModifiers()) ||
				   reportClass.getName().contains("$"))
				{
					continue;
				}
				if(!AUiParam.class.isAssignableFrom(reportClass) &&
					AUiCatalogueTableItem.class.isAssignableFrom(reportClass))
				{
					AUiItem report = 
							(AUiItem) ReflectionCache.getReflector(reportClass).createInstance();
					loadUiItem(report);
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public static void loadUiItem(
			AUiItem uiItem)
	{
		try
		{
			String strKey = uiItem.getTreeKey();
			if(!m_guiItems.containsKey(strKey))
			{
				m_guiItems.put(strKey, uiItem);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public static ConcurrentHashMap<String,AUiItem> getGuiItems() 
	{
		return m_guiItems;
	}
}
