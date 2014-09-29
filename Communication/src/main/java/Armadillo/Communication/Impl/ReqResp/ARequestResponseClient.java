package Armadillo.Communication.Impl.ReqResp;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;

import Armadillo.Core.Config;
import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.Concurrent.LockHelper;
import Armadillo.Communication.Impl.SimpleUiSocket;

public abstract class ARequestResponseClient {
	
    public String ServerName;
    public int Port;


    private static final ConcurrentHashMap<String, ARequestResponseClient> m_ownInstances = 
        new ConcurrentHashMap<String, ARequestResponseClient>();
    protected final Object m_lockObject = new Object();
    protected boolean m_blnIsLocalConnection;
    private static String m_strDefaultServer;
    private static int m_intDefaultPort;

    static 
    {
    	try
    	{
	        m_strDefaultServer = Config.getStringStatic(
	        		"TopicServerName",
	        		SimpleUiSocket.class);
	        m_intDefaultPort = Integer.parseInt(Config.getStringStatic(
	        		"ReqRespPort",
	        	     SimpleUiSocket.class));
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    		m_strDefaultServer = "";
    		m_intDefaultPort = -1;
    	}
    }

    protected ARequestResponseClient(
        String strServerName,
        int intPort)
    {
    	try
    	{
	        m_blnIsLocalConnection = strServerName.equals("local");
	        ServerName = strServerName;
	        Port = intPort;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    public static void Connect(
        String strServerName,
        int intPort,
        int intConnections)
    {
        try
        {
            String strInstanceName = GetInstanceName(
                strServerName,
                intPort);
            if (m_ownInstances.containsKey(strInstanceName))
            {
                return;
            }
            synchronized (LockHelper.GetLockObject(strInstanceName))
            {
                if (m_ownInstances.containsKey(strInstanceName))
                {
                    return;
                }
                ARequestResponseClient ownInstance;
                ownInstance = new ZmqRequestResponseClient(strServerName, intPort);
                ownInstance.DoConnect(strServerName, intPort, intConnections);
                m_ownInstances.put(strInstanceName, ownInstance);
                LoggerPublisher.ConnectPublisher(strServerName);
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public static ARequestResponseClient GetDefaultConnection()
    {
        ARequestResponseClient defaultConnection = null;

        while (defaultConnection == null)
        {
            defaultConnection = GetConnection(
                 m_strDefaultServer,
                 m_intDefaultPort);
            if (defaultConnection == null)
            {
                String strMessage = "defaultConnection is not ready [" +
                    DateTime.now() + "]";
                Logger.log(strMessage);
                Console.writeLine(strMessage);
                try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Logger.log(e);
				}
            }
        }
        return defaultConnection;
    }

    public static ARequestResponseClient GetConnection(
        String strServerName,
        int intPort)
    {
        String strInstanceName = GetInstanceName(strServerName,
                                                 intPort);
        ARequestResponseClient currInstance;
        currInstance = m_ownInstances.get(strInstanceName);
        return currInstance;
    }

     private static String GetInstanceName(
        String strServerName,
        int intPort)
    {
        return strServerName + "_" + intPort;
    }

    public abstract void Dispose();
     
    public abstract List<Object> SendRequestAndGetResponse(
        RequestDataMessage requestDataMessage,
        int intTimeOutSeconds);
    
    public abstract void DoConnect(
        String serverName,
        int intPort,
        int intConnections);

 
}
