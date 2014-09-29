package Armadillo.Communication.Impl;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Core.Concurrent.ThreadWorker;

public class SimpleUiSocket 
{
	private static InputStream m_is;
	private static ServerSocket m_serverSocket;
	private static Socket m_socket;
	private static List<AStringCallback> m_callbacks;
	private static Object m_callbacksLock;
	private static ThreadWorker<ObjectWrapper> m_worker;
	private static Object m_rcvLock = new Object();
	private static Object m_loadSocketLock = new Object();
	private static ThreadWorker<ObjectWrapper> m_loadSocketWorker;
	
	static
	{
		m_callbacksLock = new Object();
		m_callbacks = new ArrayList<AStringCallback>();
        doLoadSocket0();
        rcvLoop();
	}
	
    public static void main(String[] args)
    {
    	while(true)
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

	public static void addCallback(AStringCallback callBack)
	{
		try
		{
	        synchronized(m_callbacksLock)
	        {
				m_callbacks.add(callBack);
	        }
		} 
		catch (Exception e) 
		{
			Logger.log(e);
		}
	}

    private static void rcvLoop() 
    {
    	m_worker = new ThreadWorker<ObjectWrapper>()
			{
	    		@Override
	    		public void runTask(ObjectWrapper item) throws Exception 
	    		{
	    			try
	    			{
		    			while(true)
		    			{
		    				try
		    				{
		    					if(m_is != null)
		    					{
		    						rcvLoop0();
		    					}
			    			}
			    			catch(Exception ex)
			    			{
			    				Logger.log(ex);
			    			}
		    				loadSocket();
		    				Thread.sleep(5000);
		    			}
	    			}
	    			catch(Exception ex)
	    			{
	    				Logger.log(ex);
	    			}
	    		}
	    	};
    	m_worker.work();
    }
	
    private static void rcvLoop0() 
    {
        while(true)
        {
        	synchronized(m_rcvLock)
        	{
	        	try
	        	{
	        		if(m_is == null)
	        		{
	        			return;
	        		}
			        byte[] lenBytes = new byte[4];
			        m_is.read(lenBytes, 0, 4);
			        int len = (((lenBytes[3] & 0xff) << 24) | ((lenBytes[2] & 0xff) << 16) |
			                  ((lenBytes[1] & 0xff) << 8) | (lenBytes[0] & 0xff));
			        //Console.writeLine("Arr len " + len);
			        
			        if(len > 1e7)
			        {
			        	// this may be an error
			        	return;
			        }
			        
			        byte[] receivedBytes = new byte[len];
			        m_is.read(receivedBytes, 0, len);
			        String strReceived = new String(receivedBytes, 0, len);
			        
			        synchronized(m_callbacksLock)
			        {
			        	if(m_callbacks != null &&
			        	   m_callbacks.size() > 0)
			        	{
			        		for(AStringCallback callback : m_callbacks)
			        		{
			        			callback.OnStr(strReceived);
			        			strReceived = null;
			        		}
			        	}
			        }
			        strReceived = null;
			        receivedBytes = null;
			        lenBytes = null;
	        	}
	        	catch(Exception ex)
	        	{
	        		try 
	        		{
						Thread.sleep(5000);
					} 
	        		catch (InterruptedException e) 
	        		{
						e.printStackTrace();
					}
	        		loadSocket();
	        	}
        	}
        }
        
        // Sending
//        String toSend = "Echo: " + received;
//        byte[] toSendBytes = toSend.getBytes();
//        int toSendLen = toSendBytes.length;
//        byte[] toSendLenBytes = new byte[4];
//        toSendLenBytes[0] = (byte)(toSendLen & 0xff);
//        toSendLenBytes[1] = (byte)((toSendLen >> 8) & 0xff);
//        toSendLenBytes[2] = (byte)((toSendLen >> 16) & 0xff);
//        toSendLenBytes[3] = (byte)((toSendLen >> 24) & 0xff);
//        os.write(toSendLenBytes);
//        os.write(toSendBytes);

        //socket.close();
        //serverSocket.close();
    }

	private static void doLoadSocket0() 
	{
		m_loadSocketWorker = new ThreadWorker<ObjectWrapper>()
				{
					@Override
					public void runTask(ObjectWrapper item)
					{
						try
						{
							while(!loadSocket())
					        {
					        	try 
					        	{
									Thread.sleep(5000);
								} 
					        	catch (InterruptedException e) 
					        	{
									e.printStackTrace();
								}
					        }
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
							try 
							{
								Thread.sleep(5000);
							} 
							catch (InterruptedException e) 
							{
								e.printStackTrace();
							}
						}
					}
				};
				m_loadSocketWorker.work();
//	        while(m_is == null)
//	        {
//	        	try 
//	        	{
//					Thread.sleep(1000); // too small the wait, it will not allow other threads to run!
//				} 
//	        	catch (InterruptedException e) 
//	        	{
//					e.printStackTrace();
//				}
//	        }
	}
    
	private static boolean loadSocket()
	{
		synchronized(m_loadSocketLock)
		{
			try
			{
				if(m_is != null)
				{
					Close();
				}
				m_serverSocket = new ServerSocket(4343, 10);
		        m_socket = m_serverSocket.accept();
		        m_is = m_socket.getInputStream();
		        return true;
			}
			catch(Exception ex)
			{
				// doesnt matter
			}
		}
		return false;
	}
	
	private static void Close()
	{
		try
		{
			if(m_socket != null)
			{
				Socket socket = m_socket;
				socket.close();
				m_socket = null;
			}
			if(m_serverSocket != null)
			{
				ServerSocket serverSocket = m_serverSocket;
				serverSocket.close();
				m_serverSocket = null;
			}
			if(m_is != null)
			{
				InputStream is = m_is;
				is.close();
				m_is = null;
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
}