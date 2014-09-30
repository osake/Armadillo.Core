package Armadillo.Analytics.Optimisation.Binary.Operators.Reproduction;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Optimisation.Base.Operators.Reproduction.IReproduction;
import Armadillo.Analytics.Optimisation.Base.Operators.Reproduction.ReproductionClass;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public class ReproductionFactoryBln 
{
    public static IReproduction BuildReproductionBln(
            HeuristicProblem heuristicProblem)
        {
    	List<IReproduction> reproductionList = new ArrayList<IReproduction>();
            reproductionList.add(
                new ReproductionBlnStd(heuristicProblem));
            reproductionList.add(
                new ReproductionBlnGm(heuristicProblem));

            ReproductionClass reproduction =
                new ReproductionClass(
                    heuristicProblem,
                    reproductionList);

            return reproduction;
        }

}
