package Armadillo.Communication.Impl.ReqResp;

import Armadillo.Core.ObjectWrapper;
import Armadillo.Core.Concurrent.ThreadWorker;

public class ZmqReqRespServerThreadWorker {
	
    private final ThreadWorker<ObjectWrapper> m_threadWorker;

    public ZmqReqRespServerThreadWorker(
        final String strServerName,
        final int intPortName,
        final int intPort,
        final ZmqReqRespServerAck zmqReqRespServerAck)
    {
        m_threadWorker = new ThreadWorker<ObjectWrapper>(){
        	
        	@Override
        	public void runTask(ObjectWrapper obj){
        		ZmqReqRespServer.DoConnect(
        	            strServerName,
        	            intPortName + intPort,
        	            zmqReqRespServerAck);
        	}
        };
        
        m_threadWorker.work();
    }
}
