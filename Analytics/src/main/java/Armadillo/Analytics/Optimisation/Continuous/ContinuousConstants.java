package Armadillo.Analytics.Optimisation.Continuous;

public class ContinuousConstants 
{
    /// <summary>
    ///   DE mutation factor
    /// </summary>
    public static final double DBL_DE_MUATION_FACTOR = 0.8;

    /// <summary>
    ///   Probability of reproducing an individual 
    ///   via differential evolution
    /// </summary>
    public static final double DBL_DE_REPRODUCTION = 0.9;

    /// <summary>
    ///   DE convergence value
    /// </summary>
    public static final int INT_DE_CONVERGENCE = 40000;

    public static final int INT_DE_SMALL_CONVERGENCE = 40;

    public static final int INT_NM_CONVERGENCE = 4000;

    /// <summary>
    ///   Differential evolution small convergence.
    ///   Value used for small problems
    /// </summary>
    public static final int INT_SMALL_PROBLEM_DE = 100;

    /// <summary>
    ///   Simplex model name
    /// </summary>
    public static final String STR_SIMPLEX_MODEL_FILENAME =
        "SimplexModel.mod";

}
