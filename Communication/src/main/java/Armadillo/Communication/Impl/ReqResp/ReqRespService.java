package Armadillo.Communication.Impl.ReqResp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import Armadillo.Core.Console;
import Armadillo.Core.ListWrapper;
import Armadillo.Core.Logger;

public class ReqRespService {
    
	private static boolean m_blnIsConnectred;
    private static final Object m_connectionLock = new Object();
    public static RequestTypeEvent Callbacks;

    static
    {
        Connect();
    }

    public static void Connect()
    {
        if (m_blnIsConnectred)
        {
            return;
        }
        synchronized (m_connectionLock)
        {
            if (m_blnIsConnectred)
            {
                return;
            }
            
            Callbacks = new RequestTypeEvent();
            Callbacks.OnGetObjectList = new GetObjectListDel(){
            	
            	@Override
            	public List<Object> invoke(RequestDataMessage requestdatamessage){
            		return ExecuteMethod(requestdatamessage);
            	}
            };
            String strMessage = ReqRespService.class.getName() + ". Service is ready";
            Logger.log(strMessage);
            Console.writeLine(strMessage);
            m_blnIsConnectred = true;
        }
    }

    private static List<Object> ExecuteMethod(
    		RequestDataMessage requestDataMessage)
    {
    	return ExecuteMethod(
    			requestDataMessage.Request.toString(),
    			requestDataMessage.Params);
    }
    
    public static List<Object> ExecuteMethod(
    		String strMethodId,
    		List<Object> params)
    {
    	try{
	    	String[] requestToks = strMethodId.toString().split("\\|", -1);
	    	@SuppressWarnings("rawtypes")
			Class c = Class.forName(requestToks[0]);
	    	@SuppressWarnings("rawtypes")
			Class[] paramsTypes = null;
	    	if(params != null &&
	    			params.size() > 0){
	    		
	    		paramsTypes = new Class[params.size()];
		    	for (int i = 0; i < params.size(); i++) {
		    		paramsTypes[i] = params.get(i).getClass(); 
				}
	    	}
			@SuppressWarnings("unchecked")
			Method method = c.getMethod(requestToks[1],paramsTypes);
			Object result = method.invoke(null, params.toArray());
			new ArrayList<Object>();
			if(result instanceof ListWrapper){
				return ((ListWrapper) result).List;
			}
			ArrayList<Object> resultList = new ArrayList<Object>();
			resultList.add(result);
	        return resultList;
    	}
    	catch(Exception ex){
    		Logger.log(ex);
    	}
    	return new ArrayList<Object>();
    }

    public RequestDataMessage RequestDataOperation(RequestDataMessage transferMessage)
    {
        return InvokeRequestEventHandler(transferMessage);
    }

    public static RequestDataMessage InvokeRequestEventHandler(RequestDataMessage requestDataMessage)
    {
        requestDataMessage.Response = Callbacks.InvokeOnGetObjectList(requestDataMessage);
        return requestDataMessage;
    }

}
