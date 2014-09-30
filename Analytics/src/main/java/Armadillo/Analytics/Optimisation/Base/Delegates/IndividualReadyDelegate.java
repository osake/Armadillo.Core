package Armadillo.Analytics.Optimisation.Base.Delegates;

import Armadillo.Analytics.Optimisation.Base.EvaluationStateType;
import Armadillo.Core.IDelegate;
import Armadillo.Core.NotImplementedException;

public class IndividualReadyDelegate  implements IDelegate
{
    /// <summary>
    ///   signals that the idividual has been evaluated
    /// </summary>
    /// <param name = "state"></param>
    public void invoke(EvaluationStateType state)
    {
    	throw new NotImplementedException();
    }
}
