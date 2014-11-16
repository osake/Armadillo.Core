package Web.Dashboard;

import java.util.List;
import java.util.Map;

import javax.faces.component.html.HtmlPanelGroup;

import org.primefaces.component.column.Column;

import Armadillo.Core.Logger;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.UI.ColumnModel;
import Armadillo.Core.UI.TableRow;
import  Utils.Gui.AUiPhotoAlbumItem;
import  Utils.Gui.AUiItem;
import  Utils.Gui.AUiTableItem;

public class DynamicGuiInstanceWrapper 
{
    private String m_strTitle;
    private String m_strContent;
    private HtmlPanelGroup m_tableInTab;
    private int m_intTabIndex;
	private AUiItem m_uiItem;
	private String m_strTabName;
	private Map<String, ColumnModel> m_colMap;
	private Map<String, Column> m_pfColMap;
	
	public void setItemsList(List<TableRow> filteredUsers) 
	{
	}
	
	public int getTabIndex() 
	{
		return m_intTabIndex;
	}
	
	public void setTabIndex(int intTabIndex) 
	{
		m_intTabIndex = intTabIndex;
	}

	public void setUiItem(AUiItem uiItem) 
	{
		m_uiItem = uiItem;
	}
	
	public AUiItem getUiItem() 
	{
		return m_uiItem;
	}

	public AUiTableItem getUiTableItem() 
	{
		if(m_uiItem instanceof AUiTableItem)
		{		
			return (AUiTableItem)m_uiItem;
		}
		return null;
	}
	
//	public void setFilteredItemsList(Collection<TableRow> filteredTableRows) 
//	{
//		m_filteredTableRows = filteredTableRows;
//	}
//	
//	public Collection<TableRow> getFilteredItemsList() 
//	{
//		return m_filteredTableRows;
//	}

	public void setTabName(String strTabName) 
	{
		m_strTabName = strTabName;
	}
	
	public String getTabName() 
	{
		return m_strTabName;
	}
	
	public Map<String, ColumnModel> getColMap() 
	{
		return m_colMap;
	}
	
	public void setColMap(Map<String, ColumnModel> colMap) 
	{
		m_colMap = colMap;
	}
	
   public String getTitle() 
    {
        return m_strTitle;
    }

    public String getContent() 
    {
        return m_strContent;
    }

	public HtmlPanelGroup getComponentInTab() 
	{
		return m_tableInTab;
	}

	public void setComponentInTab(HtmlPanelGroup tableInTab) 
	{
		m_tableInTab = tableInTab;
	}

	public void setTitle(String strTitle) 
	{
		try
		{
			if(StringHelper.IsNullOrEmpty(strTitle))
			{
				return;
			}
			m_strTitle = strTitle;
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}

	public void setContent(String strContent) 
	{
		m_strContent = strContent;
	}

	public void setPfColMap(Map<String, Column> pfColMap) 
	{
		m_pfColMap = pfColMap;
	}
	
	public Map<String, Column> getPfColMap()
	{
		return m_pfColMap;
	}

	public AUiPhotoAlbumItem getPhotoAlbumUiItem() 
	{
		try
		{
			AUiItem uiItem = getUiItem();
			if(uiItem == null)
			{
				return null;
			}
			if(uiItem instanceof AUiPhotoAlbumItem)
			{
				return (AUiPhotoAlbumItem)uiItem;
			}
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return null;
	}
}
