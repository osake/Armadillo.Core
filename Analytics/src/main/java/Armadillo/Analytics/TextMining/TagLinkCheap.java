package Armadillo.Analytics.TextMining;

import Armadillo.Core.Logger;
import Armadillo.Core.Text.TokenWrapper;
import Armadillo.Core.Text.Tokeniser;

public class TagLinkCheap implements IStringMetric
{
    private  TagLinkCheapToken m_tagLinkCheapToken;
    private  TokenStatistics m_tokenStatistics;
    private  double m_dblTokenThreshold;
    private  int m_intColumnCount;
    private  int m_intLimit;
    private  int m_intW;
    private  DataWrapper m_strDataArray;

    public TagLinkCheap(
        int limit)  { 
    	
    	this(null,
                limit,
                null);    	
    }

    public TagLinkCheap(
            int intLimit,
            double dblTokenThrehold) 
    { 
    	this(null, intLimit, null, dblTokenThrehold);
    }
    
    public TagLinkCheap(
            DataWrapper rowObjectArray,
            int limit,
            TokenStatistics tokenStatistics,
            double dblTokenThrehold) 
    { 
		this(rowObjectArray,
	        limit,
	        TextMiningConstants.CHEAP_LINK_WINDOW,
	        tokenStatistics,
	        dblTokenThrehold);
    }
    
    public TagLinkCheap(
        DataWrapper rowObjectArray,
        int limit,
        TokenStatistics tokenStatistics) { 
	    	this(rowObjectArray,
	                limit,
	                TextMiningConstants.CHEAP_LINK_WINDOW,
	                tokenStatistics);    	
    }

    public TagLinkCheap(
            DataWrapper rowObjectArray,
            int limit,
            int w,
            TokenStatistics tokenStatistics)
        {
    		this(
    	        rowObjectArray,
    	        limit,
    	        w,
    	        tokenStatistics,
    	        TextMiningConstants.DBL_CHEAP_LINK_TOKEN_THRESHOLD);
        }
    
    public TagLinkCheap(
        DataWrapper rowObjectArray,
        int limit,
        int w,
        TokenStatistics tokenStatistics,
        double dblTokenThreshold)
    {
    	try
    	{
	        m_dblTokenThreshold = dblTokenThreshold;
	        m_tokenStatistics = tokenStatistics;
	        m_intW = w;
	        m_strDataArray = rowObjectArray;
	        if(rowObjectArray == null)
	        {
	        	m_intColumnCount = 1;
	        }
	        else
	        {
	        	m_intColumnCount = rowObjectArray.getDataArray()[0].Columns.length;
	        }
	        m_tagLinkCheapToken = new TagLinkCheapToken();
	        m_intLimit = limit;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    public double GetStringMetric(
            String strT,
            String strU)
        {
        	try
        	{
        		TokenWrapper[][] tTokens = new TokenWrapper[1][];
        		tTokens[0] = Tokeniser.TokeniseAndWrap(strT);
        		TokenWrapper[][] uTokens = new TokenWrapper[1][];
        		uTokens[0] = Tokeniser.TokeniseAndWrap(strU);
        		return GetStringMetric(tTokens, uTokens);
        	}
        	catch(Exception ex){
        		Logger.log(ex);
        	}
        	return 0;
        }
    
    public double GetStringMetric(int rowT, int rowU)
    {
        return GetStringMetric(rowT, rowU, 0, m_intColumnCount);
    }

    public double GetStringMetric(
        int rowT, 
        int rowU, 
        int columnIndex, 
        int totalColumns)
    {
        if (m_strDataArray == null)
        {
            return 0.0;
        }
        TokenWrapper[][] tTokens = m_strDataArray.getDataArray()[rowT].Columns;
        TokenWrapper[][] uTokens = m_strDataArray.getDataArray()[rowU].Columns;
        double[][] tIdfArray = null;
        double[][] uIdfArray = null;
        if (m_tokenStatistics != null)
        {
            tIdfArray = m_tokenStatistics.GetIdfArray(rowT);
            uIdfArray = m_tokenStatistics.GetIdfArray(rowU);
        }
        return GetStringMetric(
            columnIndex,
            totalColumns,
            tTokens,
            uTokens,
            tIdfArray,
            uIdfArray);
    }

    public double GetStringMetric(
        TokenWrapper[][] tTokens,
        TokenWrapper[][] uTokens)
    {
        double[][] tIdfArray;
        double[][] uIdfArray;
		if(m_tokenStatistics != null)
		{
	        tIdfArray = m_tokenStatistics.GetIdfArray(tTokens);
	        uIdfArray = m_tokenStatistics.GetIdfArray(uTokens);
		}
		else{
	        tIdfArray = TokenStatistics.GetDummyIdfArray(tTokens);
	        uIdfArray = TokenStatistics.GetDummyIdfArray(uTokens);
		}
    	
        return GetStringMetric(0,
            m_intColumnCount,
            tTokens,
            uTokens,
            tIdfArray,
            uIdfArray);
    }

    public double GetStringMetric(TokenWrapper[][] tTokens, int rowU)
    {
        TokenWrapper[][] uTokens = m_strDataArray.getDataArray()[rowU].Columns;
        double[][] tIdfArray = m_tokenStatistics.GetIdfArray(tTokens);
        double[][] uIdfArray = m_tokenStatistics.GetIdfArray(rowU);
        return GetStringMetric(0, 
            m_intColumnCount, 
            tTokens, 
            uTokens, 
            tIdfArray, 
            uIdfArray);
    }

    public double GetStringMetric(
        int columnIndex,
        int totalColumns,
        TokenWrapper[][] tTokens,
        TokenWrapper[][] uTokens,
        double[][] tIdfArray,
        double[][] uIdfArray)
    {
    	try
    	{
	        double stringMetric = 0.0;
	        for (int column = columnIndex; column < totalColumns; column++)
	        {
	            int highLimit = Math.min(tTokens[column].length, m_intLimit);
	            for (int i = 0; i < highLimit; i++)
	            {
	                TokenWrapper actualTToken = tTokens[column][i];
	                double dblMaxTokenStringMetric = 0.0;
	                int intMaxIndex = -1;
	                int intLowLimit2 = Math.max(0, i - m_intW);
	                int intHighLimit2 = Math.min(uTokens[column].length, m_intLimit);
	                
	                for (int j = intLowLimit2; j < intHighLimit2; j++)
	                {
	                    double dblActualTokenStringMetric;
	                    TokenWrapper actualUToken = uTokens[column][j];
	                    if (actualTToken.equals(actualUToken))
	                    {
	                        dblActualTokenStringMetric = 1.0;
	                        dblMaxTokenStringMetric = dblActualTokenStringMetric;
	                        intMaxIndex = j;
	                        break;
	                    }
	                    double dblCurrTokenMetric = TagLinkPrefix.GetStringMetric(
	                            actualTToken,
	                            actualUToken);
	                    if (dblCurrTokenMetric > 0)
	                    {
	
	                        dblActualTokenStringMetric =
	                            m_tagLinkCheapToken.GetStringMetric(
	                                actualTToken,
	                                actualUToken);
	                        
	                        if (dblActualTokenStringMetric > m_dblTokenThreshold &&
	                            dblMaxTokenStringMetric < dblActualTokenStringMetric)
	                        {
	                            dblMaxTokenStringMetric = dblActualTokenStringMetric;
	                            intMaxIndex = j;
	                            if (dblActualTokenStringMetric == 1.0)
	                            {
	                                break;
	                            }
	                        }
	                    }
	                }
	                if (intMaxIndex >= 0)
	                {
	                    double tfidfWeight =
	                        Math.max(tIdfArray[column][i],
	                                 uIdfArray[column][intMaxIndex]);
	                    stringMetric +=
	                        dblMaxTokenStringMetric*tfidfWeight*tfidfWeight;
	                }
	            }
	        }
	        return stringMetric;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return 0.0;
    }
}
