package Web.Frm;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.PhaseId;
import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.primefaces.component.autocomplete.AutoComplete;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.ParserHelper;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.Text.StringWrapper;
import Armadillo.Core.UI.EnumActions;
import Armadillo.Core.UI.FrmItem;
import Armadillo.Core.UI.ImageWrapper;
import Armadillo.Core.UI.LabelClass;
import  Utils.Gui.ACustomMenuItem;
import  Utils.Gui.AUiParam;
import  Utils.Gui.Frm.AUiFrmItem;
import  Utils.Gui.Frm.FrmAction;
import Web.Base.WebHelper;
import Web.InputData.AInputDataDialogBean;

public abstract class AFrmBean extends ADynamicFrmBean
{
	private String m_strIndexText;
	private String m_strLowerLabel;
	private String m_strSearchValue;
	//private Part m_filePart;
	
	private DefaultStreamedContent m_defaultContent;
	private byte[] m_defaultImageBytes;
	
    public AFrmBean() 
    {
    	try
    	{
	    	loadDefaultImage();
    		
    		m_strSearchValue = "";
    		String strMessage = "Loaded bean[" + getClass().getName() + "]";
    		Logger.log(strMessage);
    		Console.writeLine(strMessage);
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

	private void loadDefaultImage() throws IOException 
	{
		try
		{
			BufferedImage image = ImageIO.read(new File("C:/HC.Java/workspace/HC.Products.Web.Gch/WebContent/TrashEmpty.png")); 
			ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			ImageIO.write(image, "png", baos); 
			m_defaultImageBytes = baos.toByteArray();
			
			setImageFromBytes(m_defaultImageBytes);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
    
//	  public void upload() 
//	  {
//		  try
//		  {
//		   		if(m_uiFrmItem == null || 
//		   		   m_filePart == null)
//	    		{
//	    			return;
//	    		}
//	    		
//		    	FacesContext context = FacesContext.getCurrentInstance();
//		    	String strId = context.getExternalContext().getRequestParameterMap().get("id");
//		    	HashMap<String, LabelClass> labelMap = m_uiFrmItem.getLabelMap();
//
//				FrmItem frmItem = getCurrItem();
//				if(frmItem == null)
//				{
//					return;
//				}
//				Object obj = frmItem.getObj();
//				if(obj == null)
//				{
//					return;
//				}
//		    	if(labelMap != null &&
//		    			labelMap.containsKey(strId))
//		    	{
//					InputStream inputStream = m_filePart.getInputStream();
//					byte[] bytes = IOUtils.toByteArray(inputStream);
//					Console.writeLine(m_filePart.getName());
//		    		labelMap.get(strId).setObjValue(bytes);
//		    		m_uiFrmItem.getReflector().SetPropertyValue(obj, strId, bytes);
//		    	}
//		  }
//		  catch(Exception ex)
//		  {
//			  Logger.log(ex);
//		  }
//	  }
	
//	  public Part getMyFile() 
//	  {
//		  return m_filePart;
//	  }
		 
//	  public void setMyFile(Part file) 
//	  {
//		  m_filePart = file;
//	  }
//	  
//	 public Part getMyFile() throws IOException 
//	 {
//		 try
//		 {
//	        FacesContext context = FacesContext.getCurrentInstance();
//
//	        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) 
//	        {
//	            // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
//	            return null;
//	        }
//	        else 
//	        {
//	            return m_filePart;
//	        }
//	    }
//		catch(Exception ex)
//		{
//			Logger.log(ex);
//		}
//		 return null;
//	 }
	  
    
    public void close()
    {
    	try
    	{
			RequestContext.getCurrentInstance().execute("frmWidget.hide()");
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
    }
    
	public void onClose(CloseEvent closeEvent)
	{
		try
		{
			if(m_uiFrmItem != null)
			{
				m_uiFrmItem.resetSearcher();
				m_uiFrmItem.onClose();
			}
			Console.writeLine("Dialog " + getClass().getName() + " closed");
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public void onMenuClick(String strMenuLabel)
	{
		try
		{
			if(m_uiFrmItem == null)
			{
				return;
			}
			if(StringHelper.IsNullOrEmpty(strMenuLabel))
			{
				return;
			}
			List<ACustomMenuItem> menuItems = m_uiFrmItem.getMenuItems();
			if(menuItems == null)
			{
				return;
			}
			for(ACustomMenuItem custoMenuItem : menuItems)
			{
				if(custoMenuItem.getLabel().equals(strMenuLabel))
				{
					custoMenuItem.invokeAction(this);
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
    public void onBlurIndex(AjaxBehaviorEvent event)
    {
    	try
    	{
    		UIInput inputText = (UIInput)event.getComponent();
	    	parseValueFromComponent(inputText);
	    	moveTorequestedIndex();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
    }
    
	private void moveTorequestedIndex() 
	{
		try
		{
			if(m_uiFrmItem == null)
			{
				return;
			}
	    	if(StringHelper.IsNullOrEmpty(m_strIndexText))
	    	{
	    		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Navigation:",  
						"Could not parse an empty index");
				FacesContext.getCurrentInstance().addMessage(null, message);
				m_strIndexText = (m_uiFrmItem.getIndex() + 1) + "";
	    		return;
	    	}
	    	
	    	int[] intResultArr = new int[1];
			if(!ParserHelper.tryParseIntegerValue(m_strIndexText, intResultArr))
			{
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Navigation:",  
						"Could not parse index [" + m_strIndexText + "]");
				FacesContext.getCurrentInstance().addMessage(null, message);
				m_strIndexText = (m_uiFrmItem.getIndex() + 1) + "";
				return;
			}
			
			int intIndex = intResultArr[0] - 1;
			int intSize = m_uiFrmItem.getSize();
			if(intIndex >= intSize ||
				intIndex < 0)
			{
				FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Navigation:",  
						"Could not navigate to index [" + m_strIndexText + "]");
				FacesContext.getCurrentInstance().addMessage(null, message);
				m_strIndexText = (m_uiFrmItem.getIndex() + 1) + "";
				return;
			}
	    	
			m_uiFrmItem.setIndex(intIndex);
			m_uiFrmItem.moveToIndex(intIndex);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

    public void pressedKey(int intKeyCode) 
    {
    	Console.writeLine("kez entered");
    }
	
    public void pressedEnter() 
    {
    	try
    	{
    		InputText inputText = (InputText)WebHelper.findUiComponent("indexInputText");
	    	parseValueFromComponent(inputText);
	    	moveTorequestedIndex();
			publishToUiItems();
			doLog();
	    	
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

	private void parseValueFromComponent(UIInput inputText) 
	{
		try
		{
			if(inputText != null)
			{
				Object objVal = inputText.getValue();
				if(objVal != null)
				{
					m_strIndexText = objVal.toString();
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

    public void onNew() 
    {
		try
		{
			if(m_uiFrmItem == null)
			{
				return;
			}
			
			List<LabelClass> lblClassess = m_uiFrmItem.getLblClassess();
			m_uiFrmItem.setIndex(-1);
			//m_strIndexText = "-1"; 
			
			for(LabelClass lblClass : lblClassess)
			{
				if(lblClass.getIsDateType())
				{
					lblClass.setValue(DateTime.now().toString());
					WebHelper.setValueToInputText(lblClass.getLbl(), 
							DateTime.now().toDate());
				}
				else if(lblClass.getIsTextType())
				{
					lblClass.setValue("");
					WebHelper.setValueToInputText(lblClass.getLbl(), "");
				}
				else if(lblClass.getIsImageType())
				{
					ImageWrapper imageWrapper = new ImageWrapper(m_defaultImageBytes);
					lblClass.setObjValue(imageWrapper);
					//WebHelper.setObjValueInput(lblClass.getLbl(), imageWrapper);
				}
			}
			doLog();
			FrmAction actionNew = m_uiFrmItem.getFrmAction(EnumActions.New);
			if(actionNew != null)
			{
				actionNew.invokeAction(this, null);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
    }
    
    public void save() 
    {
    	boolean blnSaveResult = false;
		try
		{
			if(m_uiFrmItem == null)
			{
				return;
			}
			
			if(!m_uiFrmItem.save(this))
			{
				blnSaveResult = false;
				return;
			}
			
//			FrmItem frmItem = getCurrItem();
//			Object obj = null;
//			if(frmItem != null)
//			{
//				obj = frmItem.getObj();
//			}
//			Object obj = frmItem.getObj();
//			if(obj == null)
//			{
//				return;
//			}
//			List<LabelClass> lblClasses = m_uiFrmItem.getLblClassess();
//			for(LabelClass labelClass : lblClasses)
//			{
//				if(labelClass.getIsBinaryType())
//				{
//					Object objVal = labelClass.getObjValue();
//					if(objVal != null)
//					{
//						m_uiFrmItem.getReflector().SetPropertyValue(obj, labelClass.getLbl(), objVal);
//					}
//					
////					UIComponent uiComp = WebHelper.findUiComponent(labelClass.getLbl() + "_file");
////					if(uiComp == null)
////					{
////						continue;
////					}
////					Object objVal = ((FileUpload)uiComp).getValue();
////					if(objVal != null)
////					{
////						UploadedFile uploadedFile = (UploadedFile)objVal;
////						byte[] bytes = uploadedFile.getContents();
////						
////						if(obj != null)
////						{
////							m_uiFrmItem.getReflector().SetPropertyValue(obj, labelClass.getLbl(), bytes);
////						}
////					}
//				}
//			}
			FrmAction actionSave = m_uiFrmItem.getFrmAction(
					EnumActions.Save);
			if(actionSave == null)
			{
				return;
			}
				
			StringWrapper stringWrap = new StringWrapper("");
			blnSaveResult = actionSave.invokeAction(this, stringWrap);
			
			m_uiFrmItem.moveToCurrent();
			publishToUiItems();
			doLog();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		finally
		{
			try
			{
				String strMessage = "";
				if(blnSaveResult)
				{
					strMessage = "Record successfuly saved";
				}
				else
				{
					strMessage = "Record not saved";
				}
				FacesMessage message = new FacesMessage(
						FacesMessage.SEVERITY_INFO, 
						"Result:",  
						strMessage);
				FacesContext.getCurrentInstance().addMessage(null, message);
			}
			catch(Exception ex)
			{
				Logger.log(ex);
			}
		}
    }
    


	public void delete(ActionEvent e) 
	{
		boolean blnSuccessDelete = false;
		try
		{
			if(m_uiFrmItem == null)
			{
				return;
			}
			
			if(!m_uiFrmItem.delete(this))
			{
				return;
			}
			
			FrmAction actionDelete = m_uiFrmItem.getFrmAction(EnumActions.Delete);
			if(actionDelete != null)
			{
				if(!actionDelete.invokeAction(this, null))
				{
					return;
				}
			}
			m_uiFrmItem.moveToNext();
			publishToUiItems();
			doLog();
			blnSuccessDelete = true;
			if(m_uiFrmItem.getSize()== 0)
			{
				onNew();
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		finally
		{
			try
			{
				String strMessage;
				if(blnSuccessDelete)
				{
					strMessage = "Record successfuly deleted";
				}
				else
				{
					strMessage = "Record not deleted";
				}
				FacesMessage message = new FacesMessage(
						FacesMessage.SEVERITY_INFO, 
						"Result",  
						strMessage);
				FacesContext.getCurrentInstance().addMessage(null, message);
			}
			catch(Exception ex)
			{
				Logger.log(ex);
			}
		}
    }
    
    public List<LabelClass> getLblClassess() 
    {
    	if(m_uiFrmItem == null)
    	{
    		return new ArrayList<LabelClass>();
    	}
    	
        return m_uiFrmItem.getLblClassess();
    }

    public void setLblClassess(List<LabelClass> lblClassess) 
    {
    	if(m_uiFrmItem == null)
    	{
    		return;
    	}
    	m_uiFrmItem.setLblClassess(lblClassess);
    }

	public void moveToFirst(
			ActionEvent actionEvent) 
	{
		try
		{
			if(m_uiFrmItem == null)
			{
				return;
			}
			m_uiFrmItem.moveToFirst();
			publishToUiItems();
			doLog();
			
			FrmAction actionNext = m_uiFrmItem.getFrmAction(EnumActions.Next);
			if(actionNext != null)
			{
				actionNext.invokeAction(this, null);
			}
			
			if(m_uiFrmItem.getSize() == 0)
			{
				onNew();
			}
		}
		catch(Exception ex){
			Logger.log(ex);
		}
	}

	public void moveToLast(ActionEvent actionEvent) 
	{
		try
		{
			if(m_uiFrmItem == null)
			{
				return;
			}
			m_uiFrmItem.moveToLast();
			publishToUiItems();
			doLog();
			
			FrmAction actionNext = m_uiFrmItem.getFrmAction(EnumActions.Next);
			if(actionNext != null)
			{
				actionNext.invokeAction(this, null);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public void moveToNext(ActionEvent actionEvent) 
	{
		try
		{
			if(m_uiFrmItem == null)
			{
				return;
			}
			m_uiFrmItem.moveToNext();
			publishToUiItems();
			doLog();
			
			FrmAction actionNext = m_uiFrmItem.getFrmAction(EnumActions.Next);
			if(actionNext != null)
			{
				actionNext.invokeAction(this, null);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public void moveToPrev(ActionEvent actionEvent) 
	{
		try
		{
			if(m_uiFrmItem == null)
			{
				return;
			}
			m_uiFrmItem.moveToPrev();
			publishToUiItems();
			doLog();
			
			FrmAction actionPrev = m_uiFrmItem.getFrmAction(EnumActions.Prev);
			if(actionPrev != null)
			{
				actionPrev.invokeAction(this, null);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	private void publishToUiItems() 
	{
		try
		{
			//m_uiFrmItem.moveToNext();
			publishObject();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	private void doLog()
	{
		try
		{
			if(m_uiFrmItem == null)
			{
				return;
			}
			m_strIndexText = (m_uiFrmItem.getIndex() + 1) + "";
			m_strLowerLabel = "Row [" +
					(m_uiFrmItem.getIndex() + 1) + "] of [" + m_uiFrmItem.getSize() + "]";
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	private void publishObject() 
	{
		try
		{
			if(m_uiFrmItem == null)
			{
				return;
			}
			String[] colNames = m_uiFrmItem.getColNames();
			HashMap<String, LabelClass> labelMap = m_uiFrmItem.getLabelMap();
			if(colNames == null ||
			   labelMap == null)
			{
				return;
			}
			
			List<String> rowsToExclude = m_uiFrmItem.getRowsToExclude();
			
			for(String strPropName : colNames)
			{
				//
				// do not add value to fields to exclude
				//
				if(rowsToExclude.contains(strPropName))
				{
					continue;
				}
				
				LabelClass labelClass = labelMap.get(strPropName);
				String strVal = labelClass.getValue();
				if(labelClass.getIsTextType())
				{
					WebHelper.setValueToInputText(strPropName, strVal);
				}
				else if(labelClass.getIsImageType())
				{
					Object objVal = labelClass.getObjValue();
					byte[] bytes = null;
					if(objVal!=null)
					{
						bytes = ((ImageWrapper)objVal).getBytes();
					}
					setImageFromBytes(bytes);
					WebHelper.setValueToImage(strPropName, objVal);
				}
				else if(labelClass.getIsDateType())
				{
					WebHelper.setValueToCalendar(strPropName, strVal);
				}
				else if(labelClass.getIsBooleanType())
				{
					Object objVal = labelClass.getObjValue();
					if(objVal == null)
					{
						objVal = false;
					}
					WebHelper.setObjValueInput(strPropName, objVal);
				}
			}
		}
		catch(Exception ex){
			Logger.log(ex);
		}
	}

	private void setImageFromBytes(byte[] bytes) 
	{
		try
		{
			if(bytes == null)
			{
				bytes = m_defaultImageBytes;
			}
			if(bytes != null)
			{
				m_defaultContent = new DefaultStreamedContent(new ByteArrayInputStream(bytes));
			}
			else
			{
				//
				// create stub. This should not occur
				//
				m_defaultContent = new DefaultStreamedContent();
			}
		}
		catch(Exception ex){
			Logger.log(ex);
		}
	}

	public String getLowerLabel() 
	{
		return m_strLowerLabel;
	}

	public void setLowerLabel(String strLowerLabel) 
	{
		m_strLowerLabel = strLowerLabel;
	}

	public String getIndexText() 
	{
		return m_strIndexText;
	}

	public void setIndexText(String strIndexText) 
	{
		m_strIndexText = strIndexText;
	}

	@Override
	public FrmItem getCurrItem() 
	{
		try
		{
			if(m_uiFrmItem == null)
			{
				return null;
			}
			return m_uiFrmItem.getCurrFrmItem();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}

	public void loadUiItem(AUiFrmItem uiFrmItem) 
	{
		try
		{
			m_uiFrmItem = uiFrmItem;
			if(m_uiFrmItem == null)
			{
				return;
			}
			
			AUiParam params = uiFrmItem.getParams();
			if(params != null)
			{
				AInputDataDialogBean inputDataDialogBean = 
						WebHelper.findBean("inputDataDialogBean");
				inputDataDialogBean.loadUiItem(this, params, null);
			}
			else
			{
				FrmHelper.showDialog(
						m_uiFrmItem,
						"frmDialog",
						":formDialog:myPanel",
						"frmBean",
						"frmWidget",
						false,
						true);
				moveToFirst(null);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public List<String> search(String strQuery)
	{
		try
		{
			if(m_uiFrmItem == null)
			{
				return new ArrayList<String>();
			}
			List<String> queryResults = m_uiFrmItem.search(strQuery);
			if(queryResults == null  ||
					queryResults.size() == 0)
			{
				return m_uiFrmItem.getKeys();
			}
				
			return queryResults;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ArrayList<String>();
	}
	
	public void onComboSelect(SelectEvent event)
	{
		try
		{
			
			if(m_uiFrmItem == null)
			{
				return;
			}
			
			Object item = event.getObject();
			
			if(item == null)
			{
				return;
			}
			AutoComplete autoCompleteText = (AutoComplete)WebHelper.findUiComponent("searchCombo");
			String strItem = (String)item;
			m_strSearchValue = strItem;
			autoCompleteText.setValue(strItem);
			List<String> keys = m_uiFrmItem.getKeys();
			int intIndex = keys.indexOf(strItem);
			
			if(intIndex >= 0)
			{
				m_uiFrmItem.setIndex(intIndex);
				m_uiFrmItem.moveToIndex(intIndex);
				publishToUiItems();
				doLog();
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public String getSearchValue() 
	{
		return m_strSearchValue;
	}

	public void setSearchValue(String m_strSearchValue) 
	{
		this.m_strSearchValue = m_strSearchValue;
	}	

	@Override
	public AUiFrmItem getFrmItem() 
	{
		return m_uiFrmItem;
	}

//    public UploadedFile getFile() 
//    {
//    	return null;
//    }
	
    public void file(FileUploadEvent fileUploadEvent) 
    {
    	String strId = fileUploadEvent.getComponent().getId();
    	setFile(fileUploadEvent.getFile(), strId);
    }

    public void setFile(
    		UploadedFile file,
    		String strId) 
    {
    	try
    	{
    		if(m_uiFrmItem == null)
    		{
    			return;
    		}
    		
	    	//FacesContext context = FacesContext.getCurrentInstance();
	    	//String strId = context.getExternalContext().getRequestParameterMap().get("id");
    		strId = strId.replace("_fileUpload", "");
    		
	    	HashMap<String, LabelClass> labelMap = m_uiFrmItem.getLabelMap();

			FrmItem frmItem = getCurrItem();
			if(frmItem == null)
			{
				if(m_uiFrmItem == null)
				{
					return;
				}
				int intIndex = m_uiFrmItem.getIndex();
				if(intIndex < 0)
				{
					//
					// this is a new item. Save first
					//
					save();
					moveToLast(null);
					frmItem = getCurrItem();
					if(frmItem == null)
					{
						return;
					}
				}
			}
			Object obj = frmItem.getObj();
	    	if(labelMap != null &&
	    			labelMap.containsKey(strId))
	    	{
	    		InputStream inputStream = file.getInputstream();
				byte[] bytes = IOUtils.toByteArray(inputStream);
	    		
	    		//byte[] bytes = file.getContents();
	    		labelMap.get(strId).setObjValue(new ImageWrapper(bytes));
	    		if(obj != null)
	    		{
	    			m_uiFrmItem.getReflector().SetPropertyValue(obj, strId, new ImageWrapper(bytes));
					setImageFromBytes(bytes);
	    		}
	    	}
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }	
    
	 public StreamedContent getImage() throws IOException 
	 {
		 try
		 {
			if(m_uiFrmItem == null)
			{
				return new DefaultStreamedContent();
			}
			
	    	FacesContext context = FacesContext.getCurrentInstance();
	    	String strId = context.getExternalContext().getRequestParameterMap().get("id");
	    	HashMap<String, LabelClass> labelMap = m_uiFrmItem.getLabelMap();
	    	if(labelMap == null ||
	    		!labelMap.containsKey(strId))
	    	{
				return new DefaultStreamedContent();
	    	}
			 
	        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) 
	        {
	            // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
	            return new DefaultStreamedContent();
	        }
	        else 
	        {
	        	Object objVal = labelMap.get(strId).getObjValue();
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
	 
	 
	 public Object getDynamicImage() throws IOException 
	 {
		 try
		 {
	        FacesContext context = FacesContext.getCurrentInstance();

	        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) 
	        {
	            // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
	            return new DefaultStreamedContent();
	        }
	        else 
	        {
/*	        	BufferedImage image = 
	        			ImageIO.read(new File("C:/HC.Java/workspace/HC.Products.Web.Gch/WebContent/TrashEmpty.png")); 
	        	ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
	        	ImageIO.write(image, "png", baos); 
	        	byte[] res = baos.toByteArray();
*/	        	
//	        	String imageId = context.getExternalContext().getRequestParameterMap().get("idTableImage");
//	        	FacesContext facesContext = FacesContext.getCurrentInstance();
//	        	Object strTst = facesContext.getApplication().evaluateExpressionGet(
//	        			facesContext,
//	        			imageId, Object.class);
//	        	Console.writeLine(imageId);
//	        	Console.writeLine(strTst);
	        	
	        	
	        	if(m_defaultContent == null)
	        	{
	        		Console.writeLine("Empty image!");
	        	}
	        	
	            return m_defaultContent;
	        }
	    }
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		 return null;
	 }
}