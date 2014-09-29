package Armadillo.Core.Concurrent;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import Armadillo.Core.Logger;

public class Task implements Closeable {

	private Closeable m_item;
	private Object m_lock;
	private boolean m_blnIsDone;
	private ArrayList<Task> m_prevTasks;
	private ArrayList<Task> m_nextTasks;
	private Object m_taskDoneLock = new Object();
	public ArrayList<TaskNotifier> m_taskNotifiers = new ArrayList<TaskNotifier>();
	private boolean m_blnIsClosed;
	private static DateTime m_prevVerbose = DateTime.now();
	
	public Task(Closeable item) 
	{
		m_lock = new Object();
		m_item = item;
		m_prevTasks = new ArrayList<Task>();
		m_nextTasks = new ArrayList<Task>();
	}

	public Object getLockObj() 
	{
		return m_lock;
	}

	public void setTaskDone() 
	{
		try
		{
			synchronized(m_lock)
			{
				setTaskDoneUnsafe();
			}
			
			close();
			
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
	private void notifyCompletionToNextTasks() {
		
		synchronized(m_nextTasks)
		{
			if(m_nextTasks.size() > 0)
			{
				for (int i = 0; i < m_nextTasks.size(); i++) 
				{
					m_nextTasks.get(i).notifyCompletion(this);
				}
			}
			m_nextTasks.clear();
		}
	}

	private void notifyCompletion(Task task) 
	{
		try
		{
			synchronized(m_prevTasks)
			{
				if(!m_prevTasks.remove(task))
				{
						throw new Exception("task not found");
				}
				
				if(m_prevTasks.size() == 0)
				{
					onAllPrevTasksDone();
				}
			}
		} 
		catch (Exception e) 
		{
			Logger.log(e);
		}
	}

	private void onAllPrevTasksDone() throws Exception 
	{
		runTask();
		setTaskDone();
		onTaskCompleted(this);
		
		if(m_taskNotifiers != null &&
				m_taskNotifiers.size() > 0)
		{
			
			for (TaskNotifier taskNotifier : m_taskNotifiers) 
			{
				taskNotifier.noptify(this);
			}
		}
	}

	public void waitTask() 
	{
		try 
		{
			Object objLock = m_lock;
			if (!m_blnIsDone &&
					!m_blnIsClosed) 
			{
				if(objLock == null)
				{
					objLock = new Object();
				}
				Object taskDoneLock = m_taskDoneLock;
				
				synchronized (objLock) 
				{
					
					if(taskDoneLock != null)
					{
						
						if (!m_blnIsDone &&
								!m_blnIsClosed) 
						{
							objLock.wait();
						}
					}
				}
			}
		} catch (InterruptedException ex) 
		{
			Logger.log(ex);
		}
	}
	
	public void runTask() throws Exception
	{
		throw new Exception("method not implmented");
	}

	public Object getItem() 
	{
		return m_item;
	}

	public static void waitAll(ArrayList<Task> taskList) 
	{
		try
		{
			if(taskList == null)
			{
				return;
			}
			for (int i = 0; i < taskList.size(); i++) 
			{
				Task task = taskList.get(i);
				if(task !=null)
				{
					task.waitTask();
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public static void waitAll(Task taskList[]) 
	{
		try
		{
			if(taskList == null ||
					taskList.length == 0)
			{
				return;
			}
			
			for (int i = 0; i < taskList.length; i++) 
			{
				taskList[i].waitTask();
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	public void continueWhenAll(Task[] prevTasks) throws Exception 
	{
		try
		{
			synchronized(m_taskDoneLock)
			{
				if(m_blnIsDone)
				{
					//
					// task already done, no need to wait
					//
					onAllPrevTasksDone();
					return;
				}
				
				//
				// prevent race condition, any task completed in this loop could trigger all done condition
				// we must wait until the whole list is added
				//
				synchronized(m_prevTasks) 
				{
					boolean blnAnyTaskToWait = false;
					for (int i = 0; i < prevTasks.length; i++) {
						
						Task prevTask = prevTasks[i];
						Object lockObj = prevTask.m_taskDoneLock;
						if(lockObj != null)
						{
							synchronized(lockObj)
							{
								if(!prevTask.m_blnIsDone)
								{
									addPreviousTask(prevTask);
									prevTask.addNextTask(this);
									blnAnyTaskToWait = true;
								}
							}
						}
					}
					
					if(!blnAnyTaskToWait)
					{
						//
						// all tasks are already done, proceed with task
						//
						onAllPrevTasksDone();
						return;
					}
				}
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		
	}

	private void addNextTask(Task task) 
	{
		synchronized(m_nextTasks)
		{
			m_nextTasks.add(task);
		}
	}
	
	private void addPreviousTask(Task task) 
	{
		//
		// this method should be thread safe
		//
		m_prevTasks.add(task);
	}

	public void onTaskCompleted(Task task) 
	{
	}
	
	public void subscriveNotifier(TaskNotifier taskNotifier){
		m_taskNotifiers.add(taskNotifier);
	}

	private void setTaskDoneUnsafe() 
	{
		try
		{
			synchronized(m_taskDoneLock)
			{
				m_blnIsDone = true;
				m_lock.notify();
				notifyCompletionToNextTasks();
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	@Override
	public void close()
	{
		try
		{
			if(m_blnIsClosed)
			{
				return;
			}
			
			if(!m_blnIsDone)
			{
				throw new Exception("Task not done");
			}
			
			m_blnIsClosed = true;

			Object lock = m_lock;
			if(lock != null)
			{
				synchronized(lock)
				{
					lock.notify();
					purge();
				}
			}
			else
			{
				purge();
			}
			
			if(Seconds.secondsBetween(
					m_prevVerbose, DateTime.now()).getSeconds() > 5)
			{
				m_prevVerbose = DateTime.now();
			}
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}

	private void purge() throws IOException 
	{
		if(m_item != null)
		{
			m_item.close();
			m_item = null;
		}
		m_lock = null;
		
		if(m_prevTasks != null)
		{
			if(m_prevTasks.size() > 0)
			{
				for (int i = 0; i < m_prevTasks.size(); i++) 
				{
					m_prevTasks.get(i).close();
				}
			}
			
			m_prevTasks.clear();
			m_prevTasks = null;
		}
		
		if(m_nextTasks != null)
		{
			m_nextTasks.clear();
			m_nextTasks = null;
		}
		
		if(m_taskNotifiers != null)
		{
			m_taskNotifiers.clear();
			m_taskNotifiers = null;
		}
		m_taskDoneLock = null;
	}

//	public void dispose() {
//		close();
//	}
}