package Web.Base;

import java.io.ByteArrayInputStream;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import org.primefaces.model.DefaultStreamedContent;

import Armadillo.Core.UI.ImageWrapper;
import Armadillo.Core.UI.TableRow;

@SuppressWarnings("serial")
public class TableRowExtended extends TableRow
{
	@Override
	public Object getCol1() 
	{
		Object objVal =  super.getCol1();
		if(objVal != null && objVal instanceof ImageWrapper)
		{
	    	FacesContext context = FacesContext.getCurrentInstance();
	        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) 
	        {
	            // So, we're rendering the view. Return a stub StreamedContent so that it will generate right URL.
	            return new DefaultStreamedContent();
	        }
	        else 
	        {
	        	if(objVal != null)
	        	{
		            return new DefaultStreamedContent(new ByteArrayInputStream(((ImageWrapper)objVal).getBytes()));
	        	}
	        }
		}
		return objVal;
	}
	
	@Override
	public String toString() 
	{
		return super.toString();
	}
}
