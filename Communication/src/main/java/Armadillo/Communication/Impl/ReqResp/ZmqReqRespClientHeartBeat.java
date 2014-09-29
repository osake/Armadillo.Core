package Armadillo.Communication.Impl.ReqResp;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;

import Armadillo.Core.Clock;
import Armadillo.Core.Config;
import Armadillo.Core.Console;
import Armadillo.Core.KeyValuePair;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Communication.Impl.SimpleUiSocket;
import Armadillo.Communication.Impl.Topic.SubscriberCallbackDel;
import Armadillo.Communication.Impl.Topic.TopicConstants;
import Armadillo.Communication.Impl.Topic.TopicMessage;
import Armadillo.Communication.Impl.Topic.TopicPublisherCache;
import Armadillo.Communication.Impl.Topic.TopicSubscriberCache;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Core.SelfDescribing.SelfDescribingClass;

public class ZmqReqRespClientHeartBeat {
    private final static int INDIVIDUAL_SOCKET_TIMEOUT_MINS = 5;
    public final static int TIME_OUT_SECS = 30;

    private final ThreadWorker<ObjectWrapper> m_threadWorker;
    private DateTime m_lastRecvPingTime = DateTime.now();
    private boolean m_blnPingStarted;
    private final Object m_lockPingStart = new Object();
    private ThreadWorker<ObjectWrapper> m_baseSocketWorker;
    private ThreadWorker<ObjectWrapper> m_pingWorker;
    private final ZmqRequestResponseClient m_zmqRequestResponseClient;

    private static final ConcurrentHashMap<String,DateTime> m_whoIsPingMap = 
    		new ConcurrentHashMap<String,DateTime>();
    private static ThreadWorker<ObjectWrapper> m_whoIsMapFlusher;
    
    static{
		try{
	    	String strServerName = Config.getStringStatic(
	    			"TopicServerName",
	    			SimpleUiSocket.class);
	    	
			SubscriberCallbackDel subscriberCallbackDel2 = new SubscriberCallbackDel(){
				@Override
				public void invoke(TopicMessage topicMessage){
					if(topicMessage.EventData != null){
						String strRequestId = (String)topicMessage.EventData;
						m_whoIsPingMap.put(strRequestId, DateTime.now());
					}
				}
			};
			TopicSubscriberCache.GetSubscriber(
					strServerName,
					TopicConstants.PUBLISHER_HEART_BEAT_PORT).Subscribe(
					EnumReqResp.WhoIsPingTopic.toString(), 
					subscriberCallbackDel2);
			
			//
			// flush whois requests
			//
			m_whoIsMapFlusher = new ThreadWorker<ObjectWrapper>(){
				@Override
				public void runTask(ObjectWrapper item) throws Exception {
					
					try{
						
						while(true){
							
							try{
								
								ArrayList<String> keysToDelete = new ArrayList<String>();
								
								for(Entry<String, DateTime> kvp : m_whoIsPingMap.entrySet()){
									
									int intMins = Minutes.minutesBetween(kvp.getValue(), 
											DateTime.now()).getMinutes();
									
									if(intMins > 60){
										keysToDelete.add(kvp.getKey());
									}
								}
								
								if(keysToDelete.size() > 0){
									
									for(String strKey : keysToDelete){
										m_whoIsPingMap.remove(strKey);
									}
								}
							}
							catch(Exception ex){
								Logger.log(ex);
							}
							finally{
								Thread.sleep(60000);
							}
						}
					}
					catch(Exception ex){
						Logger.log(ex);
					}
				}				
			};
			m_whoIsMapFlusher.work();
		}
		catch(Exception ex){
			Logger.log(ex);
		}
    }
    
    public ZmqReqRespClientHeartBeat(
        ZmqRequestResponseClient zmqRequestResponseClient)
    {
        m_zmqRequestResponseClient = zmqRequestResponseClient;
        m_threadWorker = new ThreadWorker<ObjectWrapper>(){
        	@Override
        	public void runTask(ObjectWrapper item) throws Exception {
        		PingThread();
        	}
        };
        //m_threadWorker.OnExecute += PingThread;
        m_threadWorker.work();
        LoadPingWorker();
        StartBaseSocketPinger(zmqRequestResponseClient.BaseSocket);
    }

    private void StartBaseSocketPinger(
        final KeyValuePair<ZmqReqRespClientSocketWrapper, SocketInfo> baseSocket)
    {
        m_baseSocketWorker = new ThreadWorker<ObjectWrapper>(){
        	@Override
        	public void runTask(ObjectWrapper item) throws Exception {
                while (true)
                {
                    ReentrantReadWriteLock rwl = baseSocket.getKey().m_rwl;
                    ReadLock readLock = rwl.readLock();
                    try
                    {
                        readLock.lock();
                        ZmqReqRespClientSocketWrapper.SendBytes0(
                            new byte[] { 1 },
                            false,
                            baseSocket.getKey().Socket,
                            baseSocket.getKey().m_sendRcvLock);
                        Thread.sleep(3000);
                    }
                    catch (Exception ex)
                    {
                        Logger.log(ex);
                        Thread.sleep(5000);
                    }
                    finally
                    {
                        try
                        {
                        	readLock.unlock();
                        }
                        catch(Exception ex)
                        {
                            Logger.log(ex);
                        }
                    }
                }
        	}
        };
        m_baseSocketWorker.work();
    }

    public void StartPing(ZmqReqRespClientSocketWrapper socketWrapper)
    {
        if(m_blnPingStarted)
        {
            return;
        }
        synchronized (m_lockPingStart)
        {
            if (m_blnPingStarted)
            {
                return;
            }

            ZmqReqRespClientSocketWrapper.SendPingBytes(
                socketWrapper);

            m_blnPingStarted = true;
            m_lastRecvPingTime = DateTime.now();
        }
    }

    public void Ping(ZmqReqRespClientSocketWrapper socketWrapper)
    {
        m_lastRecvPingTime = DateTime.now();
        socketWrapper.LastRecvPingTime = m_lastRecvPingTime;
    }

    public void LoadPingWorker()
    {
        try
        {
            final String strServerName = Config.getStringStatic(
            		"TopicServerName",
            		SimpleUiSocket.class);
            m_pingWorker = new ThreadWorker<ObjectWrapper>(){
            	@Override
            	public void runTask(ObjectWrapper item){
                    while (true)
                    {
                        try
                        {
                            while (TopicPublisherCache.GetPublisher(
                                strServerName,
                                TopicConstants.SUBSCRIBER_HEART_BEAT_PORT) == null)
                            {
                                String strMessage = ZmqReqRespClientSocketWrapper.class.getName() +
                                                    " topic publisher is not initialized.";
                                Console.writeLine(strMessage);
                                Logger.log(strMessage);
                                Thread.sleep(1000);
                            }
                            SelfDescribingClass pingObj = new SelfDescribingClass();
                            String strTopic = EnumReqResp.AsyncHeartBeatClientToServerTopic.toString();
                            pingObj.SetClassName(ZmqRequestResponseClient.class.getName() + "1_" +
                                                 strTopic);
                            TopicPublisherCache.GetPublisher(
                                strServerName,
                                TopicConstants.SUBSCRIBER_HEART_BEAT_PORT).SendMessageImmediately(
                                pingObj,
                                strTopic);
                        }
                        catch (Exception ex)
                        {
                            Logger.log(ex);
                        }
                        try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							Logger.log(e);
						}
                    }
            	}
            };
            m_pingWorker.work();

            Logger.log("Loaded AsynClient [" + Config.getClientName() + "] ping worker.");
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private void PingThread()
    {
        while (true)
        {
            DateTime now = Clock.LastTime;
            Seconds period = Seconds.secondsBetween(m_lastRecvPingTime, now);
            
            //
            // check individual request timeout
            //
            checkIndividualRequests();
            
            //
            // check message ping messages from server
            //
            checkServerPings(period);

            //
            // check individual sockets
            //
            checkIndivudalSocketTimeout();

            try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				Logger.log(e);
			}
        }
    }

	private void checkIndivudalSocketTimeout() {
		
		try{
			if (m_zmqRequestResponseClient.Sockets != null)
			{
			    for (Map.Entry<ZmqReqRespClientSocketWrapper, SocketInfo> kvp :
			        m_zmqRequestResponseClient.Sockets.entrySet())
			    {
			        double dblTimeMins = 
			        		Seconds.secondsBetween(kvp.getKey().LastRecvPingTime, Clock.LastTime).getSeconds() / 60.0;
			        //
			        // leave timeout long since we are not sure if a long message is on its way.
			        // Remember that 0mq sends multipart messages, but all of these multipart messages come at the same time!
			        // A message which takes longer than INDIVIDUAL_SOCKET_TIMEOUT_MINS mins to send is too long for the infrastructure
			        //
			        if (dblTimeMins > INDIVIDUAL_SOCKET_TIMEOUT_MINS && 
			            kvp.getKey().NumRequests() > 0)
			        {
			            String strMessage = getClass().getName() +
			                                "Individual socket timeout [" + dblTimeMins + " mins]. Request-Response Connection timeout [" +
			                                kvp.getKey().EndPointAddr + "] at [" +
			                                DateTime.now() + "]. Total time [" + dblTimeMins + "]. Reconnecting...";
			            Console.writeLine(strMessage);
			            Logger.log(strMessage);
			            kvp.getKey().Connect();
			        }
			    }
			}
		}
		catch(Exception ex){
			Logger.log(ex);
		}
	}

	private void checkServerPings(Seconds period) {
		
		try{
			if (period.getSeconds() > TIME_OUT_SECS &&
			    m_lastRecvPingTime != new DateTime())
			{
	
			    //
			    // check if there are requests to do
			    //
			    boolean blnExistsRequests = false;
			    for (Map.Entry<ZmqReqRespClientSocketWrapper, SocketInfo> kvp : m_zmqRequestResponseClient.Sockets.entrySet())
			    {
			        if(kvp.getKey().NumRequests() > 0)
			        {
			            String strMessage = "Connection timeout. Active requests[" +
			            		kvp.getKey().NumRequests() + "]";
			            Logger.log(strMessage);
			            Console.writeLine(strMessage);
			            blnExistsRequests = true;
			            break;
			        }
			    }
	
			    if (blnExistsRequests)
			    {
			        //
			        // reconnect base socket. Very important! connect the base socket in the end. 
			        // todo if the base socket connects fine, then how do we know if a particular socket dies?
			        // (already done) either we reconnect individual socket at a timeout 
			        // todo - another solution would be to include in the ping (via base connection) which sockets are "busy" with large messages
			        //
			        m_zmqRequestResponseClient.BaseSocket.getKey().Connect();
			        Ping(m_zmqRequestResponseClient.BaseSocket.getKey());
	
			        //
			        // reset connection
			        //
			        for(Map.Entry<ZmqReqRespClientSocketWrapper, SocketInfo> kvp : m_zmqRequestResponseClient.Sockets.entrySet())
			        {
			            String strMessage = getClass().getName() + " request-Response Connection timeout [" +
			                                kvp.getKey().EndPointAddr + "] at [" +
			                                DateTime.now() + "]. Reconnecting...";
			            Console.writeLine(strMessage);
			            Logger.log(strMessage);
			            kvp.getKey().Connect();
			            Ping(kvp.getKey());
			        }
			    }
			}
		}
		catch(Exception ex){
			Logger.log(ex);
		}
	}

	private void checkIndividualRequests() {
		
		try{
			for (Map.Entry<ZmqReqRespClientSocketWrapper, SocketInfo> kvp : m_zmqRequestResponseClient.Sockets.entrySet())
			{
				//
				// iterate each request map
				//
				for(Entry<String, RequestDataMessage> kvpReqMap : kvp.getKey().RequestMap.entrySet()){
					String strReqId = kvpReqMap.getKey();
					
					if(!m_whoIsPingMap.containsKey(strReqId)){
						m_whoIsPingMap.put(strReqId, DateTime.now());
					}
					
					DateTime lastPingTime = m_whoIsPingMap.get(strReqId);
					int intSeconds = Seconds.secondsBetween(
							lastPingTime,
							DateTime.now()).getSeconds();
					if(intSeconds > 60){
						//
						// disconnection is detected
						//
			            String strMessage = "ReqRespId [" +
			            		strReqId + "] timeout at [" +
			            		DateTime.now() + "]. Reconnecting...";
			            Logger.log(strMessage);
			            Console.writeLine(strMessage);
			            
			            kvp.getKey().Connect();
			            Ping(kvp.getKey());
			            m_whoIsPingMap.put(strReqId, DateTime.now());
					}
				}
			}
		}
		catch(Exception ex){
			Logger.log(ex);
		}
	}
}
