package Armadillo.Analytics.Optimisation.Continuous.Operators.LocalSearch.NmLocalSearch;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.DataStructures.ANmVertex;

public class NmVertexDbl extends ANmVertex
{
    public NmVertexDbl(
        Individual individual,
        HeuristicProblem heuristicProblem) 
    {
	    super(individual,
		    heuristicProblem);
    }

    @Override
    protected ANmVertex CreateNmVertex()
    {
        return new NmVertexDbl(
            m_individual.Clone(m_heuristicProblem),
            m_heuristicProblem);
    }

    @Override
    protected void SetChromosomeValue(int intIndex, double dblValue)
    {
        Individual individual = m_individual;
        if (individual.getIndividualList() != null &&
            individual.getIndividualList().size() > 0)
        {
            individual = individual.GetIndividual(
                m_heuristicProblem.getProblemName());
        }
        individual.SetChromosomeValueDbl(intIndex, dblValue);
    }

    @Override
    protected double[] GetChromosomeCopy(Individual individual)
    {
        if (individual.getIndividualList() != null &&
            individual.getIndividualList().size() > 0)
        {
            individual = individual.GetIndividual(
                m_heuristicProblem.getProblemName());
        }
        return individual.GetChromosomeCopyDbl();
    }
}
