package Armadillo.Communication.Impl.ReqResp;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Core.HCException;
import Armadillo.Core.Logger;

public class ReqRespHelper {
    public static ArrayList<ArrayList<Object>> GroupList(
            List<Object> list,
            int intGroupSize)
        {
            if (intGroupSize == 0)
            {
                try {
					throw new HCException("Invalid request size");
				} catch (HCException ex) {
					Logger.log(ex);
				}
            }

            if (list == null || list.size() == 0)
            {
                return new ArrayList<ArrayList<Object>>();
            }
            ArrayList<ArrayList<Object>> groups = new ArrayList<ArrayList<Object>>();
            ArrayList<Object> currList = new ArrayList<Object>();
            for (Object o : list)
            {
                currList.add(o);

                if (currList.size() >= intGroupSize)
                {
                    groups.add(currList);
                    currList = new ArrayList<Object>();
                }
            }
            //
            // add last list
            //
            if (currList.size() > 0)
            {
                groups.add(currList);
            }
            return groups;
        }

        public static List<RequestDataMessage> GetListOfResponses(
            RequestDataMessage response)
        {
            ArrayList<ArrayList<Object>> groups = GroupList(
                response.Response,
                response.CallbackSize);
            ArrayList<RequestDataMessage> responseList = new ArrayList<RequestDataMessage>();
            for (ArrayList<Object> list : groups)
            {
                try
                {
                	RequestDataMessage requestDataMessage = new RequestDataMessage();
                	requestDataMessage.Request = response.Request;
                	requestDataMessage.Response = list;
                	requestDataMessage.CallbackSize =
                    response.CallbackSize;
                    requestDataMessage.Id = response.Id;
                    //requestDataMessage.RequestType = EnumRequestType.DataProvider;
                    responseList.add(requestDataMessage);
                }
                catch (Exception ex)
                {
                    Logger.log(ex);
                    break;
                }
            }

            return responseList;
        }

}
