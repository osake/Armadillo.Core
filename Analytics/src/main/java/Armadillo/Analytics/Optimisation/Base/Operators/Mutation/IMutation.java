package Armadillo.Analytics.Optimisation.Base.Operators.Mutation;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;

public interface IMutation
{
    Individual DoMutation(
        Individual individual);
}

