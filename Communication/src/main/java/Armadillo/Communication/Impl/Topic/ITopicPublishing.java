package Armadillo.Communication.Impl.Topic;


public interface ITopicPublishing {

	void Reconnect();

	void Publish(TopicMessage topicMessage);

}
