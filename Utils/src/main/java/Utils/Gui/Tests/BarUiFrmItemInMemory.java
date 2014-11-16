package Utils.Gui.Tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Armadillo.Core.Bar;
import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.UI.AComboList;
import Armadillo.Core.UI.EnumActions;
import Armadillo.Core.UI.LabelClass;
import Utils.Gui.ACustomMenuItem;
import Utils.Gui.Frm.AUiFrmInMemoryItem;
import Utils.Gui.Frm.FrmAction;

public class BarUiFrmItemInMemory extends AUiFrmInMemoryItem
{
	@Override
	public String getReportTitle() 
	{
		return BarUiFrmItemInMemory.class.getName();
	}

	@Override
	public String[] getReportTreeLabels() 
	{
		return new String[] 
				{
				"Test",
				"Bar",
				"BarFrmMemory"
				};
	}

	@Override
	protected Class<?> getParamsClass() 
	{
		return TestParam.class;
	}

	@Override
	protected HashMap<String, Object> generateObjMap() 
	{
		try
		{
			ArrayList<Bar> bars = Bar.getBarList(100);
			HashMap<String, Object> map = new HashMap<String, Object>();
			for(Bar bar : bars)
			{
				map.put(bar.toString(), bar);
			}
			return map;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new HashMap<String, Object>();
	}

	@Override
	public HashMap<EnumActions, FrmAction> generateActionsMap() 
	{
		return null;
	}
	
	@Override
	protected void onGeneratedLabelClasses(
			HashMap<String, LabelClass> labelMap) 
	{
		try
		{
			labelMap.get("m_string").setComboList(new AComboList() 
			{
				@Override
				protected List<String> generateComboItems() 
				{
					List<String> itemsList = new ArrayList<String>();
					itemsList.add("very large text, fix in window????");
					itemsList.add("dos");
					return itemsList;
				}
			});
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	@Override
	public String getObjKey(Object obj) 
	{
		try
		{
			if(obj ==  null)
			{
				return "";
			}
			
			return ((Bar)obj).toString();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
	}

	@Override
	public List<ACustomMenuItem> getMenuItems() 
	{
		List<ACustomMenuItem> menuItems = new ArrayList<ACustomMenuItem>();
		menuItems.add(new ACustomMenuItem() 
		{
			
			@Override
			public String getLabel() 
			{
				return "testLabel";
			}

			@Override
			public void invokeAction(Object callingBean) 
			{
				Console.writeLine("Invoked action");
			}
		});
		return menuItems;
	}
	
}
