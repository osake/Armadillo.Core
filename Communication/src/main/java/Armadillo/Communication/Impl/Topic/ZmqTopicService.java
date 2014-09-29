package Armadillo.Communication.Impl.Topic;

import Armadillo.Core.Config;
import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Communication.Impl.CommunicationConstants;
import Armadillo.Communication.Impl.SimpleUiSocket;
import Armadillo.Communication.zmq.zmq.Ctx;
import Armadillo.Communication.zmq.zmq.Msg;
import Armadillo.Communication.zmq.zmq.SocketBase;
import Armadillo.Communication.zmq.zmq.ZMQ;
import Armadillo.Core.Concurrent.ThreadWorker;

public class ZmqTopicService {

        private static boolean m_blnIsConnected;
        private static final Object m_connectLockObj = new Object();
        private static boolean m_blnIsReady;

        public static void Connect()
        {
            if (!m_blnIsConnected)
            {
                synchronized (m_connectLockObj)
                {
                    if (!m_blnIsConnected)
                    {
                        m_blnIsConnected = true;
                        String strServerName = Config.getStringStatic(
                        		"TopicServerName",
                        		SimpleUiSocket.class);
                        if (strServerName != "local")
                        {
                        	new ThreadWorker<ObjectWrapper>()
                        	{
                        		@Override
                        		public void runTask(ObjectWrapper obj){
                                    DoConnect(TopicConstants.PUBLISHER_DEFAULT_PORT,
                                            TopicConstants.SUBSCRIBER_DEFAULT_PORT);
                        		}
                        	}.work();
                        	
                            while (!m_blnIsReady)
                            {
                                try {
									Thread.sleep(100);
								} catch (InterruptedException ex) {
									Logger.log(ex);
								}
                            }
                            m_blnIsReady = false;
                            
                        	new ThreadWorker<ObjectWrapper>(){
                        		
                        		@Override
                        		public void runTask(ObjectWrapper obj){
                        			
                                    DoConnect(
                                            TopicConstants.PUBLISHER_HEART_BEAT_PORT,
                                            TopicConstants.SUBSCRIBER_HEART_BEAT_PORT);
                        		}
                        	}.work();
                        	
                            while (!m_blnIsReady)
                            {
                                try {
									Thread.sleep(100);
								} catch (InterruptedException ex) {
									Logger.log(ex);
								}
                            }
                            try {
								Thread.sleep(1000);
							} catch (InterruptedException ex) {
								Logger.log(ex);
							}
                        }
                    }
                }
            }
        }

        private static void DoConnect(
            int intPublisherPort,
            int intSubscriberPort)
        {
        	
            while (true)
            {
            	Ctx context = ZMQ.zmq_init (1);
                //using (var context = new Context())
                try
            	{
                    //using (Socket publisher = context.Socket(SocketType.PUB))
                    SocketBase publisher = ZMQ.zmq_socket (context, ZMQ.ZMQ_PUB);

                    try
                	{
                        String strPublisherConn = "tcp://*:" + intPublisherPort;
                        ZMQ.zmq_setsockopt (publisher, ZMQ.ZMQ_RCVHWM, CommunicationConstants.HWM);
                        //publisher.HWM = CoreConstants.HWM;
                        
                        publisher.bind(strPublisherConn);
                        String strMessage = "Publisher socket connected to [" +
                                            strPublisherConn + "]";
                        Logger.log(strMessage);
                        Console.writeLine(strMessage);
                        
                        SocketBase dealerSocket = ZMQ.zmq_socket (context, ZMQ.ZMQ_DEALER);
                        //using (Socket dealerSocket = context.Socket(SocketType.DEALER))
                        try
                        {
                            String strDealerConnection = "tcp://*:" + intSubscriberPort;
                            //dealerSocket.HWM = CoreConstants.HWM;
                            ZMQ.zmq_setsockopt (dealerSocket, ZMQ.ZMQ_RCVHWM, CommunicationConstants.HWM);
                            dealerSocket.bind(strDealerConnection);
                            strMessage = "Dealer socket connected to [" +
                                         strDealerConnection + "]";

                            Logger.log(strMessage);
                            Console.writeLine(strMessage);

                            Logger.log("ZeroMq topic server connected");
                            m_blnIsReady = true;
                            while (true)
                            {
                            	
                            	Msg message = dealerSocket.recv(0);
                                byte[] bytes = message.buf().array();
                                boolean blnMore = ZMQ.zmq_msg_get (message, ZMQ.ZMQ_MORE) == 1;
                                SendToPublisher(
                                		publisher, 
                                		bytes, 
                                		blnMore);
                                
                                while (blnMore && bytes != null)
                                {
                                	message = dealerSocket.recv(0);
                                    bytes = message.buf().array();
                                    blnMore = ZMQ.zmq_msg_get (message, ZMQ.ZMQ_MORE) == 1;
                                    SendToPublisher(
                                    		publisher, 
                                    		bytes, 
                                    		blnMore);
                                }
                            }
                        }
                        finally{
                        	ZMQ.zmq_close (dealerSocket);                    	
                        }
                    }
                    finally{
                    	ZMQ.zmq_close (publisher);                    	
                    }
                }
                finally{
                	ZMQ.zmq_term (context);
                }
            }
        }

        private static void SendToPublisher(
        	SocketBase publisher, 
            byte[] bytes,
            boolean blnSendMore)
        {
            boolean blnSuccess = false;
            boolean status = false;
            int intCounter = 0;
            while (!blnSuccess)
            {
                try
                {
                    if (blnSendMore)
                    {
                        status = publisher.send(new Msg(bytes), ZMQ.ZMQ_MORE);
                    }
                    else
                    {
                        status = publisher.send(new Msg(bytes), 0);
                    }
                    blnSuccess = status;
                    if (!blnSuccess)
                    {
                        String strMessage = ZmqTopicService.class.getName() +
                                            " could not send message [" +
                                            status + "][" +
                                            intCounter + "]. Resending...";
                        Logger.log(strMessage);
                        Console.writeLine(strMessage);
                        Thread.sleep(5000);
                    }
                }
                catch (Exception ex)
                {
                    String strMessage = ZmqTopicService.class.getName() +
                                        " could not send message [" +
                                        status + "][" +
                                        intCounter + "]. Resending...";
                    Logger.log(strMessage);
                    Console.writeLine(strMessage);
                    Logger.log(ex);
                    try {
						Thread.sleep(5000);
					} catch (InterruptedException ex2) {
						Logger.log(ex2);
					}
                }
                intCounter++;
            }
        }
}
