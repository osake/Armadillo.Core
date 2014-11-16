package Web.Frm;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIForm;
import javax.faces.component.UIOutput;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.primefaces.component.dialog.Dialog;
import org.primefaces.component.graphicimage.GraphicImage;
import org.primefaces.component.menubar.Menubar;
import org.primefaces.component.panel.Panel;
import org.primefaces.component.panelgrid.PanelGrid;
import org.primefaces.context.RequestContext;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;

import Armadillo.Core.Logger;
import Armadillo.Core.UI.LabelClass;
import  Utils.Gui.ACustomMenuItem;
import  Utils.Gui.Frm.AUiFrmItem;
import Web.Base.WebHelper;

public class FrmHelper 
{

	public static void showDialog(
			AUiFrmItem uiFrmItem,
			String strDialogName,
			String strPanelName,
			String strBeanName,
			String strWidgetName,
			boolean blnModal,
			boolean blnIsInput) 
	{
		try
		{
			FacesContext ctx = FacesContext.getCurrentInstance();
	        UIViewRoot rootView = ctx.getViewRoot();
	        Dialog dialog = (Dialog)rootView.findComponent(strDialogName);
	        
	        UIPanel panel = (UIPanel)rootView.findComponent(strPanelName);
	        dialog.setVisible(true);
	        dialog.setModal(blnModal);
	        
	        //---------------------------------------------
	        // TODO, this is just a test, check if it works!
	        dialog.setWidth("auto");
	        dialog.setHeight("auto");
	        //---------------------------------------------
	        
	        generateDialog(uiFrmItem, 
			        		dialog, 
			        		panel, 
			        		strBeanName,
			    			blnIsInput);
	        generateMenu(uiFrmItem, dialog, panel, strBeanName);
	        showFormAsDialog(strWidgetName);	
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public static void showFormAsDialog(String strWidgetName) 
	{
		RequestContext.getCurrentInstance().execute("PF('" + strWidgetName + "').show()");
	}
	
	public static void generateDialog(
			AUiFrmItem uiFrmItems,
			UIPanel dialog,
			String strBeanName,
			boolean blnIsInput)
	{
		generateDialog(
				uiFrmItems,
				dialog,
				dialog,
				strBeanName,
				blnIsInput);		
	}
	
	public static void generateDialog(
			AUiFrmItem uiFrmItem,
			UIPanel dialog,
			UIPanel uiPanel,
			String strBeanName,
			boolean blnIsInput)
	{
		try
		{
	        List<LabelClass> lblClasses0 = uiFrmItem.getLblClassess();
	        List<String> rowsToExclude = uiFrmItem.getRowsToExclude();
	        List<LabelClass> lblClasses;
	        if(rowsToExclude == null ||
	           rowsToExclude.size() == 0)
	        {
	        	lblClasses = lblClasses0;
	        }
	        else
	        {
	        	lblClasses = new ArrayList<LabelClass>();
	        	for(LabelClass labelClass : lblClasses0)
	        	{
	        		if(!rowsToExclude.contains(labelClass.getLbl()))
    				{
	        			lblClasses.add(labelClass);
    				}
	        	}
	        }
	        generateDialog(
	        		dialog, 
	        		uiPanel, 
	        		strBeanName, 
	        		blnIsInput,
					lblClasses);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public static void generateDialog(
			UIPanel dialog, 
			UIPanel uiPanel,
			String strBeanName, 
			boolean blnIsInput, 
			List<LabelClass> lblClassess) 
	{
		try
		{
			PanelGrid panel = new PanelGrid();
			panel.setColumns(2);
			panel.setValueExpression(
					"style",
					WebHelper.createValueExpression(
							"width: 100%",
							String.class));
			
			if(blnIsInput)
			{
				loadInputs(strBeanName, panel, lblClassess);
			}
			else
			{
				loadOutputs(strBeanName, panel, lblClassess);
			}
			uiPanel.getChildren().clear();
			UIForm form = new UIForm();
			form.getChildren().clear();
			form.getChildren().add(panel);
			uiPanel.getChildren().add(form);
	
			RequestContext.getCurrentInstance().update(uiPanel.getId());
			RequestContext.getCurrentInstance().update(dialog.getId());
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	private static void generateMenu(
			AUiFrmItem uiFrmItems, 
			Dialog dialog,
			UIPanel panel, 
			String strBeanName) 
	{
		try
		{
			List<ACustomMenuItem> menuItems = uiFrmItems.getMenuItems();
			if(menuItems == null ||
			   menuItems.size() == 0)
			{
				return;
			}
			Menubar menuBar = (Menubar)WebHelper.findUiComponent("frmMenuBar");
			if(menuBar.getModel() != null)
			{
				//
				// menu already loaded
				//
				menuBar.getModel().getElements().clear();
				menuBar.getElements().clear();
			}
			menuBar.setAutoDisplay(false);
			DefaultSubMenu subMenu = new DefaultSubMenu();
			subMenu.setLabel("File");
			DefaultMenuModel menuModel = new DefaultMenuModel();
			menuBar.setModel(menuModel);
			menuModel.addElement(subMenu);

			for(ACustomMenuItem customMenuItem : menuItems)
			{
				String strLabel = customMenuItem.getLabel();
				DefaultMenuItem menuItem = new DefaultMenuItem();
				menuItem.setValue(strLabel);
				subMenu.addElement(menuItem);
				menuItem.setCommand(
						"#{" + strBeanName + ".onMenuClick('" + strLabel + "')}");
				menuItem.setUpdate("myPanel, lowerLabel");
			}
			//
			// load close menu
			//
			String strLabel = "Close";
			DefaultMenuItem menuItem = new DefaultMenuItem();
			menuItem.setValue(strLabel);
			subMenu.addElement(menuItem);
			menuItem.setCommand(
					"#{" + strBeanName + ".close}");
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public static void showDialog(
			ArrayList<LabelClass> labels,
			String strDialogName,
			String strBeanName,
			String strWidgetName,
			boolean blnModal,
			boolean blnIsInput) 
	{
		try
		{
			FacesContext ctx = FacesContext.getCurrentInstance();
	        UIViewRoot rootView = ctx.getViewRoot();
	        Dialog dialog = (Dialog)rootView.findComponent(strDialogName);
	        dialog.setVisible(true);
	        dialog.setModal(blnModal);

	        //---------------------------------------------
	        // TODO, this is just a test, check if it works!
	        dialog.setWidth("auto");
	        dialog.setHeight("auto");
	        //---------------------------------------------
	        
	        generateDialog(
	        		dialog, 
	        		dialog, 
	        		strBeanName, 
	        		blnIsInput,
					labels);
	        
	        showFormAsDialog(strWidgetName);	
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	

	public static void showDialog(
			AUiFrmItem uiFrmItems,
			String strDialogName,
			String strBeanName,
			String strWidgetName,
			boolean blnModal,
			boolean blnIsInput) 
	{
		try
		{
			FacesContext ctx = FacesContext.getCurrentInstance();
	        UIViewRoot rootView = ctx.getViewRoot();
	        Dialog dialog = (Dialog)rootView.findComponent(strDialogName);
	        dialog.setVisible(true);
	        dialog.setModal(blnModal);
	        
	        //---------------------------------------------
	        // TODO, this is just a test, check if it works!
	        dialog.setWidth("auto");
	        dialog.setHeight("auto");
	        
	        //---------------------------------------------
	        
	        generateDialog(uiFrmItems, 
	        		dialog, 
	        		strBeanName,
	    			blnIsInput);
	        showFormAsDialog(strWidgetName);	
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	private static void loadOutputs(
			String strBeanName, 
			PanelGrid panel,
			List<LabelClass> lblClassess) 
	{
		try
		{
			for(LabelClass labelClass : lblClassess)
			{
				if(!labelClass.isAValidType())
				{
					continue;
				}
				
				WebHelper.loadLabel(panel, labelClass);
				loadOutputComponents(strBeanName, panel, labelClass);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	private static void loadOutputComponents(
			String strBeanName,
			PanelGrid panel, 
			LabelClass labelClass) 
	{
		try
		{
			UIOutput uiOutput = null;
 			if(labelClass.getIsImageType())
 			{
 				GraphicImage graphicImage = WebHelper.loadGraphicImageComponent(labelClass, strBeanName);
 				panel.getChildren().add(graphicImage);
 			}
 			else
			{
				uiOutput = WebHelper.loadOutputTextItem(strBeanName, labelClass);
			}
 			if(uiOutput != null)
 			{
				uiOutput.setId(labelClass.getLbl().replace(" ", ""));
				panel.getChildren().add(uiOutput);
 			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		
	}
	
	private static void loadInputs(
			String strBeanName, 
			PanelGrid panel,
			List<LabelClass> lblClassess) 
	{
		try
		{
			for(LabelClass labelClass : lblClassess)
			{
				if(!labelClass.isAValidType())
				{
					continue;
				}
				
				WebHelper.loadLabel(panel, labelClass);
				loadInputComponents(strBeanName, panel, labelClass);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	private static void loadInputComponents(
			String strBeanName, 
			PanelGrid panel,
			LabelClass labelClass) 
	{
		try
		{
			UIOutput uiInput = null;
			if(labelClass.getIsDateType())
			{
				uiInput = WebHelper.loadCalendarItem(strBeanName, labelClass);
			}
			else if(labelClass.getIsCombo())
			{
				uiInput = WebHelper.loadComboItems(strBeanName, labelClass);
			}        	
			else if(labelClass.getIsBooleanType())
			{
				uiInput = WebHelper.loadSwitchItem(strBeanName, labelClass);
			}        	
 			else if(labelClass.getIsImageType())
 			{
 				Panel myPanel = new Panel();
 				WebHelper.loadFileUpload(labelClass, 
 						myPanel,
 						strBeanName);				
 				panel.getChildren().add(myPanel);
 				
// 				graphicImage.setValueExpression("value",
// 						WebHelper.createValueExpression("#{frmBean.image}",
// 								StreamedContent.class));
// 				UIParameter param = new UIParameter();
// 				param.setName("id");
// 				param.setValue(labelClass.getLbl());
// 				graphicImage.getChildren().add(param);
 				//panel.getChildren().add(graphicImage);
 			}
			else
			{
				uiInput = WebHelper.loadInputTextItem(strBeanName, labelClass);
			}
			if(uiInput != null)
			{
				uiInput.setId(labelClass.getLbl());
				panel.getChildren().add(uiInput);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}	
}
