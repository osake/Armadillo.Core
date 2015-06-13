package Armadillo.Communication.Impl.Topic;

import java.util.List;
import java.util.Random;

import Armadillo.Communication.Impl.NotifierDel;
import Armadillo.Core.Logger;

public class TopicSubscriberEnsemble implements ITopicSubscriber 
{
    private final List<ITopicSubscriber> m_topicSubscribers;
    private final Object m_lockObj = new Object();
    private final Random m_rng = new Random();

    public TopicSubscriberEnsemble(List<ITopicSubscriber> topicSubscribers)
    {
        m_topicSubscribers = topicSubscribers;
    }
    
	@Override
	public void Dispose() 
	{
        try
        {
            for (ITopicSubscriber topicSubscriber : m_topicSubscribers)
            {
                topicSubscriber.Dispose();
            }
        }
        catch(Exception ex)
        {
            Logger.Log(ex);
        }
	}

	@Override
	public void Connect(String strServerName, int intPort) 
	{
        try
        {
            for (int i = 0; i < intPort + TopicConstants.NUM_TOPIC_CONNECTIONS; i++)
            {
                m_topicSubscribers.get(i).Connect(strServerName, intPort + i);
            }
        }
        catch (Exception ex)
        {
            Logger.Log(ex);
        }
	}

	@Override
	public void Subscribe(String strTopic,
			SubscriberCallbackDel subscriberCallback) 
	{
        try
        {
            for(int i = 0; i < m_topicSubscribers.size(); i++)
                                      {
                                          //foreach (ITopicSubscriber topicSubscriber in m_topicSubscribers)
                                          //{
                                          ITopicSubscriber topicSubscriber = m_topicSubscribers.get(i);
                                          topicSubscriber.Subscribe(strTopic, subscriberCallback);
                                      };
        }
        catch (Exception ex)
        {
            Logger.Log(ex);
        }
	}

	@Override
	public boolean IsSubscribedToTopic(String strTopic) 
	{
        try
        {
            return m_topicSubscribers.get(0).IsSubscribedToTopic(strTopic);
        }
        catch (Exception ex)
        {
            Logger.Log(ex);
        }
        return false;
	}

	@Override
	public void UnSubscribe(String strTopic) 
	{
        try
        {
            for (ITopicSubscriber topicSubscriber : m_topicSubscribers)
            {
                topicSubscriber.UnSubscribe(strTopic);
            }
        }
        catch (Exception ex)
        {
            Logger.Log(ex);
        }
	}

	@Override
	public void Publish(TopicMessage topicMessage) 
	{
        try
        {
            int intRng;
            synchronized (m_lockObj)
            {
                intRng = (int)(m_rng.nextDouble() * m_topicSubscribers.size());
            }
            m_topicSubscribers.get(intRng).Publish(topicMessage);
        }
        catch (Exception ex)
        {
            Logger.Log(ex);
        }
	}

	@Override
	public int SubscriberCount(String strTopic) 
	{
        try
        {
            return m_topicSubscribers.get(0).SubscriberCount(strTopic);
        }
        catch (Exception ex)
        {
            Logger.Log(ex);
        }
        return 0;
	}

	@Override
	public void NotifyDesconnect(String strTopic, NotifierDel notifierDel) 
	{
        try
        {
            for (ITopicSubscriber topicSubscriber : m_topicSubscribers)
            {
                topicSubscriber.NotifyDesconnect(strTopic, notifierDel);
            }
        }
        catch (Exception ex)
        {
            Logger.Log(ex);
        }
	}

}
