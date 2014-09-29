package Armadillo.Core.Serialization;

public class EnumSerializedType{
	
	public static final EnumSerializedType EndOfProperties = new EnumSerializedType((byte)1);
	public static final EnumSerializedType DoubleType = new EnumSerializedType((byte)2);
	public static final EnumSerializedType Int32Type = new EnumSerializedType((byte)3);
	public static final EnumSerializedType BooleanType = new EnumSerializedType((byte)4);
	public static final EnumSerializedType DateTimeType = new EnumSerializedType((byte)5);
	public static final EnumSerializedType StringType = new EnumSerializedType((byte)6);
	public static final EnumSerializedType Int64Type = new EnumSerializedType((byte)7);
	public static final EnumSerializedType ObjectType = new EnumSerializedType((byte)8);
	public static final EnumSerializedType NullType = new EnumSerializedType((byte)9);
	public static final EnumSerializedType NonNullType = new EnumSerializedType((byte)10);
	
	public byte m_byteItem;

	public EnumSerializedType(byte byteItem){
		m_byteItem = byteItem;
	}

	@Override
	public boolean equals(Object o) {
		return ((EnumSerializedType)o).m_byteItem == m_byteItem; 
	}
}
