package Utils.Gui;

import java.util.HashMap;
import java.util.List;

import Armadillo.Analytics.TextMining.Searcher;
import Armadillo.Core.Logger;
import Armadillo.Core.Reflection.ReflectionCache;
import Armadillo.Core.Reflection.Reflector;
import Armadillo.Core.UI.EnumActions;
import Armadillo.Core.UI.FrmItem;
import Utils.Gui.Frm.AUiFrmItem;
import Utils.Gui.Frm.FrmAction;

public abstract class AUiParam extends AUiFrmItem 
{
	@Override
	public HashMap<EnumActions, FrmAction> generateActionsMap() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void moveToNext() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveToPrev() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveToLast() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveToIndex(int intIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Reflector getReflector() 
	{
		try
		{
			return ReflectionCache.getReflector(getClass());
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}

	@Override
	public boolean save(Object callingBean) {
		// TODO Auto-generated method stub
		return true;
		
	}

	@Override
	public boolean delete(Object callingBean) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void moveToFirst() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FrmItem getCurrFrmItem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getObjKey(Object obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getReportTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getReportTreeLabels() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Class<?> getParamsClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Searcher generateSearcher() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<String> generateKeys() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<ACustomMenuItem> getMenuItems() {
		// TODO Auto-generated method stub
		return null;
	}
}
