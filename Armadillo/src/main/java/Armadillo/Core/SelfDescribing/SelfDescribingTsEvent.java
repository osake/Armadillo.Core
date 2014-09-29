package Armadillo.Core.SelfDescribing;

import org.joda.time.DateTime;

import Armadillo.Core.Math.ITsEvent;
import Armadillo.Core.Math.TsDataRequest;
import Armadillo.Core.Serialization.ISerializerReader;
import Armadillo.Core.Serialization.ISerializerWriter;
import Armadillo.Core.Serialization.Serializer;
import Armadillo.Core.Serialization.SerializerReader;


public class SelfDescribingTsEvent extends ASelfDescribingClass implements ITsEvent 
{
    public DateTime Time;
    public TsDataRequest TsDataRequest;

    public SelfDescribingTsEvent()
    {
    	super("");
    }

    public SelfDescribingTsEvent(@SuppressWarnings("rawtypes") Enum enumValue)
    {
    	this(enumValue.toString());
    }

    public SelfDescribingTsEvent(String strClassName)
    {
    	super(strClassName);
    }

    @Override 
    public void Serialize(ISerializerWriter writerBase)
    {
        writerBase.Write(m_strClassName);
        ISerializerWriter serializer = SerializeProperties();
        writerBase.Write(serializer.GetBytes());
    }

    @Override
    public Object Deserialize(byte[] bytes)
    {
        SelfDescribingTsEvent selfDescribingClass = new SelfDescribingTsEvent();
        ISerializerReader serializationReader = Serializer.GetReader(bytes);
        String strClassName = serializationReader.ReadString();
        selfDescribingClass.SetClassName(strClassName);
        byte[] propertyBytes = serializationReader.ReadByteArray();
        DeserializeProperties(selfDescribingClass, new SerializerReader(propertyBytes));
        return selfDescribingClass;
    }

	@SuppressWarnings("unchecked")
	@Override
	public Object GetHardPropertyValue(String strFieldName) {
		return null;
	}

	@Override
	public DateTime getTime() {
		return Time;
	}

	@Override
	public void setTime(DateTime dateTime) {
		Time = dateTime;
	}
}