package Armadillo.Analytics.Optimisation.Base.DataStructures.Gp;

public class GpOperatorsContainer
{
    public int MaxTreeDepth;

    public AGpOperator[] GpOperatorArr;

    public GpConstants[] GpConstantArr;

    public AGpVariable GpVariable;

    public AGpVarNodeFactory GpVarNodeFactory;

    public AGpOperatorNodeFactory GpOperatorNodeFactory;

    public double CrossoverProbability;

    public int MaxTreeDepthMutation;

    public int TournamentSize;

    public int MaxTreeSize;

    public int TimeHorizon;

    public ANodeEvaluator NodeEvaluator;

    /// <summary>
    /// Used for serialization
    /// </summary>
    public GpOperatorsContainer()
    {
    }

    public GpOperatorsContainer(
        AGpOperator[] abstractGpOperators,
        GpConstants[] constants,
        AGpVariable gpVariable,
        int intMaxTreeDepth,
        double dblCrossoverProbability,
        int intMaxTreeDepthMutation,
        int intMaxTreeSize,
        int intTournamentSize,
        int intTimeHorizon)
    {
        GpOperatorArr = abstractGpOperators;
        GpConstantArr = constants;
        GpVariable = gpVariable;
        MaxTreeDepth = intMaxTreeDepth;
        CrossoverProbability = dblCrossoverProbability;
        MaxTreeDepthMutation = intMaxTreeDepthMutation;
        MaxTreeSize = intMaxTreeSize;
        TournamentSize = intTournamentSize;
        TimeHorizon = intTimeHorizon;
    }

    public void Dispose()
    {
        
    }
}
