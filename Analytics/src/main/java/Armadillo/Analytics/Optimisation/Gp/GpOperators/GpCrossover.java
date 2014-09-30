package Armadillo.Analytics.Optimisation.Gp.GpOperators;

import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.AGpNode;
import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.GpIndividualHelper;
import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.GpOperatorNode;
import Armadillo.Analytics.Optimisation.Base.Operators.Crossover.ICrossover;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public class GpCrossover implements ICrossover
{
    private final HeuristicProblem m_heuristicProblem;
    private final int m_intMaxTreeSize;

    public GpCrossover(
        HeuristicProblem heuristicProblem,
        int intMaxTreeSize)
    {
        m_heuristicProblem = heuristicProblem;
        m_intMaxTreeSize = intMaxTreeSize;
    }

    public Individual DoCrossover(
        RngWrapper rng,
        Individual[] individuals)
    {
        Individual ind1 = individuals[0].Clone(m_heuristicProblem);
        Individual ind2 = individuals[1].Clone(m_heuristicProblem);

        int i1 = rng.NextInt(0, ind1.Size() - 1) + 1;
        int i2 = rng.NextInt(0, ind2.Size() - 1) + 1;


        AGpNode tree1 = GpIndividualHelper.ReturnNodeNumber(i1, ind1.Root());
        AGpNode tree2 = GpIndividualHelper.ReturnNodeNumber(i2, ind2.Root());
        int sizeTree1 = GpIndividualHelper.CountNodes(tree1);
        int sizeTree2 = GpIndividualHelper.CountNodes(tree2);
        GpOperatorNode p1 = tree1.Parent;
        GpOperatorNode p2 = tree2.Parent;

        if (ind1.Size() + sizeTree2 - sizeTree1 <= m_intMaxTreeSize)
        {
            ind1.setSize(ind1.Size() + sizeTree2 - sizeTree1);
            tree2.Parent = p1;
            if (p1 != null)
            {
            	AGpNode[] children = p1.ChildrenArr();
                for (int i = 0; i < children.length; i++)
                {
                    if (children[i].equals(tree1))
                    {
                        children[i] = tree2;
                    }
                }
            }
            else
            {
                ind1.setRoot(tree2);
            }
            return ind1;
        }
        ind2.setSize(ind2.Size() + sizeTree1 - sizeTree2);
        tree1.Parent = p2;
        if (p2 != null)
        {
        	AGpNode[] children = p2.ChildrenArr();
            for (int i = 0; i < children.length; i++)
            {
                if (children[i].equals(tree2))
                {
                    children[i] = tree1;
                }
            }
        }
        else
        {
            ind2.setRoot(tree1);
        }
        return ind2;
    }
}
