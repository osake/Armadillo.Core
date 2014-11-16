package Web.Frm;

import java.util.Date;
import java.util.HashMap;

import javax.faces.component.UIInput;

import org.joda.time.DateTime;
import org.primefaces.component.calendar.Calendar;
import org.primefaces.component.selectbooleanbutton.SelectBooleanButton;

import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.UI.LabelClass;
import  Utils.Gui.Frm.AFrmBeanBase;
import Web.Base.WebHelper;

public abstract class ADynamicFrmBean extends AFrmBeanBase 
{
    public void onBlur(String str)
    {
    	try 
    	{
    		if(m_uiFrmItem == null)
    		{
    			return;
    		}
    		
	    	UIInput inputText = (UIInput)WebHelper.findUiComponent(str);
	    	Object objVal = inputText.getValue();
	    	if(objVal == null)
	    	{
	    		return;
	    	}
	    	
	    	String strId = (String)inputText.getId();
	    	LabelClass labelClass = null;
    		String strKey = strId;
    		HashMap<String, LabelClass> labelMap = m_uiFrmItem.getLabelMap();
    		if(labelMap.containsKey(strKey))
    		{
    			labelClass = labelMap.get(strKey);
    		}
	    		
	    	if(labelClass != null)
	    	{
	    		String strVal = "";
	    		if(objVal != null)
	    		{
	    			if(labelClass.getIsDateType())
	    			{
	    				strVal = new DateTime((Date)objVal).toString();
	    			}
	    			else if(labelClass.getIsTextType())
	    			{
	    				strVal = objVal.toString();
	    			}
	    		}
	    		if(!StringHelper.IsNullOrEmpty(strVal))
	    		{
	    			labelClass.setValue(strVal);
					Console.writeLine("set value [" + strVal + "]");
	    		}
	    		else
	    		{
	    			if(labelClass.getIsBooleanType())
	    			{
	    				objVal = ((String)objVal).equals(((SelectBooleanButton)inputText).getOnLabel());
	    			}
	    			labelClass.setObjValue(objVal);
					Console.writeLine("set object value");
	    		}
	    	}
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }
    
	public void onDateSelect(String strId) 
	{
		try
		{
			Calendar calendar = (Calendar)WebHelper.findUiComponent(strId);
	        Date date = (Date)calendar.getValue();
	        HashMap<String, LabelClass> labelMap = m_uiFrmItem.getLabelMap();
	        labelMap.get(strId).setValue(new DateTime(date).toString());
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
    }
}
