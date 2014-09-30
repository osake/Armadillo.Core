package Armadillo.Analytics.Optimisation.Base.Solvers;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import Armadillo.Analytics.Optimisation.Base.OptiGuiHelper;
import Armadillo.Analytics.Optimisation.Base.OptimisationConstants;
import Armadillo.Analytics.Optimisation.Base.Delegates.ExceptionOccurredDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.ImprovementFoundDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.UpdateProgressDelegate;
import Armadillo.Analytics.Optimisation.Base.Operators.OperatorHelper;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.EnumHeuristicProblem;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Core.Environment;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.PrintToScreen;

public class EvolutionarySolver extends AHeuristicSolver
{
    public OptiGuiHelper OptiGuiHelper;
    
    public HeuristicExecutionHelper HeuristicExecutionHelper;

    /// <summary>
    ///   Object used to synchronized threads at the beginning/end of each iteration
    /// </summary>
    private final Object m_lockObject = new Object();

    /// <summary>
    ///   Flag which indicates if the solver should keep running or stop
    /// </summary>
    private boolean m_blnIterateSolver;


    private DateTime m_prevCheckTime = DateTime.now();
    
    /// <summary>
    ///   Constructor
    /// </summary>
    /// <param name = "heuristicProblem"></param>
    public EvolutionarySolver(
        HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);    	
        SetStatus(heuristicProblem);
    }


    /// <summary>
    ///   Solve
    /// </summary>
    @Override
    public void Solve()
    {
        ValidateSolver();

        // initialize solver
        //
        InitializeSolver();

        //
        // update stats
        //
        PublishProblemStats();

        //
        // run solver
        //
        RunSolver();
    }


    private void PublishProblemStats()
    {
        HeuristicProblem.getProblemStats().SetStrValue(
            EnumHeuristicProblem.ProblemName,
            HeuristicProblem.getProblemName());
        HeuristicProblem.getProblemStats().SetIntValue(
            EnumHeuristicProblem.PopulationSize,
            HeuristicProblem.PopulationSize());
        HeuristicProblem.getProblemStats().SetIntValue(
            EnumHeuristicProblem.Threads,
            HeuristicProblem.getThreads());
        HeuristicProblem.getProblemStats().SetBlnValue(
            EnumHeuristicProblem.DoLocalSearch,
            HeuristicProblem.isDoLocalSearch());
        HeuristicProblem.getProblemStats().SetDblValue(
            EnumHeuristicProblem.LocalSearchProb,
            HeuristicProblem.getLocalSearchProb());
        HeuristicProblem.getProblemStats().SetStrValue(
            EnumHeuristicProblem.ProblemType,
            HeuristicProblem.EnumOptimimisationPoblemType().toString());
        HeuristicProblem.getProblemStats().SetDblValue(
            EnumHeuristicProblem.RepairProb,
            HeuristicProblem.getRepairProb());
        HeuristicProblem.PublishGridStats();
    }

    private void ValidateSolver()
    {
        //
        // Solver validation
        //
        if (HeuristicProblem.getObjectiveFunction().VariableCount() == 1)
        {
            throw new HCException("The repository contains only one variable.");
        }

        if (HeuristicProblem.ContainsIntegerVariables() &&
            !HeuristicProblem.ValidateIntegerProblem())
        {
            throw new HCException("Variable ranges missing for integer problem.");
        }

        if (HeuristicProblem.PopulationSize() == 0)
        {
            throw new HCException("Error. Population size is zero.");
        }
    }

    private void SetStatus(HeuristicProblem heuristicProblem)
    {
        HeuristicProblem = heuristicProblem;
        HeuristicExecutionHelper = new HeuristicExecutionHelper(
            HeuristicProblem);

        //
        // register events
        //
        if (HeuristicProblem.getLocalSearch() != null)
        {
        	ImprovementFoundDelegate improvementFoundDelegate = new ImprovementFoundDelegate()
        	{
        		@Override
        		public void invoke(Individual individual)
        		{
        			InvokeOnImprovementFoundEvent();
        		}
        	};
        	
            HeuristicProblem.getLocalSearch().AddImprovementFoundEvent(improvementFoundDelegate);
        }


        //
        // register initial population events
        //
        UpdateProgressDelegate updateProgressBarDelegate = new UpdateProgressDelegate()
        		{
        			@Override
        			public void invoke(int intProgress, String strMessage)
        			{
        				invokeUpdateProgress(intProgress, strMessage);
        			}
        		};
        
        HeuristicProblem.getInitialPopulation().AddUpdateProgressEvent(updateProgressBarDelegate);
        
        ExceptionOccurredDelegate exceptionOccurredDelegate = new ExceptionOccurredDelegate()
        {
        	@Override
        	public void invoke(Exception ex)
        	{
        		invoke(ex);
        	}
        };
        
        HeuristicProblem.getInitialPopulation().AddExceptionOccurredEvent(exceptionOccurredDelegate);


        InitialCompletionPercentage =
            OptimisationConstants.INT_LOAD_DATA_PERCENTAGE +
            OptimisationConstants.INT_INITAL_POOL_PERCENTAGE +
            OptimisationConstants.INT_INITAL_POPULATION_PERCENTAGE;
        PercentageCompletionValue = OptimisationConstants.INT_SOLVER_PERCENTAGE;

        OptiGuiHelper = new OptiGuiHelper(this);

    }


    /// <summary>
    ///   GetTask solver
    /// </summary>
    private void RunSolver()
    {
        HeuristicExecutionHelper.ExecuteSolver();

        //
        // get final results
        //
        GetFinalResults();
    }

    /// <summary>
    ///   Calculate final results
    /// </summary>
    private void GetFinalResults()
    {
        //
        // display solver summary
        //
    	StringBuilder sb = new StringBuilder();
    	
    	
				
        int totalTime = Seconds.secondsBetween(
				StartTime, 
				DateTime.now()).getSeconds();
        sb.append(Environment.NewLine + GetSolverName());
        sb.append(Environment.NewLine + "Total time (secs): " + totalTime);
        sb.append(Environment.NewLine + "Iterations: " + CurrentIteration);
        sb.append(Environment.NewLine + "Solutions explored: " + SolutionsExplored);
        sb.append(Environment.NewLine + "Best solution: " +
                  HeuristicProblem.getPopulation().GetIndividualFromPopulation(
                      HeuristicProblem,
                      0));
        sb.append(Environment.NewLine + "Objective function = " +
                  HeuristicProblem.getPopulation().GetIndividualFromPopulation(
                      HeuristicProblem,
                      0).getFitness());

        String strMessage = sb.toString();
        Logger.Log(strMessage);
        PrintToScreen.WriteLine(strMessage);
        invokeUpdateProgress(-1, strMessage);
        InvokeSolverFinished(ResultList);
    }


    /// <summary>
    ///   Iterate solver
    /// </summary>
    public void IterateIndividual(Individual individual)
    {
    	try
    	{
	        if (m_blnIterateSolver)
	        {
	            synchronized (m_lockObject)
	            {
	                m_intIterationCounter++;
	                SolutionsExplored++;
	
	                //
	                // note that we dont want to check for stopping conditions too often
	                // this could kill performance
	                //
	                int intSeconds = Seconds.secondsBetween(
	                		m_prevCheckTime, 
	        				DateTime.now()).getSeconds();
	                
	                if (intSeconds > 1)
	                {
	                    m_prevCheckTime = DateTime.now();
	                    if (!CheckStoppingConditions())
	                    {
	                        return;
	                    }
	                }
	
	                //
	                // check if a complete iteration has been completed
	                //
	                if (m_intIterationCounter >= HeuristicProblem.PopulationSize())
	                {
	                    m_intIterationCounter = 0;
	                    IterateGeneration();
	                }
	
	                InvokeOnIndividualEvaluated(individual);
	            }
	        }
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    public void InvokeOnIndividualEvaluated(Individual individual)
    {
    	invokeIndividualEvaluatedDelegate(individual);
        OptiGuiHelper.UpdateStats();
    }

    private void IterateGeneration()
    {
    	try
    	{
	        //
	        // upgrade solver operators
	        //
	        OperatorHelper.UpgradeSolverOperators(
	            HeuristicProblem);
	
	        if (!CheckStoppingConditions())
	        {
	            return;
	        }
	
	        InvokeCompletedIterationDelegate(HeuristicProblem);
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
    }

    private boolean CheckStoppingConditions()
    {
    	try
    	{
	        boolean blnImprovementFound = CheckConvergence();
	        int intPercentage = SetProgress(blnImprovementFound);
	
	        //
	        // check if solver is finalised
	        //
	        if (intPercentage >= (PercentageCompletionValue + InitialCompletionPercentage) &&
	            m_blnIterateSolver)
	        {
	            m_blnIterateSolver = false;
	            HeuristicExecutionHelper.FinishSolver();
	            InvokeSolverFinished(null);
	            OptiGuiHelper.SetFinishStats();
	            return false;
	        }
	        return true;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return true;
    }

    private int SetProgress(boolean blnImprovementFound)
    {
        // go to next iteration
        CurrentIteration++;

        //
        // calculate percentage completion
        //
        int intPercentage = (MaxConvergence * 100) /
                            HeuristicProblem.getConvergence();
        int intPercentage2 = (CurrentIteration * 100) /
                             HeuristicProblem.getIterations();
        intPercentage = Math.max(intPercentage, intPercentage2);
        intPercentage = InitialCompletionPercentage +
                        ((intPercentage * PercentageCompletionValue) / 100);

        OptiGuiHelper.SetProgress(
            blnImprovementFound,
            intPercentage);
        return intPercentage;
    }

    private boolean CheckConvergence()
    {
    	try
    	{
	        Individual bestIndividual = HeuristicProblem.getPopulation().GetIndividualFromPopulation(
	            HeuristicProblem,
	            0);
	        boolean blnImprovementFound = false;
	        if (m_dblBestFitness < bestIndividual.getFitness())
	        {
	            m_dblBestFitness = bestIndividual.getFitness();
	            CurrentConvergence = 0;
	            blnImprovementFound = true;
	            SaveBestIndividual();
	            InvokeImprovementFound(bestIndividual);
	        }
	        else
	        {
	            CurrentConvergence++;
	        }
	
	        OptiGuiHelper.UpdateConvergence(bestIndividual);
	
	
	        if (MaxConvergence < CurrentConvergence)
	        {
	            MaxConvergence = CurrentConvergence;
	        }
	        return blnImprovementFound;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return false;
    }

    private void LoadInitialPopulation()
    {
        HeuristicProblem.getInitialPopulation().GetInitialPopulation();

        //
        // load population
        //
        IterateGeneration();
    }


    /// <summary>
    ///   Initialize solver
    /// </summary>
    private void InitializeSolver()
    {
        ResetSolverStatus();
        LoadInitialPopulation();
    }

    private void ResetSolverStatus()
    {
        m_blnIterateSolver = true;
        MaxConvergence = -1;
        StartTime = DateTime.now();
        m_dblBestFitness = -Double.MAX_VALUE;
        CurrentIteration = 0;
        SolutionsExplored = 0;
    }

    /// <summary>
    ///   Call this method each time the local search 
    ///   operator finds an improvement
    /// </summary>
    private void InvokeOnImprovementFoundEvent()
    {
        // reset convergence
        CurrentConvergence = 0;
    }

    @Override
    public void Dispose()
    {
        if(OptiGuiHelper != null)
        {
            OptiGuiHelper.Dispose();
            OptiGuiHelper = null;
        }
        if(HeuristicExecutionHelper != null)
        {
            HeuristicExecutionHelper.Dispose();
            HeuristicExecutionHelper = null;
        }
    }
}
