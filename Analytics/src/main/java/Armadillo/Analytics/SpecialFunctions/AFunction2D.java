package Armadillo.Analytics.SpecialFunctions;

public abstract class AFunction2D {
	
    public double XMin;
    public double XMax;
    public double YMin;
    public double YMax;
	public String YLabel;

	public abstract void SetFunctionLimits();
}
