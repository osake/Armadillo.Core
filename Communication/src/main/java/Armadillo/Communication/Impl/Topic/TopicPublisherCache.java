package Armadillo.Communication.Impl.Topic;

import java.util.concurrent.ConcurrentHashMap;

import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.Concurrent.LockHelper;

public class TopicPublisherCache {

    private static final ConcurrentHashMap<String, TopicPublisher> m_topicPublisher;

    static
    {
        m_topicPublisher = new ConcurrentHashMap<String, TopicPublisher>();
    }

    public static boolean ContainsPublisher(
        String strServerName)
    {
        return ContainsPublisher(strServerName,
                             TopicConstants.SUBSCRIBER_DEFAULT_PORT);
    }

    public static boolean ContainsPublisher(
        String strServerName,
        int intPort)
    {
        String strConnectionKey = strServerName + intPort;
        return m_topicPublisher.containsKey(
            strConnectionKey);
    }

    public static TopicPublisher GetPublisher(String strServerName)
    {
        return GetPublisher(strServerName,
                             TopicConstants.SUBSCRIBER_DEFAULT_PORT);
    }

    public static TopicPublisher GetPublisher(
        String strServerName,
        int intPort)
    {
        String strConnectionKey = strServerName + intPort;
        TopicPublisher topicPublisher = m_topicPublisher.get(
                strConnectionKey);
        if (topicPublisher == null)
        {
            synchronized(LockHelper.GetLockObject(strConnectionKey + "_" +
                TopicPublisherCache.class.getName()))
            {
            	topicPublisher = m_topicPublisher.get(
                        strConnectionKey);
                if (topicPublisher == null)
                {
                    topicPublisher = new TopicPublisher(
                        strServerName,
                        intPort);
                    try {
						topicPublisher.Connect(
						    strServerName,
						    intPort);
					} catch (HCException ex) {
						Logger.log(ex);
					}

                    while (topicPublisher.m_blnIsConnecting)
                    {
                        try {
							Thread.sleep(100);
						} catch (InterruptedException ex) {
							Logger.log(ex);
						}
                    }
                    m_topicPublisher.put(strConnectionKey, topicPublisher);
                }
            }
        }
        return topicPublisher;
    }
}
