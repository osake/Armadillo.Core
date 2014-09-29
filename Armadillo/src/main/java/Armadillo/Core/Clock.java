package Armadillo.Core;

import org.joda.time.DateTime;

import Armadillo.Core.Concurrent.ThreadWorker;

public class Clock 
{
	
	public static DateTime LastTime = DateTime.now();

    public static void Initialize() 
    {
    	ThreadWorker<ObjectWrapper> worker = new ThreadWorker<ObjectWrapper>(){
    		public void runTask(ObjectWrapper obj){
                try
                {
                    LastTime = DateTime.now();
                    Thread.sleep(200);
                }
                catch (Exception ex)
                {
                    Logger.log(ex);
                    try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						Logger.log(e);
					}
                }
    		}
    	};
        worker.work();
    }
}
