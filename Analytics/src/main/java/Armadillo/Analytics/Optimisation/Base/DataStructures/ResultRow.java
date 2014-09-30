package Armadillo.Analytics.Optimisation.Base.DataStructures;

import java.util.List;

/// <summary>
///   Solver results are stored in this class.
///   Note: This class in not threadsafe.
/// </summary>
public class ResultRow implements Comparable<ResultRow>
{
    /// <summary>
    ///   Chromosome
    /// </summary>
    public double[] ChromosomeArray;

    /// <summary>
    ///   Average annual loss (Aal)
    /// </summary>
    public double Aal;

    /// <summary>
    ///   Tail conditional expectation (Tce)
    /// </summary>
    public double Tce;

    /// <summary>
    ///   Return period loss (Rpl)
    /// </summary>
    public double Rpl;

    /// <summary>
    ///   years to consider for a certain loss
    /// </summary>
    public double ReturnPeriod;

    /// <summary>
    ///   return
    /// </summary>
    public double Return;

    /// <summary>
    ///   list of exposures
    /// </summary>
    public List<String> ExposureList;

    /// <summary>
    ///   initial risk
    /// </summary>
    public double BaseRisk;

    /// <summary>
    ///   Standalone risk
    /// </summary>
    public double StandaloneRisk;

    /// <summary>
    ///   Marginal risk
    /// </summary>
    public double MarginalRisk;

    /// <summary>
    ///   Constructor
    /// </summary>
    /// <param name = "dblAal">
    ///   Average Annual Loss value
    /// </param>
    /// <param name = "dblTce">
    ///   Tail Conditional Expectation value
    /// </param>
    /// <param name = "dblRpl">
    ///   Return Period Loss
    /// </param>
    /// <param name = "dblReturnPeriod">
    ///   Return Period (years)
    /// </param>
    /// <param name = "dblFitness">
    ///   Return
    /// </param>
    /// <param name = "policyList">
    ///   List of policies
    /// </param>
    /// <param name = "dblBaseRisk">
    ///   Base risk
    /// </param>
    /// <param name = "dblStandaloneRisk">
    ///   Risk alone for current policy
    /// </param>
    /// <param name = "dblMarginalRisk">
    ///   Marginal Risk
    /// </param>
    /// <param name = "dblChromosomeArray">
    ///   Chromosome
    /// </param>
    public ResultRow(
        double dblAal,
        double dblTce,
        double dblRpl,
        double dblReturnPeriod,
        double dblFitness,
        List<String> policyList,
        double dblBaseRisk,
        double dblStandaloneRisk,
        double dblMarginalRisk,
        double[] dblChromosomeArray)
    {
        Aal = dblAal;
        Tce = dblTce;
        Rpl = dblRpl;
        ReturnPeriod = dblReturnPeriod;
        Return = dblFitness;
        ExposureList = policyList;
        BaseRisk = dblBaseRisk;
        StandaloneRisk = dblStandaloneRisk;
        MarginalRisk = dblMarginalRisk;
        ChromosomeArray = dblChromosomeArray;
    }

    /// <summary>
    ///   compare method used for sorting the results
    /// </summary>
    /// <param name = "o">
    ///   Result object to compare with
    /// </param>
    /// <returns>
    ///   Compare value
    /// </returns>
    public int compareTo(ResultRow o)
    {
        double difference = Return - o.Return;
        if (difference < 0)
        {
            return 1;
        }
        if (difference > 0)
        {
            return -1;
        }
        return 0;
    }

}