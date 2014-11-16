package Web.Tests;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import Armadillo.Core.FooImage2;
import Armadillo.Core.Logger;
import Armadillo.Core.Cache.SqliteCacheFullSchema;
import Armadillo.Core.UI.TableRow;
import  Utils.Gui.AUiTableItem;
import  Utils.Gui.Tests.FooImageUiFrmItemSqLite;
import Web.Table.MyTableHelper;

public class FooImageUiTableItem extends AUiTableItem
{
	@Override
	public List<TableRow> generateTableRows() 
	{
		try
		{
			SqliteCacheFullSchema<FooImage2> db = FooImageUiFrmItemSqLite.generateDbStatic();
			FooImage2[] fooExtendedArr = db.loadAllData();
			ArrayList<FooImage2> list = new ArrayList<FooImage2>();
			for (int i = 0; i < fooExtendedArr.length; i++) 
			{
				list.add(fooExtendedArr[i]);
			}
			return MyTableHelper.generateTableRowListFromRawObjects(list);
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
		return "test";
	}

	@Override
	public String[] getReportTreeLabels() 
	{
		return new String[]
		{
				"Test",
				"Foo",
				"FooImageTable"
		};
	}

	@Override
	public Type getType() 
	{
		return FooImage2.class;
	}
}
