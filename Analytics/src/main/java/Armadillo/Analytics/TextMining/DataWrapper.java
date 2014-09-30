package Armadillo.Analytics.TextMining;

import java.util.List;

import Armadillo.Core.Logger;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.Text.TokenWrapper;
import Armadillo.Core.Text.Tokeniser;


public class DataWrapper
{
    private RowWrapper[] m_data;
	private List<String> m_dataList;
	private String[] m_stopWords;
	private int m_intCols;

    public DataWrapper(List<String> dataList)  
	{
	    this(dataList, '\0');
	}
	
    public DataWrapper(List<String> dataList,
                       char charColumnDelimiter)  
    {
    	this(dataList,
             charColumnDelimiter,
             null);
    }

    public DataWrapper(List<String> dataList,
                       char charColumnDelimiter,
                       String[] stopWords)
    {
    	try
        {
	    	m_dataList = dataList;
	    	m_stopWords = stopWords;
	    	
	        if(dataList != null && dataList.size() > 0)
	        {
	        	String strItem = dataList.get(0);
	        	if(!StringHelper.IsNullOrEmpty(strItem))
	        	{
	        		String strDelimiter = (charColumnDelimiter + "").trim();
	        		if(!StringHelper.IsNullOrEmpty(strDelimiter))
	        		{
	        			m_intCols = strItem.split(strDelimiter).length;
	        		}
	        		else
	        		{
	        			m_intCols = 1;
	        		}
			        LoadTokens();
	        	}
	        }
        }
    	catch(Exception ex)
        {
    		Logger.log(ex);
        }
    }

    public DataWrapper(TokenWrapper[] data)
    {
        m_data = new RowWrapper[1];
        m_data[0] = new RowWrapper();
        m_data[0].Columns = new TokenWrapper[1][];
        m_data[0].Columns[0] = data;
    }

    public DataWrapper(RowWrapper[] data)
    {
        m_data = data;
    }

    public int length()
    {
        return m_data.length;
    }

    private void LoadTokens()
    {
    	try
    	{
	        if (m_dataList == null || m_dataList.size() == 0)
	        {
	            m_data = new RowWrapper[0];
	            return;
	        }
	        m_data = new RowWrapper[m_dataList.size()];
	        
	        for (int intRow = 0; intRow < m_dataList.size(); intRow++)
	        {
	            String strLine = m_dataList.get(intRow);
	            m_data[intRow] = getRowWrapper(strLine);
	        }
		}
		catch(Exception ex)
    	{
			Logger.log(ex);
		}
    }
    
    public void replaceRow(String str, int intRow)
    {
    	try
    	{
    		if(m_data == null || m_data.length == 0)
    		{
    			return;
    		}
    		if(StringHelper.IsNullOrEmpty(str))
    		{
    			return;
    		}
    		if(intRow >= m_data.length ||
    				intRow < 0)
    		{
    			return;
    		}
            m_data[intRow] = getRowWrapper(str);
            m_dataList.set(intRow, str);
		}
		catch(Exception ex)
    	{
			Logger.log(ex);
		}
    }

    public void addRow(String str)
    {
    	try
    	{
    		if(StringHelper.IsNullOrEmpty(str))
    		{
    			return;
    		}
    		if(m_data == null ||
    				m_data.length == 0)
    		{
    			m_data = new RowWrapper[1];
    		}
    		else
    		{
    			int intOldLength = m_data.length;
    			RowWrapper[] oldData = m_data;
    			m_data = new RowWrapper[intOldLength + 1];
    			for (int i = 0; i < intOldLength; i++) 
    			{
    				m_data[i] = oldData[i];
				}
    		}
    		m_data[m_data.length - 1] = getRowWrapper(str);
            m_dataList.add(str);
		}
		catch(Exception ex)
    	{
			Logger.log(ex);
		}
    }
    
    public void removeRow(
    		int intRowIndex)
    {
    	try
    	{
    		if(m_data == null ||
    			m_data.length == 0)
    		{
    			return;
    		}
    		if(intRowIndex >= m_data.length ||
    		   intRowIndex < 0)
    		{
    			return;
    		}
    		
			int intOldLength = m_data.length;
			RowWrapper[] oldData = m_data;
			m_data = new RowWrapper[intOldLength - 1];
			int intCounter = 0;
			for (int i = 0; i < intOldLength; i++) 
			{
				if(i != intRowIndex)
				{
					m_data[intCounter] = oldData[i];
					intCounter++;
				}
			}
            m_dataList.remove(intRowIndex);
		}
		catch(Exception ex)
    	{
			Logger.log(ex);
		}
    }
    
	public RowWrapper getRowWrapper(String strLine) 
	{
		try
		{
			RowWrapper rowWrapper = new RowWrapper();
			rowWrapper.Columns = new TokenWrapper[m_intCols][];
			String[] cols = strLine.split(",");
			for (int intCol = 0; intCol < m_intCols; intCol++)
			{
			    String strCol = cols[intCol];
			    TokenWrapper[] tokens = Tokeniser.TokeniseAndWrap(strCol, m_stopWords);
			    rowWrapper.Columns[intCol] = tokens;
			}
			return rowWrapper;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}

	public RowWrapper[] getDataArray() {
		return m_data;
	}

	public void setDataArray(RowWrapper[] data) 
	{
		m_data = data;
	}
}