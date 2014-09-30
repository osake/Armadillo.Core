package Armadillo.Analytics.Optimisation.Base.Operators.Mutation;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public abstract class AMutation implements IMutation
{
    protected HeuristicProblem m_heuristicProblem;

    public AMutation(HeuristicProblem heuristicProblem)
    {
        m_heuristicProblem = heuristicProblem;
    }

    public abstract Individual DoMutation(
        Individual individual);
}
