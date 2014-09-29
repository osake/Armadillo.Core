package Armadillo.Communication.Impl.Distributed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Communication.Impl.Topic.SubscriberCallbackDel;
import Armadillo.Communication.Impl.Topic.TopicConstants;
import Armadillo.Communication.Impl.Topic.TopicMessage;
import Armadillo.Communication.Impl.Topic.TopicPublisherCache;
import Armadillo.Communication.Impl.Topic.TopicSubscriberCache;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Core.Math.RollingWindowStdDev;
import Armadillo.Core.SelfDescribing.ASelfDescribingClass;
import Armadillo.Core.SelfDescribing.SelfDescribingClass;
import Armadillo.Core.Text.StringHelper;

public class DistControllerToWorkerHeartBeat {
    
	public ConcurrentHashMap<String, String> WorkersStatus;
    public RollingWindowStdDev PingLatencySecs;
    public ConcurrentHashMap<String, DateTime> WorkersPingTimes;
    private String m_strControllerId;
    private DistController m_distController;
    private ThreadWorker<ObjectWrapper> m_clockThreadWorker;
    public ConcurrentHashMap<String, DateTime> WorkersJobsInProgress;

    public DistControllerToWorkerHeartBeat(DistController distController)
    {
        try
        {
        	WorkersJobsInProgress = new ConcurrentHashMap<String, DateTime>();
            m_distController = distController;
            m_strControllerId = distController.ControllerId;
            PingLatencySecs = new RollingWindowStdDev(20);
            WorkersPingTimes = new ConcurrentHashMap<String, DateTime>();
            WorkersStatus = new ConcurrentHashMap<String, String>();
            String strTopic = m_distController.GridTopic + EnumDistributed.TopicWorkerToControllerHeartBeat.toString();
            
            TopicSubscriberCache.GetSubscriber(
                distController.ServerName,
                TopicConstants.PUBLISHER_HEART_BEAT_PORT).Subscribe(
                strTopic,
                new SubscriberCallbackDel(){
                	public void invoke(TopicMessage topicMessage) {
                		OnTopicWorkerToControllerHeartBeat(topicMessage);
                	};}
                );
            
            TopicSubscriberCache.GetSubscriber(
                    distController.ServerName,
                    TopicConstants.PUBLISHER_HEART_BEAT_PORT).Subscribe(
                    		EnumDistributed.WorkerJobsToDoTopic.toString(),
                    new SubscriberCallbackDel(){
                    	public void invoke(TopicMessage topicMessage) {
                    		try{
                    			String strJobId = (String)topicMessage.EventData;
                    			WorkersJobsInProgress.put(strJobId, DateTime.now());
                    		}
                    		catch(Exception ex){
                    			Logger.log(ex);
                    		}
                    	};}
                    );

            
            m_clockThreadWorker = new ThreadWorker<ObjectWrapper>(){
            	@Override
            	public void runTask(ObjectWrapper item) {
            		try{
	            		while(true)
	            		{
	            			try{
	            				OnClockTick();
	            			}
	            			catch(Exception ex){
	            				Logger.log(ex);
	            			}
	            			finally{
	            				Thread.sleep(3000);
	            			}
	            		}
	    			}
	    			catch(Exception ex){
	    				Logger.log(ex);
	    			}
            	}
            };
            m_clockThreadWorker.work();
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private void OnTopicWorkerToControllerHeartBeat(TopicMessage topicmessage)
    {
        try
        {
            ASelfDescribingClass workerResponse = (ASelfDescribingClass)(topicmessage.EventData);
            String strWorkerId = workerResponse.GetStrValue(EnumDistributed.WorkerId);
            DateTime timeSent = new DateTime(workerResponse.GetDateValue(EnumDistributed.TimeControllerToWorker));
            DateTime now = DateTime.now();
            PingLatencySecs.Update(
            		Seconds.secondsBetween(timeSent, now).getSeconds());
            
            if (!WorkersPingTimes.containsKey(strWorkerId))
            {
                String strMessage = "Connected worker [" + strWorkerId + "]";
                DistGuiHelper.PublishControllerLog(m_distController, strMessage);
            }
            WorkersPingTimes.put(strWorkerId, now);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private void OnClockTick()
    {
        DistGuiHelper.PublishControllerLog(m_distController, "Started worker pinger...");
        while (true)
        {
            try
            {
                PingWorker();
                CheckAliveWorkers();
                
                //
                // flush old jobs in progress
                //
                ArrayList<String> keysToDelete = new ArrayList<String>(); 
                for(Entry<String, DateTime> kvp : WorkersJobsInProgress.entrySet()){
                	int intMinutes = Minutes.minutesBetween(
                			kvp.getValue(),
                			DateTime.now()).getMinutes();
                	if(intMinutes > 60){
                		keysToDelete.add(kvp.getKey());
                	}
                }
                for(String strKey : keysToDelete){
                	WorkersJobsInProgress.remove(strKey);
                }
            }
            catch (Exception ex)
            {
                Logger.log(ex);
            }
            try {
				Thread.sleep(1000 * DistConstants.PING_WORKER_TIME_SECS);
			} catch (InterruptedException e) {
				Logger.log(e);
			}
        }
    }

    public void CheckWorkersJobsInProgress(
    		String strJobId,
    		String strWorkerId) {
		try{
			if(!WorkersJobsInProgress.containsKey(strJobId)){
				WorkersJobsInProgress.put(strJobId, DateTime.now());
			}
			DateTime lastPingTime = WorkersJobsInProgress.get(strJobId);
			int intTotalSeconds = Seconds.secondsBetween(lastPingTime, DateTime.now()).getSeconds();
			if(intTotalSeconds > 120){
				//
				// job is no longer being done by worker
				//
                RemoveWorker(strWorkerId, intTotalSeconds);
                WorkersPingTimes.remove(strWorkerId);
			}
		}
		catch(Exception ex){
			Logger.log(ex);
		}
	}

	private void CheckAliveWorkers()
    {
        try
        {
            DateTime now = DateTime.now();
            for (Map.Entry<String, DateTime> kvp : WorkersPingTimes.entrySet())
            {
                int intTotalSeconds = (int) Seconds.secondsBetween(kvp.getValue(), now).getSeconds();
                if (intTotalSeconds > DistConstants.ALIVE_WORKER_TIME_SECS)
                {
                    RemoveWorker(kvp.getKey(), intTotalSeconds);
                    WorkersPingTimes.remove(kvp.getKey());
                }
                else
                {
                    WorkersStatus.put(kvp.getKey(), EnumDistributed.Connected.toString());
                }
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public void RemoveJobsInProgressFromRequestor(String strRequestorName)
    {
        try
        {
            Set<Entry<String, ASelfDescribingClass>> jobsInProgressArr;
            synchronized (m_distController.DistControllerJobPull.JobsInProgressLock)
            {
                jobsInProgressArr = m_distController.JobsToDoMap.entrySet();
            }
            for (Entry<String, ASelfDescribingClass> kvp : jobsInProgressArr)
            {
                boolean blnDoRemove = false;
                String strJobId = kvp.getKey();
                ASelfDescribingClass currParams = kvp.getValue();
                String strCurrRequestorName = currParams.TryGetStrValue(
                		EnumDistributed.RequestorName);
                if (!StringHelper.IsNullOrEmpty(strCurrRequestorName))
                {
                    if (strCurrRequestorName.equals(strRequestorName))
                    {
                        blnDoRemove = true;
                    }
                }
                else
                {
                    blnDoRemove = true;
                }
                if (blnDoRemove)
                {
                    synchronized (m_distController.DistControllerJobPull.JobsInProgressLock)
                    {
                        ASelfDescribingClass resultTsEv;
                        if (m_distController.JobsToDoMap.containsKey(
                            kvp.getKey()))
                        {
                        	resultTsEv = m_distController.JobsToDoMap.get(
                        			kvp.getKey());
                        	m_distController.JobsToDoMap.remove(
                                    kvp.getKey());
                            String strMessage = "Calc engine successfully flushed job [" + strJobId +
                                "] from client [" + strRequestorName + "]";
                            SelfDescribingClass resultObj = new SelfDescribingClass();
                            resultObj.SetClassName(
                            		getClass().getName() + "_ResultFlush");
                            DistGuiHelper.PublishControllerLog(
                                m_distController, 
                                strMessage);
                            resultObj.SetBlnValue(
                                EnumCalcCols.IsClientDisconnected,
                                true);
                            resultObj.SetStrValue(
                                EnumCalcCols.Error,
                                strMessage);
                            resultTsEv.SetObjValueToDict(
                                EnumCalcCols.Result,
                                resultObj);
                        }
                        if(m_distController.DistControllerJobPull.MapJobIdToWorkerId.containsKey(
                        		strJobId)){
                        	
	                        ASelfDescribingClass jobLog = m_distController.DistControllerJobPull.MapJobIdToWorkerId.get(
	                        		strJobId);
	                        m_distController.DistControllerJobPull.MapJobIdToWorkerId.remove(
	                            strJobId);
	                        DistGuiHelper.PublishJobLogStatus(
	                            m_distController,
	                            jobLog,
	                            "Removed");
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public boolean IsWorkerConnected(String strWorkerId)
    {
        if(!WorkersStatus.containsKey(strWorkerId))
        {
            return true;
        }
        String workerStatus = WorkersStatus.get(strWorkerId);
        return workerStatus.equals(EnumDistributed.Connected.toString());
    }

    private void RemoveWorker(
    		String strWorkerId, 
    		int intTotalSeconds)
    {
        try
        {
            DistGuiHelper.PublishControllerLog(
                m_distController,
                "Disconnected worker[" +
                    strWorkerId + "][" + intTotalSeconds + "]secs");

            WorkersStatus.put(strWorkerId, EnumDistributed.Disconnected.toString());
            
            List<String> assignedJobs = new ArrayList<String>();
            for(Entry<String, ASelfDescribingClass> kvp2 : 
            	m_distController.DistControllerJobPull.MapJobIdToWorkerId.entrySet()){
            	
            	if(DistControllerJobLogger.GetWorkerId(kvp2.getValue()).equals(strWorkerId)){
            		assignedJobs.add(kvp2.getKey());
            	}
            }
//            (from n in m_distController.DistControllerJobPull.MapJobIdToWorkerId
//                               where DistControllerJobLogger.GetWorkerId(n.Value).Equals(strWorkerId)
//                               select n.Key).ToList();
            
            for(String strJobId : assignedJobs)
            {
            	if(m_distController.DistControllerJobPull.MapJobIdToWorkerId.containsKey(strJobId))
            	{
            		ASelfDescribingClass jobLog = 
            				m_distController.DistControllerJobPull.MapJobIdToWorkerId.get(strJobId);
            	
	                m_distController.DistControllerJobPull.MapJobIdToWorkerId.remove(
	                    strJobId);
	                DistGuiHelper.PublishJobLogStatus(
	                    m_distController,
	                    jobLog,
	                    "ClientDisconnected");
	                DistGuiHelper.PublishControllerLog(m_distController,
	                    "Removed worker[" +
	                    strWorkerId + "]. Job id [" +
	                    strJobId +"]");
            	}
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private void PingWorker()
    {
        try
        {
            if(m_distController.DistTopicQueue == null)
            {
                return;
            }
            SelfDescribingClass calcParams = new SelfDescribingClass();
            calcParams.SetClassName(EnumDistributed.HeartBeatWorkerClass);
            calcParams.SetStrValue(
                EnumDistributed.ControllerId,
                m_strControllerId);
            calcParams.SetDateValue(
                EnumDistributed.TimeControllerToWorker,
                DateTime.now().toDate());
            calcParams.SetDateValue(
                EnumDistributed.Time,
                DateTime.now().toDate());
            String strTopic = m_distController.GridTopic + 
                            EnumDistributed.TopicControllerToWorkerHeartBeat.toString();
            TopicPublisherCache.GetPublisher(
                m_distController.ServerName,
                TopicConstants.SUBSCRIBER_HEART_BEAT_PORT).SendMessageImmediately(
                calcParams,
                strTopic);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public void Dispose()
    {
        if(WorkersStatus != null)
        {
            WorkersStatus.clear();
            WorkersStatus = null;
        }
        if(PingLatencySecs != null)
        {
            PingLatencySecs.Dispose();
            PingLatencySecs = null;
        }

        if(WorkersPingTimes != null)
        {
            WorkersPingTimes.clear();
            WorkersPingTimes = null;
        }
         m_distController = null;

        if(m_clockThreadWorker != null)
        {
            m_clockThreadWorker.Dispose();
            m_clockThreadWorker = null;
        }
    }

}
