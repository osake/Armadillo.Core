package Armadillo.Communication.Impl.Topic;

import java.util.ArrayList;

import Armadillo.Core.Console;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.NetworkHelper;
import Armadillo.Communication.Impl.CommunicationConstants;
import Armadillo.Communication.zmq.zmq.Ctx;
import Armadillo.Communication.zmq.zmq.Msg;
import Armadillo.Communication.zmq.zmq.SocketBase;
import Armadillo.Communication.zmq.zmq.ZMQ;
import Armadillo.Core.Serialization.ISerializerWriter;
import Armadillo.Core.Serialization.Serializer;

public class ZmqTopicPublisherConnection implements ITopicPublishing{

    private Ctx m_context;
    private SocketBase m_delerSocket;
    private Object m_sendLock = new Object();
    private String m_strserverName;
    private int m_intPort;

    public ZmqTopicPublisherConnection(
        String strServerName,
        int intPort)
    {
        m_strserverName = strServerName;
        m_intPort = intPort;
        //
        // connect to dealer
        //
        DoConnection(strServerName, intPort);
    }


    private void DoConnection(
        String strServerName,
        int intPort)
    {
        while (!DoConnection0(
            strServerName,
            intPort))
        {
            DisposeConnection();
            try {
				Thread.sleep(5000);
			} catch (InterruptedException ex) {
				Logger.log(ex);
			}
            DoConnection(strServerName, intPort);
        }
    }

    @SuppressWarnings("deprecation")
	private boolean DoConnection0(
        String strServerName,
        int intPort)
    {
        try
        {
            synchronized (m_sendLock)
            {
                Ctx context = m_context;
                if (context == null)
                {
                    context = ZMQ.zmq_init (1);
                }
                m_delerSocket = ZMQ.zmq_socket (context, ZMQ.ZMQ_XREQ);
                
                String strIp = NetworkHelper.GetIpAddr(strServerName);
                String strConnection = "tcp://" + strIp + ":" + intPort;
                //m_delerSocket.HWM = CoreConstants.HWM;
                ZMQ.zmq_setsockopt (m_delerSocket, ZMQ.ZMQ_RCVHWM, CommunicationConstants.HWM);
                if(!ZMQ.zmq_connect (m_delerSocket, strConnection))
                {
                	throw new HCException("Could not connect");
                }
                
                m_context = context;
            }
        }
        catch(Exception ex)
        {
            Logger.log(ex);
            return false;
        }
        return true;
    }

    private void DisposeConnection()
    {
        try
        {
            if (m_delerSocket != null)
            {
                ZMQ.zmq_close (m_delerSocket);
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public void Publish(TopicMessage topicMessage)
    {
        String strTopic = topicMessage.TopicName;
        SendBytes(strTopic, topicMessage.GetByteArr());
    }

    private void SendBytes(String strTopic, byte[] bytes)
    {
        if (bytes.length <= 1e20)
        {
            SendBytes(bytes, false, true);
        }
        else
        {
            ArrayList<byte[]> byteList = Serializer.GetByteArrList(bytes);
            int intListSize = byteList.size();

            synchronized (m_sendLock) // send all together
            {
                for (int i = 0; i < intListSize; i++)
                {
                    ISerializerWriter serializerWriter = Serializer.GetWriter();
                    serializerWriter.Write(strTopic);
                    serializerWriter.Write(i == 0); // check if it is the first message
                    serializerWriter.Write(byteList.get(i));
                    boolean blnLastMessage = i == intListSize - 1;
                    serializerWriter.Write(blnLastMessage); // check if it is the last message

                    SendBytes(
                        serializerWriter.GetBytes(),
                        i < intListSize - 1,
                        false);
                }
                double dblMb = ((bytes.length / 1024f) / 1024f);
                String strMessage = "Debug => " + ZmqTopicPublisherConnection.class.getName() + " sent very large message [" +
                byteList.size() + "]. [" + dblMb + "] mb";
                Console.writeLine(strMessage);
                Logger.log(strMessage);

            }
        }
    }

    private void SendBytes(
        byte[] bytes, 
        boolean blnSendMore,
        boolean blnPublishLocked)
    {
        boolean blnSuccess = false;
        int intCounter = 0;
        while (!blnSuccess)
        {
            boolean status = false;

            try
            {
                if (blnPublishLocked)
                {
                    status = SendBytesLocked(bytes, blnSendMore);
                }
                else
                {
                    status = SendBytesUnlocked(bytes, blnSendMore);
                }
                blnSuccess = status;
                if (!blnSuccess)
                {
                    String strMessage = ZmqTopicPublisherConnection.class.getName() + " could not send message [" +
                                        status + "][" +
                                        intCounter + "]. Resending...";
                    Logger.log(strMessage);
                    Console.writeLine(strMessage);
                    Thread.sleep(5000);
                }
            }
            catch (Exception ex)
            {
                String strMessage = ZmqTopicPublisherConnection.class.getName() + " could not send message [" +
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

            if (intCounter > 10)
            {
                intCounter = 0;
                Reconnect();
            }
        }
    }

    private Boolean SendBytesLocked(
        byte[] bytes, 
        boolean blnSendMore)
    {
        Boolean status;
        synchronized (m_sendLock)
        {
            status = SendBytesUnlocked(bytes, blnSendMore);
        }
        return status;
    }

    private Boolean SendBytesUnlocked(byte[] bytes, boolean blnSendMore)
    {
        Boolean status;
        if (blnSendMore)
        {
        	
            status = m_delerSocket.send(new Msg(bytes), ZMQ.ZMQ_MORE);
        }
        else
        {
            status = m_delerSocket.send(new Msg(bytes), 0);
        }
        return status;
    }

    public void Reconnect()
    {
        synchronized (m_sendLock)
        {
        	ZMQ.zmq_close (m_delerSocket);
        }   
        //
        // note, do not put it inside the synchronized block, it will get deadlocked!
        //
        DoConnection(m_strserverName,
            m_intPort);
        String strMessage = ZmqTopicPublisherConnection.class.getName() + " has been reconnected";
        Console.writeLine(strMessage);
        Logger.log(strMessage);
    }

}
