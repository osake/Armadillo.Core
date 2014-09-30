package Armadillo.Analytics.Optimisation.ProblemFactories;

import Armadillo.Analytics.Optimisation.Base.EnumOptimimisationPoblemType;
import Armadillo.Analytics.Optimisation.Base.ObjectiveFunctionType;
import Armadillo.Analytics.Optimisation.Base.OptimisationConstants;
import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.AGpBridge;
import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.GpOperatorsContainer;
import Armadillo.Analytics.Optimisation.Base.Helpers.OptimizationHelper;
import Armadillo.Analytics.Optimisation.Base.ObjectiveFunctions.IHeuristicObjectiveFunction;
import Armadillo.Analytics.Optimisation.Base.Operators.AGuidedConvergence;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.IndividualFactories.IndividualFactory;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ILocalSearch;
import Armadillo.Analytics.Optimisation.Base.Operators.PopulationClasses.IInitialPopulation;
import Armadillo.Analytics.Optimisation.Base.Operators.PopulationClasses.Population;
import Armadillo.Analytics.Optimisation.Base.Operators.PopulationClasses.RandomInitialPopulation;
import Armadillo.Analytics.Optimisation.Base.Operators.Reproduction.IReproduction;
import Armadillo.Analytics.Optimisation.Base.Problem.IHeuristicProblemFactory;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Base.Solvers.EvolutionarySolver;
import Armadillo.Analytics.Optimisation.Binary.Operators.GuidedConvergenceBln;
import Armadillo.Analytics.Optimisation.Binary.Operators.LocalSearch.LocalSearchStdBln;
import Armadillo.Analytics.Optimisation.Binary.Operators.Reproduction.ReproductionFactoryBln;
import Armadillo.Analytics.Optimisation.Continuous.ContinuousConstants;
import Armadillo.Analytics.Optimisation.Continuous.Operators.GuidedConvergenceDbl;
import Armadillo.Analytics.Optimisation.Continuous.Operators.ReproductionDblStd;
import Armadillo.Analytics.Optimisation.Continuous.Operators.LocalSearch.LocalSearchStdDbl;
import Armadillo.Analytics.Optimisation.Gp.GpConstants;
import Armadillo.Analytics.Optimisation.Integer.Operators.GuidedConvergenceInt;
import Armadillo.Analytics.Optimisation.Integer.Operators.ReproductionFactoryInt;
import Armadillo.Analytics.Optimisation.Integer.Operators.LocalSearch.LocalSearchStdInt;
import Armadillo.Analytics.Optimisation.MixedSolvers.MixedSolversConstants;
import Armadillo.Core.HCException;

public abstract class AHeuristicProblemFactory implements IHeuristicProblemFactory
{
    protected final IHeuristicObjectiveFunction m_objectiveFunction;
    private final EnumOptimimisationPoblemType m_problemType;
    protected AGpBridge m_gpBridge;
    protected GpOperatorsContainer m_gpOperatorsContainer;

    public AHeuristicProblemFactory(
            EnumOptimimisationPoblemType problemType,
            IHeuristicObjectiveFunction objectiveFunction)
        {
    		this(problemType, objectiveFunction, null, null);
        }
    
    public AHeuristicProblemFactory(
        EnumOptimimisationPoblemType problemType,
        IHeuristicObjectiveFunction objectiveFunction,
        GpOperatorsContainer gpOperatorsContainer,
        AGpBridge gpBridge)
    {
        m_problemType = problemType;
        m_objectiveFunction = objectiveFunction;
        m_gpOperatorsContainer = gpOperatorsContainer;
        m_gpBridge = gpBridge;
    }


    public HeuristicProblem BuildProblem()
    {
        return BuildProblem("");
    }

    public HeuristicProblem BuildProblem(String strProblemName)
    {
    	HeuristicProblem heuristicProblem =
            new HeuristicProblem(
                m_problemType,
                strProblemName);
        //
        // load objective
        //
        heuristicProblem.setObjectiveFunction(m_objectiveFunction);

        //
        // crete population
        //
        Population population = new Population(heuristicProblem);
        heuristicProblem.setPopulation(population);

        //
        // Create reproduction operator
        //
        BuildReproduction(heuristicProblem);

        //
        // Build guided convergence
        //
        BuildGuidedConvergence(heuristicProblem);

        //
        // create initial population
        //
        IInitialPopulation initialPopulation = new RandomInitialPopulation(
            heuristicProblem);
        heuristicProblem.setInitialPopulation(initialPopulation);

        //
        // create solver
        //
        EvolutionarySolver solver = new EvolutionarySolver(heuristicProblem);
        heuristicProblem.setSolver(solver);

        if (m_problemType == EnumOptimimisationPoblemType.CONTINUOUS)
        {
            SetContinuousDefaultParams(heuristicProblem);
        }
        else if (m_problemType == EnumOptimimisationPoblemType.INTEGER)
        {
            SetIntegerDefaultParams(heuristicProblem);
        }
        else if (m_problemType == EnumOptimimisationPoblemType.BINARY)
        {
            SetBinaryDefaultParams(heuristicProblem);
        }
        else if (m_problemType == EnumOptimimisationPoblemType.MIXED)
        {
            SetMixedDefaultParams(heuristicProblem);
        }
        else if (m_problemType == EnumOptimimisationPoblemType.GENETIC_PROGRAMMING)
        {
            //
            // set bridge
            //
            heuristicProblem.setGpBridge(m_gpBridge);

            SetGpDefaultParams(heuristicProblem);
        }

        //
        // set local searh operator
        //
        LoadLocalSearch(heuristicProblem);

        if (m_problemType != EnumOptimimisationPoblemType.MIXED)
        {
            //
            // Set default individual factory
            //
            heuristicProblem.setIndividualFactory(new IndividualFactory(
			    heuristicProblem));
        }


        //
        // set number of threads
        //
        heuristicProblem.setThreads(OptimisationConstants.INT_THREADS);

        heuristicProblem.setDoClusterSolution(true);


        return heuristicProblem;
    }

    private void BuildGuidedConvergence(
        HeuristicProblem heuristicProblem)
    {
        //
        // create guided convergence
        //
        AGuidedConvergence guidedConvergence = null;
        if (m_problemType == EnumOptimimisationPoblemType.CONTINUOUS)
        {
            guidedConvergence = new GuidedConvergenceDbl(heuristicProblem);
        }
        else if (m_problemType == EnumOptimimisationPoblemType.INTEGER)
        {
            guidedConvergence = new GuidedConvergenceInt(heuristicProblem);
        }
        else if (m_problemType == EnumOptimimisationPoblemType.BINARY)
        {
            guidedConvergence = new GuidedConvergenceBln(heuristicProblem);
        }
        heuristicProblem.setGuidedConvergence(guidedConvergence);
    }

    private void LoadLocalSearch(
        HeuristicProblem heuristicProblem)
    {
        //
        // Set local search for non multi objective problems
        //
        ILocalSearch localSearch = null;
        if (m_objectiveFunction.ObjectiveFunctionType() !=
            ObjectiveFunctionType.MULTI_OBJECTIVE_FUNCT &&
            m_objectiveFunction.ObjectiveFunctionType() !=
            ObjectiveFunctionType.MIXED)
        {
            if (m_problemType == EnumOptimimisationPoblemType.CONTINUOUS)
            {
                localSearch = new LocalSearchStdDbl(
                    heuristicProblem);
            }
            else if (m_problemType == EnumOptimimisationPoblemType.BINARY)
            {
                localSearch = new LocalSearchStdBln(
                    heuristicProblem);
            }
            else if (m_problemType == EnumOptimimisationPoblemType.INTEGER)
            {
                localSearch = new LocalSearchStdInt(
                    heuristicProblem);
            }

            heuristicProblem.setLocalSearch(localSearch);
            heuristicProblem.setDoLocalSearch(true);
            heuristicProblem.setLocalSearchProb(OptimisationConstants.DBL_LOCAL_SEARCH);
        }
    }

    private void BuildReproduction(
        HeuristicProblem heuristicProblem)
    {
        //
        // create reproduction
        //
        IReproduction reproduction = null;
        if (m_problemType == EnumOptimimisationPoblemType.CONTINUOUS)
        {
            reproduction = new ReproductionDblStd(
                heuristicProblem);
        }
        else if (m_problemType == EnumOptimimisationPoblemType.INTEGER)
        {
            reproduction = ReproductionFactoryInt.BuildReproductionInt(
                heuristicProblem);
        }
        else if (m_problemType == EnumOptimimisationPoblemType.BINARY)
        {
            reproduction = ReproductionFactoryBln.BuildReproductionBln(
                heuristicProblem);
        }
        else if (m_problemType == EnumOptimimisationPoblemType.MIXED)
        {
            //
            // Do nothing
            // Reproduction should be loaded in a later stage
            //
        }
        else if (m_problemType == EnumOptimimisationPoblemType.GENETIC_PROGRAMMING)
        {
            //
            // Do nothing
            // Reproduction should be loaded in a later stage
            //
        }
        else
        {
            throw new HCException("Error. Problem type not implemented");
        }
        heuristicProblem.setReproduction(reproduction);
    }

    private static void SetContinuousDefaultParams(
        HeuristicProblem heuristicProblem)
    {
        ValidateIterations(heuristicProblem);

        //
        // set convergence
        //
        if (heuristicProblem.getObjectiveFunction().VariableCount() <=
            ContinuousConstants.INT_SMALL_PROBLEM_DE &&
            heuristicProblem.getConvergence() == 0 &&
            heuristicProblem.ObjectiveCount() == 1)
        {
            heuristicProblem.setConvergence(ContinuousConstants.INT_DE_SMALL_CONVERGENCE);
        }
        else
        {
            heuristicProblem.setConvergence(ContinuousConstants.INT_DE_CONVERGENCE);
        }

        ValidatePopulation(heuristicProblem);

        heuristicProblem.getSolver().SetSolverName("Continuous Genetic Algorithm Solver");
    }

    private static void SetBinaryDefaultParams(
        HeuristicProblem heuristicProblem)
    {
        ValidateIterations(heuristicProblem);

        //
        // set convergence
        //
        if (heuristicProblem.getObjectiveFunction().VariableCount() <=
            OptimisationConstants.INT_SMALL_PROBLEM_GA)
        {
            heuristicProblem.setConvergence(OptimisationConstants.INT_GA_SMALL_CONVERGENCE);
        }
        else
        {
            heuristicProblem.setConvergence(OptimisationConstants.INT_GA_CONVERGENCE);
        }

        ValidatePopulation(heuristicProblem);

        heuristicProblem.getSolver().SetSolverName("Binary Genetic Algorithm Solver");
    }

    private static void SetIntegerDefaultParams(
        HeuristicProblem heuristicProblem)
    {
        ValidateIterations(heuristicProblem);

        //
        // set convergence
        //
        if (heuristicProblem.getObjectiveFunction().VariableCount() <=
        		OptimisationConstants.INT_SMALL_PROBLEM_GA)
        {
            heuristicProblem.setConvergence(OptimisationConstants.INT_GA_SMALL_CONVERGENCE);
        }
        else
        {
            heuristicProblem.setConvergence(OptimisationConstants.INT_GA_CONVERGENCE);
        }

        ValidatePopulation(heuristicProblem);

        heuristicProblem.getSolver().SetSolverName("Integer Genetic Algorithm Solver");
    }

    private static void SetMixedDefaultParams(
        HeuristicProblem heuristicProblem)
    {
        ValidateIterations(heuristicProblem);
        //
        // set convergence
        //
        if (heuristicProblem.getObjectiveFunction().VariableCount() <=
        		OptimisationConstants.INT_SMALL_PROBLEM_GA)
        {
            heuristicProblem.setConvergence(MixedSolversConstants.INT_GA_SMALL_CONVERGENCE);
        }
        else
        {
            heuristicProblem.setConvergence(MixedSolversConstants.INT_GA_CONVERGENCE);
        }

        ValidatePopulation(heuristicProblem);

        heuristicProblem.getSolver().SetSolverName("Mixed Genetic Algorithm Solver");
    }

    private static void SetGpDefaultParams(
        HeuristicProblem heuristicProblem)
    {
        //
        // set iterations
        //
        heuristicProblem.setIterations(GpConstants.INT_GP_ITERATIONS);
        //
        // set convergence
        //
        heuristicProblem.setConvergence(GpConstants.INT_GP_CONVERGENCE);

        ValidatePopulation(heuristicProblem);

        heuristicProblem.getSolver().SetSolverName("Genetic Programming Algorithm");
    }

    private static void ValidatePopulation(
        HeuristicProblem heuristicProblem)
    {
        //
        // set population
        //
        if (heuristicProblem.PopulationSize() == 0)
        {
            heuristicProblem.setPopulationSize(
            		OptimisationConstants.INT_POPULATION_SIZE);
        }
    }

    private static void ValidateIterations(
        HeuristicProblem heuristicProblem)
    {
        //
        // set iterations
        //
        if (heuristicProblem.getIterations() == 0)
        {
            heuristicProblem.setIterations(OptimizationHelper.GetHeuristicSolverIterations(
			    heuristicProblem.VariableCount()));
        }
    }
}
