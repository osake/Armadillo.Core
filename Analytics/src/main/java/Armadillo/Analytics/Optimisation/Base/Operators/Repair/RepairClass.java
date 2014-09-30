package Armadillo.Analytics.Optimisation.Base.Operators.Repair;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.HCException;

public class RepairClass extends ARepair
{
    private final List<IRepair> m_repairList;

    public HeuristicProblem HeuristicOptmizationProblem_()
    {
        return m_heuristicProblem;
    }

    public RepairClass(
        HeuristicProblem heuristicProblem)
    {
        super(heuristicProblem);
        m_heuristicProblem = heuristicProblem;
        m_repairList = new ArrayList<IRepair>();
    }

    @Override
    public void AddRepairOperator(IRepair repair)
    {
        m_repairList.add(repair);
    }

    @Override
    public boolean DoRepair(Individual individual)
    {
        if (m_repairList == null || m_repairList.size() == 0)
        {
            throw new HCException("Repair list is empty");
        }

        //
        // shuffle repair list
        //
        List<IRepair> repairList = new ArrayList<IRepair>(m_repairList);
        RngWrapper rng = HeuristicProblem.CreateRandomGenerator();
        rng.ShuffleList(repairList);

        boolean blnRepair = true;
        for (IRepair repairOperator : repairList)
        {
            if (!repairOperator.DoRepair(individual))
            {
                blnRepair = false;
            }
        }

        if (blnRepair &&
            m_heuristicProblem.DoCheckConstraints() &&
            !m_heuristicProblem.Constraints().CheckConstraints(individual))
        {
            //Debugger.Break();
            throw new HCException("Repair individual failed.");
        }

        return blnRepair;
    }
}
