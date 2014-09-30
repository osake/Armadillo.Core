package Armadillo.Analytics.Optimisation.Base;

public class OptimisationConstants 
{
    public static final double LOCAL_SEARCH_POPULATON_FACTOR = 0.3;

    //
    // progress bar percentages
    // values must sum up to one
    //

    // converge factor
    /// <summary>
    ///   Crossover probability
    /// </summary>
    public static final double CROSSOVER_PROB = 0.25;

    /// <summary>
    ///   Cheap vector metric alpha parameter
    /// </summary>
    public static final double DBL_CHEAP_METRIC_ALPHA = 0.7;

    /// <summary>
    ///   Cheap similarity threshold
    /// </summary>
    public static final double DBL_CHEAP_THRESHOLD = 0.1;

    public static final double DBL_CONVERGENCE_FACTOR = 0.9;

    //
    // clustering constants
    //
    /// <summary>
    ///   Expensive similarity threshold
    /// </summary>
    public static final double DBL_EXPENSIVE_THRESHOLD = 0.8;

    /// <summary>
    ///   Extensive local search probability
    /// </summary>
    public static final double DBL_EXTENSIVE_LOCAL_SEARCH = 0.30;

    /// <summary>
    ///   Probability of force mutation operator
    /// </summary>
    public static final double DBL_FORCE_MUATION = 0.9;

    // Guided mutation constants

    /// <summary>
    ///   Guided mutation beta
    /// </summary>
    public static final double DBL_GM_BETA = 0.9;

    /// <summary>
    ///   Guided mutation alpha
    /// </summary>
    public static final double DBL_GM_LAMBDA = 0.5;

    /// <summary>
    ///   Local search probability
    /// </summary>
    public static final double DBL_LOCAL_SEARCH = 0.25;

    /// <summary>
    ///   Mutation factor
    /// </summary>
    public static final double DBL_MUATION_FACTOR = 0.001;

    /// <summary>
    ///   Probability of mutation for a continuous problem
    /// </summary>
    public static final double DBL_MUTATION_CONTINUOUS = 0.8;


    /// <summary>
    ///   Select guided mutation operation
    /// </summary>
    public static final double DBL_USE_GM = 0.6;

    /// <summary>
    ///   Number of Local search iterations to be executed before
    ///   expensive local search kicks in
    /// </summary>
    public static final int EXPENSIVE_LOCAL_SERCH_ITERATIONS = 10;

    public static final double GUIDED_MUTATION_REPRODUCTION_PROB = 0.1;

    /// <summary>
    ///   Number of variables to be evaluated by the 
    ///   cheap vector metric
    /// </summary>
    public static final int INT_CHEAP_METRIC_WINDOW_SIZE = 1;

    /// <summary>
    ///   Parent size
    /// </summary>
    //public static final int INT_LARGE_POPULATION_SIZE = 100;
    public static final int INT_FINAL_RESULTS_PERCENTAGE = 5;

    /// <summary>
    ///   GA convergence value
    /// </summary>
    public static final int INT_GA_CONVERGENCE = 40000;

    public static final int INT_GA_SMALL_CONVERGENCE = 50;

    /// <summary>
    ///   Guided mutation population size
    /// </summary>
    public static final int INT_GM_POPULATION = 10;

    public static final int INT_INITAL_POOL_PERCENTAGE = 3;
    public static final int INT_POPULATION_SIZE = 100;

    /// <summary>
    ///   GA convergence value used for small problems
    /// </summary>
    public static final int INT_SMALL_PROBLEM_GA = 30;

    public static final int INT_SOLVER_PERCENTAGE = 85;
    public static final int INT_THREADS = 0;

    public static final double MUTATION_PROBABILITY = 0.5;

    /// <summary>
    ///   Average number of chromosomes to be mutated
    /// </summary>
    public static final double MUTATION_RATE = 0.13;


    /// <summary>
    ///   Maximum likelihood to be considered in order to 
    ///   go into one direcion in the search
    /// </summary>
    public static final double SEARCH_DIRECTION_LIKELIHOOD = 0.8;

    public static final double STD_REPRODUCTION_PROB = 0.9;
    public static int INT_INITAL_POPULATION_PERCENTAGE = 5;
    public static int INT_LOAD_DATA_PERCENTAGE = 2;
}
