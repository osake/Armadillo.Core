package Web.InputData;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.UI.ADynamicBean;
import Armadillo.Core.UI.AUiWorker;
import Armadillo.Core.UI.FrmItem;
import Armadillo.Core.UI.IUiItem;
import Armadillo.Core.UI.LabelClass;
import Armadillo.Core.UI.UiHelper;
import  Utils.Gui.AUiItem;
import  Utils.Gui.AUiParam;
import  Utils.Gui.AUiPhotoAlbumItem;
import  Utils.Gui.AUiTableItem;
import  Utils.Gui.Frm.AUiFrmItem;
import Web.Chart.AUiChartItem;
import Web.Chart.ChartHelper;
import Web.Dashboard.ADashboardBean;
import Web.Dashboard.PhotoAlbumHelper;
import Web.Frm.ADynamicFrmBean;
import Web.Frm.AFrmBean;
import Web.Frm.FrmHelper;
import Web.Table.MyTableHelper;

public abstract class AInputDataDialogBean extends ADynamicFrmBean
{
	private ADynamicBean m_bean;
	private Object m_objParams;
	
	public AInputDataDialogBean()
	{
		try
		{
			String strMessage = "Loaded bean [" + getClass().getName() + "]";
			Console.writeLine(strMessage);
			Logger.log(strMessage);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public void onClose()
	{
		try
		{
			if(m_uiFrmItem == null)
			{
				return;
			}
			
			List<LabelClass> lblClassess = m_uiFrmItem.getLblClassess();
			if(lblClassess == null)
			{
				return;
			}
			
			Console.writeLine("Dialog closed");
			
			Map<String, String> paramsMap = new Hashtable<String, String>();
			for(LabelClass labelClass : lblClassess)
			{
				String strValue = labelClass.getValue();
				if(!StringHelper.IsNullOrEmpty(strValue))
				{
					paramsMap.put(labelClass.getLbl(), strValue);
				}
				else if(labelClass.getIsBooleanType())
				{
					String strVal;
					Object objVal = labelClass.getObjValue();
					if(objVal != null)
					{
						strVal = objVal.toString();
					}
					else
					{
						strVal = "";
					}
					paramsMap.put(labelClass.getLbl(), strVal);
				}
			}
			
			//
			// save params
			//
			InputDataCacheHelper.cacheParams(paramsMap, m_uiFrmItem);
			
			//Object bean = WebHelper.findBean("frmBean");
			if(m_bean == null)
			{
				return;
			}
			
			IUiItem uiItem = m_bean.getUiItem();
			
			if(uiItem != null)
			{
				uiItem.setParamsMap(paramsMap);
			}
			else if(m_objParams != null &&
					AUiItem.class.isAssignableFrom(m_objParams.getClass()))
			{
				AUiItem currUiItem = (AUiItem)m_objParams;
				if(currUiItem != null)
				{
					currUiItem.setParamsMap(paramsMap);
				}
			}
			
			if(AFrmBean.class.isAssignableFrom(m_bean.getClass()))
			{
				String[] treeLabels = m_uiFrmItem.getReportTreeLabels();
				FrmHelper.showDialog(
						m_uiFrmItem,
						"frmDialog",
						":formDialog:myPanel",
						"frmBean",
						"frmWidget",
						false,
						true,
						treeLabels[treeLabels.length - 1]);
				((AFrmBean)m_bean).moveToFirst(null);
			}
			else if(ADashboardBean.class.isAssignableFrom(m_bean.getClass()) &&
					m_objParams != null)
			{
				//
				// we need to regenerate data
				//

				final ADashboardBean dashboardBean = ((ADashboardBean)m_bean);
				final AUiTableItem currUiItem = (AUiTableItem)m_objParams;
				final String strTabName = currUiItem.getTreeKey();
				if(!dashboardBean.existsDynamicGuiInstance(strTabName))
				{
					if(!dashboardBean.createTabComponentsGroup(strTabName, currUiItem))
					{
						return;
					}
				}
				
				AUiWorker uiWorker = new AUiWorker() 
				{
					@Override
					public void Work() 
					{
						try
						{
							if(AUiChartItem.class.isAssignableFrom(currUiItem.getClass()))
							{
								ChartHelper.reloadChart(currUiItem);
							}
							else if(AUiPhotoAlbumItem.class.isAssignableFrom(currUiItem.getClass()))
							{
								PhotoAlbumHelper.reloadPhotoAlbum(currUiItem);
							}
							else
							{
								MyTableHelper.reloadTable(currUiItem);
							}
						}
						catch(Exception ex)
						{
							Logger.log(ex);
						}
					}
				};
				//uiWorker.setBusyNotification(currUiItem.getBusyNotification());
				UiHelper.enqueueGuiTask(uiWorker).waitTask();
				dashboardBean.refreshOnTaskDone(dashboardBean.getFrmName());
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

    public void pressedKey() 
    {
		Console.writeLine("Dialog closed");
    }

	public void onload() 
	{
	}
    
    public List<LabelClass> getLblClassess() 
    {
    	try
    	{
	    	if(m_uiFrmItem == null)
	    	{
	    		return new ArrayList<LabelClass>();
	    	}
	        List<LabelClass> labelClasses = m_uiFrmItem.getLblClassess();
	        Map<String, String> cachedParams =InputDataCacheHelper.getCacheParams(m_uiFrmItem);
	        if(cachedParams != null && cachedParams.size() > 0)
	        {
		        for(LabelClass labelClass : labelClasses)
		        {
		        	String strLabel = labelClass.getLbl();
		        	if(cachedParams.containsKey(strLabel))
		        	{
		        		labelClass.setValue(cachedParams.get(strLabel));
		        	}
		        }
	        }
	        return labelClasses;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ArrayList<LabelClass>();
    }

	@Override
	public FrmItem getCurrItem() 
	{
		return null;
	}

	public void loadUiItem(
			ADynamicBean bean,
			AUiParam uiFrmItem,
			Object objParams) 
	{
		try
		{
			m_bean = bean;
			m_uiFrmItem = uiFrmItem;
			m_objParams = objParams;
			
			if(uiFrmItem == null || bean == null)
			{
				return;
			}
			FrmHelper.showDialog(
					uiFrmItem,
					"editDialog",
					"inputDataDialogBean",
					"editWidget",
					true,
					true);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	@Override
	public AUiFrmItem getFrmItem() 
	{
		return m_uiFrmItem;
	}
}
