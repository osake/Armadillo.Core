package Armadillo.Communication.Impl.Topic;

import Armadillo.Core.Logger;

public class SubscriberCallbackDel {

	public void invoke(TopicMessage topicMessage) {
		
		try {
			throw new Exception();
		} catch (Exception e) {
			Logger.log(e);
		}
	}
}
