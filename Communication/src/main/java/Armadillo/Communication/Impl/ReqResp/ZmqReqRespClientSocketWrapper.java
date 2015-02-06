package Armadillo.Communication.Impl.ReqResp;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.apache.commons.math3.util.Precision;
import org.joda.time.DateTime;
import Armadillo.Core.BooleanWrapper;
import Armadillo.Core.Console;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Communication.Impl.CommunicationConstants;
import Armadillo.Communication.zmq.zmq.Ctx;
import Armadillo.Communication.zmq.zmq.Msg;
import Armadillo.Communication.zmq.zmq.ZMQ;
import Armadillo.Core.Concurrent.Task;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Communication.zmq.zmq.SocketBase;
import Armadillo.Core.Serialization.ISerializerReader;
import Armadillo.Core.Serialization.ISerializerWriter;
import Armadillo.Core.Serialization.Serializer;

public class ZmqReqRespClientSocketWrapper {
	
    private final ZmqReqRespClient m_zmqRequestResponseClient;
    private Ctx m_context;
    private ThreadWorker<ObjectWrapper> m_socketRecvWorker;
    private ZmqReqRespClientAck m_zmqReqRespClientAck;
    public final ReentrantReadWriteLock m_rwl = new ReentrantReadWriteLock();
    private int m_intContextCounter;
    private static final int CONTEXT_RESET_COUNTER= 20;
    private final Object m_connectLock = new Object();
    public final Object m_sendRcvLock = new Object();
    private ThreadWorker<ObjectWrapper> m_pingWorker;
    public ConcurrentHashMap<String, RequestDataMessage> RequestMap;
    public boolean IsBaseSocket;
    public SocketBase Socket;
    public DateTime ConnectionDate ;
    public SocketInfo EndPointAddr;
    public int NumUsages;
    
    public int NumRequests()
    {
    	return RequestMap.size();
    }

    public DateTime LastRecvPingTime;
    

    public ZmqReqRespClientSocketWrapper(
        SocketInfo socketInfo, 
        ZmqReqRespClient zmqRequestResponseClient)
    {
        m_zmqRequestResponseClient = zmqRequestResponseClient;
        while (!Startup(socketInfo))
        {
            String strMessage = getClass().getName() + " failed to startup...";
            Console.writeLine(strMessage);
            Logger.log(strMessage, false, true);
            try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Logger.log(e);
			}
        }
    }

    private void PingSocket()
    {
        while (true)
        {
            try
            {
                if (RequestMap.size() > 0)
                {
                    SendPingBytes(this);
                }
            }
            catch (Exception ex)
            {
                Logger.log(ex);
            }
            try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				Logger.log(e);
			}
        }
    }

    private boolean Startup(SocketInfo socketInfo)
    {
        try
        {
            LastRecvPingTime = DateTime.now();
            EndPointAddr = socketInfo;
            RequestMap = new ConcurrentHashMap<String, RequestDataMessage>();
            m_zmqReqRespClientAck = new ZmqReqRespClientAck(this);
            
            m_context = ZMQ.zmq_init(1);
            Connect();
            m_socketRecvWorker = new ThreadWorker<ObjectWrapper>(){
            	@Override
            	public void runTask(ObjectWrapper item){
            		DoRecvLoop();
            	}
            };
            m_socketRecvWorker.work();

            m_pingWorker = new ThreadWorker<ObjectWrapper>(){
            	@Override
            	public void runTask(ObjectWrapper item) {
            		PingSocket();
            	}
            };
            m_pingWorker.work();
        }
        catch (Exception ex)
        {
            Logger.log(ex);
            return false;
        }
        return true;
    }

    public void Connect()
    {
        boolean blnConnected;
        synchronized(m_connectLock)
        {
            blnConnected = TryToConnect();
        }
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Logger.log(e);
		}
        if (blnConnected)
        {
            ResendJobs();
        }
    }

    @SuppressWarnings("deprecation")
	public boolean TryToConnect()
    {
        boolean blnException = false;
        ReentrantReadWriteLock rwl = m_rwl;
        WriteLock writeLock = rwl.writeLock();
        writeLock.lock();
        try
        {
            String strMessage;
            m_intContextCounter++;

            if (Socket != null)
            {
                //
                // it is reconnecting
                //
            	ZMQ.zmq_close (Socket);
                //SocketBase.Dispose();
                Thread.sleep(1000);
                strMessage = "RequestResponseClient socket disposed [" + EndPointAddr +
                             "]. Context counter [" + m_intContextCounter + "]/[" +
                             CONTEXT_RESET_COUNTER + "]";
                Logger.log(strMessage);
                strMessage = "RequestResponseClient is reconnecting to [" + EndPointAddr +
                             "]. Context counter [" + m_intContextCounter + "]/[" +
                             CONTEXT_RESET_COUNTER + "]";
                Logger.log(strMessage);

                if (m_intContextCounter > CONTEXT_RESET_COUNTER)
                {
                    ResetContext();
                }
            }
            else
            {
                strMessage = "RequestResponseClient is connecting to [" + EndPointAddr + "]";
                Logger.log(strMessage);
            }
            Socket = ZMQ.zmq_socket (m_context, ZMQ.ZMQ_XREQ);
			ZMQ.zmq_setsockopt(Socket, ZMQ.ZMQ_RCVHWM,
					CommunicationConstants.HWM);
			
            //SocketBase.HWM = CoreConstants.HWM;
			String strAddr = EndPointAddr.GetConnectionUrl();
            Socket.connect(strAddr);

            strMessage = "RequestResponseClient is connected to [" + EndPointAddr + "]";
            Console.writeLine(strMessage);
            Logger.log(strMessage);
            LastRecvPingTime = DateTime.now();
            ConnectionDate = DateTime.now();
        }
        catch (Exception ex)
        {
            try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Logger.log(e);
			}
            try
            {
                //
                // we need to release the synchronized before the finally state
                //
            	writeLock.unlock();
                //rwl.ReleaseWriterLock();
            }
            catch (Exception ex2)
            {
                Logger.log(ex2);
            }
            if (ex.getMessage().contains("Too many open files"))
            {
                ResetContext();
            }
            TryToConnect();
            blnException = true;
            Console.writeLine(ex);
            Logger.log(ex);
        }
        finally
        {
            if (!blnException)
            {
                try
                {
                	writeLock.unlock();
                    //rwl.ReleaseWriterLock();
                }
                catch (Exception ex2)
                {
                    Logger.log(ex2);
                }
            }
        }
        return !blnException;
    }

    private void ResetContext()
    {
        while (!ResetContext0())
        {
            String strMessage = "Faulure to reset context. Trying again ["  + 
                DateTime.now() + "]...";
            Logger.log(strMessage);
            Console.writeLine(strMessage);
            try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Logger.log(e);
			}
        }
    }

    private boolean ResetContext0()
    {
        try
        {
            //
            // try to dispose context
            //
            final Ctx oldContext = m_context;
            ThreadWorker<ObjectWrapper> worker = new ThreadWorker<ObjectWrapper>(){
            	@Override
            	public void runTask(ObjectWrapper item) {
                    try
                    {
                        String strMessage = "Disposing context [" +
                                            EndPointAddr.GetConnectionUrl() + "]...";
                        Console.writeLine(strMessage);
                        Logger.log(strMessage);
                        //oldContext.Dispose();
                        ZMQ.zmq_term (oldContext);
                        strMessage = "Context reset done [" + EndPointAddr.GetConnectionUrl() + "]";
                        Console.writeLine(strMessage);
                        Logger.log(strMessage);
                    }
                    catch (Exception ex)
                    {
                        Logger.log(ex);
                    }
            	}
            };
            worker.work();
            m_context = ZMQ.zmq_init (1);
            m_intContextCounter = 0;
            String strMessage2 = "New context created [" + EndPointAddr.GetConnectionUrl() + "]";
            Console.writeLine(strMessage2);
            Logger.log(strMessage2);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
            return false;
        }
        return true;
    }

    public void ResendJobs()
    {
        try
        {
            Set<Entry<String, RequestDataMessage>> requestsArr = RequestMap.entrySet();
            int intRequestLength = requestsArr.size();
            //
            // avoid spamming the server with continuous resends
            //
            int intRequestLengthHalf = Math.max(1, 
                m_zmqRequestResponseClient.NumConnections/2);
            intRequestLengthHalf = Math.min(intRequestLengthHalf, RequestMap.size());
            if (intRequestLength > 0)
            {
                String strMessage = getClass().getName() + " Request-Response [" + EndPointAddr +
                                    "] is resending [" + intRequestLengthHalf + "] out of [" +
                                    intRequestLength + "] at [" +
                                    DateTime.now() + "]";
                Console.writeLine(strMessage);
                Logger.log(strMessage);
                int i = 0;
                for (Entry<String, RequestDataMessage> kvp : requestsArr)
                {
                	if(i >= intRequestLengthHalf){
                		break;
                	}
                i++;
                    RequestDataMessage currReqMessage = kvp.getValue();
                    if (currReqMessage.GetResponse() == null) // this check avoids race conditions
                    {
                        SendRequest(
                            currReqMessage,
                            kvp.getKey(),
                            RequestMap,
                            this);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public List<RequestDataMessage> SendRequestAndGetResponse(
        RequestDataMessage requestDataMessage,
        String strRequestId)
    {
        while (m_zmqRequestResponseClient.ZmqReqRespClientHeartBeat == null)
        {
            String strMessage = "Heart beat is not ready [" + DateTime.now () + "]";
            Logger.log(strMessage);
            Console.writeLine(strMessage);
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Logger.log(e);
			}
        }
        Task tcs = new Task(new ObjectWrapper());
        requestDataMessage.SetTcs(tcs);

        m_zmqRequestResponseClient.ZmqReqRespClientHeartBeat.StartPing(this);
        SendRequest(requestDataMessage, 
            strRequestId,
            RequestMap,
            this);

        List<RequestDataMessage> response = WaitForResponse(
            requestDataMessage, 
            strRequestId);

        m_zmqReqRespClientAck.SendJobAck(strRequestId);
        return response;
    }

    private void SendRequest(
        RequestDataMessage requestDataMessage,
        String strRequestId,
        ConcurrentHashMap<String, RequestDataMessage> requestMap,
        ZmqReqRespClientSocketWrapper socketWrapper)
    {
    	try{
	    	RequestJob requestJob = new RequestJob();
	    	requestJob.RequestDataMessage = requestDataMessage;
			requestJob.RequestId = strRequestId;
			requestJob.RequestMap = requestMap;
			requestJob.SocketWrapper = socketWrapper;
			requestJob.Rwl = socketWrapper.m_rwl;
	        Task task = m_zmqRequestResponseClient.SendJobEfficientQueue.add(
	            strRequestId,
	            requestJob);
	        task.waitTask();
	        //task.dispose(); todo commented for testing, maybe keep it as it is
	        String strMessage = 
	        		"[" + getClass().getName() + "]. Sent request [" +  
	        				DateTime.now() + "]";
	        Logger.log(strMessage);
			Console.writeLine(strMessage);
		}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    }

    public static void DoSendRequest(
        RequestDataMessage requestDataMessage,
        String strRequestId,
        ConcurrentHashMap<String, RequestDataMessage> requestMap,
        ZmqReqRespClientSocketWrapper zmqReqRespClientSocketWrapper,
        ReadWriteLock rwl)
    {
    	Lock readerLock = rwl.readLock();
        try
        {
        	readerLock.lock();
            byte[] requestBytes = requestDataMessage.getByteArr();
            ISerializerWriter serializer = Serializer.GetWriter();
            serializer.Write(strRequestId);
            serializer.Write(requestBytes);
            byte[] bytes = serializer.GetBytes();
            SendBytes(bytes, zmqReqRespClientSocketWrapper);
            zmqReqRespClientSocketWrapper.LastRecvPingTime = DateTime.now();
            requestMap.put(strRequestId, requestDataMessage);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        finally
        {
            readerLock.unlock();
        }
    }

    private List<RequestDataMessage> WaitForResponse(
        RequestDataMessage requestDataMessage,
        String strRequestId)
    {
        Task task = requestDataMessage.GetTcs();
        task.waitTask();
        //task.dispose(); todo commented for testing, maybe keep it as it is
        List<RequestDataMessage> response;
        if ((response = requestDataMessage.GetResponse()) != null)
        {
            RequestMap.remove(strRequestId);
            return response;
        }
        try {
			throw new HCException("Null result");
		} catch (HCException e) {
			Logger.log(e);
		}
        return null;
    }

    private void DoRecvLoop()
    {
        while (true)
        {
            boolean blnLockAckquired = false;
            ReentrantReadWriteLock rwl = m_rwl;
            ReadLock readerLock = rwl.readLock();
            try
            {
                int intMapCount = RequestMap.size();
                if (intMapCount > 0 || IsBaseSocket)
                {
                	readerLock.lock();
                    blnLockAckquired = true;
                    BooleanWrapper blnMoreMessages = new BooleanWrapper();
                    
                    byte[] bytes = RecvMultiPart(
                        this,
                        Socket,
                        m_zmqRequestResponseClient.ZmqReqRespClientHeartBeat,
                        blnMoreMessages);

                    if (bytes != null)
                    {
                        if (!(bytes.length == 1 &&
                              bytes[0] == 1) &&
                            bytes.length > 0)
                        {
                            ISerializerReader serializerReader = Serializer.GetReader(bytes);
                            String strRequestid = serializerReader.ReadString();
                            RequestDataMessage response = new RequestDataMessage(); 
                            		Serializer.deserialize(
                            				serializerReader.ReadByteArray(), 
                            				response);
                            		ArrayList<RequestDataMessage> responseList = new ArrayList<RequestDataMessage>();
                            		responseList.add(response);
                            
                            boolean blnRecvMore = ZMQ.zmq_getsockopt (Socket, ZMQ.ZMQ_RCVMORE) == 1L;
                            boolean blnFailure = false;
                            while (blnRecvMore)
                            {
                                bytes = RecvMultiPart(
                                    this,
                                    Socket,
                                    m_zmqRequestResponseClient.ZmqReqRespClientHeartBeat,
                                    blnMoreMessages);

                                if (bytes != null)
                                {
                                	blnRecvMore = ZMQ.zmq_getsockopt (Socket, ZMQ.ZMQ_RCVMORE) == 1L;
                                    //blnRecvMore = SocketBase.RcvMore;
                                    if (!(bytes.length == 1 &&
                                          bytes[0] == 1) &&
                                        bytes.length > 0)
                                    {
                                        serializerReader = Serializer.GetReader(bytes);
                                        serializerReader.ReadString();
                                        response = new RequestDataMessage(); 
                                        Serializer.deserialize(
                                        		serializerReader.ReadByteArray(), 
                                        		response);
                                        responseList.add(response);
                                    }
                                }
                                else
                                {
                                    String strMessage =
                                        "****Exception. ReqResp client failed to recieve mesage. Request [" +
                                        strRequestid + "]";
                                    Console.writeLine(strMessage);
                                    Logger.log(strMessage);
                                    blnFailure = true;
                                }
                            }

                            RequestDataMessage request = RequestMap.get(strRequestid);
                            if (request != null)
                            {
                                if (blnFailure)
                                {
                                    responseList.get(0).Error =
                                        EnumReqRespErrors.IncompleteMessageReceived.toString();
                                }
                                request.SetResponse(
                                		new ArrayList<RequestDataMessage>(responseList));
                                Task task = request.GetTcs();
                                try
                                {
                                    task.setTaskDone();
                                }
                                catch (Exception ex)
                                {
                                    //
                                    // has the task already been completed?
                                    //
                                    Logger.log(ex);
                                }
                            }
                        }
                    }
                }
                //
                // avoid killing the cpu with too many socket calls
                //
                Thread.sleep(10);
            }
            catch (Exception ex)
            {
                Logger.log(ex);
                //
                // slow down
                //
                try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					Logger.log(e);
				}
            }
            finally
            {
                if (blnLockAckquired)
                {
                    readerLock.unlock();
                }
            }
        }
    }

    private static byte[] RecvMultiPart(
        ZmqReqRespClientSocketWrapper zmqReqRespClientSocketWrapper,
        SocketBase socket,
        ZmqReqRespClientHeartBeat zmqReqRespClientHeartBeat,
        BooleanWrapper blnMoreMessages)
    {
        blnMoreMessages.Value = false;
        try
        {
            byte[] bytes = null;
            synchronized (zmqReqRespClientSocketWrapper.m_sendRcvLock)
            {
            	Msg msg = ZMQ.zmq_recv (socket, ZMQ.ZMQ_DONTWAIT);
            	if(msg != null){
            		bytes = msg.data();
            	}
            }
            if (bytes == null)
            {
                return null;
            }

            if (zmqReqRespClientHeartBeat == null)
            {
                String strMessage = "zmqReqRespClientHeartBeat is not ready [" +
                                    DateTime.now() + "]";
                Console.writeLine(strMessage);
                Logger.log(strMessage);
                Thread.sleep(2000);
                return null;
            }
            //
            // ping message
            //
            zmqReqRespClientHeartBeat.Ping(zmqReqRespClientSocketWrapper);
            if ((bytes.length == 1 &&
                 bytes[0] == 1))
            {
                return null;
            }

            ISerializerReader serializerReader = Serializer.GetReader(bytes);
            boolean blnFoundFirstMessage = serializerReader.ReadBoolean();
            bytes = serializerReader.ReadByteArray();
            boolean blnFoundLastMesssage = serializerReader.ReadBoolean();
            blnMoreMessages.Value = serializerReader.ReadBoolean();
            
            boolean blnRecvMore = ZMQ.zmq_getsockopt (socket, ZMQ.ZMQ_RCVMORE) == 1L;

            boolean blnIsMultiMsg = blnRecvMore &&
                                 blnFoundFirstMessage &&
                                 !blnFoundLastMesssage;
            boolean blnIsValidMessage = blnFoundFirstMessage;

            ByteArrayOutputStream byteArrList = null;
            if (blnIsMultiMsg)
            {
                String strMessage = "Debug => " + ZmqReqRespClientSocketWrapper.class.getName() +
                                    " is trying to receive a very large message...";
                Console.writeLine(strMessage);
                Logger.log(strMessage);
                byteArrList = new ByteArrayOutputStream();
                byteArrList.write(bytes);
            }
            else if (blnFoundFirstMessage && blnFoundLastMesssage)
            {
                return bytes;
            }
            
            blnRecvMore = ZMQ.zmq_getsockopt (socket, ZMQ.ZMQ_RCVMORE) == 1L;
            while (!blnFoundLastMesssage &&
            		blnRecvMore &&
                   byteArrList != null) // keep receiving even if it is not a valid message
            {
                byte[] currBytes = null;
                synchronized (zmqReqRespClientSocketWrapper.m_sendRcvLock)
                {
                	Msg msg = ZMQ.zmq_recv (socket, ZMQ.ZMQ_DONTWAIT);
                	
                	if(msg != null){
                		currBytes = msg.data();
                	}
                }
                if (currBytes != null)
                {
                    zmqReqRespClientHeartBeat.Ping(zmqReqRespClientSocketWrapper);
                    serializerReader = Serializer.GetReader(currBytes);
                    serializerReader.ReadBoolean(); // dummy first message flag
                    byteArrList.write(serializerReader.ReadByteArray());
                    blnFoundLastMesssage = serializerReader.ReadBoolean();
                    if (blnFoundLastMesssage)
                    {
                        blnMoreMessages.Value = serializerReader.ReadBoolean();
                        break;
                    }
                }
            }

            if (blnIsMultiMsg &&
                (!blnFoundLastMesssage))
            {
                blnIsValidMessage = false;
            }

            if (byteArrList != null &&
                byteArrList.size() > 0 &&
                blnIsValidMessage)
            {
            	byteArrList.flush();
                bytes = byteArrList.toByteArray();
            }

            if (blnIsValidMessage)
            {
                zmqReqRespClientHeartBeat.Ping(zmqReqRespClientSocketWrapper);

                if(blnIsMultiMsg)
                {
                    double dblMb = Precision.round(((bytes.length / 1024f) / 1024f), 2);
                    String strMessage = "Debug => " + 
                    ZmqReqRespClientSocketWrapper.class.getName() +
                                        " received very large message [" + dblMb + "] mb";
                    Console.writeLine(strMessage);
                    Logger.log(strMessage);
                }
                return bytes;
            }
            return null;
        }
        catch (Exception ex)
        {
        	Logger.log(ex);
        }
        return null;
    }

    public static void SendPingBytes(
        ZmqReqRespClientSocketWrapper socket)
    {
        ReadWriteLock rwl = socket.m_rwl;
        Lock readerLock = rwl.readLock();
        try
        {
        	readerLock.lock();
            byte[] bytes = new byte[] {1};
            SendBytes(bytes, socket);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        finally
        {
        	readerLock.unlock();
        }
    }

    public static void SendBytes(
        byte[] bytes,
        ZmqReqRespClientSocketWrapper socket)
    {
        if (bytes.length <= Serializer.BYTES_LIMIT)
        {
            while(!SendBytes(bytes, false, socket))
            {
                socket.Connect();
            }
        }
        else
        {
            List<byte[]> byteList = Serializer.GetByteArrList(bytes);
            int intListSize = byteList.size();

            boolean blnSentAll = false;
            while (!blnSentAll)
            {
                blnSentAll = true;
                for (int i = 0; i < intListSize; i++)
                {
                    ISerializerWriter serializerWriter = Serializer.GetWriter();
                    serializerWriter.Write(i == 0); // check if it is the first message
                    serializerWriter.Write(byteList.get(i));
                    serializerWriter.Write(i == intListSize - 1); // check if it is the last message

                    if (!SendBytes(
                        serializerWriter.GetBytes(),
                        i < intListSize - 1,
                        socket))
                    {
                        blnSentAll = false;
                        break;
                    }
                }
                if (!blnSentAll)
                {
                    socket.Connect();
                }
            }
            double dblMb = Precision.round(((bytes.length / 1024f) / 1024f), 2);
            String strMessage = "Debug => " + ZmqReqRespClientSocketWrapper.class.getName() +
                                " sent very large message [" + dblMb + "] mb";
            Console.writeLine(strMessage);
            Logger.log(strMessage);
        }
    }

    private static boolean SendBytes(
        byte[] bytes,
        boolean blnSendMore,
        ZmqReqRespClientSocketWrapper socket)
    {
        boolean blnSuccess = false;
        int intCounter = 0;
        while (!blnSuccess)
        {
            boolean status = false;

            try
            {
                status = SendBytes0(
                    bytes, 
                    blnSendMore, 
                    socket.Socket,
                    socket.m_sendRcvLock);
                blnSuccess = status;
                if (!blnSuccess)
                {
                    String strMessage = ZmqReqRespClientSocketWrapper.class.getName() + " could not send message [" +
                                        status + "][" +
                                        intCounter + "]. Resending...";
                    Logger.log(strMessage);
                    Console.writeLine(strMessage);
                    Thread.sleep(5000);
                }
            }
            catch (Exception ex)
            {
                String strMessage = ZmqReqRespClientSocketWrapper.class.getName() + " could not send message [" +
                                    status + "][" +
                                    intCounter + "]. Resending...";
                Logger.log(strMessage);
                Console.writeLine(strMessage);
                Logger.log(ex);
                try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					Logger.log(e);
				}
            }
            intCounter++;

            if (intCounter > 10)
            {
                return false;
            }
        }
        return true;
    }

    public static boolean SendBytes0(
        byte[] bytes, 
        boolean blnSendMore,
        SocketBase socket,
        Object sendLock)
    {
        try
        {
            boolean status;
            synchronized (sendLock)
            {
                if (blnSendMore)
                {
                    status = socket.send(
                    		new Msg(bytes), ZMQ.ZMQ_MORE);
                }
                else
                {
                    status = socket.send(new Msg(bytes), ZMQ.ZMQ_DONTWAIT);
                }
            }
            return status;
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return false;
    }

    public void Dispose()
    {
    	ZMQ.zmq_close (Socket);
    }
}
