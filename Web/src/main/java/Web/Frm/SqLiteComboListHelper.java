package Web.Frm;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Core.Logger;
import Armadillo.Core.Cache.SqliteCacheFullSchema;
import  Utils.Gui.AUiItem;
import  Utils.Gui.Frm.AUiFrmSqLiteItem;
import Web.Base.LiveGuiPublisher;

public class SqLiteComboListHelper 
{
	public static List<String> getComboList(
			String strFrmItemName,
			String strColName)
	{
		try
		{
			AUiItem uiItem = LiveGuiPublisher.getOwnInstance().getGuiItems()
					.get(strFrmItemName);
			if(uiItem == null || 
					!(uiItem instanceof AUiFrmSqLiteItem))
			{
				return new ArrayList<String>();
			}
			SqliteCacheFullSchema<?> db = ((AUiFrmSqLiteItem)uiItem).getDb();
			String strTableName = db.getTableName();
			
			String strSql = "SELECT " + strColName + " FROM " + strTableName;
			ArrayList<Object[]> data = new ArrayList<Object[]>();
			db.execute(strSql, data);
			if(data != null && data.size() > 0)
			{
				List<String> results = new ArrayList<String>();
				for(Object[] row : data)
				{
					results.add(row[0].toString());
				}
				return results;
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ArrayList<String>();
	}
}
