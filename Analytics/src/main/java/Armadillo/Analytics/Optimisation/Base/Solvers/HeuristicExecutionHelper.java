package Armadillo.Analytics.Optimisation.Base.Solvers;

import java.util.List;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.IndividualWorker;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Core.Logger;
import Armadillo.Core.Concurrent.ILoopBody;
import Armadillo.Core.Concurrent.Parallel;

public class HeuristicExecutionHelper
{
    private boolean m_blnRunThread;
    private int m_intBatchRunSize;

    public void OnRunBatchIndividuals(List<Individual> individualList)
    {
    	
    }
    
    private HeuristicProblem m_heuristicProblem;

    /// <summary>
    ///   Thread workers. 
    ///   Each individual worker generates a new offspring in a loop
    /// </summary>
    private IndividualWorker[] m_workerArr;

    public HeuristicExecutionHelper(HeuristicProblem heuristicProblem)
    {
        m_heuristicProblem = heuristicProblem;
        m_blnRunThread = true;
    }

    public void ExecuteSolver()
    {
        if (m_heuristicProblem.getThreads() > 1)
        {
            // run individual threads
            RunIndividualThreads();
        }
        else if (m_heuristicProblem.getThreads() == 1)
        {
            // run one single thread
            RunOneThread();
        }
        else if (m_heuristicProblem.getThreads() == 0)
        {
            // run all threads at once
            RunAllThreads();
        }
        //
        // load population for last iteration
        //
        m_heuristicProblem.getPopulation().LoadPopulation();
    }

    private void RunOneThread()
    {
        IndividualWorker individualWorker = CreateIndividualWorker();

        for (int i = 0;
             i <
             m_heuristicProblem.getIterations()*
             m_heuristicProblem.PopulationSize();
             i++)
        {
            if (m_blnRunThread)
            {
                individualWorker.IterateIndividual();
            }
            else
            {
                break;
            }
        }

        FinishThreads();
    }

    private void RunAllThreads()
    {
        RunAllThreads0();
        FinishThreads();
    }

    private void RunAllThreads0()
    {
    	final IndividualWorker individualWorker = CreateIndividualWorker();
    	
    	Parallel.For(0, m_heuristicProblem.getIterations()*
                m_heuristicProblem.PopulationSize(), new ILoopBody<Integer>() 
    	{
			public void run(Integer i) 
			{
                if (m_blnRunThread)
                {
                    individualWorker.IterateIndividual();
                    return;
                }
			}
		});
    }

    public void FinishSolver()
    {
        // run individual threads
        FinishThreads();
    }

    private void RunIndividualThreads()
    {
        CreateIndividualWorkers();

        // load the initial threads
    	Parallel.For(0, m_heuristicProblem.getThreads(), new ILoopBody<Integer>() 
    	{
			public void run(Integer i) 
			{
				m_workerArr[i].Work();
			}
		});
        
        
        FinishThreads();
    }

    private void CreateIndividualWorkers()
    {
        m_workerArr = new IndividualWorker[
            m_heuristicProblem.getThreads()];

        for (int i = 0;
             i <
             m_heuristicProblem.getThreads();
             i++)
        {
            IndividualWorker worker = CreateIndividualWorker();
            m_workerArr[i] = worker;
        }
    }

    private IndividualWorker CreateIndividualWorker()
    {
    	try
    	{
	        Individual newIndividual =
	            m_heuristicProblem.getReproduction().DoReproduction();
	
	        IndividualWorker worker = new IndividualWorker(
	            newIndividual,
	            this,
	            m_heuristicProblem)
	        {
	        	@Override
	        	public void WorkerReadyEventHandler(Individual individual) 
	        	{
	        		try
	        		{
		        		m_heuristicProblem.getSolver().IterateIndividual(individual);
		        	}
		        	catch(Exception ex)
		        	{
		        		Logger.log(ex);
		        	}
	        	}
	        };
	
	        return worker;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }

    /// <summary>
    ///   Finalize threads
    /// </summary>
    private void FinishThreads()
    {
        m_blnRunThread = false;
    }

    public void RunBatchIndividuals(List<Individual> individualList)
    {
            OnRunBatchIndividuals(individualList);
            for (int i = 0; i < individualList.size(); i++)
            {
                individualList.get(i).AckEvaluate(m_heuristicProblem);
                List<Individual> innerIndividualList = individualList.get(i).getIndividualList();
                for (int j = 0; j < innerIndividualList.size(); j++)
                {
                    innerIndividualList.get(j).SetFitnessValue(individualList.get(i).getFitness());
                }

                m_workerArr[0].InvokeWokerReadyEventHandler(individualList.get(i));
            }
    }

    public void Dispose()
    {
        m_blnRunThread = false;
        if(m_workerArr != null)
        {
            for (int i = 0; i < m_workerArr.length; i++)
            {
                m_workerArr[i].Dispose();
            }
            m_workerArr = null;
        }
        m_heuristicProblem = null;
    }

	public boolean isRunThread() {
		return m_blnRunThread;
	}

	public void setRunThread(boolean runThread) {
		m_blnRunThread = runThread;
	}

	public int getBatchRunSize() {
		return m_intBatchRunSize;
	}

	public void setBatchRunSize(int batchRunSize) {
		m_intBatchRunSize = batchRunSize;
	}
}