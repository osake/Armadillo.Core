package Armadillo.Analytics.TextMining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import Armadillo.Core.Logger;
import Armadillo.Core.Text.TokenWrapper;
import Armadillo.Core.Text.Tokeniser;

public class TagLink implements IStringMetric
{
    private static final int WINDOW_SIZE = 3;
    private static final double TOTAL_SCORE_THRESHOLD = 0.2;

    public double TokenThreshold;

    private  TokenStatistics m_tokenStatistics;
    private  double m_dblScoreLossThreshold;
    private  int m_intColumnCount;
    private  int m_intW;
    private  TagLinkTokenCheap m_tagLinkToken;
    private  DataWrapper m_strDataArray;

    public TagLink() { 
        	
        	this(TOTAL_SCORE_THRESHOLD);
       }
 
    public TagLink(
        double dblTotalScoreThreshold) { 
    	
    	this(null,
                 null,
                 WINDOW_SIZE,
                 dblTotalScoreThreshold);
   }

    public TagLink(
            DataWrapper rowObjectArray,
            TokenStatistics tokenStatistics){ 
        	
        	this(rowObjectArray,
                    tokenStatistics,
                    WINDOW_SIZE,
                    TOTAL_SCORE_THRESHOLD);
       }
    
    public TagLink(
        DataWrapper rowObjectArray,
        TokenStatistics tokenStatistics,
        double dblTotalScoreThreshold){ 
    	
    	this(rowObjectArray,
                tokenStatistics,
                WINDOW_SIZE,
                dblTotalScoreThreshold);
   }
    
    public TagLink(
        DataWrapper strDataArray,
        TokenStatistics tokenStatistics,
        int intW,
        double dblTotalScoreThreshold)
    {
    	try{
	        m_tokenStatistics = tokenStatistics;
	        m_intW = intW;
	        m_strDataArray = strDataArray;
	        if(strDataArray != null &&
	        		strDataArray.getDataArray() != null &&
	        		strDataArray.getDataArray().length > 0){
	        	m_intColumnCount = strDataArray.getDataArray()[0].Columns.length;
	        }
	        else{
	        	m_intColumnCount = 1;
	        }
	        m_tagLinkToken = new TagLinkTokenCheap();
	        m_dblScoreLossThreshold = 1.0 - dblTotalScoreThreshold;
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    }

    public double GetStringMetric(
            String strT,
            String strU)
        {
        	try{
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
    
    public double GetStringMetric(
        TokenWrapper[][] tTokens,
        TokenWrapper[][] uTokens)
    {
    	try{
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
	        return GetStringMetric(
	            m_intColumnCount,
	            tTokens,
	            uTokens,
	            tIdfArray,
	            uIdfArray,
	            m_intW,
	            m_tagLinkToken,
	            m_dblScoreLossThreshold);
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return 0;
    }

    public double GetStringMetric(
        TokenWrapper[][] tTokens, 
        int rowU)
    {
    	try{
	        TokenWrapper[][] uTokens = m_strDataArray.getDataArray()[rowU].Columns;
	        double[][] tIdfArray = m_tokenStatistics.GetIdfArray(tTokens);
	        double[][] uIdfArray = m_tokenStatistics.GetIdfArray(rowU);
	        return GetStringMetric(
	            m_intColumnCount, 
	            tTokens, 
	            uTokens, 
	            tIdfArray, 
	            uIdfArray,
	            m_intW,
	            m_tagLinkToken,
	            m_dblScoreLossThreshold);
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return 0;
    }

    public double GetStringMetric(
        int rowT, 
        int rowU)
    {
    	try{
	        TokenWrapper[][] tTokens = m_strDataArray.getDataArray()[rowT].Columns;
	        TokenWrapper[][] uTokens = m_strDataArray.getDataArray()[rowU].Columns;
	        double[][] tIdfArray = m_tokenStatistics.GetIdfArray(rowT);
	        double[][] uIdfArray = m_tokenStatistics.GetIdfArray(rowU);
	        return GetStringMetric(
	            m_intColumnCount,
	            tTokens, 
	            uTokens, 
	            tIdfArray, 
	            uIdfArray,
	            m_intW,
	            m_tagLinkToken,
	            m_dblScoreLossThreshold);
		}
		catch(Exception ex){
			Logger.log(ex);
		}
    	return 0;
    }

    public double GetStringMetric(
        int rowT,
        int rowU,
        DataWrapper dataWrapper1,
        DataWrapper dataWrapper2,
        TokenStatistics tokenStatistics1,
        TokenStatistics tokenStatistics2)
    {
    	try{
	        TokenWrapper[][] tTokens = dataWrapper1.getDataArray()[rowT].Columns;
	        TokenWrapper[][] uTokens = dataWrapper2.getDataArray()[rowU].Columns;
	        double[][] tIdfArray = tokenStatistics1.GetIdfArray(rowT);
	        double[][] uIdfArray = tokenStatistics2.GetIdfArray(rowU);
	        return GetStringMetric(
	            m_intColumnCount,
	            tTokens,
	            uTokens,
	            tIdfArray,
	            uIdfArray,
	            m_intW,
	            m_tagLinkToken,
	            m_dblScoreLossThreshold);
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return 0;
    }

    private double GetStringMetric(
        int totalColumns, 
        TokenWrapper[][] tTokens,
        TokenWrapper[][] uTokens, 
        double[][] tIdfArray, 
        double[][] uIdfArray,
        int intW,
        TagLinkTokenCheap tagLinkToken,
        double dblLossThreshold)
    {
    	try{
	        // let T be the smallest String size
	        int tSize = GetMinStringSize(tTokens);
	        int uSize = GetMinStringSize(uTokens);
	        double minStringSize = Math.min(tSize, uSize);
	
	        if (uSize < tSize)
	        {
	            TokenWrapper[][] tmpTokens = tTokens;
	            tTokens = uTokens;
	            uTokens = tmpTokens;
	            double[][] tmpIdfArray = tIdfArray;
	            tIdfArray = uIdfArray;
	            uIdfArray = tmpIdfArray;
	        }
	        boolean blnComputeScore;
	        boolean blnPerfectMatch;
	        
	        boolean[] flags = new boolean[2];
	        List<Candidates> candidateList = GetCandidateList(
	            tTokens,
	            uTokens,
	            tIdfArray,
	            uIdfArray,
	            totalColumns,
	            minStringSize,
	            flags,
	            intW,
	            tagLinkToken,
	            dblLossThreshold);
	        
	        blnComputeScore = flags[0];
	        blnPerfectMatch = flags[1];
	
	        if (!blnComputeScore)
	        {
	            return 0.0;
	        }
	        if (blnPerfectMatch)
	        {
	            return 1.0;
	        }
	        
			Collections.sort(candidateList, new Comparator<Candidates>() {
	
				@Override
				public int compare(
						Candidates item1,
						Candidates item2) {
					return Candidates.compareStatic(item1, item2);
					
				}
			});
	        
			Collections.reverse(candidateList); 
	        double score = GetScore(candidateList);
	        if (score >= 1.0)
	        {
	            return 0.999999999;
	        }
	        return score;
		}
		catch(Exception ex){
			Logger.log(ex);
		}
    	return 0;
    }


    private static double GetScore(List<Candidates> candidateList)
    {
    	try{
	        double scoreValue = 0;
	        HashMap<String, String> tMap = new HashMap<String, String>();
	        HashMap<String, String> uMap = new HashMap<String, String>();
	        for (Candidates actualCandidates : candidateList)
	        {
	            int intActualTPos = actualCandidates.GetTPos();
	            int intActualUPos = actualCandidates.GetUPos();
	            int intActualTCol = actualCandidates.GetTCol();
	            int intActualUCol = actualCandidates.GetUCol();
	            String strTKey = intActualTPos + " " + intActualTCol;
	            String strUKey = intActualUPos + " " + intActualUCol;
	            if ((!tMap.containsKey(strTKey)) &&
	                (!uMap.containsKey(strUKey)))
	            {
	                double actualScore = actualCandidates.GetScore();
	                tMap.put(strTKey, "");
	                uMap.put(strUKey, "");
	                scoreValue += actualScore;
	            }
	        }
	        return scoreValue;
		}
		catch(Exception ex){
			Logger.log(ex);
		}
    	return 0;
    }

    private List<Candidates> GetCandidateList(
        TokenWrapper[][] tTokens,
        TokenWrapper[][] uTokens,
        double[][] tIdfArray, 
        double[][] uIdfArray,
        int totalColumns,
        double minStringSize,
        boolean[] flags,
        int intW,
        TagLinkTokenCheap tagLinkToken,
        double dblScoreLossThreshold)
    {
        boolean blnComputeScore = true;
        boolean blnPerfectMatch = true;
    	
        try{
	        ArrayList<Candidates> candidateList = new ArrayList<Candidates>();
	        double dblTotalScoreLoss = 0.0;
	        int intTRealPosition = 0;
	        for (int columnT = 0; columnT < totalColumns && 
	            blnComputeScore; columnT++)
	        {
	            int tLength = tTokens[columnT].length;
	            for (int t = 0; t < tLength && blnComputeScore; t++)
	            {
	                double dblMaxFoundTokenScore = 0.0;
	                TokenWrapper tTok = tTokens[columnT][t];
	                int intLastTr = -1;
	                int intURealPosition = 0;
	                int intColumnU = columnT;
	                int intUTokensLength = uTokens[intColumnU].length;
	                if (intUTokensLength == 0)
	                {
	                    break;
	                }
	                int intLowLimit = Math.min(Math.max(0, t - intW), intUTokensLength - 1);
	                int intUpLimit = Math.min(intUTokensLength, t + intW);
	                for (int u = intLowLimit, intFlag = 0; u < intUpLimit && intFlag == 0; u++)
	                {
	                    int intTr = Math.abs(intTRealPosition - intURealPosition);
	                    if (intLastTr >= 0 && intLastTr < intTr)
	                    {
	                        intFlag = 1;
	                    }
	                    else
	                    {
	                        TokenWrapper uTok = uTokens[intColumnU][u];
	                        double dblCheapInnerScore = TagLinkPrefix.GetStringMetric(tTok, uTok);
	                        double dblInnerScore = 0.0;
	                        if (dblCheapInnerScore >= 0.1)
	                        {
	                            dblInnerScore = tagLinkToken.GetStringMetric(tTok, uTok);
	                        }
	                        if (dblInnerScore >= 0.0)
	                        {
	                            double dblMatched;
	                            if (dblInnerScore == 1.0)
	                            {
	                                dblMatched = tTokens[columnT][t].length();
	                            }
	                            else
	                            {
	                                dblMatched = tagLinkToken.GetMatched();
	                                if (t == u && blnPerfectMatch)
	                                {
	                                    blnPerfectMatch = false;
	                                }
	                            }
	                            double dblWeightMatched = 0.0;
	                            if (minStringSize > 0.0)
	                            {
	                                dblWeightMatched = dblMatched/minStringSize;
	                            }
	                            double dblWeightTfidf =
	                                tIdfArray[columnT][t]*uIdfArray[intColumnU][u];
	                            double dblWeight = (dblWeightMatched + dblWeightTfidf)/2.0;
	                            if (dblInnerScore == 1)
	                            {
	                                intLastTr = intTr;
	                            }
	                            if (dblInnerScore > 0)
	                            {
	                                double dblTokenScore = dblInnerScore*dblWeight;
	                                if (dblTokenScore > dblMaxFoundTokenScore)
	                                {
	                                    dblMaxFoundTokenScore = dblTokenScore;
	                                }
	                                if (TokenThreshold == 0 || 
	                                    dblTokenScore >= TokenThreshold)
	                                {
	                                    candidateList.add(
	                                        new Candidates(t, u, columnT, intColumnU,
	                                                       dblTokenScore));
	                                }
	                            }
	                        }
	                    }
	                    intURealPosition++;
	                }
	                //
	                // get the expected score
	                //
	                double dblPerfectMatchScore =
	                    ((tIdfArray[columnT][t]*
	                      tIdfArray[columnT][t]) +
	                     ((tTok.length())/minStringSize))
	                    /2.0;
	                
	                dblTotalScoreLoss += (dblPerfectMatchScore - dblMaxFoundTokenScore);
	                if (dblTotalScoreLoss > dblScoreLossThreshold)
	                {
	                    blnComputeScore = false;
	                }
	                intTRealPosition++;
	            }
	        }
	        return candidateList;
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	finally{
            flags[0] = blnComputeScore;
            flags[1] = blnPerfectMatch;
    	}
    	return null;
    }

    private static int GetMinStringSize(
        TokenWrapper[][] tokens)
    {
    	try{
	        int tSize = 0;
	        for (int column = 0; column < tokens.length; column++)
	        {
	            for (int i = 0; i < tokens[column].length; i++)
	            {
	                tSize += tokens[column][i].length();
	            }
	        }
	        return tSize;
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return 0;
    }
}
