package Armadillo.Communication.Impl.ReqResp;

import java.util.concurrent.ConcurrentHashMap;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import Armadillo.Core.Clock;
import Armadillo.Core.Logger;
import Armadillo.Communication.zmq.zmq.Msg;
import Armadillo.Communication.zmq.zmq.SocketBase;
import Armadillo.Communication.zmq.zmq.ZMQ;

public class WhoIsWrapper {
	
    private static final int WHO_IS_PING_TIME_SECONDS = 5;
    private final Object m_lockObj = new Object();
    private static final ConcurrentHashMap<String, WhoIsWrapper> m_whoIsMap =
        new ConcurrentHashMap<String, WhoIsWrapper>();
    private static final Object m_whoIsLock =
        new Object();
    private DateTime m_lastUpdateTime = DateTime.now();

    public static WhoIsWrapper GetWhoIsWrapper(
        byte[] whoIs)
    {
    	StringBuilder sb = new StringBuilder();
        for (int i = 0; i < whoIs.length; i++)
        {
            sb.append(whoIs[i]);
        }
        String strKey = sb.toString();
        WhoIsWrapper whoIsWrapper = null;
        if (!m_whoIsMap.containsKey(strKey))
        {
            synchronized (m_whoIsLock)
            {
                if (!m_whoIsMap.containsKey(strKey))
                {
                    whoIsWrapper = new WhoIsWrapper();
                    m_whoIsMap.put(strKey, whoIsWrapper);
                }
                else{
                	whoIsWrapper = m_whoIsMap.get(strKey);
                }
            }
        }
        else{
        	whoIsWrapper = m_whoIsMap.get(strKey);
        }
        return whoIsWrapper;
    }

    public void PingWhoIs(
        SocketBase socket,
        Object socketLock,
        byte[] whoIs)
    {
        DateTime now = Clock.LastTime;
        if (Seconds.secondsBetween(m_lastUpdateTime, now).getSeconds() > WHO_IS_PING_TIME_SECONDS)
        {
            synchronized (m_lockObj)
            {
                if ((Seconds.secondsBetween(m_lastUpdateTime, now).getSeconds()) > WHO_IS_PING_TIME_SECONDS)
                {
                    synchronized (socketLock)
                    {
                        try
                        {
                            m_lastUpdateTime = DateTime.now();
                            //SendPingMsg(socket, whoIs); // ping should be done only by the base socket
                        }
                        catch (Exception ex)
                        {
                            Logger.log(ex);
                        }
                    }
                }
            }
        }
    }

    public static void SendPingMsg(
        SocketBase socket, 
        byte[] whoIs,
        Object socketLock)
    {
        synchronized (socketLock)
        {
            socket.send(new Msg(whoIs), ZMQ.ZMQ_SNDMORE);
            socket.send(new Msg(new byte[] {1}), 0);
        }
    }
}
