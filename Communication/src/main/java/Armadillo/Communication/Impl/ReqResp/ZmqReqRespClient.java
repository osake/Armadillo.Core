package Armadillo.Communication.Impl.ReqResp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import Armadillo.Core.Config;
import Armadillo.Core.Console;
import Armadillo.Core.KeyValuePair;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Core.Concurrent.EfficientProducerConsumerQueue;
import Armadillo.Core.Concurrent.ILoopBody;
import Armadillo.Core.Concurrent.Parallel;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Core.Text.StringHelper;

public class ZmqReqRespClient extends ARequestResponseClient 
{
        public KeyValuePair<ZmqReqRespClientSocketWrapper, SocketInfo> BaseSocket;
        public ConcurrentHashMap<ZmqReqRespClientSocketWrapper, SocketInfo> Sockets;
        public ZmqReqRespClientHeartBeat ZmqReqRespClientHeartBeat;
        public EfficientProducerConsumerQueue<RequestJob> SendJobEfficientQueue;
        public int NumConnections;

        private boolean m_socketsReady;
        private final Object m_socketLock = new Object();
        private int m_intRequestCounter;
        private Object m_lockCounter = new Object();
        
        private Random m_rng = new Random();

        public ZmqReqRespClient(
            String strServerName,
            int intPort)
             { 
        		super(strServerName, intPort);
             }

        public void Dispose()
        {
            if (Sockets != null &&
                Sockets.size() > 0)
            {
                for (ZmqReqRespClientSocketWrapper socketWrapper : Sockets.keySet())
                {
                    socketWrapper.Dispose();
                }
            }
        }

        @Override
        public void DoConnect(
            final String strServerName,
            final int intPort,
            final int intConnections)
        {
            try
            {
                if (!strServerName.equals("local"))
                {
                    if (!m_blnIsLocalConnection)
                    {
                        NumConnections = intConnections;
                        ThreadWorker<ObjectWrapper> worker = new ThreadWorker<ObjectWrapper>(){
                        	@Override
                        	public void runTask(ObjectWrapper item) {
                                LoadConnections(strServerName,
                                		intPort,
                                		intConnections);
                        	}
                        };
                        worker.work();
                        while (!m_socketsReady)
                        {
                            Thread.sleep(100);
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                Logger.log(ex);
            }
        }

        @Override
        public List<Object> SendRequestAndGetResponse(
            RequestDataMessage requestDataMessage,
            int intTimeOutSeconds)
        {
            List<Object> response;
            if (ServerName.equals("local") ||
                (ReqRespServer.IsInitialized &&
                 ReqRespServer.OwnInstance.ServerName.equals(ServerName) &&
                 ReqRespServer.OwnInstance.Port == Port))
            {
                response = ReqRespService.InvokeRequestEventHandler(
                        requestDataMessage).Response;
            }
            else
            {
                response = SendRequestAndGetResponseViaService(
                    requestDataMessage);
            }
            return response;
        }

        private List<Object> SendRequestAndGetResponseViaService(
            RequestDataMessage requestDataMessage)
        {
            try
            {
            	synchronized(m_lockCounter){
            		m_intRequestCounter++;
            	}
                
                ZmqReqRespClientSocketWrapper socketWrapper = GetSocket();
                ArrayList<Object> responseObjs = new ArrayList<Object>();

                boolean blnIsError = true;
                while (blnIsError)
                {
                    String strRequestId =
                        Config.getClientName() + "_" +
                        m_intRequestCounter + "_" +
                        UUID.randomUUID().toString();
                    
                    List<RequestDataMessage> responseList = socketWrapper.SendRequestAndGetResponse(
                        requestDataMessage,
                        strRequestId);

                    String strError = "";
                    blnIsError = responseList != null &&
                                      responseList.size() > 0 &&
                                      !(StringHelper.IsNullOrEmpty(strError = responseList.get(0).Error));
                    if (blnIsError)
                    {
                        //
                        // faulty state, resend message
                        //
                        String strMesssage = "Req/Resp client received faulty message [" +
                                             strError + "]. Resending reqId [" +
                                             strRequestId + "] " +
                                             requestDataMessage;
                        Console.writeLine(strMesssage);
                        Logger.log(strMesssage);
                        Thread.sleep(5000); // slow down
                        requestDataMessage.SetResponse(null);
                        requestDataMessage.Error = ""; // reset error
                    }
                    else
                    {
                        if (responseList != null)
                        {
                            for (int i = 0; i < responseList.size(); i++)
                            {
                                responseObjs.addAll(responseList.get(i).Response);
                            }
                        }
                    }
                }

                synchronized (m_socketLock)
                {
                    socketWrapper.NumUsages--;
                }

                return responseObjs;
            }
            catch(Exception ex)
            {
                Logger.log(ex);
                try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					Logger.log(e);
				}
                return SendRequestAndGetResponseViaService(requestDataMessage);
            }
        }
        

        private ZmqReqRespClientSocketWrapper GetSocket()
        {
            try
            {
                //
                // loop until we manage to get an empty socket
                //
                while (true)
                {
                    if (Sockets.size() > 0)
                    {
                        synchronized (m_socketLock)
                        {
                            if (Sockets.size() > 0)
                            {
                                Set<Entry<ZmqReqRespClientSocketWrapper, SocketInfo>> socketsArr =
                                    Sockets.entrySet();
                                int intMinNumRequests = Integer.MAX_VALUE;
                                ZmqReqRespClientSocketWrapper socketWrapper = null;
                                for (Map.Entry<ZmqReqRespClientSocketWrapper, SocketInfo> kvp : socketsArr)
                                {
                                    ZmqReqRespClientSocketWrapper currSocket = kvp.getKey();
                                    int intCurrReq = currSocket.NumUsages;
                                    if (intCurrReq < intMinNumRequests)
                                    {
                                        intMinNumRequests = intCurrReq;
                                        socketWrapper = currSocket;
                                    }
                                }
                                if (socketWrapper != null)
                                {
                                	List<ZmqReqRespClientSocketWrapper> sockets = new ArrayList<ZmqReqRespClientSocketWrapper>();
                                	
                                    for (Map.Entry<ZmqReqRespClientSocketWrapper, SocketInfo> kvp : socketsArr)
                                    {
                                        ZmqReqRespClientSocketWrapper currSocket = kvp.getKey();
                                        int intCurrReq = currSocket.NumUsages;
                                        if(intCurrReq == intMinNumRequests)
                                        {
                                        	sockets.add(currSocket);
                                        }
                                    }
                                    Shuffle(sockets);
                                    socketWrapper.NumUsages++;
                                    return socketWrapper;
                                }
                            }
                        }
                    }
                    Thread.sleep(100);
                }
            }
            catch(Exception ex)
            {
                Logger.log(ex);
            }
            return null;
        }

        public <T> void Shuffle(List<T> a)
        {
        	try
        	{
    	        int N = a.size();
    	        for (int i = 0; i < N; i++)
    	        {
    	            int r = i + (int) (m_rng.nextDouble()*(N - i)); // between i and N-1
    	            Exch(a, i, r);
    	        }
        	}
        	catch(Exception ex)
        	{
        		Logger.log(ex);
        	}
        }
        
        private void LoadConnections(
            String strServerName,
            int intPortId,
            int intConnections)
        {
            ZmqReqRespClientSocketWrapper socket = CreateSocket(strServerName, 
                intPortId-1);
            BaseSocket = new KeyValuePair<ZmqReqRespClientSocketWrapper, SocketInfo>(
                socket, socket.EndPointAddr);
            socket.IsBaseSocket = true;
            Sockets = new ConcurrentHashMap<ZmqReqRespClientSocketWrapper, SocketInfo>();
            final String strDns = strServerName.toLowerCase();
            
            Parallel.For(intPortId, intPortId +
                    intConnections, new ILoopBody<Integer>() {
            			public void run(Integer i) {
            				CreateConnection(strDns, i);
            			}
            		});
            
            LoadSendJobQueue();
            m_socketsReady = true;
            ZmqReqRespClientHeartBeat = new ZmqReqRespClientHeartBeat(this);
        }

        // swaps array elements i and j
        private <T> void Exch(List<T> a, int i, int j)
        {
            T swap = a.get(i);
            a.set(i, a.get(j));
            a.set(j, swap);
        }

        private void LoadSendJobQueue()
        {
            try
            {
                SendJobEfficientQueue =
                    new EfficientProducerConsumerQueue<RequestJob>(
                    		RequestResponseConstant.CLIENT_REQUEST_THREADS){
                	@Override
                	public void runTask(RequestJob jobItem){
                		ZmqReqRespClientSocketWrapper.DoSendRequest(jobItem.RequestDataMessage,
                                jobItem.RequestId,
                                jobItem.RequestMap,
                                jobItem.SocketWrapper,
                                jobItem.Rwl);                	}
                };
            }
            catch (Exception ex)
            {
                Logger.log(ex);
            }
        }

        private void CreateConnection(String strServerName, int intPort)
        {
            try
            {
                ZmqReqRespClientSocketWrapper socket = 
                		CreateSocket(strServerName, intPort);
                Sockets.put(socket, socket.EndPointAddr);
            }
            catch (Exception ex)
            {
                Logger.log(ex);
            }
        }

        private ZmqReqRespClientSocketWrapper CreateSocket(
            String strServerName, 
            int intPort)
        {
            SocketInfo socketInfo = new SocketInfo();
            socketInfo.DNS = strServerName;
            socketInfo.Port = intPort;
            ZmqReqRespClientSocketWrapper socket = new ZmqReqRespClientSocketWrapper(
                socketInfo,
                this);
            return socket;
        }
}
