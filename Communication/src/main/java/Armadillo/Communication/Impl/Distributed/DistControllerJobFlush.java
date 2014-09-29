package Armadillo.Communication.Impl.Distributed;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.joda.time.DateTime;
import Armadillo.Core.Logger;
import Armadillo.Core.SelfDescribing.ASelfDescribingClass;
import Armadillo.Core.SelfDescribing.SelfDescribingTsEvent;
import Armadillo.Core.Text.StringHelper;

public class DistControllerJobFlush {
	
    public static SelfDescribingTsEvent DoFlushJobs(
            DistController distController,
            ASelfDescribingClass paramsClass,
            String strJobId,
            DistControllerJobPull distControllerJobPull,
            ConcurrentHashMap<String, ASelfDescribingClass> jobsInProgressMap)
        {
            try
            {
                String strClassName = 
                paramsClass.TryGetStrValue(
                    EnumCalcCols.ClassName);
                Set<Map.Entry<String, ASelfDescribingClass>> jobsInProgressArr;
                int intFlushedJobs = 0;
                synchronized (distControllerJobPull.JobsInProgressLock)
                {
                    jobsInProgressArr = jobsInProgressMap.entrySet();
                }
                for (Map.Entry<String, ASelfDescribingClass> kvp : 
                	jobsInProgressArr)
                {
                    boolean blnDoRemove = false;
                    ASelfDescribingClass currParams = kvp.getValue();
                    if (!StringHelper.IsNullOrEmpty(strClassName))
                    {
                        String strCurrClassName = 
                        		currParams.TryGetStrValue(EnumCalcCols.ClassName);
                        if (!StringHelper.IsNullOrEmpty(strCurrClassName))
                        {
                            if (strCurrClassName.equals(strClassName))
                            {
                                blnDoRemove = true;
                            }
                        }
                    }
                    else
                    {
                        blnDoRemove = true;
                    }
                    if (blnDoRemove)
                    {
                        synchronized (distControllerJobPull.JobsInProgressLock)
                        {
                            jobsInProgressMap.remove(
                                kvp.getKey());
                            if(distControllerJobPull.MapJobIdToWorkerId.containsKey(strJobId)){
                                ASelfDescribingClass jobLog = 
                                		distControllerJobPull.MapJobIdToWorkerId.get(strJobId);
	                            distControllerJobPull.MapJobIdToWorkerId.remove(
	                                strJobId);
	                            DistGuiHelper.PublishJobLogStatus(
	                                    distController,
	                                    jobLog,
	                                    "JobFlushed");
                            }
                            intFlushedJobs++;
                        }
                    }
                }
                SelfDescribingTsEvent resultTsEv = new SelfDescribingTsEvent(
                    EnumCalcCols.FlushJobs);
                resultTsEv.Time = DateTime.now();
                
                resultTsEv.SetObjValueToDict(
                    EnumCalcCols.Result,
                    "Successfull flushed [" + intFlushedJobs + "] jobs");
                return resultTsEv;
            }
            catch (Exception ex)
            {
                Logger.log(ex);
                SelfDescribingTsEvent resultTsEv = new SelfDescribingTsEvent(
                    EnumCalcCols.FlushJobs);
                resultTsEv.Time = DateTime.now();
                
                resultTsEv.SetObjValueToDict(
                    EnumCalcCols.Result,
                    "failed flushed jobs");
                return resultTsEv;
            }
        }
}
