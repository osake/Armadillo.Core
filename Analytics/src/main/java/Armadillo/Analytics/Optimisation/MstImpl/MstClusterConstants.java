package Armadillo.Analytics.Optimisation.MstImpl;

public class MstClusterConstants 
{
    /// <summary>
    /// Difference between the maximum score and 
    /// the actual score
    /// </summary>
    public static final double NODE_DEGREES_THRESHOLD = 0.15;
    public static final double BRANCH_LENGHT_THRESHOLD = 0.7;
    public static final double ADJACENT_NODE = 0.01;
    public static final double BRANCH_SIZE_THRESHOLD = 0.1;
    public static final double EDGE_THRESHOLD = 0.65;
    public static final int PURGER_SEARCH_LENGTH = 1000;
}
