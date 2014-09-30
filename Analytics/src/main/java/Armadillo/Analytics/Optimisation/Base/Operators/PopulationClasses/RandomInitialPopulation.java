package Armadillo.Analytics.Optimisation.Base.Operators.PopulationClasses;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import Armadillo.Analytics.Optimisation.Base.OptimisationConstants;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.MixedSolvers.DummyObjectiveFunctions.MixedObjectiveFunctionDummy;
import Armadillo.Core.Environment;
import Armadillo.Core.Guid;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.PrintToScreen;
import Armadillo.Core.Concurrent.ConcurrentHelper;
import Armadillo.Core.Concurrent.ILoopBody;
import Armadillo.Core.Concurrent.Parallel;
import Armadillo.Core.SelfDescribing.SelfDescribingTsEvent;
import Armadillo.Core.UI.PublishUiMessageEvent;

/// <summary>
///   For very large problems the Genetic Algorithm requires a 
///   large number of iterations to converge to its local optimal. 
///   In order to enhance the converge speed we generate a 
///   set of �improved solutions�. 
///   These solutions will be input to the Genetic Algorithm which 
///   will be re-sampling more good solutions.
/// </summary>
public class RandomInitialPopulation extends AInitialPopulation
{
    public enum Enums
    {
        ProblemName,
        Individual,
        Iteration,
        TimeSecs,
        InitialPopulationStats,
        Fitness,
        PopulationSize,
        Percentage
    }

    private boolean m_blnDoLocalSearch;
    
    @Override
    public boolean DoLocalSearch()
    {
    	return m_blnDoLocalSearch;
    }

    private final HeuristicProblem m_heuristicProblem;
    private final SelfDescribingTsEvent m_initialPopulationStats;
    private final Object m_lockObject1 = new Object();
    private final Object m_lockObject2 = new Object();
    private int m_intInitialPoolIndividualReady;

    /// <summary>
    ///   Constructor
    /// </summary>
    public RandomInitialPopulation(
        HeuristicProblem heuristicProblem)
    {
        m_blnDoLocalSearch = true;
        m_heuristicProblem = heuristicProblem;
        String strClassName =
            "IntitalPopulationStats_" + getClass().getName();
        m_initialPopulationStats = new SelfDescribingTsEvent(
            strClassName);
        m_initialPopulationStats.SetStrValue(
            Enums.ProblemName,
            heuristicProblem.getProblemName());
        PublishGridStats();
    }

    /// <summary>
    ///   Gets the initial population. Do an extensive local search in 
    ///   order to start the GA with good invididuals and reduce the number
    ///   of GA iterations.
    ///   <returns>
    ///     Population
    ///   </returns>
    public void GetInitialPopulation()
    {
    	try
    	{
	        String strMessage =
	            m_heuristicProblem.getSolver().GetSolverName() +
	            Environment.NewLine + "Generating random initial population. Please wait...";
	        PrintToScreen.WriteLine(strMessage);
	        invokeUpdateProgress(-1, strMessage);
	        synchronized (m_lockObject2)
	        {
	        	int intPopulationSize = m_heuristicProblem.PopulationSize();
	    		
	        	if(m_heuristicProblem.getThreads() == 1)
	        	{
	        		for (int i = 0; i < intPopulationSize; i++) 
	        		{
		                  iterateIndividual();
					}
	        	}
	        	else
	        	{
		        	Parallel.For(0, 
		    				intPopulationSize, 
		    				new ILoopBody<Integer>() 
		    		{
		    			public void run(Integer i) 
		    			{
		    				try
		    				{
				                  iterateIndividual();
		    		    	}
		    		    	catch(Exception ex)
		    		    	{
		    		    		Logger.log(ex);
		    		    	}
		    			}
		    		},
		    		ConcurrentHelper.CPUS);
	        	}
	    		
	            m_heuristicProblem.getPopulation().LoadPopulation();
	            // evaluate random population in parallel
	            strMessage = "Finish evaluating random initial pool.";
	            PrintToScreen.WriteLine(strMessage);
	            invokeUpdateProgress(-1, strMessage);
	        }
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    /// <summary>
    ///   Evaluate individual
    /// </summary>
    /// <param name = "individual">
    ///   Evaluate individual
    /// </param>
    /// <returns></returns>
    private void EvaluateIndividual(Individual individual)
    {
    	try
    	{
	        DateTime dateLog = DateTime.now();
	
	        individual.Evaluate(DoLocalSearch(),
	                            true,
	                            true,
	                            m_heuristicProblem);
	
	        loadStats(individual, dateLog);
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

	private void loadStats(Individual individual, DateTime dateLog) {
		synchronized (m_lockObject1)
		{
		    m_intInitialPoolIndividualReady++;
		}

		int intPercentage = (m_intInitialPoolIndividualReady*100)/
		                    m_heuristicProblem.PopulationSize();

		intPercentage =
		    OptimisationConstants.INT_LOAD_DATA_PERCENTAGE +
		    intPercentage*OptimisationConstants.INT_INITAL_POOL_PERCENTAGE/100;

		String strMessage = "Finish evaluating initial pool. Individual " +
		                 m_intInitialPoolIndividualReady + " of " +
		                 m_heuristicProblem.PopulationSize();
		strMessage = strMessage + ". Percentage = " + intPercentage + "%";
		PrintToScreen.WriteLine(strMessage);
		invokeUpdateProgress(intPercentage, strMessage);
		Logger.Log(strMessage);

		//
		// publish grid stats
		//
		m_initialPopulationStats.SetStrValue(
		    Enums.Individual,
		    individual.toString());
		m_initialPopulationStats.SetDblValue(
		    Enums.Fitness,
		    individual.getFitness());
		int intSeconds = Seconds.secondsBetween(
				dateLog, 
				DateTime.now()).getSeconds();        
		m_initialPopulationStats.SetDblValue(
		    Enums.TimeSecs,
		    intSeconds);
		m_initialPopulationStats.SetIntValue(
		    Enums.Iteration,
		    m_intInitialPoolIndividualReady);
		m_initialPopulationStats.SetIntValue(
		    Enums.PopulationSize,
		    m_heuristicProblem.PopulationSize());
		m_initialPopulationStats.SetIntValue(
		    Enums.Percentage,
		    intPercentage);
		m_heuristicProblem.getSolver().InvokeOnIndividualEvaluated(individual);
		PublishGridStats();
	}

    private void PublishGridStats()
    {
        if (!(m_heuristicProblem.getObjectiveFunction() instanceof MixedObjectiveFunctionDummy))
        {
            String strProblemName =
                m_initialPopulationStats.GetStrValue(
                    Enums.ProblemName);
            m_initialPopulationStats.Time = DateTime.now();
            PublishUiMessageEvent.PublishGrid(
                strProblemName,
                strProblemName,
                Enums.InitialPopulationStats.toString(),
                Guid.NewGuid().toString(),
                m_initialPopulationStats,
                2);
        }
    }

	@Override
	public void setDoLocalSearch(boolean bln) 
	{
		m_blnDoLocalSearch = bln;
		
	}

	private void iterateIndividual() 
	{
		try
		{
			Individual currentIndividual =
					  m_heuristicProblem.getIndividualFactory().BuildRandomIndividual();
	
			  if(currentIndividual.isIsEvaluated() ||
			      currentIndividual.IsReadOnly())
			  {
			      throw new HCException("Invalid individual state");
			  }
	
			  EvaluateIndividual(currentIndividual);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
}
