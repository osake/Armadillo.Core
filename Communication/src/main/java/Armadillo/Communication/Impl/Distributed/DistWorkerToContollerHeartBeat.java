package Armadillo.Communication.Impl.Distributed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Communication.Impl.NotifierDel;
import Armadillo.Communication.Impl.Topic.SubscriberCallbackDel;
import Armadillo.Communication.Impl.Topic.TopicConstants;
import Armadillo.Communication.Impl.Topic.TopicMessage;
import Armadillo.Communication.Impl.Topic.TopicPublisherCache;
import Armadillo.Communication.Impl.Topic.TopicSubscriberCache;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Core.SelfDescribing.ASelfDescribingClass;
import Armadillo.Core.SelfDescribing.SelfDescribingClass;
import Armadillo.Core.Text.StringHelper;

public class DistWorkerToContollerHeartBeat {
	
    private List<NotifierDel> OnControllerDiconnected = 
    		new ArrayList<NotifierDel>();

    public ConcurrentHashMap<String, String> ControllerStatus;
    public ConcurrentHashMap<String, DateTime> ControllerPingTimes;
    private String m_strWorkerId;
    private DistWorker m_distWorker;
    private ThreadWorker<ObjectWrapper> m_pingWorker;

    public DistWorkerToContollerHeartBeat(DistWorker distWorker)
    {
        try
        {
            if (StringHelper.IsNullOrEmpty(distWorker.GridTopic))
            {
                throw new HCException("Emtpy topic");
            }

            m_distWorker = distWorker;
            m_strWorkerId = distWorker.WorkerId;
            ControllerPingTimes = new ConcurrentHashMap<String, DateTime>();
            ControllerStatus = new ConcurrentHashMap<String, String>();
            TopicSubscriberCache.GetSubscriber(
                distWorker.ServerName,
                TopicConstants.PUBLISHER_HEART_BEAT_PORT).Subscribe(
                 m_distWorker.GridTopic + EnumDistributed.TopicControllerToWorkerHeartBeat.toString(),
                new SubscriberCallbackDel(){
                	 public void invoke(Armadillo.Communication.Impl.Topic.TopicMessage topicMessage) {
                		 OnTopicControllerToWorkerHeartBeat(topicMessage);	 
                	 };
                 });

            m_pingWorker = new ThreadWorker<ObjectWrapper>(){
            	@Override
            	public void runTask(ObjectWrapper item) {
            		
                    while (true)
                    {
                        try
                        {
                            OnClockTick();
                            Thread.sleep(DistConstants.PING_CONTROLLER_TIME_SECS*1000);
                        }
                        catch (Exception ex)
                        {
                            Logger.log(ex);
                            //
                            // slow down
                            //
                            try {
								Thread.sleep( 
								    5000);
							} catch (InterruptedException e) {
								Logger.log(e);
							}
                        }
                    }
            	}
            };
            m_pingWorker.work();
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private void OnTopicControllerToWorkerHeartBeat(TopicMessage topicmessage)
    {
        try
        {
            ASelfDescribingClass controllerMessage = (ASelfDescribingClass)(topicmessage.EventData);
            String strControllerId = controllerMessage.GetStrValue(EnumDistributed.ControllerId);
            PingBackController(controllerMessage);
            DateTime now = DateTime.now();
            ControllerPingTimes.put(strControllerId, now);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public boolean IsControllerDisconnected(
        String strControllerId)
    {
        try
        {
        	if(ControllerStatus.containsKey(strControllerId)){
	            String strControllerStatus = 
	            		ControllerStatus.get(strControllerId);
	            if (!StringHelper.IsNullOrEmpty(strControllerStatus) &&
	                strControllerStatus.contains(EnumDistributed.Disconnected.toString()))
	            {
	                return true;
	            }
        	}
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        return false;
    }

    private void OnClockTick()
    {
        try
        {
            CheckAliveController();

            SelfDescribingClass calcParams = new SelfDescribingClass();
            calcParams.SetClassName(EnumDistributed.HeartBeatWorkerClass);
            calcParams.SetStrValue(
                EnumDistributed.ControllerId,
                "unknown");
            calcParams.SetDateValue(
                EnumDistributed.TimeControllerToWorker,
                DateTime.now().toDate());
            calcParams.SetDateValue(
                EnumDistributed.Time,
                DateTime.now().toDate());
            PingBackController(calcParams);
            
            PingJobsInProgress();
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private void PingJobsInProgress() {
    	try{
	    	for(Entry<String, Object> kvp : m_distWorker.m_jobsToDo.entrySet()){
				
	    		TopicPublisherCache.GetPublisher(
						m_distWorker.ServerName,
						TopicConstants.SUBSCRIBER_HEART_BEAT_PORT).SendMessage(
						kvp.getKey(), 
						EnumDistributed.WorkerJobsToDoTopic.toString(), 
						true);
	    		
	    	}
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
	}

	private void CheckAliveController()
    {
        try
        {
            DateTime now = DateTime.now();
            for (Map.Entry<String, DateTime> kvp : ControllerPingTimes.entrySet())
            {
                if (Seconds.secondsBetween(kvp.getValue(), now).getSeconds() > DistConstants.ALIVE_CONTROLLER_TIME_SECS)
                {
                    RemoveController(kvp);
                }
                else
                {
                    if (!ControllerStatus.containsKey(kvp.getKey()))
                    {
                        DistGuiHelper.PublishWorkerLog(
                            m_distWorker, 
                            "Connected controller [" + 
                                kvp.getKey() + "]");
                    }
                    ControllerStatus.put(kvp.getKey(), EnumDistributed.Connected.toString());
                }
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private void RemoveController(Map.Entry<String, DateTime> kvp)
    {
        try
        {
            String strControllerId = kvp.getKey();
            if(!IsControllerDisconnected(strControllerId))
            {
                DistGuiHelper.PublishWorkerLog(m_distWorker, "disconnected controller [" +
                    strControllerId + "]");
                ControllerStatus.put(strControllerId, EnumDistributed.Disconnected.toString());
                
                if(OnControllerDiconnected.size() > 0)
                {
                	for (int i = 0; i < OnControllerDiconnected.size(); i++) {
                		OnControllerDiconnected.get(i).invoke(strControllerId);
					}
                }
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private void PingBackController(ASelfDescribingClass controllerMessage)
    {
        try
        {
            if (controllerMessage== null)
            {
                return;
            }
            controllerMessage.SetStrValue(
                EnumDistributed.WorkerId,
                m_strWorkerId);
            controllerMessage.SetDateValue(
                EnumDistributed.Time,
                DateTime.now().toDate());
            
            while (TopicPublisherCache.GetPublisher(
                m_distWorker.ServerName,
                TopicConstants.SUBSCRIBER_HEART_BEAT_PORT) == null)
            {
                Thread.sleep(50);
            }
            TopicPublisherCache.GetPublisher(
                m_distWorker.ServerName,
                TopicConstants.SUBSCRIBER_HEART_BEAT_PORT).SendMessageImmediately(
                controllerMessage,
                m_distWorker.GridTopic + EnumDistributed.TopicWorkerToControllerHeartBeat.toString());

        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public void Dispose()
    {
        if(ControllerStatus != null)
        {
            ControllerStatus.clear();
            ControllerStatus = null;
        }

        if(ControllerPingTimes != null)
        {
            ControllerPingTimes.clear();
            ControllerPingTimes = null;
        }
        if(m_pingWorker != null)
        {
            m_pingWorker.dispose();
            m_pingWorker = null;
        }
        m_distWorker = null;
        //EventHandlerHelper.RemoveAllEventHandlers(this);
    }
}
