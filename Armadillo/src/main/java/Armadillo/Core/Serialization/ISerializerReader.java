package Armadillo.Core.Serialization;

import java.lang.reflect.Type;
import java.util.Date;

public interface ISerializerReader {

	public abstract short ReadInt16();

	public abstract int ReadInt32();

	public abstract long ReadInt64();

	public abstract float ReadSingle();

	public abstract double ReadDouble();

	public abstract boolean ReadBoolean();

	public abstract Date ReadDateTime();

	public abstract String ReadString();

	public abstract Object ReadObject();

	public abstract byte[] ReadByteArray();

	public abstract int getBytesRemaining();

	public abstract byte readByte();

	public abstract Type readType();

}