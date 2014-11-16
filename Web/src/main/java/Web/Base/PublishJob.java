package Web.Base;

public class PublishJob 
{
	public String str1;
	public String str2;
	public String str3; 
	public String strObjectKey;
	public String[] cols;
	public String[] vals;
	
	public void dispose() 
	{
		str1 = null;
		str2 = null;
		str3 = null;
		strObjectKey = null;
		cols = null;
		vals = null;
	}
}
