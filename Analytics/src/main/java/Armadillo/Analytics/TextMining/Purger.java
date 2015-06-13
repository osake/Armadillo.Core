package Armadillo.Analytics.TextMining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Armadillo.Analytics.Optimisation.MstImpl.Mst;
import Armadillo.Analytics.Optimisation.MstImpl.MstClusterConstants;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import Armadillo.Core.PrintToScreen;
import Armadillo.Core.Concurrent.ILoopBody;
import Armadillo.Core.Concurrent.Parallel;

public class Purger 
{
    public Mst Mst;
    public TokenStatistics TokenStatistics;
    public double NodeDegreesThreshold;
    public double BranchLenghtThreshold;
    public double AdjacentNodeThreshold;
    public double BranchSizeThreshold;
    public double EdgeThreshold;
    
    // declare progress bar event handler
    private static final Object m_lockObject = new Object();
    private final double m_dblCheapThreshold1;
    private final double m_dblCheapThreshold2;
    private final double m_dblCheapThreshold3;
    private final int m_intSearchLength;
    private final DataWrapper m_data;
    private final IStringMetric m_cheapLink;
    private final IStringMetric m_tagLink;
    private final Blocker blocker;
    private final Mst mst;
    private final TagLinkCheap cheapLink1;
    private final TagLinkCheap cheapLink2;
    private final TagLinkCheap cheapLink3;
    private final int[] intTotalComparisons = new int[1];
    private final int intN;
    private final double dblGoal;
    private final int[] lastPercentage;
    private final double[] dblSumMinutes;
    private final DateTime[] start = new DateTime[1];
    
    public Purger(
            double dblEdgeThreshold,
            double nodeDegrees,
            double branchLenghtThreshold,
            double adjacentNodeThreshold,
            double branchSizeThreshold,
            DataWrapper data)
        {
            EdgeThreshold = dblEdgeThreshold;
            NodeDegreesThreshold = nodeDegrees;
            BranchLenghtThreshold = branchLenghtThreshold;
            AdjacentNodeThreshold = adjacentNodeThreshold;
            AdjacentNodeThreshold = branchSizeThreshold;
            m_dblCheapThreshold1 = TextMiningConstants.PURGER_CHEAP_THRESHOLD_1;
            m_dblCheapThreshold2 = TextMiningConstants.PURGER_CHEAP_THRESHOLD_2;
            m_dblCheapThreshold3 = TextMiningConstants.PURGER_CHEAP_THRESHOLD_3;
            m_intSearchLength = TextMiningConstants.PURGER_SEARCH_LENGTH;
            m_data = data;
            TokenStatistics = new TokenStatistics(data);
            m_cheapLink = new TagLinkCheap(data, 4, TokenStatistics);
            m_tagLink = new TagLink(
                data, 
                TokenStatistics, 
                EdgeThreshold);
            
            intN = m_data.length();
            dblGoal = (((intN) * ((intN) - 1.0)) / 2.0);
            
            lastPercentage = new int[1];
            //double dblIteration = 0;
            dblSumMinutes = new double[1];
            
            blocker = new Blocker(m_data);
            mst = new Mst(EdgeThreshold,
                              NodeDegreesThreshold,
                              BranchLenghtThreshold,
                              AdjacentNodeThreshold,
                              AdjacentNodeThreshold)
            {
            	@Override
            	public double GetDistance(int intI, int intJ) 
            	{
            		return OnGetDistance(intI, intJ);
            	}
            };
            
            cheapLink1 = new TagLinkCheap(m_data, 2, TokenStatistics);
            cheapLink2 = new TagLinkCheap(m_data, 4, TokenStatistics);
            cheapLink3 = new TagLinkCheap(m_data, 8, TokenStatistics);
        }

        public Purger(
            DataWrapper dataArray) 
        { 
        	this(
                    MstClusterConstants.EDGE_THRESHOLD,
                    MstClusterConstants.NODE_DEGREES_THRESHOLD,
                    MstClusterConstants.BRANCH_LENGHT_THRESHOLD,
                    MstClusterConstants.ADJACENT_NODE,
                    MstClusterConstants.BRANCH_SIZE_THRESHOLD,
                    dataArray);
        }

        public Purger(
                DataWrapper dataArray,
                double dblEdgeThreshold) 
            { 
            	this(
            			dblEdgeThreshold,
                        MstClusterConstants.NODE_DEGREES_THRESHOLD,
                        MstClusterConstants.BRANCH_LENGHT_THRESHOLD,
                        MstClusterConstants.ADJACENT_NODE,
                        MstClusterConstants.BRANCH_SIZE_THRESHOLD,
                        dataArray);
            }
        
        public void Purge()
        {
            Mst = DoPurge();
            // evaluate the results
            //Mst.EvaluateTree();
        }

        private Mst DoPurge()
        {
            //
            // declare string metrics
            //

            start[0] = DateTime.now();
            DateTime start2 = DateTime.now();
    		Parallel.For(0, intN, new ILoopBody<Integer>() 
    		{
    								  public void run(Integer i) 
                                      {
                                          List<MstDistanceObj> candidateList = new ArrayList<MstDistanceObj>();
                                          List<RowWrapper> candidateRowList = new ArrayList<RowWrapper>();
                                          List<Integer> indexList = new ArrayList<Integer>();

                                          GetCandidatesList(
                                              i, 
                                              intN, 
                                              blocker, 
                                              mst, 
                                              cheapLink1, 
                                              cheapLink2, 
                                              cheapLink3, 
                                              candidateList, 
                                              candidateRowList, 
                                              indexList);

                                          //
                                          // define column and token weights for each cluster
                                          //
                                          // compare the candidates
                                          if (candidateRowList.size() > 0)
                                          {
                                              candidateRowList.add(m_data.getDataArray()[i]);
                                              DataWrapper candidatesDataWrapper = 
                                                  new DataWrapper(
                                                    candidateRowList.toArray(new RowWrapper[candidateRowList.size()]));
                                              Integer[] indexArray = indexList.toArray(new Integer[indexList.size()]);
                                              TokenStatistics tokenStatistics2 =
                                                  new TokenStatistics(candidatesDataWrapper);
                                              TagLink tagLink2 = new TagLink(candidatesDataWrapper, 
                                                                              tokenStatistics2, 
                                                                              EdgeThreshold);

                                              // sort the candidate list
                                              List<MstDistanceObj> candidateList2 = new ArrayList<MstDistanceObj>();
                                              Collections.sort(candidateList, new MstDistanceObj());
                                              Collections.reverse(candidateList);
                                              int searchCount = 0;
                                              // search for the most likely match
                                              for (MstDistanceObj currentScoreObject2 : candidateList)
                                              {
                                                  int y = currentScoreObject2.GetY();
                                                  // compute taglink
                                                  double currentStringMetric =
                                                      tagLink2.GetStringMetric(y, candidateRowList.size() - 1);
                                                  if (currentStringMetric > 0.0)
                                                  {
                                                      MstDistanceObj actualScoreObject =
                                                          new MstDistanceObj(i, indexArray[y], currentStringMetric);
                                                      candidateList2.add(actualScoreObject);
                                                  }

                                                  searchCount++;

                                                  if (searchCount >= m_intSearchLength)
                                                  {
                                                      break;
                                                  }
                                              }

                                              // process the colected scores

                                              Collections.sort(candidateList2, new MstDistanceObj());
                                              Collections.reverse(candidateList2);
                                              for (MstDistanceObj currentScoreObject2 : candidateList2)
                                              {
                                                  if (currentScoreObject2.GetScore() > 1.0)
                                                  {
                                                      currentScoreObject2.SetScore(1.0);
                                                  }
                                                  synchronized (m_lockObject)
                                                  {
                                                      mst.Link(currentScoreObject2, null);
                                                  }
                                              }
                                          }

                                          //
                                          // display progress
                                          //
                                          DisplayProgress(
                                              i, 
                                              intN, 
                                              intTotalComparisons, 
                                              dblGoal, 
                                              lastPercentage, 
                                              start, 
                                              dblSumMinutes, 
                                              "");
                                      }
    		});
    		Seconds seconds = Seconds.secondsBetween(start2, DateTime.now());
            PrintToScreen.WriteLine("time: " + seconds.getSeconds());
            return mst;
        }

        private void GetCandidatesList(
            int i, 
            int intN, 
            Blocker blocker, 
            Mst mst, 
            TagLinkCheap cheapLink1, 
            TagLinkCheap cheapLink2, 
            TagLinkCheap cheapLink3, 
            List<MstDistanceObj> candidateList,
            List<RowWrapper> rowList, 
            List<Integer> indexList)
        {
            for (int j = i + 1; j < intN; j++)
            {
                if (blocker.CheckCodeMatch(i, j))
                {
                    if (mst.CheckLoop(i + 1, j + 1))
                    {
                        if (cheapLink1.GetStringMetric(i, j) >= m_dblCheapThreshold1)
                        {
                            if (cheapLink2.GetStringMetric(i, j) >= m_dblCheapThreshold2)
                            {
                                double cheapStringMetric = cheapLink3.GetStringMetric(i, j);
                                if (cheapStringMetric >= m_dblCheapThreshold3)
                                {
                                    MstDistanceObj actualScoreObject =
                                        new MstDistanceObj(i, candidateList.size(),
                                                           cheapStringMetric);
                                    candidateList.add(actualScoreObject);

                                    // add row to the list
                                    rowList.add(m_data.getDataArray()[j]);
                                    indexList.add(j);
                                }
                            }
                        }
                    }
                }
            }
        }

        private void DisplayProgress(
            int i, 
            int intN, 
            int[] intTotalComparisons, 
            double dblGoal, 
            int[] lastPercentage, 
            DateTime[] start, 
            double[] dblSumMinutes, 
            String strMessage)
        {
            synchronized (m_lockObject)
            {
                intTotalComparisons[0] += intN - i;
                int intPercentage = (int)((intTotalComparisons[0] / dblGoal) * 100.00);
                if (intPercentage != lastPercentage[0])
                {
                	Seconds mins = Seconds.secondsBetween(start[0], DateTime.now());
                    dblSumMinutes[0] += mins.getSeconds() / 60;
                    double avgMinutes = dblSumMinutes[0] / (intPercentage);
                    int estimatedTime = (int)((100.00 - intPercentage) * avgMinutes) + 1;
                    String message = "Comparing records... Total comparisons: " +
                                     intTotalComparisons + ", Completed: " +
                                     intPercentage + "%. Estimated time for completion: " +
                                     estimatedTime + " Min.";
                    InvokeProgressBarEventHandler(message, intPercentage);
                    PrintToScreen.WriteLine(strMessage + "," + intPercentage);
                    start[0] = DateTime.now();
                }
                lastPercentage[0] = intPercentage;
            }
        }

        private double OnGetDistance(int intI, int intJ)
        {
            double tmpScore = 0;
            if (m_cheapLink.GetStringMetric(intI, intJ) >= 0.1)
            {
                tmpScore = m_tagLink.GetStringMetric(intI, intJ);
            }
            return tmpScore;
        }

        private void InvokeProgressBarEventHandler(
            String strMessage, int p)
        {
        	// to be overriden
        }
    
}
