package Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses;

import java.util.Comparator;

import Armadillo.Core.Logger;

public class IndividualComparator implements Comparator<Individual> 
{

	public int compare(Individual arg0, Individual arg1) 
	{
		try
		{
			return compareTo(arg0, arg1);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return 0;
	}

	public int compareTo(
			Individual thisIndividual, 
			Individual o) 
	{
    	try
    	{
	        int intCompareToValue =
	            CompareToStd(thisIndividual, o);
	
	        if (intCompareToValue == 0)
	        {
	            intCompareToValue =
	                CompareToTree(thisIndividual, o);
	        }
	
	        return intCompareToValue;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return 0;
	}
	
    /// <summary>
    ///   Sort individuals by their fitness
    /// </summary>
    /// <param name = "o">
    ///   IIndividual
    /// </param>
    /// <returns>
    ///   Compare value
    /// </returns>
    public int CompareToStd(Individual thisIndividual, Individual o)
    {
    	try
    	{
	        if (thisIndividual.getFitness() < o.getFitness())
	        {
	            return 1;
	        }
	        if (thisIndividual.getFitness() > o.getFitness())
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

    public int CompareToTree(
    		Individual thisIndividual,
    		Individual other)
    {
    	try
    	{
	        return thisIndividual.getGpTreeSize() - other.getGpTreeSize();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
		return 0;
    }
}
