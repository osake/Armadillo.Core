package Armadillo.Analytics.Optimisation.Binary.Operators;

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

public class GuidedConvergenceBln extends AGuidedConvergence
{
    private double[][][] m_dblProbArr;

    /// <summary>
    ///   Constructor
    /// </summary>
    public GuidedConvergenceBln(HeuristicProblem heuristicProblem)
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
            // dictionary will cound the number of integer values per variable
            List<Map<Integer, Integer>> variableCounterArr = new ArrayList<Map<Integer, Integer>>();

            int intPopulationSize = GetPopulationSize();

            for (int j = 0; j < m_intVariableCount; j++)
            {
                variableCounterArr.add(new HashMap<Integer, Integer>(intPopulationSize + 1));
            }


            for (int i = 0; i < intPopulationSize; i++)
            {
                for (int j = 0; j < m_intVariableCount; j++)
                {
                    Individual currIndividual = m_heuristicProblem.getPopulation().GetIndividualFromLargePopulation(
                        m_heuristicProblem,
                        i);
                    int intValue = currIndividual.GetChromosomeValueBln(j)
                                       ? 1
                                       : 0;
                    if (!variableCounterArr.get(j).containsKey(intValue))
                    {
                        variableCounterArr.get(j).put(intValue, 1);
                    }
                    else
                    {
                        //
                        // increase counter for variable
                        //
                        int intCounter = variableCounterArr.get(j).get(intValue);
                        intCounter++;
                        variableCounterArr.get(j).put(intValue, intCounter);
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
                        dblTmpProbArr[j][0] = new double[variableCounterArr.get(j).size()];
                        dblTmpProbArr[j][1] = new double[variableCounterArr.get(j).size()];

                        int intI = 0;
                        double dblAccumProb = 0;
                        for (Entry<Integer, Integer> kvp : variableCounterArr.get(j).entrySet())
                        {
                            int intKeyValue = kvp.getKey();
                            dblSumKeyValues += intKeyValue;
                            m_gcProbabilityArray[j] += intKeyValue;
                            dblTmpProbArr[j][0][intI] = intKeyValue;
                            dblTmpProbArr[j][1][intI] = dblAccumProb;
                            dblAccumProb += kvp.getValue()/(double) intPopulationSize;
                            intI++;
                        }

                        if (Precision.round(dblAccumProb, 0) != 1.0)
                        {
                            throw new HCException("Error. Total weight is not 1.0");
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

                    if (Precision.round(dblTotalProb, 0) != 1.0)
                    {
                        //Debugger.Break();
                        throw new HCException("Error. prob not = 1");
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
            //Debugger.Break();
            throw e2;
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
