package Armadillo.Analytics.Optimisation.Integer.Operators.LocalSearch.NmLocalSearch;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.ANmPopulationGenerator;
import Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.DataStructures.ANmVertex;

public class NmPopulationGeneratorInt extends ANmPopulationGenerator
{
    public NmPopulationGeneratorInt(HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
    }

    @Override
    public String ToStringIndividual(Individual individual)
    {
        return individual.ToStringInt();
    }

    @Override
    protected double[] GetChromosomeCopy(
        Individual individual)
    {
        int[] intChromosomeArr = individual.GetChromosomeCopyInt();
        double[] dblChromosomeArr = new double[intChromosomeArr.length];

        for (int i = 0; i < intChromosomeArr.length; i++)
        {
            dblChromosomeArr[i] = intChromosomeArr[i];
        }
        return dblChromosomeArr;
    }

    @Override
    public ANmVertex CreateNmVertex(
        int intDimensions,
        Individual individual)
    {
        return new NmVertexInt(
            individual.Clone(m_heuristicProblem),
            m_heuristicProblem);
    }
}
