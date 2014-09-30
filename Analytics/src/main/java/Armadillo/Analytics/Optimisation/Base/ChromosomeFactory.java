package Armadillo.Analytics.Optimisation.Base;

import org.apache.commons.math3.util.Precision;

import Armadillo.Analytics.Base.MathConstants;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;

public class ChromosomeFactory
{
    public static void BuildRandomChromosome(
        double[][] dblChromosomeArr,
        int[][] intChromosomeArr,
        boolean[][] blnChromosomeArr,
        HeuristicProblem heuristicProblem,
        RngWrapper rngWrapper)
    {
    	try
    	{
	        dblChromosomeArr[0] = null;
	        intChromosomeArr[0] = null;
	        blnChromosomeArr[0] = null;
	
	        if (heuristicProblem.EnumOptimimisationPoblemType() ==
	            EnumOptimimisationPoblemType.BINARY)
	        {
	            blnChromosomeArr[0] =
	                BuildRandomChromosomeBln(
	                    heuristicProblem,
	                    rngWrapper);
	        }
	        else if (heuristicProblem.EnumOptimimisationPoblemType() ==
	                 EnumOptimimisationPoblemType.CONTINUOUS)
	        {
	            dblChromosomeArr[0] =
	                BuildRandomChromosomeDbl(
	                    heuristicProblem,
	                    rngWrapper);
	        }
	        else if (heuristicProblem.EnumOptimimisationPoblemType() ==
	                 EnumOptimimisationPoblemType.INTEGER)
	        {
	            intChromosomeArr[0] =
	                BuildRandomChromosomeInt(
	                    heuristicProblem,
	                    rngWrapper);
	        }
	        else if (heuristicProblem.EnumOptimimisationPoblemType() ==
	                 EnumOptimimisationPoblemType.GENETIC_PROGRAMMING)
	        {
	            // do nothing
	        }
	        else
	        {
	            throw new HCException("Error. Problem type not supported.");
	        }
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    public static double[] BuildRandomChromosomeDbl(
        HeuristicProblem heuristicProblem,
        RngWrapper rngWrapper)
    {
    	try
    	{
	        double[] dblChromosomeArr = new double[
	            heuristicProblem.VariableCount()];
	
	        for (int i = 0; i < heuristicProblem.VariableCount(); i++)
	        {
	        	double dblRng = Precision.round(rngWrapper.nextDouble(),
                        MathConstants.ROUND_DECIMALS);
	            dblChromosomeArr[i] = dblRng;
	        }
	        return dblChromosomeArr;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }

    public static boolean[] BuildRandomChromosomeBln(
        HeuristicProblem heuristicProblem,
        RngWrapper rngWrapper)
    {
        boolean[] blnChromosomeArr = new boolean[
            heuristicProblem.VariableCount()];

        for (int i = 0; i < heuristicProblem.VariableCount(); i++)
        {
            blnChromosomeArr[i] = (rngWrapper.NextInt(0, 1) == 1);
        }
        return blnChromosomeArr;
    }

    public static int[] BuildRandomChromosomeInt(
        HeuristicProblem heuristicProblem,
        RngWrapper rngWrapper)
    {
        int[] intChromosomeArr = new int[
            heuristicProblem.VariableCount()];

        for (int i = 0; i < heuristicProblem.VariableCount(); i++)
        {
            intChromosomeArr[i] = rngWrapper.NextInt(0,
                                                     (int) heuristicProblem.getVariableRangesIntegerProbl()[i]);
        }
        return intChromosomeArr;
    }
}
