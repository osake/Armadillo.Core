package Web.Catalogue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import Armadillo.Core.Logger;
import Armadillo.Core.UI.TableRow;
import  Utils.Gui.AUiTableItem;

public abstract class AUiCatalogueTableItem extends AUiTableItem
{
	public abstract int getRowCount();
	
	@Override
	public Collection<TableRow> getTableRows() 
	{
		try
		{
			synchronized(m_lockObj)
			{
				List<TableRow> tableRows = m_tableRows;
				if(tableRows != null) 
				{
					int intCurrRowCount = getRowCount();
					if(tableRows.size() != intCurrRowCount)
					{
						//
						// generate again
						//
						m_tableRows.clear();
						m_tableRows = null;
					}
				}
			}
			// its important to be out of the lock here
			return super.getTableRows();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ArrayList<TableRow>();
	}
}
