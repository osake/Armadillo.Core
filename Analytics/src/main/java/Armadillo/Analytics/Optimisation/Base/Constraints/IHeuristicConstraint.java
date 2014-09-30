package Armadillo.Analytics.Optimisation.Base.Constraints;

import java.util.List;

import Armadillo.Analytics.Optimisation.Base.DataStructures.VariableContribution;

public interface IHeuristicConstraint extends IConstraint
{
    /// <summary>
    ///   Variables are ranked by the constribution given
    ///   by evaluating the constraint.
    /// </summary>
    /// <returns></returns>
    List<VariableContribution> GetRankList();
}
