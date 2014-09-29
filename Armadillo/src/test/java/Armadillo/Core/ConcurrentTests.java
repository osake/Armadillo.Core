package Armadillo.Core;

import java.util.ArrayList;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.junit.Test;

import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Core.Concurrent.EfficientMemoryBuffer;
import Armadillo.Core.Concurrent.EfficientProducerConsumerQueue;
import Armadillo.Core.Concurrent.ILoopBody;
import Armadillo.Core.Concurrent.Parallel;
import Armadillo.Core.Concurrent.ProducerConsumerQueue;
import Armadillo.Core.Concurrent.Task;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Core.Text.StringWrapper;

public class ConcurrentTests {

	private EfficientProducerConsumerQueue<Object> m_efficientQueue;

	@Test
	public void testQueue()
	{
		final EfficientProducerConsumerQueue<ObjectWrapper> queue = new EfficientProducerConsumerQueue<ObjectWrapper>(10)
				{
						@Override
						public void runTask(ObjectWrapper objectToTake) 
						{
							String strJobId = (String)objectToTake.m_obj;
							System.out.println("Trabajando {" + strJobId + "}...");
							try 
							{
								Thread.sleep(500);
							} 
							catch (InterruptedException e) 
							{
								e.printStackTrace();
							}
							System.out.println("Trabajando {" + strJobId + "} terminado. " +
							getInProgress() + " in progress. Tamanio "+ 
						    getSize());
						}
				};
				
				int intContador = 0;
				Random rng = new Random();
				for (int i = 0; i < 5; i++) 
				{
					while(queue.getInProgress() < 10)
					{
						String strTrabajoId = "test_" + intContador;
						if(rng.nextDouble()> 0.5)
						{
							intContador++;
						}
						queue.add(
								strTrabajoId,
								new ObjectWrapper(strTrabajoId));
					}
					try 
					{
						Thread.sleep(100);
					} 
					catch (InterruptedException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
	}
	
	
	@Test
	public void testTaskDepend() {

		try {
			final Random random = new Random();
			final ArrayList<Task> taskList = new ArrayList<Task>();
			final ArrayList<Task> continueWhenAllTaskList = new ArrayList<Task>();

			//
			// setup queue
			//
			ProducerConsumerQueue<StringWrapper> queue = 
					new ProducerConsumerQueue<StringWrapper>(10) {

				@Override
				public void runTask(StringWrapper objectToTake) {
					try {
						long intWait = 500 + random.nextInt(1000);
						Thread.sleep(intWait);
						// System.out.println("Done base task [" + objectToTake
						// + "]");
					} catch (Exception ex) {
						Logger.log(ex);
					}
				}

				@Override
				public void onTaskCompleted(Task task) {
					synchronized (taskList) {
						if (!taskList.remove(task)) {
							try {
								throw new Exception("Task not found");
							} catch (Exception e) {
								Logger.log(e);
							}
						}
					}
				}
			};

			int i = 0;
			int intOut = 0;

			//
			// setup worker
			//
			ThreadWorker<ObjectWrapper> afterTaskWorker = new ThreadWorker<ObjectWrapper>() 
					{
				@Override
				public void runTask(ObjectWrapper ob) 
				{
					for (int j = 0; j < 5; j++) 
					{
						ArrayList<Task> currTaskList = new ArrayList<Task>();

						synchronized (taskList) {

							if (taskList.size() > 0) {
								currTaskList = new ArrayList<Task>(taskList);
							}
						}

						if (currTaskList.size() > 0) {

							Task task = new Task(null) {

								@Override
								public void runTask() {
									long intWait = 500 + random.nextInt(1000);
									try {
										Thread.sleep(intWait);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									System.out.println("Done after task ["
											+ intWait / 1000 + "]secs");
								}

								@Override
								public void onTaskCompleted(Task task) {

									synchronized (continueWhenAllTaskList) {
										continueWhenAllTaskList.remove(task);
									}
								}
							};

							try 
							{

								DateTime logTime = DateTime.now();

								System.out.println("continue when ["
										+ currTaskList.size()
										+ "] tasks are done...");
								task.continueWhenAll(currTaskList
										.toArray(new Task[currTaskList.size()]));
								task.waitTask();
								continueWhenAllTaskList.add(task);
								System.out.println("finish wait ["
										+ Seconds.secondsBetween(logTime, DateTime.now())
												.getSeconds() + "]secs");

							}
							catch (Exception e) 
							{
								e.printStackTrace();
							}
						}
						try 
						{
							Thread.sleep(1000);
						} 
						catch (InterruptedException e) 
						{
							e.printStackTrace();
						}
					}
				}
			};

			afterTaskWorker.work();

			//
			// final wait list
			//
			ThreadWorker<ObjectWrapper> waitWorker = new ThreadWorker<ObjectWrapper>() {

				@Override
				public void runTask(ObjectWrapper object) 
				{
					for (int j = 0; j < 10; j++) 
					{
						ArrayList<Task> currContinueWhenAllTaskList = new ArrayList<Task>();

						synchronized (continueWhenAllTaskList) {

							if (continueWhenAllTaskList.size() > 0) {

								currContinueWhenAllTaskList = new ArrayList<Task>(
										continueWhenAllTaskList);
							}
						}

						if (currContinueWhenAllTaskList.size() > 0) 
						{
							Task.waitAll(currContinueWhenAllTaskList);
						} 
						else 
						{
							try 
							{
								Thread.sleep(500);
							} 
							catch (InterruptedException e) 
							{
								e.printStackTrace();
							}
						}
					}
				}
			};
			waitWorker.work();

			//
			// add items to queue
			//
			for(int k = 0; k<20; k++) 
			{
				try 
				{

					Task task = queue.add(
							new StringWrapper("test_" + i));

					synchronized (taskList) {
						taskList.add(task);
					}

					// System.out.println("added = " + "test_" + i);
					i++;
					if (intOut > 5) {
						intOut = 0;
						System.out.println("--------------------------------");
						System.out.println("inProgress = "
								+ queue.getInProgress());
						System.out.println("done = " + queue.getDone());
						System.out.println("size = " + queue.getSize());
						System.out.println("--------------------------------");
					}

					Thread.sleep(1000);
				} catch (Exception ex) {
					Logger.log(ex);
				}
			}
		} 
		catch (Exception ex) 
		{
			Logger.log(ex);
		}
	}

	@Test
	public void testWorker() 
	{
		ThreadWorker<ObjectWrapper> threadWorker = new ThreadWorker<ObjectWrapper>(null) {
			@Override
			public void runTask(ObjectWrapper item) {
				try {
					System.out.println("doing work...");
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// Task.waitAll(item);
			}
		};

		Task workerTask = threadWorker.work();

		System.out.println("waiting...");
		workerTask.waitTask();
		System.out.println("done work!");
	}

	@Test
	public void testMemoryBuffer() {
		final int intSize = 20;
		final EfficientMemoryBuffer<String, String> buffer = new EfficientMemoryBuffer<String, String>(
				intSize);
		final Object lockObject = new Object();
		final Random rng = new Random();

		Parallel.For(0, 10, new ILoopBody<Integer>() {
			public void run(Integer i0) {
				try {
					Integer i;
					synchronized (lockObject) {
						i = rng.nextInt(50);
					}
					buffer.put(i.toString(), i.toString());
					if (buffer.count() > intSize) {
						throw new Exception("Invalid size");
					}
					synchronized (lockObject) {
						buffer.put(i.toString(), i.toString());
						if (!buffer.containsKey(i.toString())) {
							throw new Exception("key not found");
						}

						if (i0 % 100 == 0) {
							System.out.println("Done " + i);
							System.out.println("Count " + buffer.count());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		System.out.println("Finish test");
	}

	@Test
	public void testQueueWorker() 
	{
		final Random random = new Random();
		ProducerConsumerQueue<StringWrapper> queue = 
				new ProducerConsumerQueue<StringWrapper>(
				10) {
			@Override
			public void runTask(StringWrapper objectToTake) 
			{
				try 
				{
					// System.out.println("Doing " + objectToTake + "...");
					long intWait = 500 + random.nextInt(1000);
					Thread.sleep(intWait);
					System.out.println("Done " + objectToTake);
				} 
				catch (Exception ex) 
				{
					Logger.log(ex);
				}
			}
		};

		int i = 0;
		int intOut = 0;
		ArrayList<Task> list = new ArrayList<Task>();
		int intToDo = 5;
		for (int k = 0; k < intToDo; k++) {
			try {
				Task task = queue.add(new StringWrapper("test_" + i));
				list.add(task);
				System.out.println("added = " + "test_" + i);
				i++;
				if (intOut > 5) {
					intOut = 0;
					System.out.println("--------------------------------");
					System.out.println("inProgress = " + queue.getInProgress());
					System.out.println("done = " + queue.getDone());
					System.out.println("size = " + queue.getSize());
					System.out.println("--------------------------------");
				}
			} catch (Exception ex) {
				Logger.log(ex);
			}
		}

		ThreadWorker<ObjectWrapper> threadWorker = new ThreadWorker<ObjectWrapper>(
				new ObjectWrapper(list)) {
			@SuppressWarnings("unchecked")
			@Override
			public void runTask(ObjectWrapper item) {
				try {
					System.out.println("worker is doing work...");
					Task.waitAll((ArrayList<Task>) item.getObj());
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

		Task workerTask = threadWorker.work();

		workerTask.waitTask();

		Task.waitAll(list);

		if (queue.getDone() != intToDo) {
			try {
				throw new Exception("Invalid number of tasks");
			} catch (Exception e) {
				Logger.log(e);
				e.printStackTrace();
			}
		}
		System.out.println("Done test");
	}

	@Test
	public void testEfficientQueue() {

		m_efficientQueue = new EfficientProducerConsumerQueue<Object>(10) {
			@Override
			public void runTask(Object item) {
				Random random = new Random();
				long intWait = 1000 + random.nextInt(5000);
				try {
					Thread.sleep(intWait);
					System.out.println("--------------------------------");
					System.out.println("inProgress = "
							+ m_efficientQueue.getInProgress());
					System.out.println("done = " + m_efficientQueue.getDone());
					System.out.println("size = " + m_efficientQueue.getSize());
					System.out.println("queueSize = "
							+ m_efficientQueue.getQueueSize());
					System.out.println("--------------------------------");
				} catch (InterruptedException ex) {
					Logger.log(ex);
				}
			}
		};

		ThreadWorker<ObjectWrapper> worker1 = new ThreadWorker<ObjectWrapper>() {
			@Override
			public void runTask(ObjectWrapper item) {
				runTaskLocal(item);
			}
		};
		worker1.work();

		ThreadWorker<ObjectWrapper> worker2 = new ThreadWorker<ObjectWrapper>() {
			@Override
			public void runTask(ObjectWrapper item) {
				runTaskLocal(item);
			}
		};
		worker2.work();
		runTaskLocal(null);
	}

	private void runTaskLocal(Object item) {
		Random random = new Random();
		for (int i = 0; i < 10; i++) {
			String strKey = random.nextInt(2) + "";
			m_efficientQueue.add(strKey, strKey);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
