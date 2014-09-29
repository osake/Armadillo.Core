package Armadillo.Communication.Impl.Topic;

import java.util.concurrent.ConcurrentHashMap;

import Armadillo.Core.Concurrent.LockHelper;

public class TopicSubscriberCache {

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
                    topicSubscriber = new ZmqTopicSubscriber();
                    topicSubscriber.Connect(
                        strServerName,
                        intPort);
                    m_topicSubscribers.put(strConnectionKey, topicSubscriber);
                }
            }
        }
        return topicSubscriber;
    }
}
