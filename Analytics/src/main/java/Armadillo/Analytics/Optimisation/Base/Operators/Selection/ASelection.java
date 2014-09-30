package Armadillo.Analytics.Optimisation.Base.Operators.Selection;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public abstract class ASelection implements ISelection
{
     protected HeuristicProblem m_heuristicProblem;

     protected ASelection(
        HeuristicProblem heuristicProblem)
    {
        m_heuristicProblem = heuristicProblem;
    }

     public abstract Individual DoSelection();
}