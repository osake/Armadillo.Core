package Utils.Gui.Frm;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.joda.time.DateTime;

import Armadillo.Core.Logger;
import Armadillo.Core.ParserHelper;
import Armadillo.Core.Reflection.Reflector;
import Armadillo.Core.UI.EnumActions;
import Armadillo.Core.UI.FrmItem;
import Armadillo.Core.UI.LabelClass;
import Armadillo.Core.UI.UiHelper;
import Utils.Gui.ACustomMenuItem;
import Utils.Gui.AUiItem;

public abstract class AUiFrmItem extends AUiItem
{
	private int m_intIndex;
    private List<LabelClass> m_lblClassess;
	private HashMap<String, LabelClass> m_labelClassesMap;
	private String[] m_colNames;
	private HashMap<EnumActions, FrmAction> m_actionsMap;
	public Reflector m_reflector;
	
	public abstract HashMap<EnumActions, FrmAction> generateActionsMap();
	public abstract void moveToNext();
	public abstract void moveToPrev();
	public abstract void moveToLast();
	public abstract void moveToIndex(int intIndex);
	public abstract int getSize();
	public abstract Reflector getReflector();
	public abstract boolean save(Object callingBean);
	public abstract boolean delete(Object callingBean);
	public abstract void moveToFirst();
	public abstract FrmItem getCurrFrmItem();
	public abstract String getObjKey(Object obj);
	public abstract List<ACustomMenuItem> getMenuItems();
	
	public FrmAction getFrmAction(EnumActions enumActions) 
	{
		try
		{
			if(m_actionsMap == null)
			{
				return null;
			}
			if(m_actionsMap.containsKey(enumActions))
			{
				return m_actionsMap.get(enumActions);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}

	
    private void generateLblClassess()
    {
    	try
    	{
    		String[] colNames = getColNames();
    		Reflector reflector = getReflector();
    		
    		if(colNames == null ||
    				reflector == null)
    		{
    			return;
    		}
    		HashMap<String, LabelClass> labelClassesMap = new HashMap<String, LabelClass>();
    		ArrayList<LabelClass> lblClassess = new ArrayList<LabelClass>();
	    	UiHelper.doLoadColumnItems(
	    			reflector, 
	    			labelClassesMap,
					lblClassess);
	    	m_labelClassesMap = labelClassesMap;
	    	m_lblClassess = lblClassess;
	    	
	    	onGeneratedLabelClasses(m_labelClassesMap);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
    }
    
    protected void resetLabels()
    {
    	try
    	{
    		if(m_labelClassesMap != null)
    		{
    			m_labelClassesMap.clear();
    	    	m_labelClassesMap = null;
    		}
    		if(m_lblClassess != null)
    		{
		    	m_lblClassess.clear();
		    	m_lblClassess = null;
    		}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
    }
    
	protected void onGeneratedLabelClasses(
			HashMap<String, LabelClass> labelMap) 
	{
		// this method is to be overridden by implementations
	}

	public List<LabelClass> getLblClassess() 
	{
		try
		{
			if(m_lblClassess == null)
			{
				synchronized(m_lockObj)
				{
					if(m_lblClassess == null)
					{
						generateLblClassess();
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_lblClassess;
	}

	public HashMap<String, LabelClass> getLabelMap() 
	{
		try
		{
			if(m_labelClassesMap == null)
			{
				synchronized(m_lockObj)
				{
					if(m_labelClassesMap == null)
					{
						getLblClassess();
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_labelClassesMap;
	}


	public String[] getColNames() 
	{
		try
		{
			if(m_colNames == null)
			{
				synchronized(m_lockObj)
				{
					if(m_colNames == null)
					{
						Reflector reflector = getReflector();
						if(reflector == null)
						{
							return null;
						}
				    	m_colNames = reflector.getColNames();
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_colNames;
	}
	
	public List<String> getRowsToExclude()
	{
		//
		// to be overriden
		//
		return new ArrayList<String>();
	}

	public HashMap<EnumActions, FrmAction> getActionsMap() 
	{
		try
		{
			if(m_actionsMap == null)
			{
				synchronized(m_lockObj)
				{
					if(m_actionsMap == null)
					{
						m_actionsMap = generateActionsMap();
					}
				}
			}
			
			return m_actionsMap;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new HashMap<EnumActions, FrmAction>();
	}

	public void setLblClassess(List<LabelClass> lblClassess) 
	{
		m_lblClassess = lblClassess;
	}

	protected void publishObject(FrmItem frmItem) 
	{
		try
		{
			if(frmItem == null ||
			   frmItem.getObj() == null)
			{
				return;
			}
			Object obj = frmItem.getObj();
			String[] colNames = getColNames();
			Reflector reflector = getReflector();
			HashMap<String, LabelClass> labelMap = getLabelMap();
			
			if(obj == null ||
				colNames == null ||
				reflector == null ||
				labelMap == null)
			{
				return;
			}
			
			for(String strPropName : colNames)
			{
				LabelClass labelClass = labelMap.get(strPropName);
				Object objVal = reflector.getPropValue(obj, strPropName);
				String strVal = "";
				
				if(labelClass.getIsDateType() ||
						(objVal != null && (objVal.getClass() == Date.class ||
								objVal.getClass() == DateTime.class)))
				{
					if(objVal != null)
					{
						if(objVal.getClass() == DateTime.class)
						{
							strVal = ((DateTime)objVal).toString();
						}
						else
						{
							strVal = new DateTime((Date)objVal).toString();
						}
					}
					else
					{
						strVal = new DateTime().toString();
					}
				}
				else if(labelClass.getIsImageType() ||
						labelClass.getIsBooleanType())
				{
					labelClass.setObjValue(objVal);
				}
				else if(objVal != null)
				{
					strVal = objVal.toString();
				}
				
				labelClass.setValue(strVal);
			}
		}
		catch(Exception ex){
			Logger.log(ex);
		}
	}
	
	public Object createNewInstanceItem() 
	{
    	try
    	{
	    	Object instance = getReflector().createInstance();
	    	setPropertyValues(instance);
	    	return instance;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
		return null;
	}

	protected void setPropertyValues(Object instance) 
	{
		try
		{
    		Reflector reflector = getReflector();
    		
    		HashMap<String, LabelClass> labelMap = getLabelMap();
    		
			for(Entry<String, LabelClass> kvp : labelMap.entrySet())
			{
				
				LabelClass labelClass = kvp.getValue();
				
				String strProp = kvp.getKey();
				Type propType = reflector.getPropertyType(strProp);
				Object objVal;
				if(labelClass.getIsImageType() || 
						labelClass.getIsBooleanType())
				{
					objVal = labelClass.getObjValue();
				}
				else
				{
					objVal = ParserHelper.ParseString(
							kvp.getValue().getValue(), 
							propType);
				}
				reflector.SetPropertyValue(
						 instance,
						 strProp,
						 objVal);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	

//	public void setIndex(int intIndex) 
//	{
//		m_intIndex = intIndex;
//	}

	public void moveToCurrent() 
	{
		moveToIndex(getIndex());
	}
	public String getCurrKey() 
	{
		try
		{
			List<String> keys = getKeys();
			if(getIndex() < 0 || 
					keys == null || 
					keys.size() == 0)
			{
				return "";
			}
			return m_keys.get(getIndex());
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return "";
	}
	
	public void onClose() 
	{
	}
	
	@Override
	public String toString() 
	{
		try
		{
			String strKey = getTreeKey() + "|" +
					getClass().getName();
			
			return strKey;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return super.toString();
	}
	public int getIndex() 
	{
		return m_intIndex;
	}
	public void setIndex(int intIndex) 
	{
		m_intIndex = intIndex;
	}
}