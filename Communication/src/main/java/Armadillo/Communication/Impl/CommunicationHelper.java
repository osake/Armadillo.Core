package Armadillo.Communication.Impl;

import Armadillo.Core.Config;

public class CommunicationHelper {
    
	public static String GetTopicPublisher(String strServerName)
    {
        String strTopicPublisher =
            Config.getStringStatic(
                "TopicPublisher",
                SimpleUiSocket.class);
        return strTopicPublisher.replace("[server]", strServerName);
    }
}
