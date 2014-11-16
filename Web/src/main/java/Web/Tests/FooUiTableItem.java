package Web.Tests;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Core.Foo;
import Armadillo.Core.Logger;
import Armadillo.Core.Reflection.ReflectionCache;
import Armadillo.Core.Reflection.Reflector;
import Armadillo.Core.UI.TableRow;
import Armadillo.Core.UI.UiHelper;
import  Utils.Gui.AUiTableItem;
import Web.Table.MyTableHelper;

public class FooUiTableItem extends AUiTableItem
{
	private static Reflector m_reflector;

	static
	{
		try
		{
			m_reflector = ReflectionCache.getReflector(Foo.class);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	@Override
	public List<TableRow> generateTableRows() 
	{
		try
		{
			Thread.sleep(5000);
			ArrayList<Foo> foos = Foo.getFooList(100, false);
			return MyTableHelper.generateTableRowListFromRawObjects(foos);
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
		return new String[]{
				"Test",
				"Foo",
				"FooTable"
		};
	}

	@Override
	public String[] getFieldNames() 
	{
		return m_reflector.getColNames();
	}

	@Override
	protected Class<?> getParamsClass() 
	{
		return null;
	}

	@Override
	protected String getObjKey(TableRow tableRow) 
	{
		return UiHelper.getStdKey(tableRow);
	}
}
