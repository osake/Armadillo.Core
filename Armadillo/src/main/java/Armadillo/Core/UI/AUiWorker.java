package Armadillo.Core.UI;

import java.io.Closeable;

import Armadillo.Core.Concurrent.Task;

public abstract class AUiWorker implements Closeable
{
	//private ABusyNotification m_busyNotification;
	private Task m_task;

//	public ABusyNotification getBusyNotification() 
//	{
//		return m_busyNotification;
//	}
//
//	public void setBusyNotification(ABusyNotification busyNotification) 
//	{
//		m_busyNotification = busyNotification;
//	}
	
	public void setTask(Task task) 
	{
		m_task = task;
	}

	public Task getTask() 
	{
		return m_task;
	}
	
	@Override
	public void close()
	{
		//m_busyNotification = null;
		m_task = null;
	}

	public abstract void Work(); 
}
