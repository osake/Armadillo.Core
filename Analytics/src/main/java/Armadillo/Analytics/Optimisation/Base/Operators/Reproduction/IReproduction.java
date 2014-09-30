package Armadillo.Analytics.Optimisation.Base.Operators.Reproduction;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

/// <summary>
///   Reproduction interface
/// </summary>
public interface IReproduction
{
    /// <summary>
    ///   Likelihood of selecting current reproduction operator
    /// </summary>
    double ReproductionProb();

    HeuristicProblem HeuristicProblem();

    /// <summary>
    ///   Reproducte individual
    /// </summary>
    /// <returns>
    ///   New individual
    /// </returns>
    Individual DoReproduction();

    /// <summary>
    ///   Cluster individual
    /// </summary>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    void ClusterInstance(
        Individual individual);

	void Dispose();
}
