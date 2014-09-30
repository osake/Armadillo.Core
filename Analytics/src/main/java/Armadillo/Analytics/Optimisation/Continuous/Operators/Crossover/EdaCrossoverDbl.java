package Armadillo.Analytics.Optimisation.Continuous.Operators.Crossover;

import Armadillo.Analytics.Optimisation.Base.Helpers.OptimizationHelper;
import Armadillo.Analytics.Optimisation.Base.Operators.Crossover.ACrossover;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;

/// <summary>
///   Allows generate new individuals based on the the
///   distribution of solutions.
///   It requires a larger population size in order to
///   estimate the parameters of the crossover.
/// </summary>
public class EdaCrossoverDbl extends ACrossover
{
    public EdaCrossoverDbl(HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
    }

    @Override
    public Individual DoCrossover(
        RngWrapper rng,
        Individual[] individuals)
    {
        double[] dblNewChromosomeArray = new double[
            m_heuristicProblem.VariableCount()];

        //
        // reproduction via EDA (global search)
        //
        // get mean & stdDev vectors
        double[] dblMeanVector = GetMeanVector();

        double[] dblStdDevVector =
            GetStdDevVector(
                dblMeanVector,
                rng);

        for (int j = 0;
             j <
             m_heuristicProblem.VariableCount();
             j++)
        {
            // noise is a random number normally distributed
            double dblNoise = OptimizationHelper.Noise(rng);

            dblNewChromosomeArray[j] = dblMeanVector[j] + dblStdDevVector[j]*dblNoise;
            //
            // validate chromosome value
            //
            if (dblNewChromosomeArray[j] < 0)
            {
                dblNewChromosomeArray[j] = 0;
            }
            else if (dblNewChromosomeArray[j] > 1.0)
            {
                dblNewChromosomeArray[j] = 1.0;
            }
        }

        return new Individual(
            dblNewChromosomeArray,
            m_heuristicProblem);
    }

    private double[] GetStdDevVector(
        double[] dblMeanVector,
        RngWrapper rng)
    {
        int intEdaPartentSize = GetEdaParentSize();
        int intEdaCurrentPartenSize = 0;
        double[] dblStdDevVector = new double[
            m_heuristicProblem.VariableCount()];
        intEdaCurrentPartenSize = 0;
        for (int i = 0; i < intEdaPartentSize; i++)
            //foreach (IIndividual solution in
            //    HeuristicProblem.Population_.LargePopulationSizeLargePopulationArr)
        {
            Individual solution =
                m_heuristicProblem.getPopulation().GetIndividualFromLargePopulation(
                    m_heuristicProblem,
                    i);
            // get stdDev vector
            for (int j = 0;
                 j <
                 m_heuristicProblem.VariableCount();
                 j++)
            {
                double dblCrValue = solution.GetChromosomeValueDbl(j);
                dblStdDevVector[j] += Math.pow(dblCrValue - dblMeanVector[j], 2);
            }

            intEdaCurrentPartenSize++;
            if (intEdaCurrentPartenSize == intEdaPartentSize)
            {
                break;
            }
        }
        for (int j = 0;
             j <
             m_heuristicProblem.VariableCount();
             j++)
        {
            dblStdDevVector[j] = Math.sqrt(dblStdDevVector[j]/
                                           (intEdaCurrentPartenSize - 1.0));

            //
            // validate std deviation
            //
            if (dblStdDevVector[j] < 1E-4)
            {
                if (rng.nextDouble() >= 0.6)
                {
                    dblStdDevVector[j] = 0.1;
                }
                else
                {
                    dblStdDevVector[j] = 1E-4;
                }
            }
        }

        return dblStdDevVector;
    }

    private double[] GetMeanVector()
    {
        int intEdaCurrentPartenSize = 0;
        int intEdaParentSize = GetEdaParentSize();

        double[] dblMeanVector = new double[
            m_heuristicProblem.VariableCount()];

        //foreach (IIndividual solution in
        //    HeuristicProblem.Population_.LargePopulationArr)
        for (int i = 0; i < intEdaParentSize; i++)
        {
            Individual solution =
                m_heuristicProblem.getPopulation().GetIndividualFromLargePopulation(
                    m_heuristicProblem,
                    i);

            for (int j = 0;
                 j <
                 m_heuristicProblem.VariableCount();
                 j++)
            {
                dblMeanVector[j] +=
                    solution.GetChromosomeValueDbl(j);
            }
            intEdaCurrentPartenSize++;

            if (intEdaCurrentPartenSize == intEdaParentSize)
            {
                break;
            }
        }

        for (int j = 0;
             j <
             m_heuristicProblem.VariableCount();
             j++)
        {
            dblMeanVector[j] /= intEdaCurrentPartenSize;
        }
        return dblMeanVector;
    }

    private int GetEdaParentSize()
    {
    	try
    	{
	        if (m_heuristicProblem.getPopulation().GetIndividualFromLargePopulation(
	            m_heuristicProblem,
	            m_heuristicProblem.getPopulation().LargePopulationSize() - 1) == null)
	        {
	            //
	            // large population is not ready
	            // count the number of individuals
	            //
	        	boolean blnThereWasIndividual = false;
	            for (int i = 0; i < m_heuristicProblem.getPopulation().LargePopulationSize(); i++)
	            {
	                if (m_heuristicProblem.getPopulation().GetIndividualFromLargePopulation(
	                    m_heuristicProblem,
	                    i) == null)
	                {
	                    return i;
	                }
	                else
	                {
	                	blnThereWasIndividual = true;
	                }
	            }
	            if(blnThereWasIndividual)
	            {
	            	m_heuristicProblem.getPopulation().LargePopulationSize();
	            }
	        }
	        else
	        {
	            return m_heuristicProblem.getPopulation().LargePopulationSize();
	        }
	        //Debugger.Break();
	        throw new HCException("Error. Individual is null.");
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return 0;
    }
}
