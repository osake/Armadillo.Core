package Armadillo.Analytics.Optimisation.Base.Operators.PopulationClasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import Armadillo.Analytics.Optimisation.Base.Clustering.ChromosomeSimilarityMetric;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.IndividualComparator;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;

/// <summary>
///   Pupulation class.
///   Hold solutions explored by the solver. 
///   Each element in the population serves to the generation of new solutions for 
///   reproduction and repair purposes.
/// </summary>
public class Population
{
    /// <summary>
    ///   Evaluates the similarity between two chromosomes.
    /// </summary>
    private final ChromosomeSimilarityMetric m_chromosomeSimilarityMetric;
    private final HeuristicProblem m_heuristicProblem;
    private final Object m_newIndividualLockobject = new Object();
    private Individual[] m_largePopulationArr;
    private int m_largePopulationSize;
    private List<Individual> m_newIndividualsList;
    private Individual[] m_populationArr;

    public int LargePopulationSize()
    {
        if (m_largePopulationArr == null)
        {
            return 0;
        }
        return m_largePopulationArr.length;
    }

    /// <summary>
    ///   Default constructor.
    ///   Save solutions explored by the solver and cluster them. 
    ///   Each clustered element serves to the generation of new solutions for 
    ///   reproduction and repair purposes.
    /// </summary>
    public Population(
        HeuristicProblem heuristicProblem)
    {
        m_heuristicProblem = heuristicProblem;

        // initilaize similarity metrics
        m_chromosomeSimilarityMetric = new ChromosomeSimilarityMetric();

        InitializePopulation();
    }

    private void InitializePopulation()
    {
        if (m_heuristicProblem.PopulationSize() > 0)
        {
            m_populationArr = new Individual[
                m_heuristicProblem.PopulationSize()];

            m_largePopulationSize = Math.max(
                2*m_heuristicProblem.PopulationSize(),
                150);


            synchronized (m_newIndividualLockobject)
            {
                m_newIndividualsList = new ArrayList<Individual>(
                    2*m_largePopulationSize);
            }
            m_largePopulationArr = new Individual[m_largePopulationSize];
        }
    }


    /// <summary>
    ///   Replace the best clustered solution if the given solution is 
    ///   very similar to the best clustered solution. Otherwise,
    ///   cluster the solution.
    ///   This method avoids clustering too many similar vectors which will
    ///   cause an excesively quick convergence
    /// </summary>
    public void ClusterBestSolution(
        Individual individual,
        HeuristicProblem heuristicProblem)
    {
        if (m_largePopulationArr[0] == null)
        {
            synchronized (m_newIndividualLockobject)
            {
                m_newIndividualsList.add(individual);
            }
        }
        else
        {
            // proceed to cluster instance only if the fitness is 
            // greather than the best clustered solution
            if (individual.getFitness() > m_largePopulationArr[0].getFitness())
            {
                Individual individual1 = individual;
                if (individual1.getIndividualList() != null &&
                    individual1.getIndividualList().size() > 0)
                {
                    individual1 = individual1.GetIndividual(
                        heuristicProblem.getProblemName());
                }

                Individual individual2 = m_largePopulationArr[0];
                if (individual2.getIndividualList() != null &&
                    individual2.getIndividualList().size() > 0)
                {
                    individual2 = individual2.GetIndividual(
                        heuristicProblem.getProblemName());
                }


                // get the similarity between the given chromosome and
                // the best clustered item
                double dblSimilarityScore =
                    m_chromosomeSimilarityMetric.GetStringMetric(
                        individual2,
                        individual1);
                //
                // in order to avoid having too many similar individuals in the population
                // then if the similarity between individuals is greather than 
                // a certain threshold then replace the best solution by the new one.
                // Otherwise, cluster the individual
                //
                if (dblSimilarityScore >= 0.95)
                {
                    m_largePopulationArr[0] = individual;
                }
                else
                {
                    synchronized (m_newIndividualLockobject)
                    {
                        m_newIndividualsList.add(individual);
                    }
                }
            }
        }
    }

    private static Individual GetNestedIndividual(
        Individual originalIndividual,
        Individual selectedIndividual,
        HeuristicProblem heuristicProblem)
    {
        if (selectedIndividual.getIndividualList() != null &&
            selectedIndividual.getIndividualList().size() > 0)
        {
            Individual nestedIndividual =
                selectedIndividual.GetIndividual(
                    heuristicProblem.getProblemName());

            if (nestedIndividual != null)
            {
                return nestedIndividual;
            }
            //
            // check for nested individuals recursively
            //
            for (Individual currentInd : selectedIndividual.getIndividualList())
            {
                Individual retrievedIndividual = GetNestedIndividual(
                    originalIndividual,
                    currentInd,
                    heuristicProblem);
                if (retrievedIndividual != null)
                {
                    return retrievedIndividual;
                }
            }
        }
        // check if individual is from its parent
        if (heuristicProblem.getProblemName().equals(originalIndividual.getProblemName()))
        {
            return originalIndividual;
        }

        return null;
    }

    public void AddIndividualToPopulation(
        Individual individual)
    {
        if (!individual.isIsEvaluated())
        {
            throw new HCException("Error. Individual not evaluated.");
        }

        individual.SetReadOnly();
        synchronized (m_newIndividualLockobject)
        {
            m_newIndividualsList.add(individual);
        }
    }

    public void SetPopulationSize()
    {
        if (m_populationArr == null ||
            m_heuristicProblem.PopulationSize() !=
            m_populationArr.length)
        {
            InitializePopulation();
        }
    }


    public Individual[] GetPopulationArr()
    {
        return (Individual[]) m_populationArr.clone();
    }

    public void LoadPopulation()
    {
    	try
    	{
	        Individual[] largePopulationArr = m_largePopulationArr;
	        //
	        // create a local copy of the new individuals
	        //
	        List<Individual> newIndividualList = new ArrayList<Individual>();
	        synchronized (m_newIndividualLockobject)
	        {
	        	for(Individual individual: m_newIndividualsList)
	        	{
	        		newIndividualList.add(individual);
	        	}
	        }
	
	        //
	        // merge the new individuals with the large population
	        //
	        for (Individual individual : largePopulationArr)
	        {
	            if (individual != null)
	            {
	                newIndividualList.add(individual);
	            }
	        }
	        Collections.sort(newIndividualList, new IndividualComparator());
	
	        //
	        // load population arrays
	        //
	        for (int i = 0;
	             i < Math.min(newIndividualList.size(),
	                          largePopulationArr.length);
	             i++)
	        {
	            largePopulationArr[i] = newIndividualList.get(i);
	
	            if (i < m_heuristicProblem.PopulationSize())
	            {
	                m_populationArr[i] = newIndividualList.get(i);
	            }
	        }
	
	        synchronized (m_newIndividualLockobject)
	        {
	            m_newIndividualsList = new ArrayList<Individual>(
	                largePopulationArr.length*2);
	        }
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    public Individual[] GetPopulationAndNewCandidates()
    {
    	try
    	{
	        List<Individual> newIndividualList;
	        synchronized (m_newIndividualLockobject)
	        {
	            newIndividualList = new ArrayList<Individual>(
	                m_newIndividualsList.size()*2);
	            newIndividualList.addAll(
	                m_newIndividualsList);
	        }
	
	        int intPopulation =
	            m_heuristicProblem.PopulationSize();
	        int intCandidateListLength = newIndividualList.size();
	        Individual[] pop = new Individual[
	            m_populationArr.length +
	            intCandidateListLength];
	        
	        System.arraycopy(
	            m_populationArr,
	            0,
	            pop,
	            0,
	            intPopulation);
	
	        if (intCandidateListLength > 0)
	        {
	        	System.arraycopy(
	                newIndividualList.toArray(),
	                0,
	                pop,
	                intPopulation,
	                intCandidateListLength);
	        }
	        return pop;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }

    public Individual GetIndividualFromPopulation(
        HeuristicProblem heuristicProblem,
        int intIndex)
    {
    	try
    	{
	        if (m_populationArr == null)
	        {
	            throw new HCException("Error. Population is null.");
	        }
	
	        Individual selectedIndividual = m_populationArr[intIndex];
	        if (selectedIndividual == null)
	        {
	            return null;
	        }
	
	        //
	        // select individual from  given heuristic problem
	        // this will allow to select individuals from the population
	        //
	        selectedIndividual = GetNestedIndividual(
	            selectedIndividual,
	            selectedIndividual,
	            heuristicProblem);
	
	        return selectedIndividual;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }

    public Individual GetIndividualFromLargePopulation(
        HeuristicProblem heuristicProblem,
        int intIndex)
    {
    	Individual selectedIndividual = m_largePopulationArr[intIndex];

        //
        // select individual from a given heuristic problem
        // this will allow to select nested individuals from the population
        //
        //
        if (selectedIndividual != null &&
            heuristicProblem != null)
        {
            selectedIndividual = GetNestedIndividual(
                selectedIndividual,
                selectedIndividual,
                heuristicProblem);

            if (selectedIndividual == null)
            {
                throw new HCException("Invalid nested individual");
            }
        }
        return selectedIndividual;
    }

    public void LoadPopulationMultiObjective(
        Individual[] pop,
        int[] intRanksArr)
    {
    	try
    	{
	        //
	        // assign ranks to temporary population array
	        //
	        for (int i = 0; i < pop.length; i++)
	        {
	            pop[i] = pop[i].Clone(m_heuristicProblem);
	            pop[i].SetFitnessValue(-intRanksArr[i]);
	        }
	        Arrays.sort(pop, new IndividualComparator());
	
	        //
	        // load individuals to population
	        //
	        for (int i = 0; i < m_heuristicProblem.PopulationSize(); i++)
	        {
	            m_populationArr[i] = pop[i];
	        }
	        m_largePopulationArr =
	            m_populationArr;
	        synchronized (m_newIndividualLockobject)
	        {
	            m_newIndividualsList = new ArrayList<Individual>();
	        }
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    private double GetFitnessMean()
    {
        double dblSum = 0;
        for (Individual individual : m_populationArr)
        {
            dblSum += individual.getFitness();
        }
        return dblSum/m_populationArr.length;
    }

    public double GetFinessVarCoeff()
    {
        double dblMean = GetFitnessMean();
        double dblStdDev = GetFinessStdDev(dblMean);
        return GetFinessVarCoeff(
            dblMean,
            dblStdDev);
    }

    public double GetFinessVarCoeff(
        double dblMean,
        double dblStdDev)
    {
        return dblStdDev/dblMean;
    }

    public double GetFinessStdDev()
    {
        double dblMean = GetFitnessMean();
        double dblStdDev = GetFinessStdDev(dblMean);
        return dblStdDev;
    }

    public double GetFinessStdDev(double dblMean)
    {
    	double dblStdDev = Math.sqrt(GetFinessVariance(dblMean));
        return dblStdDev;
    }

    public double GetFinessVariance()
    {
    	double dblMean = GetFitnessMean();
        return GetFinessVariance(dblMean);
    }

    public double GetFinessVariance(double dblMean)
    {
        double dblSumSq = 0;
        for (Individual individual : m_populationArr)
        {
            dblSumSq +=
                Math.pow((individual.getFitness() - dblMean), 2);
        }
        double dblVariance = dblSumSq/(m_populationArr.length - 1.0);

        return dblVariance;
    }

    public String ToStringPopulationStats()
    {
        double dblMean = GetFitnessMean();
        double dblStdDev = GetFinessStdDev(dblMean);
        double dblVarCoeff = GetFinessVarCoeff(
            dblMean,
            dblStdDev);
        return
            "Population Stats:" +
            "\nMean = " + dblMean +
            "\nStd. Dev. = " + GetFinessStdDev(dblMean) +
            "\nCoeff. of Variation = " + dblVarCoeff;
    }

    public void Dispose()
    {
        if (m_largePopulationArr != null)
        {
            for (int i = 0; i < m_largePopulationArr.length; i++)
            {
                Individual currInd = m_largePopulationArr[i];
                if(currInd != null)
                {
                    currInd.Dispose();
                }
            }
            m_largePopulationArr = null;
        }

        if (m_newIndividualsList != null)
        {
            for (int i = 0; i < m_newIndividualsList.size(); i++)
            {
                Individual currInd = m_newIndividualsList.get(i);
                if (currInd != null)
                {
                    currInd.Dispose();
                }
            }
            m_newIndividualsList.clear();
            m_newIndividualsList = null;
        }

        if (m_populationArr != null)
        {
            for (int i = 0; i < m_populationArr.length; i++)
            {
                Individual currInd = m_populationArr[i];
                if (currInd != null)
                {
                    currInd.Dispose();
                }
            }
            m_populationArr = null;
        }
    }
}
