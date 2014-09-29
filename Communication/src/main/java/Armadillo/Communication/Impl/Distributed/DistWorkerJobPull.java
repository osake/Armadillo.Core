package Armadillo.Communication.Impl.Distributed;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;

import Armadillo.Core.Config;
import Armadillo.Core.Console;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Communication.Impl.Topic.SubscriberCallbackDel;
import Armadillo.Communication.Impl.Topic.TopicMessage;
import Armadillo.Communication.Impl.Topic.TopicPublisherCache;
import Armadillo.Communication.Impl.Topic.TopicSubscriberCache;
import Armadillo.Core.Concurrent.LockHelper;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Core.SelfDescribing.ASelfDescribingClass;
import Armadillo.Core.SelfDescribing.SelfDescribingClass;
import Armadillo.Core.Text.StringHelper;

public class DistWorkerJobPull {
    
	private static final int PULL_WAIT_MILLS = 20;

    private DistWorker m_distWorker;
    private ConcurrentHashMap<String, ObjectWrapper> m_jobsToPull;
    private ConcurrentHashMap<String, String> m_jobsToResendPull;
    private int m_intJobsPulled;
    private static int m_intPullIdCounter;
    private static final Object m_counterLock = new Object();

    public DistWorkerJobPull(
        DistWorker distWorker)
    {
        try
        {
            m_distWorker = distWorker;
            m_jobsToPull = new ConcurrentHashMap<String, ObjectWrapper>();
            m_jobsToResendPull = new ConcurrentHashMap<String, String>();

            //
            // Initialise pull workers
            //
            for (int i = 0; i < distWorker.Threads; i++)
            {
            	ThreadWorker<ObjectWrapper> pullWorker = 
            			new ThreadWorker<ObjectWrapper>(){
            		public void runTask(ObjectWrapper item) {
            			PullLoop();
            		};
            	};
                pullWorker.work();
            }

            //
            // subscribe to jobs being pulled
            //
            SubscriberCallbackDel subscriberCallbackDel = new SubscriberCallbackDel(){
            	
            	@Override
            	public void invoke(TopicMessage topicMessage) {
            		OnTopicControllerToWorkerPullJob(topicMessage);
            	};
            };
            TopicSubscriberCache.GetSubscriber(distWorker.ServerName).Subscribe(
                distWorker.GridTopic +
                EnumDistributed.TopicControllerToWorkerPullJob.toString() +
                distWorker.WorkerId,
                subscriberCallbackDel);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private void OnTopicControllerToWorkerPullJob(TopicMessage topicmessage)
    {
        try
        {
            String strMessage;
            ASelfDescribingClass jobParams = (ASelfDescribingClass) topicmessage.EventData;
            String strWorkerId = jobParams.TryGetStrValue(EnumDistributed.WorkerId);
            if (StringHelper.IsNullOrEmpty(strWorkerId))
            {
                throw new HCException("Empty worker id");
            }

            if (!m_distWorker.WorkerId.equals(strWorkerId))
            {
                //
                // message sent to another worker
                //
                return;
            }

            String strPullId = jobParams.GetStrValue(EnumDistributed.PullId);
            if (StringHelper.IsNullOrEmpty(strPullId))
            {
                //
                // pull id not found
                //
                throw new HCException("Pull id not found");
            }

            String strControllerId = jobParams.GetStrValue(EnumDistributed.ControllerId);
            if (StringHelper.IsNullOrEmpty(strControllerId))
            {
                throw new HCException("Controller id not found");
            }

            //
            // the controller has the request in its queue, so stop pinging it
            //
            m_jobsToResendPull.put(strPullId, strControllerId);

            //
            // check controller's answer
            //
            boolean blnPullAnswer = jobParams.TryGetBlnValue(EnumDistributed.DoWorkAnswer);
            if (!blnPullAnswer)
            {
                //
                // The controller should now have our request in its queue
                // He will call me back once there is some work to do
                //
                strMessage = "Controller said [NO] to pull id [" +
                             strPullId + "]";
                Console.writeLine(strMessage);
                Logger.log(strMessage);

                return;
            }

            //
            // Controller Said [YES]
            // Avoid leaving the controller waiting forever. Always ack the job back to the controller
            //
            TopicPublisherCache.GetPublisher(
                m_distWorker.ServerName).SendMessage(
                strPullId,
                m_distWorker.GridTopic + EnumDistributed.TopicWorkerToControllerPullJobAck.toString(),
                true);

            //
            // the job has been pulled
            //
            if (m_jobsToPull.containsKey(strPullId))
            {
                strMessage = "Controller said [YES] to pull id [" +
                             strPullId + "]";
                Console.writeLine(strMessage);
                Logger.log(strMessage);
                m_jobsToPull.put(strPullId, 
                		new ObjectWrapper(
                				jobParams));
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private void PullLoop()
    {
        while (true)
        {
            try
            {
                //
                // pull job
                //
                ASelfDescribingClass jobParams = PullJob();

                synchronized(m_counterLock){
                	m_intJobsPulled++;
                }
                //
                // do work
                //
                DoWork(jobParams);

                synchronized(m_counterLock){
                	m_intJobsPulled--;
                }

                if (m_intJobsPulled < 0 || m_intJobsPulled > m_distWorker.Threads)
                {
                    throw new HCException("Invalid number of jobs pulled");
                }
            }
            catch (Exception ex)
            {
                Logger.log(ex);
                try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					Logger.log(e);
				}
            }
        }
    }

    private void DoWork(ASelfDescribingClass jobParams)
    {
        try
        {
            if (jobParams == null)
            {
                return;
            }

            String strJobId = jobParams.GetStrValue(EnumDistributed.JobId);
            
            if (m_distWorker.JobsCompletedMap.containsKey(strJobId))
            {
            	DateTime timeDone = 
            			m_distWorker.JobsCompletedMap.get(strJobId);
                String strMessage = "Job igored. " + DistWorkerJobPull.class.getName() +
                                    " job [" +
                                    strJobId + "] already done. Since [" +
                                    timeDone + "]";
                Console.writeLine(strMessage);
                Logger.log(strMessage);
                return;
            }

            String strCalcType = 
            jobParams.TryGetStrValue(
                EnumCalcCols.CalcType);
            
            if (m_distWorker.CalcTypesSet.size() > 0 &&
                !m_distWorker.CalcTypesSet.contains(strCalcType))
            {
                //
                // calc type not found
                //
                return;
            }

            synchronized (LockHelper.GetLockObject(strJobId + "_worker"))
            {
                if (m_distWorker.JobsCompletedMap.containsKey(strJobId))
                {
                    String strMessage = "Job igored. " + DistWorkerJobPull.class.getName() +
                                        " job [" +
                                        strJobId + "] already done.";
                    Console.writeLine(strMessage);
                    Logger.log(strMessage);
                    return;
                }
                m_distWorker.DoJob(strJobId, jobParams);
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private ASelfDescribingClass PullJob()
    {
        try
        {
            SelfDescribingClass pullParams = new SelfDescribingClass();
            String strPullId = GetPullParams(pullParams);

            String strMessage = "Pulling job [" + strPullId + "]...";
            Console.writeLine(strMessage);
            Logger.log(strMessage);
            m_jobsToPull.put(strPullId, new ObjectWrapper());
            m_jobsToResendPull.put(strPullId, "");
            ASelfDescribingClass jobParams = null;
            int intWaitCounter = 0;

            while (TopicPublisherCache.GetPublisher(m_distWorker.ServerName) == null)
            {
                strMessage = getClass().getName() + " is waiting for topic publisher to connect...";
                Console.writeLine(strMessage);
                Logger.log(strMessage);
                Thread.sleep(1000);
            }

            //
            // send pull request for the first time
            //
            TopicPublisherCache.GetPublisher(m_distWorker.ServerName).SendMessage(
                pullParams,
                m_distWorker.GridTopic + EnumDistributed.TopicWorkerToControllerPullJob.toString(),
                true);

            //
            // loop until a job is pulled
            //
            int intLongWait = 0;
            ObjectWrapper objWrapper = null;
            while (m_jobsToPull.containsKey(strPullId) &&
            		(objWrapper = m_jobsToPull.get(strPullId)).m_obj == null)
            {
                intWaitCounter += PULL_WAIT_MILLS;
                intLongWait += PULL_WAIT_MILLS;
                if (intLongWait > 60000)
                {
                    //
                    // ping job again. Controller may have lost it...
                    //
                    m_jobsToResendPull.put(strPullId, "");
                    intLongWait = 0;
                }
                if (intWaitCounter > 5000)
                {
                    intWaitCounter = 0;
                    String strControllerId;
                    if (m_jobsToResendPull.containsKey(strPullId))
                    {
                    	strControllerId = m_jobsToResendPull.get(strPullId);
                        if (StringHelper.IsNullOrEmpty(strControllerId))
                        {
                            //
                            // maybe the controller did not get the message
                            // resend request
                            //
                            TopicPublisherCache.GetPublisher(m_distWorker.ServerName).SendMessage(
                                pullParams,
                                m_distWorker.GridTopic +
                                EnumDistributed.TopicWorkerToControllerPullJob.toString(),
                                true);
                            strMessage = "Ping pull job id [" + strPullId + "]";
                            Console.writeLine(strMessage);
                            Logger.log(strMessage);
                        }
                        else if (m_distWorker.DistWorkerToContollerHeartBeat
                            .IsControllerDisconnected(strControllerId))
                        {
                            //
                            // Controller is disconnected.
                            // We need to start pinging again the controller
                            //
                            strMessage = "Controller disconnected job id [" + strPullId + "]";
                            Console.writeLine(strMessage);
                            Logger.log(strMessage);
                            m_jobsToResendPull.put(strPullId, "");
                        }
                    }
                }
                Thread.sleep(PULL_WAIT_MILLS);
            }
            
        	jobParams = (ASelfDescribingClass) objWrapper.m_obj;

            if (jobParams == null)
            {
                throw new HCException("Null jobs params");
            }
            //
            // the pull map is no longer required
            //
            m_jobsToPull.remove(strPullId);
            m_jobsToResendPull.remove(strPullId);

            strMessage = "Successfully pulled job id [" + strPullId + "]";
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

    private String GetPullParams(SelfDescribingClass pullParams)
    {
        try
        {
        	synchronized(m_counterLock){
	            m_intPullIdCounter++;
	            String strPullId = "pull_" + m_intPullIdCounter + "_" + 
	            Config.getClientName() + "_" + UUID.randomUUID().toString();
	            //pullParams = new SelfDescribingClass();
	            pullParams.SetClassName(getClass().getName() + EnumDistributed.PullParams);
	            pullParams.SetStrValue(EnumDistributed.WorkerId, m_distWorker.WorkerId);
	            pullParams.SetStrValue(EnumDistributed.PullId, strPullId);
	            return strPullId;
        	}
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        pullParams = null;
        return "";
    }

    public void Dispose()
    {
        try
        {
            m_distWorker = null;
            if (m_jobsToPull != null)
            {
                m_jobsToPull.clear();
                m_jobsToPull = null;
            }
            if (m_jobsToResendPull != null)
            {
                m_jobsToResendPull.clear();
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }
}
