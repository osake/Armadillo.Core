package Armadillo.Communication.Impl.ReqResp;

import java.util.Date;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import Armadillo.Core.Clock;
import Armadillo.Core.Config;
import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Communication.Impl.NotifierDel;
import Armadillo.Communication.Impl.Topic.SubscriberCallbackDel;
import Armadillo.Communication.Impl.Topic.TopicClientHeartBeat;
import Armadillo.Communication.Impl.Topic.TopicClientHeartBeatThreadWorker;
import Armadillo.Communication.Impl.Topic.TopicConstants;
import Armadillo.Communication.Impl.Topic.TopicMessage;
import Armadillo.Communication.Impl.Topic.TopicSubscriberCache;
import Armadillo.Communication.Impl.Topic.ZmqTopicSubscriber;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Core.SelfDescribing.SelfDescribingClass;
import Armadillo.Core.UI.PublishUiMessageEvent;

public class ReqRespServerHeartBeat {

    public NotifierDel OnClientDisconnected;


    public ConcurrentHashMap<String, SelfDescribingClass> MapClientToPingTime;
    public SelfDescribingClass ProviderStats;

    private final ThreadWorker<ObjectWrapper> m_threadWorker;
    public ConcurrentHashMap<String, String> m_knownClients;
    private final Object m_clientLock = new Object();
    private final String m_strServerName;
    private final boolean m_blnPublishGui;
    private static final double TIME_OUT_SECONDS = 30;
    private int m_intPort;

    public ReqRespServerHeartBeat(
        String strServerName,
        int intPort,
        boolean blnPublishGui)
    {
        m_intPort = intPort;
        m_strServerName = strServerName;
        m_blnPublishGui = blnPublishGui;
        m_knownClients = new ConcurrentHashMap<String, String>();
        MapClientToPingTime = new ConcurrentHashMap<String, SelfDescribingClass>();
        ProviderStats = new SelfDescribingClass();
        ProviderStats.SetClassName(ReqRespServerHeartBeat.class.getName() + "_providerStats");
        
        m_threadWorker = new ThreadWorker<ObjectWrapper>(){
        	@Override
        	public void runTask(ObjectWrapper objectWrapper){
        		CheckDisconnectLoop();
        	}
        };
        //m_threadWorker.OnExecute += CheckDisconnectLoop;
        m_threadWorker.work();
        ZmqTopicSubscriber.subscribePublishAnyMessage(
        		new SubscriberCallbackDel(){
        			
        			@Override
        			public void invoke(TopicMessage topicMessage){
        				OnTopicCallback(topicMessage.PublisherName);
        			}
        		});
        
        TopicSubscriberCache.GetSubscriber(
            strServerName,
            TopicConstants.PUBLISHER_HEART_BEAT_PORT).Subscribe(
            EnumReqResp.AsyncHeartBeatClientToServerTopic.toString(),
            new SubscriberCallbackDel()
            {
            	@Override
            	public void invoke(TopicMessage topicMessage){
            		OnTopicCallback(topicMessage);
            	}
            });
    }

    public boolean IsClientConnected(
        String strClientName)
    {
        if(!MapClientToPingTime.containsKey(
            strClientName))
        {
            if(!m_knownClients.containsKey(strClientName))
            {
                //
                // the client has not yet shown up in the ping list
                //
                OnTopicCallback(strClientName);
                return true;
            }
            return false;
        }
        return true;
    }

    private void OnTopicCallback(TopicMessage topicmessage)
    {
    	//ASelfDescribingClass topicParams = (ASelfDescribingClass) topicmessage.EventData;
        String strClientName = topicmessage.PublisherName;
        OnTopicCallback(strClientName);
    }

    public void OnTopicCallback(String strClientName)
    {
        SelfDescribingClass pingStats = null;
        if (!MapClientToPingTime.containsKey(
            strClientName))
        {
        	pingStats = MapClientToPingTime.get(strClientName);
            synchronized (m_clientLock)
            {
                if (!MapClientToPingTime.containsKey(
                    strClientName))
                {
                    pingStats = new SelfDescribingClass();
                    pingStats.SetClassName(ReqRespServerHeartBeat.class.getName() + "_clientStats");
                    MapClientToPingTime.put(strClientName, pingStats);
                    m_knownClients.put(strClientName, strClientName);
                    String strMessage = "[" +
                    		getClass().getName() + "] [" + strClientName + "] is now connected";
                    Console.writeLine(strMessage);
                    Logger.log(strMessage);
                }
                else{
                	pingStats = MapClientToPingTime.get(strClientName);
                }
            }
        }
        else{
        	pingStats = MapClientToPingTime.get(strClientName);
        }
        pingStats.SetDateValue("Time", DateTime.now().toDate());
    }

    private void CheckDisconnectLoop()
    {
        while (true)
        {
            try
            {
                //
                // iterate each client
                //
                Set<Entry<String, SelfDescribingClass>> clientToPingTimeArr = 
                		MapClientToPingTime.entrySet();
                
                TopicClientHeartBeatThreadWorker topicClientHeartBeatThreadWorker;
                double dblLastTopicPingSecs = 0;
                if(TopicClientHeartBeat.HeartBeatWorker.containsKey(m_strServerName))
                {
                	topicClientHeartBeatThreadWorker = TopicClientHeartBeat.HeartBeatWorker.get(m_strServerName);
                	
                	Seconds interval = Seconds.secondsBetween(
                			 new DateTime(topicClientHeartBeatThreadWorker.LastTopicPingTime),
                			 Clock.LastTime);
                    dblLastTopicPingSecs =interval.getSeconds();
                }
                for (Entry<String, SelfDescribingClass> kvp : clientToPingTimeArr)
                {
                    EnumReqResp connectionState;
                    Date lastPingTime;
                    
                    if(!kvp.getValue().ContainsProperty("Time"))
                    {
                        lastPingTime = DateTime.now().toDate();
                    }
                    else{
                    	lastPingTime = kvp.getValue().GetDateValue("Time");
                    }
                    double dblTotalSeconds = Seconds.secondsBetween(
                    		new DateTime(lastPingTime),
                    		Clock.LastTime).getSeconds();
                    double dblTimeoutLimit = Math.min(TopicClientHeartBeatThreadWorker.CONNECTION_TIME_OUT_SECS,
                                                      TIME_OUT_SECONDS/2);
                    if (dblTotalSeconds > TIME_OUT_SECONDS &&
                        dblLastTopicPingSecs < dblTimeoutLimit)
                    {
                        connectionState = EnumReqResp.Disconnected;
                    }
                    else
                    {
                        connectionState = EnumReqResp.Connected;
                    }
                    String strClientName = kvp.getKey();

                    if (m_blnPublishGui)
                    {
                        //
                        // publish state
                        //
                        SelfDescribingClass publishObj = kvp.getValue();
                        publishObj.SetDateValue("Time", lastPingTime);
                        publishObj.SetStrValue(EnumReqResp.ClientName, strClientName);
                        publishObj.SetStrValue(EnumReqResp.ConnectionState, connectionState.toString());
                        
                        PublishUiMessageEvent.PublishGrid(
                            EnumReqResp.Admin.toString(),
                            EnumReqResp.RequestResponse.toString() + "_" + 
                                m_strServerName + "_" +
                                m_intPort + "_" + 
                                Config.getClientName(),
                            "Connections",
                            kvp.getKey(),
                            publishObj);
                    }

                    if (connectionState == EnumReqResp.Disconnected)
                    {
                        synchronized (m_clientLock)
                        {
                            if(MapClientToPingTime.containsKey(strClientName))
                            {
                            	MapClientToPingTime.remove(strClientName);
                                String strMessage = getClass().getName() +
                                                    ". Client [" + strClientName + "] is disconnected";
                                Console.writeLine(strMessage);
                                Logger.log(strMessage);
                                
                                if (OnClientDisconnected != null)
                                {
                                    OnClientDisconnected.invoke(strClientName);
                                }
                            }
                        }
                    }
                }

                if (m_blnPublishGui)
                {
                    //
                    // publish provider stats
                    //
                    ProviderStats.SetDateValue("Time", DateTime.now().toDate());
                    ProviderStats.SetStrValue(
                        EnumReqResp.ConnectionState,
                        EnumReqResp.Connected.toString());
                    PublishUiMessageEvent.PublishGrid(
                        EnumReqResp.Admin.toString(),
                        EnumReqResp.RequestResponse.toString() + "_" + 
                        m_strServerName + "_" + 
                            m_intPort + "_" + 
                            Config.getClientName(),
                        "Service",
                        "Service",
                        ProviderStats,
                        0,
                        true);
                }
            }
            catch (Exception ex)
            {
                Logger.log(ex);
            }
            try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				Logger.log(e);
			}
        }
    }
	
}
