package Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.IndividualFactories;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;

public interface IIndividualFactory
{
    /// <summary>
    ///   Build a random individual
    /// </summary>
    /// <returns></returns>
    Individual BuildRandomIndividual();
}
