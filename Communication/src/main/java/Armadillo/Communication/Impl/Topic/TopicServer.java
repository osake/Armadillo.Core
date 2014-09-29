package Armadillo.Communication.Impl.Topic;

import Armadillo.Core.Config;
import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Communication.Impl.SimpleUiSocket;

public class TopicServer {

	private static TopicServer m_ownInstance;

	private static final Object m_lockObject = new Object();
	private static final Object m_lockObject2 = new Object();
	public static boolean IsInitialized;

	private TopicServer() 
	{
		try 
		{
			ZmqTopicService.Connect();
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}

	public static void Connect(String strServerName) 
	{
		if (m_ownInstance == null) 
		{
			synchronized (m_lockObject) 
			{
				if (m_ownInstance == null) 
				{
					m_ownInstance = new TopicServer();
					Logger.log("Topic server initialized for DNS = "
							+ Config.strDnsName);
					TopicServerHeartBeat.StartHeartBeat(strServerName);
				}
			}
		}
	}

	public static void StartTopicService() 
	{
		if (!IsInitialized) 
		{
			synchronized (m_lockObject2) 
			{
				if (!IsInitialized) 
				{
					IsInitialized = true;
					while (true) 
					{
						LoadTopicService();
						//
						// topic service died, try again
						//
						m_ownInstance = null;
						try 
						{
							Thread.sleep(10000);
						} 
						catch (InterruptedException ex) 
						{
							Logger.log(ex);
						}
					}
				}
			}
		}
	}

	private static void LoadTopicService() 
	{
		try 
		{
			// AssemblyCache.Initialize();
			String strServerName = Config.getStringStatic(
					"TopicServerName",
					SimpleUiSocket.class);
			//String strServerName = "localhost"; // the network does no like localhost
			String strMessage = "Starting topic service in [" + strServerName
					+ "]...";
			Console.writeLine(strMessage);
			Logger.log(strMessage);
			Connect(strServerName);
			strMessage = "Topic service started in [" + strServerName + "]";
			Console.writeLine(strMessage);
			Logger.log(strMessage);
			//
			// keep thread working, unless there is an exception
			//
			WaitForever();
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}

	private static void WaitForever() {
		ThreadWorker<ObjectWrapper> threadWorker = new ThreadWorker<ObjectWrapper>() {
			@Override
			public void runTask(ObjectWrapper obj) {
				OnThreadExecute();
			}
		};
		// threadWorker.WaitForExit = true;
		// threadWorker.OnExecute += OnThreadExecute;
		try {
			threadWorker.work().waitTask();
		} catch (Exception ex) {
			Logger.log(ex);
		}
	}

	private static void OnThreadExecute() {
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				Logger.log(ex);
			}
		}
	}

	public static void laodTopicServer() 
	{
		try
		{
			new ThreadWorker<ObjectWrapper>()
			{
				@Override
				public void runTask(ObjectWrapper objectWrapper)
				{
					TopicServer.StartTopicService();				
				}
			}.work();
			
			
			while(!TopicServer.IsInitialized)
			{
				try 
				{
					Thread.sleep(100);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
			String strMessage = "Topic server initialized";
			Console.writeLine(strMessage);
			Logger.log(strMessage);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
}
