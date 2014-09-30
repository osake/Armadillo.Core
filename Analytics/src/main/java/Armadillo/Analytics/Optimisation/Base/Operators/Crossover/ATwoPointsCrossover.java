package Armadillo.Analytics.Optimisation.Base.Operators.Crossover;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public abstract class ATwoPointsCrossover extends ACrossover
{
    public ATwoPointsCrossover(
        HeuristicProblem heuristicProblem)
            
    {
        	super(heuristicProblem);
    }

    @Override
    public Individual DoCrossover(
        RngWrapper rng,
        Individual[] individuals)
    {
    	Individual parent1 = individuals[0];
    	Individual parent2 = individuals[1];

        double[] c3 = null;
        if (m_heuristicProblem.VariableCount() > 0)
        {
            double[] c1 = GetChromosomeCopy(parent1); //chromosome parent 1
            double[] c2 = GetChromosomeCopy(parent2); //chromosome parent 2
            c3 = new double[m_heuristicProblem.VariableCount()]; //new chromosome  

            //Generate crossing points
            int p1 = (int) (m_heuristicProblem.VariableCount()*
                            rng.nextDouble()); //crossing point 1
            int p2 = (int) (m_heuristicProblem.VariableCount()*
                            rng.nextDouble()); //crossing point 2
            while (p1 == p2)
            {
                p2 = (int) (m_heuristicProblem.VariableCount()*rng.nextDouble());
            }
            if (p1 > p2)
            {
                int temp = p1;
                p1 = p2;
                p2 = temp;
            }

            // Copy everything between point1 and point2 from parent1, the rest from parent2        
            for (int i = 0; i < m_heuristicProblem.VariableCount(); i++)
            {
                if (i < p1)
                {
                    c3[i] = c2[i];
                }
                else if (i < p2)
                {
                    c3[i] = c1[i];
                }
                else
                {
                    c3[i] = c2[i];
                }
            }
        }

        // Create and return new individual using the new chromosome
        Individual individual =
            CreateIndividual(c3);

        return individual;
    }

    protected abstract double GetChromosomeValue(
        Individual individual,
        int intIndex);

    protected abstract void AddChromosomeValue(
        Individual individual,
        int intIndex,
        double dblWeight);

    protected abstract void RemoveChromosomeValue(
        Individual individual,
        int intIndex,
        double dblWeight);

    protected abstract double[] GetChromosomeCopy(
        Individual individual);

    protected abstract double GetMaxChromosomeValue(int intIndex);

    protected abstract Individual CreateIndividual(double[] dblChromosomeArr);

}
