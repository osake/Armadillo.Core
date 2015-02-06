package Armadillo.Communication.Impl.ReqResp;

import Armadillo.Core.Config;
import Armadillo.Core.Logger;
import Armadillo.Communication.Impl.SimpleUiSocket;

public abstract class ReqRespServer {
	
    public static ReqRespServer OwnInstance;
    public static boolean IsInitialized;
    public static ReqRespServerHeartBeat ReqRespServerHeartBeat;
    public String ServerName;
    public int Port;

    private static final Object m_lockObj = new Object();

    public static void StartService(
        String strServerName,
        int intPort,
        int intConnections)
    {
        if (!IsInitialized)
        {
            synchronized (m_lockObj)
            {
                if (!IsInitialized)
                {
                        OwnInstance = new ZmqReqRespServer();

                    OwnInstance.Connect(strServerName,
                        intPort,
                        intConnections);
                    ReqRespServerHeartBeat = new ReqRespServerHeartBeat(
                        Config.getStringStatic(
                        		"TopicServerName",
                        		SimpleUiSocket.class),
                        intPort,
                        true);
                    OwnInstance.ServerName = strServerName;
                    OwnInstance.Port = intPort;
                    IsInitialized = true;
                    Logger.log(ReqRespServer.class.getName() +
                        " initialized for DNS [" + strServerName + "]");
                }
            }
        }
    }

    protected abstract void Connect(String strServerName,
        int intPort,
        int intConnections);

	public static void loadProviderService() 
	{
		try
		{
			final int intReqRespPort = Integer.parseInt(Config.getStringStatic(
					"ReqRespPort",
					SimpleUiSocket.class));
			String strServerName = Config.getStringStatic(
					"TopicServerName",
					SimpleUiSocket.class);
			ReqRespServer.StartService(strServerName, intReqRespPort, 5);
			ReqRespService.Connect();
			ZmqReqRespClient.Connect(strServerName, intReqRespPort, 5);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
}
