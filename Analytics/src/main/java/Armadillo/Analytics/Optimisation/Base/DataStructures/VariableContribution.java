package Armadillo.Analytics.Optimisation.Base.DataStructures;

/// <summary>
///   Hold a variable and its contribution.
///   In some cases it is important to sort variables 
///   by their contribution to the objective function.
/// 
///   Note: This class is not threadsafe.
/// </summary>
public class VariableContribution
{
    /// <summary>
    ///   Contribution value
    /// </summary>
    public double Contribution;

    /// <summary>
    ///   Variable index
    /// </summary>
    public int Index;

    /// <summary>
    ///   Constructor
    /// </summary>
    /// <param name = "intIndex">
    ///   Variable index
    /// </param>
    /// <param name = "dblContribution">
    ///   Contribution value
    /// </param>
    public VariableContribution(
        int intIndex,
        double dblContribution)
    {
        Index = intIndex;
        Contribution = dblContribution;
    }

    /// <summary>
    ///   Check if a variable is equals by 
    ///   comparing their indexes.
    /// </summary>
    /// <param name = "obj">
    ///   Object to compare with
    /// </param>
    /// <returns>
    ///   True if the two objects are equal. Zero otherwise.
    /// </returns>
    public boolean Equals(VariableContribution obj)
    {
        return obj.Index == Index;
    }
}
