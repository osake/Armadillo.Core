package Armadillo.Communication.Impl.ReqResp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import Armadillo.Communication.zmq.zmq.SocketBase;

public class RequestJob {
    public RequestDataMessage RequestDataMessage;

    public String RequestId;

    public Object SocketLock;

    public ConcurrentHashMap<String, RequestDataMessage> RequestMap;

    public SocketBase Socket;

    public ZmqReqRespClientSocketWrapper SocketWrapper;

    public byte[] WhoIs;

    public ReadWriteLock Rwl;
}
