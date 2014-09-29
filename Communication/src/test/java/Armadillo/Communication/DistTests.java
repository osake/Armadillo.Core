package Armadillo.Communication;

import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;

import Armadillo.Core.Clock;
import Armadillo.Core.Config;
import Armadillo.Core.Console;
import Armadillo.Core.Foo;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Communication.Impl.SimpleUiSocket;
import Armadillo.Communication.Impl.Distributed.DefaultDistributedController;
import Armadillo.Communication.Impl.Distributed.DistWorker;
import Armadillo.Communication.Impl.ReqResp.ReqRespServer;
import Armadillo.Communication.Impl.ReqResp.ZmqRequestResponseClient;
import Armadillo.Communication.Impl.Topic.TopicServer;
import Armadillo.Core.Concurrent.ProducerConsumerQueue;

public class DistTests 
{
	private static int m_intRequestCounter = 0;
	
	public static void main(String[] args)
	{
		try
		{
			ProducerConsumerQueue<ObjectWrapper> queue = 
					new ProducerConsumerQueue<ObjectWrapper>(10)
					{
						@Override
						public void runTask(ObjectWrapper objectToTake)
						{
							new DistTests().DoTest();			
						}
					};
					
			while(true)
			{
				while(queue.getSize() > 20)
				{
					Thread.sleep(100);
				}
				queue.add(null);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	@Test
	public void DoTest()
	{
		try
		{
			loadInfrastructure();
			
	        for (int i = 0; i < 5; i++) 
	        {
	        	sendJob();
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	private void loadInfrastructure() 
	{
		try
		{
			Clock.Initialize();
			TopicServer.laodTopicServer();
			ReqRespServer.loadProviderService();
			
			final int intReqRespPort = Integer.parseInt(Config.getStringStatic(
					"ReqRespPort",
					SimpleUiSocket.class));
			ZmqRequestResponseClient.Connect(
					Config.getStringStatic(
							"TopicServerName",
							SimpleUiSocket.class), 
					intReqRespPort, 5);
			
			DistWorker.loadDistributedWorker();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	private void sendJob() 
	{
		try
		{
			m_intRequestCounter++;
			String strMessage = "-------------------------------";
			Console.writeLine(strMessage);
			DateTime logTime = DateTime.now();
			strMessage = "Sending request [" + m_intRequestCounter + "]...";
			Console.writeLine(strMessage);
			ArrayList<Object> params = new ArrayList<Object>();
			params.add(5);
			params.add(false);
			Object result = DefaultDistributedController.runDistributedViaService(
					"getFooList",
					Foo.class,
					params);
			
			Duration period = new Duration(logTime,DateTime.now()); 
			strMessage = "Got response in [" +
					period.getMillis() +
					"]mills. " + result;
			Console.writeLine(strMessage);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
}
