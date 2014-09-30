package Armadillo.Analytics.Optimisation.Base.Delegates;

import Armadillo.Core.IDelegate;
import Armadillo.Core.NotImplementedException;

public class ExceptionOccurredDelegate implements IDelegate
{
	public void invoke(Exception ex) 
	{
    	throw new NotImplementedException();
	}
}
