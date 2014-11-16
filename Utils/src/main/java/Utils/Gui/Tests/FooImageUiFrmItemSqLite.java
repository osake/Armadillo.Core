package Utils.Gui.Tests;

import Armadillo.Core.FooImage2;
import Armadillo.Core.Cache.SqliteCacheFullSchema;
import Utils.Gui.Frm.AUiFrmSqLiteItem;

public class FooImageUiFrmItemSqLite extends AUiFrmSqLiteItem
{
	public FooImageUiFrmItemSqLite() 
	{
		super(FooImage2.class);
	}
	
	@Override
	public String[] getReportTreeLabels() 
	{
		return new String[] 
				{
					"Test",
					"Foo",
					"FooImageFrm"
				};
	}

	public static SqliteCacheFullSchema<FooImage2> generateDbStatic() 
	{
		String strDbFileName = AUiFrmSqLiteItem.getDbFileName(FooImage2.class);
		return new SqliteCacheFullSchema<FooImage2>(
				strDbFileName,
				FooImage2.class);
	}
	
	@Override
	protected SqliteCacheFullSchema<?> generateDb() 
	{
		return generateDbStatic();
	}
	
	@Override
	public String getObjKey(Object obj) 
	{
		if(obj == null)
		{
			return "";
		}
		return ((FooImage2)obj).toString();
	}
}
