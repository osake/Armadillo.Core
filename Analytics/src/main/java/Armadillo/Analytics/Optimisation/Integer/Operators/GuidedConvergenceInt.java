package Armadillo.Analytics.Optimisation.Integer.Operators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.util.Precision;

import Armadillo.Analytics.Base.SearchUtilsClass;
import Armadillo.Analytics.Optimisation.Base.Operators.AGuidedConvergence;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;

public class GuidedConvergenceInt extends AGuidedConvergence
{
    private double[][][] m_dblProbArr;

    /// <summary>
    ///   Constructor
    /// </summary>
    public GuidedConvergenceInt(HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
        m_dblProbArr = new double[m_intVariableCount][][];
    }

    @Override
    public void UpdateGcProbabilities(
        HeuristicProblem heuristicProblem)
    {
        try
        {
            // dictionary will count the number of integer values per variable
        	List<Map<Integer, Integer>> m_variableCounterArr = new ArrayList<Map<Integer, Integer>>();
        	
            //    new HashMap<Integer, Integer>[m_intVariableCount];

            int intPopulationSize = GetPopulationSize();

            for (int j = 0; j < m_intVariableCount; j++)
            {
                m_variableCounterArr.add(
                    new HashMap<Integer, Integer>(intPopulationSize + 1));
            }


            for (int i = 0; i < intPopulationSize; i++)
            {
                for (int j = 0; j < m_intVariableCount; j++)
                {
                    Individual selectedIndividual = m_heuristicProblem.getPopulation().
                        GetIndividualFromLargePopulation(
                            m_heuristicProblem,
                            i);

                    int intValue = selectedIndividual.GetChromosomeValueInt(j);

                    if (!m_variableCounterArr.get(j).containsKey(intValue))
                    {
                        m_variableCounterArr.get(j).put(intValue, 1);
                    }
                    else
                    {
                        //
                        // increase counter for variable
                        //
                        int intCounter = m_variableCounterArr.get(j).get(intValue) + 1;
                        m_variableCounterArr.get(j).put(intValue, intCounter);
                    }
                }
            }

            synchronized (m_dblProbArr)
            {
                try
                {
                    double[][][] dblTmpProbArr = new double[m_intVariableCount][][];
                    m_gcProbabilityArray = new double[m_intVariableCount];
                    double dblSumKeyValues = 0;
                    for (int j = 0; j < m_intVariableCount; j++)
                    {
                        dblTmpProbArr[j] = new double[2][];
                        dblTmpProbArr[j][0] = new double[m_variableCounterArr.get(j).size()];
                        dblTmpProbArr[j][1] = new double[m_variableCounterArr.get(j).size()];

                        int intI = 0;
                        double dblAccumProb = 0;
                        for (Entry<Integer, Integer> kvp : m_variableCounterArr.get(j).entrySet())
                        {
                            int intKeyValue = kvp.getKey();
                            dblSumKeyValues += intKeyValue;
                            m_gcProbabilityArray[j] += intKeyValue;
                            dblTmpProbArr[j][0][intI] = intKeyValue;
                            dblTmpProbArr[j][1][intI] = dblAccumProb;
                            dblAccumProb += kvp.getValue()/(double) intPopulationSize;
                            intI++;
                        }

                        double dblRoundedAcumProb = Precision.round(dblAccumProb, 0);
                        if (dblRoundedAcumProb != 1.0)
                        {
                            throw new HCException(
                            		"Error. Total weight [" + 
                            				dblRoundedAcumProb + "] is not [1.0]");
                        }
                    }
                    m_dblProbArr = dblTmpProbArr;

                    double dblTotalProb = 0;
                    for (int j = 0; j < m_intVariableCount; j++)
                    {
                        double dblProb = m_gcProbabilityArray[j]/dblSumKeyValues;
                        m_gcProbabilityArray[j] = dblProb;
                        dblTotalProb += dblProb;
                    }

                    if(Double.isNaN(dblTotalProb))
                    {
                        dblTotalProb = 0;
                    }

                    if (Precision.round(dblTotalProb, 0) != 1.0)
                    {
                        //Debugger.Break();
                        throw new HCException("Error. prob[" + dblTotalProb +
                            "] not = 1");
                    }
                }
                catch (Exception e)
                {
                    //Debugger.Break();
                    throw e;
                }
            }
        }
        catch (Exception e2)
        {
        	Logger.log(e2);
        }
    }

    private int GetPopulationSize()
    {
        if (m_heuristicProblem.getPopulation().GetIndividualFromLargePopulation(
            m_heuristicProblem,
            m_heuristicProblem.getPopulation().LargePopulationSize() - 1) != null)
        {
            return m_heuristicProblem.getPopulation().LargePopulationSize();
        }
        for (int i = 0; i < m_heuristicProblem.getPopulation().LargePopulationSize(); i++)
        {
            if (m_heuristicProblem.getPopulation().GetIndividualFromLargePopulation(
                m_heuristicProblem,
                m_heuristicProblem.getPopulation().LargePopulationSize() - 1) == null)
            {
                return i + 1;
            }
        }
        return 0;
    }

    @Override
    public double DrawGuidedConvergenceValue(
        int intIndex,
        RngWrapper rng)
    {
        double dblRandom = rng.nextDouble();

        if (m_dblProbArr[intIndex][1] == null)
        {
            //Debugger.Break();
        }

        int intRandomIndex = SearchUtilsClass.DoBinarySearch(
            m_dblProbArr[intIndex][1],
            dblRandom);
        return m_dblProbArr[intIndex][0][intRandomIndex];
    }

    @Override
    protected void InitializeGcProbabilities()
    {
        if (CheckPopulation())
        {
            UpdateGcProbabilities(m_heuristicProblem);
        }
    }
}
