package Web.Dashboard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

import Armadillo.Communication.Impl.SimpleUiSocket;
import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.UI.ColumnModel;
import Armadillo.Core.UI.ImageWrapper;
import  Utils.Gui.AUiPhotoAlbumItem;
import  Utils.Gui.AUiItem;
import  Utils.Gui.AUiParam;
import  Utils.Gui.AUiTableItem;
import  Utils.Gui.Frm.AUiFrmItem;
import Web.Base.LiveGuiPublisher;
import Web.Base.WebHelper;
import Web.Chart.AUiChartItem;
import Web.Chart.ChartHelper;
import Web.Frm.AFrmBean;
import Web.InputData.AInputDataDialogBean;
import Web.Table.MyTableHelper;

public abstract class ADashboardBean extends AFrontEndBean 
{
	private boolean m_blnHasChaged;
	private int m_tabIndex;
	private Object m_tabChangeLock = new Object();

	public ADashboardBean() 
	{
		try 
		{
			LiveGuiPublisher.initialize();
			String strMessage = "Loaded bean [" + getClass().getName() + "]";
			Console.writeLine(strMessage);
			Logger.log(strMessage);
			SimpleUiSocket.Initialize();			
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}

	@Override
	public String getFrmName() 
	{
		return "Dashboard";
	}
	

	public int getTabIndex() 
	{
		return m_tabIndex;
	}
	
	protected void selectTab(String strTabName) 
	{
		try 
		{
			if (!existsDynamicGuiInstance(strTabName)) 
			{
				return;
			}

			m_dynamicGuiInstanceWrapper = m_dynamicGuiInstancesMap.get(strTabName);
			m_tabIndex = m_dynamicGuiInstanceWrapper.getTabIndex();
			if (LiveGuiPublisher.getOwnInstance().getGuiItems()
					.containsKey(strTabName)) 
			{
				AUiItem uiFrmItem = LiveGuiPublisher
						.getOwnInstance().getGuiItems().get(strTabName);
				m_dynamicGuiInstanceWrapper.setUiItem(uiFrmItem);
			}
		}
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
	
	@Override
	public void reloadPage(FacesContext facesContext, String strFrmName) 
	{
		try 
		{
			if(m_dynamicGuiInstanceWrapper == null &&
					!StringHelper.IsNullOrEmpty(m_strGuiItemName) &&
					m_dynamicGuiInstancesMap.containsKey(m_strGuiItemName))
			{
				m_dynamicGuiInstanceWrapper = m_dynamicGuiInstancesMap.get(m_strGuiItemName);
			}
			super.reloadPage(facesContext, strFrmName);
		}
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
	
	@Override
	public AUiItem getUiItem() 
	{
		try 
		{
			if (m_dynamicGuiInstanceWrapper == null) 
			{
				return null;
			}
			if(StringHelper.IsNullOrEmpty(m_strGuiItemName))
			{
				m_strGuiItemName = m_dynamicGuiInstanceWrapper.getTabName();
			}
			if(!m_strGuiItemName.equals(m_dynamicGuiInstanceWrapper.getTabName()))
			{
				selectTabInstance(m_strGuiItemName);
			}
			return m_dynamicGuiInstanceWrapper.getUiItem();
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return null;
	}
	
	@Override
	public void onNodeSelect(NodeSelectEvent event) 
	{
		try 
		{
			synchronized(m_nodeSelectLock)
			{
				TreeNode treeNode = event.getTreeNode();
				if(treeNode == null)
				{
					return;
				}
				if (treeNode.getChildren() == null
					|| treeNode.getChildren().size() == 0) 
				{
					m_strGuiItemName = WebHelper.getGuiItemName(treeNode);
					if (!existsDynamicGuiInstance(m_strGuiItemName)) 
					{
						//
						// generate a new tab
						//
						addNewTab(m_strGuiItemName);
						selectTabInstance(m_strGuiItemName);
						selectTab(m_strGuiItemName);
						return;
					}
					else 
					{
						if(isTableTabInstance(m_strGuiItemName))
						{
							selectTabInstance(m_strGuiItemName);
							AUiTableItem uiItem = m_dynamicGuiInstanceWrapper.getUiTableItem();
							AUiParam uiParams = uiItem.getParams();
							if (uiParams != null) 
							{
								//
								// load params
								//
								AInputDataDialogBean inputDataDialogBean = WebHelper.findBean("inputDataDialogBean");
								inputDataDialogBean.loadUiItem(this, uiParams, uiItem);
								selectTab(m_strGuiItemName);
								return;
							}
							
							selectTab(m_strGuiItemName);
							if (uiParams == null) 
							{
								synchronized(m_pollLock)
								{
									reloadPage(FacesContext.getCurrentInstance(),
											getFrmName());
								}
							}
						}
					}
				}
				//
				// this should expand the panel in case of large descriptions
				//
				// TODO find a better solution for this. It does not work
				// RequestContext.getCurrentInstance().update(
				// "westForm:tree");
				// RequestContext.getCurrentInstance().update(
				// "westForm");
				// RequestContext.getCurrentInstance().update(
				// "centerForm");
			}
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		} 
	}

	public void selectedTab(Object selection) 
	{
		System.out.println(selection);
	}

	private void addNewTab(String strTabName) 
	{
		try 
		{
			if (!LiveGuiPublisher.getOwnInstance().getGuiItems()
					.containsKey(strTabName)) 
			{
				return;
			}

			final AUiItem uiItem = LiveGuiPublisher.getOwnInstance().getGuiItems()
					.get(strTabName);

			if (uiItem == null) 
			{
				return;
			}
			
			// TODO, improve this logic, it is messy
			Class<?> classObj = uiItem.getClass();
			
			if (!AUiParam.class.isAssignableFrom(classObj)) 
			{
				if (AUiTableItem.class.isAssignableFrom(classObj) ||
						AUiPhotoAlbumItem.class.isAssignableFrom(uiItem.getClass())) 
				{
					AUiParam uiParams = uiItem.getParams();
					//
					// first generate ui component in main thread
					//
					if(!createTabComponentsGroup(strTabName, uiItem))
					{
						return;
					}
					if (uiParams != null) 
					{
						//
						// load params, the queue step will be done from the params
						//
						AInputDataDialogBean inputDataDialogBean = WebHelper
								.findBean("inputDataDialogBean");
						inputDataDialogBean.loadUiItem(this, uiParams, uiItem);
					} 
					else 
					{
						enqueueDataLoad(uiItem);
					}
				} 
				else 
				{
					//
					// load form
					//
					AFrmBean frmBean = (AFrmBean) WebHelper.findBean("frmBean");
					frmBean.loadUiItem((AUiFrmItem) uiItem);
				}
			}
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		finally
		{
			selectTabInstance(strTabName);
		}
	}

	@Override
	public void refreshOnTaskDone(String strFrmName) 
	{
		try
		{
			super.refreshOnTaskDone(strFrmName);
			selectTab(m_strGuiItemName);
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}

	public HtmlPanelGroup getDataTableGroup() 
	{
		if(m_dynamicGuiInstanceWrapper == null)
		{
			return null;
		}
		
		return m_dynamicGuiInstanceWrapper.getComponentInTab();
	}
	
	public void setDataTableGroup(HtmlPanelGroup dataTableGroup) {
	}	

	public boolean createTabComponentsGroup(
			String strTabName,
			AUiItem uiItem) 
	{
		try 
		{
			if(uiItem == null || StringHelper.IsNullOrEmpty(strTabName))
			{
				return false;
			}
			
			createDynamicGuiInstance(strTabName, uiItem);
			UIComponent uiComponent;
			if(AUiChartItem.class.isAssignableFrom(uiItem.getClass()))
			{
				uiComponent = ChartHelper.loadChartUiComponent(
						strTabName, 
						uiItem,
						m_dynamicGuiInstanceWrapper);
			}
			else if(AUiPhotoAlbumItem.class.isAssignableFrom(uiItem.getClass()))
			{
				uiComponent = PhotoAlbumHelper.loadPhotoAlbumUiComponent(
						strTabName, 
						m_dynamicGuiInstanceWrapper);
			}
			else
			{
				uiComponent = MyTableHelper.loadTableUiComponent(
						strTabName, 
						m_dynamicGuiInstanceWrapper,
						true,
						"dashboardBean");
			}
			if(uiComponent != null)
			{
				WebHelper.loadPanelGroup(
						strTabName, 
						uiComponent,
						m_dynamicGuiInstanceWrapper);
				return true;
			}
		}
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return false;
	}

	public void onPoolListener() 
	{
		try 
		{
			if (m_dynamicGuiInstanceWrapper == null) 
			{
				return;
			}

			AUiItem uiItem = m_dynamicGuiInstanceWrapper.getUiItem();
			if (uiItem == null) 
			{
				return;
			}

			if (m_blnHasChaged || uiItem.getHasChanged()) 
			{
				m_blnHasChaged = false;
				uiItem.setHasChanged(false);
				refreshComponent();
			}
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
	
	public StreamedContent getFile() 
	{
		try
		{
			StreamedContent streamedContent = new StreamedContent()
			{
	
				public String getName() 
				{
					return DataExporterHelper.getExportFileName(m_strGuiItemName) + ".csv";
				}
	
				public InputStream getStream() 
				{
					InputStream is = new ByteArrayInputStream(
							DataExporterHelper.getOutputString(m_dynamicGuiInstanceWrapper).getBytes());
					return is;
				}
	
				public String getContentType() 
				{
					return "csv";
				}
	
				public String getContentEncoding() 
				{
					return "csv";
				}
			};
			
	        return streamedContent;
		}
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return null;
    }	
	
	
	 public List<String> getGalleriaFiles()
	 {
		 try
		 {
			if(m_dynamicGuiInstanceWrapper == null)
			{
				return new ArrayList<String>();
			}
			AUiPhotoAlbumItem photoAlbumItem = m_dynamicGuiInstanceWrapper.getPhotoAlbumUiItem();
			if(photoAlbumItem != null)
			{
				List<String> fileList = photoAlbumItem.getFileList();
				if(fileList != null)
				{
					return fileList;
				}
			}
	    }
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	 	return new ArrayList<String>();
	 }
	 
	public void setHasChaged(boolean b) 
	{
		m_blnHasChaged = b;
	}
	
	 public StreamedContent getDynamicImage() throws IOException 
	 {
		 try
		 {
			if(m_dynamicGuiInstanceWrapper == null)
			{
				return new DefaultStreamedContent();
			}
			
	    	FacesContext context = FacesContext.getCurrentInstance();
	    	String strId = context.getExternalContext().getRequestParameterMap().get("idTableImage");
	        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) 
	        {
	            // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
	            return new DefaultStreamedContent();
	        }
	        else 
	        {
	        	Object objVal = m_dynamicGuiInstanceWrapper.getColMap().get(strId).getValue();
	        	if(objVal != null)
	        	{
		        	byte[] res = ((ImageWrapper)objVal).getBytes();
		            return new DefaultStreamedContent(new ByteArrayInputStream(res));
	        	}
	        }
	    }
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new DefaultStreamedContent();
	 }
	 
		private void remove(DynamicGuiInstanceWrapper tab) 
		{
			try
			{
				if(tab == null)
				{
					return;
				}
				String strTabName = tab.getTabName();
				if(m_dynamicGuiInstancesMap.containsKey(strTabName))
				{
					DynamicGuiInstanceWrapper itemToRemove = m_dynamicGuiInstancesMap.get(strTabName);
					m_dynamicGuiInstancesMap.remove(strTabName);
					m_dynamicGuiInstancesList.remove(itemToRemove);
					m_tabIndex = 0;
					
					if(m_dynamicGuiInstancesList.size() > 0)
					{
						m_dynamicGuiInstanceWrapper = m_dynamicGuiInstancesList.get(0);
					}
					else
					{
						m_dynamicGuiInstanceWrapper = null;
					}
				}
				reloadPage(FacesContext.getCurrentInstance(), getFrmName());
			} 
			catch (Exception ex) 
			{
				Logger.log(ex);
			}
		}
		
		protected void selectTabInstance(String strTabName) 
		{
			try
			{
				if(m_dynamicGuiInstancesMap.containsKey(strTabName))
				{
					if(isTableTabInstance(strTabName))
					{
						m_dynamicGuiInstanceWrapper = m_dynamicGuiInstancesMap.get(strTabName);
					}
				}
			}
			catch (Exception ex) 
			{
				Logger.log(ex);
			} 
		}
		
		public void onCloseTab()
		{
			try
			{
				if(m_dynamicGuiInstanceWrapper != null)
				{
					remove(m_dynamicGuiInstanceWrapper);
				}
			} 
			catch (Exception ex) 
			{
				Logger.log(ex);
			}
		}
		
		protected boolean isTableTabInstance(String strTabName) 
		{
			try
			{
				if(m_dynamicGuiInstancesMap.containsKey(strTabName))
				{
					AUiItem uiItem = m_dynamicGuiInstancesMap.get(strTabName).getUiItem();
					if (uiItem != null && 
						AUiTableItem.class.isAssignableFrom(uiItem.getClass())) 
					{
						return true;
					}
				}
			}
			catch (Exception ex) 
			{
				Logger.log(ex);
			} 
			return false;
		}		
		
		public void setTabIndex(int intTabIndex) 
		{
			Console.writeLine(intTabIndex + "");
		}
		
		public List<DynamicGuiInstanceWrapper> getTabs() 
		{
			return m_dynamicGuiInstancesList;
		}

		public List<ColumnModel> getColumns() 
		{
			try 
			{
				if (m_dynamicGuiInstanceWrapper != null) 
				{
					AUiTableItem tableItem = m_dynamicGuiInstanceWrapper.getUiTableItem();
					if(tableItem != null)
					{
						return tableItem.getColumns();
					}
				}
			} 
			catch (Exception ex) 
			{
				Logger.log(ex);
			}
			return null;
		}

		public void setColumns(List<ColumnModel> columns) 
		{
			try 
			{
				if (m_dynamicGuiInstanceWrapper != null) 
				{
					m_dynamicGuiInstanceWrapper.getUiTableItem().setColumns(columns);
				}
			} 
			catch (Exception ex) 
			{
				Logger.log(ex);
			}
		}
		
		public void onTabChange(TabChangeEvent event) 
		{
			try 
			{
				synchronized(m_tabChangeLock)
				{
					String strTabName = ((DynamicGuiInstanceWrapper) event.getData()).getTitle();
					
					if(!StringHelper.IsNullOrEmpty(m_strGuiItemName) &&
							strTabName.equals(m_strGuiItemName))
					{
						//
						// tab already selected
						//
						return;
					}
					m_strGuiItemName = strTabName;
					((ATreeBean) WebHelper.findBean("treeBean")).selectNode(m_strGuiItemName);
					
					if(isTableTabInstance(m_strGuiItemName))
					{
						selectTab(m_strGuiItemName);
						synchronized(m_pollLock)
						{
							reloadPage(FacesContext.getCurrentInstance(), getFrmName());
						}
					}
				}
			} 
			catch (Exception ex) 
			{
				Logger.log(ex);
			}
		}		
	 
		
}
