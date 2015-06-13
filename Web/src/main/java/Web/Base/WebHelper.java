package Web.Base;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UISelectItem;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.event.BehaviorEvent;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpSession;

import org.joda.time.DateTime;
import org.primefaces.behavior.ajax.AjaxBehavior;

import org.primefaces.behavior.ajax.AjaxBehaviorListenerImpl;
//import org.primefaces.component.behavior.ajax.AjaxBehaviorListenerImpl;
import org.primefaces.component.calendar.Calendar;
import org.primefaces.component.fileupload.FileUpload;
import org.primefaces.component.graphicimage.GraphicImage;
import org.primefaces.component.inputtextarea.InputTextarea;
import org.primefaces.component.outputlabel.OutputLabel;
import org.primefaces.component.panel.Panel;
import org.primefaces.component.panelgrid.PanelGrid;
import org.primefaces.component.selectbooleanbutton.SelectBooleanButton;

import com.sun.faces.component.visit.FullVisitContext;

import Armadillo.Core.Logger;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.UI.AComboList;
import Armadillo.Core.UI.ColumnModel;
import Armadillo.Core.UI.ImageWrapper;
import Armadillo.Core.UI.LabelClass;
import Armadillo.Core.UI.UiHelper;
import  Utils.Gui.AUiItem;
import Web.Dashboard.DynamicGuiInstanceWrapper;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.UploadedFile;

public class WebHelper 
{
	public static byte[] m_defaultContent;
	
	static
	{
		try
		{
			BufferedImage image = ImageIO.read(new File("C:/HC.Java/workspace/HC.Products.Web.Gch/WebContent/TrashEmpty.png")); 
			ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			ImageIO.write(image, "png", baos); 
			m_defaultContent = baos.toByteArray();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public static String getGuiItemName(TreeNode treeNode3) 
	{
		try
		{
			String strTabName3 = treeNode3.toString();
			TreeNode treeNode2 = treeNode3.getParent();
			String strTabName2 = treeNode2.toString();
			TreeNode treeNode1 = treeNode2.getParent();
			String strTabName1 = treeNode1.toString();
			String strTabName = UiHelper.getNodeKey(strTabName1,
					strTabName2, strTabName3);
			return strTabName;
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		} 
		return "";
	}
	
	public static HtmlPanelGroup loadPanelGroup(
			String strTabName, 
			UIComponent uiComponent,
			DynamicGuiInstanceWrapper dynamicGuiInstanceWrapper) 
	{
		try
		{
			// Finally add the datatable to <h:panelGroup
			// binding="#{myBean.dataTableGroup}">.
			uiComponent.setRendered(true);
			HtmlPanelGroup panelGroup = new HtmlPanelGroup();
			dynamicGuiInstanceWrapper.setComponentInTab(panelGroup);
			panelGroup.getChildren().add(uiComponent);
			
			//CommandButton closeButton = createCloseBtn(strTabName);
			//panelGroup.getChildren().add(closeButton);
			String strDataTableGroupId = WebHelper.getPanelGroupId(strTabName);
			panelGroup.setId(strDataTableGroupId);
			panelGroup.setRendered(true);
			panelGroup.setValueExpression(
					"style",
					WebHelper.createValueExpression(
							"height: 100%",
							String.class));
			return panelGroup;
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return null;
	}
	
	public static DynamicGuiInstanceWrapper createDynamicGuiInstanceWrapper(
			String strTabName, 
			AUiItem uiItem) 
	{
		try
		{
			return createDynamicGuiInstanceWrapper(
						strTabName, 
						uiItem,
						-1);
		}
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return null;
	}
	
	
	public static DynamicGuiInstanceWrapper createDynamicGuiInstanceWrapper(
			String strTabName, 
			AUiItem uiItem,
			int intTabIndex) 
	{
		try
		{
			DynamicGuiInstanceWrapper dyncamicGuiInstanceWrapper = new DynamicGuiInstanceWrapper();
			dyncamicGuiInstanceWrapper.setTabIndex(intTabIndex);
			dyncamicGuiInstanceWrapper.setUiItem(uiItem);
			dyncamicGuiInstanceWrapper.setTabName(strTabName);
			dyncamicGuiInstanceWrapper.setTitle(strTabName);
			dyncamicGuiInstanceWrapper.setContent(strTabName);
			return dyncamicGuiInstanceWrapper;
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return null;
	}
	
	public static Date parseDateFromParams(
			Map<String, String> paramsMap,
			String strParamName,
			Date defaultDate)
	{
		try
		{
			if(paramsMap.containsKey(strParamName))
			{
				return DateTime.parse(paramsMap.get(strParamName)).toDate();
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return defaultDate;
	}
	
	public static boolean parseBooleanFromParams(
			Map<String, String> paramsMap,
			String strParamName)
	{
		try
		{
			if(paramsMap.containsKey(strParamName))
			{
				return Boolean.parseBoolean(paramsMap.get(strParamName));
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return false;
	}
	
	
	public static ArrayList<ColumnModel> getColumnItemsList(
			String[] columnNames,
			Type[] columnTypes) 
	{
		try 
		{
			ArrayList<ColumnModel> columns = new ArrayList<ColumnModel>();
			for (int i = 0; i < Math.min(
					columnNames.length,
					UiHelper.getMaxNumCols()); i++) 
			{
				String strCurrCol = columnNames[i];
				String[] r = strCurrCol.split("(?=\\p{Upper})");
				String strColDescr = StringHelper.join(r, " ");
				String strDummyCol = "col" + (i + 1);
				Type colType;
				if(columnTypes == null)
				{
					colType = String.class;
				}
				else
				{
					colType = columnTypes[i];
				}
				columns.add(new ColumnModel(strColDescr, strDummyCol, colType));
			}
			return columns;
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return new ArrayList<ColumnModel>();
	}
	
	public static TreeNode parseTree(
			String strKey, 
			DefaultTreeNode root) 
	{
		try
		{
			if(StringHelper.IsNullOrEmpty(strKey))
			{
				return null;
			}
			String[] tokens = strKey.split("\\|");
			
			if(tokens.length < 3)
			{
				return null;
			}
			
			//
			// find node1
			//
			boolean blnFoundNode1 = false;
			TreeNode defaultTreeNode1 = null;
	    	for(TreeNode treeNode1 : root.getChildren())
	    	{
	    		String strTabName1 = treeNode1.toString();
	    		if(strTabName1.equals(tokens[0]))
	    		{
	    			defaultTreeNode1 = treeNode1;
	    			blnFoundNode1 = true;
	    			break;
	    		}
	    	}
	    	if(!blnFoundNode1)
	    	{
	    		defaultTreeNode1 = 
	    				new DefaultTreeNode(
	    						tokens[0], 
	    						root);
	    	}
	    	
	    	//
	    	// find node2
	    	//
			boolean blnFoundNode2 = false;
			TreeNode defaultTreeNode2 = null;
			for(TreeNode treeNode2 : defaultTreeNode1.getChildren())
	    	{
				String strTabName2 = treeNode2.toString();
	    		if(strTabName2.equals(tokens[1]))
	    		{
	    			defaultTreeNode2 = treeNode2;
	    			blnFoundNode2 = true;
	    			break;
	    		}
	    	}
	    	if(!blnFoundNode2)
	    	{
	    		defaultTreeNode2 = 
	    				new DefaultTreeNode(
	    						tokens[1], 
	    						defaultTreeNode1);
	    	}
	    	
			
			//
			// find node3
			//
			boolean blnFound3 = false;
			TreeNode leafNode = null;
	    	for(TreeNode treeNode3 : defaultTreeNode2.getChildren())
	    	{
				String strTabName3 = treeNode3.toString();            			
	    		if(strTabName3.equals(tokens[2]))
	    		{
	    			blnFound3 = true;
	    			leafNode = treeNode3;
	    			break;
	    		}
	    	}
	    	if(!blnFound3)
	    	{
	    		leafNode = new DefaultTreeNode(
						tokens[2], 
						defaultTreeNode2);
	    	}
    		return leafNode;
		}
		catch(Exception ex){
			Logger.log(ex);
		}
		return null;
	}
	
	public static void setValueToInputText(
			String strPropName, 
			Object objVal) 
	{
		try
		{
			UIOutput inputText = (UIOutput)WebHelper.findUiComponent(strPropName);
			if(inputText != null)
			{
				inputText.setValue(objVal);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public static void setValueToCalendar(
			String strPropName, 
			String strVal) 
	{
		try
		{
			Calendar calendar = (Calendar)WebHelper.findUiComponent(strPropName);
			if(calendar != null &&
				!StringHelper.IsNullOrEmpty(strVal))
			{
				calendar.setValue(
						DateTime.parse(strVal).toDate());
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	

//	public static void showWaitDialog()
//	{
//		try
//		{
//			FacesContext ctx = FacesContext.getCurrentInstance();
//			RequestContext requestContext = RequestContext.getCurrentInstance();
//			showWaitDialog(ctx, requestContext);
//		}
//		catch(Exception ex)
//		{
//			Logger.log(ex);
//		}
//	}
//	
//	public static void showWaitDialog(
//			FacesContext ctx,
//			RequestContext requestContext)
//	{
//		try
//		{
//			//FacesContext ctx = FacesContext.getCurrentInstance();
//		    UIViewRoot rootView = ctx.getViewRoot();
//		    Dialog dialog = (Dialog)rootView.findComponent("blockPage");
//		    dialog.setVisible(true);
//		    dialog.setModal(true);
//		    dialog.setMinWidth(10);
//		    dialog.setMinHeight(10);
//		    requestContext.update(dialog.getId());
//		    requestContext.execute("PF('blockPageDialog').show()");	
//		}
//		catch(Exception ex)
//		{
//			Logger.log(ex);
//		}
//	}
//	
//	public static void closeWaitDialog()
//	{
//		RequestContext requestContext = RequestContext.getCurrentInstance();
//		closeWaitDialog(
//				null,
//				requestContext);
//	}
//	
//	public static void closeWaitDialog(
//			FacesContext ctx,
//			RequestContext requestContext)
//	{
//		try
//		{
//			requestContext.execute("blockPageDialog.hide()");
//		}
//		catch(Exception ex)
//		{
//			Logger.log(ex);
//		}
//	}
//	

	public static UIOutput loadSwitchItem(
			String strBeanName,
			LabelClass labelClass) 
	{
		try
		{
			UIOutput uiInput;
			//org.primefaces.component.
			SelectBooleanButton selectBooleanButton = new SelectBooleanButton();
			selectBooleanButton.setLabel(labelClass.getLbl());
			selectBooleanButton.setOnLabel("true");
			selectBooleanButton.setOffLabel("false");
			uiInput = selectBooleanButton;
			//			uiInput.setValueExpression(
//					"style",
//					createValueExpression(
//							"width: 95%",
//							String.class));
			
			Object objVal = labelClass.getObjValue();
			if(objVal == null)
			{
				objVal = false;
			}
			uiInput.setValue(objVal);
			
    		loadEvent(
    				labelClass.getLbl(), 
    				uiInput, 
	        		strBeanName + ".onBlur",
	        		"valueChange");	        		
			
// 			loadEvent(
//					labelClass.getLbl(), 
//					uiInput, 
//					strBeanName + ".onBlur",
//					"blur");
			
//			uiInput.setValueExpression(
//					"onclick",
//					createValueExpression(
//							"this.select()",
//							String.class));
			return uiInput;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}

	public static void loadFileUpload(
			LabelClass labelClass, 
			Panel panel,
			String strBeanName) 
	{
		try
		{
			//HtmlForm form = new HtmlForm(); 
			
			FileUpload fileUpload = new FileUpload();
			fileUpload.setId(labelClass.getLbl() + "_fileUpload");
//			htmlInputFile.setValueExpression("value",
//					WebHelper.createValueExpression("#{frmBean.file}",
//							UploadedFile.class));
//			UIParameter param = new UIParameter();
//			param.setName("id");
//			param.setValue(labelClass.getLbl());
//			htmlInputFile.getChildren().add(param);
//			form.getChildren().add(fileUpload);
//			form.setEnctype("multipart/form-data");
//			
//			form.setValueExpression("method",
//					WebHelper.createValueExpression("POST",
//							String.class));
//			form.getChildren().add(fileUpload);
			panel.getChildren().add(fileUpload);
			//htmlInputFile.setAuto(true);
			
//			CommandButton button = new CommandButton();
//			button.setValue("Upload");
//			button.setAjax(false);
			MethodExpression methodExpression = WebHelper.createMethodExpression(
				"#{" + strBeanName + ".file}", 
				Void.class, 
				new Class[] { UploadedFile.class });
			fileUpload.setFileUploadListener(methodExpression);
			//button.setActionExpression(methodExpression);

			//panel.getChildren().add(button);
//			graphicImage.setValueExpression("dynamic",
//			WebHelper.createValueExpression("true",
//					boolean.class));
			//setDefaultImage(graphicImage);
			
			GraphicImage graphicImage = loadGraphicImageComponent(labelClass, strBeanName);
			panel.getChildren().add(graphicImage);
			//button.seta
			
//			FileUpload fileUpload = new FileUpload();
//			fileUpload.setValueExpression("value",
//					WebHelper.createValueExpression("#{frmBean.file}",
//							UploadedFile.class));
			//fileUpload.setValue("#{frmBean.file}");
			//fileUpload.setMode("simple");
//			fileUpload.setId(labelClass.getLbl() + "_file");
//			UIParameter param = new UIParameter();
//			param.setName("id");
//			param.setValue(labelClass.getLbl());
//			fileUpload.getChildren().add(param);
//			return fileUpload;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public static GraphicImage loadGraphicImageComponent(
			LabelClass labelClass,
			String strBeanName) 
	{
		try
		{
			String strLabel = labelClass.getLbl().replace(" ", "");
			return loadGraphicImageComponent(strLabel, strBeanName);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}

	public static GraphicImage loadGraphicImageComponent(
			String strLabel,
			String strBeanName) 
	{
		try
		{
			return loadGraphicImageComponentProp(strLabel, "#{" + strBeanName + ".dynamicImage}");
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
	private static int intCounter = 0;
	
	public static GraphicImage loadGraphicImageComponentProp(
			String strLabel,
			String strProp) 
	{
		try
		{
			GraphicImage graphicImage = new GraphicImage();
			graphicImage.setId(strLabel + "_" + intCounter++);
			graphicImage.setCache(false);
			
			
			graphicImage.setValueExpression("value",
			WebHelper.createValueExpression(
					"#{frmBean.dynamicImage}",
					StreamedContent.class));
			
//			graphicImage.setValueExpression("value",
//				WebHelper.createValueExpression(strProp,
//						StreamedContent.class));
			return graphicImage;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}

	public static void loadLabel(
			PanelGrid panel, 
			LabelClass labelClass) 
	{
		try
		{
			OutputLabel label = new OutputLabel();
			label.setValue(labelClass.getLbl());
			panel.getChildren().add(label);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public static UIOutput loadCalendarItem(
			String strBeanName,
			LabelClass labelClass) 
	{
		try
		{
			UIOutput uiInput;
			uiInput = new Calendar();	
			
			((Calendar)uiInput).setPattern("dd.MM.yyyy HH:mm:ss");
			
			loadEvent(labelClass.getLbl(), 
					uiInput, 
					strBeanName + ".onDateSelect",
					"dateSelect");
			
			loadEvent(
					labelClass.getLbl(), 
					uiInput, 
					strBeanName + ".onBlur",
					"blur");
			
			return uiInput;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}

	public static UIOutput loadInputTextItem(
			String strBeanName,
			LabelClass labelClass) 
	{
		try
		{
			UIOutput uiInput;
			uiInput = new InputTextarea();
			uiInput.setValueExpression(
					"style",
					createValueExpression(
							"width: 95%",
							String.class));
			
			uiInput.setValueExpression(
					"rows",
					createValueExpression(
							"1",
							Integer.class));
			
			uiInput.setValue(labelClass.getValue());
			loadEvent(
					labelClass.getLbl(), 
					uiInput, 
					strBeanName + ".onBlur",
					"blur");
			
			uiInput.setValueExpression(
					"ondblclick",
					createValueExpression(
							"this.select()",
							String.class));
			return uiInput;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
	
	public static UIOutput loadOutputTextItem(
			String strBeanName,
			LabelClass labelClass) 
	{
		try
		{
			UIOutput uiOutput;
			uiOutput = new UIOutput();
			uiOutput.setValueExpression(
					"style",
					createValueExpression(
							"width: 95%",
							String.class));
			uiOutput.setValue(labelClass.getValue());
			return uiOutput;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
	

	public static UIOutput loadComboItems(
			String strBeanName,
			LabelClass labelClass) 
	{
		UIOutput uiInput = null;
		try
		{
			HtmlSelectOneMenu selectOneMenu = new HtmlSelectOneMenu();
			
			selectOneMenu.setValueExpression(
					"style",
					createValueExpression(
							"width: 95%",
							String.class));
			
			selectOneMenu.setId(labelClass.getLbl());
			AComboList comboItems = labelClass.getComboItems();
			if(comboItems != null)
			{
				List<String> items = comboItems.getComboItemsList();
				boolean blnFoundItem = false;
        		UISelectItem uISelectItem = new UISelectItem();
        		uISelectItem.setItemLabel("");
        		uISelectItem.setItemValue("");
        		selectOneMenu.getChildren().add(uISelectItem);
        		blnFoundItem = true;

				if(items != null)
				{
					for(String strItem : items)
					{
						if(!StringHelper.IsNullOrEmpty(strItem))
						{
			        		uISelectItem = new UISelectItem();
			        		uISelectItem.setItemLabel(strItem);
			        		uISelectItem.setItemValue(strItem);
			        		selectOneMenu.getChildren().add(uISelectItem);
			        		blnFoundItem = true;
						}
					}
					if(blnFoundItem)
					{
			    		loadEvent(
			    				labelClass.getLbl(), 
			    				selectOneMenu, 
				        		strBeanName + ".onBlur",
				        		"valueChange");	        		
					}
					uiInput = selectOneMenu;
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return uiInput;
	}

	@SuppressWarnings("unchecked")
	public static <T> T findBean(String strBeanName) 
	{
		try
		{
		    FacesContext context = FacesContext.getCurrentInstance();
		    return (T) context.getApplication().evaluateExpressionGet(context, 
		    		"#{" + strBeanName + "}", Object.class);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
	
    public static UIComponent findUiComponent(final String strId0)
    {
    	try
    	{
    		if(StringHelper.IsNullOrEmpty(strId0))
    		{
    			return null;
    		}
    		
	        FacesContext context = FacesContext.getCurrentInstance(); 
	        UIViewRoot root = context.getViewRoot();
	        final UIComponent[] found = new UIComponent[1];
	        root.visitTree(new FullVisitContext(context), new VisitCallback() 
	        {   
	            public VisitResult visit(VisitContext context, UIComponent component) 
	            {
	            	try
	            	{
		                if(component != null)
		                {
		                	String strId = component.getId();
		                	if(!StringHelper.IsNullOrEmpty(strId) &&
		                		strId0.equals(strId))
		                	{
			                    found[0] = component;
			                    return VisitResult.COMPLETE;
		                	}
		                }
		                return VisitResult.ACCEPT;              
		        	}
		        	catch(Exception ex)
		        	{
		        		Logger.log(ex);
		        	}
	            	return VisitResult.REJECT;
	            }
	        });
	        UIComponent foundItem = found[0];
	        found[0] = null; // would there be a memory leak here?
	        return foundItem;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }    
	
	public static void loadEvent(
			String strId, 
			UIOutput inputText,
			String strMethodName,
			String strEventName) 
	{
		try
		{
			AjaxBehavior ajaxBehavior = (AjaxBehavior) FacesContext
					.getCurrentInstance().getApplication()
					.createBehavior(AjaxBehavior.BEHAVIOR_ID);  
			MethodExpression listener = FacesContext.getCurrentInstance()
					.getApplication().getExpressionFactory()
					.createMethodExpression(FacesContext.getCurrentInstance().getELContext(), 
					"#{" + strMethodName + "('" + strId + "')}", null, 
					new Class[] { BehaviorEvent.class });
			ajaxBehavior.addAjaxBehaviorListener(new AjaxBehaviorListenerImpl(listener, null)); 
			inputText.addClientBehavior(strEventName, ajaxBehavior);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public static String getSessionIdStatic()
	{
		try
		{
			FacesContext fCtx = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
			String strSessionId = session.getId();
			return strSessionId;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
	}	
	
	public static ValueExpression createValueExpression(
			String valueExpression,
			Class<?> valueType) 
	{
		try 
		{
			FacesContext facesContext = FacesContext.getCurrentInstance();
			return facesContext
					.getApplication()
					.getExpressionFactory()
					.createValueExpression(facesContext.getELContext(),
							valueExpression, valueType);
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public static MethodExpression createMethodExpression(
			String strValueExpression,
			Class returnValueType,
			Class[] paramsValueTypes) 
	{
		try 
		{
			FacesContext facesContext = FacesContext.getCurrentInstance();
			return facesContext
					.getApplication()
					.getExpressionFactory()
					.createMethodExpression(
							facesContext.getELContext(),
							strValueExpression, 
							returnValueType,
							paramsValueTypes);
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
		return null;
	}

	public static String getPanelGroupId(String strTabName) 
	{
		return cleanId(strTabName);
	}

	public static String getTableId(String strTabName) 
	{
		if (StringHelper.IsNullOrEmpty(strTabName)) 
		{
			return "";
		}
		return cleanId(strTabName) + "_table";
	}

	public static String getChartId(String strTabName) 
	{
		if (StringHelper.IsNullOrEmpty(strTabName)) 
		{
			return "";
		}
		return cleanId(strTabName) + "_chart";
	}	
	
	private static String cleanId(String strTabName) {
		return StringHelper.CleanString(strTabName)
				.replace("|", "_")
				.replace(".", "_")
				.replace(",", "_")
				.replace("%", "_")
				.replace("-", "_")
				.replace("+", "_")
				.replace("@", "_")
				.replace("[", "_")
				.replace("]", "_")
				.replace("(", "_")
				.replace(")", "_")
				.replace("{", "_")
				.replace("}", "_")
				.replace("#", "_")
				.replace("~", "_")
				.replace("&", "_")
				.replace("$", "_")
				.replace("!", "_")
				.replace("?", "_")
				.replace(" ", "_");
	}

	public static void setValueToFileLoader(
			String strPropName, 
			Object objVal) 
	{
		try
		{
			UIOutput inputText = (UIOutput)WebHelper.findUiComponent(strPropName);
			if(inputText != null)
			{
				inputText.setValue(objVal);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public static void setValueToImage(
			String strPropName, 
			Object objValue) 
	{
		try
		{
			UIComponent image = WebHelper.findUiComponent(strPropName);
			if(image == null)
			{
				return;
			}
			if(objValue == null)
			{
				//setDefaultImage(image);
				return;
			}
        	byte[] res = ((ImageWrapper)objValue).getBytes();
        	DefaultStreamedContent stream = new DefaultStreamedContent(new ByteArrayInputStream(res));
			((GraphicImage)image).setValue(stream);
			
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public static void setObjValueInput(String strPropName, Object objVal) 
	{
		try
		{
			UIOutput inputText = (UIOutput)WebHelper.findUiComponent(strPropName);
			if(inputText != null)
			{
				inputText.setValue(objVal);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public static String parseStringFromParams(
			Map<String, String> paramsMap,
			String strParamName) 
	{
		try
		{
			if(paramsMap.containsKey(strParamName))
			{
				return paramsMap.get(strParamName);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
	}


//	private static void setDefaultImage(UIComponent image)
//	{
//		try
//		{
//			((GraphicImage)image).setValue(m_defaultContent);
//		}
//		catch(Exception ex)
//		{
//			Logger.log(ex);
//		}
//	}
}
