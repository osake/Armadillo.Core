package Utils.Gui.Frm;

import java.util.List;

import Armadillo.Analytics.TextMining.Searcher;
import Armadillo.Core.Logger;
import Armadillo.Core.Cache.SqliteCacheFullSchema;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.UI.FrmItem;

public class SqLiteSaveAction<T> extends FrmAction 
{
	private SqliteCacheFullSchema<T> m_db;
	
	public SqLiteSaveAction(SqliteCacheFullSchema<T> db) 
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
			int intIndex = frmBean.getFrmItem().getIndex();
			boolean blnIsReplacement = intIndex >= 0;
			String strOldKey = "";
			if(blnIsReplacement)
			{
				FrmItem oldFrmItem = frmBean.getCurrItem();
				if(oldFrmItem == null)
				{
					blnIsReplacement = false;
				}
				else
				{
					strOldKey = frmBean.getFrmItem().getCurrKey();
				}
			}
			
			Object obj = frmBean.getFrmItem().createNewInstanceItem();
			String strNewKey = frmBean.getFrmItem().getObjKey(obj);
			
			if(StringHelper.IsNullOrEmpty(strNewKey))
			{
				return false;
			}
			SqliteCacheFullSchema<?> db = m_db;
			if(db == null)
			{
				return false;
			}
			//
			// remove old key
			//
			List<String> keys = frmBean.getFrmItem().getKeys();
			Searcher searcher = frmBean.getFrmItem().getSearcher();
			if(blnIsReplacement &&
			   !StringHelper.IsNullOrEmpty(strOldKey))
			{
				if(db.containsKey(strOldKey))
				{
					db.delete(strOldKey);
				}
				if(keys != null)
				{
					//
					// replace key
					//
					keys.set(intIndex, strNewKey);
					if(searcher != null)
					{
						searcher.replaceRow(strNewKey, intIndex);
					}
				}
			}
			else
			{
				//
				// add key
				//
				keys.add(strNewKey);
				if(searcher != null)
				{
					searcher.addRow(strNewKey);
				}
				frmBean.getFrmItem().setIndex(keys.size() - 1);
			}
			
			if(db.containsKey(strNewKey))
			{
				db.delete(strNewKey);
			}
			db.insert(obj, strNewKey).waitTask();
			
			
			return true;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return false;
	}
}
