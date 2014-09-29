package Armadillo.Communication.Impl.ReqResp;

import Armadillo.Core.Config;
import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Communication.Impl.Topic.SubscriberCallbackDel;
import Armadillo.Communication.Impl.Topic.TopicMessage;
import Armadillo.Communication.Impl.Topic.TopicPublisherCache;
import Armadillo.Communication.Impl.Topic.TopicSubscriberCache;
import Armadillo.Core.Concurrent.ThreadWorker;

public class LoggerPublisher {
    
	public static boolean IsPublisherConnected;
    public static boolean IsSubscriberConnected;
    private static final Object m_connectLock = new Object();
    private static String m_strServerNamePublisher;

    public static void ConnectPublisher(String strServerName)
    {
        if (IsPublisherConnected)
        {
            return;
        }
        synchronized (m_connectLock)
        {
            if (IsPublisherConnected)
            {
                return;
            }
            m_strServerNamePublisher = strServerName;
            IsPublisherConnected = true;
        }
    }

    public static void ConnectSubscriber(final String strServerName)
    {
    	ThreadWorker<ObjectWrapper> worker = new ThreadWorker<ObjectWrapper>(){
    		@Override
    		public void runTask(ObjectWrapper item) {
    			ConnectSubscriber0(strServerName);
    		}
    	};
    	worker.work();
    }

    private static void ConnectSubscriber0(String strServerName)
    {
        if (IsSubscriberConnected)
        {
            return;
        }
        synchronized (m_connectLock)
        {
            if (IsSubscriberConnected)
            {
                return;
            }

            while(!TopicSubscriberCache.ContainsSubscriber(strServerName))
            {
                String strMessage = "Subscriber not found [" + strServerName + "]";
                Console.writeLine(strMessage);
                Logger.log(strMessage, false, false, false);
                try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Logger.log(e);
				}
            }

            TopicSubscriberCache.GetSubscriber(strServerName).Subscribe(
                "LogGlobal",
                new SubscriberCallbackDel(){
                	@Override
                	public void invoke(TopicMessage topicMessage) {
                        try
                        {
                            Logger.log(topicMessage.EventData.toString(), false, false, false);
                        }
                        catch (Exception ex)
                        {
                            Logger.log(ex);
                        }
                	}
                });
            
            IsSubscriberConnected = true;
        }
    }

    public static void PublishLog(String strLog)
    {
        try
        {
            if (!IsPublisherConnected ||
                !TopicPublisherCache.ContainsPublisher(m_strServerNamePublisher))
            {
                return;
            }
            strLog = "\n--RemoteLog-- [" + Config.getClientName() + "]\n" + strLog;
            TopicPublisherCache.GetPublisher(m_strServerNamePublisher).SendMessage(
                strLog,
                "LogGlobal", false);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

}
