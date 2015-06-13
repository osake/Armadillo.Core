package Armadillo.Communication.Impl.Topic;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;

import Armadillo.Core.Config;
import Armadillo.Core.HCException;
import Armadillo.Core.KeyValuePair;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Communication.Impl.PerformanceHelper;
import Armadillo.Communication.Impl.ReqResp.EnumReqResp;
import Armadillo.Core.Concurrent.ProducerConsumerQueue;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Core.SelfDescribing.SelfDescribingClass;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.UI.PublishUiMessageEvent;

public class ZmqTopicSubscriberHelper {

    public SubscriberCallbackDel SubscriberCallback;

    private static final int CONSUMER_QUEUE_SIZE = 20;
    private final String m_strTopic;
    private static ProducerConsumerQueue<KeyValuePair<SubscriberCallbackDel, TopicMessage>> m_topicConsumerQueue;
    private static int m_intJobsDone;
    private static int m_intJobsInProgress;
    private static ThreadWorker<ObjectWrapper> m_logWorker;
    private static final ConcurrentHashMap<String, Integer> m_topicCounter = new ConcurrentHashMap<String, Integer>();
    private static final ConcurrentHashMap<String, Object> m_topicCounterChanges = new ConcurrentHashMap<String, Object>();
    private static final Object m_counterLock = new Object();
    public static ConcurrentHashMap<String,DateTime> m_topicNameToLastUpdate = new ConcurrentHashMap<String, DateTime>();
    
    static 
    {
        SetConsumerQueue();
        SetQueueMonitor();
    }

    public ZmqTopicSubscriberHelper(
        String strTopic,
        SubscriberCallbackDel subscriberCallback)
    {
        m_strTopic = strTopic;
        SubscriberCallback = subscriberCallback;
    }

   private static void SetConsumerQueue()
    {
        m_topicConsumerQueue =
            new ProducerConsumerQueue<KeyValuePair<SubscriberCallbackDel, TopicMessage>>(CONSUMER_QUEUE_SIZE)
                {
					@Override
					public void runTask(KeyValuePair<SubscriberCallbackDel, TopicMessage> kvp) {
			            
						try
			            {
			                synchronized (m_counterLock)
			                {
			                	m_intJobsInProgress++;
			                }
			                String strTopicName = kvp.getValue().TopicName;
			                kvp.getKey().invoke(kvp.getValue());
			                int intCounter = 0;
			                ConcurrentHashMap<String, Integer> topicCounter = m_topicCounter;
			                ConcurrentHashMap<String, Object> topicCounterChanges = m_topicCounterChanges;
			                if(topicCounter != null &&
			                		!StringHelper.IsNullOrEmpty(strTopicName))
			                {
				                if(topicCounter.containsKey(strTopicName)){
				                	if(topicCounter.containsKey(strTopicName)){
				                		intCounter = topicCounter.get(strTopicName);
				                	}
				                }
				                
			                	m_topicNameToLastUpdate.put(
			                			GetCounterKey(strTopicName, 
			                							kvp.getValue().GetConnectionName()),
			                			DateTime.now());
			                	
				                synchronized (m_counterLock)
				                {
				                    intCounter++;
					                m_intJobsDone++;
					                m_intJobsInProgress--;
				                }
				                topicCounter.put(strTopicName, intCounter);
				                if(topicCounterChanges != null)
				                {
				                	topicCounterChanges.put(strTopicName, new Object());
				                }
			                }
			            }
			            catch (Exception ex)
			            {
			                Logger.log(ex);
			            }
		        	}
                };
        
    }

    private static void SetQueueMonitor()
    {
        m_logWorker = new ThreadWorker<ObjectWrapper>()                
        {
			@Override
			public void runTask(ObjectWrapper obj)
	        {
	            while (true)
	            {
	                try
	                {
	                    PublishTopicSubscriber();
	                    PublishTopicSubscriberDetails();
	                    PublishTopicPublisher(TopicPublisher.m_publisherLogCounter);
	                    PublishTopicPublisherDetails(
	                        TopicPublisher.m_publisherLogCounter,
	                        TopicPublisher.m_publisherLogCounterChanges);
	                    PerformanceHelper.PublishPerformance();
	                }
	                catch (Exception ex)
	                {
	                    Logger.log(ex);
	                }
	                try {
						Thread.sleep(5000);
					} catch (InterruptedException ex) {
						Logger.log(ex);
					}
	            }
	        }
		};
        m_logWorker.work();
    }

    private static void PublishTopicPublisherDetails(
        ConcurrentHashMap<String, Integer> counterMap,
        ConcurrentHashMap<String, Object> counterMapChanges)
    {
        try
        {
            //var mapArr = counterMapChanges.ToArray();
            for (Map.Entry<String, Object> kvp : counterMapChanges.entrySet())
            {
                if (!counterMap.containsKey(kvp.getKey()))
                {
                    continue;
                }
                int intCounter = counterMap.get(kvp.getKey());
                SelfDescribingClass publishObj = new SelfDescribingClass();
                publishObj.SetClassName(
                		ZmqTopicSubscriberHelper.class.getName() + "_Publisherdetails_log");
                publishObj.SetStrValue("Id", Config.getClientName());
                publishObj.SetStrValue("Topic",
                    kvp.getKey());
                publishObj.SetIntValue("JobsDone", intCounter);
                publishObj.SetDateValue("Time", DateTime.now().toDate());
                PublishUiMessageEvent.PublishGrid(
                    EnumReqResp.Admin.toString(),
                    "Topic",
                    "TopicPublisherDetails",
                    Config.getClientName() + "_" + kvp.getKey(),
                    publishObj);
            }
            counterMapChanges.clear();
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private static void PublishTopicPublisher(
        ConcurrentHashMap<String, Integer> m_counterMap)
    {
        try
        {
        	int intTotal = 0;
        	for (int intVal : m_counterMap.values()) {
        		intTotal+= intVal;
			}
            
            SelfDescribingClass publishObj = new SelfDescribingClass();
            publishObj.SetClassName(ZmqTopicSubscriberHelper.class.getName() + "_Publisher_log");
            publishObj.SetStrValue("Id", Config.getClientName());
            publishObj.SetIntValue("JobsDone", intTotal);
            publishObj.SetDateValue("Time", DateTime.now().toDate());
            PublishUiMessageEvent.PublishGrid(
                EnumReqResp.Admin.toString(),
                "Topic",
                "TopicPublisher",
                Config.getClientName() + "__Publisher_log",
                publishObj);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private static void PublishTopicSubscriberDetails()
    {
        try
        {
            //var mapArr = m_topicCounterChanges.ToArray();
            for (Map.Entry<String, Object> kvp : m_topicCounterChanges.entrySet())
            {
                //var kvp = mapArr[i];
                if (!m_topicCounter.containsKey(kvp.getKey()))
                {
                    continue;
                }

                int intCounter = m_topicCounter.get(kvp.getKey());
                
                SelfDescribingClass publishObj = new SelfDescribingClass();
                publishObj.SetClassName(ZmqTopicSubscriberHelper.class.getName() + "_details_log");
                publishObj.SetStrValue("Id", Config.getClientName());
                publishObj.SetStrValue("Topic",
                    kvp.getKey());
                publishObj.SetIntValue("JobsDone", intCounter);
                publishObj.SetDateValue("Time", DateTime.now().toDate());
                PublishUiMessageEvent.PublishGrid(
                    EnumReqResp.Admin.toString(),
                    "Topic",
                    "TopicSubscriberDetails",
                    Config.getClientName() + "_" + kvp.getKey(),
                    publishObj);
            }
            m_topicCounter.clear();
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private static void PublishTopicSubscriber()
    {
        try
        {
        	SelfDescribingClass publishObj = new SelfDescribingClass();
            publishObj.SetClassName(ZmqTopicSubscriberHelper.class.getName() + "log");
            publishObj.SetStrValue("Id", Config.getClientName());
            publishObj.SetIntValue("JobsInProgress",
                (m_intJobsInProgress + m_topicConsumerQueue.getSize()));
            publishObj.SetIntValue("JobsDone", m_intJobsDone);
            publishObj.SetDateValue("Time", DateTime.now().toDate());
            PublishUiMessageEvent.PublishGrid(
                EnumReqResp.Admin.toString(),
                "Topic",
                "TopicSubscriber",
                Config.getClientName(),
                publishObj);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public void InvokeCallback(
       TopicMessage topicMessage) throws HCException
    {
        if (!m_strTopic.equals(topicMessage.TopicName))
        {
            throw new HCException("Invalid topic");
        }
        m_topicConsumerQueue.add(new KeyValuePair<SubscriberCallbackDel, TopicMessage>(
                                             SubscriberCallback, topicMessage));
    }

	public static String GetCounterKey(String strTopic, String strConnectionName) {
		return strTopic + strConnectionName;
	}
	
}
