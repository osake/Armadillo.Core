package Utils.Gui.Frm;

import Armadillo.Core.UI.ADynamicBean;
import Armadillo.Core.UI.FrmItem;
import Utils.Gui.AUiItem;

public abstract class AFrmBeanBase extends ADynamicBean
{
	protected AUiFrmItem m_uiFrmItem;
	
	public abstract FrmItem getCurrItem();
	public abstract AUiFrmItem getFrmItem();
	
	@Override
	public AUiItem getUiItem()
	{
		return m_uiFrmItem;
	}
	
}
