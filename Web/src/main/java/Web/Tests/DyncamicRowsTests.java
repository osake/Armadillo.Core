package Web.Tests;

import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Core.UI.TableRow;
import Web.Dashboard.ADashboardBean;
import Web.Dashboard.DynamicGuiInstanceWrapper;

public class DyncamicRowsTests 
{
	private int intCounter = 0;

	public void testDynamicRows(final DynamicGuiInstanceWrapper m_tabInstanceWrapper,
			final ADashboardBean dashboardBean) 
	{
		ThreadWorker<ObjectWrapper> worker = new ThreadWorker<ObjectWrapper>() 
		{
			@Override
			public void runTask(ObjectWrapper item) 
			{
				while (true) 
				{
					try 
					{
						if (m_tabInstanceWrapper != null
								&& m_tabInstanceWrapper.getUiTableItem().getTableRows() != null
								&& m_tabInstanceWrapper.getUiTableItem().getTableRows().size() > 0) 
						{
							for (TableRow row : m_tabInstanceWrapper.getUiTableItem().getTableRows()) 
							{
								row.setCol1("Counter " + intCounter);
							}
						}
						intCounter++;
						dashboardBean.setHasChaged(true);
						Thread.sleep(5000);
					} 
					catch (Exception ex) 
					{
						Logger.log(ex);
					}
				}
			}
		};
		worker.work();
	}	
}
