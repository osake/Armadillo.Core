package Armadillo.Core.UI;

public abstract class AGuiCallback 
{
	public abstract void OnStr(String str);
	
	public abstract boolean PublishTableRow(
			String str1, 
			String str2,
			String str3, 
			String strObjectKey,
			Object obj);
	
	public abstract boolean PublishLineChartRow(
			String str1, 
			String str2,
			String str3, 
			String strSeries, 
			String strX, 
			double dblY);
}
