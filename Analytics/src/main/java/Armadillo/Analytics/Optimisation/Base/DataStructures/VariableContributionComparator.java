package Armadillo.Analytics.Optimisation.Base.DataStructures;

import java.util.Comparator;

import Armadillo.Core.Logger;

public class VariableContributionComparator implements Comparator<VariableContribution> 
{

    /// <summary>
    ///   Sort object by their contribution.
    /// </summary>
    /// <param name = "obj">
    ///   Object to compare with.
    /// </param>
    /// <returns>
    ///   Compare value.
    /// </returns>
	@Override
	public int compare(VariableContribution arg0, VariableContribution arg1) 
	{
		try
		{
	    	VariableContribution Compare = arg1;
	        double difference = arg0.Contribution - Compare.Contribution;
	        if (difference < 0)
	        {
	            return 1;
	        }
	        if (difference > 0)
	        {
	            return -1;
	        }
	        difference = arg0.Index - Compare.Index;
	        if (difference < 0)
	        {
	            return 1;
	        }
	        if (difference > 0)
	        {
	            return -1;
	        }
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
        return 0;
	}

}
