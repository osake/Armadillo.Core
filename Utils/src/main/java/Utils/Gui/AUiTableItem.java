package Utils.Gui;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import Armadillo.Analytics.TextMining.DataWrapper;
import Armadillo.Analytics.TextMining.Searcher;
import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.Reflection.ReflectionCache;
import Armadillo.Core.Reflection.Reflector;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.UI.ColumnModel;
import Armadillo.Core.UI.TableRow;
import Armadillo.Core.UI.UiHelper;

public abstract class AUiTableItem extends AUiItem 
{
	protected List<TableRow> m_tableRows;
	protected Collection<TableRow> m_filteredTableRows;
	private List<ColumnModel> m_columns;
	private ConcurrentMap<String, TableRow> m_rowsMap;
	private Class<? extends Object> m_type;
	private Reflector m_reflector;
	private Object m_lockObject = new Object();
	
	public abstract List<TableRow> generateTableRows();
	
	protected Class<?> getParamsClass() 
	{
		return null;
	}
	
	protected String getObjKey(TableRow tableRow) 
	{
		return UiHelper.getStdKey(tableRow);
	}
	
	public String[] getFieldNames()
	{
		try
		{
			Reflector reflector = getReflector();
			if(reflector != null)
			{
				return reflector.getColNames();
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
	
	public Type[] getFieldTypes()
	{
		try
		{
			Reflector reflector = getReflector();
			if(reflector != null)
			{
				return reflector.getColTypes();
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}
	
	public Reflector getReflector()
	{
		try
		{
			if(m_reflector == null)
			{
				synchronized(m_lockObject)
				{
					if(m_reflector == null)
					{
						Type type = getType();
						if(type != null)
						{
							m_reflector = ReflectionCache.getReflector((Class<?>) type);
						}
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_reflector;
	}
	
	public Type getType()
	{
		return null;
	}
	
	private ConcurrentMap<String, TableRow> generateRowsMap()
	{
		try
		{
			Collection<TableRow> tableRows = getTableRows();
			if(tableRows == null || tableRows.size() == 0)
			{
				return new ConcurrentHashMap<String, TableRow>();
			}
			ConcurrentHashMap<String, TableRow> map = new ConcurrentHashMap<String, TableRow>();
			for(TableRow tableRow : tableRows)
			{
				String strKey = tableRow.getKey();
				map.put(strKey, tableRow);
			}
			return map;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ConcurrentHashMap<String, TableRow>();
	}

	public void setTableRows(Collection<TableRow> tableRows) 
	{
		if(tableRows == null)
		{
			m_tableRows = null;
		}
		else
		{
			m_tableRows = new ArrayList<TableRow>(tableRows);
		}
	}

	public ConcurrentMap<String, TableRow> getRowsMap() 
	{
		try
		{
			if(m_rowsMap == null) 
			{
				synchronized(m_lockObj)
				{
					if(m_rowsMap == null) 
					{
//						AUiWorker uiWorker = new AUiWorker() 
//						{
//							@Override
//							public void Work() 
//							{
//								m_rowsMap = generateRowsMap();
//							}
//						};
//						UiHelper.enqueueGuiTask(uiWorker);
						
						m_rowsMap = generateRowsMap();
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_rowsMap;
	}

	public Collection<TableRow> getTableRows() 
	{
		try
		{
			if(m_tableRows == null) 
			{
				synchronized(m_lockObj)
				{
					if(m_tableRows == null) 
					{
						m_tableRows = generateTableRows();
						if(m_tableRows == null)
						{
							//
							// avoid generating empty results multiple times
							//
							m_tableRows = new ArrayList<TableRow>(); 
						}
					}
				}
			}
			return m_tableRows;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ArrayList<TableRow>();
	}
	
	@Override
	protected Searcher generateSearcher() 
	{
		try
		{
			List<String> keys = getKeys();
			
			if(keys == null || keys.size() == 0)
			{
				return null;
			}
			
			DataWrapper dataWrapper = new DataWrapper(keys);
			m_searcher = new Searcher(dataWrapper);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return m_searcher;
	}

	@Override
	protected List<String> generateKeys() 
	{
		try
		{
			// TODO, generate this async
			Collection<TableRow> tableRows = getTableRows();
			if(tableRows == null)
			{
				return new ArrayList<String>();
			}
			List<String[]> dataList = UiHelper.generateStringList(
					tableRows, 
					m_columns.size());
			
			if(dataList == null || 
			   dataList.size() == 0)
			{
				return new ArrayList<String>();
			}
			
			List<String> keys = new ArrayList<String>();
			
			for(TableRow obj : tableRows)
			{
				String strKey = getObjKey(obj);
				keys.add(strKey);
			}
			return keys;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return new ArrayList<String>();
	}
	
	public Collection<TableRow> getFilteredTableRows() 
	{
		return m_filteredTableRows;
	}
	
	public void setFilteredTableRows(Collection<TableRow> filteredTableRows) 
	{
		m_filteredTableRows = filteredTableRows;
	}
	
	public List<ColumnModel> getColumns() 
	{
		return m_columns;
	}
	
	public void setColumns(List<ColumnModel> columns) 
	{
		m_columns = columns;
	}
	
	public int getColCount()
	{
		try
		{
			if(m_columns == null)
			{
				return 0;
			}
			return m_columns.size();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return 0;
	}
	
	public void populateTableRow(
			String strKey, 
			Object obj)
	{
		try
		{
			if(obj == null || StringHelper.IsNullOrEmpty(strKey))
			{
				return;
			}
			if(m_type == null)
			{
				synchronized(m_lockObj)
				{
					if(m_type == null)
					{
						m_type = obj.getClass();
						m_reflector = ReflectionCache.getReflector(m_type); 
					}
				}
			}
			if(obj.getClass() != m_type)
			{
				return;
			}
			
			if(m_rowsMap == null)
			{
				m_rowsMap = new ConcurrentHashMap<String, TableRow>(); 
			}
			if(m_tableRows == null)
			{
				m_tableRows = new ArrayList<TableRow>();
			}
			synchronized(m_lockObj)
			{
				if(m_rowsMap.containsKey(strKey))
				{
					//
					// replace table row
					//
					TableRow tableRow = m_rowsMap.get(strKey);
					UiHelper.replaceValues(obj, tableRow, m_reflector);
					tableRow.setKey(strKey);
				}
				else
				{
					//
					// generate new table row
					//
					TableRow tableRow = UiHelper.generateTableRow(m_reflector, obj, strKey);
					m_tableRows.add(tableRow);
					m_rowsMap.put(strKey, tableRow);
				}
			}
			m_blnHasChanged = true;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public void populateTableRow(
			String strKey, 
			String[] vals,
			String[] cols)
	{
		try
		{
			if(vals == null || StringHelper.IsNullOrEmpty(strKey))
			{
				return;
			}
			
			
			if(m_rowsMap == null)
			{
				m_rowsMap = new ConcurrentHashMap<String, TableRow>(); 
			}
			if(m_tableRows == null)
			{
				m_tableRows = new ArrayList<TableRow>();
			}
			if(m_rowsMap.containsKey(strKey))
			{
				//
				// replace table row
				//
				TableRow tableRow = m_rowsMap.get(strKey);
				UiHelper.replaceValues(vals, tableRow, m_reflector);
				checkTableRows(tableRow);
				tableRow.setKey(strKey);
			}
			else
			{
				//
				// generate new table row
				//
				TableRow tableRow = UiHelper.generateTableRow(m_reflector, vals, strKey);
				checkTableRows(tableRow);
				m_tableRows.add(tableRow);
				m_rowsMap.put(strKey, tableRow);
			}
			
			m_blnHasChanged = true;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	private void checkTableRows(TableRow tableRow) {
		String[] rows = tableRow.getRows();
		boolean blnFound = false;
		for (int i = 0; i < rows.length; i++) 
		{
			if(!StringHelper.IsNullOrEmpty(rows[i]))
			{
				blnFound = true;
				break;
			}
		}
		if(!blnFound)
		{
			Console.writeLine("Not found");
		}
	}
	
}
