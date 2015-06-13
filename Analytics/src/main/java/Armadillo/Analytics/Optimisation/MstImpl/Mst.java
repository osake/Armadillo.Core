package Armadillo.Analytics.Optimisation.MstImpl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Armadillo.Analytics.TextMining.MstDistanceObj;
import Armadillo.Core.HCException;
import Armadillo.Core.PrintToScreen;

public class Mst 
{
        public double GetDistance(int intI, int intJ)
        {
        	throw new HCException("Method not implemented");
        }
        
        public Map<Integer, MstEdge> HtEdge;

        public Map<Integer, List<Integer>> HtParent;

        public Map<String, Double> HtScores;

        /// <summary>
        /// delta1 verifies nodes which degree >1
        /// </summary>
        public double NodeDegreesThreshold;

        /// <summary>
        /// parameter delta2 reduces the branch length in the trees
        /// It also controls the weights in the root edges
        /// </summary>
        public double BranchLenghtThreshold;

        /// <summary>
        /// parameter delta3 controls the number of adjacent nodes in the root nodes
        /// The value set to this parameter is the same for all String
        /// metrics wich return score is in [0,1]
        /// </summary>
        public double AdjacentNodeThreshold;

        /// <summary>
        /// controls how much the threshold will be relaxed according branch size
        /// </summary>
        public double BranchSizeThreshold;

        /// <summary>
        /// Main similairty threshold
        /// </summary>
        public double EdgeThreshold;

        private Map<Integer, Double> m_htMaxAdjacentEdge;
        private Map<Integer, Object> m_htParentObject;

        public Mst() 
        {
            
            this(MstClusterConstants.EDGE_THRESHOLD,
	            MstClusterConstants.NODE_DEGREES_THRESHOLD,
	            MstClusterConstants.BRANCH_LENGHT_THRESHOLD,
	            MstClusterConstants.ADJACENT_NODE,
	            MstClusterConstants.BRANCH_SIZE_THRESHOLD);
        }

        public Mst(
            double edgeThreshold,
            double dblNodeDegreesThreshold,
            double dblBranchLenghtThreshold,
            double dblAdjacentNodeThreshold,
            double branchSizeThreshold)
        {
            EdgeThreshold = edgeThreshold;
            NodeDegreesThreshold = dblNodeDegreesThreshold;
            BranchLenghtThreshold = dblBranchLenghtThreshold;
            AdjacentNodeThreshold = dblAdjacentNodeThreshold;
            BranchSizeThreshold = branchSizeThreshold;
            HtEdge = new Hashtable<Integer, MstEdge>();
            HtParent = new Hashtable<Integer, List<Integer>>();
            m_htParentObject = new Hashtable<Integer, Object>();
            HtScores = new Hashtable<String, Double>();
            m_htMaxAdjacentEdge = new Hashtable<Integer, Double>();
        }

        public boolean CheckLoop(int x, int y)
        {
            MstEdge mstEdgeX = null;
            MstEdge mstEdgeY = null;
            if (HtEdge.containsKey(x))
            {
                mstEdgeX = HtEdge.get(x);
            }
            if (HtEdge.containsKey(y))
            {
                mstEdgeY = HtEdge.get(y);
            }
            // check for cycles
            if (mstEdgeX != null && mstEdgeY != null)
            {
                if (mstEdgeX.GetParent() == mstEdgeY.GetParent())
                {
                    return false;
                }
            }
            return true;
        }

        public boolean Link(
            MstDistanceObj actualScoreObject,
            Object rootObject)
        {
            return Link(
                actualScoreObject,
                rootObject,
                true);
        }

        public double GetScore(int i, int j)
        {
            //
            // get confidence score
            //
            String strScoreKey;
            if (i > j)
            {
                strScoreKey = j + "-" + i;
            }
            else
            {
                strScoreKey = i + "-" + j;
            }
            return HtScores.get(strScoreKey);
        }

        public boolean Link(
            MstDistanceObj actualScoreObject, 
            Object rootObject,
            boolean blnValidateConstraints)
        {
            // initialization data
            double actualScore = actualScoreObject.GetScore();
            int x = actualScoreObject.GetX();
            int y = actualScoreObject.GetY();
            MstEdge mstEdgeX = null;
            MstEdge mstEdgeY = null;

            if (x > y)
            {
                //Debugger.Break();
                throw new HCException("Error. MST index order not correct");
            }

            String position = x + "-" + y;


            if (HtEdge.containsKey(x))
            {
                mstEdgeX = HtEdge.get(x);
            }
            if (HtEdge.containsKey(y))
            {
                mstEdgeY = HtEdge.get(y);
            }
            // check for cycles
            if (mstEdgeX != null && mstEdgeY != null)
            {
                if (mstEdgeX.GetParent() == mstEdgeY.GetParent())
                {
                    return false;
                }
            }

            if (blnValidateConstraints)
            {
                if (!ValidateConstraints(
                         x,
                         y,
                         mstEdgeX,
                         mstEdgeY,
                         actualScore,
                         position))
                {
                    return false;
                }
            }
            else
            {
                //
                // add score to map
                //
                if (!HtScores.containsKey(position))
                {
                    HtScores.put(position, actualScoreObject.GetScore());
                }
            }

            // add the edge for the current position
            if (AddEdge(x, y, rootObject))
            {
                // keep track of the maximum adjacent edges
                if (!m_htMaxAdjacentEdge.containsKey(x))
                {
                    m_htMaxAdjacentEdge.put(x, actualScore);
                }
                if (!m_htMaxAdjacentEdge.containsKey(y))
                {
                    m_htMaxAdjacentEdge.put(y, actualScore);
                }
            }
            else
            {
                return false;
            }
            return true;
        }

        private boolean ValidateConstraints(
            int x, 
            int y, 
            MstEdge mstEdgeX, 
            MstEdge mstEdgeY, 
            double actualScore, 
            String position)
        {
            // Constraint 1: Set a loose threshold k_star
            if (actualScore < EdgeThreshold)
            {
                return false;
            }

            if (!HtScores.containsKey(position))
            {
                HtScores.put(position, actualScore);
            }

            // Constrain 2: Verify nodes which degree >1
            if (m_htMaxAdjacentEdge.containsKey(x))
            {
                double maxScore = m_htMaxAdjacentEdge.get(x);
                if (maxScore - actualScore > NodeDegreesThreshold)
                {
                    return false;
                }
            }

            if (m_htMaxAdjacentEdge.containsKey(y))
            {
                double maxScore = m_htMaxAdjacentEdge.get(y);
                if (maxScore - actualScore > NodeDegreesThreshold)
                {
                    return false;
                }
            }

            // Constraint 4: keep a low weight to incident edges to the root node
            if (mstEdgeX != null)
            {
                if (mstEdgeX.GetParent() == x)
                {
                    double maxScore = m_htMaxAdjacentEdge.get(x);
                    if (maxScore - actualScore > AdjacentNodeThreshold)
                    {
                        return false;
                    }
                }
            }
            if (mstEdgeY != null)
            {
                if (mstEdgeY.GetParent() == y)
                {
                    double maxScore = m_htMaxAdjacentEdge.get(y);
                    if (maxScore - actualScore > AdjacentNodeThreshold)
                    {
                        return false;
                    }
                }
            }

            // Constraint 3: Verify branches with more than two nodes
            // verify in node x
            if (mstEdgeX != null)
            {
                List<Integer> beforeList = GetBeforeNodes(x);
                int listSize = beforeList.size();
                if (listSize > 0)
                {
                    double sumScores = 0.0;
                    for (int actualNode : beforeList)
                    {
                        int xNew = Math.min(y, actualNode);
                        int yNew = Math.max(y, actualNode);
                        String positionNew = xNew + "-" + yNew;
                        if (HtScores.containsKey(positionNew))
                        {
                            sumScores += HtScores.get(positionNew);
                        }
                        else
                        {
                            double tmpScore = InvokeOnGetDistance(xNew, yNew);
                            HtScores.put(positionNew, tmpScore);
                            sumScores += tmpScore;
                        }
                    }
                    double averageScore = sumScores / listSize;
                    double actualThreshold;

                    if (listSize == 1)
                    {
                        actualThreshold = BranchLenghtThreshold;
                    }
                    else
                    {
                        actualThreshold = BranchLenghtThreshold - (listSize - 1.0)*BranchSizeThreshold;
                    }
                    if (averageScore < actualThreshold)
                    {
                        return false;
                    }
                }
            }

            // verify in node y
            if (mstEdgeY != null)
            {
                List<Integer> beforeList = GetBeforeNodes(y);
                int listSize = beforeList.size();
                if (listSize > 0)
                {
                    double sumScores = 0.0;
                    for (int actualNode : beforeList)
                    {
                        int intXNew = Math.min(x, actualNode);
                        int intYNew = Math.max(x, actualNode);
                        String strPositionNew = intXNew + "-" + intYNew;
                        if (HtScores.containsKey(strPositionNew))
                        {
                            sumScores += HtScores.get(strPositionNew);
                        }
                        else
                        {
                            double tmpScore = InvokeOnGetDistance(intXNew, intYNew);
                            sumScores += tmpScore;
                        }
                    }
                    double dblAverageScore = sumScores/listSize;
                    double dblActualThreshold;
                    if (listSize == 1)
                    {
                        dblActualThreshold = BranchLenghtThreshold;
                    }
                    else
                    {
                        dblActualThreshold = BranchLenghtThreshold - ((listSize) - 1.0)*BranchSizeThreshold;
                    }
                    if (dblAverageScore < dblActualThreshold)
                    {
                        //Console::WriteLine("averageScore2 "+averageScore);
                        return false;
                    }
                }
            }
            return true;
        }

        private List<Integer> GetBeforeNodes(int node)
        {
            //Console::WriteLine("node "+node);
            List<Integer> beforeList = new ArrayList<Integer>();
            MstEdge mStEdge = HtEdge.get(node);
            int before = mStEdge.GetBefore();
            //Console::WriteLine("before "+before);
            if (before > -1)
            {
                beforeList.add(before);
            }
            while (before > -1)
            {
                mStEdge = HtEdge.get(before);
                before = mStEdge.GetBefore();

                if (before > -1)
                {
                    beforeList.add(before);
                }
            }
            return beforeList;
        }


        private boolean AddEdge(int x, int y, Object rootObject)
        {
            if (!HtEdge.containsKey(x) && !HtEdge.containsKey(y))
            {
                int parent, beforeX = -1, beforeY = -1;
                if (x < y)
                {
                    parent = x;
                    beforeY = x;
                }
                else
                {
                    parent = y;
                    beforeX = y;
                }
                MSTParentObject mStParentObject = new MSTParentObject(parent);
                HtEdge.put(x, new MstEdge(beforeX, mStParentObject));
                HtEdge.put(y, new MstEdge(beforeY, mStParentObject));
                List<Integer> parentMembersList = new ArrayList<Integer>();
                parentMembersList.add(x);
                parentMembersList.add(y);
                HtParent.put(parent, parentMembersList);
                m_htParentObject.put(parent, rootObject);
                return true;
            }
            if (HtEdge.containsKey(x) && HtEdge.containsKey(y))
            {
                MstEdge mstEdgeX = HtEdge.get(x);
                MstEdge mstEdgeY = HtEdge.get(y);
                MSTParentObject parentObjectX = mstEdgeX.getParentObject();
                MSTParentObject parentObjectY = mstEdgeY.getParentObject();
                if (parentObjectX.GetParent() == parentObjectY.GetParent())
                {
                    // there is a cycle
                    return false;
                }
                //MSTParentObject parentObject;
                if (parentObjectX.GetParent() < parentObjectY.GetParent())
                {
                    // change the parents of all members of y
                    ChangeParent(y, x, parentObjectX);
                }
                else
                {
                    // change the parents of all members of x
                    ChangeParent(x, y, parentObjectY);
                }
                return true;
            }
            if (HtEdge.containsKey(x))
            {
                MstEdge actualMstEdge = HtEdge.get(x);
                HtEdge.put(y, new MstEdge(x, actualMstEdge.getParentObject()));
                // add y to parent list
                int currentParent = actualMstEdge.GetParent();
                List<Integer> parentMembersList = HtParent.get(currentParent);
                parentMembersList.add(y);
            }
            else
            {
                MstEdge actualMstEdge = HtEdge.get(y);
                HtEdge.put(x, new MstEdge(y, actualMstEdge.getParentObject()));
                // add x to parent list
                int currentParent = actualMstEdge.GetParent();
                List<Integer> parentMembersList = HtParent.get(currentParent);
                parentMembersList.add(x);
            }
            return true;
        }

        private void ChangeParent(
            int position,
            int beforeFinal,
            MSTParentObject newMstParentObject)
        {
            MstEdge mStEdge = HtEdge.get(position);
            // set before. Change the direction of the tree
            int before = mStEdge.GetBefore(),
                oldParent = mStEdge.GetParent();
            mStEdge.SetBefore(beforeFinal);
            beforeFinal = position;
            while (before > -1)
            {
                mStEdge = HtEdge.get(before);
                int beforeOld = before;
                before = mStEdge.GetBefore();
                mStEdge.SetBefore(beforeFinal);
                beforeFinal = beforeOld;
            }
            // change all the parents from the old tree
            int newParent = newMstParentObject.GetParent();
            List<Integer> oldMemberList = HtParent.get(oldParent);
            List<Integer> newMemberList = HtParent.get(newParent);
            for (int currentNode : oldMemberList)
            {
                mStEdge = HtEdge.get(currentNode);
                mStEdge.SetParentObject(newMstParentObject);
                newMemberList.add(currentNode);
            }
            HtParent.remove(oldParent);
            m_htParentObject.remove(oldParent);
        }

        @Override
        public String toString()
        {
            for (Entry<Integer, MstEdge> de : HtEdge.entrySet())
            {
                MstEdge currentMstEdge = de.getValue();
                int current = de.getKey();
                int before = currentMstEdge.GetBefore();
                int parent = currentMstEdge.GetParent();
                PrintToScreen.WriteLine(current + "-" + before + ", parent " + parent);
            }
            String out1;
            for (Entry<Integer, List<Integer>> de : HtParent.entrySet())
            {
                List<Integer> memberList = de.getValue();
                int current = de.getKey();
                out1 = "parent " + current + " : ";

                for (int currentMember : memberList)
                {
                    out1 += currentMember + ", ";
                }
                PrintToScreen.WriteLine(out1);
            }
            return "";
        }



        private double InvokeOnGetDistance(int intI, int intJ)
        {
             return GetDistance(intI, intJ);
        }
}
