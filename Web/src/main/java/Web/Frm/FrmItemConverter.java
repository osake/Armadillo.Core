package Web.Frm;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import Armadillo.Core.Logger;
import Armadillo.Core.UI.FrmItem;

@FacesConverter(value = "FrmItemConverter")
public class FrmItemConverter implements Converter 
{
	
        public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) 
        {
        	try
        	{
//	        	FrmBean bean = (FrmBean) arg0.getApplication().evaluateExpressionGet(arg0, 
//	        			"#{frmBean}", FrmBean.class);
//	        	
//	        	if(bean != null)
//	        	{
////	            	HashMap<String, FrmItem> frmItems = bean.getItemsMap();
////	            	return frmItems.get(arg2);
//	        	}
        	}
        	catch(Exception ex){
        		Logger.log(ex);
        	}
        	return null;
        }

        public String getAsString(FacesContext arg0, UIComponent arg1, Object value) {
        	try{
	            if (value == null || value.equals("")) {
	                return "";
	            }
	            
	        	return ((FrmItem)value).getKey();
	    	}
	    	catch(Exception ex){
	    		Logger.log(ex);
	    	}
        	return "";
        }
}