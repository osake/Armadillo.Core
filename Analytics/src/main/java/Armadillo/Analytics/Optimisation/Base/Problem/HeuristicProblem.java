package Armadillo.Analytics.Optimisation.Base.Problem;

import java.util.List;

import org.joda.time.DateTime;

import Armadillo.Analytics.Optimisation.Base.EnumOptimimisationPoblemType;
import Armadillo.Analytics.Optimisation.Base.Constraints.ConstraintClass;
import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.AGpBridge;
import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.GpOperatorsContainer;
import Armadillo.Analytics.Optimisation.Base.ObjectiveFunctions.HeuristicMultiObjectiveFunction;
import Armadillo.Analytics.Optimisation.Base.ObjectiveFunctions.IHeuristicObjectiveFunction;
import Armadillo.Analytics.Optimisation.Base.Operators.AGuidedConvergence;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.IndividualFactories.IIndividualFactory;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ILocalSearch;
import Armadillo.Analytics.Optimisation.Base.Operators.MultiObjective.MultiObjectiveRanking;
import Armadillo.Analytics.Optimisation.Base.Operators.PopulationClasses.IInitialPopulation;
import Armadillo.Analytics.Optimisation.Base.Operators.PopulationClasses.Population;
import Armadillo.Analytics.Optimisation.Base.Operators.Repair.IRepair;
import Armadillo.Analytics.Optimisation.Base.Operators.Reproduction.IReproduction;
import Armadillo.Analytics.Optimisation.Base.Solvers.EvolutionarySolver;
import Armadillo.Analytics.Optimisation.MixedSolvers.DummyObjectiveFunctions.MixedObjectiveFunctionDummy;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.Guid;
import Armadillo.Core.Logger;
import Armadillo.Core.SelfDescribing.SelfDescribingTsEvent;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.UI.PublishUiMessageEvent;

public class HeuristicProblem 
{
    /// <summary>
    ///   Optimisation problem type
    /// </summary>
    private final EnumOptimimisationPoblemType m_enumOptimimisationPoblemType;

    /// <summary>
    ///   Constraints
    /// </summary>
    private ConstraintClass m_constraints;

    /// <summary>
    ///   The size of the population. Very large populations tend to converge slower 
    ///   but it is more likely to find the "global solution" if the number of 
    ///   generations is very large.
    ///   Very small populations converge too quickly to a local solution.
    ///   The size of the population is also dictated by the machine phisical memory
    /// </summary>
    private int m_intPopulationSize;

    private int m_intThreads;

    /// <summary>
    ///   Ranks objective functions for multi-objective problems
    /// </summary>
    private MultiObjectiveRanking m_multiObjectiveRanking;
    private double[] m_variableRangesIntegerProbl;
    private int m_inrCurrentLocalSearchInstances;
    private double m_dblRepairProb;
    private double m_dblLocalSearchProb;
    private boolean m_blnVerbose;
    private IIndividualFactory m_individualFactory;
    private IInitialPopulation m_initialPopulation;
    private EvolutionarySolver m_solver;
    private List<HeuristicProblem> m_innerProblemList;
    private SelfDescribingTsEvent m_problemStats;
    private int m_intIterations;
    private String m_strProblemName;
    private int m_intConvergence;
    private IHeuristicObjectiveFunction m_objectiveFunction;
    private Population m_population;
    private IReproduction m_reproduction;
    private IRepair m_repairIndividual;
    private ILocalSearch m_localSearch;
    private AGuidedConvergence m_guidedConvergence;
    private boolean m_blnDoLocalSearch;
    private boolean m_blnDoClusterSolution;
    private AGpBridge m_gpBridge;
    private GpOperatorsContainer m_gpOperatorsContainer;

    public MultiObjectiveRanking MultiObjectiveRanking()
    {
        if (m_multiObjectiveRanking == null && ObjectiveCount() > 1)
        {
            m_multiObjectiveRanking = new MultiObjectiveRanking(
                this);
        }
        return m_multiObjectiveRanking;
    }
    
    public int VariableCount()
    {
        return m_objectiveFunction == null ? 0 : m_objectiveFunction.VariableCount();
    }

    public int ObjectiveCount()
    {
        if (!IsMultiObjective())
        {
            return 1;
        }
        return ((HeuristicMultiObjectiveFunction) m_objectiveFunction).ObjectiveCount();
    }

    public boolean DoCheckConstraints()
    {
        return m_constraints != null; 
    }

    public void setConstraints(ConstraintClass constraintClass)
    {
    	m_constraints = constraintClass;
    }
    
    public ConstraintClass Constraints()
    {
        if (m_constraints == null)
        {
            //
            // load dummy constraints
            //
            m_constraints = new ConstraintClass();
        }
        return m_constraints;
    }

    public boolean DoRepairSolution()
    {
        return m_repairIndividual != null;
    }

    public int PopulationSize()
    {
        return m_intPopulationSize; 
    }
    
     public void setPopulationSize(int value)
     {
        m_intPopulationSize = value;
        if (m_population != null)
        {
            m_population.SetPopulationSize();
        }
        // set population to nested problems
        SetPopulationToNestedProblems(
            value,
            this);
    }

    public EnumOptimimisationPoblemType EnumOptimimisationPoblemType()
    {
        return m_enumOptimimisationPoblemType;
    }

    public int getThreads()
    {
        return m_intThreads; 
    }
    
    public void setThreads(int value)
    {
        m_intThreads = value;
        if(m_innerProblemList != null)
        {
            for (HeuristicProblem heuristicProblem : m_innerProblemList)
            {
                heuristicProblem.setThreads(value);
            }
        }
    }
    
    public HeuristicProblem(
        EnumOptimimisationPoblemType problemType) 
    {
        this(problemType,
                GetRandomProblemName(problemType));
    }

    private static String GetRandomProblemName(EnumOptimimisationPoblemType problemType)
    {
        return Guid.NewGuid() + "_" +
               problemType;
    }

    public HeuristicProblem(
        EnumOptimimisationPoblemType problemType,
        String strProblemName)
    {
        if (StringHelper.IsNullOrEmpty(strProblemName))
        {
            strProblemName = GetRandomProblemName(problemType);
        }
        m_strProblemName = strProblemName;
        m_enumOptimimisationPoblemType = problemType;
        m_blnVerbose = true;
        //
        // cluster class opeartor: Saves explored solutions.
        // This option is enabled by default
        //
        m_blnDoClusterSolution = true;

        String strClassName =
            "name_" +
            m_strProblemName
                .replace(";", "_")
                .replace(",", "_")
                .replace(".", "_")
                .replace(":", "_")
                .replace("-", "_");
        m_problemStats = new SelfDescribingTsEvent(
            strClassName);

        m_problemStats.SetStrValue(
            EnumHeuristicProblem.ProblemName,
            m_strProblemName);
    }

    @Override
    public String toString()
    {
        return m_strProblemName;
    }

    public void Dispose()
    {
        try
        {
            if (m_population != null)
            {
                m_population.Dispose();
            }
            if (m_solver != null)
            {
                m_solver.Dispose();
            }
            if (m_innerProblemList != null)
            {
                for (int i = 0; i < m_innerProblemList.size(); i++)
                {
                    m_innerProblemList.get(i).Dispose();
                }
                m_innerProblemList.clear();
            }
            if (m_objectiveFunction != null)
            {
                m_objectiveFunction.Dispose();
            }
            if (Constraints() != null)
            {
                Constraints().Dispose();
            }
            if (m_reproduction != null)
            {
                m_reproduction.Dispose();
            }
            if (m_repairIndividual != null)
            {
                m_repairIndividual.Dispose();
            }
            if (m_localSearch != null)
            {
                m_localSearch.Dispose();
            }
            if (m_guidedConvergence != null)
            {
                m_guidedConvergence.Dispose();
            }
            if (m_gpBridge != null)
            {
                m_gpBridge.Dispose();
            }

            if (m_gpOperatorsContainer != null)
            {
                m_gpOperatorsContainer.Dispose();
            }
        }
        catch(Exception ex)
        {
            Logger.Log(ex);
        }
    }

    public static RngWrapper CreateRandomGenerator()
    {
        return new RngWrapper();
    }

    public boolean IsMultiObjective()
    {
        return m_objectiveFunction instanceof HeuristicMultiObjectiveFunction;
    }

    public boolean CheckConstraints(
        Individual individual)
    {
        return (m_constraints == null
                    ? true
                    : m_constraints.CheckConstraints(individual));
    }

    public boolean ContainsIntegerVariables()
    {
        if (m_enumOptimimisationPoblemType == EnumOptimimisationPoblemType.INTEGER)
        {
            return true;
        }
        return false;
    }

    public boolean ContainsContinuousVariables()
    {
        if (m_enumOptimimisationPoblemType == EnumOptimimisationPoblemType.CONTINUOUS)
        {
            return true;
        }
        return false;
    }


    public boolean ValidateIntegerProblem()
    {
        if (m_enumOptimimisationPoblemType == EnumOptimimisationPoblemType.INTEGER)
        {
            if (m_variableRangesIntegerProbl == null)
            {
                return false;
            }
            if (m_variableRangesIntegerProbl.length != VariableCount())
            {
                return false;
            }
        }
        return true;
    }

    public void PublishGridStats()
    {
        String strProblemName =
            m_problemStats.GetStrValue(
                EnumHeuristicProblem.ProblemName);
        m_problemStats.Time = DateTime.now();
        if (!(m_objectiveFunction instanceof MixedObjectiveFunctionDummy))
        {
            PublishUiMessageEvent.PublishGrid(
                strProblemName,
                strProblemName,
                EnumHeuristicProblem.SolverParams.toString(),
                m_problemStats.GetClassName(),
                m_problemStats,
                2,
                true);
        }
    }

    private static void SetPopulationToNestedProblems(
        int intPopulationSize,
        HeuristicProblem heuristicProblem)
    {
        if (heuristicProblem.m_innerProblemList != null &&
            heuristicProblem.m_innerProblemList.size() > 0)
        {
            for (HeuristicProblem currentHeuristicProblem : 
                heuristicProblem.m_innerProblemList)
            {
                currentHeuristicProblem.setPopulationSize(intPopulationSize);
            }
        }
    }

	public IRepair getRepairIndividual() 
	{
		return m_repairIndividual;
	}

	public void setRepairIndividual(IRepair repairIndividual) 
	{
		m_repairIndividual = repairIndividual;
	}

	public int getIterations() 
	{
		return m_intIterations;
	}

	public void setIterations(int iterations) 
	{
		m_intIterations = iterations;
	}

	public IHeuristicObjectiveFunction getObjectiveFunction() {
		return m_objectiveFunction;
	}

	public void setObjectiveFunction(IHeuristicObjectiveFunction objectiveFunction) {
		m_objectiveFunction = objectiveFunction;
	}

	public double[] getVariableRangesIntegerProbl() {
		return m_variableRangesIntegerProbl;
	}

	public void setVariableRangesIntegerProbl(
			double[] variableRangesIntegerProbl) {
		m_variableRangesIntegerProbl = variableRangesIntegerProbl;
	}

	public int getCurrentLocalSearchInstances() {
		return m_inrCurrentLocalSearchInstances;
	}

	public void setCurrentLocalSearchInstances(int currentLocalSearchInstances) {
		m_inrCurrentLocalSearchInstances = currentLocalSearchInstances;
	}

	public double getRepairProb() {
		return m_dblRepairProb;
	}

	public void setRepairProb(double repairProb) {
		m_dblRepairProb = repairProb;
	}

	public double getLocalSearchProb() {
		return m_dblLocalSearchProb;
	}

	public void setLocalSearchProb(double localSearchProb) {
		m_dblLocalSearchProb = localSearchProb;
	}

	public boolean isVerbose() {
		return m_blnVerbose;
	}

	public void setVerbose(boolean verbose) {
		m_blnVerbose = verbose;
	}

	public IIndividualFactory getIndividualFactory() {
		return m_individualFactory;
	}

	public void setIndividualFactory(IIndividualFactory individualFactory) {
		m_individualFactory = individualFactory;
	}

	public IInitialPopulation getInitialPopulation() {
		return m_initialPopulation;
	}

	public void setInitialPopulation(IInitialPopulation initialPopulation) {
		m_initialPopulation = initialPopulation;
	}

	public EvolutionarySolver getSolver() {
		return m_solver;
	}

	public void setSolver(EvolutionarySolver solver) {
		m_solver = solver;
	}

	public List<HeuristicProblem> getInnerProblemList() {
		return m_innerProblemList;
	}

	public void setInnerProblemList(List<HeuristicProblem> innerProblemList) {
		m_innerProblemList = innerProblemList;
	}

	public String getProblemName() {
		return m_strProblemName;
	}

	public void setProblemName(String problemName) {
		m_strProblemName = problemName;
	}

	public int getConvergence() {
		return m_intConvergence;
	}

	public void setConvergence(int convergence) {
		m_intConvergence = convergence;
	}

	public SelfDescribingTsEvent getProblemStats() {
		return m_problemStats;
	}

	public void setProblemStats(SelfDescribingTsEvent problemStats) {
		m_problemStats = problemStats;
	}

	public Population getPopulation() {
		return m_population;
	}

	public void setPopulation(Population population) {
		m_population = population;
	}

	public IReproduction getReproduction() {
		return m_reproduction;
	}

	public void setReproduction(IReproduction reproduction) {
		m_reproduction = reproduction;
	}

	public ILocalSearch getLocalSearch() {
		return m_localSearch;
	}

	public void setLocalSearch(ILocalSearch localSearch) 
	{
		m_localSearch = localSearch;
	}

	public AGuidedConvergence getGuidedConvergence() 
	{
		return m_guidedConvergence;
	}

	public void setGuidedConvergence(AGuidedConvergence guidedConvergence) 
	{
		m_guidedConvergence = guidedConvergence;
	}

	public boolean isDoLocalSearch() 
	{
		return m_blnDoLocalSearch;
	}

	public void setDoLocalSearch(boolean doLocalSearch) 
	{
		m_blnDoLocalSearch = doLocalSearch;
	}

	public boolean isDoClusterSolution() 
	{
		return m_blnDoClusterSolution;
	}

	public void setDoClusterSolution(boolean doClusterSolution) 
	{
		m_blnDoClusterSolution = doClusterSolution;
	}

	public AGpBridge getGpBridge() 
	{
		return m_gpBridge;
	}

	public void setGpBridge(AGpBridge gpBridge) 
	{
		m_gpBridge = gpBridge;
	}

	public GpOperatorsContainer getGpOperatorsContainer() 
	{
		return m_gpOperatorsContainer;
	}

	public void setGpOperatorsContainer(GpOperatorsContainer gpOperatorsContainer) 
	{
		m_gpOperatorsContainer = gpOperatorsContainer;
	}

}
