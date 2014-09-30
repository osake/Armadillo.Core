package Armadillo.Analytics.Optimisation.MixedSolvers.DummyProblemFactories;

import Armadillo.Analytics.Optimisation.Base.EnumOptimimisationPoblemType;
import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.AGpBridge;
import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.GpOperatorsContainer;
import Armadillo.Analytics.Optimisation.MixedSolvers.DummyObjectiveFunctions.MixedObjectiveFunctionDummy;
import Armadillo.Analytics.Optimisation.ProblemFactories.AHeuristicProblemFactory;

public class HeuristicProblFactDummy extends AHeuristicProblemFactory
{
    public HeuristicProblFactDummy(
        EnumOptimimisationPoblemType enumOptimimisationPoblemType,
        GpOperatorsContainer gpOperatorsContainer,
        AGpBridge gpBridge) 
    {
        this(
	        enumOptimimisationPoblemType,
	        0,
	        gpOperatorsContainer,
	        gpBridge);
    }

    public HeuristicProblFactDummy(
        EnumOptimimisationPoblemType enumOptimimisationPoblemType,
        int intVariableCount) 
    {
        this(
        enumOptimimisationPoblemType,
        intVariableCount,
        null,
        null);
    }

    public HeuristicProblFactDummy(
        EnumOptimimisationPoblemType enumOptimimisationPoblemType,
        int intVariableCount,
        GpOperatorsContainer gpOperatorsContainer,
        AGpBridge gpBridge)
    {
            super(
	            enumOptimimisationPoblemType,
	            new MixedObjectiveFunctionDummy(intVariableCount),
	            gpOperatorsContainer,
	            gpBridge);
    }
}
