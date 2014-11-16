package Armadillo.Analytics.TextMining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import Armadillo.Core.Logger;
import Armadillo.Core.Text.TokenWrapper;

public class Searcher
{
    private TagLinkCheap m_tagLinkCheapStringMetric1;
    private TagLinkCheap m_tagLinkCheapStringMetric2;
    private TagLinkCheap m_tagLinkCheapStringMetric3;
    private TokenStatistics m_tokenStatistics;
    private double m_dblCheapThreshold1;
    private double m_dblCheapThreshold2;
    private double m_dblCheapThreshold3;
    private DataWrapper m_dataWrapper;
    private TagLink m_stringMetric;

    public Searcher(DataWrapper dataArray)
    {
    	this(dataArray, 0);
    }

    public Searcher(
    		DataWrapper dataWrapper, 
    		int intSearchIntensity)
    {
    	try
    	{
	        m_dblCheapThreshold1 = TextMiningConstants.DBL_SEARCHER_CHEAP_THRESHOLD_1;
	        m_dblCheapThreshold2 = TextMiningConstants.DBL_SEARCHER_CHEAP_THRESHOLD_2;
	        m_dblCheapThreshold3 = TextMiningConstants.DBL_SEARCHER_CHEAP_THRESHOLD_3;
	
	        m_dataWrapper = dataWrapper;
	        m_tokenStatistics = new TokenStatistics(dataWrapper);
	        if (intSearchIntensity <= 0)
	        {
	            m_stringMetric = new TagLink(dataWrapper, m_tokenStatistics);
	        }
	        else
	        {
	            m_stringMetric = new TagLink(dataWrapper, m_tokenStatistics, intSearchIntensity);
	        }
	        m_tagLinkCheapStringMetric1 = new TagLinkCheap(
	        		dataWrapper, 
	        		intSearchIntensity + 2, 
	        		m_tokenStatistics, 
	        		0.6);
	        m_tagLinkCheapStringMetric2 = new TagLinkCheap(
	        		dataWrapper, 
	        		intSearchIntensity + 4, 
	        		m_tokenStatistics, 
	        		0.65);
	        m_tagLinkCheapStringMetric3 = new TagLinkCheap(
	        		dataWrapper, 
	        		intSearchIntensity + 8, 
	        		m_tokenStatistics, 
	        		0.7);
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    public List<MstDistanceObj> Search(String str)
    {
    	try
    	{
	    	RowWrapper rowWrapper = m_dataWrapper.getRowWrapper(str);
	    	return Search(rowWrapper.Columns);
		}		catch(Exception ex)
    	{
			Logger.log(ex);
		}
    	return new ArrayList<MstDistanceObj>();
    }
    
    public DataWrapper getDataWrapper()
    {
    	return m_dataWrapper;
    }
    
    public List<MstDistanceObj> Search(TokenWrapper[][] row)
    {
    	try
    	{
	        int intN = m_dataWrapper.length();
	        int lastPercentage = 0;
	        double iteration = 0;
	        double sumMinutes = 0;
	        double goal = intN;
	
	        DateTime start = DateTime.now();
	        final String strMessage = "Please wait. Comparing records...";
	        InvokeProgressBarEventHandler(strMessage, 0);
	
	        List<MstDistanceObj> candidateList = new ArrayList<MstDistanceObj>();
	
	        for (int j = 0; j < intN; j++, iteration++)
	        {
	            if (m_tagLinkCheapStringMetric1.GetStringMetric(row, j) >= m_dblCheapThreshold1)
	            {
	                if (m_tagLinkCheapStringMetric2.GetStringMetric(row, j) >= m_dblCheapThreshold2)
	                {
	                    if (m_tagLinkCheapStringMetric3.GetStringMetric(row, j) >= m_dblCheapThreshold3)
	                    {
	                        // compute TagLink
	                        double dblTagLinkScore = m_stringMetric.GetStringMetric(row, j);
	                        if (dblTagLinkScore > 0.0)
	                        {
	                            MstDistanceObj actualScoreObject =
	                                new MstDistanceObj(-1, j, dblTagLinkScore);
	                            candidateList.add(actualScoreObject);
	                        }
	                    }
	                }
	            }
	            //
	            // update progress
	            //
	            int intPercentage = (int) ((iteration/goal)*100.00);
	            if (intPercentage != lastPercentage)
	            {
	                DateTime end = DateTime.now();
	                Seconds ts = Seconds.secondsBetween(start, end);
	                sumMinutes += ((ts.getSeconds())/60.0);
	                double avgMinutes = sumMinutes/(intPercentage);
	                int estimatedTime = (int) ((100.00 - intPercentage)*avgMinutes) + 1;
	                String message = "Comparing records... Completed: " +
	                                 intPercentage + "%. Estimated time for completion: " +
	                                 estimatedTime + " Min.";
	
	                InvokeProgressBarEventHandler(message, intPercentage);
	                start = DateTime.now();
	            }
	            lastPercentage = intPercentage;
	        }
	        
	        //
	        // sort the collected scores
	        //
			Collections.sort(candidateList, new Comparator<MstDistanceObj>() 
					{
						@Override
						public int compare(
								MstDistanceObj item1,
								MstDistanceObj item2) 
						{
							return MstDistanceObj.compareStatic(item1, item2);
							
						}

						@Override
						public Comparator<MstDistanceObj> reversed() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public Comparator<MstDistanceObj> thenComparing(
								Comparator<? super MstDistanceObj> other) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public <U> Comparator<MstDistanceObj> thenComparing(
								Function<? super MstDistanceObj, ? extends U> keyExtractor,
								Comparator<? super U> keyComparator) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public <U extends Comparable<? super U>> Comparator<MstDistanceObj> thenComparing(
								Function<? super MstDistanceObj, ? extends U> keyExtractor) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public Comparator<MstDistanceObj> thenComparingInt(
								ToIntFunction<? super MstDistanceObj> keyExtractor) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public Comparator<MstDistanceObj> thenComparingLong(
								ToLongFunction<? super MstDistanceObj> keyExtractor) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public Comparator<MstDistanceObj> thenComparingDouble(
								ToDoubleFunction<? super MstDistanceObj> keyExtractor) {
							// TODO Auto-generated method stub
							return null;
						}
			});
	        
			Collections.reverse(candidateList); 
	        
	        return candidateList;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return new ArrayList<MstDistanceObj>();
    }

    private void InvokeProgressBarEventHandler(
        String strMessage, int p)
    {
    }

	public void replaceRow(String strNewKey, int intIndex) 
	{
		try
		{
			if(m_dataWrapper == null)
			{
				return;
			}
			m_dataWrapper.replaceRow(strNewKey, intIndex);
			m_tokenStatistics.replaceIdfStats(intIndex, 
					m_dataWrapper.getDataArray()[intIndex].Columns);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public void addRow(String strNewKey) 
	{
		try
		{
			if(m_dataWrapper == null)
			{
				return;
			}
			m_dataWrapper.addRow(strNewKey);
			m_tokenStatistics.addIdfStats(
					m_dataWrapper.getDataArray()[m_dataWrapper.getDataArray().length - 1].Columns);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public void removeRow(int intIndex) 
	{
		try
		{
			if(m_dataWrapper == null)
			{
				return;
			}
			m_dataWrapper.removeRow(intIndex);
			m_tokenStatistics.removeIdfStats(intIndex);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public void setCheapThreshold1(
			double dblCheapThreshold1)
	{
		m_dblCheapThreshold1 = dblCheapThreshold1;
	}
	public void setCheapThreshold2(
			double dblCheapThreshold2)
	{
		m_dblCheapThreshold2 = dblCheapThreshold2;
	}
	public void setCheapThreshold3(
			double dblCheapThreshold3)
	{
		m_dblCheapThreshold3 = dblCheapThreshold3;
	}
}
