package Armadillo.Analytics.Optimisation.Base.Solvers;

import java.util.List;

import Armadillo.Analytics.Optimisation.Base.DataStructures.ResultRow;
import Armadillo.Analytics.Optimisation.Base.Delegates.CompletedIterationDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.ExceptionOccurredDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.SolverFinishedDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.UpdateProgressDelegate;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

/// <summary>
///   Solver interface
/// </summary>
public interface ISolver 
{
    /// <summary>
    ///   Solve
    /// </summary>
    void Solve();

    /// <summary>
    ///   Get solver name
    /// </summary>
    /// <returns>
    ///   Solver name
    /// </returns>
    String GetSolverName();

    void SetSolverName(String strSolverName);
    
    void Dispose();

	void addUpdateProgressDelegate(
			UpdateProgressDelegate updateProgressBarDelegate);
	
	void addExceptionOccurredDelegate(ExceptionOccurredDelegate exceptionOccurredDelegate);
	
	void addSolverFinishDelegate(SolverFinishedDelegate solverFinishedDelegate);
	
	void addCompletedIterationDelegate(CompletedIterationDelegate completedIterationDelegate);
    
    
    /// <summary>
    ///   Finalize solver
    /// </summary>
    /// <param name = "resultList">
    ///   List with results
    /// </param>
    void InvokeSolverFinished(List<ResultRow> resultList);

    /// <summary>
    ///   Event called when an exception is thrown
    /// </summary>
    /// <param name = "e">
    ///   HCException
    /// </param>
    void invokeExceptionOccurred(Exception e);

    /// <summary>
    ///   Update progress bar
    /// </summary>
    /// <param name = "intProgress">
    ///   Progress value
    /// </param>
    /// <param name = "strMessage"></param>
    void invokeUpdateProgress(int intProgress, String strMessage);
    
    /// <summary>
    ///   Call this method when a improvent has been found
    /// </summary>
    void InvokeImprovementFound(Individual bestSolution);
    
    void InvokeCompletedIterationDelegate(HeuristicProblem heuristicProblem);
}