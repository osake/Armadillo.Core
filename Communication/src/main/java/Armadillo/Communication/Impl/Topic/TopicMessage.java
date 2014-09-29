package Armadillo.Communication.Impl.Topic;

import java.io.Closeable;
import java.io.IOException;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Core.Serialization.ISerializerReader;
import Armadillo.Core.Serialization.ISerializerWriter;
import Armadillo.Core.Serialization.Serializer;

public class TopicMessage implements Closeable {

	    public String TopicName;
	    public Object EventData;
	    public String PublisherName;
	    private String m_strConnectionName;
		public boolean IsDisposed;


	    public void SetConnectionName(String strConnectionName)
	    {
	        m_strConnectionName = strConnectionName;
	    }

	    public String GetConnectionName()
	    {
	        return m_strConnectionName;
	    }

	    public void Dispose()
	    {
	    	if(IsDisposed){
	    		return;
	    	}
	    	
	    	IsDisposed = true;
	        TopicName = null;
	        EventData = null;
	        PublisherName = null;
	        m_strConnectionName = null;
	    }

	    public static TopicMessage DeserializeStatic(byte[] bytes)
	    {
	        try
	        {
	            ISerializerReader serializerReader = Serializer.GetReader(bytes);
	            String strCurrTopic = serializerReader.ReadString();
	            TopicMessage topicMessage = new TopicMessage();
	            topicMessage.TopicName = strCurrTopic;
	            topicMessage.PublisherName = serializerReader.ReadString();
	            topicMessage.EventData = serializerReader.ReadObject();
	            return topicMessage;
	        }
	        catch (Exception ex)
	        {
	            Logger.log(ex);
	        }
	        return new TopicMessage();
	    }

	    public byte[] GetByteArr()
	    {
	        try
	        {
	            ISerializerWriter serializerWriter = Serializer.GetWriter();
	            serializerWriter.Write(TopicName);
	            serializerWriter.Write(PublisherName);
	            serializerWriter.Write(
	            		new ObjectWrapper(EventData));
	            byte[] bytes = serializerWriter.GetBytes();
	            return bytes;
	        }
	        catch (Exception ex)
	        {
	            Logger.log(ex);
	        }
	        return null;
	    }

		@Override
		public void close() throws IOException {
			Dispose();
		}	
}
