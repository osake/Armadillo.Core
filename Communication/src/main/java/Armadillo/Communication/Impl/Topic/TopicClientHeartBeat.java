package Armadillo.Communication.Impl.Topic;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import Armadillo.Core.Logger;
import Armadillo.Communication.Impl.NotifierDel;
import Armadillo.Communication.Impl.ReqResp.EnumReqResp;

public class TopicClientHeartBeat {
	
        public static boolean DoNotPing;
        public static ConcurrentHashMap<String, TopicClientHeartBeatThreadWorker> HeartBeatWorker;
        private static Object m_lockObject = new Object();
        private static ConcurrentHashMap<String, Object> m_serversConnected;
        public static ArrayList<NotifierDel> OnDisconnectedState = 
        		new ArrayList<NotifierDel>();

        static {
            HeartBeatWorker = new ConcurrentHashMap<String, TopicClientHeartBeatThreadWorker>();
            m_serversConnected = new ConcurrentHashMap<String, Object>();
        }

        public static boolean IsConnected(String strServerName)
        {
            TopicClientHeartBeatThreadWorker heartBeatWorker = 
            		HeartBeatWorker.get(strServerName);
            if(heartBeatWorker != null)
            {
                return heartBeatWorker.ConnectionState == EnumReqResp.Connected;
            }
            return false;
        }

        public static void StartHeartBeat(String strServerName)
        {
            if(DoNotPing)
            {
                return;
            }
            if (!m_serversConnected.containsKey(strServerName))
            {
                synchronized (m_lockObject)
                {
                    if (!m_serversConnected.containsKey(strServerName))
                    {
                        m_serversConnected.put(strServerName, new Object());
                        
                        NotifierDel connectionStateDel = new NotifierDel(){
                        	@Override
                        	public void invoke(String strServerName){
                        		InvokeOnDisconnectedState(strServerName);
                        	}
                        };
                        
                        HeartBeatWorker.put(strServerName,  
                            new TopicClientHeartBeatThreadWorker(
                            		strServerName,
                            		connectionStateDel));
                        Logger.log("Started " + TopicClientHeartBeat.class.getName() + " heart beat");
                    }
                }
            }
        }

        private static void InvokeOnDisconnectedState(String strservername)
        {
            if (DoNotPing)
            {
                return;
            }
            if (OnDisconnectedState.size() > 0)
            {
            	for (int i = 0; i < OnDisconnectedState.size(); i++) {
            		OnDisconnectedState.get(i).invoke(strservername);					
				}
            }
        }

}
