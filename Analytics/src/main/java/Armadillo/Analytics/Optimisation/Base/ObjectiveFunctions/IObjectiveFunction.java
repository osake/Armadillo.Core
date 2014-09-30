package Armadillo.Analytics.Optimisation.Base.ObjectiveFunctions;

public interface IObjectiveFunction
{
	Armadillo.Analytics.Optimisation.Base.ObjectiveFunctionType ObjectiveFunctionType();

    /// <summary>
    ///   Count the number of variables
    /// </summary>
    /// <returns></returns>
    int VariableCount();

    double Evaluate();

    /// <summary>
    ///   Get the description of a given variable
    /// </summary>
    /// <param name = "intIndex">
    ///   Variable index
    /// </param>
    /// <returns>
    ///   Variable description
    /// </returns>
    String GetVariableDescription(int intIndex);
    
    void Dispose();
}
