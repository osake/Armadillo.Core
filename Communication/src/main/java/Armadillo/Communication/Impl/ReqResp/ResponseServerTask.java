package Armadillo.Communication.Impl.ReqResp;

import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Communication.zmq.zmq.SocketBase;
import Armadillo.Core.Concurrent.ProducerConsumerQueue;

public class ResponseServerTask {

    public byte[] WhoIs;
    public RequestDataMessage Request;
    public boolean BlnWait;
    public SocketBase Socket;
    public Object SocketLock;
    public WhoIsWrapper WhoIsWrapper;
    private String m_strRequestId;
    private final ZmqReqRespServerAck m_zmqReqRespServerAck;
	private boolean m_blnIsDisposed;
    private static final ProducerConsumerQueue<ResponseServerTaskWrapper> m_queue;

    static 
    {
        m_queue = new ProducerConsumerQueue<ResponseServerTaskWrapper>(50){
        	@Override
        	public void runTask(ResponseServerTaskWrapper reqServerTask)
        	{
        		reqServerTask.ResponseServerTask.GetResponse();
        	}
        };
        //m_queue.SetAutoDisposeTasks(true);
        //m_queue.OnWork += reqServerTask => reqServerTask.ResponseServerTask.GetResponse();
    }

    public ResponseServerTask(
        String strRequestId,
        RequestDataMessage request,
        SocketBase socket,
        Object socketLock,
        byte[] whoIs,
        ZmqReqRespServerAck zmqReqRespServerAck)
    {
        m_strRequestId = strRequestId;
        Request = request;
        Socket = socket;
        SocketLock = socketLock;
        m_zmqReqRespServerAck = zmqReqRespServerAck;
        WhoIs = whoIs;
        BlnWait = true;
        WhoIsWrapper = Armadillo.Communication.Impl.ReqResp.WhoIsWrapper.GetWhoIsWrapper(whoIs);
    }

    public void Run()
    {
        if (!IsClientConnected())
        {
            FinalizeTask(true);
            return;
        }
        m_queue.add(new ResponseServerTaskWrapper(this));
    }

    private void FinalizeTask(boolean blnIsClientDisconnected)
    {
    	try{
    		if(m_blnIsDisposed){
    			return;
    		}
    		
	        synchronized (ZmqReqRespServer.m_RequestResponseLock)
	        {
	        	if(ZmqReqRespServer.MapRequestToTask.containsKey(m_strRequestId)){
	        		ResponseServerTask responseServerTask = ZmqReqRespServer.MapRequestToTask.remove(m_strRequestId);
		            if(responseServerTask != null)
		            {
		                responseServerTask.Dispose();
		            }
	        	}
	        }
	        
	        RequestDataMessage request = Request; 
	
	        if (blnIsClientDisconnected &&
	        		!m_blnIsDisposed &&
	        		request != null)
	        {
	            //
	            // send faulty response
	            //
	            String strMessage = "Sending faulty response due to client disconnected. " +
	                                request.Id;
	            Console.writeLine(strMessage);
	            Logger.log(strMessage);
	
	            RequestDataMessage response = (RequestDataMessage) request.copy();
	            response.Error = EnumReqRespErrors.ClientDisconnected.toString();
	            ZmqReqRespServer.SendResponse(
	                Socket,
	                m_strRequestId,
	                response,
	                SocketLock,
	                WhoIs);
	        }
	        BlnWait = false;
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    }

    private void GetResponse()
    {
        if (!IsClientConnected())
        {
            FinalizeTask(true);
            return;
        }

        RequestDataMessage response =
            ReqRespService.InvokeRequestEventHandler(Request);
        boolean blnIsClientDisconnected;
        if (!response.GetIsClientDisconnected())
        {
            blnIsClientDisconnected = false;
            m_zmqReqRespServerAck.RequestAck(
                Socket,
                m_strRequestId,
                response,
                SocketLock,
                WhoIs);
        }
        else
        {
            blnIsClientDisconnected = true;
        }
        FinalizeTask(blnIsClientDisconnected);
    }

    public boolean IsClientConnected()
    {
        while (ReqRespServer.ReqRespServerHeartBeat == null)
        {
            try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				Logger.log(ex);
			}
        }
        RequestDataMessage request = Request;
        if(Request == null ||
        		request == null){
        	return true;
        }
        String strRequestorName = Request.RequestorName;
        if (!ReqRespServer.ReqRespServerHeartBeat.IsClientConnected(strRequestorName))
        {
            String strMessage = "Client [" + strRequestorName + "] is disconnected. Request [" +
                                m_strRequestId +
                                " ] is not loaded.";
            Logger.log(strMessage);
            Console.writeLine(strMessage);
            FinalizeTask(true);
            return false;
        }
        return true;
    }

    public void Dispose()
    {
    	if(m_blnIsDisposed){
    		return;
    	}
    	m_blnIsDisposed = true;
        WhoIs = null;
        Request = null;
        m_strRequestId = null;
        Socket = null;
        SocketLock = null;
        WhoIsWrapper = null;
    }
}
