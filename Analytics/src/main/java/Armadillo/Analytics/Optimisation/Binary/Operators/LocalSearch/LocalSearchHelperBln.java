package Armadillo.Analytics.Optimisation.Binary.Operators.LocalSearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Armadillo.Analytics.Optimisation.Base.DataStructures.VariableContribution;
import Armadillo.Analytics.Optimisation.Base.DataStructures.VariableContributionComparator;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public class LocalSearchHelperBln
{
    public static void GetRankLists(
        Individual individual,
        RngWrapper rng,
        int intChromosomeLength,
        boolean blnGoForward,
        HeuristicProblem heuristicProblem,
        List<Integer> indexList,
        List<VariableContribution> selectedRankedList)
    {

    	ArrayList<Integer> oneList = new ArrayList<Integer>(intChromosomeLength + 1);
    	ArrayList<Integer> zeroList = new ArrayList<Integer>(intChromosomeLength + 1);
        for (int i = 0; i < intChromosomeLength; i++)
        {
            if (individual.GetChromosomeValueBln(i))
            {
                oneList.add(i);
            }
            else
            {
                zeroList.add(i);
            }
        }

        //
        // rank one list
        //
        ArrayList<VariableContribution> oneListRanked =
            new ArrayList<VariableContribution>();
        for (int i = 0; i < oneList.size(); i++)
        {
            int intCurrentIndex = oneList.get(i);
            oneListRanked.add(
                new VariableContribution(
                    intCurrentIndex,
                    (heuristicProblem.getReproduction() == null
                         ? 1.0
                         : heuristicProblem.getGuidedConvergence().GetGcProb(
                             intCurrentIndex))*
                    rng.nextDouble()));
        }
        Collections.sort(oneListRanked, new VariableContributionComparator());


        //
        // rank zero list
        //
        ArrayList<VariableContribution> zeroListRanked =
            new ArrayList<VariableContribution>();
        for (int i = 0; i < zeroList.size(); i++)
        {
            int intCurrentIndex = zeroList.get(i);
            zeroListRanked.add(
                new VariableContribution(
                    intCurrentIndex,
                    (
                        heuristicProblem.getReproduction() == null
                            ? 1.0
                            : heuristicProblem.getGuidedConvergence().GetGcProb(
                                intCurrentIndex))*
                    rng.nextDouble()));
        }
        Collections.sort(zeroListRanked, new VariableContributionComparator());

        //
        // reverse the list in order to 
        // allow the most likely zeros to be converted into ones
        //
        selectedRankedList.clear();
        List<VariableContribution> nonRankedList = null;

        if (blnGoForward)
        {
            Collections.reverse(zeroListRanked);
            selectedRankedList.addAll(zeroListRanked);
            nonRankedList = oneListRanked;
        }
        else
        {
        	Collections.reverse(oneListRanked);
            selectedRankedList.addAll(oneListRanked);
            nonRankedList = zeroListRanked;
        }

        //
        // load index list
        //
        for (VariableContribution variableContribution :nonRankedList)
        {
            indexList.add(variableContribution.Index);
        }
    }
}
