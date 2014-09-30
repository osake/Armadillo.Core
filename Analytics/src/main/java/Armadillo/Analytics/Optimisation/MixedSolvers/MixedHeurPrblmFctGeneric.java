package Armadillo.Analytics.Optimisation.MixedSolvers;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Optimisation.Base.EnumOptimimisationPoblemType;
import Armadillo.Analytics.Optimisation.Base.Constraints.ConstraintClass;
import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.GpOperatorsContainer;
import Armadillo.Analytics.Optimisation.Base.Delegates.CompletedIterationDelegate;
import Armadillo.Analytics.Optimisation.Base.Delegates.EvaluateObjectiveDelegate;
import Armadillo.Analytics.Optimisation.Base.ObjectiveFunctions.IHeuristicObjectiveFunction;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.Repair.RepairClass;
import Armadillo.Analytics.Optimisation.Integer.Operators.RepairConstraintsInt;
import Armadillo.Analytics.Optimisation.MixedSolvers.DummyObjectiveFunctions.MixedObjectiveFunctionDummy;
import Armadillo.Analytics.Optimisation.MixedSolvers.DummyProblemFactories.HeuristicProblFactDummy;
import Armadillo.Analytics.Optimisation.MixedSolvers.Operators.MixedIndividualFactoryGeneric;
import Armadillo.Analytics.Optimisation.MixedSolvers.Operators.MixedLocalSearch;
import Armadillo.Analytics.Optimisation.MixedSolvers.Operators.MixedReproductionGeneric;
import Armadillo.Analytics.Optimisation.ProblemFactories.AHeuristicProblemFactory;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Binary.Operators.RepairConstraintsBln;
import Armadillo.Analytics.Optimisation.Continuous.Operators.RepairConstraintsDbl;
import Armadillo.Core.HCException;

public class MixedHeurPrblmFctGeneric extends AHeuristicProblemFactory
{
    private final ConstraintClass m_constraintClass;
    private final List<HeuristicProblem> m_dummyProblems;

    public MixedHeurPrblmFctGeneric(
        IHeuristicObjectiveFunction objectiveFunction,
        int intVarCountInt,
        int intVarCountDbl,
        int intVarCountBln,
        ConstraintClass constraintClass,
        double[] dblVariableRangesIntegerProbl)
    {
        this(objectiveFunction,
                GetDummyProblems(
                    intVarCountInt,
                    intVarCountDbl,
                    intVarCountBln,
                    dblVariableRangesIntegerProbl),
                constraintClass);
    }

    public MixedHeurPrblmFctGeneric(
        IHeuristicObjectiveFunction objectiveFunction,
        List<HeuristicProblem> problFactDummies,
        ConstraintClass constraintClass) 
    {
        this(objectiveFunction,
                problFactDummies,
                constraintClass,
                null);
    }

    public MixedHeurPrblmFctGeneric(
        IHeuristicObjectiveFunction objectiveFunction,
        List<HeuristicProblem> problFactDummies,
        ConstraintClass constraintClass,
        GpOperatorsContainer gpOperatorsContainer)
    {
        super(EnumOptimimisationPoblemType.MIXED,
                objectiveFunction);
        m_constraintClass = constraintClass;
        m_dummyProblems = problFactDummies;
        m_gpOperatorsContainer = gpOperatorsContainer;
    }

    public static HeuristicProblem BuildDummyProblemDouble(int intVarCounts)
    {
    	HeuristicProblFactDummy heuristicProblFactDummy =
            new HeuristicProblFactDummy(
                EnumOptimimisationPoblemType.CONTINUOUS,
                intVarCounts);
        return heuristicProblFactDummy.BuildProblem();
    }

    public static HeuristicProblem BuildDummyProblemInteger(int intVarCounts)
    {
    	HeuristicProblFactDummy heuristicProblFactDummyInt =
            new HeuristicProblFactDummy(
                EnumOptimimisationPoblemType.INTEGER,
                intVarCounts);
        return heuristicProblFactDummyInt.BuildProblem();
    }

    @Override
    public HeuristicProblem BuildProblem()
    {
        return BuildProblem("");
    }

    public HeuristicProblem BuildProblem(String strProblemName)
    {
        //
        // This method creates dummy subproblems for integer 
        // and continuous cases.
        // 
        // The subproblems are linked to the main mixed problem:
        //     - Load main solver operators for these subproblems.
        // 
        // The fitness function evaluation is done by the main problem
        // The reproduction, repair and local search opeations
        // are done by the dummy problems
        // 

    	HeuristicProblem mixedProblem = super.BuildProblem(strProblemName);
        //
        // invalidate guided convergence operator
        //
        mixedProblem.setGuidedConvergence(null);

        mixedProblem.setInnerProblemList(m_dummyProblems);


        for (HeuristicProblem dummyHeuristicProbl : m_dummyProblems)
        {
            //
            // Register objective functions
            // The objective function is evaluated by the main problem. 
            // Communication is done via event trigger.
            //
            SetObjectiveFunction(
                mixedProblem,
                dummyHeuristicProbl);

            //
            // Register Guided Convergence operator
            // The operator is executed by the main problem.
            // Comunication is done via event trigger.
            //
            SetGcOperators(
                mixedProblem,
                dummyHeuristicProbl);

            //
            // Register population.
            // The population of the subproblems is the same as the populaion
            // of the main problem
            // 
            SetPopulation(
                mixedProblem,
                dummyHeuristicProbl);

            //if(dummyHeuristicProbl.OptimimisationPoblemType ==
            //    OptimimisationPoblemType.INTEGER)
            //{
            //    if(mixedProblem.VariableRangesIntegerProbl != null)
            //    {
            //        throw new HCException("Multiple integer problems not allowed");
            //    }
            //    dummyHeuristicProbl.VariableRangesIntegerProbl =
            //        m_dblVariableRangesIntegerProbl;
            //    mixedProblem.VariableRangesIntegerProbl =
            //        m_dblVariableRangesIntegerProbl;
            //}
        }

        //
        // load reproduction operators
        //
        SetReproduction(mixedProblem);

        //
        // load individual factory
        //
        mixedProblem.setIndividualFactory(new MixedIndividualFactoryGeneric(
		    mixedProblem,
		    m_dummyProblems));

        //
        // load local search operators
        //
        mixedProblem.setLocalSearch(new MixedLocalSearch(
		    mixedProblem,
		    m_dummyProblems));

        //
        // load constraints
        //
        LoadConstraints(mixedProblem);

        //
        // set gc operators
        //
        SetGpOperators(mixedProblem);

        return mixedProblem;
    }

    public static HeuristicProblem BuildDummyProblem(
        EnumOptimimisationPoblemType enumOptimimisationPoblemType,
        int intVarCount)
    {
    	HeuristicProblFactDummy heuristicProblFactDummy =
            new HeuristicProblFactDummy(
                enumOptimimisationPoblemType,
                intVarCount);
        return heuristicProblFactDummy.BuildProblem();
    }

    private static List<HeuristicProblem> GetDummyProblems(
        int intVarCountInt,
        int intVarCountDbl,
        int intVarCountBln,
        double[] dblVariableRangesIntegerProbl)
    {
    	List<HeuristicProblem> dummyHeuristicProblems =
            new ArrayList<HeuristicProblem>();
        //
        // load dummy problems
        //
        // dummy continous problem
        if (intVarCountDbl > 0)
        {
        	HeuristicProblem heuristicProblDbl =
                BuildDummyProblem(
                    EnumOptimimisationPoblemType.CONTINUOUS,
                    intVarCountDbl);
            dummyHeuristicProblems.add(
                heuristicProblDbl);
        }

        // dummy integer problem
        if (intVarCountInt > 0)
        {
        	HeuristicProblem heuristicProblDummyInt =
                BuildDummyProblem(
                    EnumOptimimisationPoblemType.INTEGER,
                    intVarCountInt);

            heuristicProblDummyInt.setVariableRangesIntegerProbl(dblVariableRangesIntegerProbl);

            dummyHeuristicProblems.add(heuristicProblDummyInt);
        }

        // dummy binary problem
        if (intVarCountBln > 0)
        {
            HeuristicProblem heuristicProblDummyBln =
                BuildDummyProblem(
                    EnumOptimimisationPoblemType.BINARY,
                    intVarCountBln);
            dummyHeuristicProblems.add(heuristicProblDummyBln);
        }
        if (dummyHeuristicProblems.size() == 0)
        {
            throw new HCException("No dummy problems");
        }

        return dummyHeuristicProblems;
    }

    private void SetGpOperators(
        HeuristicProblem mixedProblem)
    {
        //
        // set gp operators
        //
        for (HeuristicProblem problem : m_dummyProblems)
        {
            SetGpOperatorRecursively(problem);
            //
            // set params for gp problem
            //
            mixedProblem.setGpOperatorsContainer(m_gpOperatorsContainer);
            mixedProblem.getInitialPopulation().setDoLocalSearch(false);
            mixedProblem.setLocalSearchProb(0.1);
        }
    }

    private void SetGpOperatorRecursively(
        HeuristicProblem dummyHeuristicProbl)
    {
        if (dummyHeuristicProbl.EnumOptimimisationPoblemType() ==
            EnumOptimimisationPoblemType.GENETIC_PROGRAMMING ||
            dummyHeuristicProbl.EnumOptimimisationPoblemType() ==
            EnumOptimimisationPoblemType.MIXED)
        {
            dummyHeuristicProbl.setGpOperatorsContainer(m_gpOperatorsContainer);
        }

        //
        // set population for nested problems
        //
        if (dummyHeuristicProbl.getInnerProblemList() != null &&
            dummyHeuristicProbl.getInnerProblemList().size() > 0)
        {
            for (HeuristicProblem nestedHeuristicProblem : 
                dummyHeuristicProbl.getInnerProblemList())
            {
                SetGpOperatorRecursively(
                    nestedHeuristicProblem);
            }
        }
    }

    private void SetReproduction(
        HeuristicProblem mixedProblem)
    {
        mixedProblem.setReproduction(new MixedReproductionGeneric(
            mixedProblem,
            m_dummyProblems,
            m_gpOperatorsContainer));

        //
        // set reproduction recursively
        //
        for (HeuristicProblem dummyHeuristicProb :
            m_dummyProblems)
        {
            SetReproductionOperatorRecursively(
                mixedProblem,
                dummyHeuristicProb);
        }
    }

    private static void SetReproductionOperatorRecursively(
        HeuristicProblem mixedProblem,
        HeuristicProblem dummyHeuristicProblem)
    {
        dummyHeuristicProblem.setReproduction(mixedProblem.getReproduction());

        //
        // recursively set reproduction for nested problems
        //
        if (dummyHeuristicProblem.getInnerProblemList() != null &&
            dummyHeuristicProblem.getInnerProblemList().size() > 0)
        {
            for (HeuristicProblem nestedHeuristicProblem :
                dummyHeuristicProblem.getInnerProblemList())
            {
                SetReproductionOperatorRecursively(
                    mixedProblem,
                    nestedHeuristicProblem);
            }
        }
    }

    private void LoadConstraints(
        HeuristicProblem mixedProblem)
    {
        mixedProblem.setConstraints(m_constraintClass);

        if (m_constraintClass != null)
        {
            //
            // set repair operator
            //
            mixedProblem.setRepairIndividual(new RepairClass(mixedProblem));

            for (HeuristicProblem dummyProblem : m_dummyProblems)
            {
                //
                // add constraints
                // The same cosntraints apply for main problem
                // and for the subproblems
                //
                LoadConstraint(mixedProblem, dummyProblem);
            }
        }
    }

    private void LoadConstraint(
        HeuristicProblem mixedProblem,
        HeuristicProblem dummyProblem)
    {
        if (dummyProblem != null)
        {
            //
            // Create repair operator
            // 
        	RepairClass repairOperator =
                new RepairClass(dummyProblem);
            dummyProblem.setRepairIndividual(repairOperator);
            dummyProblem.setConstraints(m_constraintClass);

            if (dummyProblem.EnumOptimimisationPoblemType() ==
                EnumOptimimisationPoblemType.INTEGER)
            {
            	RepairConstraintsInt repairConstraintsInt =
                    new RepairConstraintsInt(
                        dummyProblem);

                dummyProblem.getRepairIndividual().AddRepairOperator(
                    repairConstraintsInt);

                mixedProblem.getRepairIndividual().AddRepairOperator(
                    repairConstraintsInt);
            }
            else if (dummyProblem.EnumOptimimisationPoblemType() ==
                     EnumOptimimisationPoblemType.CONTINUOUS)
            {
            	RepairConstraintsDbl repairConstraintsDbl =
                    new RepairConstraintsDbl(
                        dummyProblem);

                dummyProblem.getRepairIndividual().AddRepairOperator(
                    repairConstraintsDbl);

                mixedProblem.getRepairIndividual().AddRepairOperator(
                    repairConstraintsDbl);
            }
            else if (dummyProblem.EnumOptimimisationPoblemType() ==
                     EnumOptimimisationPoblemType.BINARY)
            {
            	RepairConstraintsBln repairConstraintsBln =
                    new RepairConstraintsBln(
                        dummyProblem);

                dummyProblem.getRepairIndividual().AddRepairOperator(
                    repairConstraintsBln);

                mixedProblem.getRepairIndividual().AddRepairOperator(
                    repairConstraintsBln);
            }

            //
            // load constraint to nested problems recursively
            //
            if (dummyProblem.getInnerProblemList() != null &&
                dummyProblem.getInnerProblemList().size() > 0)
            {
                for (HeuristicProblem innerHeuristicProblem :
                    dummyProblem.getInnerProblemList())
                {
                    LoadConstraint(
                        mixedProblem,
                        innerHeuristicProblem);
                }
            }
        }
    }

    private static void SetPopulation(
        HeuristicProblem mixedProblem,
        HeuristicProblem dummyHeuristicProb)
    {
        if (dummyHeuristicProb != null)
        {
            dummyHeuristicProb.setPopulation(mixedProblem.getPopulation());
            dummyHeuristicProb.setPopulationSize(mixedProblem.PopulationSize());

            //
            // set population for nested problems
            //
            if (dummyHeuristicProb.getInnerProblemList() != null &&
                dummyHeuristicProb.getInnerProblemList().size() > 0)
            {
                for (HeuristicProblem nestedHeuristicProblem :
                    dummyHeuristicProb.getInnerProblemList())
                {
                    SetPopulation(
                        mixedProblem,
                        nestedHeuristicProblem);
                }
            }
        }
    }

    private static void SetObjectiveFunction(
        final HeuristicProblem mixedHeuristicProb,
        HeuristicProblem dummyHeuristicProb)
    {
        if (dummyHeuristicProb != null)
        {
        	MixedObjectiveFunctionDummy mixedObjectiveFunctionDummy =
                (MixedObjectiveFunctionDummy) dummyHeuristicProb.getObjectiveFunction();
        	EvaluateObjectiveDelegate evaluateObjectiveDelegate = new EvaluateObjectiveDelegate()
        	{
        		@Override
        		public double invoke(Individual individual) 
        		{
        			return mixedHeuristicProb.getObjectiveFunction().Evaluate(individual);
        		}
        	};
			mixedObjectiveFunctionDummy.addEvaluateDelegate(evaluateObjectiveDelegate);
//            mixedObjectiveFunctionDummy.OnEvaluateObjective +=
//                mixedHeuristicProb.ObjectiveFunction.Evaluate;

            //
            // set objective function for nested problems
            //
            if (dummyHeuristicProb.getInnerProblemList() != null &&
                dummyHeuristicProb.getInnerProblemList().size() > 0)
            {
                for (HeuristicProblem nestedHeuristicProblem : 
                    dummyHeuristicProb.getInnerProblemList())
                {
                    SetObjectiveFunction(
                        mixedHeuristicProb,
                        nestedHeuristicProblem);
                }
            }
        }
    }

    private static void SetGcOperators(
        HeuristicProblem mixedHeuristicProb,
        final HeuristicProblem dummyHeuristicProb)
    {
        if (dummyHeuristicProb != null &&
            dummyHeuristicProb.getGuidedConvergence() != null)
        {
            CompletedIterationDelegate completedIterationDelegate = 
            		new CompletedIterationDelegate()
            {
            	@Override
            	public void invoke(HeuristicProblem heuristicProblem) 
            	{
            		dummyHeuristicProb.getGuidedConvergence().UpdateGcProbabilities(heuristicProblem);
            	}
            };
			mixedHeuristicProb.getSolver().addCompletedIterationDelegate(completedIterationDelegate);
//            .OnCompletedGeneration +=
//                dummyHeuristicProb.GuidedConvergence.UpdateGcProbabilities;

            //
            // load inner gc operators recursively
            //
            if (dummyHeuristicProb.getInnerProblemList() != null &&
                dummyHeuristicProb.getInnerProblemList().size() > 0)
            {
                for (HeuristicProblem nestedHeuristicProblem : 
                    dummyHeuristicProb.getInnerProblemList())
                {
                    SetGcOperators(
                        mixedHeuristicProb,
                        nestedHeuristicProblem);
                }
            }
        }
    }
}
