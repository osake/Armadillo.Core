package Armadillo.Core.Concurrent;

import java.util.concurrent.ConcurrentHashMap;

import Armadillo.Core.Logger;
import Armadillo.Core.Text.StringWrapper;

public class EfficientProducerConsumerQueue<T> 
{
	private int m_intThreads;
	private int m_intCapacity;
	private ProducerConsumerQueue<StringWrapper> m_producerConsumerQueue;
	private ConcurrentHashMap<String, T> m_keyLookup;
	private int m_intWaitMillSec;
	private Object m_taskCounterlocker = new Object();
	private int m_intInProgress;
	private int m_intDone;

	public EfficientProducerConsumerQueue(int intThreads) 
	{

		this(intThreads, 0);
	}

	public EfficientProducerConsumerQueue(int intThreads, int intCapacity) 
	{
		try
		{
			m_intThreads = intThreads;
			m_intCapacity = intCapacity;
			m_producerConsumerQueue = new ProducerConsumerQueue<StringWrapper>(intThreads,
					intCapacity) {
				@Override
				public void runTask(StringWrapper strItem) {
					ProducerConsumerOnWork(strItem);
				}
			};
	
			m_keyLookup = new ConcurrentHashMap<String, T>();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public Task add(String strKey, T obj) 
	{
		try
		{
			if (m_keyLookup.containsKey(strKey)) 
			{
				Task fakeTask = new Task(null);
				fakeTask.setTaskDone();
				return fakeTask;
			}
			m_keyLookup.put(strKey, obj);
			return m_producerConsumerQueue.add(new StringWrapper(strKey));
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return null;
	}

	private void ProducerConsumerOnWork(StringWrapper strWrapper) 
	{
		String strItem = strWrapper.getStr();
		
		if (m_keyLookup.containsKey(strItem)) 
		{
			T item = m_keyLookup.get(strItem);
			try 
			{
				synchronized (m_taskCounterlocker) 
				{
					m_intInProgress++;
				}

				runTask(item);

				if (m_intWaitMillSec > 0) 
				{
					Thread.sleep(m_intWaitMillSec);
				}

				synchronized (m_taskCounterlocker) 
				{
					m_intInProgress--;
					m_intDone++;
				}
			} 
			catch (Exception ex) 
			{
				Logger.log(ex);
			}
			finally
			{
				m_keyLookup.remove(strItem);
			}
		}
	}

	public void runTask(T item) throws Exception {
		throw new Exception("Method not implemented");
	}

	public void setWaitMillSecs(int intWaitMillSec) {
		m_intWaitMillSec = intWaitMillSec;
	}

	public int getThreads() {
		return m_intThreads;
	}

	public int getCapacity() {
		return m_intCapacity;
	}

	public boolean containsKey(String strKey) 
	{
		return m_keyLookup.containsKey(strKey);
	}

	public T tryGetValue(String strKey) {
		
		try{
			if(m_keyLookup.containsKey(strKey))
			{
				return m_keyLookup.get(strKey);
			}
			return null;
		}
		catch(Exception ex){
			Logger.log(ex);
		}
		return null;
	}
	
	public int getQueueSize() {
		return m_producerConsumerQueue.getSize();
	}

	public int getInProgress() {
		return m_intInProgress;
	}

	public int getSize() {
		return m_keyLookup.size();
	}

	public int getDone() {
		return m_intDone;
	}

	public void dispose() 
	{
		if(m_producerConsumerQueue != null)
		{
			m_producerConsumerQueue.dispose();
			m_producerConsumerQueue = null;
		}
		if(m_keyLookup != null)
		{
			m_keyLookup.clear();
			m_keyLookup = null;
		}
	}
}
