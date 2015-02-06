package Armadillo.Communication.Impl.Distributed;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;
import org.joda.time.Minutes;

import Armadillo.Core.Config;
import Armadillo.Core.Console;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Communication.Impl.SimpleUiSocket;
import Armadillo.Communication.Impl.ReqResp.ReqRespService;
import Armadillo.Communication.Impl.ReqResp.ZmqReqRespClient;
import Armadillo.Communication.Impl.Topic.SubscriberCallbackDel;
import Armadillo.Communication.Impl.Topic.TopicMessage;
import Armadillo.Communication.Impl.Topic.TopicSubscriberCache;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Core.SelfDescribing.ASelfDescribingClass;
import Armadillo.Core.Text.StringHelper;

public class DistWorker {
    
	public int Threads;
    public String GridTopic;
    public int JobsInProgress;
    public String ServerName;
    public DistTopicQueue DistTopicQueue;
    
    public int JobsCompleted()
    {
        return JobsCompletedMap.size();
    }

    public String WorkerId;
    public static boolean IsConnected;
    public DistWorkerToContollerHeartBeat DistWorkerToContollerHeartBeat;
    public ConcurrentHashMap<String, DateTime> JobsCompletedMap;
    public HashSet<String> CalcTypesSet;
    public ConcurrentHashMap<String, Object> m_jobsToDo;
    private static final Object m_connectionLock = new Object();
    private DistWorkerResultSender m_distWorkerResultSender;
    private final Object m_progressLock = new Object();
    private ThreadWorker<ObjectWrapper> m_clockTickWorker;
    private DistWorkerJobPull m_distWorkerJobPull;
    private static int m_intWorkerCounter;
    private static final Object m_couterLock = new Object();
    private ThreadWorker<ObjectWrapper> m_jobsDoneFlusherWorker;

    public static DistWorker OwnInstance;

    private DistWorker(
        String strServerName)
    {
    	try{
	        ServerName = strServerName;
	        JobsCompletedMap = new ConcurrentHashMap<String, DateTime>();
	
	        Threads = Integer.parseInt(Config.getStringStatic(
	        		"CalcThreads",
	        		SimpleUiSocket.class));
	        String strTopicFromConfig = Config.getStringStatic(
	        		"CalcTopic",
	        		SimpleUiSocket.class);
	
	        CalcTypesSet = new HashSet<String>();
	        GridTopic = strTopicFromConfig;
	        
	        if(StringHelper.IsNullOrEmpty(GridTopic))
	        {
	            throw new HCException("Empty grid topic");
	        }
	
	        synchronized(m_couterLock){
	        	m_intWorkerCounter++;
	        }
	
	        WorkerId = "Worker_" +
	                Config.getClientName() + "_" +
	                GridTopic + "_" +
	                m_intWorkerCounter;
	
	        m_jobsToDo = new ConcurrentHashMap<String, Object>();
	
	        DistTopicQueue = new DistTopicQueue(strServerName);
	        SubscribeToTopics();
	        DistWorkerToContollerHeartBeat = new DistWorkerToContollerHeartBeat(this);
	        SetupThreadWorker();
	        m_distWorkerResultSender = new DistWorkerResultSender(this);
	        m_distWorkerJobPull = new DistWorkerJobPull(this);
	
	        m_jobsDoneFlusherWorker = new ThreadWorker<ObjectWrapper>(){
	        	
	        	@Override
	        	public void runTask(ObjectWrapper item) {
	        		
	        		try{
	        			
	        			while(true){
	        				
	        				try{
	        					
	        					if(JobsCompletedMap.size() > (int)10e3){
	        						
	        						ArrayList<String> jobsToRemove = new ArrayList<String>(); 
	        						for(Entry<String, DateTime> kvp : JobsCompletedMap.entrySet()){
	        							
	        							int intMins = Minutes.minutesBetween(
	        									kvp.getValue(), DateTime.now()).getMinutes();
	        							if(intMins > 30){
	        								jobsToRemove.add(kvp.getKey());
	        							}
	        						}
	        						
	        						if(jobsToRemove.size() > 0){
	        							
	        							for(String strJobId : jobsToRemove){
	        								
	        								JobsCompletedMap.remove(strJobId);
	        							}
	        						}
	        					}
	        				}
	        				catch(Exception ex){
	        					
	        					Logger.log(ex);
	        				}
	        				
	        				Thread.sleep(60000);
	        			}
	        		}
	        		catch(Exception ex){
	        			
	        			Logger.log(ex);
	        		}
	        	}
	        };
	        m_jobsDoneFlusherWorker.work();
	        
	        //
	        // log connection
	        //
	        String strMessage = getClass().getName() + " is connected to [" + strServerName + 
	                "] via topic [" + strTopicFromConfig + "]";
	        Console.writeLine(strMessage);
	        Logger.log(strMessage);
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    }

    private void SetupThreadWorker()
    {
        m_clockTickWorker = new ThreadWorker<ObjectWrapper>(){
        	@Override
        	public void runTask(ObjectWrapper item) {
        		while (true)
                {
                    try
                    {
                        OnClockTick();
                        Thread.sleep(DistConstants.WORKER_PUBLISH_GUI_TIME_SECS*1000);
                    }
                    catch (Exception ex)
                    {
                        Logger.log(ex);
                    }
                }
        	}
        };
        m_clockTickWorker.work();
    }

    public static void Connect(String strServerName)
    {
        if (IsConnected)
        {
            return;
        }
        synchronized (m_connectionLock)
        {
            if (IsConnected)
            {
                return;
            }
            OwnInstance = new DistWorker(
                strServerName);
            IsConnected = true;
            Logger.log(OwnInstance.getClass().getName() + " is now connected. Id = " +
                OwnInstance.WorkerId);
        }
    }

    private void OnClockTick()
    {
        DistGuiHelper.PublishWorkerStats(this);
    }

    
    private void SubscribeToTopics()
    {
        try
        {
            TopicSubscriberCache.GetSubscriber(ServerName).Subscribe(
                GridTopic + EnumDistributed.JobsDoneTopic.toString(),
                new SubscriberCallbackDel(){
                	@Override
                	public void invoke(TopicMessage topicMessage) {
                		OnTopicControllerJobsDone(topicMessage);
                	}
                });
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private void OnTopicControllerJobsDone(
        TopicMessage topicmessage)
    {
        String strJobId = (String) topicmessage.EventData;
        JobsCompletedMap.put(strJobId, DateTime.now());
    }

    public void DoJob(
        String strJobId,
        ASelfDescribingClass selfDescribingClass)
    {
        try
        {
            DateTime workTimeLog = DateTime.now();
            synchronized (m_progressLock)
            {
                JobsInProgress++;
            }
            
            m_jobsToDo.put(strJobId, new Object());            
            
            DistGuiHelper.PublishWorkerLog(this, "Runnning jobs params [" +
                                                 selfDescribingClass + "]");

            String strParentId = selfDescribingClass.GetStrValue(EnumDistributed.ControllerId);
            
            @SuppressWarnings("unchecked")
			List<Object> params = (List<Object>) selfDescribingClass.GetObjValue("Params");
			String strMethodId = selfDescribingClass.GetStrValue("Request");
			List<Object> result = ReqRespService.ExecuteMethod(strMethodId, params);
            //
            // send result
            //
            boolean blnResultIsSent = m_distWorkerResultSender.SendResult(
            		result,
                strParentId,
                strJobId);

            if (blnResultIsSent)
            {
                DistGuiHelper.PublishWorkerLog(this, "Sent result. JobId [" +
                                                     strJobId + "]");
                JobsCompletedMap.put(strJobId, DateTime.now());
            }
            else
            {
                JobsCompletedMap.remove(strJobId);
                DistGuiHelper.PublishWorkerLog(this, "Could not send result for [" +
                                                     strJobId + "]");
            }


            synchronized (m_progressLock)
            {
                JobsInProgress--;
            }

            DistGuiHelper.PublishWorkerLog(this, "Job [" +
                                                 strJobId + "] done in [" +
                                                 Minutes.minutesBetween(
                                                		 workTimeLog, 
                                                		 DateTime.now()).getMinutes() + 
                                                 "] mins");
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        finally{
            m_jobsToDo.remove(strJobId);
        }
    }

    public void Dispose()
    {
        if(DistTopicQueue != null)
        {
            DistTopicQueue.Dispose();
            DistTopicQueue = null;
        }

        if(DistWorkerToContollerHeartBeat != null)
        {
            DistWorkerToContollerHeartBeat.Dispose();
            DistWorkerToContollerHeartBeat = null;
        }

        if(JobsCompletedMap != null)
        {
            JobsCompletedMap.clear();
            JobsCompletedMap = null;
        }

        if(CalcTypesSet != null)
        {
            CalcTypesSet.clear();
            CalcTypesSet = null;
        }

        if(m_jobsToDo != null)
        {
            m_jobsToDo.clear();
            m_jobsToDo = null;
        }

        if(m_distWorkerResultSender != null)
        {
            m_distWorkerResultSender.Dispose();
            m_distWorkerResultSender = null;
        }

        if(m_distWorkerJobPull != null)
        {
            m_distWorkerJobPull.Dispose();
            m_distWorkerJobPull = null;
        }

        if(m_clockTickWorker != null)
        {
            m_clockTickWorker.Dispose();
            m_clockTickWorker = null;
        }

        if(m_distWorkerJobPull != null)
        {
            m_distWorkerJobPull.Dispose();
            m_distWorkerJobPull = null;
        }
    }

	public static void loadDistributedWorker() 
	{
		try
		{
			String strServerName = Config.getStringStatic(
					"TopicServerName",
					SimpleUiSocket.class);
			final int intReqRespPort = Integer.parseInt(
					Config.getStringStatic("ReqRespPort",
							SimpleUiSocket.class));
			ZmqReqRespClient.Connect(strServerName, intReqRespPort, 5);
			DistWorker.Connect(Config.getStringStatic(
					"TopicServerName",
					SimpleUiSocket.class));
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
}
