package Armadillo.Communication.Impl.Topic;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.math3.util.Precision;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import Armadillo.Core.Console;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.NetworkHelper;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Communication.Impl.CommunicationConstants;
import Armadillo.Communication.Impl.NotifierDel;
import Armadillo.Communication.zmq.zmq.Ctx;
import Armadillo.Communication.zmq.zmq.Msg;
import Armadillo.Communication.zmq.zmq.SocketBase;
import Armadillo.Communication.zmq.zmq.ZMQ;
import Armadillo.Core.Concurrent.ProducerConsumerQueue;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Core.Serialization.ByteWrapper;
import Armadillo.Core.Serialization.ISerializerReader;
import Armadillo.Core.Serialization.ISerializerWriter;
import Armadillo.Core.Serialization.Serializer;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.Text.StringWrapper;

public class ZmqTopicSubscriber implements ITopicSubscriber {

     private Ctx m_context;
     private SocketBase m_socket;
     private final Object m_subscribeLock = new Object();
     private ConcurrentHashMap<String, ArrayList<ZmqTopicSubscriberHelper>> m_subscribers;
     private boolean m_blnIsALocalConnection;
     private HashMap<String, ArrayList<SubscriberCallbackDel>> m_callbacks;
     private boolean m_blnIsConnected;
     private final Object m_connectLockObj = new Object();
     private final Object m_startConnectLockObj = new Object();
     private String m_strServerName;
     private int m_intPort;
     private ThreadWorker<ObjectWrapper> m_subscriberWorker;
     private String m_strDoDisonnect;
     private final ConcurrentHashMap<String, ArrayList<NotifierDel>> m_subscriberNotifier =
         new ConcurrentHashMap<String, ArrayList<NotifierDel>>();
     private ProducerConsumerQueue<ByteWrapper> m_queue;
     private static ArrayList<SubscriberCallbackDel> m_onPublishAnyMessage = 
    		 new ArrayList<SubscriberCallbackDel>();
     private static Object m_subscriberLock = new Object();
     final int intWaitTimeOut = 5000;

     public ZmqTopicSubscriber()
     {
         try
         {
             m_queue = new ProducerConsumerQueue<ByteWrapper>(10, 500){
            	 @Override
            	 public void runTask(ByteWrapper byteWrapper){
            		 try{
            			 ProcessBytes0(byteWrapper);
            		 }
            		 catch(Exception ex){
            			 Logger.log(ex);
            		 }
            	 }
             };
             //m_queue.OnWork += ProcessBytes0;
         }
         catch(Exception ex)
         {
             Logger.log(ex);
         }
     }

     public void Dispose()
     {
     }

     public void Connect(String strServerName, int intPort)
     {
         try
         {
             if (!m_blnIsConnected)
             {
                 synchronized (m_startConnectLockObj)
                 {
                     m_subscribers = new ConcurrentHashMap<String, ArrayList<ZmqTopicSubscriberHelper>>();
                     m_strServerName = strServerName;
                     m_intPort = intPort;
                     if (strServerName != "local")
                     {
                         LoadConnection(strServerName);
                         Thread.sleep(1000);
                     }
                     else
                     {
                         m_callbacks = new HashMap<String, ArrayList<SubscriberCallbackDel>>();
                         m_blnIsALocalConnection = true;
                     }
                     m_subscriberWorker = new ThreadWorker<ObjectWrapper>(){
                    	 @Override
                    	 public void runTask(ObjectWrapper obj){
                             while (true)
                             {
                                 try
                                 {
                                     Resubscribe(false);
                                 }
                                 catch(Exception ex)
                                 {
                                     Logger.log(ex);
                                 }
                                 try {
									Thread.sleep(60000);
								} catch (InterruptedException ex) {
									Logger.log(ex);
								}
                             }
                    	 }
                     };
                     
                     m_subscriberWorker.work();
                     TopicClientHeartBeat.OnDisconnectedState.add(
                    		 new NotifierDel(){
                    			 @Override
                    			 public void invoke(String strCurrServerName){
                                     if(strCurrServerName == m_strServerName)
                                     {
                                         m_strDoDisonnect = "Topic client heart beat is disconnected";
                                     }
                    			 }
                    		 });

                     m_blnIsConnected = true;
                 }
             }
         }
         catch (Exception ex)
         {
             Logger.log(ex);
         }
     }

     private void LoadConnection(
         final String strServerName)
     {
         try
         {
             m_socket = null;
             ThreadWorker<StringWrapper> threadWorker = new ThreadWorker<StringWrapper>(){
            	 @Override
            	 public void runTask(StringWrapper stringWrapper){
            		 StartPoller(strServerName);
            	 }
             };
             threadWorker.work();
             while (m_socket == null)
             {
                 String strMessage = "[" + ZmqTopicSubscriber.class.getName() + "] is not ready [" +
                                     DateTime.now() + "]";
                 Console.writeLine(strMessage);
                 Logger.log(strMessage);
                 Thread.sleep(1000);
             }
         }
         catch (Exception ex)
         {
             Logger.log(ex);
         }
     }

     public void Subscribe(
         String strTopic,
         SubscriberCallbackDel subscriberCallback)
     {
         try
         {
             synchronized (m_subscribeLock)
             {
                 if (m_blnIsALocalConnection)
                 {
                     SubscribeLocal(strTopic, subscriberCallback);
                 }
                 else
                 {
                     SubscribeService(strTopic, subscriberCallback);
                 }
             }
         }
         catch (Exception ex)
         {
             Logger.log(ex);
         }
     }

     public boolean IsSubscribedToTopic(String strTopic)
     {
         return m_subscribers.containsKey(strTopic);
     }

     private void SubscribeLocal(
         String strTopic,
         SubscriberCallbackDel subscriberCallback)
     {
         try
         {
             synchronized (m_subscribeLock)
             {
                 ArrayList<SubscriberCallbackDel> callbacks = m_callbacks.get(strTopic);
                 if (callbacks == null)
                 {
                     callbacks = new ArrayList<SubscriberCallbackDel>();
                     m_callbacks.put(strTopic, callbacks);
                 }
                 callbacks.add(subscriberCallback);
             }
         }
         catch (Exception ex)
         {
             Logger.log(ex);
         }
     }

     private void SubscribeService(
         String strTopic,
         SubscriberCallbackDel subscriberCallback)
     {
         try
         {
             SubscribeSocket(strTopic);
             ZmqTopicSubscriberHelper topicHelper = new ZmqTopicSubscriberHelper(
                 strTopic,
                 subscriberCallback);
             ArrayList<ZmqTopicSubscriberHelper> topicHelpers =
            		 m_subscribers.get(strTopic);
             if (topicHelpers == null)
             {
                 topicHelpers = new ArrayList<ZmqTopicSubscriberHelper>();
                 m_subscribers.put(strTopic, topicHelpers);
             }
             topicHelpers.add(topicHelper);
         }
         catch (Exception ex)
         {
             Logger.log(ex);
         }
     }

     private void SubscribeSocket(String strTopic)
     {
         try
         {
             String strMessage;
             while (m_socket == null)
             {
                 Thread.sleep(1000);
                 strMessage = "Trying to subscribe to topic [" +
                                     strTopic + "] but socket is not ready...";
                 Console.writeLine(strMessage);
                 Logger.log(strMessage);
             }

             ISerializerWriter serializer = Serializer.GetWriter();
             serializer.Write(strTopic);
             byte[] bytes = serializer.GetBytes();
             synchronized (m_connectLockObj)
             {
            	 m_socket.setsockopt(ZMQ.ZMQ_SUBSCRIBE, bytes);
                 //m_socket.Subscribe(bytes);
             }
             Thread.sleep(10); // TODO this look is not ideal!

             strMessage = 
            		 "[" + ZmqTopicSubscriber.class.getName() + 
            		 	"]. SocketBase is subscribed to topic [" +
                                 strTopic + "]";
             Logger.log(strMessage);
         }
         catch(Exception ex)
         {
             Console.writeLine(ex);
             Logger.log(ex);
             Logger.log(ZmqTopicSubscriber.class.getName() + "subscription exception. Try to subsribe again...");
             try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				Logger.log(ex);
			}
             SubscribeSocket(strTopic);
         }
     }

     private void OnRecv()
     {
         try
         {
             if (m_socket == null)
             {
                 return;
             }
             Recv();
         }
         catch (Exception ex)
         {
             Logger.log(ex);
         }
     }

     private void Recv()
     {
         try
         {
             byte[] bytes = GetBytes();
             boolean blnIsMultiMsg;
             synchronized (m_connectLockObj)
             {
                 blnIsMultiMsg = bytes != null && 
                		 ZMQ.zmq_getsockopt (m_socket, ZMQ.ZMQ_RCVMORE) == 1L;
             }
             Thread.sleep(10); // TODO this look is not ideal!
             ByteArrayOutputStream byteArrList = null;
             boolean blnIsValidMessage = true;
             boolean blnFoundFirstMessage = false;
             String strTopic = "";
             if (blnIsMultiMsg)
             {
                 byteArrList = new ByteArrayOutputStream();
                 ISerializerReader serializerReader = Serializer.GetReader(bytes);
                 strTopic = serializerReader.ReadString(); // read the topic
                 blnFoundFirstMessage = serializerReader.ReadBoolean();
                 if (blnFoundFirstMessage)
                 {
                	 byte[] byteArr = serializerReader.ReadByteArray();
                	 byteArrList.write(byteArr);
                 }
             }

             boolean blnFoundLastMesssage = false;
             boolean blnRcvMore;
             synchronized (m_connectLockObj)
             {
                 blnRcvMore = ZMQ.zmq_getsockopt (m_socket, ZMQ.ZMQ_RCVMORE) == 1L;
             }
             Thread.sleep(10); // TODO this look is not ideal!
             while (bytes != null &&
                    blnRcvMore &&
                    byteArrList != null) // keep receiving even if it is not a valid message
             {
                 byte[] currBytes = GetBytes();
                 synchronized (m_connectLockObj)
                 {
                     blnRcvMore = ZMQ.zmq_getsockopt (m_socket, ZMQ.ZMQ_RCVMORE) == 1L;
                 }

                 ISerializerReader serializerReader = Serializer.GetReader(currBytes);
                 serializerReader.ReadString(); // read the topic
                 serializerReader.ReadBoolean(); // dummy first message flag
                 byte[] currBytesArr = serializerReader.ReadByteArray();
                 if (blnFoundFirstMessage)
                 {
                     byteArrList.write(currBytesArr);
                 }
                 blnFoundLastMesssage = serializerReader.ReadBoolean();
                 if (blnFoundLastMesssage)
                 {
                     break;
                 }
                 Thread.sleep(10); // TODO this look is not ideal!
             }

             if (blnIsMultiMsg &&
                 (!blnFoundFirstMessage ||
                  !blnFoundLastMesssage))
             {
                 blnIsValidMessage = false;
             }

             if (bytes != null &&
                 byteArrList != null &&
                 byteArrList.size() > 0 &&
                 blnIsValidMessage)
             {
            	 
            	 //byteArrList.toArray();
            	 bytes = new byte[byteArrList.size()];
            	 for (int i = 0; i < byteArrList.size(); i++) {
					
				}
                 //bytes = byteArrList.toArray(new byte[byteArrList.size()]);
                 double dblMb = Precision.round(((bytes.length/1024f)/1024f), 2);
                 String strMessage = "Debug => " +
                                     ZmqTopicSubscriber.class.getName() +
                                     " received very large message. Topic [" +
                                     strTopic + "]. [" + dblMb + "] mb";
                 Console.writeLine(strMessage);
                 Logger.log(strMessage);
             }

             if (bytes == null)
             {
                 //
                 // intTimeoutMills, reconnect
                 //
                 Reconnect("Bytes null");
             }
             else if (blnIsValidMessage)
             {
                 ProcessBytes(bytes);
             }
         }
         catch (Exception ex)
         {
             Logger.log(ex);
             Reconnect("Exception [" + ex + "]");
         }
     }

     private byte[] GetBytes()
     {
         try {
	         byte[] currBytes = null;
	         int intTotalTimeOut = 0;
	         while (currBytes == null)
	         {
	        	 try{
		             synchronized (m_connectLockObj)
		             {
		            	 Msg message = m_socket.recv(0);
		            	 if(message != null){
		            		 byte [] byteArr = message.data();
		            		 if(byteArr != null &&
		            				 byteArr.length > 0){
		            			 
		            			 currBytes = byteArr;
		            		 }
		            	 }
		             }
		             // TODO this is  not ideal!
					 Thread.sleep(10); // without this the framework locks with m_connectLockObj not able to release it
		             
		             intTotalTimeOut += intWaitTimeOut/1000;
		             if (currBytes == null)
		             {
		                 if ((intTotalTimeOut / 60) > 5 ||
		                     !StringHelper.IsNullOrEmpty(m_strDoDisonnect))
		                 {
		                     String strReason;
		                     if (!StringHelper.IsNullOrEmpty(m_strDoDisonnect))
		                     {
		                         strReason = m_strDoDisonnect;
		                     }
		                     else
		                     {
		                         strReason = "Timeout mins [" + (intTotalTimeOut / 60) + "]";
		                     }
		                     intTotalTimeOut = 0;
		                     m_strDoDisonnect = "";
		                     Reconnect(true, strReason);
		                 }
		             }
		             else
		             {
		                 intTotalTimeOut = 0;
		             }
		 		} catch (InterruptedException e) {
					Logger.log(e);
				}
	         }
	         
	         return currBytes;
	         
	         
		} catch (Exception e) {
			Logger.log(e);
		}
         return null;
     }

     private void ProcessBytes(byte[] bytes)
     {
         m_queue.add(new ByteWrapper(bytes));
     }

     @SuppressWarnings({ "rawtypes", "unchecked" })
	private void ProcessBytes0(ByteWrapper byteWrapper)
     {
         try
         {
             TopicMessage topicMessage = TopicMessage.DeserializeStatic(byteWrapper.m_bytes);
             ArrayList<ZmqTopicSubscriberHelper> subscribers;
             synchronized (m_subscribeLock)
             {
            	 subscribers = m_subscribers.get(topicMessage.TopicName);
                 if(subscribers != null)
                 {
                     subscribers = new ArrayList(subscribers);
                 }
             }
             if (subscribers != null)
             {
                 for(int i = 0; i <subscribers.size(); i++)
                 {
                     ZmqTopicSubscriberHelper zeroMqTopicSubscriberHelper = subscribers.get(i);
                     topicMessage.SetConnectionName(
                         m_strServerName + ":" + m_intPort);
                     zeroMqTopicSubscriberHelper.InvokeCallback(topicMessage);
                 }
             }
             
             ArrayList<SubscriberCallbackDel> onPublishAnyMessage;
        	 synchronized(m_subscriberLock){
        		 onPublishAnyMessage = 
                		 new ArrayList<SubscriberCallbackDel>(m_onPublishAnyMessage);
             }
        	 
             if (onPublishAnyMessage.size() > 0)
             {
            	 for (int i = 0; i < onPublishAnyMessage.size(); i++) {
                     onPublishAnyMessage.get(i).invoke(topicMessage);
				}
             }
         }
         catch (Exception ex)
         {
             Logger.log(ex);
         }
     }

     private void Reconnect(String strReason)
     {
         Reconnect(false, strReason);
     }

     private void Reconnect(
         boolean blnForceConnection,
         String strReason)
     {
    	 try{
	         if (TopicClientHeartBeat.DoNotPing)
	         {
	             return;
	         }
	         if (TopicClientHeartBeat.IsConnected(m_strServerName) &&
	             !blnForceConnection)
	         {
	             return;
	         }
	         if(m_subscribers.size() == 0)
	         {
	             return;
	         }
	         String strMessage = ZmqTopicSubscriber.class.getName() + " is reconnecting [" + 
	             strReason + "]. " + 
	             StringHelper.join(m_subscribers.keys(), "|") + "...";
	         Console.writeLine(strMessage);
	         Logger.log(strMessage);
	
	         synchronized (m_connectLockObj)
	         {
	             ZMQ.zmq_close (m_socket);
	         }
	         // TODO this is not ideal
	         Thread.sleep(10);
	         CreateConnection(m_strServerName);
	
	         //
	         // resubscribe to topics
	         //
	         Resubscribe(true);
    	 }
    	 catch(Exception ex){
    		 Logger.log(ex);
    	 }
     }

     private void Resubscribe(boolean blnNotify)
     {
         if (m_subscribers != null)
         {
             synchronized (m_subscribeLock)
             {
                 if (m_subscribers != null)
                 {
                	 Enumeration<String> enumerator = m_subscribers.keys();
                     while (enumerator.hasMoreElements())
                     {
                    	 String strTopic = enumerator.nextElement();
                    	 
                    	 if(!ZmqTopicSubscriberHelper.m_topicNameToLastUpdate.containsKey(
                    			 strTopic) ||
                    	     Seconds.secondsBetween(
                    	    		 ZmqTopicSubscriberHelper
                    	    		 	.m_topicNameToLastUpdate.get(
                                			 strTopic),
                                			 DateTime.now()).getSeconds() > 60)
                    	 {
	                         SubscribeSocket(strTopic);
	                         if (blnNotify)
	                         {
	                             ArrayList<NotifierDel> notifierDelList =
	                            		 m_subscriberNotifier.get(strTopic);
	                             if (notifierDelList != null)
	                             {
	                                 for (int i = 0; i < notifierDelList.size(); i++)
	                                 {
	                                     notifierDelList.get(i).invoke(strTopic);
	                                 }
	                             }
	                         }
                    	 }
                     }
                 }
             }
         }
     }

     public void NotifyDesconnect(
         String strTopic,
         NotifierDel notifierDel)
     {
         ArrayList<NotifierDel> notifierDelList = 
        		 m_subscriberNotifier.get(strTopic);
         if (notifierDelList == null)
         { 
             notifierDelList = new ArrayList<NotifierDel>();
             m_subscriberNotifier.put(strTopic, notifierDelList);
         }
         notifierDelList.add(notifierDel);
     }

     private void StartPoller(String strServerName)
     {
         try
         {
             CreateConnection(strServerName);
             while (true)
             {
                 OnRecv();
             }
         }
         catch (Exception ex)
         {
             Logger.log(ex);
         }
     }

     private void CreateConnection(String strServerName)
     {
    	 try{
	         if (m_intPort == 0)
	         {
	             try {
					throw new HCException("Invalid port");
				} catch (HCException ex) {
					Logger.log(ex);
				}
	         }
	
	         synchronized (m_connectLockObj)
	         {
	             if (m_context == null)
	             {
	                 m_context = ZMQ.zmq_init (1);
	             }
	             SocketBase socket = ZMQ.zmq_socket (m_context, ZMQ.ZMQ_SUB);
	             String strIp = NetworkHelper.GetIpAddr(strServerName);
	             String strAddr = "tcp://" + strIp + ":" + m_intPort;
	             //socket.HWM = CoreConstants.HWM; 
	             ZMQ.zmq_setsockopt (socket, ZMQ.ZMQ_RCVHWM, CommunicationConstants.HWM);
	             
	             if(!ZMQ.zmq_connect (socket, strAddr))
	             {
	             	try {
						throw new HCException("Could not connect");
					} catch (HCException ex) {
						Logger.log(ex);
					}
	             }
	             
	             //socket.Connect(strAddr);
	             String strMessage = "[" + ZmqTopicSubscriber.class.getName() + "]" + 
	            		 " is connected to [" +
	                                 strAddr + "]";
	             Console.writeLine(strMessage);
	             Logger.log(strMessage, false, false, false);
	
	             //
	             // wait for connection to be loaded
	             //
	             try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					Logger.log(ex);
				}
	        	socket.setsockopt(ZMQ.ZMQ_RCVTIMEO, intWaitTimeOut);
	            m_socket = socket;
	         }
	         // TODO this is not ideal
	         Thread.sleep(10);
    	 }
    	 catch(Exception ex){
    		 Logger.log(ex);
    	 }
     }

     public void UnSubscribe(String strTopic)
     {
         try
         {
             ISerializerWriter serializer = Serializer.GetWriter();
             serializer.Write(strTopic);
             synchronized (m_connectLockObj)
             {
                 //m_socket.Unsubscribe(serializer.GetBytes());
                 m_socket.setsockopt(ZMQ.ZMQ_UNSUBSCRIBE, serializer.GetBytes());
             }
             Thread.sleep(10); // TODO this look is not ideal!
             ArrayList<ZmqTopicSubscriberHelper> subscribersList = m_subscribers.get(strTopic);
             
             if (subscribersList != null)
             {
            	 m_subscribers.remove(strTopic);
                 subscribersList.clear();
             }
         }
         catch (Exception ex)
         {
             Logger.log(ex);
         }
     }

     public void Publish(TopicMessage topicMessage)
     {
         try
         {
             if (m_blnIsALocalConnection)
             {
                 PublishToLocal(topicMessage);
             }
             else
             {
                 PublishToService(topicMessage);
             }
         }
         catch (Exception ex)
         {
             Logger.log(ex);
         }
     }

     public int SubscriberCount(String strTopic)
     {
         try
         {
             ArrayList<SubscriberCallbackDel> callbacks = 
            		 m_callbacks.get(strTopic);
             if (callbacks != null)
             {
                 return callbacks.size();
             }
             return 0;
         }
         catch (Exception ex)
         {
             Logger.log(ex);
         }
         return 0;
     }

     private void PublishToLocal(TopicMessage tioTopicMessage) throws HCException
     {
         Exception exception = null;
         try
         {
             if (tioTopicMessage != null)
             {
                 try
                 {
                     String strTopicName = tioTopicMessage.TopicName;
                     ArrayList<SubscriberCallbackDel> subscriberCallbacks =
                    		 m_callbacks.get(strTopicName);
                     if (subscriberCallbacks != null)
                     {
                         synchronized (m_subscribeLock)
                         {
                             subscriberCallbacks = new ArrayList<SubscriberCallbackDel>(subscriberCallbacks);
                         }
                         for (SubscriberCallbackDel subscriberCallback : subscriberCallbacks)
                         {
                             subscriberCallback.invoke(tioTopicMessage);
                         }
                     }
                 }
                 catch (Exception ex)
                 {
                     exception = ex;
                     Logger.log(ex);
                 }
             }
         }
         catch (Exception ex)
         {
             exception = ex;
             Logger.log(ex);
         }
         if (exception != null)
         {
             throw new HCException(exception.getMessage());
         }
     }

     private void PublishToService(TopicMessage topicMessage)
     {
         try
         {
        	 
        	 
        	 
             byte[] bytes = Serializer.getBytes(topicMessage);
             ISerializerWriter serializer = Serializer.GetWriter();
             serializer.Write(topicMessage.TopicName);
             serializer.Write(bytes);

             boolean blnSuccess = false;
             int intCounter = 0;
             
             while (!blnSuccess)
             {
                 try
                 {
                     synchronized (m_connectLockObj)
                     {
                    	 blnSuccess = m_socket.send(new Msg(serializer.GetBytes()), 0);
                         //status = m_socket.Send(serializer.GetBytes());
                     }
                     
                     // TODO this is not ideal
                     Thread.sleep(10);
                     if (!blnSuccess)
                     {
                         String strMessage = ZmqTopicSubscriber.class.getName() +
                                             " could not send message [" +
                                             blnSuccess + "][" +
                                         intCounter + "]. Resending...";
                         Logger.log(strMessage);
                         Console.writeLine(strMessage);
                         Thread.sleep(5000);
                     }
                 }
                 catch (Exception ex)
                 {
                     String strMessage = ZmqTopicSubscriber.class.getName() +
                                         " could not send message [" +
                                         blnSuccess + "][" +
                                         intCounter + "]. Resending...";
                     Logger.log(strMessage);
                     Console.writeLine(strMessage);
                     Thread.sleep(5000);
                     Logger.log(ex);
                 }

                 intCounter++;

                 if (intCounter > 10)
                 {
                     intCounter = 0;
                     Reconnect(true, "Number if trials is [" + intCounter + "]");
                 }
                 
                 Thread.sleep(100); // TODO, this is not ideal!
             }
         }
         catch (Exception ex)
         {
             Logger.log(ex);
         }
     }
     
     public static void subscribePublishAnyMessage(SubscriberCallbackDel e){
    	 synchronized(m_subscriberLock){
    		 m_onPublishAnyMessage.add(e);
    	 }
     }
}