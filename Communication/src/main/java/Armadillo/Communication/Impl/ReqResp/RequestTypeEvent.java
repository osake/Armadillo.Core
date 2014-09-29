package Armadillo.Communication.Impl.ReqResp;

import java.util.List;

import Armadillo.Core.Console;
import Armadillo.Core.Logger;

public class RequestTypeEvent {
	
	public GetObjectListDel OnGetObjectList;

    public List<Object> InvokeOnGetObjectList(
        RequestDataMessage requestDataMessage)
    {
        while (OnGetObjectList == null)
        {
            String strMessage = "Event handler not found";
            Console.writeLine(strMessage);
            Logger.log(strMessage);
            try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				Logger.log(ex);
			}
        }
        return OnGetObjectList.invoke(requestDataMessage);
    }
}
