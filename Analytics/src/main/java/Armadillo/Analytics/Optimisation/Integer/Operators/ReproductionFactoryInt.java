package Armadillo.Analytics.Optimisation.Integer.Operators;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Optimisation.Base.Operators.Reproduction.IReproduction;
import Armadillo.Analytics.Optimisation.Base.Operators.Reproduction.ReproductionClass;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public class ReproductionFactoryInt
{
    public static IReproduction BuildReproductionInt(
        HeuristicProblem heuristicProblem)
    {
    	List<IReproduction> reproductionList = new ArrayList<IReproduction>();
        reproductionList.add(
            new ReproductionIntStd(heuristicProblem));
        reproductionList.add(
            new ReproductionIntGm(heuristicProblem));

        ReproductionClass reproduction =
            new ReproductionClass(
                heuristicProblem,
                reproductionList);

        return reproduction;
    }
}
