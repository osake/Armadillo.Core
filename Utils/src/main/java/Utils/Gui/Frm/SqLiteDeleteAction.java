package Utils.Gui.Frm;

import java.util.List;

import Armadillo.Analytics.TextMining.Searcher;
import Armadillo.Core.Logger;
import Armadillo.Core.Cache.SqliteCacheFullSchema;
import Armadillo.Core.Text.StringHelper;

public class SqLiteDeleteAction<T> extends FrmAction 
{
	private SqliteCacheFullSchema<T> m_db;
	
	public SqLiteDeleteAction(SqliteCacheFullSchema<T> db) 
	{
		m_db = db;
	}
	
	@Override
	public boolean invokeAction(
			AFrmBeanBase frmBean,
			Object objParams) 
	{
		try
		{
			if(frmBean == null)
			{
				return false;
			}
			
			AUiFrmItem frmItem = frmBean.getFrmItem();
			if(frmItem == null)
			{
				return false;
			}
			
			Object obj = frmItem.createNewInstanceItem();
			if(obj == null)
			{
				return false;
			}
			String strKey = frmItem.getCurrKey();
			//String strKey = frmItem.getObjKey(obj);
			if(StringHelper.IsNullOrEmpty(strKey))
			{
				return false;
			}
			SqliteCacheFullSchema<?> db = m_db;
			if(db == null)
			{
				return false;
			}
			if(db.containsKey(strKey))
			{
				db.delete(strKey);
			
				int intIndex = frmItem.getIndex();
				if(intIndex >= 0 && 
				   intIndex <= frmItem.getSize())
				{
					Searcher searcher = frmItem.getSearcher();
					if(searcher != null)
					{
						searcher.removeRow(intIndex);
					}
				}
				List<String> keys = frmItem.getKeys();
				keys.remove(strKey);

				frmItem.moveToIndex(intIndex);
				return true;
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return false;
	}
}