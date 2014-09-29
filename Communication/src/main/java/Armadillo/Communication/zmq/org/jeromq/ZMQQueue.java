package Armadillo.Communication.zmq.org.jeromq;

import Armadillo.Communication.zmq.org.jeromq.ZMQ.Context;
import Armadillo.Communication.zmq.org.jeromq.ZMQ.Socket;

/**
 * @deprecated use org.zeromq namespace
 */
public class ZMQQueue implements Runnable {

    private final ZMQ.Socket inSocket;
    private final ZMQ.Socket outSocket;

    /**
     * Class constructor.
     * 
     * @param context
     *            a 0MQ context previously created.
     * @param inSocket
     *            input socket
     * @param outSocket
     *            output socket
     */
    public ZMQQueue(Context context, Socket inSocket, Socket outSocket) {
        this.inSocket = inSocket;
        this.outSocket = outSocket;
    }

    @Override
    public void run() 
    {
       Armadillo.Communication.zmq.zmq.ZMQ.zmq_proxy (inSocket.base(), outSocket.base(), null);
    }

}
