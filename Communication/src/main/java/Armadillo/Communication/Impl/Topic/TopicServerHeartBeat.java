package Armadillo.Communication.Impl.Topic;

import org.joda.time.DateTime;

import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Core.SelfDescribing.SelfDescribingClass;

public class TopicServerHeartBeat {
    
	private static final int HEART_BEAT_TIME = 1000;
    private static final Object m_lockObject = new Object();
    private static ThreadWorker<ObjectWrapper> m_threadWorker;
    private static String m_strServerName;

    public static void StartHeartBeat(String strServerName)
    {
        if (m_threadWorker == null)
        {
            synchronized (m_lockObject)
            {
                if (m_threadWorker == null)
                {
                    m_strServerName = strServerName;
                    m_threadWorker = new ThreadWorker<ObjectWrapper>(){
                    	@Override
                    	public void runTask(ObjectWrapper obj){
                    		Work();
                    	}
                    };
                    //m_threadWorker.OnExecute += Work;
                    m_threadWorker.work();
                    Logger.log("Started " + TopicServerHeartBeat.class.getName());
                }
            }
        }
    }

    private static void Work()
    {
        while(!TopicServer.IsInitialized)
        {
            try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				Logger.log(ex);
			}
        }
        while (true)
        {
            String strTopic = TopicServerHeartBeat.class.getName();
            SelfDescribingClass selfDescribingClass = new SelfDescribingClass();
            selfDescribingClass.SetDateValue("Time", DateTime.now().toDate());
            selfDescribingClass.SetClassName(strTopic);
            TopicPublisherCache.GetPublisher(
                m_strServerName,
                TopicConstants.SUBSCRIBER_HEART_BEAT_PORT).SendMessage(
                selfDescribingClass,
                strTopic, // topic name
                true); // wait
            TopicPublisherCache.GetPublisher(
                m_strServerName,
                TopicConstants.SUBSCRIBER_DEFAULT_PORT).SendMessage(
                selfDescribingClass,
                strTopic, // topic name
                true); // wait
            try {
				Thread.sleep(HEART_BEAT_TIME);
			} catch (InterruptedException ex) {
				Logger.log(ex);
			}
        }
    }
}
