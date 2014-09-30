package Armadillo.Analytics.Optimisation.Continuous.Operators.Mutation;

import Armadillo.Analytics.Base.MathConstants;
import Armadillo.Analytics.Optimisation.Base.OptimisationConstants;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public class DeMutationHelper
{
    /// <summary>
    ///   In order to avoid convergence into a local optimal,
    ///   allow the mutation factor to take random values when
    ///   the mutation is very close to zero
    /// </summary>
    /// <param name = "rng"></param>
    /// <param name = "dblMutation"></param>
    /// <returns></returns>
    public static double ValidateMutationFactor(
        RngWrapper rng,
        double dblMutation)
    {
        // validate mutation factor
        if (Math.abs(dblMutation) <=
            MathConstants.DBL_ROUNDING_FACTOR)
        {
            double dblSymbol;
            if (dblMutation == 0)
            {
                dblSymbol = 1.0;
            }
            else
            {
                dblSymbol = dblMutation/Math.abs(dblMutation);
            }
            if (rng.nextDouble() >=
                OptimisationConstants.DBL_FORCE_MUATION)
            {
                dblMutation = dblSymbol*
                              OptimisationConstants.DBL_MUATION_FACTOR;
            }
            else
            {
                if (rng.nextDouble() >=
                    OptimisationConstants.DBL_FORCE_MUATION)
                {
                    dblMutation = dblSymbol*
                                  MathConstants.DBL_ROUNDING_FACTOR;
                }
            }
        }
        return dblMutation;
    }
}
