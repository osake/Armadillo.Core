package Armadillo.Analytics.Optimisation.Base;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Solvers.EnumEvolutionarySolver;
import Armadillo.Analytics.Optimisation.Base.Solvers.EvolutionarySolver;
import Armadillo.Analytics.Optimisation.MixedSolvers.DummyObjectiveFunctions.MixedObjectiveFunctionDummy;
import Armadillo.Core.Environment;
import Armadillo.Core.Guid;
import Armadillo.Core.Logger;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Core.Concurrent.EfficientProducerConsumerQueue;
import Armadillo.Core.Math.RollingWindowStdDev;
import Armadillo.Core.SelfDescribing.SelfDescribingTsEvent;
import Armadillo.Core.UI.PublishUiMessageEvent;

public class OptiGuiHelper
{
    private EvolutionarySolver m_evolutionarySolver;
    private final SelfDescribingTsEvent m_solverStats;
    private EfficientProducerConsumerQueue<ObjectWrapper> m_efficientQueueChart;
    private RollingWindowStdDev m_individualStats;
    private DateTime m_lastUpdateTime = DateTime.now();
    private final String m_strSolverName;
    private int m_intPreviousPercentage;
    private DateTime m_prevProgress = DateTime.now();

    public OptiGuiHelper(
        EvolutionarySolver evolutionarySolver)
    {
        m_evolutionarySolver = evolutionarySolver;
        m_individualStats = new RollingWindowStdDev(100);
        //
        // get solver stats
        //
        m_strSolverName = evolutionarySolver.GetSolverName();
        String strClassName =
            "name_" +
            (evolutionarySolver.HeuristicProblem.getProblemName() + "_" +
             (m_strSolverName == null ? "" : m_strSolverName))
                .replace(";", "_")
                .replace(",", "_")
                .replace(".", "_")
                .replace(":", "_")
                .replace("-", "_");
        m_solverStats = new SelfDescribingTsEvent(
            strClassName);
        m_solverStats.SetStrValue(
            EnumEvolutionarySolver.ProblemName,
            evolutionarySolver.HeuristicProblem.getProblemName());
        m_solverStats.SetStrValue(
            EnumEvolutionarySolver.SolverName,
            m_strSolverName);
        m_solverStats.SetIntValue(
            EnumEvolutionarySolver.MaxConvergence,
            evolutionarySolver.MaxConvergence);

        m_efficientQueueChart = new EfficientProducerConsumerQueue<ObjectWrapper>(1)
        		{
        	@Override
        	public void runTask(ObjectWrapper item) throws Exception 
        	{
        		PublishGridStats();
        	}
        };
        m_efficientQueueChart.add("PublishQueue", new ObjectWrapper());

    }

    private void PublishGridStats()
    {
        String strProblemName =
            m_solverStats.GetStrValue(
                EnumEvolutionarySolver.ProblemName);
        m_solverStats.Time = DateTime.now();

        if (!(m_evolutionarySolver.HeuristicProblem.getObjectiveFunction() instanceof MixedObjectiveFunctionDummy))
        {
            PublishUiMessageEvent.PublishGrid(
                strProblemName,
                strProblemName,
                EnumEvolutionarySolver.SolverStats.toString(),
                m_solverStats.GetClassName(),
                m_solverStats,
                2,
                true);
        }
    }

    public void UpdateStats()
    {
        //
        // update stats of how often an individual is evaluated
        //
        //DateTime now = DateTime.now();
        if (m_lastUpdateTime.getYear() == DateTime.now().getYear())
        {
            m_individualStats.Update(Seconds.secondsBetween(m_lastUpdateTime, DateTime.now()).getSeconds());
            m_solverStats.SetDblValue(
                EnumEvolutionarySolver.IndividualsPerSecond,
                1.0 / m_individualStats.Mean());
        }
        m_lastUpdateTime = DateTime.now();
    }

    public void SetFinishStats()
    {
        m_solverStats.SetBlnValue(
            EnumEvolutionarySolver.StopSolver,
            true);
        m_efficientQueueChart.add("PublishQueue", new ObjectWrapper());
    }

    public int SetProgress(
        boolean blnImprovementFound,
        int intPercentage)
    {
        Individual bestIndividual = null;
        if (blnImprovementFound)
        {
            if (m_evolutionarySolver.HeuristicProblem.isVerbose())
            {
            	
                if (Seconds.secondsBetween(m_prevProgress, DateTime.now()).getSeconds() > 2)
                {
                    bestIndividual = m_evolutionarySolver.HeuristicProblem.getPopulation().GetIndividualFromPopulation(
                        m_evolutionarySolver.HeuristicProblem,
                        0);
                    StringBuilder sb = new StringBuilder();
                    sb.append(Environment.NewLine + "-------------------------------------" + Environment.NewLine);
                    sb.append("(*) " + m_strSolverName + ". Iteration " + 
                        m_evolutionarySolver.CurrentIteration + " of ");
                    sb.append(m_evolutionarySolver.HeuristicProblem.getIterations());
                    sb.append(", percentage: " + intPercentage);
                    sb.append(Environment.NewLine + "Best Individual Description:");
                    sb.append(Environment.NewLine +
                        m_evolutionarySolver.HeuristicProblem.getPopulation().GetIndividualFromPopulation(
                        m_evolutionarySolver.HeuristicProblem,
                        0));
                    sb.append(Environment.NewLine + Environment.NewLine + "Objective = " +
                              bestIndividual.getFitness());
                    sb.append(Environment.NewLine + Environment.NewLine +
                              m_evolutionarySolver.HeuristicProblem.getPopulation().ToStringPopulationStats());
                    sb.append(Environment.NewLine + "-------------------------------------" + Environment.NewLine);

                    String strMessage = sb.toString();
                    PublishLog(strMessage);
                    m_evolutionarySolver.invokeUpdateProgress(intPercentage, strMessage);
                    m_prevProgress = DateTime.now();
                }
            }
        }
        if (m_intPreviousPercentage != intPercentage)
        {
            if (m_evolutionarySolver.HeuristicProblem.isVerbose())
            {
                if (Seconds.secondsBetween(m_prevProgress, DateTime.now()).getSeconds() > 2)
                {
                    bestIndividual = m_evolutionarySolver.HeuristicProblem.getPopulation().GetIndividualFromPopulation(
                        m_evolutionarySolver.HeuristicProblem,
                        0);
                    m_intPreviousPercentage = intPercentage;
                    StringBuilder sb = new StringBuilder();
                    sb.append(Environment.NewLine + "-------------------------------------" + Environment.NewLine);
                    sb.append(m_strSolverName + ", " + intPercentage + "% completed...");
                    sb.append(Environment.NewLine + "Iteration " + m_evolutionarySolver.CurrentIteration + " of ");
                    sb.append(m_evolutionarySolver.HeuristicProblem.getIterations());
                    sb.append(Environment.NewLine + Environment.NewLine + "Objective = ");
                    sb.append(bestIndividual.getFitness());
                    sb.append(Environment.NewLine + Environment.NewLine +
                              m_evolutionarySolver.HeuristicProblem.getPopulation().ToStringPopulationStats());
                    sb.append(Environment.NewLine + "-------------------------------------" + Environment.NewLine);
                    String strMessage = sb.toString();
                    PublishLog(strMessage);
                    m_evolutionarySolver.invokeUpdateProgress(intPercentage, strMessage);
                    m_prevProgress = DateTime.now();
                }
            }
        }

        if (bestIndividual != null)
        {
            m_solverStats.SetIntValue(
                EnumEvolutionarySolver.Percentage,
                intPercentage);
            UpdateSolverStats(
                bestIndividual);
            m_efficientQueueChart.add("PublishQueue", new ObjectWrapper());
        }

        return intPercentage;
    }

    public void PublishLog(
        String strLog)
    {
        PublishUiMessageEvent.PublishLog(
            m_evolutionarySolver.HeuristicProblem.getProblemName(),
            m_evolutionarySolver.HeuristicProblem.getProblemName(),
            EnumEvolutionarySolver.Log.toString(),
            Guid.NewGuid().toString(),
            strLog);
    }

    private void UpdateSolverStats(
        Individual bestIndividual)
    {
    	try
    	{
	        String strPopulationStats = m_evolutionarySolver.HeuristicProblem.getPopulation().ToStringPopulationStats();
	        double dblTotalTimeSecs = Seconds.secondsBetween(m_evolutionarySolver.StartTime, DateTime.now()).getSeconds();
	        m_solverStats.SetDblValue(
	            EnumEvolutionarySolver.MaxIterations,
	            m_evolutionarySolver.HeuristicProblem.getIterations());
	        m_solverStats.SetDblValue(
	            EnumEvolutionarySolver.Objective,
	            bestIndividual.getFitness());
	        m_solverStats.SetStrValue(
	            EnumEvolutionarySolver.BestIndividual,
	            bestIndividual.toString());
	        m_solverStats.SetStrValue(
	            EnumEvolutionarySolver.PopulationStats,
	            strPopulationStats);
	        m_solverStats.SetDblValue(
	            EnumEvolutionarySolver.TotalTimeMins,
	            dblTotalTimeSecs / 60.0);
	        m_solverStats.SetIntValue(
	            EnumEvolutionarySolver.SolutionsExplored,
	            m_evolutionarySolver.SolutionsExplored);
	        m_solverStats.SetIntValue(
	            EnumEvolutionarySolver.Iterations,
	            m_evolutionarySolver.CurrentIteration);
	        m_solverStats.SetDateValue(
	            EnumEvolutionarySolver.StartTime,
	            m_evolutionarySolver.StartTime.toDate());
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    public void UpdateConvergence(Individual bestIndividual)
    {
    	try
    	{
	        m_solverStats.SetIntValue(
	            EnumEvolutionarySolver.CurrentConvergence,
	            m_evolutionarySolver.CurrentConvergence);
	        UpdateSolverStats(
	            bestIndividual);
	        m_efficientQueueChart.add("PublishQueue", new ObjectWrapper());
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    public void Dispose()
    {
        m_evolutionarySolver = null;
        if(m_solverStats != null)
        {
            m_solverStats.Dispose();
        }
        if(m_efficientQueueChart != null)
        {
            m_efficientQueueChart.dispose();
            m_efficientQueueChart = null;
        }
        if(m_individualStats != null)
        {
            m_individualStats.Dispose();
            m_individualStats = null;
        }
    }
}
