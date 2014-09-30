package Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import Armadillo.Analytics.Optimisation.Base.Delegates.IndividualReadyDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.NmImprovementFoundDelegate;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Solvers.AHeuristicSolver;
import Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.DataStructures.ANmVertex;
import Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.DataStructures.NmIterationType;
import Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.DataStructures.NmVertexComparator;
import Armadillo.Analytics.Optimisation.Integer.Operators.LocalSearch.NmLocalSearch.NmPopulationGeneratorInt;
import Armadillo.Core.Console;
import Armadillo.Core.Environment;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.PrintToScreen;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Base.EvaluationStateType;

/// <summary>
///   Nelder - Mead algorithm.
/// 
///   Solver used for continuous optimisation. Finds a local solution.
/// </summary>
public class NmSolver extends AHeuristicSolver
{
    private final int m_intNmDimensions;
    private Object m_lockObject1 = new Object();
    private ANmPopulationGenerator m_nmPopulationGenerator;
    private double m_dblLowerBound;
    private List<Individual> m_individualQueue;
    private int m_intPopulationReady;
    private int m_intQueueSize;
    private ANmVertex m_midVertex;
    private ANmVertex[] m_vertexArray;
    private final boolean m_blnIsIntegerProblem;
    private DateTime m_prevLogTime = DateTime.now();
    private List<NmImprovementFoundDelegate> m_nmImprovementFoundDelegates;
    
    public NmSolver(
        HeuristicProblem heuristicProblem,
        ANmPopulationGenerator abstractNmPopulationGenerator)
    {
    	super(heuristicProblem);
    	m_nmImprovementFoundDelegates = new ArrayList<NmImprovementFoundDelegate>(); 
        m_blnIsIntegerProblem = abstractNmPopulationGenerator instanceof NmPopulationGeneratorInt;
        m_strSolverName = "Nelder-Mead";

        m_nmPopulationGenerator = abstractNmPopulationGenerator;
        //
        // the number of dimensions is n+1
        //
        m_intNmDimensions =
            HeuristicProblem.VariableCount() + 1;
    	Console.writeLine("Running nm solver...");
    }

    /// <summary>
    ///   Solve
    /// </summary>
    @Override
    public void Solve()
    {
        try
        {
            Initialize();
            RunNm();
            //
            // consolidate population
            //
            HeuristicProblem.getPopulation().LoadPopulation();
        	Console.writeLine("Finish running nm solver. [" + CurrentIteration + "] iterations");
        }
        catch (Exception ex)
        {
            Logger.Log(ex);
        }
    }

    /// <summary>
    ///   Get best solution
    /// </summary>
    /// <returns></returns>
    public Individual GetBestSolution()
    {
    	try
    	{
	        Arrays.sort(m_vertexArray, new NmVertexComparator());
	        ArrayUtils.reverse(m_vertexArray);
	        return m_vertexArray[0].Individual_().Clone(HeuristicProblem);
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }

    /// <summary>
    ///   Get longest edge to best solution
    /// </summary>
    /// <returns></returns>
    public double GetLongestEdgeToBest()
    {
        double dblDistanceMax = 0.0;
        for (int i = 1; i < m_intNmDimensions; i++)
        {
            double dblDistance = m_vertexArray[i].DistanceTo(m_vertexArray[0]);
            if (dblDistanceMax < dblDistance)
            {
                dblDistanceMax = dblDistance;
            }
        }
        return dblDistanceMax;
    }

    /// <summary>
    ///   Iterate NM algorithm
    /// </summary>
    /// <returns></returns>
    public NmIterationType IterateNmAlgorithm()
    {
        for (int i = 0;
             i <
             HeuristicProblem.VariableCount();
             i++)
        {
            m_midVertex.SetVertexValue(i, 0.0);
            for (int j = 0;
                 j <
                 HeuristicProblem.VariableCount();
                 j++)
            {
                double dblChromosomeValue =
                    m_midVertex.GetVertexValue(i) +
                    m_vertexArray[j].GetVertexValue(i);

                m_midVertex.SetVertexValue(i, dblChromosomeValue);
            }
            double dblValue = m_midVertex.GetVertexValue(i) / HeuristicProblem.VariableCount();
            m_midVertex.SetVertexValue(i, dblValue);
        }

        ANmVertex refV = m_vertexArray[
            HeuristicProblem.VariableCount()].Combine(-1.0, m_midVertex);
        EvaluateVertex(refV);
        if (refV.Value < m_vertexArray[0].Value)
        {
            ANmVertex expV = m_vertexArray[HeuristicProblem.VariableCount()].Combine(-2.0, m_midVertex);
            EvaluateVertex(expV);
            if (expV.Value < refV.Value)
            {
                m_vertexArray[HeuristicProblem.VariableCount()] = expV;

                return NmIterationType.EXPANSION;
            }
            m_vertexArray[HeuristicProblem.VariableCount()] = refV;

            return NmIterationType.REFLECTION;
        }
        if (refV.Value >= m_vertexArray[HeuristicProblem.VariableCount()].Value)
        {
            ANmVertex icV = m_vertexArray[HeuristicProblem.VariableCount()].Combine(0.5, m_midVertex);
            EvaluateVertex(icV);
            if (icV.Value < m_vertexArray[HeuristicProblem.VariableCount()].Value)
            {
                m_vertexArray[HeuristicProblem.VariableCount()] = icV;

                return NmIterationType.SRINK;
            }
            Shrink(EvaluationStateType.INITIAL_STATE);
            return NmIterationType.INSIDE_CONTRACTION;
        }

        if (refV.Value > m_vertexArray[HeuristicProblem.VariableCount() - 1].Value)
        {
            ANmVertex ocV = m_vertexArray[HeuristicProblem.VariableCount()].Combine(-0.5, m_midVertex);
            EvaluateVertex(ocV);
            if (ocV.Value < refV.Value)
            {
                m_vertexArray[HeuristicProblem.VariableCount()] = ocV;

                return NmIterationType.SRINK;
            }
            Shrink(EvaluationStateType.INITIAL_STATE);
            return NmIterationType.OUTSIDE_CONTRACTION;
        }
        m_vertexArray[HeuristicProblem.VariableCount()] = refV;

        return NmIterationType.REFLECTION;
    }

    /// <summary>
    ///   To string results
    /// </summary>
    public void CheckResults()
    {
        double dblMaxValue = -Double.MAX_VALUE;
        int intMaxIndex = -1;
        for (int i = 0; i < m_intNmDimensions; i++)
        {
            if (dblMaxValue < -m_vertexArray[i].Value)
            {
                dblMaxValue = -m_vertexArray[i].Value;
                intMaxIndex = i;
            }
        }

        if (m_dblLowerBound < m_vertexArray[intMaxIndex].Value)
        {
            throw new HCException("NM weight is lower than the initial condition");
        }
        //
        // get individual from large population since
        // we are clustering in the large population directly
        //
        Individual bestIndividual =
            HeuristicProblem.getPopulation().GetIndividualFromLargePopulation(
                HeuristicProblem,
                0);

        if (bestIndividual == null || 
            (bestIndividual.getFitness() <
                -m_vertexArray[intMaxIndex].Value))
        {
            Individual bestSolution = GetBestSolution();
            HeuristicProblem.getPopulation().ClusterBestSolution(
                bestSolution,
                HeuristicProblem);

            LogProgress(bestSolution);
            InvokeImprovementFound(bestSolution);
        }
    }

    private void LogProgress(Individual bestVector)
    {
    	try
    	{
	        int intSeconds = Seconds.secondsBetween(
	        		m_prevLogTime, 
					DateTime.now()).getSeconds();        
	    	
	        if (intSeconds > 2)
	        {
	            String strMessage = GetSolverName() +
	                                ". Iteration " +
	                                CurrentIteration +
	                                " of " +
	                                HeuristicProblem.getIterations() +
	                                ". Best objective function = " +
	                                bestVector.getFitness() +
	                                ", " +
	                                Environment.NewLine +
	                                bestVector;
	
	            PrintToScreen.WriteLine(strMessage);
	            invokeUpdateProgress(-1, strMessage);
	            m_prevLogTime = DateTime.now();
	        }
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    @Override
    public void Dispose()
    {
    	try
    	{
	        super.Dispose();
	        m_lockObject1 = null;
	        m_nmPopulationGenerator = null;
	        if(m_individualQueue != null)
	        {
	            m_individualQueue.clear();
	        }
	        m_midVertex.Dispose();
	        if(m_vertexArray != null)
	        {
	            for (int i = 0; i < m_vertexArray.length; i++)
	            {
	                m_vertexArray[i].Dispose();
	            }
	        }
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    private void LoadGraphItems()
    {
        m_vertexArray = m_nmPopulationGenerator.GenerateVertexArray();
        //
        // set an arbitrary individual for the mid vertex
        //
        m_midVertex = m_nmPopulationGenerator.CreateNmVertex(
            HeuristicProblem.VariableCount(),
            m_vertexArray[0].Individual_().Clone(HeuristicProblem));
    }

    /// <summary>
    ///   GetTask algorithm
    /// </summary>
    private double RunNm()
    {
        double dblLongestEdgeToBest = Double.NaN;
        try
        {
            dblLongestEdgeToBest = GetLongestEdgeToBest();
            while (dblLongestEdgeToBest > 1E-8)
            {
                // sort results
                Arrays.sort(m_vertexArray, new NmVertexComparator());
                ArrayUtils.reverse(m_vertexArray);
                CheckResults();
                // stoping condition
                dblLongestEdgeToBest = GetLongestEdgeToBest();
                //Console.WriteLine("dblLongestEdgeToBest = " + dblLongestEdgeToBest);
                IterateNmAlgorithm();
                CurrentIteration++;
                if (CurrentIteration >= HeuristicProblem.getIterations())
                {
                    break;
                }
            }
        }
        catch (Exception ex)
        {
            Logger.Log(ex);
            Console.WriteLine(ex);
        }
        return dblLongestEdgeToBest;
    }

    /// <summary>
    ///   Get individual from a given vertex
    /// </summary>
    /// <param name = "vertex">
    ///   Vertex
    /// </param>
    /// <param name = "intVertedIndex">
    ///   Vertex index
    /// </param>
    /// <returns>
    ///   IIndividual
    /// </returns>
    private Individual GetIndividual(
        ANmVertex vertex,
        int intVertedIndex)
    {
        if (!m_blnIsIntegerProblem)
        {
            for (int i = 0;
                 i < HeuristicProblem.VariableCount();
                 i++)
            {
                if (vertex.GetVertexValue(i) > 1.0 ||
                    vertex.GetVertexValue(i) < 0.0)
                {
                    // penalise weights out of the [0,1] range
                    vertex.Value = Double.MAX_VALUE;
                    return null;
                }
            }
        }
        vertex.Individual_().setIndividualId(intVertedIndex);
        return vertex.Individual_();
    }

    /// <summary>
    ///   Evaluate vertex
    /// </summary>
    /// <param name = "vertex">
    ///   Vertex
    /// </param>
    /// <returns>
    ///   Vertex value
    /// </returns>
    private void EvaluateVertex(ANmVertex vertex)
    {
        Individual individual = GetIndividual(vertex, -1);
        if (individual == null)
        {
            return;
        }

        individual.Evaluate(false,
                            false,
                            false,
                            HeuristicProblem);
        LoadVetexFitness(
            individual,
            vertex);
    }

    /// <summary>
    ///   Interpolate individual
    /// </summary>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    /// <param name = "vertex">
    ///   Vertex
    /// </param>
    /// <returns>
    ///   Intepolated individual
    /// </returns>
    private void LoadVetexFitness(
        Individual individual,
        ANmVertex vertex)
    {
        //
        // Penalise individuals out of bounds.
        // Assign large negative fitness value
        //
        if (!HeuristicProblem.CheckConstraints(individual))
        {
            // 
            vertex.Value = Double.MAX_VALUE;
            return;
        }
        vertex.Value = -individual.getFitness();
    }

    /// <summary>
    ///   Initialize algorithm
    /// </summary>
    private void Initialize()
    {
        LoadGraphItems();
        //
        // get lower bound value
        //
        double dblMin = Double.MAX_VALUE;
        for (ANmVertex nmVertex : m_vertexArray) 
        {
        	dblMin = Math.min(nmVertex.Value, dblMin);  
		}
        m_dblLowerBound = dblMin;
        CurrentIteration = 0;
    }

    /// <summary>
    ///   Get a queue of individuals
    /// </summary>
    private void GetIndividualQueueList()
    {
        m_individualQueue = new ArrayList<Individual>(HeuristicProblem.VariableCount());
        for (int i = 1; i < m_intNmDimensions; i++)
        {
            m_vertexArray[i] = m_vertexArray[i].Combine(0.5, m_vertexArray[0]);


            Individual currentIndividual = GetIndividual(m_vertexArray[i], i);
            if (currentIndividual != null)
            {
                currentIndividual.addIndividualReadyDelegate(new IndividualReadyDelegate()
                {
                	@Override
                	public void invoke(EvaluationStateType state) 
                	{
                		Shrink(state);
                	}
                });

                m_individualQueue.add(currentIndividual);
            }
        }
        m_intQueueSize = m_individualQueue.size();
    }

    /// <summary>
    ///   Shrink vector
    /// </summary>
    /// <param name = "state">
    ///   IIndividual state
    /// </param>
    private void Shrink(EvaluationStateType state)
    {
        synchronized (m_lockObject1)
        {
            if (state == EvaluationStateType.INITIAL_STATE)
            {
                GetIndividualQueueList();
                EvaluatePopulation(m_individualQueue);
            }
            else
            {
                if (state == EvaluationStateType.SUCCESS_EVALUATION)
                {
                    m_intPopulationReady++;
                }
                else
                {
                    throw new HCException("Error. IIndividual not evaluated.");
                }
                // counts the number of individuals which finished with been evaluated
                if (m_intPopulationReady == m_intQueueSize)
                {
                    // interpretate each individual
                    for (Individual individual : m_individualQueue)
                    {
                        int intVertexId = individual.getIndividualId();
                        LoadVetexFitness(individual, m_vertexArray[intVertexId]);
                    }
                    // initialize number of individuals ready
                    m_intPopulationReady = 0;
                    // sort results
                    Arrays.sort(m_vertexArray, new NmVertexComparator());
                    ArrayUtils.reverse(m_vertexArray);
                    // run another iteration
                    RunNm();
                }
            }
        }
    }

    /// <summary>
    ///   Evaluate population
    /// </summary>
    /// <param name = "individualQueue">
    ///   IIndividual queue
    /// </param>
    private void EvaluatePopulation(List<Individual> individualQueue)
    {
        for (int i = 0; i < individualQueue.size(); i++)
        //Parallel.For(0, individualQueue.Count, delegate(int i)
        {
            EvaluateIndividual(individualQueue.get(i));
        }
        //);
    }

    /// <summary>
    ///   Evaluate individual
    /// </summary>
    /// <param name = "individual">
    ///   IIndividual
    /// </param>
    /// <returns></returns>
    private void EvaluateIndividual(Individual individual)
    {
        individual.Evaluate(false,
                            true,
                            true,
                            HeuristicProblem);
    }
    
    public void invokeNmImprovementFoundDelegate(NmSolver nmSolver, Individual bestIndividual)
    {
    	for(NmImprovementFoundDelegate nmImprovementFoundDelegate : m_nmImprovementFoundDelegates)
    	{
    		nmImprovementFoundDelegate.invoke(nmSolver, bestIndividual);
    	}
    }
    
    public void addNmImprovementFoundDelegates(NmImprovementFoundDelegate nmImprovementFoundDelegate)
    {
    	m_nmImprovementFoundDelegates.add(nmImprovementFoundDelegate);
    }
}
