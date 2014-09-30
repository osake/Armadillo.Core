package Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch;

import Armadillo.Analytics.Optimisation.Base.Delegates.NmImprovementFoundDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.UpdateProgressDelegate;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.ANmPopulationGenerator;
import Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.NmConstants;
import Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.NmSolver;
import Armadillo.Core.Logger;

/// <summary>
///   Nelder-Mead Local search
/// </summary>
public abstract class ALocalSearchNm extends ALocalSearch
{
    private final Object m_nmCounterLockObject = new Object();

    /// <summary>
    ///   Number of Nelder-Mead solvers
    /// </summary>
    private int m_intNmCounter;

    protected ANmPopulationGenerator m_nmPopulationGenerator;
    private int m_intNmLimit;

    protected ALocalSearchNm(
        HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
        LoadNmLimit();
    }

    private  void LoadNmLimit()
    {
        int intThreads;
        if (m_heuristicProblem.getThreads() == 0)
        {
            intThreads = Integer.MAX_VALUE;
        }
        else
        {
            intThreads = m_heuristicProblem.getThreads();
            intThreads = intThreads == 1 ? 2 : intThreads;
        }
        m_intNmLimit = Math.min(intThreads, NmConstants.NM_THREADS);
    }

    @Override
    public void DoLocalSearch(Individual individual)
    {
    	try
    	{
	        synchronized (m_nmCounterLockObject)
	        {
		        if (!ValidateNmInstances())
		        {
		            return;
		        }
	
	            m_intNmCounter++;
	        }
	
	        m_intLocaSearchIterations = 0;
	
	        //
	        // run Nelder-Mead algorithm
	        //
	        NmSolver nmSolver = LoadNmSolver();
	
	        nmSolver.addUpdateProgressDelegate(new UpdateProgressDelegate()
	        {
	        	public void invoke(int intProgress, String strMessage)
	        	{
	        		InvokeUpdateProgress(intProgress, strMessage);
	        	}
	        });
	        
	        nmSolver.addNmImprovementFoundDelegates(new NmImprovementFoundDelegate()
	        {
	        	@Override
	        	public void invoke(NmSolver nmSolver, Individual bestIndividual) 
	        	{
	        		NmSolverOnOnImprovementFound(nmSolver, bestIndividual);
	        	}
	        });
	        
	        nmSolver.Solve();
	
	        Individual bestNmSolution = nmSolver.GetBestSolution();
	        //
	        // if imrovement found, the cluster solution
	        //
	        if (m_heuristicProblem.getPopulation().GetIndividualFromPopulation(
	            m_heuristicProblem,
	            0).getFitness() <
	            bestNmSolution.getFitness())
	        {
	            bestNmSolution.Clone(m_heuristicProblem).Evaluate(
	                false,
	                false,
	                true,
	                m_heuristicProblem);
	        }
	        synchronized (m_nmCounterLockObject)
	        {
	            m_intNmCounter--;
	        }
	        nmSolver.Dispose();
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    /// <summary>
    ///   Improvement found by Nelder-Mead
    /// </summary>
    private void NmSolverOnOnImprovementFound(NmSolver nmSolver, Individual bestIndividual)
    {
        m_intLocaSearchIterations = 0;
        InvokeUpdateProgress(-1, "");
    }

    /// <summary>
    ///   Validate conditions for NM the solver
    /// </summary>
    public boolean ValidateNmSolver(int intLocaSearchIterations)
    {
    	try
    	{
	        if (!ValidateNmInstances())
	        {
	            return false;
	        }
	
	        if (intLocaSearchIterations < 10)
	        {
	            return false;
	        }
	
	        return true;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return false;
    }

    private boolean ValidateNmInstances()
    {
        if (m_intNmCounter >= m_intNmLimit)
        {
            return false;
        }

        synchronized (m_nmCounterLockObject)
        {
            if (m_intNmCounter >= m_intNmLimit)
            {
                return false;
            }
        }
        return true;
    }

    protected abstract NmSolver LoadNmSolver();

    protected abstract double GetChromosomeValue(
        Individual individual,
        int intIndex);

    protected abstract void AddChromosomeValue(
        Individual individual,
        int intIndex,
        double dblWeight);

    protected abstract void RemoveChromosomeValue(
        Individual individual,
        int intIndex,
        double dblWeight);

    protected abstract double[] GetChromosomeCopy(
        Individual individual);

    protected abstract double GetMaxChromosomeValue(int intIndex);

    protected abstract Individual BuildIndividual(double[] dblChromosomeArr, double dblFitness);

}
