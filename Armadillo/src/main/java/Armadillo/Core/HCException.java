package Armadillo.Core;

public class HCException extends IllegalArgumentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HCException() 
	{
		this("");
	}

	public HCException(String string) 
	{
		super(string);
	}

	public static void ThrowIfTrue(boolean blnCondition, String string) throws HCException 
	{
		if(blnCondition)
		{
			throw new HCException(string);
		}
	}

}
