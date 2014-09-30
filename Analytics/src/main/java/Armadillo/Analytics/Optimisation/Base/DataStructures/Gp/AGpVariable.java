package Armadillo.Analytics.Optimisation.Base.DataStructures.Gp;

public abstract class AGpVariable
{
    public Object Value;

    public String VariableName;

    public AGpVariable()
    {
    }

    public AGpVariable(String name)
    {
        VariableName = name;
    }

    public void SetValue(Object value)
    {
        Value = value;
    }

    public Object GetValue()
    {
        return Value;
    }

    @Override
    public String toString()
    {
        return VariableName;
    }


    public abstract AGpVariable Clone();
}
