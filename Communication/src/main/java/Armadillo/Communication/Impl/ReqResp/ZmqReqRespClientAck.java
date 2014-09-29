package Armadillo.Communication.Impl.ReqResp;


public class ZmqReqRespClientAck {
    
	//private final ConcurrentHashMap<String, Object> m_jobsDone;

    public ZmqReqRespClientAck(ZmqReqRespClientSocketWrapper zmqReqRespClientSocketWrapper)
    {
        //m_jobsDone = new ConcurrentHashMap<String, Object>();
        //TopicSubscriberCache.GetSubscriber(zmqReqRespClientSocketWrapper.EndPointAddr.DNS).Subscribe(
        //    EnumReqResp.ServerToClientReqRespAck.ToString(),
        //    OnServerToClientReqRespAck);
    }

//    private void OnServerToClientReqRespAck(TopicMessage topicmessage)
//    {
//        String strJobId = (String)topicmessage.EventData;
//        if(m_jobsDone.containsKey(strJobId))
//        {
//            SendJobAck(strJobId);
//        }
//    }

    public void SendJobAck(String strJobId)
    {
    }
}
