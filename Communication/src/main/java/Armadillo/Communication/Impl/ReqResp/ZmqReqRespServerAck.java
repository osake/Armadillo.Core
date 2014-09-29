package Armadillo.Communication.Impl.ReqResp;

import Armadillo.Communication.zmq.zmq.SocketBase;

public class ZmqReqRespServerAck {

    //private static final int WAIT_MILLS = 10;
    //private static final int TOPIC_CONFIRM_MILLS = 5000;

    public ZmqReqRespServerAck(String strServerName)
    {
    }

    public void RequestAck(
        SocketBase socket,
        String strRequestId,
        RequestDataMessage response,
        Object socketLock,
        byte[] bytesWhoIs)
    {
        ZmqReqRespServer.SendResponse(
            socket,
            strRequestId,
            response,
            socketLock,
            bytesWhoIs);
    }
}
