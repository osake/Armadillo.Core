package Armadillo.Analytics.TextMining;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import Armadillo.Core.Logger;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.Text.TokenWrapper;

public class TokenStatistics
{
    private static final String FREQ_WORDS_FILE_NAME = "C:\\HC\\Data\\TextMining\\CommonWordsFreq.txt";

    private HashMap<TokenWrapper, Double> m_idfMap ;
    private int m_intColumns ;
    private double[] m_columnWeights ;
    private double[][][] m_idfWeights ;
    private List<HashMap<TokenWrapper, Integer>> m_tokenFrequencies ;
    private DataWrapper m_dataWrapper;
    private static HashMap<String, Double> m_freqWordMap;

    public TokenStatistics(
        ADocument document) 
    { 
    	this(document.Data);
    }

    public TokenStatistics(
        DataWrapper strDataArray)
    {
        LoadStats(strDataArray);
    }

    public static HashMap<String, Double> LoadFrequencyWords()
    {
    	BufferedReader sr = null;
    	try
    	{
	        if(m_freqWordMap== null)
	        {
	            m_freqWordMap = new HashMap<String, Double>();
	            sr = new BufferedReader(new FileReader(FREQ_WORDS_FILE_NAME));
	                String strLine;
	                while ((strLine = sr.readLine()) != null)
	                {
	                    String[] tokens = strLine.split(",");
	                    m_freqWordMap.put(tokens[0], Double.parseDouble(tokens[1]));
	                }
	        }
	        return m_freqWordMap;
    	}
        catch(Exception ex)
    	{
        	Logger.log(ex);
        }
    	finally
    	{
    		if(sr != null)
    		{
    			try 
    			{
					sr.close();
				} 
    			catch (IOException ex) 
    			{
					Logger.log(ex);
				}
    		}
    	}
        return null;
    }


    private void LoadStats(DataWrapper strDataArray)
    {
    	try
    	{
	        if(strDataArray.getDataArray() == null ||
	        	strDataArray.getDataArray().length == 0)
	        {
	            return;
	        }
	        m_dataWrapper = strDataArray;
	        m_intColumns = strDataArray.getDataArray()[0].Columns.length;
	        GetWordStatistics();
	        GetIdfStats();
	        GetTokenFrequencies();
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    /// <summary>
    /// Returns a hashmap version of the term-vector (bag of words) for this
    /// document, where each token is a key whose value is the number of times
    /// it occurs in the document as stored in a Weight.
    /// </summary>
    /// <returns></returns>
    private void GetTokenFrequencies()
    {
        int intCols = m_dataWrapper.getDataArray()[0].Columns.length;
        setTokenFrequencies(new ArrayList<HashMap<TokenWrapper, Integer>>());
        for (int intColId = 0; intColId < intCols; intColId++)
        {
            getTokenFrequencies().add(new HashMap<TokenWrapper, Integer>());
        }
        for (int intRows = 0; intRows < m_dataWrapper.length(); intRows++)
        {
            for (int intColId = 0; intColId < intCols; intColId++)
            {
                if (m_dataWrapper.getDataArray()[intRows].Columns[intColId] != null)
                {
                    for (int intTokenId = 0;
                         intTokenId < m_dataWrapper.getDataArray()[intRows].Columns[intColId].length;
                         intTokenId++)
                    {
                        TokenWrapper tokenWrapper =
                            m_dataWrapper.getDataArray()[intRows].Columns[intColId][intTokenId];
                        HashMap<TokenWrapper, Integer> tokFreq = getTokenFrequencies().get(intColId);
                        if (!tokFreq.containsKey(
                            tokenWrapper))
                        {
                            getTokenFrequencies().get(intColId).put(tokenWrapper, 1);
                        }
                        else
                        {
                        	int intValue = tokFreq.get(tokenWrapper);
                            getTokenFrequencies().get(intColId).put(tokenWrapper, intValue + 1);
                        }
                    }
                }
            }
        }
    }

    private void GetWordStatistics()
    {
        int[] tokenCountArray = new int[m_intColumns];
        int[] differentTokens = new int[m_intColumns];
        double[] tokensColumnWeight = new double[m_intColumns];
        List<HashMap<TokenWrapper, Object>> mapArray = new ArrayList<HashMap<TokenWrapper, Object>>();
        for (int column = 0; column < m_intColumns; column++)
        {
            mapArray.add(new HashMap<TokenWrapper, Object>());
        }

        //
        // get idf weights ignoring columns
        //
        double dblN = m_dataWrapper.getDataArray().length;
        HashMap<TokenWrapper, Integer> idfMap1 = new HashMap<TokenWrapper, Integer>();
        for (int row = 0; row < dblN; row++)
        {
            TokenWrapper[][] currentRowArray = m_dataWrapper.getDataArray()[row].Columns;
            HashMap<TokenWrapper, Object> rowMap = new HashMap<TokenWrapper, Object>();
            for (int column = 0; column < m_intColumns; column++)
            {
                int tokenCount = currentRowArray[column].length;
                tokenCountArray[column] += tokenCount;
                for (int token = 0; token < tokenCount; token++)
                {
                    TokenWrapper actualToken = currentRowArray[column][token];
                    //if (!StringHelper.IsNullOrEmpty(actualToken))
                    {
                        if (!rowMap.containsKey(actualToken))
                        {
                            rowMap.put(actualToken, new Object());
                        }
                        if (!mapArray.get(column).containsKey(actualToken))
                        {
                            mapArray.get(column).put(actualToken, new Object());
                            differentTokens[column]++;
                        }
                    }
                }
            }
            for (TokenWrapper actualToken2 : rowMap.keySet())
            {
                if (!idfMap1.containsKey(actualToken2))
                {
                    idfMap1.put(actualToken2, 1);
                }
                else
                {
                    int freq0 = idfMap1.get(actualToken2);
                    idfMap1.put(actualToken2, freq0 + 1);
                }
            }
        }

        m_idfMap = new HashMap<TokenWrapper, Double>();
        for (Entry<TokenWrapper, Integer> de : idfMap1.entrySet())
        {
            TokenWrapper actualToken2 = de.getKey();
            int freq = de.getValue();
            double idfWeight = Math.log(((dblN)/(freq)) + 1.0);
            m_idfMap.put(actualToken2, idfWeight);
        }
        for (int column = 0; column < m_intColumns; column++)
        {
            double numerator = Math.log(tokenCountArray[column]);
            tokensColumnWeight[column] = (differentTokens[column])/numerator;
        }
        m_columnWeights = tokensColumnWeight;
        //
        // normalize weights
        //
        double sum = 0.0;
        for (int i = 0; i < m_intColumns; i++)
        {
            sum += m_columnWeights[i];
        }
        for (int i = 0; i < m_intColumns; i++)
        {
            m_columnWeights[i] = m_columnWeights[i]/sum;
        }
    }

    public double[] GetColumnWeights()
    {
        return m_columnWeights;
    }

    public double[][] GetIdfArray(int intRow)
    {
        return m_idfWeights[intRow];
    }
    
    public void replaceIdfStats(
    		int i,
    		TokenWrapper[][] currentRowArray)
    {
    	try
    	{
    		if(m_dataWrapper == null ||
    		   m_dataWrapper.getDataArray() == null ||
    		   i < 0 || i >= m_dataWrapper.getDataArray().length)
    		{
    			return;
    		}
            m_idfWeights[i] = GetIdfArray(currentRowArray);
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    public void removeIdfStats(int intRow)
    {
    	try
    	{
    		if(m_idfWeights == null ||
    		   m_dataWrapper.getDataArray() == null ||
    		   intRow < 0 || intRow >= m_idfWeights.length)
    		{
    			return;
    		}
    		double[][][] oldIdfWeights = m_idfWeights;
    		double[][][] newIdfWeights = new double[oldIdfWeights.length - 1][][];
    		int intCounter = 0;
    		for (int j = 0; j < oldIdfWeights.length; j++) 
    		{
				if(j != intRow)
				{
					newIdfWeights[intCounter] = oldIdfWeights[j];
					intCounter++;
				}
			}
    		m_idfWeights = newIdfWeights;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    public void addIdfStats(
    		TokenWrapper[][] currentRowArray)
    {
    	try
    	{
    		if(currentRowArray == null)
    		{
    			return;
    		}
            if(m_dataWrapper == null ||
    		   m_dataWrapper.getDataArray() == null)
    		{
    			return;
    		}
            
            double[][] idfArray = GetIdfArray(currentRowArray);
    		double[][][] oldIdfWeights = m_idfWeights;
    		double[][][] newIdfWeights = new double[oldIdfWeights.length + 1][][];
    		for (int j = 0; j < oldIdfWeights.length; j++) 
    		{
    			newIdfWeights[j] = oldIdfWeights[j];
			}
    		newIdfWeights[newIdfWeights.length - 1] = idfArray;
    		m_idfWeights = newIdfWeights;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }
    
    private void GetIdfStats()
    {
    	try
    	{
    		int intN = m_dataWrapper.getDataArray().length;
	        m_idfWeights = new double[intN][][];
	
	        for (int i = 0; i < intN; i++)
	        {
	            TokenWrapper[][] currentRowArray = m_dataWrapper.getDataArray()[i].Columns;
	            m_idfWeights[i] = GetIdfArray(currentRowArray);
	        }
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    public double[][] GetIdfArray(TokenWrapper[][] strRowArray)
    {
    	return GetIdfArrayStatic(
        		strRowArray,
        		m_intColumns,
        		m_idfMap,
        		m_columnWeights,
        		m_dataWrapper.length());
    }

    public static double[][] GetDummyIdfArray(
    		TokenWrapper[][] strRowArray)
    {
		HashMap<TokenWrapper, Double> idfMap = new HashMap<TokenWrapper, Double>();
    	for(TokenWrapper tokWrapper : strRowArray[0]){
    		idfMap.put(tokWrapper, 1.0);
    	}
    	return GetIdfArrayStatic(strRowArray, 1, idfMap, new double[] {1.0}, 1);
    }
    		
    
    private static double[][] GetIdfArrayStatic(
    		TokenWrapper[][] strRowArray,
    		int intColumns,
    		HashMap<TokenWrapper, Double> idfMap,
    		double[] columnWeights,
    		int intN)
    {
    	try{
	        double[][] idfArray = new double[intColumns][];
	        double dblSq = 0.0;
	        for (int columnIndex = 0; columnIndex < intColumns; columnIndex++)
	        {
	            int currentTokenArrayLength = strRowArray[columnIndex].length;
	            idfArray[columnIndex] = new double[currentTokenArrayLength];
	            for (int j = 0; j < currentTokenArrayLength; j++)
	            {
	                double idfWeight;
	                TokenWrapper token = strRowArray[columnIndex][j];
	                if (StringHelper.IsNullOrEmpty(token.Token))
	                {
	                    idfWeight = 0.0;
	                }
	                else if (idfMap.containsKey(token))
	                {
	                    idfWeight = (idfMap.get(strRowArray[columnIndex][j]))
	                                *columnWeights[columnIndex];
	                }
	                else
	                {
	                    idfWeight = Math.log(((intN)/1.0) + 1.0);
	                }
	                idfArray[columnIndex][j] = idfWeight;
	                dblSq += idfWeight*idfWeight;
	            }
	        }
	        dblSq = Math.sqrt(dblSq);
	        for (int columnIndex = 0; columnIndex < intColumns; columnIndex++)
	        {
	            int currentTokenArrayLength = strRowArray[columnIndex].length;
	            for (int j = 0; j < currentTokenArrayLength; j++)
	            {
	                idfArray[columnIndex][j] = idfArray[columnIndex][j]/dblSq;
	            }
	        }
	        return idfArray;
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return null;
    }

	public HashMap<TokenWrapper, Double> getIdfMap() {
		return m_idfMap;
	}

	public void setIdfMap(HashMap<TokenWrapper, Double> idfMap) {
		m_idfMap = idfMap;
	}

	public int getColumns() {
		return m_intColumns;
	}

	public void setColumns(int columns) {
		m_intColumns = columns;
	}

	public double[] getColumnWeights() {
		return m_columnWeights;
	}

	public void setColumnWeights(double[] columnWeights) {
		m_columnWeights = columnWeights;
	}

	public double[][][] getIdfWeights() {
		return m_idfWeights;
	}

	public void setIdfWeights(double[][][] idfWeights) {
		m_idfWeights = idfWeights;
	}

	public List<HashMap<TokenWrapper, Integer>> getTokenFrequencies() {
		return m_tokenFrequencies;
	}

	public void setTokenFrequencies(List<HashMap<TokenWrapper, Integer>> tokenFrequencies) {
		m_tokenFrequencies = tokenFrequencies;
	}
}
