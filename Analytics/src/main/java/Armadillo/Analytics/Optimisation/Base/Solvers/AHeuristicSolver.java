package Armadillo.Analytics.Optimisation.Base.Solvers;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import Armadillo.Analytics.Optimisation.Base.EvaluationStateType;
import Armadillo.Analytics.Optimisation.Base.OptimisationConstants;
import Armadillo.Analytics.Optimisation.Base.DataStructures.ResultRow;
import Armadillo.Analytics.Optimisation.Base.Delegates.CompletedIterationDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.ExceptionOccurredDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.ImprovementFoundDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.IndividualEvaluatedDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.IndividualReadyDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.SolverFinishedDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.UpdateProgressDelegate;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Core.Config;

/// <summary>
///   Abstract solver.
///   Generic form of a solver
/// </summary>
public abstract class AHeuristicSolver implements ISolver
{
    /// <summary>
    ///   Solver's best individual
    /// </summary>
    protected Individual m_bestIndividual;

    /// <summary>
    ///   Best fitness found so far
    /// </summary>
    protected double m_dblBestFitness;

    /// <summary>
    ///   Final results percentage of completion
    /// </summary>
    protected int m_intFinalResultsPercentage;

    protected int m_intIterationCounter;


    /// <summary>
    ///   Number of results calculated
    /// </summary>
    protected int m_intResultCounter;

    protected String m_strSolverName;

    /// <summary>
    ///   Current level of convergence.
    ///   This value is reset if an improvement is found
    /// </summary>
    public int CurrentConvergence;
    
    /// <summary>
    ///   Solver start time
    /// </summary>
    public DateTime StartTime;

    /// <summary>
    ///   Solutions explored
    /// </summary>
    public int SolutionsExplored;

    /// <summary>
    ///   Current iteration
    /// </summary>
    public int CurrentIteration;
    
    /// <summary>
    ///   Maximum level of convergence. The covergence
    ///   value is reset to zero if an improvement is
    ///   found
    /// </summary>
    public int MaxConvergence;

    /// <summary>
    ///   Number of results explored by the solver
    ///   finished
    /// </summary>
    public List<ResultRow> ResultList;

    /// <summary>
    ///   Number of results to display once the solver is
    /// </summary>
    public int ResultsSize;

    /// <summary>
    ///   Flag which states if the final results are to be analysed
    /// </summary>
    public boolean EvaluateFinalResults;

    /// <summary>
    ///   Completion percentage
    /// </summary>
    public int InitialCompletionPercentage;

    /// <summary>
    ///   Total percentage value
    /// </summary>
    public int PercentageCompletionValue;

    public HeuristicProblem HeuristicProblem;
    
    private List<ImprovementFoundDelegate> m_improvementFoundDelegates;
    private List<UpdateProgressDelegate> m_updateProgressBarDelegates;
    private List<ExceptionOccurredDelegate> m_exceptionOccurredDelegates;
    private List<SolverFinishedDelegate> m_solverFinishedDelegates;
    private List<CompletedIterationDelegate> m_completedIterationDelegates;
    private List<IndividualReadyDelegate> m_individualReadyDelegates;
    private List<IndividualEvaluatedDelegate> m_individualEvaluatedDelegates;
    
    public AHeuristicSolver(
        HeuristicProblem heuristicProblem)
    {
    	m_improvementFoundDelegates = new ArrayList<ImprovementFoundDelegate>();
    	m_updateProgressBarDelegates = new ArrayList<UpdateProgressDelegate>(); 
    	m_exceptionOccurredDelegates = new ArrayList<ExceptionOccurredDelegate>();
    	m_solverFinishedDelegates = new ArrayList<SolverFinishedDelegate>();
    	m_completedIterationDelegates = new ArrayList<CompletedIterationDelegate>();
    	m_individualReadyDelegates = new ArrayList<IndividualReadyDelegate>();
    	m_individualEvaluatedDelegates = new ArrayList<IndividualEvaluatedDelegate>();
    	
        HeuristicProblem = heuristicProblem;
        EvaluateFinalResults = true;
        m_intFinalResultsPercentage = OptimisationConstants.INT_FINAL_RESULTS_PERCENTAGE;
        ResultsSize = Integer.parseInt(Config.getStringStatic("ResultSize"));
    }

    /// <summary>
    ///   Save best individual distcription into a text file
    /// </summary>
    protected void SaveBestIndividual()
    {
    }

    /// <summary>
    ///   Solve
    /// </summary>
    public abstract void Solve();

    /// <summary>
    ///   Get the name of the current solver
    /// </summary>
    /// <returns></returns>
    public String GetSolverName()
    {
        return (m_strSolverName == null ? "" : m_strSolverName);
    }

    public void SetSolverName(String strSolverName)
    {
        m_strSolverName = strSolverName;
    }

    public void Dispose()
    {
        if(ResultList != null)
        {
            ResultList.clear();
        }
        m_bestIndividual = null;
    }
    
    public void addIndividualReadyDelegate(
    		IndividualReadyDelegate individualReadyDelegate)
    {
    	m_individualReadyDelegates.add(individualReadyDelegate);
    }
    
    public void addIndividualEvaluatedDelegate(
    		IndividualEvaluatedDelegate individualEvaluatedDelegate)
    {
    	m_individualEvaluatedDelegates.add(individualEvaluatedDelegate);
    }
    
	public void addUpdateProgressDelegate(
			UpdateProgressDelegate updateProgressBarDelegate) 
	{
		m_updateProgressBarDelegates.add(updateProgressBarDelegate);
	}
	
	public void addExceptionOccurredDelegate(ExceptionOccurredDelegate exceptionOccurredDelegate)
	{
		m_exceptionOccurredDelegates.add(exceptionOccurredDelegate);
	}
	
	public void addSolverFinishDelegate(SolverFinishedDelegate solverFinishedDelegate)
	{
		m_solverFinishedDelegates.add(solverFinishedDelegate);
	}
	
	public void addCompletedIterationDelegate(CompletedIterationDelegate completedIterationDelegate)
	{
		m_completedIterationDelegates.add(completedIterationDelegate);
	}
	
    /// <summary>
    ///   Finalize solver
    /// </summary>
    /// <param name = "resultList">
    ///   List with results
    /// </param>
    public void InvokeSolverFinished(List<ResultRow> resultList)
    {
    	for(SolverFinishedDelegate solverFinishDelegate : m_solverFinishedDelegates)
    	{
    		solverFinishDelegate.invoke(resultList);
    	}
    }

    /// <summary>
    ///   Event called when an exception is thrown
    /// </summary>
    /// <param name = "e">
    ///   HCException
    /// </param>
    public void invokeExceptionOccurred(Exception e)
    {
    	for(ExceptionOccurredDelegate exceptionOccurredDelegate : m_exceptionOccurredDelegates)
    	{
    		exceptionOccurredDelegate.invoke(e);
    	}
    }

    /// <summary>
    ///   Update progress bar
    /// </summary>
    /// <param name = "intProgress">
    ///   Progress value
    /// </param>
    /// <param name = "strMessage"></param>
    public void invokeUpdateProgress(int intProgress, String strMessage)
    {
    	for(UpdateProgressDelegate updateProgressBarDelegate : m_updateProgressBarDelegates)
    	{
    		updateProgressBarDelegate.invoke(intProgress, strMessage);
    	}
    }
    
    
    /// <summary>
    ///   Call this method when a improvent has been found
    /// </summary>
    public void InvokeImprovementFound(Individual bestSolution)
    {
    	for (ImprovementFoundDelegate improvementFoundDelegate : m_improvementFoundDelegates) 
    	{
    		improvementFoundDelegate.invoke(bestSolution);
		}
    }
    
    public void invokeIndividualReadyDelegate(EvaluationStateType state)
    {
    	for(IndividualReadyDelegate individualReadyDelegate : m_individualReadyDelegates)
    	{
			individualReadyDelegate.invoke(state);
    	}
    }

    public void invokeIndividualEvaluatedDelegate(Individual state)
    {
    	for(IndividualEvaluatedDelegate individualEvaluatedDelegate : 
    		m_individualEvaluatedDelegates)
    	{
			individualEvaluatedDelegate.invoke(state);
    	}
    }
    
    public void InvokeCompletedIterationDelegate(HeuristicProblem heuristicProblem)
    {
    	for(CompletedIterationDelegate completedIterationDelegate : m_completedIterationDelegates)
    	{
    		completedIterationDelegate.invoke(heuristicProblem);
    	}
    }
}
