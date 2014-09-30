package Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Base.Solvers.HeuristicExecutionHelper;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;

/// <summary>
///   Worker for evaluating individuals.
///   Note: This class is non threadsafe.
/// </summary>
public class IndividualWorker
{
    /// <summary>
    ///   Event called when exception ocurs while evaluating individual
    /// </summary>
    public void ExceptionOccurred(HCException e)
    {
    	
    }

    /// <summary>
    ///   Event called when individual is finish with evaluation
    /// </summary>
    public void WorkerReadyEventHandler(Individual individual)
    {
    	
    }

    /// <summary>
    ///   Event called when individual is finish with evaluation
    /// </summary>
    public void OnWorkerFinishedEventHandler()
    {
    	
    }

    private HeuristicExecutionHelper m_heuristicExecutionHelper;
    private HeuristicProblem m_heuristicProblem;

    /// <summary>
    ///   IIndividual to be evaluated
    /// </summary>
    private Individual m_individual;

    /// <summary>
    ///   Constrcutor
    /// </summary>
    /// <param name = "individual">
    ///   IIndividual to be evalauted
    /// </param>
    public IndividualWorker(
        Individual individual,
        HeuristicExecutionHelper heuristicExecutionHelper,
        HeuristicProblem heuristicProblem)
    {
        m_heuristicProblem = heuristicProblem;
        m_individual = individual;
        m_heuristicExecutionHelper = heuristicExecutionHelper;
    }

    /// <summary>
    ///   Do work
    /// </summary>
    private void DoWork()
    {
        try
        {
            m_individual.Evaluate(m_heuristicProblem);
            InvokeWokerReadyEventHandler(m_individual);

            while (m_heuristicExecutionHelper.isRunThread())
            {
                IterateIndividual();
            }
        }
        catch (HCException e2)
        {
            //Logger.GetLogger().Write(e2);
            //Debugger.Break();
            OnExceptionOccurred(e2);
        }
        InvokeWokerFinishedEventHandler();
    }

    public void IterateIndividual()
    {
    	try
    	{
	        if (m_heuristicExecutionHelper.isRunThread())
	        {
	            if (m_heuristicExecutionHelper.getBatchRunSize() > 0)
	            {
	            	List<Individual> individualList = new ArrayList<Individual>();
	                for (int i = 0; i < m_heuristicExecutionHelper.getBatchRunSize(); i++)
	                {
	                    Individual newIndividual =
	                        m_heuristicProblem.getReproduction().DoReproduction();
	                    individualList.add(newIndividual);
	                }
	                m_heuristicExecutionHelper.RunBatchIndividuals(individualList);
	            }
	            else
	            {
	                Individual newIndividual =
	                    m_heuristicProblem.getReproduction().DoReproduction();
	                newIndividual.Evaluate(m_heuristicProblem);
	                InvokeWokerReadyEventHandler(newIndividual);
	            }
	        }
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    /// <summary>
    ///   Do Work
    /// </summary>
    public void Work()
    {
        DoWork();

        //ThreadWorker worker = new ThreadWorker();
        //worker.WaitForExit = true;
        //worker.m_onDelegateThreadExecute += DoWork;
        //worker.Work();
    }

    /// <summary>
    ///   Call this method when worker is finish
    /// </summary>
    public void InvokeWokerReadyEventHandler(Individual individual)
    {
    	WorkerReadyEventHandler(individual);
    }

    /// <summary>
    ///   Call this method when worker is finish
    /// </summary>
    public void InvokeWokerFinishedEventHandler()
    {
    	OnWorkerFinishedEventHandler();
    }

    /// <summary>
    ///   Call this method when worker evaluation throws an exception
    /// </summary>
    /// <param name = "e"></param>
    protected void OnExceptionOccurred(HCException e)
    {
    	ExceptionOccurred(e);
    }

    public void Dispose()
    {
        m_heuristicExecutionHelper = null;
        m_heuristicProblem = null;
        m_individual = null;
    }
}
