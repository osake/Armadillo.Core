package Armadillo.Communication.Impl.Distributed;

import java.util.ArrayList;
import java.util.UUID;

import Armadillo.Core.Config;
import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Communication.Impl.SimpleUiSocket;
import Armadillo.Communication.Impl.ReqResp.ARequestResponseClient;
import Armadillo.Communication.Impl.ReqResp.RequestDataMessage;
import Armadillo.Communication.Impl.ReqResp.ZmqRequestResponseClient;
import Armadillo.Core.SelfDescribing.ASelfDescribingClass;
import Armadillo.Core.SelfDescribing.SelfDescribingClass;

public class DefaultDistributedController 
{
	private static DistController m_controller;
	private static int intReqRespPort;
	private static int intTimeOutSeconds = Integer.MAX_VALUE;
	private static String strServerName;
	private static ARequestResponseClient m_reqRespClientConnection;
	
	static {
		
		try{
			intReqRespPort = Integer.parseInt(Config.getStringStatic(
					"ReqRespPort",
					SimpleUiSocket.class));
			strServerName = Config.getStringStatic(
					"TopicServerName",
					SimpleUiSocket.class);
	        while(m_reqRespClientConnection == null){
				m_reqRespClientConnection = ZmqRequestResponseClient.GetConnection(
						strServerName, 
						intReqRespPort);
				if(m_reqRespClientConnection == null){
					String strMessage = "[" + DefaultDistributedController.class.getName() +
							"]. ReqResp client connection not ready.";
					Logger.log(strMessage);
					Console.writeLine(strMessage);
					Thread.sleep(3000);
				}
	        }
		}
		catch(Exception ex){
			Logger.log(ex);
		}
	}
	
	public static Object runDistributedViaService(
			String strMethodName, 
			Class<?> classToRun, 
			ArrayList<Object> params) {
		
		try{
			
			RequestDataMessage requestDataMessage = new RequestDataMessage();
			requestDataMessage.Id = UUID.randomUUID().toString();
			requestDataMessage.Request = 
					DefaultDistributedController.class.getName() + "|runDistributed";
			requestDataMessage.Params = new ArrayList<Object>();
			requestDataMessage.Params.add(strMethodName);
			requestDataMessage.Params.add(classToRun);
			requestDataMessage.Params.add(params);
			
			String strMessage = "[" + DefaultDistributedController.class.getName() + 
					"]. Attempt to send request...";
			Console.writeLine(strMessage);
			Logger.log(strMessage);
			
			ASelfDescribingClass response = 
					(ASelfDescribingClass)m_reqRespClientConnection.SendRequestAndGetResponse(
						requestDataMessage, 
						intTimeOutSeconds).get(0);
			
			return response.GetObjValue("TsEvents");
		}
		catch(Exception ex){
			Logger.log(ex);
		}
		return null;
	}
	
	public static ASelfDescribingClass runDistributed(
			String strMethodName, 
			Class<?> classToRun, 
			ArrayList<Object> params) {
		
		try{
			if(m_controller == null){
		        String strTopicFromConfig = Config.getStringStatic(
		        		"CalcTopic",
		        		SimpleUiSocket.class);
				m_controller = DistController.GetController(strTopicFromConfig);				
			}
			ASelfDescribingClass paramsClass = new SelfDescribingClass();
			paramsClass.SetClassName("CalcClassParams");
			paramsClass.SetStrValue(EnumDistributed.JobId, UUID.randomUUID().toString());
			paramsClass.SetStrValue("Request", classToRun.getName() + "|" + strMethodName);
			paramsClass.SetObjValueToDict("Params", params);
			ASelfDescribingClass result = m_controller.DoWork(paramsClass);
			return result;
		}
		catch(Exception ex){
			Logger.log(ex);
		}
		return null;
	}
}
