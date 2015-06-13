package Web.Table;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.el.MethodExpression;
import javax.faces.component.html.HtmlOutputText;

//import org.primefaces.component.behavior.ajax.AjaxBehavior;
import org.primefaces.behavior.ajax.AjaxBehavior;

import org.primefaces.component.column.Column;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.graphicimage.GraphicImage;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;

import Armadillo.Core.Logger;
import Armadillo.Core.Reflection.ReflectionCache;
import Armadillo.Core.Reflection.Reflector;
import Armadillo.Core.UI.ColumnModel;
import Armadillo.Core.UI.ImageWrapper;
import Armadillo.Core.UI.TableRow;
import  Utils.Gui.AUiTableItem;
import Web.Base.TableRowExtended;
import Web.Base.WebHelper;
import Web.Dashboard.DynamicGuiInstanceWrapper;

public class MyTableHelper 
{
	private static final int ROWS_PER_PAGE = 20;
	private static Reflector m_tableRowReflector;
	
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
	
	public static void reloadTable(AUiTableItem currUiItem) 
	{
		try
		{
			currUiItem.setTableRows(null);
			currUiItem.resetSearcher();
			currUiItem.getTableRows();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public static DataTable loadTableUiComponent(
			String strTabName,
			DynamicGuiInstanceWrapper tabInstanceWrapper,
			boolean blnLoadItemDetail,
			String strBeanName) 
	{
		try
		{
			String[] columnNames = ((AUiTableItem)tabInstanceWrapper.getUiItem()).getFieldNames();
			Type[] columnTypes = ((AUiTableItem)tabInstanceWrapper.getUiItem()).getFieldTypes();
			loadColumnItems(
					columnNames, 
					columnTypes,
					tabInstanceWrapper);
			Map<String, ColumnModel> colMap = new HashMap<String, ColumnModel>();
			Map<String, Column> pfColMap = new HashMap<String, Column>();
			tabInstanceWrapper.setColMap(colMap);
			tabInstanceWrapper.setPfColMap(pfColMap);
			DataTable dataTable = new DataTable();
			dataTable.setScrollable(true);
			dataTable.setLiveScroll(true);
			//dataTable.setScrollWidth("90%");
			//dataTable.setScrollHeight("90%");
			dataTable.setValueExpression("value",
					WebHelper.createValueExpression("#{" + strBeanName + ".tableRows}",
							List.class));
			dataTable.setVar("dummyRow");
	
			dataTable.setValueExpression("selection", WebHelper
					.createValueExpression("#{" + strBeanName + ".selectedTableRow}",
							TableRow.class));
	
			dataTable.setValueExpression("selectionMode",
					WebHelper.createValueExpression("single", String.class));
	
			String strTableId = WebHelper.getTableId(strTabName);
			dataTable.setId(strTableId);
	
			dataTable.setValueExpression("dynamic", WebHelper
					.createValueExpression(((Object) true).toString(),
							Boolean.class));
	
			dataTable.setValueExpression("paginator", WebHelper
					.createValueExpression(((Object) true).toString(),
							Boolean.class));
	
			dataTable.setValueExpression("rows", WebHelper
					.createValueExpression(ROWS_PER_PAGE + "", Integer.class));
	
			dataTable.setValueExpression("filteredValue", WebHelper
					.createValueExpression("#{" + strBeanName + ".filteredTableRows}",
							List.class));
	
			dataTable.setValueExpression("rowKey", WebHelper
					.createValueExpression("#{dummyRow.key}", String.class));
	
			dataTable.setResizableColumns(true);
			dataTable.setLiveResize(true);
			dataTable.setRendered(true);
			//
			// NOTE: primefaces v4 causes issues with draggable columns!
			//
			//dataTable.setDraggableColumns(true);
			
			List<ColumnModel> columns = ((AUiTableItem)tabInstanceWrapper.getUiItem()).getColumns();
			for (ColumnModel columnModel : columns) 
			{
				Column pfColumn = new Column();
				dataTable.getChildren().add(pfColumn);
	
				// Create <h:outputText value="ID"> for <f:facet name="header">
				// of 'ID' column.
				HtmlOutputText idHeader = new HtmlOutputText();
				idHeader.setValue(columnModel.getHeader());
				pfColumn.setHeader(idHeader);
				
				String strColId = columnModel.getProperty();
				pfColumn.setId(strColId);
	
				if(colMap != null)
				{
			
					colMap.put(strColId, columnModel);
					pfColMap.put(strColId, pfColumn);
				}
				
//				 pfColumn.setValueExpression(
//				 "width",
//				 WebHelper.createValueExpression("#{" + strBeanName + ".colWidth('" + 
//						 strColId + "')}", String.class));
	
				// Create <h:outputText value="#{dataItem.id}"> for the body of
				// 'ID' column.
				String strProp = "#{dummyRow." + columnModel.getProperty() + "}";
				if(columnModel.isImageType())
				{
					String strLabel = columnModel.getHeader() + "_imgCol";
					pfColumn.getChildren().add(
							getImageComponent(strLabel, strProp));
				}
				else
				{
					HtmlOutputText idOutput = new HtmlOutputText();
					idOutput.setValueExpression("value", WebHelper
							.createValueExpression(
									strProp,
									String.class));
					pfColumn.getChildren().add(idOutput);
				}
				pfColumn.setValueExpression("sortBy", WebHelper
						.createValueExpression(
								"#{dummyRow." + columnModel.getProperty() + "}",
								String.class));
				
				pfColumn.setValueExpression("filterBy", WebHelper
						.createValueExpression(
								"#{dummyRow." + columnModel.getProperty() + "}",
								String.class));
				pfColumn.setFilterMatchMode("contains");
				//
				// there is no dynamic setter
				//
	//				pfColumn.setValueExpression("dynamic", WebHelper
	//						.createValueExpression(((Object) true).toString(),
	//								Boolean.class));
			}
			if(blnLoadItemDetail)
			{
				addOnRowSelectEvent(dataTable,
						strBeanName);
			}
			return dataTable;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
	
	public static void loadColumnItems(
			String[] columnNames,
			Type[] types,
			DynamicGuiInstanceWrapper dynamicGuiInstanceWrapper) 
	{
		try
		{
			ArrayList<ColumnModel> columns = WebHelper.getColumnItemsList(
					columnNames,
					types);
			((AUiTableItem)dynamicGuiInstanceWrapper.getUiItem()).setColumns(columns);
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}
	
	//private static int intCounter = 0;
	
	private static GraphicImage getImageComponent(
			String strLabel,
			String strPropertyName)
	{
		try
		{
			GraphicImage graphicImage = WebHelper.loadGraphicImageComponentProp(
					strLabel, 
					strPropertyName);
			
//			UIParameter param1 = new UIParameter();
//			param1.setName("idTableImage");
//			param1.setValue("#{dummyRow.key}");
//			param1.setId(strLabel + intCounter++);
//			graphicImage.getChildren().add(param1);
//			
//			UIParameter param2 = new UIParameter();
//			param2.setName("idLabelImage");
//			param2.setValue(strLabel);
//			param2.setId(strLabel + intCounter++);
//			graphicImage.getChildren().add(param2);
			
			return graphicImage;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
	
	private static void addOnRowSelectEvent(
			DataTable dataTable,
			String strBeanName) 
	{
		try 
		{
			AjaxBehavior ajaxBehaviour = 
					new AjaxBehavior();
			
			ajaxBehaviour.setValueExpression("event",
					WebHelper.createValueExpression("rowSelect", String.class));

			MethodExpression methodExpression = WebHelper.createMethodExpression(
					"#{" + strBeanName + ".onSelectRow}", 
					Void.class, 
					new Class[] { SelectEvent.class });
			ajaxBehaviour.setListener(methodExpression);	
			
			ajaxBehaviour.setValueExpression("listener", WebHelper
					.createValueExpression("#{" + strBeanName + ".onSelectRow}",
							String.class));

			ajaxBehaviour.setValueExpression("update", WebHelper
					.createValueExpression(":display :growl", String.class));

			ajaxBehaviour.setValueExpression("oncomplete", WebHelper
					.createValueExpression("PF('itemDialog').show()",
							String.class));
			
//			ajaxBehaviour.setValueExpression("oncomplete", WebHelper
//					.createValueExpression("#{" + strBeanName + ".onSelectRow1()}",
//							String.class));

			dataTable.addClientBehavior("rowSelect", ajaxBehaviour);
		}
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}

	public static <T> List<TableRow> generateTableRowListFromRawObjects(
			List<T> rawDataList)
	{
		return generateTableRowListFromRawObjects(rawDataList.toArray(new Object[0]));
	}
	
	public static <T> List<TableRow> generateTableRowListFromRawObjects(
			T[] rawDataList)
	{
		try
		{
			if(rawDataList == null || rawDataList.length == 0)
			{
				return new ArrayList<TableRow>();
			}
			Reflector reflector = ReflectionCache.getReflector(rawDataList[0].getClass());
			ArrayList<TableRow> tableRows = new ArrayList<TableRow>();
			int intCounter = 0;
			for(Object obj : rawDataList)
			{
				TableRow tableRow = null;
				tableRow = generateTableRow(
						reflector, 
						obj,
						(intCounter++) + obj.toString());
				tableRows.add(tableRow);
			}
			return tableRows;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ArrayList<TableRow>();
	}
	
	
	public static TableRow generateTableRow(
			Reflector reflector,
			Object obj,
			String strKey) 
	{
		try
		{
			if(obj == null)
			{
				return null;
			}
			String[] tableRowCols = m_tableRowReflector.getColNames();
			TableRow tableRow = null;
			Object[] objs = reflector.getPropValues(obj);
			if(objs != null)
			{
				tableRow = new TableRowExtended();
				for (int i = 1; i <= objs.length; i++) // zero is the key index 
				{
					Object currObj = objs[i-1];
					if(currObj != null)
					{
//						if(currObj instanceof ImageWrapper)
//						{
//							currObj = new DefaultStreamedContent(new ByteArrayInputStream(
//									((ImageWrapper)currObj).getBytes()));
//						}
						m_tableRowReflector.SetPropertyValue(
								tableRow, 
								tableRowCols[i], 
								currObj);
					}
					else
					{
						Type propType = reflector.getPropertyType(i-1);
						if(propType == ImageWrapper.class)
						{
							//
							// add default image
							//
							m_tableRowReflector.SetPropertyValue(
									tableRow, 
									tableRowCols[i], 
									new DefaultStreamedContent(new ByteArrayInputStream(WebHelper.m_defaultContent)));
						}
					}
				}
			}
			tableRow.setKey(strKey);
			return tableRow;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}		
}
