package Armadillo.Analytics.TextMining;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Core.Logger;
import Armadillo.Core.Text.TokenWrapper;
import Armadillo.Core.Text.Tokeniser;

public abstract class ADocument
{
    public List<String> DataList;

    /// <summary>
    /// Data stored as row-column-token
    /// </summary>
    public DataWrapper Data;

    public TokenStatistics TokenStatistics()
    {
            if (m_tokenStatistics == null)
            {
                m_tokenStatistics = new TokenStatistics(this);
            }
            return m_tokenStatistics;
    }


    private static PorterStemer m_stemmer = new PorterStemer();
    private StopWords m_stopWords;
    private TokenStatistics m_tokenStatistics;

    /// <summary>
    /// Whether to stem tokens with the Porter stemmer
    /// </summary>
    private boolean m_blnDoStem;

	private int m_intCols;


    /// <summary>
    /// Creates a new Document making sure that the stopwords
    ///  are loaded, indexed, and ready for use.  Subclasses
    /// that create concrete instances MUST call prepareNextToken
    /// before finishing to ensure that the first token is precomputed
    /// and available.
    /// </summary>
    /// <param name="blnDoStem"></param>
    /// <param name="stopWords"></param>
    /// <param name="document"></param>
    /// <param name="charColumnDelimiter"></param>
    protected ADocument(
        boolean blnDoStem,
        StopWords stopWords,
        List<String> document,
        char charColumnDelimiter)
    {
        m_stopWords = stopWords;
        DataList = document;
        m_blnDoStem = blnDoStem;
        m_intCols = DataList.get(0).split(charColumnDelimiter + "").length;
        LoadTokens(charColumnDelimiter);
    }


    private void LoadTokens(char charColumnDelimiter)
    {
    	try{
	        if (DataList == null || DataList.size() == 0)
	        {
	            return;
	        }
	        Data.setDataArray(new RowWrapper[DataList.size()]);
	        for (int intRow = 0; intRow < DataList.size(); intRow++)
	        {
	            String strLine = DataList.get(intRow);
	            TokenWrapper[][] currToks = LoadTokenWrapperArr(strLine);
	            Data.getDataArray()[intRow].Columns = currToks;
	        }
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    }
    
    public TokenWrapper[][] LoadTokenWrapperArr(
    		String strLine)
    {
    	try{
	    	TokenWrapper[][] Columns = new TokenWrapper[m_intCols][];
	        String[] cols = strLine.split(",");
	        for (int intCol = 0; intCol < m_intCols; intCol++)
	        {
	            String strCol = cols[intCol];
	            ArrayList<TokenWrapper> validTokens = new ArrayList<TokenWrapper>();
	            String[] tokens = Tokeniser.Tokenise(strCol, false);
	            for (int i = 0; i < tokens.length; i++)
	            {
	                String strToken = tokens[i];
	                if (m_stopWords == null ||
	                    !m_stopWords.Contains(strToken))
	                {
	                    if (m_blnDoStem)
	                    {
	                        strToken = m_stemmer.DoStem(strToken);
	                    }
	                    validTokens.add(new TokenWrapper(strToken));
	                }
	            }
	            Columns[intCol] = validTokens.toArray(new TokenWrapper[0]);
	        }
	        return Columns;
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return null;
    }
}
