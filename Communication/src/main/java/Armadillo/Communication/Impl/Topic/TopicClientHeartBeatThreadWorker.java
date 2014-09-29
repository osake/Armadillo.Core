package Armadillo.Communication.Impl.Topic;

import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import Armadillo.Core.Clock;
import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Communication.Impl.NotifierDel;
import Armadillo.Communication.Impl.ReqResp.EnumReqResp;
import Armadillo.Core.Concurrent.ThreadWorker;

public class TopicClientHeartBeatThreadWorker {

    public static final int CONNECTION_TIME_OUT_SECS = 45;
    private static final  int HEART_BEAT_TIME_SECS = 1;
    public Date LastTopicPingTime;
    public EnumReqResp ConnectionState;
    private String m_strServerName;
    private DateTime m_lastTopicCheckTime = DateTime.now();;
    private ThreadWorker<ObjectWrapper> m_threadWorker;
    private DateTime m_diconnectedTimer = DateTime.now();
	private NotifierDel m_connectionStateDel;

    public TopicClientHeartBeatThreadWorker(
        String strServerName,
        NotifierDel connectionStateDel)
    {
    	try
    	{
	        m_connectionStateDel = connectionStateDel;
	        m_strServerName = strServerName;
	        m_lastTopicCheckTime = DateTime.now();
	        LastTopicPingTime = DateTime.now().toDate();
	        m_threadWorker = new ThreadWorker<ObjectWrapper>(){
	        	
	        	@Override
	        	public void runTask(ObjectWrapper obj){
	        		try{
	        			HeartBeat();
	        		}
	        		catch(Exception ex)
	        		{
	        			Logger.log(ex);
	        		}
	        	}
	        };
	        m_threadWorker.work();
	        TopicSubscriberCache.GetSubscriber(
	            strServerName,
	            TopicConstants.PUBLISHER_HEART_BEAT_PORT).Subscribe(
	            TopicServerHeartBeat.class.getName(),
	            new SubscriberCallbackDel(){
	            	
	            	@Override
	            	public void invoke(TopicMessage topicMessage){
	            		OnTopicPingEvent(topicMessage);
	            	}
	            });
	
	        TopicSubscriberCache.GetSubscriber(
	            strServerName,
	            TopicConstants.PUBLISHER_HEART_BEAT_PORT).Subscribe(
	            TopicServerHeartBeat.class.getName(),
	            new SubscriberCallbackDel(){
	            	
	            	@Override
	            	public void invoke(TopicMessage topicMessage){
	            		OnTopicPingEvent(topicMessage);
	            	}
	            });
	        
	        String strMessage = "[" + getClass().getName() + "] is started";
	        Console.writeLine(strMessage);
	        Logger.log(strMessage);
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    }

    private void HeartBeat()
    {
        while (true)
        {
        	int intSecs = Seconds.secondsBetween(m_lastTopicCheckTime, Clock.LastTime).getSeconds();
        	int intSecs2 = Seconds.secondsBetween(m_diconnectedTimer, Clock.LastTime).getSeconds();
            boolean blnIsTimeOut = intSecs > CONNECTION_TIME_OUT_SECS;
            if (blnIsTimeOut &&
                intSecs2 >
                    CONNECTION_TIME_OUT_SECS)
            {
                ConnectionState = EnumReqResp.Disconnected;
                m_connectionStateDel.invoke(m_strServerName);
                m_diconnectedTimer = DateTime.now();
            }
            else if (!blnIsTimeOut)
            {
                ConnectionState = EnumReqResp.Connected;
            }
            try {
				Thread.sleep(HEART_BEAT_TIME_SECS * 1000);
			} catch (InterruptedException ex) {
				Logger.log(ex);;
			}
        }
    }

    public void OnTopicPingEvent(TopicMessage topicMessage)
    {
        m_lastTopicCheckTime = DateTime.now();
        LastTopicPingTime = DateTime.now().toDate();
    }
}