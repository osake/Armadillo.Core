package Armadillo.Core.Serialization;

import java.util.Date;

public interface ISerializerWriter {

	public abstract void Write(byte value);

	public abstract void Write(short value);

	public abstract void Write(int value);

	public abstract void Write(long value);

	public abstract void Write(double value);

	public abstract void Write(float value);

	public abstract void Write(boolean value);

	public abstract void Write(Date value);

	public abstract void Write(String value);

	public abstract void Write(Object obj);

	public abstract void Write(byte[] obj);
	
	public abstract byte[] GetBytes();

}