package Armadillo.Communication.Impl.Distributed;

import org.joda.time.DateTime;

import Armadillo.Core.SelfDescribing.ASelfDescribingClass;
import Armadillo.Core.SelfDescribing.SelfDescribingClass;

public class DistControllerJobLogger {
    
	public static String GetWorkerId(ASelfDescribingClass jobLog)
    {
        if(jobLog == null)
        {
            return "";
        }
        String strWorkerId = jobLog.TryGetStrValue(
            EnumDistributedGui.WorkerId);
        return strWorkerId;
    }

    public static SelfDescribingClass GetJobLog(
        String strWorkerId, 
        String strJobId,
        String strPullId)
    {
        SelfDescribingClass jobLog = new SelfDescribingClass();
        jobLog.SetClassName(EnumDistributedGui.JobsInProgress + DistControllerJobLogger.class.getName());
        jobLog.SetStrValue(EnumDistributedGui.WorkerId, strWorkerId);
        jobLog.SetStrValue(EnumDistributedGui.JobId, strJobId);
        jobLog.SetStrValue(EnumDistributedGui.State, "JobsInProgress");
        jobLog.SetDateValue(EnumDistributedGui.StartTime, DateTime.now().toDate());
        jobLog.SetDateValue(EnumDistributedGui.LastUpdateTime, DateTime.now().toDate());
        jobLog.SetDateValue(EnumDistributedGui.EndTime, new DateTime().toDate());
        jobLog.SetIntValue(EnumDistributedGui.TotalTimeMins, 0);
        jobLog.SetStrValue(EnumDistributed.PullId, strPullId);
        return jobLog;
    }

    public static String GetJobId(ASelfDescribingClass jobLog)
    {
        if (jobLog == null)
        {
            return "";
        }
        String strJobId = jobLog.TryGetStrValue(EnumDistributedGui.JobId);
        return strJobId;
    }

}
