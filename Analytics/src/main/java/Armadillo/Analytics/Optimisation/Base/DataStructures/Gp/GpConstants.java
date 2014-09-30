package Armadillo.Analytics.Optimisation.Base.DataStructures.Gp;

public class GpConstants
{
    public static final double FULL_TREE_PROB = 0.5;

    public String GpConstantName;

    public Object Value;

    public GpConstants()
    {
    }

    public GpConstants(Object value, String name)
    {
        Value = value;
        GpConstantName = name;
    }

    public void SetValue(Object value)
    {
        Value = value;
    }

    public Object GetValue()
    {
        return Value;
    }

    public void ToStringB(StringBuilder sb)
    {
        sb.append(GpConstantName);
    }

    @Override
    public String toString()
    {
        return GpConstantName;
    }
}
