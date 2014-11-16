package Web.Dashboard;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.faces.context.FacesContext;

import org.primefaces.component.autocomplete.AutoComplete;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.SelectEvent;

import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.Reflection.ReflectionCache;
import Armadillo.Core.Reflection.Reflector;
import Armadillo.Core.UI.ADynamicBean;
import Armadillo.Core.UI.AUiWorker;
import Armadillo.Core.UI.ColumnModel;
import Armadillo.Core.UI.ImageWrapper;
import Armadillo.Core.UI.TableRow;
import  Utils.Gui.AUiItem;
import  Utils.Gui.AUiPhotoAlbumItem;
import  Utils.Gui.AUiTableItem;
import Web.Base.WebHelper;
import Web.Chart.AUiChartItem;
import Web.Chart.ChartHelper;

public abstract class AFrontEndBean extends ADynamicBean  
{
	protected static Reflector m_tableRowReflector;
	protected DynamicGuiInstanceWrapper m_dynamicGuiInstanceWrapper;
	private String m_strSearchValue;
	protected Object m_pollLock = new Object();
	protected Object m_nodeSelectLock = new Object();
	protected TableRow m_selectedTableRow;
	protected Hashtable<String, DynamicGuiInstanceWrapper> m_dynamicGuiInstancesMap;
	protected ArrayList<DynamicGuiInstanceWrapper> m_dynamicGuiInstancesList; 
	protected String m_strGuiItemName;

	static 
	{
		try
		{
			m_tableRowReflector = ReflectionCache.getReflector(TableRow.class);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public AFrontEndBean()
	{
		m_dynamicGuiInstancesMap = new Hashtable<String, DynamicGuiInstanceWrapper>();
		m_dynamicGuiInstancesList = new ArrayList<DynamicGuiInstanceWrapper>();
		m_strSearchValue = "";
	}
	
	public TableRow getSelectedTableRow() 
	{
		try 
		{
			if (m_selectedTableRow == null) 
			{
				m_selectedTableRow = new TableRow();
				if(m_dynamicGuiInstanceWrapper != null)
				{
					AUiTableItem tableItem = m_dynamicGuiInstanceWrapper.getUiTableItem();
					Reflector reflector = tableItem.getReflector();
					if(reflector != null)
					{
						Reflector rowReflector = ReflectionCache.getReflector(TableRow.class);
						String[] strRowCols = rowReflector.getColNames();
						Type[] types = reflector.getColTypes();
						for (int i = 0; i < types.length; i++) 
						{
							if(types[i] == ImageWrapper.class)
							{
								rowReflector.SetPropertyValue(
										m_selectedTableRow, 
										strRowCols[i+1], 
										WebHelper.m_defaultContent); 
							}
						}
					}
				}
				
			}
			return m_selectedTableRow;
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return null;
	}

	public void setSelectedTableRow(TableRow selectedTableRow) 
	{
		try 
		{
			if (m_dynamicGuiInstanceWrapper == null) 
			{
				return;
			}

			List<ColumnModel> columns = m_dynamicGuiInstanceWrapper.getUiTableItem().getColumns();
			if (columns != null && selectedTableRow != null) 
			{
				for (ColumnModel col : columns) 
				{
					Object strVal = m_tableRowReflector.getPropValue(
							selectedTableRow, col.getProperty());
					col.setValue(strVal);
				}
			}

			m_selectedTableRow = selectedTableRow;
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}	
			
	protected void createDynamicGuiInstance(
			String strTabName, 
			AUiItem uiItem) 
	{
		try
		{
			int intTabIndex = m_dynamicGuiInstancesMap.size();
			m_dynamicGuiInstanceWrapper = WebHelper.createDynamicGuiInstanceWrapper(
					strTabName, 
					uiItem, 
					intTabIndex);
			m_dynamicGuiInstanceWrapper.setUiItem(uiItem);
			m_dynamicGuiInstancesMap.put(strTabName, m_dynamicGuiInstanceWrapper);
			m_dynamicGuiInstancesList.add(m_dynamicGuiInstanceWrapper);
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
	
	protected void refreshComponent() 
	{
		try 
		{
			//
			// This is an alternative to
			// requestContext.getCurrentInstance().
			// We want this to be non null, so that we could update from outside the so called
			// a JSF lifecycle processing.
			//
			RequestContext requestContext = RequestContext.getCurrentInstance();
			requestContext.update("centerForm:tab");
			requestContext.update("westForm:tree");
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}

	public static RequestContext getCurrentInstance()
	{
		 return (RequestContext)FacesContext.getCurrentInstance().getAttributes().get("primefaces.REQUEST_CONTEXT");
	} 
	
	protected void enqueueDataLoad(final AUiItem uiItem) 
	{
		try
		{
			//
			// then load data in alternate thread
			//
			AUiWorker uiWorker = new AUiWorker() 
			{
				@Override
				public void Work() 
				{
					try
					{
						if(AUiChartItem.class.isAssignableFrom(uiItem.getClass()))
						{
							ChartHelper.loadChart(uiItem);
						}
						else if(AUiPhotoAlbumItem.class.isAssignableFrom(uiItem.getClass()))
						{
							((AUiPhotoAlbumItem)uiItem).getFileList();
						}
						else
						{
							((AUiTableItem)uiItem).getTableRows();
						}
					}
					catch(Exception ex)
					{
						Logger.log(ex);
					}
				}
			};
			
			uiWorker.Work();
			uiWorker.close();
			//uiWorker.setBusyNotification(uiItem.getBusyNotification());
			//UiHelper.enqueueGuiTask(uiWorker).waitTask();
			refreshOnTaskDone(getFrmName());
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
	
	public abstract String getFrmName();

	public void refreshOnTaskDone(String strFrmName) 
	{
		try
		{
			reloadPage(FacesContext.getCurrentInstance(), strFrmName);
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
	
	
	public void reloadPage(
			FacesContext facesContext,
			String strFrmName) 
	{
		try 
		{
			//
			// TODO HACK!!, this is used only to refresh the data!
			//
			facesContext.getExternalContext().redirect(strFrmName + ".jsf");
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
	
	public abstract void onNodeSelect(NodeSelectEvent event); 
	
    public void onNodeCollapse(NodeCollapseEvent event) 
    {
    	try
    	{
	        if (event != null && event.getTreeNode() != null) 
	        {
	           event.getTreeNode().setExpanded(false);
	           refreshComponent();
	        }
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
     }  

    
    public void onNodeExpand(NodeExpandEvent event) 
    {
    	try
    	{
	        if (event != null && event.getTreeNode() != null) 
	        {
	           event.getTreeNode().setExpanded(true);
	           refreshComponent();
	        }
		}
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
     }
    
	public List<TableRow> getTableRows() 
	{
		try 
		{
			if (m_dynamicGuiInstanceWrapper == null) 
			{
				return null;
			}
			AUiTableItem uiItem = m_dynamicGuiInstanceWrapper.getUiTableItem();
			if (uiItem == null) 
			{
				return null;
			}
			List<TableRow> tableRows = (List<TableRow>) uiItem.getTableRows();
			return tableRows;
		}
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return null;
	}

	public void setTableRows(Collection<TableRow> tableRows) 
	{
		try 
		{
			if (m_dynamicGuiInstanceWrapper != null) 
			{
				m_dynamicGuiInstanceWrapper.setItemsList((List<TableRow>) tableRows);
			}
			m_dynamicGuiInstanceWrapper.getUiTableItem().setTableRows(tableRows);
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
	

	public Collection<TableRow> getFilteredTableRows() 
	{
		try 
		{
			if (m_dynamicGuiInstanceWrapper == null) 
			{
				return null;
			}
			return m_dynamicGuiInstanceWrapper.getUiTableItem().getFilteredTableRows();
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return null;
	}

	public void setFilteredTableRows(Collection<TableRow> filteredTableRows) 
	{
		try 
		{
			if (m_dynamicGuiInstanceWrapper == null) 
			{
				return;
			}
			m_dynamicGuiInstanceWrapper.getUiTableItem().setFilteredTableRows(filteredTableRows);
			// m_filteredTableRows = filteredTableRows;
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
	
	public boolean existsDynamicGuiInstance(String strTabName) 
	{
		try
		{
			return m_dynamicGuiInstancesMap.containsKey(strTabName);
		}
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return false;
	}
	
	public List<String> search(String strQuery) 
	{
		try 
		{
			if (m_dynamicGuiInstanceWrapper == null) 
			{
				return new ArrayList<String>();
			}
			List<String> queryResults = m_dynamicGuiInstanceWrapper.getUiItem().search(
					strQuery);
			if (queryResults == null || queryResults.size() == 0) 
			{
				return m_dynamicGuiInstanceWrapper.getUiItem().getKeys();
			}

			return queryResults;
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return new ArrayList<String>();
	}

	public void onComboSelect(SelectEvent event) 
	{
		try 
		{
			if (m_dynamicGuiInstanceWrapper == null) 
			{
				return;
			}

			Object item = event.getObject();

			if (item == null) 
			{
				return;
			}
			AutoComplete autoCompleteText = (AutoComplete) WebHelper
					.findUiComponent("searchCombo");
			String strItem = (String) item;
			autoCompleteText.setValue(strItem);
			List<String> keys = m_dynamicGuiInstanceWrapper.getUiItem().getKeys();
			int intIndex = keys.indexOf(strItem);
			ConcurrentMap<String, TableRow> rowsMap = m_dynamicGuiInstanceWrapper.getUiTableItem()
					.getRowsMap();

			if (rowsMap.containsKey(strItem)) 
			{
				// List<TableRow> tableRowsList = new ArrayList<TableRow>(
				// m_tabInstance.getUiItem().getTableRows());

				m_selectedTableRow = rowsMap.get(strItem);

				DataTable dataTable = ((DataTable) m_dynamicGuiInstanceWrapper.getComponentInTab().getChildren()
						.get(0));

				//
				// remove any filters
				//
				Collection<TableRow> filteredTableRows = m_dynamicGuiInstanceWrapper
						.getUiTableItem().getFilteredTableRows();
				if (filteredTableRows != null) {
					filteredTableRows.clear();
					filteredTableRows = null;
				}
				int intSelectedrowIndex = dataTable.getRowIndex();
				Console.writeLine(intSelectedrowIndex + "");

				dataTable.setFirst(intIndex);
			}
			//
			// set value to combo
			//
			m_strSearchValue = strItem;
			synchronized(m_pollLock)
			{
				reloadPage(FacesContext.getCurrentInstance(), getFrmName());
			}
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
	
	public String getSearchValue() 
	{
		return m_strSearchValue;
	}

	public void setSearchValue(String strSearchValue) 
	{
		m_strSearchValue = strSearchValue;
	}
	

	public void onSelectRow(SelectEvent event) 
	{
		System.out.println("selectedRow = " + m_selectedTableRow);
		m_selectedTableRow = (TableRow) event.getObject();
	}	
}
