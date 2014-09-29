package Armadillo.Core.Concurrent;

import java.io.Closeable;

import Armadillo.Core.Logger;

public class Consumer<T extends Closeable> implements Runnable 
{
	ProducerConsumerQueue<T> m_queue;
	private Thread m_thread;

	public Consumer(ProducerConsumerQueue<T> queue) 
	{
		m_queue = queue;
	}

	@Override
	public void run() 
	{
		while (true) 
		{
			try 
			{
				if(m_thread == null || m_thread.isInterrupted())
				{
					return;
				}
				Task objectToTake = m_queue.m_blockingQueue.take();
				m_queue.invokeRunTask(objectToTake);
				if (objectToTake == null) 
				{
					throw new Exception("Null object to take");
				}
			} 
			catch (Exception ex) 
			{
				dispose();
				if(ex instanceof java.lang.InterruptedException)
				{
					return;
				}
				Logger.log(ex);
			}
		}
	}

	public void dispose() 
	{
		try
		{
			if(m_thread != null)
			{
				m_thread.interrupt();
			}
			m_thread = null;
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public void setThread(Thread thread) 
	{
		m_thread = thread;
	}
}
