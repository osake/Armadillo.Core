package Armadillo.Communication.Impl.Distributed;

import Armadillo.Communication.Impl.Topic.TopicMessage;
import Armadillo.Communication.Impl.Topic.TopicPublisher;
import Armadillo.Communication.Impl.Topic.TopicPublisherCache;
import Armadillo.Core.Concurrent.EfficientProducerConsumerQueue;
import Armadillo.Core.Concurrent.Task;

public class DistTopicQueue {
    
	private final String m_strServerName;

    private EfficientProducerConsumerQueue<TopicMessage> m_efficientQueue;

    public DistTopicQueue(final String strServerName)
    {
        m_strServerName = strServerName;
        m_efficientQueue = new EfficientProducerConsumerQueue<TopicMessage>(1){
        	@Override
        	public void runTask(TopicMessage topicMessage) throws Exception {
        		TopicPublisherCache.GetPublisher(
        	            strServerName).SendMessage(
        	            true,
        	            topicMessage);        	
        	}
        };

    }

    public Task AddItem(
        String strTopic,
        String strKey,
        Object obj)
    {
        return AddItem(
            strTopic,
            strKey,
            obj,
            true);
    }

    public Task AddItem(
        String strTopic,
        String strKey,
        Object obj,
        boolean blnUseQueue)
    {
        TopicMessage topicMessage = TopicPublisher.PrepareEvent(
            obj,
            strTopic);
        if (blnUseQueue)
        {
            return m_efficientQueue.add(
                strKey,
                topicMessage);
        }
        return TopicPublisherCache.GetPublisher(m_strServerName).SendMessage(
            true,
            topicMessage);
    }

    public void Dispose()
    {
        if(m_efficientQueue != null)
        {
            //m_efficientQueue.dispose();
            m_efficientQueue = null;
        }
        //EventHandlerHelper.RemoveAllEventHandlers(this);
    }
}
