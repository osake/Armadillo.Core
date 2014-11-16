package Web.Catalogue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseId;
import javax.imageio.ImageIO;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.TreeNode;

import Armadillo.Analytics.TextMining.Searcher;
import Armadillo.Core.Logger;
import Armadillo.Core.Reflection.ReflectionCache;
import Armadillo.Core.Reflection.Reflector;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.UI.ImageWrapper;
import Armadillo.Core.UI.TableRow;
import  Utils.Gui.AUiItem;
import  Utils.Gui.AUiTableItem;
import Web.Base.LiveGuiPublisher;
import Web.Base.WebHelper;
import Web.Dashboard.AFrontEndBean;
import Web.Dashboard.DynamicGuiInstanceWrapper;
import Web.Table.MyTableHelper;

@ManagedBean(name = "catalogueBean")
@SessionScoped
public abstract class ACatalogueBean extends AFrontEndBean
{
	private static byte[] m_defaultContent;
	
	static
	{
		try
		{
			BufferedImage image = ImageIO.read(new File("C:/HC.Java/workspace/HC.Products.Web.Gch/WebContent/TrashEmpty.png")); 
			ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			ImageIO.write(image, "png", baos); 
			byte[] bytes = baos.toByteArray();
			m_defaultContent = bytes;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public ACatalogueBean() 
	{
		m_dynamicGuiInstanceWrapper = new DynamicGuiInstanceWrapper();
		final List<TableRow> dummyTableRows = loadDummyTableRows();
		AUiItem uiItem = new AUiTableItem() 
		{
			
			@Override
			public String[] getReportTreeLabels() {
				return new String[] {
						"test",
						"test",
						"test"
				};
			}
			
			@Override
			public String getReportTitle() {
				return null;
			}
			
			@Override
			protected Class<?> getParamsClass() {
				return null;
			}
			
			@Override
			protected Searcher generateSearcher() {
				return null;
			}
			
			@Override
			protected List<String> generateKeys() 
			{
				return null;
			}

			@Override
			public List<TableRow> generateTableRows() 
			{
				return dummyTableRows;
			}
			
		};
		m_dynamicGuiInstanceWrapper.setUiItem(uiItem);
	}

	@Override
	public String getFrmName() 
	{
		return "Catalogue";
	}
	
	private static List<TableRow> loadDummyTableRows() 
	{
		try
		{
			List<TableRow> dummyTableRows = new ArrayList<TableRow>();
			CatalogueItem item = new CatalogueItem();
			item.Id = "None";
			item.Description = "Empty";
			Reflector reflector = ReflectionCache.getReflector(CatalogueItem.class); 
			dummyTableRows.add(MyTableHelper.generateTableRow(reflector, item, "None"));
			return dummyTableRows;
		}
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return null;
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
				if (treeNode.getChildren() == null
					|| treeNode.getChildren().size() == 0) 
				{
					m_strGuiItemName = WebHelper.getGuiItemName(treeNode);
					if (!existsDynamicGuiInstance(m_strGuiItemName)) 
					{
						AUiItem uiItem = CatalogueHelper.getGuiItems()
								.get(m_strGuiItemName);

						if (uiItem == null) 
						{
							return;
						}		
						
						createDynamicGuiInstance(m_strGuiItemName, uiItem);
						enqueueDataLoad(uiItem);
						
						String[] columnNames = ((AUiTableItem)m_dynamicGuiInstanceWrapper.getUiItem()).getFieldNames();
						Type[] columnTypes = ((AUiTableItem)m_dynamicGuiInstanceWrapper.getUiItem()).getFieldTypes();
						MyTableHelper.loadColumnItems(
								columnNames, 
								columnTypes,
								m_dynamicGuiInstanceWrapper);
					}
					selectGuiInstance(m_strGuiItemName);
					//reloadPage(FacesContext.getCurrentInstance(), getFrmName());
				}
			}
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
	
	protected void selectGuiInstance(String strTabName) 
	{
		try 
		{
			if (!existsDynamicGuiInstance(strTabName)) 
			{
				return;
			}

			m_dynamicGuiInstanceWrapper = m_dynamicGuiInstancesMap.get(strTabName);
		}
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
	
	 public Object getImage() 
	 {
		 try
		 {
	        FacesContext context = FacesContext.getCurrentInstance();

	        if (m_dynamicGuiInstanceWrapper == null ||
	        		context == null ||
	        		context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) 
	        {
	            // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
	            return new DefaultStreamedContent();
	        }
	        else 
	        {
	        	String strImageId = context.getExternalContext().getRequestParameterMap().get("itemId");
	        	AUiTableItem uiTableItem = m_dynamicGuiInstanceWrapper.getUiTableItem();
	        	ConcurrentMap<String, TableRow> rowsMap = uiTableItem.getRowsMap();
	        	if(rowsMap.containsKey(strImageId))
	        	{
	        		TableRow item = rowsMap.get(strImageId);
	        		Object obj = item.getCol3();
	        		if(obj != null &&
	        				obj instanceof ImageWrapper)
	        		{
		        		byte[] bytes = ((ImageWrapper)obj).getBytes();
		        		return new DefaultStreamedContent((new ByteArrayInputStream(bytes)));
	        		}
	        	}
	            return new DefaultStreamedContent(new ByteArrayInputStream(m_defaultContent));
	        }
	    }
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		 return null;
	 }
	
	
	public void btnClick(
			ActionEvent actionEvent) 
	{
		try
		{
			String strTabName = 				
					"Test|Foo|FooImageTable";
			AUiItem uiItem = LiveGuiPublisher.getOwnInstance().getGuiItems().get(strTabName);
			((AUiTableItem)uiItem).getTableRows();
			DynamicGuiInstanceWrapper dynamicGuiInstanceWrapper =
					WebHelper.createDynamicGuiInstanceWrapper(strTabName, uiItem);
			DataTable uiComponent = MyTableHelper.loadTableUiComponent(
					strTabName, 
					dynamicGuiInstanceWrapper,
					false,
					"catalogueBean");
			
//			HtmlPanelGroup panelGroup = WebHelper.loadPanelGroup(
//					strTabName, 
//					uiComponent,
//					dynamicGuiInstanceWrapper);
			uiComponent.setRendered(true);

			UIComponent frmComponent = WebHelper.findUiComponent("formMain");
			frmComponent.getChildren().add(uiComponent);
			m_dynamicGuiInstanceWrapper = dynamicGuiInstanceWrapper;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
}
