package Armadillo.Analytics.Optimisation.Gp.GpOperators;

import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.AGpNode;
import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.GpIndividualHelper;
import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.GpOperatorNode;
import Armadillo.Analytics.Optimisation.Base.DataStructures.Gp.GpOperatorsContainer;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Operators.Mutation.IMutation;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public class GpMutation implements IMutation
{
    private final GpOperatorsContainer m_gpOperatorsContainer;
    //private final HeuristicProblem m_heuristicProblem;
    private final int m_intDeph;

    public GpMutation(
        int intDeph,
        GpOperatorsContainer gpOperatorsContainer,
        HeuristicProblem heuristicProblem)
    {
        m_intDeph = intDeph;
        m_gpOperatorsContainer = gpOperatorsContainer;
        //m_heuristicProblem = heuristicProblem;
    }

    public Individual DoMutation(
        Individual individual)
    {
        RngWrapper rng = HeuristicProblem.CreateRandomGenerator();

        Individual gpIndividual = individual;
        Mutate(
            rng,
            m_intDeph,
            gpIndividual);
        return gpIndividual;
    }

    private void Mutate(
        RngWrapper random,
        int intMaxDepth,
        Individual gpIndividual)
    {
        int nodeNumber = random.NextInt(0,
                                        gpIndividual.Size() - 1) + 1;
        AGpNode tree1 = GpIndividualHelper.ReturnNodeNumber(
            nodeNumber,
            gpIndividual.Root());
        GpOperatorNode parent = tree1.Parent;

        //
        // create random tree
        //
        AGpNode tree2 =
            m_gpOperatorsContainer.GpOperatorNodeFactory.BuildOperator(
                parent,
                intMaxDepth,
                gpIndividual.Depth(),
                random);

        if (parent != null)
        {
        	AGpNode[] children = parent.ChildrenArr();
            for (int i = 0; i < children.length; i++)
            {
                if (children[i].equals(tree1))
                {
                    children[i] = tree2;
                    break;
                }
            }
        }
        else
        {
            gpIndividual.setRoot(tree2);
        }
        gpIndividual.setSize(gpIndividual.Size() - GpIndividualHelper.CountNodes(tree1) +
                            GpIndividualHelper.CountNodes(tree2));
    }

}
