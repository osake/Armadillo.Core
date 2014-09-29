package Armadillo.Communication.Impl.Distributed;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;

import Armadillo.Core.Config;
import Armadillo.Core.Console;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Communication.Impl.SimpleUiSocket;
import Armadillo.Communication.Impl.Topic.SubscriberCallbackDel;
import Armadillo.Communication.Impl.Topic.TopicMessage;
import Armadillo.Communication.Impl.Topic.TopicPublisher;
import Armadillo.Communication.Impl.Topic.TopicPublisherCache;
import Armadillo.Communication.Impl.Topic.TopicSubscriberCache;
import Armadillo.Core.Concurrent.LockHelper;
import Armadillo.Core.SelfDescribing.ASelfDescribingClass;
import Armadillo.Core.SelfDescribing.SelfDescribingTsEvent;
import Armadillo.Core.Text.StringHelper;

public class DistController {
	
    public ConcurrentHashMap<String, ASelfDescribingClass> JobsToDoMap;
    public String ServerName;
    public String ControllerId;
    public String GridTopic;
    private static ConcurrentHashMap<String, DistController> Instances;
    
    public int JobsDone()
    {
        return m_intJobsDone; 
    }

    public DistControllerToWorkerHeartBeat DistControllerToWorkerHeartBeat;

    public DistTopicQueue DistTopicQueue;
    public Boolean IsReady = false;

    public ConcurrentHashMap<String, JobDoneWrapper> JobsDoneMap;

    public DistControllerJobPull DistControllerJobPull;
    
    private static final Object m_connectionLock = new Object();
    private int m_intJobsDone;
    public final Object m_jobReadyLock = new Object();
    private static int m_intControllerCounter;
    private static Object m_controllerCounterLockObj = new Object();

    static
    {
        Instances = new ConcurrentHashMap<String, DistController>();
    }

    private DistController(String strGridTopic)
    {
        try
        {
            JobsDoneMap = new ConcurrentHashMap<String, JobDoneWrapper>();
            DistGuiHelper.ResetGui();
            //
            // get params first
            //
            synchronized(m_controllerCounterLockObj){
            	m_intControllerCounter++;
            }
            JobsToDoMap = new ConcurrentHashMap<String, ASelfDescribingClass>();
            ControllerId = 
                "Controller_" +
                Config.getClientName() + "_" +
                strGridTopic + "_" +
                m_intControllerCounter;
            String strMessage = "Starting controller [" + 
            		ControllerId +
            		"]...";
            Logger.log(strMessage);
            Console.writeLine(strMessage);
            
            ServerName = Config.getStringStatic(
            		"TopicServerName",
            		SimpleUiSocket.class);
            GridTopic = strGridTopic;

            //
            // set objects
            //
            DistControllerToWorkerHeartBeat = new DistControllerToWorkerHeartBeat(this);
            TopicSubscriberCache.GetSubscriber(ServerName).Subscribe(
                GridTopic + EnumDistributed.TopicWorkerToControllerResult.toString(),
                new SubscriberCallbackDel(){
                	public void invoke(TopicMessage topicMessage) {
                		OnTopicWorkerToControllerResult(topicMessage);
                	};
                });

            DistTopicQueue = new DistTopicQueue(ServerName);
            DistControllerJobPull = new DistControllerJobPull(this);
            IsReady = true;
            strMessage = "Started controller [" + 
            		ControllerId +
            		"]";
            Logger.log(strMessage);
            Console.writeLine(strMessage);
            
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public static DistController GetController(String strTopic)
    {
        try
        {
        	DistController distController;
            if (Instances.containsKey(strTopic))
            {
            	distController = Instances.get(strTopic);
            	if(distController != null){
            		return distController;
            	}
            }
            synchronized (m_connectionLock)
            {
                if (Instances.containsKey(strTopic))
                {
                	distController = Instances.get(strTopic);
                	if(distController != null){
                		return distController;
                	}
                }
                distController = new DistController(strTopic);
                Instances.put(strTopic, distController);
                Logger.log(Instances.getClass().getName() + " is now connected. Id = " +
                    Instances.get(strTopic).ControllerId);
                return distController;
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        return null;
    }

    public ASelfDescribingClass DoWork(ASelfDescribingClass paramsClass)
    {
        try
        {
            if(StringHelper.IsNullOrEmpty(paramsClass.GetClassName()))
            {
                throw new HCException("Empty class name");
            }

            String strJobId = paramsClass.TryGetStrValue(EnumDistributed.JobId);
            if (StringHelper.IsNullOrEmpty(strJobId))
            {
                throw new HCException("Job id not found");
            }
            synchronized (LockHelper.GetLockObject(strJobId))
            {
                String strMessage;
                if(JobsDoneMap.containsKey(strJobId))
                {
                    strMessage = "Job[" + strJobId + "] already done. Found in map";
                    Console.writeLine(strMessage);
                    Logger.log(strMessage);
                    return null;
                }
                paramsClass.SetStrValue(
                    EnumDistributed.ControllerId,
                    ControllerId);

                //
                // check flush task
                //
                Boolean blnFlushJobs = paramsClass.TryGetBlnValue(
                        EnumCalcCols.FlushJobs);
                if (blnFlushJobs)
                {
                    SelfDescribingTsEvent resultTsEv = DistControllerJobFlush.DoFlushJobs(
                        this,
                        paramsClass,
                        strJobId,
                        DistControllerJobPull,
                        JobsToDoMap);
                    
                    return resultTsEv;
                }

                //
                // this is the job to be pulled by the workers
                //
                JobsToDoMap.put(strJobId, paramsClass); // make sure this is set before advertising it!

                //
                // wait until job is done
                //
                int intWaitCounter = 0;
                Object results = null;
                while (JobsToDoMap.containsKey(strJobId) && // if it is not here, then it has been flushed
                    (!DistControllerJobPull.MapJobIdToWorkerId.containsKey(strJobId) ||
                       ((results = paramsClass.TryGetObjValue(
                           EnumDistributed.Result)) == null)))
                {
                    intWaitCounter += 100;
                    if (intWaitCounter > 5000)
                    {
                        intWaitCounter = 0;
                        if (!DistControllerJobPull.MapJobIdToWorkerId.containsKey(strJobId))
                        {
                            strMessage = "Controller is wating for any worker to pickup JobId [" +
                                         strJobId + "]";
                            Console.writeLine(strMessage);
                            Logger.log(strMessage);
                        }
                        else
                        {
                            if(JobsDoneMap.containsKey(strJobId))
                            {
                                strMessage = "Job[" + strJobId + "] already done";
                                Console.writeLine(strMessage);
                                Logger.log(strMessage);
                                return null;
                            }
                            String strWorkerId = paramsClass.TryGetStrValue(EnumDistributed.WorkerId);
                            if(Armadillo.Core.Text.StringHelper.IsNullOrEmpty(strWorkerId)){
                            	throw new HCException("Worker id not found");
                            }
                            
                            //
                            // check hertbeats
                            //
                            if(!DistControllerToWorkerHeartBeat.WorkersPingTimes.containsKey(
                            		strWorkerId)){
                            	DistControllerToWorkerHeartBeat.WorkersPingTimes.put(strWorkerId, 
                            			DateTime.now());
                            }
                            DistControllerToWorkerHeartBeat.CheckWorkersJobsInProgress(
                            		strJobId, 
                            		strWorkerId);
                            strMessage = "Controller is wating for worker [" +
                            			 strWorkerId + "] to send result. JobId [" +
                                         strJobId + "]";
                            Console.writeLine(strMessage);
                            Logger.log(strMessage);
                        }
                    }
                    Thread.sleep(100);
                }
                strMessage = "-Result found for jobID [" +
                             strJobId + "]";
                Console.writeLine(strMessage);
                Logger.log(strMessage);

                //
                // get rid of the job
                //
                synchronized (m_jobReadyLock)
                {
                    Boolean blnSucessDone = true;
                    
                    if (!JobsToDoMap.containsKey(strJobId))
                    {
                        strMessage = "Job in progress not found: " + strJobId;
                        DistGuiHelper.PublishControllerLog(this, strMessage);
                        blnSucessDone = false;
                    }
                    else{
                    	JobsToDoMap.remove(strJobId);
                    }
                    ASelfDescribingClass jobLog = null;
                    synchronized (DistControllerJobPull.JobsInProgressLock)
                    {
                        if (!DistControllerJobPull.MapJobIdToWorkerId.containsKey(
                            strJobId))
                        {
                            strMessage = "JobId to worker not found: " + strJobId;
                            DistGuiHelper.PublishControllerLog(this, strMessage);
                            blnSucessDone = false;
                        }
                        else{
                        	jobLog = DistControllerJobPull.MapJobIdToWorkerId.get(
                                    strJobId);
                        }
                    }
                    if(blnSucessDone && results == null)
                    {
                        strMessage = "DistController warning. Null result for job[ " + strJobId + "]";
                        DistGuiHelper.PublishControllerLog(this, strMessage);
                        blnSucessDone = false;
                    }
                    if (blnSucessDone)
                    {
                        strMessage = "**-- Success job done [" + strJobId + "]";
                        Console.writeLine(strMessage);
                        Logger.log(strMessage);
                        String strWorkerId = DistControllerJobLogger.GetWorkerId(jobLog);
                        int intJobsDone = 0;
                        
                        if(DistControllerJobPull.MapWorkerToJobsDone.containsKey(
                            strWorkerId)){
                        	
                        	intJobsDone = DistControllerJobPull.MapWorkerToJobsDone.get(
	                            strWorkerId);
                        }
                        intJobsDone++;
                        DistControllerJobPull.MapWorkerToJobsDone.put(strWorkerId, intJobsDone);
                        DistGuiHelper.PublishJobLogDone(this, jobLog);
                    }
                    
                    if(DistControllerJobPull.MapJobIdToWorkerId.containsKey(strJobId)){
	                    
                    	DistControllerJobPull.MapJobIdToWorkerId.remove(
	    	                    strJobId);
                    }
                    JobsDoneMap.put(
                    		strJobId, 
                    		new JobDoneWrapper(
                    				blnSucessDone));
                    return (ASelfDescribingClass) results;
                }
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        finally
        {
            //
            // the reult flag is no longer needed
            //
            HashMap<String, Object> objValues = paramsClass.GetObjValues();
            if (objValues != null)
            {
                objValues.remove(EnumCalcCols.Result.toString());
            }
            synchronized(m_connectionLock){
            	m_intJobsDone++;
            }
        }
        try {
			throw new HCException("Null result");
		} catch (HCException e) {
			Logger.log(e);
		}
        return null;
    }

    private void OnTopicWorkerToControllerResult(TopicMessage topicmessage)
    {
        try
        {
            ASelfDescribingClass results = (ASelfDescribingClass)(topicmessage.EventData);
            String strControllerId = results.GetStrValue(EnumDistributed.ControllerId);
            if (!strControllerId.equals(ControllerId))
            {
                //
                // this result was sent to another parent
                //
                return;
            }
            String strJobId = results.GetStrValue(EnumDistributed.JobId);

            Boolean blnJobFound = false;
            ASelfDescribingClass resultWaiting;
            if (!JobsToDoMap.containsKey(strJobId) ||
                (resultWaiting = JobsToDoMap.get(strJobId)) == null)
            {
                String strMessage = "Result not found. JobId [" + 
                    strJobId + "]";
                DistGuiHelper.PublishControllerLog(this,strMessage);
            }
            else
            {
                String strMessage = "Found result. JobId [" +
                    strJobId + "]";
                Console.writeLine(strMessage);
                Logger.log(strMessage);
                blnJobFound = true;
                resultWaiting.SetObjValueToDict(
                    EnumDistributed.Result,
                    results);
            }
            String strWorkerId = results.TryGetStrValue(EnumDistributed.WorkerId);
            if(StringHelper.IsNullOrEmpty(strWorkerId))
            {
                throw new HCException("Worker id not found");
            }

            //
            // acknowledge the result
            //
            DistControllerJobPull.SendJobDoneAck(
                strJobId,
                strWorkerId);
            
            //
            // publish resut to all other workers
            //
            if (blnJobFound)
            {
                TopicMessage topicMessage = TopicPublisher.PrepareEvent(
                    strJobId,
                    GridTopic + EnumDistributed.JobsDoneTopic.toString());

                TopicPublisherCache.GetPublisher(ServerName).SendMessage(
                    false,
                    topicMessage);
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public void Dispose()
    {
        if(JobsToDoMap != null)
        {
            JobsToDoMap.clear();
            JobsToDoMap = null;
        }

        if(DistControllerToWorkerHeartBeat != null)
        {
            DistControllerToWorkerHeartBeat.Dispose();
            DistControllerToWorkerHeartBeat = null;
        }

        if(DistTopicQueue != null)
        {
            DistTopicQueue.Dispose();
            DistTopicQueue = null;
        }

        if (DistControllerJobPull != null)
        {
            DistControllerJobPull.Dispose();
            DistControllerJobPull = null;
        }
    }
}
