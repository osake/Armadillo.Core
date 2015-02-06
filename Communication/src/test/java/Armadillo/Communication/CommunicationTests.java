package Armadillo.Communication;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



import org.junit.Assert;
import org.junit.Test;

import Armadillo.Core.Clock;
import Armadillo.Core.Config;
import Armadillo.Core.Console;
import Armadillo.Core.Foo;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Communication.Impl.SimpleUiSocket;
import Armadillo.Communication.Impl.ReqResp.ARequestResponseClient;
import Armadillo.Communication.Impl.ReqResp.ReqRespServer;
import Armadillo.Communication.Impl.ReqResp.ReqRespService;
import Armadillo.Communication.Impl.ReqResp.RequestDataMessage;
import Armadillo.Communication.Impl.ReqResp.ZmqReqRespClient;
import Armadillo.Communication.Impl.Topic.SubscriberCallbackDel;
import Armadillo.Communication.Impl.Topic.TopicMessage;
import Armadillo.Communication.Impl.Topic.TopicPublisherCache;
import Armadillo.Communication.Impl.Topic.TopicServer;
import Armadillo.Communication.Impl.Topic.TopicSubscriberCache;
import Armadillo.Core.Concurrent.ProducerConsumerQueue;
import Armadillo.Core.Concurrent.Task;
import Armadillo.Core.Concurrent.ThreadWorker;

public class CommunicationTests 
{
	
	protected static final int LIST_SIZE = 5;
	public static String m_strServerName = "127.0.0.1";
	
	public void testAll()
	{
		
		try
		{
			Clock.Initialize();
			loadTopicService();
			
			ThreadWorker<ObjectWrapper> workerTopic = new ThreadWorker<ObjectWrapper>()
			{
				public void runTask(ObjectWrapper item)
				{
					RunTopictests();
				};
			};
			workerTopic.work();
			
			ThreadWorker<ObjectWrapper> reqRestTopic = new ThreadWorker<ObjectWrapper>()
			{
				public void runTask(ObjectWrapper item)
				{
					testReqRespClientServer();
				};
			};
			reqRestTopic.work();
			
			while(true)
			{
				Thread.sleep(10);
			}
	        
		}
		catch(Exception ex){
			Logger.log(ex);
		}
	}

	public static void startAllServices(){
		Clock.Initialize();
		loadTopicService();
		final int intReqRespPort = Integer.parseInt(Config.getStringStatic(
				"ReqRespPort",
				SimpleUiSocket.class));
		ReqRespServer.StartService(m_strServerName, intReqRespPort, 5);
		ReqRespService.Connect();
		ZmqReqRespClient.Connect(
				Config.getStringStatic(
						"TopicServerName",
						SimpleUiSocket.class), 
				intReqRespPort, 5);
	}
	
	@Test
	public void testReqRespClientServer() 
	{
		Clock.Initialize();
		loadTopicService();
		
		final String strServerName = "localhost";
		loadReqRespServer(strServerName);
		
		testReqRespClient();
	}

	public void testReqRespServer() 
	{
		try
		{
			String strServerName = Config.getStringStatic(
					"TopicServerName",
					SimpleUiSocket.class);
			loadReqRespServer(strServerName);
			while(true)
			{
				Thread.sleep(100);
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	public void loadReqRespServer(String strServerName) 
	{
		Clock.Initialize();
		final int intReqRespPort = Integer.parseInt(Config.getStringStatic(
				"ReqRespPort",
				SimpleUiSocket.class));
		ReqRespServer.StartService(strServerName, intReqRespPort, 5);
		ReqRespService.Connect();
		ZmqReqRespClient.Connect(strServerName, intReqRespPort, 5);
	}

	public void testReqRespClient() 
	{
		try
		{
			final int intReqRespPort = 
					Integer.parseInt(Config.getStringStatic(
							"ReqRespPort",
							SimpleUiSocket.class));
			
			ZmqReqRespClient.Connect(
					"localhost", 
					intReqRespPort, 5);
			final int intTimeOutSeconds = Integer.MAX_VALUE;
			
			ProducerConsumerQueue<ObjectWrapper> queue = new ProducerConsumerQueue<ObjectWrapper>(3)
			{
				@Override
				public void runTask(ObjectWrapper objectToTake) 
				{
					
					RequestDataMessage requestDataMessage = new RequestDataMessage();
					requestDataMessage.Id = UUID.randomUUID().toString();
					requestDataMessage.Request = Foo.class.getName() + "|getFooList";
					requestDataMessage.Params = new ArrayList<Object>();
					requestDataMessage.Params.add(LIST_SIZE);
					requestDataMessage.Params.add(false);
					
					String strMessage = "Attempt to send request...";
					Console.writeLine(strMessage);
					Logger.log(strMessage);
					
					ARequestResponseClient connection = ZmqReqRespClient.GetConnection("localhost", intReqRespPort);
					@SuppressWarnings("unchecked")
					ArrayList<Foo> response = 
							(ArrayList<Foo>)connection.SendRequestAndGetResponse(
								requestDataMessage, 
								intTimeOutSeconds).get(0);
					Assert.assertTrue("Invalid list size", response.size() == LIST_SIZE);
					Console.writeLine("List size= " + response.size());
				}
			}; 
			
			List<Task> taskList = new ArrayList<Task>();
			for (int i = 0; i < 5; i++) 
			{
				try 
				{
					if(queue.getSize() < 50)
					{
						taskList.add(queue.add(null));
					}
					Thread.sleep(100);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
			Task.waitAll(taskList);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	@Test
	public void testTopicServer()
	{
		Clock.Initialize();
		loadTopicService();
		RunTopictests();
	}


	public void RunTopicListenerTests() 
	{
		String strServerName = m_strServerName;
		
		SubscriberCallbackDel subscriberCallbackDel2 = new SubscriberCallbackDel()
		{
			@Override
			public void invoke(TopicMessage topicMessage)
			{
				Console.writeLine(topicMessage.EventData.toString());
			}
		};
		String strTopic1 = "testTopic_0";
		TopicSubscriberCache.GetSubscriber(strServerName).Subscribe(strTopic1, 
				subscriberCallbackDel2);
		
		while(true)
		{
			try
			{
				Thread.sleep(100);
			}
			catch(Exception ex)
			{
				
			}
		}
	}
	
	public void RunTopictests() 
	{
		String strServerName = Config.getStringStatic(
				"TopicServerName",
				SimpleUiSocket.class);
		final boolean[] doneTest = new boolean[1];
		
		SubscriberCallbackDel subscriberCallbackDel2 = new SubscriberCallbackDel()
		{
			@Override
			public void invoke(TopicMessage topicMessage)
			{
				Console.writeLine(topicMessage.EventData.toString());
				doneTest[0] = true;
			}
		};
		String strTopic1 = "testTopic1";
		TopicSubscriberCache.GetSubscriber(strServerName).Subscribe(strTopic1, 
				subscriberCallbackDel2);
		
		String strTopic2 = "testTopic2";
		TopicSubscriberCache.GetSubscriber(strServerName).Subscribe(strTopic2, 
				subscriberCallbackDel2);
		
		String strTopic3 = "testTopic3";
		
		int intCounter = 0;
		while(!doneTest[0])
		{
			TopicPublisherCache.GetPublisher(strServerName).SendMessage(
					"hello1_" + intCounter++, 
					strTopic1, 
					true);
			ArrayList<Foo> foos = Foo.getFooList(2, false);
			TopicPublisherCache.GetPublisher(strServerName).SendMessage(
					foos, 
					strTopic2, 
					true);
			TopicPublisherCache.GetPublisher(strServerName).SendMessage(
					"hello3_" + intCounter, 
					strTopic3, 
					true);
			Console.writeLine("Done publish [" + intCounter + "]");
			try 
			{
				Thread.sleep(100);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}

	private static void loadTopicService() 
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
	}
}
