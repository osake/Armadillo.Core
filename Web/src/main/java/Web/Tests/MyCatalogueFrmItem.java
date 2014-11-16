package Web.Tests;

import Armadillo.Core.Cache.SqliteCacheFullSchema;
import  Utils.Gui.Frm.AUiFrmSqLiteItem;
import Web.Catalogue.CatalogueItem;

public class MyCatalogueFrmItem extends AUiFrmSqLiteItem
{
	public MyCatalogueFrmItem() 
	{
		super(CatalogueItem.class);
	}

	@Override
	protected SqliteCacheFullSchema<?> generateDb() 
	{
		return MyCathalogueTableItem.m_db;
	}
	
	@Override
	public String getObjKey(Object obj) 
	{
		return ((CatalogueItem)obj).Id;
	}

	@Override
	public String[] getReportTreeLabels() 
	{
		return new String[]
		{
				"Test",
				"Test",
				"CatalogueTestFrm"
		};
	}

}
