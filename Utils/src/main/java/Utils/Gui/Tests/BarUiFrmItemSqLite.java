package Utils.Gui.Tests;

import Armadillo.Core.Bar;
import Armadillo.Core.Cache.SqliteCacheFullSchema;
import Utils.Gui.Frm.AUiFrmSqLiteItem;

public class BarUiFrmItemSqLite extends AUiFrmSqLiteItem
{

	public BarUiFrmItemSqLite() 
	{
		super(Bar.class);
	}


	@Override
	public String[] getReportTreeLabels() 
	{
		return new String[] 
				{
					"Test",
					"Bar",
					"BarFrmSqLite"
				};
	}


	@Override
	protected SqliteCacheFullSchema<?> generateDb() 
	{
		return new SqliteCacheFullSchema<Bar>(
				m_strDbFileName,
				Bar.class);
	}
}
