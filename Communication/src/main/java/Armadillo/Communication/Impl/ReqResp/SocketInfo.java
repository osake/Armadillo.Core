package Armadillo.Communication.Impl.ReqResp;

import Armadillo.Core.NetworkHelper;

public class SocketInfo {
    public String DNS;
    public int Port;

    public String GetConnectionUrl()
    {
        String strIp = NetworkHelper.GetIpAddr(DNS);
        return "tcp://" + strIp + ":" + Port;
    }

    @Override
    public String toString()
    {
        return GetConnectionUrl();
    }
}
