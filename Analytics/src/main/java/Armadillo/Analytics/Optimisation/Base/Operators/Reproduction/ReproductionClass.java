package Armadillo.Analytics.Optimisation.Base.Operators.Reproduction;

import java.util.List;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.HCException;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;

public class ReproductionClass extends AReproduction
{
    /// <summary>
    ///   A list containing all the reproduction operators
    /// </summary>
    protected List<IReproduction> m_reproductionList;

    protected ReproductionClass(
        HeuristicProblem heuristicProblem)
    {
        this(heuristicProblem,
                null);
    }

    public ReproductionClass(
        HeuristicProblem heuristicProblem,
        List<IReproduction> reproductionList)
    {
        super(heuristicProblem);
        m_reproductionList = reproductionList;
        ValidateReproductionList();
    }

    @Override
    public Individual DoReproduction()
    {
        //
        // turn the wheel, select a reproduction 
        // operator and reproduce
        //
        RngWrapper rng =
            HeuristicProblem.CreateRandomGenerator();
        double dblRandom = rng.nextDouble();
        double dblTotalProb = 0;
        for (IReproduction reproduction : m_reproductionList)
        {
            dblTotalProb += reproduction.ReproductionProb();
            if (dblRandom <= dblTotalProb)
            {
                Individual newIndividual = reproduction.DoReproduction();
                if (newIndividual.isIsEvaluated())
                {
                    //Debugger.Break();
                    throw new HCException("Individual already evaluated.");
                }

                return newIndividual;
            }
        }
        throw new HCException("Error. Reproduction not selected");
    }

    @Override
    public void ClusterInstance(Individual individual)
    {
    }

    protected void ValidateReproductionList()
    {
        if (m_reproductionList == null)
        {
            return;
        }

        double dblTotalProb = 0;
        for (IReproduction reproduction : m_reproductionList)
        {
            dblTotalProb += reproduction.ReproductionProb();
        }
        if (Math.abs(dblTotalProb - 1.0) > 1.0e-4)
        {
            throw new HCException("Error. Probability is not = 1");
        }
    }
}
