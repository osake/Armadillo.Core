package Web.Table;

import java.util.Map;
import java.util.Map.Entry;




import org.primefaces.component.column.Column;

import Armadillo.Core.Logger;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.UI.ColumnModel;
import Web.Dashboard.DynamicGuiInstanceWrapper;

public class TableColumnHelper 
{
	public static String DEFAULT_COL_WIDTH = "10";
	
	public static String getColWidth(
			DynamicGuiInstanceWrapper tabInstanceWrapper,
			String strColName)
	{
		try
		{
			if(tabInstanceWrapper != null)
			{
				Map<String, ColumnModel> colMap = tabInstanceWrapper.getColMap();
				if(colMap != null &&
				   colMap.containsKey(strColName))
				{
					String strColWidth = colMap.get(strColName).getWidth();
					if(!StringHelper.IsNullOrEmpty(strColWidth))
					{
						return strColWidth;
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return DEFAULT_COL_WIDTH;
	}

	public static void presistColumnWidth(DynamicGuiInstanceWrapper tabInstanceWrapper) 
	{
		try
		{
			if(tabInstanceWrapper == null)
			{
				return;
			}
			Map<String, Column> pfColMap = tabInstanceWrapper.getPfColMap();
			Map<String, ColumnModel> colMap = tabInstanceWrapper.getColMap();
			if(pfColMap == null)
			{
				return;
			}
			for(Entry<String, Column> kvp : pfColMap.entrySet())
			{
				Column column = kvp.getValue();
				String strColId = column.getField();
				String strWidth = column.getWidth();
				if(colMap.containsKey(strColId))
				{
					ColumnModel columnModel = colMap.get(strColId);
					columnModel.setWidth(strWidth);
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
}
