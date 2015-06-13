package Armadillo.Communication.Impl.Topic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import Armadillo.Core.Logger;
import Armadillo.Core.Concurrent.LockHelper;

public class TopicSubscriberCache 
{

    private static final ConcurrentHashMap<String, ITopicSubscriber> m_topicSubscribers;

    static 
    {
        m_topicSubscribers = new ConcurrentHashMap<String, ITopicSubscriber>();
    }

    public static ITopicSubscriber GetSubscriber(String strServerName)
    {
        return GetSubscriber(strServerName,
                             TopicConstants.PUBLISHER_DEFAULT_PORT);
    }

    public static ITopicSubscriber GetSubscriber(
    		String strServerName,
			int intPort) 
    {
        String strConnectionKey = strServerName + intPort;
        ITopicSubscriber topicSubscriber;
        topicSubscriber = m_topicSubscribers.get(
                strConnectionKey);
        if (topicSubscriber == null)
        {
            synchronized (LockHelper.GetLockObject(strConnectionKey + "_" +
                TopicPublisherCache.class.getName()))
            {
            	topicSubscriber = m_topicSubscribers.get(
                        strConnectionKey);
                if (topicSubscriber == null)
                {
                    topicSubscriber = CreateSubscriberEnsemble(strServerName, intPort);
                    m_topicSubscribers.put(strConnectionKey, topicSubscriber);
                }
            }
        }
        return topicSubscriber;
    }

	public static boolean ContainsSubscriber(
        String strServerName)
    {
        return ContainsSubscriber(strServerName,
                                  TopicConstants.PUBLISHER_DEFAULT_PORT);
    }

    public static boolean ContainsSubscriber(
        String strServerName,
        int intPort)
    {
        String strConnectionKey = strServerName + intPort;
        return m_topicSubscribers.containsKey(
            strConnectionKey);
    }

    
    private static ITopicSubscriber CreateSubscriberEnsemble(
            String strServerName, 
            int intPort)
        {
            try
            {
            	List<ITopicSubscriber> topicList = new ArrayList<ITopicSubscriber>();
                for (int i = 0; i < TopicConstants.NUM_TOPIC_CONNECTIONS; i++)
                {
                    topicList.add(CreateSubscriber(strServerName, intPort + i));
                }
                TopicSubscriberEnsemble topicSubscriber = new TopicSubscriberEnsemble(topicList);
                return topicSubscriber;
            }
            catch (Exception ex)
            {
                Logger.Log(ex);
            }
            return null;
        }
    

    private static ITopicSubscriber CreateSubscriber(
        String strServerName,
        int intPort)
    {
    	ZmqTopicSubscriber topicSubscriber = new ZmqTopicSubscriber();
        topicSubscriber.Connect(
            strServerName,
            intPort);
        return topicSubscriber;
    }
}
