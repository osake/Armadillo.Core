package Armadillo.Communication.Impl.Distributed;

import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.Minutes;

import Armadillo.Core.Config;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Communication.Impl.ReqResp.EnumReqResp;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Core.SelfDescribing.ASelfDescribingClass;
import Armadillo.Core.SelfDescribing.SelfDescribingClass;
import Armadillo.Core.UI.LiveGuiPublisher;
import Armadillo.Core.UI.PublishUiMessageEvent;

public class DistGuiHelper {
	
    public static void ResetGui()
    {
    	ThreadWorker<ObjectWrapper> worker = new ThreadWorker<ObjectWrapper>(){
    		@Override
    		public void runTask(ObjectWrapper item) {
                PublishUiMessageEvent.RemoveForm(
                        EnumReqResp.Admin.toString(),
                        EnumDistributedGui.CloudWorkers.toString());
                    PublishUiMessageEvent.RemoveForm(
                        EnumReqResp.Admin.toString(),
                        EnumDistributedGui.CloudControllers.toString());
    		}
    	};
    	
    	worker.work();
    	
    }

    public static void PublishJobLog(
        DistController distController,
        String strWorkerId,
        String strJobId,
        ASelfDescribingClass jobLog)
    {
        String strKey = strWorkerId + strJobId;
        LiveGuiPublisher.PublishGui(
                EnumReqResp.Admin.toString(),
                EnumDistributedGui.CloudControllers.toString(),
                EnumDistributedGui.JobsDetails + "_" + distController.ControllerId,
                strKey,
                jobLog);
    }


    public static void PublishControllerGui(DistController distController)
    {
        try
        {
        	if(distController == null){
        		return;
        	}
        	
            while(!distController.IsReady)
            {
                Thread.sleep(1000);
            }
            PublishControllerStats(distController);
            PublishControllerWorkerStats(distController);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public static void PublishWorkerLog(
        DistWorker distWorker,
        String strLog)
    {
        LiveGuiPublisher.PublishLog(
                EnumReqResp.Admin.toString(),
                EnumDistributedGui.CloudWorkers.toString(),
                EnumDistributedGui.Log + "_" + distWorker.WorkerId,
                UUID.randomUUID().toString(),
                strLog);
    }

    public static void PublishWorkerStats(DistWorker distWorker)
    {
        try
        {
            SelfDescribingClass guiValues = new SelfDescribingClass();
            guiValues.SetClassName(EnumDistributedGui.WorkerGuiClass);
            guiValues.SetDateValue(
                EnumDistributed.Time,
                DateTime.now().toDate());
            guiValues.SetIntValue(EnumDistributedGui.JobsCompleted, 
            		distWorker.JobsCompleted());
            guiValues.SetIntValue(EnumDistributedGui.Threads, distWorker.Threads);
            guiValues.SetIntValue(EnumDistributedGui.JobsInProgress, 
                distWorker.JobsInProgress);
            
            PublishUiMessageEvent.PublishGrid(
                EnumReqResp.Admin.toString(),
                EnumDistributedGui.CloudWorkers.toString(),
                "Stats_" + distWorker.WorkerId,
                distWorker.WorkerId,
                guiValues,
                0,
                true);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private static void PublishControllerWorkerStats(DistController distController)
    {
        try
        {
            for (Map.Entry<String, String> kvp : 
            	distController.DistControllerToWorkerHeartBeat.WorkersStatus.entrySet())
            {
                String strWorkerId = kvp.getKey();
                SelfDescribingClass guiValues = new SelfDescribingClass();
                guiValues.SetDateValue(
                    EnumDistributed.Time,
                    DateTime.now().toDate());
                guiValues.SetClassName(EnumDistributedGui.WorkersGuiClass);
                guiValues.SetStrValue(EnumDistributed.WorkerId, strWorkerId);
                guiValues.SetStrValue(EnumDistributedGui.State, kvp.getValue());
                
                int intJobsInProgress =0;
                for(Entry<String, ASelfDescribingClass> kvp2 : 
                	distController.DistControllerJobPull.MapJobIdToWorkerId.entrySet()){
                	
                	if(DistControllerJobLogger.GetWorkerId(kvp2.getValue()).equals(strWorkerId)){
                		intJobsInProgress++;
                	}
                }
                
                guiValues.SetIntValue(
                    EnumDistributedGui.JobsInProgress, 
                    intJobsInProgress);
                int intJobsDone = 0;
                
                if(distController.DistControllerJobPull.MapWorkerToJobsDone.containsKey(strWorkerId)){
                	
                	intJobsDone = distController.DistControllerJobPull.MapWorkerToJobsDone.get(
	                    strWorkerId);
                }
                guiValues.SetIntValue(
                    EnumDistributedGui.JobsDone,
                    intJobsDone);

                DateTime lastPingTime;
                
                if(!distController.DistControllerToWorkerHeartBeat.WorkersPingTimes.containsKey(
                    strWorkerId))
                {
                    lastPingTime = DateTime.now();
                }
                else{
                	lastPingTime = distController.DistControllerToWorkerHeartBeat.WorkersPingTimes.get(strWorkerId);
                }
                guiValues.SetDateValue(
                		EnumDistributedGui.LastPingTime, 
                		lastPingTime.toDate());

                PublishUiMessageEvent.PublishGrid(
                    EnumReqResp.Admin.toString(),
                    EnumDistributedGui.CloudControllers.toString(),
                    EnumDistributedGui.Workers.toString() + "_" +
                                        distController.ServerName + "_" +
                                        distController.GridTopic,
                    strWorkerId,
                    guiValues,
                    0,
                    false);
            }
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    private static void PublishControllerStats(DistController distController)
    {
        try
        {
            SelfDescribingClass guiValues = new SelfDescribingClass();
            guiValues.SetClassName(EnumDistributedGui.ControllerGuiClass);
            guiValues.SetDateValue(
                EnumDistributed.Time,
                DateTime.now().toDate());
            guiValues.SetStrValue(
                EnumDistributed.ControllerId,
                distController.ControllerId);
            guiValues.SetIntValue(
                EnumDistributedGui.JobsInProgress,
                distController.JobsToDoMap.size());
            guiValues.SetIntValue(
                EnumDistributedGui.JobsAssigned,
                distController.DistControllerJobPull.MapJobIdToWorkerId.size());
            guiValues.SetIntValue(
                EnumDistributedGui.JobsDone,
                distController.JobsDone());
            guiValues.SetDblValue(
                EnumDistributedGui.PingLatencySecs,
                distController.DistControllerToWorkerHeartBeat.PingLatencySecs.Mean());
            guiValues.SetDblValue(
                EnumDistributedGui.NumWorkersConnected,
                distController.DistControllerToWorkerHeartBeat.WorkersPingTimes.size());

            String strControllerName =
            	Config.getHostName() + "_" +
                distController.ControllerId;

            PublishUiMessageEvent.PublishGrid(
                EnumReqResp.Admin.toString(),
                EnumDistributedGui.CloudControllers.toString(),
                EnumDistributedGui.Jobs.toString() + "_" + 
                    distController.ServerName + "_" +
                    distController.GridTopic,
                strControllerName,
                guiValues,
                0,
                true);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public static void PublishControllerLog(
        DistController distController,
        String strLog)
    {
        try
        {
            LiveGuiPublisher.PublishLog(
                EnumReqResp.Admin.toString(),
                EnumDistributedGui.CloudControllers.toString(),
                EnumDistributedGui.Log.toString() + "_" +
                    distController.ServerName + "_" +
                    distController.GridTopic,
                UUID.randomUUID().toString(),
                strLog);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
    }

    public static void PublishJobLogStatus(
        DistController distController, 
        ASelfDescribingClass jobLog, 
        String strLog)
    {
        if (jobLog == null)
        {
            return;
        }
        jobLog.SetStrValue(EnumDistributedGui.State, strLog);
        jobLog.SetDateValue(EnumDistributedGui.LastUpdateTime, DateTime.now().toDate());
        PublishJobLog(
            distController,
            DistControllerJobLogger.GetWorkerId(jobLog),
            DistControllerJobLogger.GetJobId(jobLog),
            jobLog);
    }

    public static void PublishJobLogDone(
        DistController distController, 
        ASelfDescribingClass jobLog)
    {
        jobLog.SetStrValue(EnumDistributedGui.State, EnumDistributedGui.Done.toString());
        jobLog.SetDateValue(EnumDistributedGui.LastUpdateTime, DateTime.now().toDate());
        DateTime startTime = new DateTime(jobLog.TryGetDateValue(EnumDistributedGui.StartTime));
        jobLog.SetDateValue(EnumDistributedGui.EndTime, DateTime.now().toDate());
        jobLog.SetDblValue(EnumDistributedGui.TotalTimeMins, 
        		Minutes.minutesBetween(startTime, DateTime.now()).getMinutes());

        PublishJobLog(
            distController,
            DistControllerJobLogger.GetWorkerId(jobLog),
            DistControllerJobLogger.GetJobId(jobLog),
            jobLog);
    }
}
