package Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.Precision;

import Armadillo.Analytics.Base.MathConstants;
import Armadillo.Analytics.Optimisation.Base.ChromosomeFactory;
import Armadillo.Analytics.Optimisation.Base.EvaluationStateType;
import Armadillo.Analytics.Optimisation.Base.OptimisationConstants;
import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.AGpNode;
import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.AGpVariable;
import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.GpIndividualHelper;
import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.GpOperatorsContainer;
import Armadillo.Analytics.Optimisation.Base.Delegates.IndividualReadyDelegate;
import Armadillo.Analytics.Optimisation.Base.Problem.EnumHeuristicProblem;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.Environment;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.PrintToScreen;
import Armadillo.Core.Text.StringHelper;

/// <summary>
///   Individual Class:
///   An individual is a posible solution considered by 
///   an evolutionary solver. Each individual hold a chromosome 
///   which indicates the distributions (ELTs or CPLTs) included 
///   in the portfolio.
/// 
///   Individuals can be evaluated and compared against each other. 
///   The comparison is based on the "fitness" value (m_dblFitness).
///   If an individual is out of bounds then it can be "repaired". 
///   The repair method uses old individuals previously clustered in 
///   order to speed up computation.
/// 
///   Individuals can be improved. The improvement is done by a 
///   local search operator.
/// </summary>
public class Individual
{
    private List<IndividualReadyDelegate> m_individualReadyDelegates = new ArrayList<IndividualReadyDelegate>();
    private double m_dblFitness;
    private boolean m_dblIsEvaluated;
    private int m_intIndividualId;
    private String m_strProblemName;
    private List<Individual> m_individualList;
    private double[] m_fitnessArr;
    private boolean m_blnIsReadOnly;
    private String m_strDescr;
    private Object m_descrLock = new Object();
    private int m_intHashCode;
    private boolean[] m_blnChromosomeArr;
    private double[] m_dblChromosomeArr;
    private int[] m_intChromosomeArr;
    private GpOperatorsContainer m_gpOperatorsContainer;
    private int m_intGpTreeDepth;
    private int m_intGpTreeSize;
    private AGpNode m_gpTreeRoot;

    public Individual()
    {
    }

    public Individual(
            int[] intChromosomeArray,
            HeuristicProblem heuristicProblem)
    {
    	this(null,
            intChromosomeArray,
            null,
            0,
            heuristicProblem);
    }

    public Individual(
        int[] intChromosomeArray,
        double dblFitness,
        HeuristicProblem heuristicProblem)
            
    {
    	this(null,
                intChromosomeArray,
                null,
                dblFitness,
                heuristicProblem);
        setIsEvaluated(true);
    }
            
    public Individual(
            double[] dblChromosomeArray,
            HeuristicProblem heuristicProblem) 
    {
    	this(dblChromosomeArray,
                null,
                null,
                0,
                heuristicProblem);
    }

    public Individual(
        double[] dblChromosomeArray,
        double dblFitness,
        HeuristicProblem heuristicProblem)
    {
    	this(dblChromosomeArray,
                null,
                null,
                dblFitness,
                heuristicProblem);
         setIsEvaluated(true);
    }
        
    public Individual(
        double[] dblChromosomeArray,
        int[] intChromosomeArray,
        boolean[] blnChromosomeArray,
        double dblFitness,
        HeuristicProblem heuristicProblem)
    {
        setProblemName(heuristicProblem.getProblemName());
        setIsEvaluated(false);
        m_dblChromosomeArr = dblChromosomeArray;
        m_intChromosomeArr = intChromosomeArray;
        setBlnChromosomeArr(blnChromosomeArray);
        m_dblFitness = dblFitness;
        //
        // avoid numerical rounding errors
        //
        ValidateChromosomeRoundDbl();
        InitializeFitnessArr(heuristicProblem);
    }

    /// <summary>
    ///   Create a random solution
    /// </summary>
    public Individual(
        HeuristicProblem heuristicProblem)
    {
        setProblemName(heuristicProblem.getProblemName());
        if (heuristicProblem.getGpOperatorsContainer() != null)
        {
            m_gpOperatorsContainer = heuristicProblem.getGpOperatorsContainer();
        }

        InitializeFitnessArr(heuristicProblem);

        if (m_gpOperatorsContainer != null)
        {
            CreateRandomTree(heuristicProblem);
        }

        double[][] dblChromosomeArr = new double[1][];
        int[][] intChromosomeArr = new int[1][];
        boolean[][] blnChromosomeArr = new boolean[1][];

        ChromosomeFactory.BuildRandomChromosome(
            dblChromosomeArr,
            intChromosomeArr,
            blnChromosomeArr,
            heuristicProblem,
            HeuristicProblem.CreateRandomGenerator());

        m_dblChromosomeArr = dblChromosomeArr[0];
        m_intChromosomeArr = intChromosomeArr[0];
        setBlnChromosomeArr(blnChromosomeArr[0]);
    }

    public Individual(
            boolean[] blnChromosomeArray,
            HeuristicProblem heuristicProblem)
                
    {
    	this(null,
                null,
                blnChromosomeArray,
                0,
                heuristicProblem);
    }

        public Individual(
        		boolean[] blnChromosomeArray,
            double dblFitness,
            HeuristicProblem heuristicProblem)
                
        {
        	this(null,
                    null,
                    blnChromosomeArray,
                    dblFitness,
                    heuristicProblem);
            setIsEvaluated(true);
        }    
    
        public Individual(
                GpOperatorsContainer gpOperatorsContainer,
                HeuristicProblem heuristicProblem)
            {
        		this(
                    null,
                    null,
                    null,
                    0,
                    heuristicProblem);
                    m_gpOperatorsContainer = gpOperatorsContainer;
                CreateRandomTree(heuristicProblem);
            }


            public Individual(
                HeuristicProblem heuristicProblem,
                GpOperatorsContainer gpOperatorsContainer,
                AGpNode root)
            {
            	this(
                        null,
                        null,
                        null,
                        0,
                        heuristicProblem);
                m_gpTreeRoot = root;
                m_gpOperatorsContainer = gpOperatorsContainer;
                m_intGpTreeSize = GpIndividualHelper.CountNodes(root);
            }

            
            public int Depth()
            {
                return getGpTreeDepth();
                
            }
            
            public void setDepth(int value)
            {
            	setGpTreeDepth(value);
            }

            public AGpNode Root()
            {
                return m_gpTreeRoot;
            }
            
            public void setRoot(AGpNode value)
            {
                m_gpTreeRoot = value;
            }

            public int Size()
            {
                return m_intGpTreeSize;
            }
            
            public void setSize(int value)
            {
                m_intGpTreeSize = value;
            }
            
            private void CreateRandomTree(
                HeuristicProblem heuristicProblem)
            {
                RngWrapper rng =
                    HeuristicProblem.CreateRandomGenerator();

                m_gpTreeRoot = m_gpOperatorsContainer.
                    GpOperatorNodeFactory.BuildOperator(
                        null,
                        m_gpOperatorsContainer.MaxTreeDepth,
                        getGpTreeDepth(),
                        rng);

                m_intGpTreeSize = GpIndividualHelper.CountNodes(m_gpTreeRoot);
            }

            public Individual CloneIndividualTree(
                Individual newIndividual,
                HeuristicProblem heuristicProblem)
            {
                newIndividual.m_gpTreeRoot = m_gpTreeRoot.Clone(
                    null,
                    heuristicProblem);
                newIndividual.setGpTreeDepth(m_intGpTreeDepth);
                newIndividual.m_intGpTreeSize = m_intGpTreeSize;
                newIndividual.m_gpOperatorsContainer =
                    m_gpOperatorsContainer;
                return newIndividual;
            }

            public double EvaluateTree(AGpVariable gpVariable)
            {
                // evaluate a gp operator node
                return (Double)m_gpTreeRoot.Compute(gpVariable);
            }

            public void ToStringTree(StringBuilder sb)
            {
                if (m_gpTreeRoot != null)
                {
                    m_gpTreeRoot.ToStringB(sb);
                }
                //return "";
            }

            public boolean ContainsChromosomeTree()
            {
                return m_gpTreeRoot != null;
            }
            
        public int[] GetChromosomeCopyInt()
        {
            ValidateChromosomeInt();
            return (int[]) m_intChromosomeArr.clone();
        }

        private void ValidateChromosomeInt()
        {
        	try
        	{
	            if (m_intChromosomeArr == null)
	            {
	                throw new HCException("Error. Null chromosome.");
	            }
        	}
        	catch(Exception ex)
        	{
        		Logger.log(ex);
        	}
        }

        public int GetChromosomeValueInt(int intIndex)
        {
            try
            {
                ValidateChromosomeInt();
                return m_intChromosomeArr[intIndex];
            }
            catch (HCException e)
            {
                //Debugger.Break();
                throw e;
            }
        }

        public void SetChromosomeValueInt(
            int intIndex,
            int intValue)
        {
            ValidateChromosomeInt();

            ValidateReadOnly();
            m_intChromosomeArr[intIndex] = intValue;
            setIsEvaluated(false);
            m_dblFitness = 0;
        }

        public void AddChromosomeValueInt(
            int intIndex,
            int intValue,
            HeuristicProblem heuristicProblem)
        {
            ValidateChromosomeInt();
            ValidateReadOnly();

            if (intValue == 0)
            {
                return;
            }

            int intValueToAdd =
                intValue +
                GetChromosomeValueInt(intIndex);
            //
            // check that value is in the specified range
            //
            if (intValueToAdd > heuristicProblem.getVariableRangesIntegerProbl()[intIndex] ||
                intValueToAdd < 0)
            {
                //Debugger.Break();
                throw new HCException("Error. Value not valid: " + intValueToAdd);
            }

            SetChromosomeValueInt(
                intIndex,
                intValueToAdd);
        }

        public void RemoveChromosomeValueInt(
            int intIndex,
            int intValue,
            HeuristicProblem heuristicProblem)
        {
            ValidateChromosomeInt();

            ValidateReadOnly();

            if (intValue == 0)
            {
                return;
            }

            AddChromosomeValueInt(
                intIndex,
                -intValue,
                heuristicProblem);
        }

        public String ToStringInt()
        {
        	StringBuilder stb = new StringBuilder();
            if (m_intChromosomeArr != null &&
                m_intChromosomeArr.length > 0)
            {
                stb.append("Chromosome integer = ");
                stb.append(m_intChromosomeArr[0]);
                for (int i = 1; i < m_intChromosomeArr.length; i++)
                {
                    stb.append(", " + m_intChromosomeArr[i]);
                }
            }

            if (m_individualList != null)
            {
                for (Individual individual : m_individualList)
                {
                    stb.append("_inner_" + individual.ToStringInt());
                }
            }

            return stb.toString();
        }

        /// <summary>
        ///   Check is the individual contains the requested chromosome
        /// </summary>
        /// <returns></returns>
        public boolean ContainsChromosomeInt()
        {
            return m_intChromosomeArr != null;
        }

        
        public double[] GetChromosomeCopyDbl()
        {
            ValidateChromosomeDbl();
            return (double[]) m_dblChromosomeArr.clone();
        }

        public double GetChromosomeValueDbl(int intIndex)
        {
            try
            {
                ValidateChromosomeDbl();
                return m_dblChromosomeArr[intIndex];
            }
            catch (HCException e)
            {
                //Debugger.Break();
                throw e;
            }
        }

        private void ValidateChromosomeDbl()
        {
            if (m_dblChromosomeArr == null)
            {
                //Debugger.Break();
                throw new HCException("Error. Null chromosome.");
            }
        }

        public void SetChromosomeValueDbl(int intIndex, double dblValue)
        {
            if (dblValue > 1.0 || dblValue < 0.0)
            {
                throw new HCException("Chromosome value = " + dblValue);
            }

            dblValue = Precision.round(dblValue,
                                  MathConstants.ROUND_DECIMALS);

            ValidateChromosomeDbl();
            ValidateReadOnly();
            m_dblChromosomeArr[intIndex] = dblValue;
            setIsEvaluated(false);
            m_dblFitness = 0;
        }

        public void RemoveChromosomeValueDbl(
            int intIndex,
            double dblValue)
        {
            ValidateChromosomeDbl();
            ValidateReadOnly();

            if (dblValue < MathConstants.ROUND_ERROR)
            {
                return;
            }

            AddChromosomeValueDbl(
                intIndex,
                -dblValue);
        }

        public void AddChromosomeValueDbl(
            int intIndex,
            double dblValue)
        {
            ValidateChromosomeDbl();
            ValidateReadOnly();

            double dblNewChromosomeValue =
                dblValue +
                GetChromosomeValueDbl(intIndex);

            if (dblNewChromosomeValue > 1.0 ||
                dblNewChromosomeValue < 0.0)
            {
                //Debugger.Break();
                throw new HCException("Error. Value not valid: " + dblNewChromosomeValue);
            }

            SetChromosomeValueDbl(
                intIndex,
                dblNewChromosomeValue);
        }

        protected void ValidateChromosomeRoundDbl()
        {
            //
            // avoid numerical rounding errors
            //
            if (m_dblChromosomeArr == null)
            {
                return;
            }

            for (int i = 0; i < m_dblChromosomeArr.length; i++)
            {
                m_dblChromosomeArr[i] = Precision.round(m_dblChromosomeArr[i],
                                                 MathConstants.ROUND_DECIMALS);
            }
        }

        public String ToStringDbl()
        {
        	StringBuilder stb = new StringBuilder();
            if (m_dblChromosomeArr != null &&
                m_dblChromosomeArr.length > 0)
            {
                stb.append("Chromosome continuous = ");
                stb.append(Precision.round(m_dblChromosomeArr[0], 4));
                for (int i = 1; i < m_dblChromosomeArr.length; i++)
                {
                    stb.append(", " + Precision.round(m_dblChromosomeArr[i], 4));
                }
            }

            if (m_individualList != null)
            {
                for (Individual individual : m_individualList)
                {
                    stb.append("_inner_" + individual.ToStringDbl());
                }
            }

            return stb.toString();
        }

        /// <summary>
        ///   Check is the individual contains the requested chromosome
        /// </summary>
        /// <returns></returns>
        public boolean ContainsChromosomeDbl()
        {
            return m_dblChromosomeArr != null;
        }
        
        public boolean[] GetChromosomeCopyBln()
        {
            if (getBlnChromosomeArr() == null)
            {
                return null;
            }

            return (boolean[]) getBlnChromosomeArr().clone();
        }

        public boolean GetChromosomeValueBln(int intIndex)
        {
            return getBlnChromosomeArr()[intIndex];
        }

        public void SetChromosomeValueBln(
            int intIndex,
            boolean blnValue)
        {
            ValidateReadOnly();

            if (getBlnChromosomeArr()[intIndex] == blnValue)
            {
                throw new HCException("Error. Chromosome value already set.");
            }
            getBlnChromosomeArr()[intIndex] = blnValue;
            setIsEvaluated(false);
            m_dblFitness = 0;
        }

        private String ToStringBln()
        {
        	StringBuilder stb = new StringBuilder();
            if (getBlnChromosomeArr() != null &&
                getBlnChromosomeArr().length > 0)
            {
                stb.append(getBlnChromosomeArr()[0] ? "1" : "0");
                for (int i = 1; i < getBlnChromosomeArr().length; i++)
                {
                    stb.append(", " + (getBlnChromosomeArr()[i] ? "1" : "0"));
                }
            }

            if (m_individualList != null)
            {
                for (Individual individual : m_individualList)
                {
                    stb.append("_inner_" + individual.ToStringBln());
                }
            }

            return stb.toString();
        }

        public boolean ContainsChromosomeBln()
        {
            return getBlnChromosomeArr() != null;
        }
        
//    @Override
//    public int compareTo(Individual o)
//    {
//    	try
//    	{
//	        int intCompareToValue =
//	            CompareToStd(o);
//	
//	        if (intCompareToValue == 0)
//	        {
//	            intCompareToValue =
//	                CompareToTree(o);
//	        }
//	
//	        return intCompareToValue;
//    	}
//    	catch(Exception ex)
//    	{
//    		Logger.log(ex);
//    	}
//    	return 0;
//    }

    public Individual GetIndividual(String strProblemName)
    {
        if(StringHelper.IsNullOrEmpty(strProblemName))
        {
            throw new HCException("Null problem name");
        }

        for (Individual individual : m_individualList) 
        {
        	if(individual.getProblemName().equals(strProblemName))
        	{
        		return individual;
        	}
		}
        return null;
    }

    private void InitializeFitnessArr(
        HeuristicProblem heuristicProblem)
    {
        if (heuristicProblem != null &&
            heuristicProblem.ObjectiveCount() > 1)
        {
            m_fitnessArr = new double[
                heuristicProblem.ObjectiveCount()];
        }
    }

    public double GetFitnesValue(
        int intIndex)
    {
        return m_fitnessArr[intIndex];
    }

    public double[] GetFitnessArrCopy()
    {
        return (double[]) m_fitnessArr.clone();
    }

    public double Evaluate(
        HeuristicProblem heuristicProblem)
    {
        return Evaluate(
            heuristicProblem.isDoLocalSearch(),
            heuristicProblem.DoRepairSolution(),
            heuristicProblem.isDoClusterSolution(),
            heuristicProblem);
    }


    public double Evaluate(boolean blnDoLocalSearch,
                           boolean blnRepairSolution,
                           boolean blnClusterSolution,
                           HeuristicProblem heuristicProblem)
    {
        try
        {
            ValidateEvaluateIndividual();
            //
            // create a pointer to this instance
            //
            Individual localIndividual = this;

            boolean blnSucessRepair = EvaluateOperators(
                blnDoLocalSearch,
                blnRepairSolution,
                localIndividual,
                heuristicProblem);

            EvaluateFitness(
                localIndividual,
                blnSucessRepair,
                heuristicProblem);


            AckEvaluate(
                blnClusterSolution, 
                heuristicProblem, 
                localIndividual, 
                blnSucessRepair);
        }
        catch (HCException e2)
        {
            //Logger.GetLogger().Write(e2);
            //Debugger.Break();
            InvokeIndividualReadyEventHandler(EvaluationStateType.FAILURE_EVALUATION);
            PrintToScreen.WriteLine(e2.toString());
        }
        return m_dblFitness;
    }

    public void AckEvaluate(
        HeuristicProblem heuristicProblem)
    {
        AckEvaluate(true, heuristicProblem, this, true);
    }

    private void AckEvaluate(
        boolean blnClusterSolution, 
        HeuristicProblem heuristicProblem, 
        Individual localIndividual,
        boolean blnSucessRepair)
    {

        ClusterIndividual(
            blnClusterSolution,
            localIndividual,
            heuristicProblem);

        ValidateEvaluate(localIndividual);

        //
        // invoke individual evaluate event
        //
        InvokeIndividualReadyEventHandler(EvaluationStateType.SUCCESS_EVALUATION);
    }

    private void ValidateEvaluate(Individual localIndividual)
    {
        //
        // validate operators
        //
        if (localIndividual.m_dblFitness != m_dblFitness &&
            !(Double.isNaN(localIndividual.m_dblFitness) &&
              Double.isNaN(m_dblFitness)))
        {
            throw new HCException("Fitness not equal.");
        }
    }

    private void ClusterIndividual(
        boolean blnClusterSolution,
        Individual localIndividual,
        HeuristicProblem heuristicProblem)
    {
        if (blnClusterSolution)
        {
            heuristicProblem.getPopulation().AddIndividualToPopulation(localIndividual);

            if (heuristicProblem.getReproduction() != null)
            {
                heuristicProblem.getReproduction().ClusterInstance(localIndividual);
            }
        }
    }

    private void EvaluateFitness(
        Individual localIndividual,
        boolean blnSucessRepair,
        HeuristicProblem heuristicProblem)
    {
        //
        // evaluate individual
        //
        m_dblFitness =
            heuristicProblem.getObjectiveFunction().Evaluate(localIndividual);

        if (m_individualList != null &&
            m_individualList.size() > 0)
        {
            for (Individual individual : m_individualList)
            {
                individual.m_dblFitness =
                    m_dblFitness;
            }
        }

        if (!blnSucessRepair)
        {
            PenaliseRepair();
        }
        // set the individual as evaluated
        setIsEvaluated(true);
    }

    private boolean EvaluateOperators(
        boolean blnDoLocalSearch,
        boolean blnRepairSolution,
        Individual localIndividual,
        HeuristicProblem heuristicProblem)
    {
        //
        // repair solution
        //
        boolean blnSucessRepair = true;
        if (blnRepairSolution &&
            heuristicProblem.getRepairIndividual() != null)
        {
            if (heuristicProblem.getRepairProb() > 0 &&
                heuristicProblem.getRepairProb() > HeuristicProblem.CreateRandomGenerator().nextDouble() &&
                !heuristicProblem.Constraints().CheckConstraints(localIndividual))
            {
                blnSucessRepair = false;
            }
            else
            {
                blnSucessRepair = heuristicProblem.
                    getRepairIndividual().
                    DoRepair(localIndividual);

                if (blnSucessRepair &&
                    heuristicProblem.DoCheckConstraints() &&
                    !heuristicProblem.Constraints().CheckConstraints(localIndividual))
                {
                    throw new HCException("Repair individual failed.");
                }
            }
        }

        //
        // Do local search
        //
        if (blnSucessRepair &&
            blnDoLocalSearch &&
            heuristicProblem.getLocalSearch() != null)
        {
            int intMaxLocalSearchInstance = (int) (heuristicProblem.PopulationSize()*
                                                   OptimisationConstants.LOCAL_SEARCH_POPULATON_FACTOR);

            if (heuristicProblem.getLocalSearchProb() > 0 &&
                heuristicProblem.getLocalSearchProb() <
                HeuristicProblem.CreateRandomGenerator().nextDouble() &&
                heuristicProblem.getCurrentLocalSearchInstances() < intMaxLocalSearchInstance)
            {
                //
                // take control of the number of local search iterations in progress
                //
                int intCurrentLocalSearchInstances = heuristicProblem.getCurrentLocalSearchInstances();
                intCurrentLocalSearchInstances++;
                heuristicProblem.setCurrentLocalSearchInstances(intCurrentLocalSearchInstances);

                //
                // update grid
                //
                heuristicProblem.getProblemStats().SetIntValue(
                    EnumHeuristicProblem.CurrentLocalSearchInstances,
                    intCurrentLocalSearchInstances);
                heuristicProblem.PublishGridStats();

                heuristicProblem.getLocalSearch().DoLocalSearch(localIndividual);

                //
                // take control of the number of local search iterations in progress
                //
                intCurrentLocalSearchInstances = heuristicProblem.getCurrentLocalSearchInstances();
                intCurrentLocalSearchInstances--;
                heuristicProblem.setCurrentLocalSearchInstances(intCurrentLocalSearchInstances);

                //
                // update grid
                //
                heuristicProblem.getProblemStats().SetIntValue(
                    EnumHeuristicProblem.CurrentLocalSearchInstances,
                    intCurrentLocalSearchInstances);
                heuristicProblem.PublishGridStats();
            }
        }
        return blnSucessRepair;
    }

    private void ValidateEvaluateIndividual()
    {
        ValidateReadOnly();
        //
        // check if the solution is evaluated
        //
        if (isIsEvaluated())
        {
            //Debugger.Break();
            throw new HCException("IIndividual already evaluated");
        }
    }

    private void PenaliseRepair()
    {
        //
        // penalise individual since it did not satisfy the contrains
        //
        double dblLargeNegativeValue = -Double.MAX_VALUE/2.0;

        if (dblLargeNegativeValue > m_dblFitness ||
            Math.abs(dblLargeNegativeValue) < m_dblFitness)
        {
            //Debugger.Break();
            throw new HCException("Error. Infinite fitness value.");
        }

        double dblTmpFitness = m_dblFitness - dblLargeNegativeValue;
        m_dblFitness = dblTmpFitness - Double.MAX_VALUE;
        ////Debugger.Break();
    }

    /// <summary>
    ///   Call this method once the individual is finish with its evaluation
    /// </summary>
    /// <param name = "state">
    ///   Evaluation state (Success or Failure)
    /// </param>
    public void InvokeIndividualReadyEventHandler(EvaluationStateType state)
    {
    	OnIndividualReady(state);
    }

    private void OnIndividualReady(EvaluationStateType state) 
    {
	}

	public void SetFitnessValue(
        double dblFitness,
        int intIndex)
    {
        ValidateReadOnly();
        setIsEvaluated(true);
        m_fitnessArr[intIndex] = dblFitness;
    }

    public void SetFitnessValue(double dblFitness)
    {
        ValidateReadOnly();
        setIsEvaluated(true);
        m_dblFitness = dblFitness;
    }

    public boolean IsReadOnly()
    {
        return m_blnIsReadOnly;
    }

    public void SetReadOnly()
    {
        m_blnIsReadOnly = true;
    }

    protected void ValidateReadOnly()
    {
        if (m_blnIsReadOnly)
        {
            //Debugger.Break();
            throw new HCException("Individual is read-only");
        }
    }

    /// <summary>
    ///   Override Object.Equals.
    /// </summary>
    /// <param name = "obj">Object to compare. 
    ///   Compare only the vectors and not the weights</param>
    /// <returns>True if equal, false otherwise.</returns>
    public boolean Equals(Individual obj)
    {
        if(hashCode() != obj.hashCode())
        {
            return false;
        }
        //
        // compare integer and continous chromosomes
        //
        if (m_dblChromosomeArr != null)
        {
            for (int i = 0; i < m_dblChromosomeArr.length; i++)
            {
                if (Math.abs(GetChromosomeValueDbl(i) -
                             obj.GetChromosomeValueDbl(i)) > 1E-6)
                {
                    return false;
                }
            }
        }
        if (m_intChromosomeArr != null)
        {
            for (int i = 0; i < m_intChromosomeArr.length; i++)
            {
                if (GetChromosomeValueInt(i) !=
                    obj.GetChromosomeValueInt(i))
                {
                    return false;
                }
            }
        }
        if (m_gpTreeRoot != null &&
            !m_gpTreeRoot.toString().equals(obj.m_gpTreeRoot.toString()))
        {
            return false;
        }
        return true;
    }

    private void CheckIndDescr()
    {
        if (StringHelper.IsNullOrEmpty(m_strDescr))
        {
            synchronized (m_descrLock)
            {
                if (StringHelper.IsNullOrEmpty(m_strDescr))
                {
                    m_strDescr = GetStrDescr();
                    m_intHashCode = m_strDescr.hashCode();
                }
            }
        }
    }

    
    @Override
    public int hashCode()
    {
        CheckIndDescr();
        return m_intHashCode;
    }

    public void Dispose()
    {
        if(m_individualList != null)
        {
            for (int i = 0; i < m_individualList.size(); i++)
            {
                m_individualList.get(i).Dispose();
            }
            m_individualList.clear();
            m_individualList = null;
        }
        m_fitnessArr = null;
        m_descrLock = null;
        setBlnChromosomeArr(null);
        m_intChromosomeArr = null;
        m_dblChromosomeArr = null;
        if (m_gpTreeRoot != null)
        {
            m_gpTreeRoot.Dispose();
        }
        m_gpTreeRoot = null;
    }

    /// <summary>
    ///   String description of current individual
    /// </summary>
    /// <returns>
    ///   String description
    /// </returns>
    public String PrintSolution()
    {
    	StringBuilder sb = new StringBuilder();
        sb.append("Objective function: " + m_dblFitness + Environment.NewLine);
        if (m_dblChromosomeArr != null)
        {
            for (int i = 0; i < m_dblChromosomeArr.length; i++)
            {
                sb.append("ELT " + (i + 1) + "=" + m_dblChromosomeArr[i] + Environment.NewLine);
            }
        }
        return sb.toString();
    }

    /// <summary>
    ///   String representation of current Indivudal
    /// </summary>
    /// <returns>
    ///   String representation of current Indivudal
    /// </returns>
    @Override
    public String toString()
    {
        CheckIndDescr();
        return m_strDescr;
    }

    private String GetStrDescr()
    {
    	StringBuilder sb = new StringBuilder();
        boolean blnAddSep = false;

        if (ContainsChromosomeDbl())
        {
            sb.append(ToStringDbl());
            blnAddSep = true;
        }

        if (ContainsChromosomeInt())
        {
            if (blnAddSep)
            {
                sb.append(" || ");
            }
            sb.append(ToStringInt());
            blnAddSep = true;
        }

        if (ContainsChromosomeBln())
        {
            if (blnAddSep)
            {
                sb.append(" || ");
            }
            sb.append(ToStringBln());
            blnAddSep = true;
        }

        if (ContainsChromosomeTree())
        {
            if (blnAddSep)
            {
                sb.append(" || ");
            }
            ToStringTree(sb);
            //sb.append();
        }

        if (m_individualList != null)
        {
            for (Individual individual : m_individualList)
            {
                sb.append("_innr_" + individual.getProblemName() + "_" + individual);
            }
        }

        sb.append("\nIndividual Fitness = " + m_dblFitness);
        return sb.toString();
    }

    public Individual Clone(
        HeuristicProblem heuristicProblem)
    {
    	try
    	{
	        //
	        // get chromosomes
	        //
	        double[] dblChromosomeArr =
	            m_dblChromosomeArr == null
	                ? null
	                : GetChromosomeCopyDbl();
	
	        int[] intChromosomeArr =
	            m_intChromosomeArr == null
	                ? null
	                : GetChromosomeCopyInt();
	
	        boolean[] blnChromosomeArr =
	            getBlnChromosomeArr() == null
	                ? null
	                : GetChromosomeCopyBln();
	
	        Individual newIndividual =
	            new Individual(
	                dblChromosomeArr,
	                intChromosomeArr,
	                blnChromosomeArr,
	                m_dblFitness,
	                heuristicProblem);
	
	        // multi-objective 
	        newIndividual.m_fitnessArr =
	            m_fitnessArr == null ? null : (double[]) m_fitnessArr.clone();
	
	        if (m_gpTreeRoot != null)
	        {
	            CloneIndividualTree(
	                newIndividual,
	                heuristicProblem);
	        }
	        newIndividual.setProblemName(m_strProblemName);
	
	        if (m_individualList != null &&
	            m_individualList.size() > 0)
	        {
	            newIndividual.m_individualList =
	                new ArrayList<Individual>();
	
	            for (Individual curruentIndividual : m_individualList)
	            {
	                Individual newInnerIndividual =
	                    curruentIndividual.Clone(heuristicProblem);
	                newIndividual.m_individualList.add(
	                    newInnerIndividual);
	            }
	        }
	
	        return newIndividual;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }

    public double[] GetChromosomeCopy()
    {
        if (m_dblChromosomeArr != null)
        {
            return GetChromosomeCopyDbl();
        }
        if (m_intChromosomeArr != null)
        {
            int[] intChromosomeArr = GetChromosomeCopyInt();
            double[] dblChromosomeArr = new double[intChromosomeArr.length];

            for (int i = 0; i < intChromosomeArr.length; i++)
            {
                dblChromosomeArr[i] = intChromosomeArr[i];
            }
            return dblChromosomeArr;
        }
        throw new HCException("Error. Chromosome not found.");
    }

	public void addIndividualReadyDelegate(
			IndividualReadyDelegate individualReadyEventHandler)
	{
		m_individualReadyDelegates.add(individualReadyEventHandler);
	}

	public double getFitness() {
		return m_dblFitness;
	}

	public void setFitness(double fitness) {
		m_dblFitness = fitness;
	}

	public boolean isIsEvaluated() {
		return m_dblIsEvaluated;
	}

	public void setIsEvaluated(boolean isEvaluated) {
		m_dblIsEvaluated = isEvaluated;
	}

	public int getIndividualId() {
		return m_intIndividualId;
	}

	public void setIndividualId(int individualId) {
		m_intIndividualId = individualId;
	}

	public String getProblemName() {
		return m_strProblemName;
	}

	public void setProblemName(String problemName) {
		m_strProblemName = problemName;
	}

	public List<Individual> getIndividualList() {
		return m_individualList;
	}

	public void setIndividualList(List<Individual> individualList) {
		m_individualList = individualList;
	}

//	public double[] getFitnessArr() {
//		return m_fitnessArr;
//	}

	public void setFitnessArr(double[] fitnessArr) {
		m_fitnessArr = fitnessArr;
	}

	public boolean[] getBlnChromosomeArr() {
		return m_blnChromosomeArr;
	}

	public void setBlnChromosomeArr(boolean[] blnChromosomeArr) {
		m_blnChromosomeArr = blnChromosomeArr;
	}

	public double[] getDblChromosomeArr() {
		return m_dblChromosomeArr;
	}

	public void setDblChromosomeArr(double[] dblChromosomeArr) {
		m_dblChromosomeArr = dblChromosomeArr;
	}

	protected int[] getIntChromosomeArr() {
		return m_intChromosomeArr;
	}

	protected void setIntChromosomeArr(int[] intChromosomeArr) {
		m_intChromosomeArr = intChromosomeArr;
	}

	public int getGpTreeDepth() {
		return m_intGpTreeDepth;
	}

	public void setGpTreeDepth(int gpTreeDepth) {
		m_intGpTreeDepth = gpTreeDepth;
	}

	public int getGpTreeSize() {
		return m_intGpTreeSize;
	}

	public void setGpTreeSize(int gpTreeSize) {
		m_intGpTreeSize = gpTreeSize;
	}

	public AGpNode getGpTreeRoot() {
		return m_gpTreeRoot;
	}

	public void setGpTreeRoot(AGpNode gpTreeRoot) {
		m_gpTreeRoot = gpTreeRoot;
	}

}
