package Web.Tests;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Armadillo.Core.Logger;
import Armadillo.Core.Cache.SqliteCacheFullSchema;
import Armadillo.Core.Io.PathHelper;
import Armadillo.Core.UI.TableRow;
import Web.Catalogue.AUiCatalogueTableItem;
import Web.Catalogue.CatalogueItem;
import Web.Table.MyTableHelper;

public class MyCathalogueTableItem extends AUiCatalogueTableItem
{
	private static String DB_FILE_NAME;
	public static SqliteCacheFullSchema<CatalogueItem> m_db;
	
	static
	{
		try
		{
			DB_FILE_NAME = PathHelper.combinePaths(
					"C:/HC.Java.HC/Data", "Catalogue2.db");
			m_db = new SqliteCacheFullSchema<CatalogueItem>(
					DB_FILE_NAME,
					CatalogueItem.class);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public static SqliteCacheFullSchema<?> getDbStatic()
	{
		return m_db;
	}

	@Override
	public List<TableRow> generateTableRows() 
	{
		try
		{
			CatalogueItem[] arr = m_db.loadAllData();
			return MyTableHelper.generateTableRowListFromRawObjects(arr);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ArrayList<TableRow>();
	}

	@Override
	public String getReportTitle() 
	{
		return null;
	}

	@Override
	public String[] getReportTreeLabels() 
	{
		return new String[]
				{
					"Test",
					"Test",
					"CatalogueTestTable2"
				};
	}
	
	@Override
	public Type getType()
	{
		return CatalogueItem.class;
	}

	@Override
	public int getRowCount() {
		return m_db.getSize();
	}
	
}
