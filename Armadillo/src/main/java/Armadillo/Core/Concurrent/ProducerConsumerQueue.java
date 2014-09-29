package Armadillo.Core.Concurrent;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import Armadillo.Core.Logger;

public class ProducerConsumerQueue<T extends Closeable> {

	private int m_intThreads;
	public LinkedBlockingQueue<Task> m_blockingQueue;
	private Object m_taskCounterlocker = new Object();
	private int m_intInProgress;
	private int m_intDone;
	private List<Consumer<T>> m_consumerList;

	public ProducerConsumerQueue(int intThreads) 
	{
		this(intThreads, 0);
	}

	public ProducerConsumerQueue(int intThreads, int intCapacity) 
	{
		m_consumerList = new ArrayList<Consumer<T>>();
		m_intThreads = intThreads;
		if (intCapacity > 0) 
		{
			m_blockingQueue = new LinkedBlockingQueue<Task>(intCapacity);
		} 
		else 
		{
			m_blockingQueue = new LinkedBlockingQueue<Task>();
		}
		loadTreads();
	}

	public int getSize() 
	{
		return m_blockingQueue.size();
	}

	public int getInProgress() 
	{
		return m_intInProgress;
	}

	public int getDone() {
		return m_intDone;
	}

	public Task add(T item) 
	{
		Task task = new Task(item);
		try 
		{
			m_blockingQueue.put(task);
			// if this throws exception then use .offer method and loop until space is available
		} 
		catch (InterruptedException ex) 
		{
			Logger.log(ex);
		}
		return task;
	}

	private void loadTreads() 
	{
		try 
		{
			for (int i = 0; i < m_intThreads; i++) 
			{
				Consumer<T> consumer = new Consumer<T>(this);
				Thread thread = new Thread(consumer);
				consumer.setThread(thread);
				thread.start();
				m_consumerList.add(consumer);
			}
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public void invokeRunTask(Task task) 
	{
		try 
		{
			synchronized (m_taskCounterlocker) 
			{
				m_intInProgress++;
			}
	
			synchronized (task.getLockObj()) 
			{
				runTask((T)task.getItem());
				onTaskCompleted(task);
			}
	
			synchronized (m_taskCounterlocker) 
			{
				m_intInProgress--;
				m_intDone++;
				task.setTaskDone();
				task.close();
			}
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}

	public void onTaskCompleted(Task task) 
	{
	}

	public void runTask(T objectToTake) throws Exception {
		throw new Exception("This method should be overriden");
	}

	public void dispose() 
	{
		try
		{
			if(m_consumerList != null)
			{
				for(Consumer<T> item : m_consumerList)
				{
					item.dispose();
				}
				m_consumerList.clear();
				m_consumerList = null;
				m_blockingQueue.clear();
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
}