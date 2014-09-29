package Armadillo.Communication.Impl.Topic;

import Armadillo.Communication.Impl.NotifierDel;

public interface ITopicSubscriber
{
    void Dispose();
    void Connect(
        String strServerName,
        int intPort);

    void Subscribe(
        String strTopic,
        SubscriberCallbackDel subscriberCallback);

    boolean IsSubscribedToTopic(String strTopic);

    void UnSubscribe(String strTopic);
    void Publish(TopicMessage topicMessage);

    int SubscriberCount(String strTopic);

    void NotifyDesconnect(
        String strTopic,
        NotifierDel notifierDel);
}