package Armadillo.Communication.Impl.Distributed;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import Armadillo.Core.Console;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Communication.Impl.Topic.SubscriberCallbackDel;
import Armadillo.Communication.Impl.Topic.TopicMessage;
import Armadillo.Communication.Impl.Topic.TopicPublisherCache;
import Armadillo.Communication.Impl.Topic.TopicSubscriberCache;
import Armadillo.Core.SelfDescribing.ASelfDescribingClass;
import Armadillo.Core.SelfDescribing.SelfDescribingClass;
import Armadillo.Core.Text.StringHelper;

public class DistWorkerResultSender {
    
	static final int TOTAL_TRIALS = 15;
	static final int DISCONNECTION_TRIALS = 3;

    private ConcurrentHashMap<String, ASelfDescribingClass> m_jobsToSend;
    private DistWorker m_distWorker;

    public DistWorkerResultSender(
        DistWorker distWorker)
    {
        m_distWorker = distWorker;
        m_jobsToSend = new ConcurrentHashMap<String, ASelfDescribingClass>();
        TopicSubscriberCache.GetSubscriber(distWorker.ServerName).Subscribe(
            distWorker.GridTopic + EnumDistributed.TopicControllerToWorkerResultConfirm.toString(),
            new SubscriberCallbackDel(){
            	public void invoke(TopicMessage topicMessage) {
            		OnTopicControllerToWorkerConfirm(topicMessage);
            	};
            }
            );
    }

    public boolean SendResult(
        List<Object> events, 
        String strParentId, 
        String strJobId)
    {
        try
        {
            SelfDescribingClass resultParams = GetResultParams(
                strParentId,
                strJobId,
                events);
            return SendResult(resultParams,
                strJobId);
        }
        catch (Exception ex)
        {
            Logger.log(ex);
            return false;
        }
    }

    private SelfDescribingClass GetResultParams(
        String strParentId,
        String strJobId,
        List<Object> events)
    {
    	SelfDescribingClass resultParams;
        resultParams = new SelfDescribingClass();
        resultParams.SetClassName(
            EnumDistributed.Result + "_" + DistWorker.class.getName());
        resultParams.SetDateValue(
            EnumCalcCols.TimeStamp,
            DateTime.now().toDate());
        resultParams.SetObjValueToDict(
            EnumCalcCols.TsEvents,
            events);
        resultParams.SetStrValue(
            EnumCalcCols.WorkerId,
            m_distWorker.WorkerId);
        //String strParentId = calcParams.GetStrValue(EnumDistributed.ControllerId);
        resultParams.SetStrValue(
            EnumDistributed.ControllerId,
            strParentId);
        //strJobId = calcParams.GetStrValue(EnumDistributed.JobId);
        resultParams.SetStrValue(
            EnumDistributed.JobId,
            strJobId);
        return resultParams;
    }

    private boolean SendResult(
        ASelfDescribingClass resultObj,
        String strJobId)
    {
        ASelfDescribingClass response = null;
        try
        {
            SendResult(resultObj);
            DateTime prevTime = DateTime.now();
            DateTime sentStart = DateTime.now();
            int intDisconnectedTrials = 0;
            int intTrials = 0;

            while (response == null)
            {
            	response = m_jobsToSend.get(strJobId);

                if (Seconds.secondsBetween(prevTime, DateTime.now()).getSeconds() > 10 &&
                    response == null)
                {
                    String strMessage = "DistWorker is trying to send result to controller. Total time (secs) = [" +
                                        Seconds.secondsBetween(sentStart, DateTime.now()).getSeconds() + "]. JobId [" +
                                        strJobId + "] Tials [" + (intTrials++) + "]/" +
                                        "[" + TOTAL_TRIALS + "]";
                    DistGuiHelper.PublishWorkerLog(
                        m_distWorker,
                        strMessage);
                    
                    //
                    // check if controller is disconnected
                    //
                    try
                    {
                        String strControllerId = resultObj.GetStrValue(EnumDistributed.ControllerId);
                        if (m_distWorker.DistWorkerToContollerHeartBeat
                            .IsControllerDisconnected(strControllerId))
                        {
                            strMessage = "DistWorker cannot send result due to controller disconnection. Job [" +
                                strJobId + "]. Keep trying [" + intDisconnectedTrials + "]...";
                            DistGuiHelper.PublishWorkerLog(
                                m_distWorker,
                                strMessage);
                            intDisconnectedTrials++;
                        }
                        else
                        {
                            if (intDisconnectedTrials > 0)
                            {
                                //
                                // controller is reconnected. Send result again
                                //
                                SendResult(resultObj);
                                strMessage = "DistWorker re-sent result due to controller re-connection. Job [" +
                                    strJobId + "]";
                                DistGuiHelper.PublishWorkerLog(
                                    m_distWorker,
                                    strMessage);
                            }
                            intDisconnectedTrials = 0;
                        }
                        if (intDisconnectedTrials > DISCONNECTION_TRIALS ||
                            intTrials > TOTAL_TRIALS)
                        {
                            if (intDisconnectedTrials > DISCONNECTION_TRIALS)
                            {
                                strMessage =
                                    "DistWorker failed to send result due to controller disconnection. Job [" +
                                    strJobId + "]";
                            }
                            else
                            {
                                strMessage =
                                    "DistWorker failed to send result due to long wait and no response. Job [" +
                                    strJobId + "]";
                            }
                            DistGuiHelper.PublishWorkerLog(
                                m_distWorker,
                                strMessage);
                            return false;
                        }
                    }
                    catch (Exception ex)
                    {
                        Logger.log(ex);
                    }

                    prevTime = DateTime.now();
                }
                Thread.sleep(20);
            }
            return true;
        }
        catch (Exception ex)
        {
            Logger.log(ex);
            String strMessage = "DistWorker failed to send result due to exception. Job [" +
                strJobId + "]";
            DistGuiHelper.PublishWorkerLog(
                m_distWorker,
                strMessage);
            return false;
        }
        finally
        {
            m_jobsToSend.remove(strJobId);
        }
    }


    private void OnTopicControllerToWorkerConfirm(
        TopicMessage topicmessage)
    {
    	try{
        ASelfDescribingClass selfDescribingClass = (ASelfDescribingClass)topicmessage.EventData;
        
        String strWorkerId = selfDescribingClass.TryGetStrValue(EnumDistributed.WorkerId);
        if (StringHelper.IsNullOrEmpty(strWorkerId))
        {
            throw new HCException("Worker id not found");
        }

        if(!m_distWorker.WorkerId.equals(strWorkerId))
        {
            //
            // this message was sent to another worker
            //
            return;
        }

        String strJobId = selfDescribingClass.GetStrValue(EnumDistributed.JobId);
        Console.writeLine("Worker got confirmation from controller that result job [" + 
            strJobId  +"] has been recieved");
        m_jobsToSend.put(strJobId, selfDescribingClass);
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    }

    private void SendResult(Object resultParams)
    {
        TopicPublisherCache.GetPublisher(m_distWorker.ServerName).SendMessage(
            resultParams,
            m_distWorker.GridTopic + EnumDistributed.TopicWorkerToControllerResult.toString(),
            true);
    }

    public void Dispose()
    {
        if(m_jobsToSend != null)
        {
            m_jobsToSend.clear();
            m_jobsToSend = null;
        }
        m_distWorker = null;
    }
}
