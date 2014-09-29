package Armadillo.Communication.Impl.ReqResp;

import java.util.List;
import Armadillo.Core.Config;
import Armadillo.Core.Logger;
import Armadillo.Core.Concurrent.Task;
import Armadillo.Core.Reflection.ReflectionCache;
import Armadillo.Core.Reflection.Reflector;
import Armadillo.Core.Serialization.Serializer;

public class RequestDataMessage {

    private static Reflector m_binder;
    private List<RequestDataMessage> m_response;
    private boolean m_isClientDisconnected;
    private Task m_tcs;
    public String Id;
    public Object Request;
    public String RequestorName;
    public int CallbackSize;
    public List<Object> Params;
    public List<Object> Response;
    public boolean IsAsync;
    public String Error;

    public RequestDataMessage()
    {
        RequestorName = Config.getClientName();
    }

    public void SetResponse(List<RequestDataMessage> response)
    {
        m_response = response;
    }

    public List<RequestDataMessage> GetResponse()
    {
        return m_response;
    }

    public void SetIsClientDisconnected(boolean blnValue)
    {
        m_isClientDisconnected = blnValue;
    }

    public boolean GetIsClientDisconnected()
    {
        return m_isClientDisconnected;
    }

    @Override
    public String toString()
    {
        LoadBinder();
        StringBuilder sb = new StringBuilder();
        boolean blnIsTitle = true;
        for (String strPropertyName : m_binder.getColNames())
        {
            if (!blnIsTitle)
            {
                sb.append(",\n");
            }
            else
            {
                blnIsTitle = false;
            }
            sb.append(strPropertyName + " = " +
                m_binder.getPropValue(this, strPropertyName));
        }
        return sb.toString();
    }

    public void Dispose()
    {
        if (m_response != null &&
            m_response.size() > 0)
        {
            for (int i = 0; i < m_response.size(); i++)
            {
                m_response.get(i).Dispose();
            }
        }
        if (Response != null &&
            Response.size() > 0)
        {
            Response.clear();
        }
    }

    private static void LoadBinder()
    {
        if (m_binder == null)
        {
            m_binder = ReflectionCache.getReflector(RequestDataMessage.class);
        }
    }
    
    public Task GetTcs()
    {
        return m_tcs;
    }

    public void SetTcs(Task tcs)
    {
        m_tcs = tcs;
    }

	public RequestDataMessage copy() {
		try {
			return (RequestDataMessage) clone();
		} catch (CloneNotSupportedException ex) {
			Logger.log(ex);
		}
		return null;
	}

	public byte[] getByteArr() {
		return Serializer.getBytes(this);
	}
}