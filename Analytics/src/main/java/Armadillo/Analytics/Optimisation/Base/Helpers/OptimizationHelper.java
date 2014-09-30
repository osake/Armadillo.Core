package Armadillo.Analytics.Optimisation.Base.Helpers;

import Armadillo.Analytics.Optimisation.Base.DataStructures.ResultRow;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Stat.Random.RngWrapper;

/// <summary>
///   Optimisation helper class.
///   Note: This class it not threadsafe.
/// </summary>
public class OptimizationHelper
{

    //public static boolean CompareTwoIndividuals(
    //    IIndividual individual1,
    //    IIndividual individual2)
    //{
    //    if (CompareTwoChromosomes(
    //        individual1,
    //        individual2))
    //    {
    //        return true;
    //    }
    //    return false;
    //}
    //public static boolean CompareTwoChromosomes(
    //    IIndividual individual1,
    //    IIndividual individual2)
    //{
    //    for (int i = 0; i < individual1.HeuristicOptimizationProblem_.VariableCount; i++)
    //    {
    //        if (individual1.GetChromosomeValue(i) != 
    //            individual2.GetChromosomeValue(i))
    //        {
    //            return false;
    //        }
    //    }
    //    return true;
    //}
    /// <summary>
    ///   compare two individuals. Check if the two chromosomes are equal
    /// </summary>
    /// <param name = "individual1">
    ///   IIndividual 1
    /// </param>
    /// <param name = "individual2">
    ///   IIndividual 2
    /// </param>
    /// <returns></returns>
    /// <summary>
    ///   Compare two chrosomes and return true if the 
    ///   two chromosomes are equal
    /// </summary>
    /// <param name = "dblChromosome1">
    ///   Chromosome 1
    /// </param>
    /// <param name = "dblChromosome2">
    ///   Chromosome 2
    /// </param>
    /// <returns></returns>
    /// <summary>
    ///   Get result row. Method called when optimizer finish working
    /// </summary>
    /// <param name = "dblChromosomeArray">
    ///   Chromosome
    /// </param>
    /// <param name = "objectiveFunction">
    ///   Objective function
    /// </param>
    /// <param name = "constraints">
    ///   Constraints
    /// </param>
    /// <param name = "cc">
    ///   Cluster
    /// </param>
    /// <param name = "clusterRow">
    ///   IIndividual
    /// </param>
    /// <param name = "reproduction">
    ///   Reproduction
    /// </param>
    /// <param name = "blnGetFinalResults">
    ///   Get final results
    /// </param>
    /// <param name = "repository">
    ///   Repository
    /// </param>
    /// <param name = "baseDistribution">
    ///   Base distribution
    /// </param>
    /// <param name = "dblReturnPeriod">
    ///   Return period
    /// </param>
    /// <param name = "epType">
    ///   EP type
    /// </param>
    /// <param name = "dblBaseRisk">
    ///   Base risk
    /// </param>
    /// <param name = "repairIndividual">
    ///   Repair solution
    /// </param>
    /// <param name = "localSearch">
    ///   Local search
    /// </param>
    /// <returns></returns>
    public static ResultRow GetResultRow(
        double[] dblChromosomeArray,
        boolean blnGetFinalResults,
        Individual clusterRow)
    {
        return null;
    }

    /// <summary>
    ///   Get a random number distributed normally
    /// </summary>
    /// <returns>
    ///   Normal random number
    /// </returns>
    public static double Noise(
        RngWrapper rng)
    {
        double fac, rsq, v1, v2;

        do
        {
            v1 = 2.0*rng.nextDouble() - 1.0;
            v2 = 2.0*rng.nextDouble() - 1.0;
            rsq = v1*v1 + v2*v2;
        } while ((rsq >= 1.0) || (rsq == 0.0));

        fac = Math.sqrt(-2.0*Math.log(rsq)/rsq);
        return v2*fac;
    }

    /// <summary>
    ///   Get number of iterations performed by the GA solver
    /// </summary>
    /// <param name = "intCapacity">
    ///   Repository capacity
    /// </param>
    /// <returns>
    ///   Number of iterations
    /// </returns>
    public static int GetHeuristicSolverIterations(int intVariables)
    {
        int iterationsPerHundredPolicies = 10000;
        if (intVariables <= 10)
        {
            return 200;
        }
        int intIterations = (intVariables*iterationsPerHundredPolicies)/100;
        return intIterations > 20000 ? 20000 : intIterations;
    }

    /// <summary>
    ///   Get the number of iterations to be performed by a solver
    /// </summary>
    /// <param name = "intCapacity">
    ///   Repository capacity
    /// </param>
    /// <param name = "zeroOneCount">
    ///   Number of zeros/ones in an initial solution set.
    ///   Provide an insight of the combinatorial level of the current problem
    /// </param>
    /// <returns>
    ///   Number of iterations
    /// </returns>
    public static int GetSolverIterations(int intCapacity, int zeroOneCount)
    {
        // return the number of iterations according to how difficult the problem is.
        // if the problem contains, on average, the half amount of ones and zeros
        // then run the full amount of iteratios
        int intIterations = GetHeuristicSolverIterations(intCapacity);
        return Math.max((zeroOneCount*intIterations)/(intCapacity/2), 200);
    }

    public static int GetMsIterations(int intProblemSize, int intCapacity)
    {
        if (intCapacity >= intProblemSize)
        {
            return 1;
        }
        int intPercentage = 100 - (intCapacity*100)/intProblemSize;
        intPercentage = intPercentage*5/40;
        return intPercentage > 3 ? 3 : intPercentage;
    }


    /// <summary>
    ///   Get a string reperesentation of a given chromosome
    /// </summary>
    /// <param name = "dblChromosomeArray">
    ///   Chromosome array
    /// </param>
    /// <returns>
    ///   String representation of a given chromosome
    /// </returns>
    public static String GetChromosomeString(
        double[] dblChromosomeArray)
    {
    	StringBuilder sb = new StringBuilder();
        if (dblChromosomeArray != null)
        {
            sb.append("|| ");
            for (int i = 0; i < dblChromosomeArray.length; i++)
            {
                sb.append(dblChromosomeArray[i] + " ");
            }
        }
        return sb.toString();
    }

    /// <summary>
    ///   Get a chromosome array from a give string
    /// </summary>
    /// <param name = "strCandidate">
    ///   String description
    /// </param>
    /// <returns></returns>
    public static double[] GetChromosomeArray(String strCandidate)
    {
        double[] chromosome = new double[strCandidate.length()];

        for (int i = 0; i < strCandidate.length(); i++)
        {
            if (strCandidate.charAt(i) == '1')
            {
                chromosome[i] = 1;
            }
        }
        return chromosome;
    }

    /// <summary>
    ///   Check if two vectors are identical
    /// </summary>
    /// <param name = "intVector1">
    ///   Vector 1
    /// </param>
    /// <param name = "intVector2">
    ///   Vector 2
    /// </param>
    /// <returns></returns>
    public static boolean CompareTwoVectors(double[] intVector1, double[] intVector2)
    {
        for (int i = 0; i < intVector1.length; i++)
        {
            if (intVector1[i] != intVector2[i])
            {
                return false;
            }
        }
        return true;
    }

}
