package Armadillo.Analytics.Optimisation.Tests;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Mathematics.InequalityType;
import Armadillo.Analytics.Optimisation.Base.EnumOptimimisationPoblemType;
import Armadillo.Analytics.Optimisation.Base.Constraints.ConstraintClass;
import Armadillo.Analytics.Optimisation.Base.Constraints.IHeuristicConstraint;
import Armadillo.Analytics.Optimisation.Base.ObjectiveFunctions.IHeuristicObjectiveFunction;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.Concurrent.ConcurrentHelper;
import Armadillo.Analytics.Optimisation.MixedSolvers.MixedHeurPrblmFctGeneric;
import Armadillo.Analytics.Optimisation.MixedSolvers.DummyProblemFactories.HeuristicProblFactDummy;

public class KnapsackProblemGenericTest
{
	private static final int THREADS = ConcurrentHelper.CPUS;

	public static void main(String args[])
	{
		RunKnapsackTest();
	}
	
    public static void RunKnapsackTest()
    {
    	try
    	{
	        //TestKnapsackProblem.TestDeSolver();
	        runSolver();
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
    }

    public static void runSolver()
    {
    	try
    	{
	        //
	        // test problem
	        //
	        HeuristicProblem mixedProblem = GetMixedProblem();
	        mixedProblem.getSolver().Solve();
	        mixedProblem.Dispose();
	        Console.WriteLine("End of the problem!");
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    private static HeuristicProblem GetMixedProblem()
    {
    	try
    	{
	        IHeuristicObjectiveFunction objectiveFunction = CreateMixedSolverObjectiveFunction();
	        int intVarCountInteger = GetReturnArr().length;
	        int intVarCountContinuous = GetReturnArr().length;
	
	        List<HeuristicProblem> dummyHeuristicProblem =
	            new ArrayList<HeuristicProblem>();
	
	        HeuristicProblem heuristicProblemDbl =
	            new HeuristicProblFactDummy(
	                EnumOptimimisationPoblemType.CONTINUOUS,
	                intVarCountContinuous).BuildProblem();
	
	        HeuristicProblem heuristicProblemInt =
	            new HeuristicProblFactDummy(
	                EnumOptimimisationPoblemType.INTEGER,
	                intVarCountContinuous).BuildProblem();
	        heuristicProblemInt.setVariableRangesIntegerProbl(GetVariableRanges());
	
	        dummyHeuristicProblem.add(heuristicProblemDbl);
	        dummyHeuristicProblem.add(heuristicProblemInt);
	
	        ConstraintClass constraintClass = LoadMixedConstraints(
	            intVarCountInteger,
	            intVarCountContinuous,
	            heuristicProblemInt,
	            heuristicProblemDbl);
	
	        MixedHeurPrblmFctGeneric mixedProblemFactory =
	            new MixedHeurPrblmFctGeneric(
	                objectiveFunction,
	                dummyHeuristicProblem,
	                constraintClass);
	
	        HeuristicProblem mixedProblem =
	            mixedProblemFactory.BuildProblem();
	        mixedProblem.setThreads(THREADS);
	
	        return mixedProblem;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }

    private static double[] GetVariableRanges()
    {
        return new double[] {100, 100};
    }

    private static ConstraintClass LoadMixedConstraints(
        int intVarCountInteger,
        int intVarCountContinuous,
        HeuristicProblem heuristicProblemInteger,
        HeuristicProblem heuristicProblemContinuous)
    {
        // scale is required for continous problem
        // the solver support inly values in the [0,1] range
        //
        double[] dblScaleInt = GetIntScaleFactors();
        double[] dblScaleDbl = GetDblScaleFactors();
        ConstraintClass constraintClass = new ConstraintClass();
        //
        // load integer and continuous constraints
        //
        if (intVarCountInteger > 0)
        {
            LoadConstraints(
                constraintClass,
                true,
                dblScaleInt,
                heuristicProblemInteger);
        }
        if (intVarCountContinuous > 0)
        {
            LoadConstraints(
                constraintClass,
                false,
                dblScaleDbl,
                heuristicProblemContinuous);
        }
        return constraintClass;
    }

    private static IHeuristicObjectiveFunction CreateMixedSolverObjectiveFunction()
    {
        //
        // scale is required for continous problem
        // the solver support inly values in the [0,1] range
        //
        double[] dblScaleInt = GetIntScaleFactors();
        double[] dblScaleDbl = GetDblScaleFactors();

        // load objective function
        double[] dblReturnArr = GetReturnArr();
        IHeuristicObjectiveFunction objectiveFunction =
            new KnapsackObjectiveFunctionGeneric(
                dblReturnArr,
                dblReturnArr,
                dblScaleInt,
                dblScaleDbl);

        return objectiveFunction;
    }

    private static double[] GetIntScaleFactors()
    {
        return new double[] { 1, 1 };
    }

    private static double[] GetDblScaleFactors()
    {
        return new double[] { 1000, 1000 };
    }

    private static double[] GetReturnArr()
    {
        return new double[] {50, 60};
    }

    private static void LoadConstraints(
        ConstraintClass constraintClass,
        boolean blnIsInteger,
        double[] dblScale,
        HeuristicProblem heuristicProblem)
    {
        double[] dblCoeff = { 2, 1 };
        LoadConstraint(
            constraintClass,
            dblScale,
            dblCoeff,
            60,
            blnIsInteger,
            InequalityType.LESS_OR_EQUAL,
            heuristicProblem);

        dblCoeff = new double[] { 1, 2 };
        LoadConstraint(
            constraintClass,
            dblScale,
            dblCoeff,
            60,
            blnIsInteger,
            InequalityType.LESS_OR_EQUAL,
            heuristicProblem);

        dblCoeff = new double[] { 0, 1 };
        LoadConstraint(
            constraintClass,
            dblScale,
            dblCoeff,
            10,
            blnIsInteger,
            InequalityType.LESS_OR_EQUAL,
            heuristicProblem);

        dblCoeff = new double[] { 1, 0 };
        LoadConstraint(
            constraintClass,
            dblScale,
            dblCoeff,
            10,
            blnIsInteger,
            InequalityType.GREATER_THAN,
            heuristicProblem);
    }

    private static void LoadConstraint(
        ConstraintClass constraintClass,
        double[] dblScale,
        double[] dblCoeff,
        double dblBoundary,
        boolean blnIsInteger,
        InequalityType inequalityType,
        HeuristicProblem heuristicProblem)
    {
        IHeuristicConstraint constraint;

        if (blnIsInteger)
        {
            constraint = new LinearConstraintGeneric(
                dblCoeff,
                dblScale,
                null,
                inequalityType,
                dblBoundary,
                heuristicProblem);
        }
        else
        {
            constraint = new LinearConstraintGeneric(
                dblCoeff,
                dblScale,
                null,
                inequalityType,
                dblBoundary,
                heuristicProblem);
        }

        constraintClass.AddConstraint(constraint);
    }
}
