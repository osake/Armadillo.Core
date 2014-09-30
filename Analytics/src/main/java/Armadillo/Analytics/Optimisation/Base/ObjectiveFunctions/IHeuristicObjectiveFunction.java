package Armadillo.Analytics.Optimisation.Base.ObjectiveFunctions;

import Armadillo.Analytics.Optimisation.Base.Delegates.EvaluateObjectiveDelegate;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;

public interface IHeuristicObjectiveFunction extends IObjectiveFunction
{
    String ObjectiveName();

    /// <summary>
    ///   Get real return. Ignore return weights to 
    ///   calculate the return
    /// </summary>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    /// <returns></returns>
    double Evaluate(Individual individual);
    
    void addEvaluateDelegate(EvaluateObjectiveDelegate evaluateObjectiveDelegate);
}