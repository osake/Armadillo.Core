package Armadillo.Core.Concurrent;

import java.io.Closeable;
import Armadillo.Core.Logger;

public class ThreadWorker<T extends Closeable> implements Runnable {

	private Task m_task;
	private T m_item;
	private Thread m_thread;

	public ThreadWorker() {
		this(null);
	}

	public ThreadWorker(T item) {
		m_item = item;
	}

	public Task work() 
	{
		m_task = new Task(m_item);
		m_thread = new Thread(this);
		m_thread.start();
		return m_task;
	}

	public void runTask(T item) throws Exception 
	{
		throw new Exception("Method not implemented");
	}

	@Override
	public void run() {
		try {
			runTask(m_item);
		} catch (Exception ex) {
			Logger.log(ex);
		} finally {
			//synchronized (m_task.m_lock) {
				m_task.setTaskDone();
			//}
		}
	}

	public void dispose() {
		m_thread.interrupt();
	}

	public void Dispose() {
	}
}
