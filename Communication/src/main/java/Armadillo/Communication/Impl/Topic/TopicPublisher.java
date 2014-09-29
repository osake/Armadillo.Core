package Armadillo.Communication.Impl.Topic;

import java.util.concurrent.ConcurrentHashMap;
import Armadillo.Core.Config;
import Armadillo.Core.Console;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Communication.Impl.NotifierDel;
import Armadillo.Core.Concurrent.ProducerConsumerQueue;
import Armadillo.Core.Concurrent.Task;
import Armadillo.Core.Text.StringHelper;

public class TopicPublisher {

    private String m_strServerName;
    private ProducerConsumerQueue<TopicMessage> m_messageQueue;
    private ITopicPublishing m_topicPublishing;
    private boolean m_blnIsLocalConnection;
    private Object m_lockObject = new Object();
    private boolean m_blnIsConnected;
    public TopicPublisher m_ownInstance;
    public boolean m_blnIsConnecting;
    private Object m_publishLock = new Object();
    private int m_intPort;
    public static ConcurrentHashMap<String, Integer> m_publisherLogCounter = 
        new ConcurrentHashMap<String, Integer>();
    public static ConcurrentHashMap<String, Object> m_publisherLogCounterChanges =
        new ConcurrentHashMap<String, Object>();

    public void Connect(
        String strServerName,
        int intPort) throws HCException
    {
        if(m_blnIsConnected)
        {
            return;
        }
        synchronized (m_lockObject)
        {
            if(m_blnIsConnected)
            {
                return;
            }
            if(StringHelper.IsNullOrEmpty(strServerName))
            {
                throw new HCException("Empty server name");
            }
            m_blnIsConnecting = true;
            m_strServerName = strServerName;
            m_intPort = intPort;
            if (m_strServerName == "local")
            {
                m_blnIsLocalConnection = true;
            }
            m_ownInstance = new TopicPublisher(
                strServerName,
                intPort);
            m_blnIsConnected = true;

            Logger.log(TopicPublisher.class.getName() + " is connected to server: "  +
                strServerName);
            m_blnIsConnecting = false;
        }
    }

    private void TopicClientHeartBeatOnDisconnectedState(String strservername)
    {
        if(m_topicPublishing == null)
        {
            return;
        }
        String strMessage = TopicPublisher.class.getName() + " server disconnected...";
        Console.writeLine(strMessage);
        Logger.log(strMessage);
        m_topicPublishing.Reconnect();
    }

    public TopicPublisher(
        String strServerName,
        int intPort)
    {
        StartPublisher(
            strServerName,
            intPort);
    }

    public void SendMessage(
        Object dataObj,
        String strTopic,
        boolean blnWait)
    {
        try
        {
            TopicMessage topicMessage = PrepareEvent(
                dataObj,
                strTopic);

            SendMessage(blnWait, topicMessage);
        }
        catch (Exception ex)
        {
            Logger.log(ex, false);
        }
    }

    public Task SendMessage(boolean blnWait, TopicMessage topicMessage)
    {
        try
        {
        	if(topicMessage == null){
        		throw new HCException("Null message");
        	}
            Task task = m_messageQueue.add(
                topicMessage);
            if (blnWait)
            {
                task.waitTask();
                //task.dispose(); todo commented for testing, maybe keep it as it is
            }
            return task;
        }
        catch (Exception ex)
        {
            Logger.log(ex, false);
        }
        return null;
    }

    private void StartPublisher(
        String strServerName,
        int intPort)
    {
        m_strServerName = strServerName;
        CreateProxy(strServerName, intPort);
     // keep one thead, otherwise the chart will display wrong date order
        m_messageQueue =
            new ProducerConsumerQueue<TopicMessage>(1,10000) 
						{
								@Override
								public void runTask(TopicMessage topicMessage) {
									
									try{
										OnPublishMessage(topicMessage);
									}
									catch(Exception ex){
										Logger.log(ex);
									}
								}
						};
						
        //
        // check if topic server is alive
        //
        TopicClientHeartBeat.StartHeartBeat(strServerName);
        TopicClientHeartBeat.OnDisconnectedState.add(
        		new NotifierDel()
        		{
        			@Override
        			public void invoke(String strservername){
        				TopicClientHeartBeatOnDisconnectedState(strservername);
        			}
        		});
    }

    public void SendMessageImmediately(
        Object dataObj,
        String strTopic)
    {
        try
        {
            TopicMessage topicMessage = PrepareEvent(
                dataObj,
                strTopic);
            PublishMessageImmediately(topicMessage);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public void PublishMessageImmediately(TopicMessage topicMessage)
    {
        OnPublishMessage(topicMessage);
    }

    /// <summary>
    ///   Note that this call should be thrad safe
    /// </summary>
    /// <param name = "topicMessage"></param>
    private void OnPublishMessage(TopicMessage topicMessage)
    {
        try
        {
            synchronized (m_publishLock)
            {
                if (m_blnIsLocalConnection)
                {
                    //
                    // local connection. Pass the message
                    //
                    TopicSubscriberCache.GetSubscriber(
                        m_strServerName).Publish(topicMessage);
                }
                else
                {
                    m_topicPublishing.Publish(topicMessage);
                }
                int intCounter = 0;
                if(m_publisherLogCounter.containsKey(topicMessage.TopicName)){
                	m_publisherLogCounter.get(topicMessage.TopicName);
                }
                intCounter++;
                m_publisherLogCounter.put(topicMessage.TopicName, intCounter);
                m_publisherLogCounterChanges.put(topicMessage.TopicName, new Object());
            }
        }
        catch (Exception ex)
        {
            OnFailurePublish(topicMessage, ex);
        }
    }

    private void OnFailurePublish(TopicMessage topicMessage, Exception ex)
    {
        try
        {
            StartPublisher(
                m_strServerName,
                m_intPort);
            //
            // note => publish again!
            //
            m_topicPublishing.Publish(
                topicMessage);
            Logger.log(ex, false);
            String strTopciName = topicMessage.TopicName;
            String strMessage = "Published topic [" + strTopciName + "] in failure mode";
            Logger.log(strMessage);
            Console.writeLine(strMessage);
        }
        catch (Exception ex2)
        {
            try
            {
                String strTopciName = topicMessage.TopicName;
                String strMessage = "Error published topic [" + strTopciName + "] in failure mode";
                Logger.log(strMessage);
                Console.writeLine(strMessage);
                Logger.log(ex2, false);
            }
            catch (Exception ex3)
            {
                Logger.log(ex3, false);
            }
        }
    }

    private void CreateProxy(
        String strServerName,
        int intPort)
    {
        try
        {
            if (strServerName != "local")
            {
                m_topicPublishing = new ZmqTopicPublisherConnection(strServerName, intPort);
            }
        }
        catch (Exception ex)
        {
            Logger.log("Error while creating proxy");
            Logger.log(ex, false);
        }
    }


    public static TopicMessage PrepareEvent(
        Object objData,
        String strTopic)
    {
    	TopicMessage topicMessage = new TopicMessage();
    	topicMessage.EventData = objData; 
		topicMessage.TopicName = strTopic;
		topicMessage.PublisherName = Config.getClientName();
        return topicMessage;
    }

}
