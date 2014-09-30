package Armadillo.Analytics.Optimisation.Continuous.Operators.LocalSearch.NmLocalSearch;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.LocalSearch.ALocalSearchNm;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.NmSolver;

public class LocalSearchNmDbl extends ALocalSearchNm
{
    public LocalSearchNmDbl(HeuristicProblem heuristicProblem)
    {
    	super(heuristicProblem);
        m_nmPopulationGenerator = new NmPopulationGeneratorDbl(heuristicProblem);
    }

    @Override
    protected NmSolver LoadNmSolver()
    {
        return new NmSolver(
            m_heuristicProblem,
            m_nmPopulationGenerator);
    }

    @Override
    protected double GetChromosomeValue(
        Individual individual,
        int intIndex)
    {
        return individual.GetChromosomeValueDbl(intIndex);
    }

    @Override
    protected void AddChromosomeValue(
        Individual individual,
        int intIndex,
        double dblWeight)
    {
        individual.AddChromosomeValueDbl(
            intIndex,
            dblWeight);
    }

    @Override
    protected void RemoveChromosomeValue(
        Individual individual,
        int intIndex,
        double dblWeight)
    {
        individual.RemoveChromosomeValueDbl(
            intIndex,
            dblWeight);
    }

    @Override
    protected double[] GetChromosomeCopy(
        Individual individual)
    {
        return individual.GetChromosomeCopyDbl();
    }

    @Override
    protected double GetMaxChromosomeValue(int intIndex)
    {
        return 1.0;
    }

    @Override
    protected Individual BuildIndividual(
        double[] dblChromosomeArr,
        double dblFitness)
    {
        return new Individual(
            dblChromosomeArr,
            dblFitness,
            m_heuristicProblem);
    }
}
