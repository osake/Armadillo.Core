package Armadillo.Analytics.Optimisation.Continuous.Operators.LocalSearch.NmLocalSearch;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.ANmPopulationGenerator;
import Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.DataStructures.ANmVertex;

public class NmPopulationGeneratorDbl extends ANmPopulationGenerator
{
    public NmPopulationGeneratorDbl(HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
    }

    @Override
    public String ToStringIndividual(Individual individual)
    {
        return individual.ToStringDbl();
    }

    @Override
    protected double[] GetChromosomeCopy(
        Individual individual)
    {
        return individual.GetChromosomeCopyDbl();
    }

    @Override
    public ANmVertex CreateNmVertex(
        int intDimensions,
        Individual individual)
    {
        return new NmVertexDbl(
            individual.Clone(m_heuristicProblem),
            m_heuristicProblem);
    }
}
