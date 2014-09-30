package Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead;

import java.util.HashMap;
import java.util.Map;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.DataStructures.ANmVertex;
import Armadillo.Core.Logger;

/// <summary>
///   This class is used for local search purposes.
/// 
///   The Nelder-Mead algorithm requires n+1 solutions. The number
///   of required solutions is selected in this class.
/// 
///   Since the same solver may be called
///   multiple times (for each local search call), 
///   the solver should select a different starting point each
///   time it is executed (Otherwise the same solution may be found).
/// </summary>
public abstract class ANmPopulationGenerator
{
    protected HeuristicProblem m_heuristicProblem;
    private final int m_intNmDimensions;
    private Object m_lockObjectAddValidation = new Object();
    private Map<String, Object> m_solutionValidator;

    protected ANmPopulationGenerator(HeuristicProblem heuristicProblem)
    {
        m_heuristicProblem = heuristicProblem;
        m_solutionValidator = new HashMap<String, Object>();
        m_intNmDimensions = m_heuristicProblem.VariableCount() + 1;
    }

    public ANmVertex[] GenerateVertexArray()
    {
    	ANmVertex[] vertexArray = new ANmVertex[m_intNmDimensions];
        int intIndex = GetLowerIndividual(vertexArray);
        LoadVertexList(vertexArray, intIndex);
        return vertexArray;
    }

    protected void LoadVertexList(
        ANmVertex[] vertexArray, 
        int intIndex)
    {
        //
        // create a new validator dictionary in order to avoid
        // adding the same vertex to the vertex array
        //
        HashMap<String,Object> validationDictionary =
            new HashMap<String, Object>(m_intNmDimensions + 1);

        while (intIndex < m_intNmDimensions)
        {
            Individual currentIndividual = m_heuristicProblem.
                getReproduction().DoReproduction();

            currentIndividual.Evaluate(m_heuristicProblem);
            String strIndividualDescr =
                ToStringIndividual(currentIndividual);
            if (!validationDictionary.containsKey(strIndividualDescr) &&
                !m_solutionValidator.containsKey(strIndividualDescr) &&
                m_heuristicProblem.Constraints().CheckConstraints(currentIndividual))
            {
                LoadVertexItem(vertexArray, intIndex, currentIndividual);
                validationDictionary.put(strIndividualDescr, new Object());
                intIndex++;
            }
        }
    }

    private int GetLowerIndividual(ANmVertex[] vertexArray)
    {
    	try
    	{
	        //
	        // Get lower bound individual.
	        // Ensure that the lower bound has not been included in a previous 
	        // call to the solver
	        //
	        int intIndex = 0;
	        for (int i = 0; i < m_heuristicProblem.getPopulation().LargePopulationSize(); i++)
	        {
	            Individual currentIndividual =
	                m_heuristicProblem.
	                    getPopulation().GetIndividualFromLargePopulation(
	                        null,
	                        i);
	
	            if (currentIndividual != null)
	            {
	                String strIndividualDescr = ToStringIndividual(currentIndividual);
	                synchronized (m_lockObjectAddValidation)
	                {
	                    if (!m_solutionValidator.containsKey(strIndividualDescr) &&
	                        m_heuristicProblem.Constraints().CheckConstraints(currentIndividual))
	                    {
	                        m_solutionValidator.put(strIndividualDescr, new Object());
	                        LoadVertexItem(vertexArray, intIndex, currentIndividual);
	                        intIndex++;
	                        break;
	                    }
	                }
	            }
	        }
	        return intIndex;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return 0;
    }

    /// <summary>
    ///   Add a new vertex to vertex arr
    /// </summary>
    /// <param name = "vertexArray"></param>
    /// <param name = "intIndex"></param>
    /// <param name = "currentIndividual"></param>
    protected void LoadVertexItem(
        ANmVertex[] vertexArray,
        int intIndex,
        Individual currentIndividual)
    {
        vertexArray[intIndex] =
            CreateNmVertex(
                m_heuristicProblem.VariableCount(),
                currentIndividual);
        vertexArray[intIndex].Value = -currentIndividual.getFitness();
    }

    public abstract String ToStringIndividual(Individual individual);
    protected abstract double[] GetChromosomeCopy(Individual individual);

    public abstract ANmVertex CreateNmVertex(
        int intDimensions,
        Individual individual);

    public void Dispose()
    {
        if (m_solutionValidator != null)
        {
            m_solutionValidator.clear();
        }
        m_solutionValidator = null;
        m_heuristicProblem = null;
        m_lockObjectAddValidation = null;
    }
}
