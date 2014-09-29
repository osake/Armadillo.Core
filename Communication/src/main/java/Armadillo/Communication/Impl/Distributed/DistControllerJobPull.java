package Armadillo.Communication.Impl.Distributed;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import Armadillo.Core.Console;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Communication.Impl.Topic.SubscriberCallbackDel;
import Armadillo.Communication.Impl.Topic.TopicMessage;
import Armadillo.Communication.Impl.Topic.TopicPublisherCache;
import Armadillo.Communication.Impl.Topic.TopicSubscriberCache;
import Armadillo.Core.Concurrent.EfficientProducerConsumerQueue;
import Armadillo.Core.Concurrent.LockHelper;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Core.SelfDescribing.ASelfDescribingClass;
import Armadillo.Core.SelfDescribing.SelfDescribingClass;
import Armadillo.Core.Text.StringHelper;

public class DistControllerJobPull {
    
	private static final int PULL_WAIT_MILLS = 20;
    public ConcurrentMap<String, ASelfDescribingClass> MapJobIdToWorkerId;
    public Object JobsInProgressLock;
    public ConcurrentHashMap<String, Integer> MapWorkerToJobsDone;
    private DistController m_distController;
    private EfficientProducerConsumerQueue<ASelfDescribingClass> m_jobQueue;
    private ConcurrentHashMap<String, Object> m_jobsToAck;
    private final ConcurrentHashMap<String, DateTime> m_pullIds = new ConcurrentHashMap<String, DateTime>();
    private final ConcurrentHashMap<String, Object> m_jobsToPull = new ConcurrentHashMap<String, Object>(); 
    private static final Object m_toPullLock = new Object();
    private ThreadWorker<ObjectWrapper> m_clockTickWorker;
	private ThreadWorker<ObjectWrapper> m_pullIdsFlusherWorker;
	private final Object m_mapJobIdToWorkerIdLock = new Object();

    public DistControllerJobPull(DistController distController)
    {
        try
        {
            m_jobsToAck = new ConcurrentHashMap<String, Object>();
            m_distController = distController;
            MapWorkerToJobsDone = new ConcurrentHashMap<String, Integer>();
            JobsInProgressLock = new Object();
            m_distController = distController;
            MapJobIdToWorkerId = new ConcurrentHashMap<String, ASelfDescribingClass>();

            SetupClockTick();

            if (StringHelper.IsNullOrEmpty(m_distController.GridTopic))
            {
                throw new HCException("Empty grid topic");
            }
            
            m_jobQueue = new EfficientProducerConsumerQueue<ASelfDescribingClass>(5){
            	
            	@Override
            	public void runTask(ASelfDescribingClass item) {
            		OnQueueConsume(item);
            	}
            };
            TopicSubscriberCache.GetSubscriber(distController.ServerName).Subscribe(
                distController.GridTopic + EnumDistributed.TopicWorkerToControllerPullJob.toString(),
                new SubscriberCallbackDel(){
                	
                	public void invoke(TopicMessage topicMessage) {
                		OnTopicWorkerToControllerPullJob(topicMessage);
                	};
                }
                );
            TopicSubscriberCache.GetSubscriber(distController.ServerName).Subscribe(
                distController.GridTopic + EnumDistributed.TopicWorkerToControllerPullJobAck.toString(),
                new SubscriberCallbackDel(){
                	
                	public void invoke(TopicMessage topicMessage) {
                		OnTopicWorkerToControllerPullJobAck(topicMessage);
                	};
                });
            
            m_pullIdsFlusherWorker = new ThreadWorker<ObjectWrapper>(){
            	@Override
            	public void runTask(ObjectWrapper item) throws Exception {
            		try{
	            		while(true){
	            			
	            			try{
	            				
	            				//
	            				// flush pull ids
	            				//
		            			if(m_pullIds.size() > 10e3){
		            				
		            				ArrayList<String> pullIdsToDelete = new ArrayList<String>(); 
			            			for(Entry<String, DateTime> kvp : m_pullIds.entrySet()){
			            				double dblTotalMins = Minutes.minutesBetween(
			            						kvp.getValue(),
			            						DateTime.now()).getMinutes();
			            				if(dblTotalMins > 30){
			            					String strKeyId = kvp.getKey();
			            					pullIdsToDelete.add(strKeyId);
			            					String strMessage = "Deleting old pull id [" +
			            							strKeyId + "]";
			            					Logger.log(strMessage);
			            					Console.writeLine(strMessage);
			            				}
			            			}
			            			if(pullIdsToDelete.size() > 0){
			            				for(String strPullId : pullIdsToDelete){
			            					m_pullIds.remove(strPullId);
			            				}
			            			}
		            			}
		            			//
		            			// flush jobs done
		            			//
		            			if(m_distController.JobsDoneMap.size() > 10e3){
		            				ArrayList<String> jobsDoneToDelete = new ArrayList<String>(); 
		            				
		            				for(Entry<String, JobDoneWrapper> kvp : m_distController.JobsDoneMap.entrySet()){
		            					JobDoneWrapper jobDoneWrapper = kvp.getValue();
			            				
		            					double dblTotalMins = Minutes.minutesBetween(
		            							jobDoneWrapper.m_dateCreated,
			            						DateTime.now()).getMinutes();
			            				if(dblTotalMins > 30){
			            					String strKeyId = kvp.getKey();
			            					jobsDoneToDelete.add(strKeyId);
			            					String strMessage = "Deleting old job done id [" +
			            							strKeyId + "]";
			            					Logger.log(strMessage);
			            					Console.writeLine(strMessage);
			            				}
		            					
		            				}
		            				
			            			if(jobsDoneToDelete.size() > 0){
			            				for(String strJobId : jobsDoneToDelete){
			            					m_distController.JobsDoneMap.remove(strJobId);
			            				}
			            			}
		            			}
	            			}
	            			catch(Exception ex){
	            				Logger.log(ex);
	            			}
	            			Thread.sleep(60*1000);
	            		}
            		}
            		catch(Exception ex){
            			Logger.log(ex);
            		}
            	}
            };
            m_pullIdsFlusherWorker.work();
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private void SetupClockTick()
    {
        try
        {
            m_clockTickWorker = new ThreadWorker<ObjectWrapper>(){
            	
            	@Override
            	public void runTask(ObjectWrapper item) {
                    while (true)
                    {
                        try
                        {
                            OnClockTick();
                        }
                        catch (Exception ex)
                        {
                            Logger.log(ex);
                        }
                        try {
							Thread.sleep(DistConstants.JOB_ADVERTISE_TIME_SECS*1000);
						} catch (InterruptedException e) {
							Logger.log(e);
						}
                    }
            	}
            };
            m_clockTickWorker.work();
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public void SendJobDoneAck(
        String strJobId,
        String strWorkerId)
    {
        try
        {
            ASelfDescribingClass doWorkClass = new SelfDescribingClass();
            doWorkClass.SetClassName(EnumDistributed.JobDoneAck);
            doWorkClass.SetStrValue(EnumDistributed.JobId,
                                    strJobId);
            doWorkClass.SetStrValue(EnumDistributed.WorkerId,
                                    strWorkerId);

            TopicPublisherCache.GetPublisher(m_distController.ServerName).SendMessage(
                doWorkClass,
                m_distController.GridTopic + EnumDistributed.TopicControllerToWorkerResultConfirm.toString(),
                true);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private void OnClockTick()
    {
        try
        {
            //AdvertiseJobs();
            DistGuiHelper.PublishControllerGui(m_distController);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private void OnTopicWorkerToControllerPullJobAck(
        TopicMessage topicmessage)
    {
        try
        {
            String strPullId = (String) topicmessage.EventData;
            if (m_jobsToAck.containsKey(strPullId))
            {
                String strMessage = "Successfuly acked pullid [" + strPullId + "]";
                Console.writeLine(strMessage);
                Logger.log(strMessage);
                m_jobsToAck.remove(strPullId);
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }
    
    private static Hashtable<String,Object> m_debugMap = new Hashtable<String,Object>(); 
//    private static Hashtable<String,Object> m_lockMap = new Hashtable<String,Object>(); 

    /// <summary>
    /// Method should be thread safe
    /// </summary>
    /// <param name="pullParams"></param>
    private void OnQueueConsume(ASelfDescribingClass pullParams)
    {
    	String strPullId = null;
        try
        {
            strPullId = pullParams.TryGetStrValue(EnumDistributed.PullId);
            if (StringHelper.IsNullOrEmpty(strPullId))
            {
                throw new HCException("Pull id [" + strPullId + "] not found");
            }

            Object lockObj = LockHelper.GetLockObject(strPullId);
            
            synchronized (lockObj)
            {
        		try{
        		while(m_debugMap.containsKey(strPullId)){
            		Thread.sleep(1000);
            	}
            	
            	m_debugMap.put(strPullId, new Object());
            	
            	String strMessage;
            	
                if (m_pullIds.containsKey(strPullId))
                {
                    strMessage = "PullId [" + strPullId + "] already pulled";
                    Console.writeLine(strMessage);
                    Logger.log(strMessage);
                    return;
                }

                strMessage = "Pulling job from pull id [" + strPullId + "]...";
                Console.writeLine(strMessage);
                Logger.log(strMessage);

                //
                // loop until a job to do is found
                //
                ASelfDescribingClass jobParams = PullJob(strPullId);

                String strWorkerId = pullParams.TryGetStrValue(EnumDistributed.WorkerId);
                if (StringHelper.IsNullOrEmpty(strWorkerId))
                {
                    throw new HCException("Worker id not found");
                }

                String strJobId = jobParams.TryGetStrValue(EnumDistributed.JobId);
                if (StringHelper.IsNullOrEmpty(strJobId))
                {
                    throw new HCException("Job id not found");
                }

                //
                // wait for worker to confirm ack
                //
                if (!WaitForWorkerToConfirm(
                    jobParams,
                    strWorkerId,
                    strPullId))
                {
                    m_jobsToPull.remove(strJobId);
                    strMessage = "***Worker is disconnected";
                    Console.writeLine(strMessage);
                    Logger.log(strMessage);
                    return;
                }

                synchronized(m_mapJobIdToWorkerIdLock )
                {
                	if(MapJobIdToWorkerId.containsKey(strJobId)){
                        strMessage = "***JobId [" + 
                        		strJobId + "] already taken by another worker";
                        Console.writeLine(strMessage);
                        Logger.log(strMessage);
                        return;
                	}
                
                
	                SelfDescribingClass jobLog = DistControllerJobLogger.GetJobLog(
	                    strWorkerId,
	                    strJobId,
	                    strPullId);
	
	                if (MapJobIdToWorkerId.containsKey(strJobId))
	                {
	                    throw new HCException("Job id already assigned to a worker");
	                }
	
	                MapJobIdToWorkerId.put(strJobId, jobLog);
	                m_jobsToPull.remove(strJobId);
	
	                DistGuiHelper.PublishJobLog(
	                    m_distController,
	                    strWorkerId,
	                    strJobId,
	                    jobLog);
	
	                m_pullIds.put(strPullId, DateTime.now());
	            	
                }
                
            }
            catch (Exception ex)
            {
                Logger.log(ex);
            }
            finally{
            	if(!StringHelper.IsNullOrEmpty(strPullId))
            	{
            		m_debugMap.remove(strPullId);
            	}
            }
                
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private ASelfDescribingClass PullJob(
        String strPullId)
    {
        try
        {
            ASelfDescribingClass jobParams = null;
            boolean blnFoundJob = false;
            int intLogTimeCounter = 0;
            String strMessage;
            while (!blnFoundJob)
            {
                if (m_distController.JobsToDoMap.size() > 0)
                {
                	List<Map.Entry<String, ASelfDescribingClass>> jobsUnassigned = 
                			new ArrayList<Map.Entry<String, ASelfDescribingClass>>();
                	
                	for(Map.Entry<String, ASelfDescribingClass> kvp : m_distController.JobsToDoMap.entrySet())
                	{
                		if(!MapJobIdToWorkerId.containsKey(kvp.getKey()) &&
                               !m_jobsToPull.containsKey(kvp.getKey())){
                			jobsUnassigned.add(kvp);
                		}
                		
                	}
                	
                    if (jobsUnassigned.size() > 0)
                    {
                        synchronized (m_toPullLock)
                        {
                        	
                        	jobsUnassigned = 
                        			new ArrayList<Map.Entry<String, ASelfDescribingClass>>();
                        	
                        	for(Map.Entry<String, ASelfDescribingClass> kvp : m_distController.JobsToDoMap.entrySet())
                        	{
                        		if(!MapJobIdToWorkerId.containsKey(kvp.getKey()) &&
                                       !m_jobsToPull.containsKey(kvp.getKey())){
                        			jobsUnassigned.add(kvp);
                        		}
                        		
                        	}
                        	
                            if (jobsUnassigned.size() > 0)
                            {
                                Entry<String, ASelfDescribingClass> firstKvp = jobsUnassigned.get(0);
                                jobParams = firstKvp.getValue();
                                m_jobsToPull.put(firstKvp.getKey(), new Object());
                                blnFoundJob = true;
                            }
                        }
                    }
                }
                
                if (intLogTimeCounter > 5000)
                {
                    intLogTimeCounter = 0;
                    strMessage = "DistController. Finding job for worker's pullid [" + strPullId +
                                 "]. Jobs to do [" + m_distController.JobsToDoMap.size() + "]";
                    Console.writeLine(strMessage);
                    Logger.log(strMessage);
                }
                Thread.sleep(100);
                intLogTimeCounter += 100;
            }

            if (jobParams == null)
            {
                throw new HCException("job params not found");
            }
            strMessage = "DistController. Successfuly found job for worker's pull id [" + 
            		strPullId + "]";
            Console.writeLine(strMessage);
            Logger.log(strMessage);
            return jobParams;
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        return null;
    }

    private boolean WaitForWorkerToConfirm(
        ASelfDescribingClass jobParams,
        String strWorkerId,
        String strPullId)
    {
        try
        {
            //
            // check if worker is still alive
            //
            if (!m_distController.DistControllerToWorkerHeartBeat.IsWorkerConnected(
                strWorkerId))
            {
                return false;
            }
            String strMessage = "Sending job ready to worker. Pull id [" +
                                strPullId + "]...";
            Console.writeLine(strMessage);
            Logger.log(strMessage);

            jobParams.SetStrValue(EnumDistributed.PullId,
                                  strPullId);
            jobParams.SetStrValue(EnumDistributed.WorkerId,
                                  strWorkerId);
            jobParams.SetStrValue(EnumDistributed.ControllerId,
                                  m_distController.ControllerId);
            jobParams.SetBlnValue(EnumDistributed.DoWorkAnswer, true);
            TopicPublisherCache.GetPublisher(m_distController.ServerName).SendMessage(
                jobParams,
                m_distController.GridTopic +
                EnumDistributed.TopicControllerToWorkerPullJob.toString() +
                strWorkerId,
                true);

            m_jobsToAck.put(strPullId, new Object());
            int intWaitCounter = 0;
            while (m_jobsToAck.containsKey(strPullId))
            {
                Thread.sleep(PULL_WAIT_MILLS);
                if (intWaitCounter > 5000)
                {
                    //
                    // check if worker is still alive
                    //
                    if (!m_distController.DistControllerToWorkerHeartBeat.IsWorkerConnected(
                        strWorkerId))
                    {
                        return false;
                    }

                    intWaitCounter = 0;
                    //
                    // resend answer to worker
                    //
                    TopicPublisherCache.GetPublisher(m_distController.ServerName).SendMessage(
                        jobParams,
                        m_distController.GridTopic +
                        EnumDistributed.TopicControllerToWorkerPullJob.toString() +
                        strWorkerId,
                        true);
                }
                intWaitCounter += PULL_WAIT_MILLS;
            }
            
            strMessage = "Successfuly sent job ready to worker. Pull id [" +
                                strPullId + "]";
            Console.writeLine(strMessage);
            Logger.log(strMessage);

            return true;
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        return false;
    }

    private void OnTopicWorkerToControllerPullJob(TopicMessage topicmessage)
    {
        try
        {
            ASelfDescribingClass pullParams = (ASelfDescribingClass) topicmessage.EventData;
            String strPullId = pullParams.TryGetStrValue(EnumDistributed.PullId);
            if (StringHelper.IsNullOrEmpty(strPullId))
            {
                throw new HCException("Pull id not found");
            }

            if (m_jobQueue.containsKey(strPullId))
            {
                String strWorkerId = pullParams.TryGetStrValue(EnumDistributed.WorkerId);
                if (StringHelper.IsNullOrEmpty(strWorkerId))
                {
                    throw new HCException("Worker id not found");
                }
                //
                // send "no" to worker. This will avoid being pinged too often
                //
                pullParams.SetBlnValue(EnumDistributed.DoWorkAnswer, false);
                pullParams.SetStrValue(EnumDistributed.ControllerId,
                                       m_distController.ControllerId);

                TopicPublisherCache.GetPublisher(m_distController.ServerName).SendMessage(
                    pullParams,
                    m_distController.GridTopic +
                    EnumDistributed.TopicControllerToWorkerPullJob.toString() +
                    strWorkerId,
                    true);

                String strMessage = "Sent [NO] to worker. Pull id [" +
                             strPullId + "]. Worker id [" + strWorkerId + "]";
                Console.writeLine(strMessage);
                Logger.log(strMessage);
            }
            else
            {
                m_jobQueue.add(strPullId, pullParams);
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public void Dispose()
    {
        try
        {
            m_distController = null;
            if (m_jobQueue != null)
            {
                m_jobQueue.dispose();
                m_jobQueue = null;
            }
            if (m_jobsToAck != null)
            {
                m_jobsToAck.clear();
                m_jobsToAck = null;
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }
}
