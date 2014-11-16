package Armadillo.Analytics.Optimisation.Base.Operators.MultiObjective;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import Armadillo.Analytics.Optimisation.Base.ObjectiveFunctions.AEqualityConstrainObjFunc;
import Armadillo.Analytics.Optimisation.Base.ObjectiveFunctions.ARangeConstrainedObjFunc;
import Armadillo.Analytics.Optimisation.Base.ObjectiveFunctions.HeuristicMultiObjectiveFunction;
import Armadillo.Analytics.Optimisation.Base.ObjectiveFunctions.IHeuristicObjectiveFunction;
import Armadillo.Analytics.Optimisation.Base.Operators.IndividualClasses.Individual;
import Armadillo.Analytics.Optimisation.Base.Problem.HeuristicProblem;
import Armadillo.Analytics.TimeSeries.ListHelper;
import Armadillo.Core.DoubleHelper;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;

public class MultiObjectiveRanking
{
    private static final double M_DBL_KAPPA = 0.05; // recommended value
    private static final double M_DBL_Z = 3.5; // reference point
    private static final double OBJECTIVE_FACTOR = 0.9;
    private static final double RANGE_FACTOR = 0.1;
    private static final double VALID_RANK_PROPORTION = 0.3;
    private static final int VALID_COUNT = 3;

    private final HeuristicProblem m_heuristicProblem;
    private final boolean m_blnRepairRangeRank;
    private final List<IHeuristicObjectiveFunction> m_objectives;
    private final boolean m_blnRepairEquialityRank;

    public MultiObjectiveRanking(
        HeuristicProblem heuristicProblem)
    {
        m_heuristicProblem = heuristicProblem;
        m_objectives = ((HeuristicMultiObjectiveFunction)m_heuristicProblem.getObjectiveFunction()).ObjectiveFunctions();

        List<IHeuristicObjectiveFunction> constrainedObjectives = new ArrayList<IHeuristicObjectiveFunction>(); 
        for (IHeuristicObjectiveFunction heuristicObjectiveFunction : m_objectives)
        {
        	if(heuristicObjectiveFunction instanceof ARangeConstrainedObjFunc)
        	{
        		constrainedObjectives.add(heuristicObjectiveFunction);
        	}
		}
        
//        List<IHeuristicObjectiveFunction> constrainedObjectives = from n in m_objectives
//                                    where (n as ARangeConstrainedObjFunc) != null
//                                    select n;

        m_blnRepairRangeRank = constrainedObjectives.size() > 0;

        constrainedObjectives = new ArrayList<IHeuristicObjectiveFunction>();
        for (IHeuristicObjectiveFunction heuristicObjectiveFunction : m_objectives)
        {
        	if(heuristicObjectiveFunction instanceof AEqualityConstrainObjFunc)
        	{
        		constrainedObjectives.add(heuristicObjectiveFunction);
        	}
		}
        
//        constrainedObjectives = from n in m_objectives
//                                where (n as AEqualityConstrainObjFunc) != null
//                                select n;

        m_blnRepairEquialityRank = constrainedObjectives.size() > 0;
    }


    public void Rank()
    {
    	try
    	{
	        int intObjectiveCount =
	            m_heuristicProblem.ObjectiveCount();
	        //
	        // load temporary population list
	        //
	        Individual[] populationArr =
	            m_heuristicProblem.getPopulation().GetPopulationAndNewCandidates();
	
	        double[][] objs = new double[populationArr.length][];
	        for(int i = 0; i < populationArr.length; i++)
	        {
	        	Individual ind = populationArr[i];
	        	objs[i] = ind.GetFitnessArrCopy();
	        }
	        int[] ranks = GetRanks(intObjectiveCount, objs);
	
	        //
	        // Step 4. Repair ranks based on constrained objectives
	        //
	        ranks = RepairRangeRanks(intObjectiveCount, populationArr, ranks);
	        ranks = RepairEqualityRanks(intObjectiveCount, populationArr, ranks);
	
	        //
	        // Step 4. Assign rankings to individuals
	        //
	        m_heuristicProblem.getPopulation().LoadPopulationMultiObjective(
	            populationArr,
	            ranks);
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    }

    private int[] RepairEqualityRanks(
        int intObjectives,
        Individual[] populationArr,
        int[] ranks)
    {
        if (!m_blnRepairEquialityRank)
        {
            return ranks;
        }

        RankObj[] rankObjs = new RankObj[populationArr.length];
        boolean blnValidRank = false;
        for (int i = 0; i < populationArr.length; i++)
        {
            Individual individual = populationArr[i];
            boolean blnIsValid = true;
            double[] objs = new double[intObjectives];
            for (int j = 0; j < intObjectives; j++)
            {
                objs[j] = individual.GetFitnesValue(j);
                
                		
                if (m_objectives.get(j) instanceof AEqualityConstrainObjFunc)
                {
                	AEqualityConstrainObjFunc constrObj = (AEqualityConstrainObjFunc)m_objectives.get(j); 
                    if (!constrObj.CheckConstraint(individual))
                    {
                        blnIsValid = false;
                        break;
                    }
                }
            }
            rankObjs[i] = new RankObj();
            
            rankObjs[i].Index = i;
            rankObjs[i].IsValid = blnIsValid;
            rankObjs[i].Rank = ranks[i];
            rankObjs[i].Objs = objs;
            
            if (blnIsValid)
            {
                blnValidRank = true;
            }
        }
        if (blnValidRank)
        {
            //
            // rank valid
            //
        	List<RankObj> validRanked = new ArrayList<RankObj>();
        	for (int i = 0; i < rankObjs.length; i++) 
        	{
        		if(rankObjs[i].IsValid)
        		{
        			validRanked.add(rankObjs[i]);
        		}
			}
        	Collections.sort(validRanked, 
        			new Comparator<RankObj>() 
        			{

						public int compare(RankObj arg0, RankObj arg1) 
						{
							return DoubleHelper.compare(arg0.Rank, arg1.Rank);
						}

						@Override
						public Comparator<RankObj> reversed() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public Comparator<RankObj> thenComparing(
								Comparator<? super RankObj> other) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public <U> Comparator<RankObj> thenComparing(
								Function<? super RankObj, ? extends U> keyExtractor,
								Comparator<? super U> keyComparator) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public <U extends Comparable<? super U>> Comparator<RankObj> thenComparing(
								Function<? super RankObj, ? extends U> keyExtractor) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public Comparator<RankObj> thenComparingInt(
								ToIntFunction<? super RankObj> keyExtractor) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public Comparator<RankObj> thenComparingLong(
								ToLongFunction<? super RankObj> keyExtractor) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public Comparator<RankObj> thenComparingDouble(
								ToDoubleFunction<? super RankObj> keyExtractor) {
							// TODO Auto-generated method stub
							return null;
						}
        			});
            int i = 0;
            for (; i < validRanked.size(); i++)
            {
                RankObj rankObj = validRanked.get(i);
                ranks[rankObj.Index] = i + 1;
            }

            //
            // rank non-valid
            //
        	List<RankObj> nonValidRanked = new ArrayList<RankObj>();
        	for (int j = 0; j < rankObjs.length; j++) 
        	{
        		if(!rankObjs[j].IsValid)
        		{
        			nonValidRanked.add(rankObjs[j]);
        		}
			}
        	Collections.sort(nonValidRanked, 
        			new Comparator<RankObj>() 
        			{

						public int compare(RankObj arg0, RankObj arg1) 
						{
							return DoubleHelper.compare(arg0.Rank, arg1.Rank);
						}

						@Override
						public Comparator<RankObj> reversed() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public Comparator<RankObj> thenComparing(
								Comparator<? super RankObj> other) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public <U> Comparator<RankObj> thenComparing(
								Function<? super RankObj, ? extends U> keyExtractor,
								Comparator<? super U> keyComparator) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public <U extends Comparable<? super U>> Comparator<RankObj> thenComparing(
								Function<? super RankObj, ? extends U> keyExtractor) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public Comparator<RankObj> thenComparingInt(
								ToIntFunction<? super RankObj> keyExtractor) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public Comparator<RankObj> thenComparingLong(
								ToLongFunction<? super RankObj> keyExtractor) {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						public Comparator<RankObj> thenComparingDouble(
								ToDoubleFunction<? super RankObj> keyExtractor) {
							// TODO Auto-generated method stub
							return null;
						}
        			});
        	
            for (int j = 0; j < nonValidRanked.size(); j++)
            {
                ranks[nonValidRanked.get(j).Index] = i + 1;
                i++;
            }

            //
            // check proportion of ranks
            //
            CheckValidRankProportion(intObjectives, rankObjs, validRanked);
        }

        return ranks;
    }

    private void CheckValidRankProportion(
        int intObjectives,
        RankObj[] rankObjs,
        List<RankObj> validRanked)
    {
        double dblValidRankProportion = (validRanked.size() * 1.0) / rankObjs.length;
        if (dblValidRankProportion > VALID_RANK_PROPORTION)
        {
            //
            // we can reduce the search space
            //
        	HeuristicMultiObjectiveFunction objFunction = (HeuristicMultiObjectiveFunction)m_heuristicProblem.getObjectiveFunction();
            for (int j = 0; j < intObjectives; j++)
            {
                IHeuristicObjectiveFunction heuristicObjectiveFunction = objFunction.ObjectiveFunctions().get(j);
                AEqualityConstrainObjFunc currEqObj;
                if (heuristicObjectiveFunction instanceof AEqualityConstrainObjFunc)
                {
                	currEqObj = (AEqualityConstrainObjFunc)heuristicObjectiveFunction; 
                    if (currEqObj.LowValue == currEqObj.HighValue)
                    {
                        continue;
                    }
                    //
                    // get min and max obj values
                    //
                    List<Double> allObjValues = new ArrayList<Double>();
                    for (int i = 0; i < rankObjs.length; i++) 
                    {
                    	allObjValues.add(rankObjs[i].Objs[j]);
					}
                    
                    double dblMaxObjectiveAll = ListHelper.max(allObjValues);
                    double dblMinObjectiveAll = ListHelper.min(allObjValues);
                    
                    List<Double> objValues = new ArrayList<Double>();
                    for (int i = 0; i < validRanked.size(); i++) 
                    {
                    	objValues.add(validRanked.get(i).Objs[j]);
					}
                    
                    double dblMaxObjective = ListHelper.max(objValues);
                    double dblMinObjective = ListHelper.min(objValues);
                    double dblMinObj = Math.max(
                        currEqObj.LowValue,
                        dblMinObjective);
                    double dblMaxObj = Math.min(
                        currEqObj.HighValue,
                        dblMaxObjective);
                    double dblHalfRange = (dblMaxObj - dblMinObj) / 2.0;
                    double dblObjRange = OBJECTIVE_FACTOR * dblHalfRange;
                    double dblLowValue = currEqObj.TargetValue - dblObjRange;
                    double dblHighValue = currEqObj.TargetValue + dblObjRange;

                    int intValidCount = 0;
                    for (int i = 0; i < objValues.size(); i++) 
                    {
                    	double dblObjVal = objValues.get(i);
                    	if(dblObjVal >= dblLowValue &&
                    			dblObjVal <= dblHighValue)
                    	{
                    		intValidCount++;
                    	}
					}
                    
                    boolean blnUpdateValues = false;
                    if (intValidCount > VALID_COUNT)
                    {
                        //
                        // do not set new values if there are no valid
                        // solutions for the new area
                        //
                        currEqObj.LowValue = Math.max(dblLowValue,
                                                      currEqObj.LowValue);
                        currEqObj.HighValue = Math.min(dblHighValue,
                                                       currEqObj.HighValue);
                        blnUpdateValues = true;
                    }
                    else
                    {
                        //
                        // check if we are moving in the right direction
                        //
                        double dblObjectiveRangeFactor = (dblMaxObjective - dblMinObjective) * RANGE_FACTOR;
                        if (dblLowValue > currEqObj.TargetValue)
                        {
                            if (currEqObj.HighValue > dblMaxObjectiveAll)
                            {
                                currEqObj.HighValue = dblMaxObjectiveAll;
                            }
                            currEqObj.HighValue = currEqObj.HighValue - dblObjectiveRangeFactor;
                            blnUpdateValues = true;
                        }
                        else if (dblHighValue < currEqObj.TargetValue)
                        {
                            if (currEqObj.LowValue < dblMinObjectiveAll)
                            {
                                currEqObj.LowValue = dblMinObjectiveAll;
                            }
                            currEqObj.LowValue = currEqObj.LowValue - dblObjectiveRangeFactor;
                            blnUpdateValues = true;
                        }
                    }

                    if(blnUpdateValues)
                    {
                        m_heuristicProblem.getSolver().OptiGuiHelper.PublishLog(
                            "EqualityConstraint [" +
                            currEqObj.ObjectiveName + "], low["+
                            currEqObj.LowValue + "], high["+
                            currEqObj.HighValue + "], target[" +
                            currEqObj.TargetValue + "]");
                    }

                    if (currEqObj.LowValue > currEqObj.HighValue ||
                        currEqObj.LowValue == currEqObj.HighValue)
                    {
                        throw new HCException("Invalid min/max values");
                    }
                }
            }
        }
    }

    private int[] RepairRangeRanks(
        int intObjectives,
        Individual[] populationArr,
        int[] ranks)
    {
    	try
    	{
	        if (!m_blnRepairRangeRank)
	        {
	            return ranks;
	        }
	
	        RankObj[] rankObjs = new RankObj[populationArr.length];
	        boolean blnValidRank = false;
	        for (int i = 0; i < populationArr.length; i++)
	        {
	            Individual individual = populationArr[i];
	            boolean blnIsValid = true;
	            for (int j = 0; j < intObjectives; j++)
	            {
	                if (m_objectives.get(j) instanceof ARangeConstrainedObjFunc)
	                {
	                	ARangeConstrainedObjFunc constrObj = (ARangeConstrainedObjFunc)m_objectives.get(j);
	                    if (!constrObj.CheckConstraint(individual))
	                    {
	                        blnIsValid = false;
	                        break;
	                    }
	                }
	            }
	            RankObj rankObj = new RankObj();
	            
	            rankObjs[i] = rankObj; 
	            
	            rankObj.Index = i;
	            rankObj.IsValid = blnIsValid;
	            rankObj.Rank = ranks[i];
	            
	            if (blnIsValid)
	            {
	                blnValidRank = true;
	            }
	        }
	        if (blnValidRank)
	        {
	            //
	            // rank valid
	            //
	        	List<RankObj> ranked = new ArrayList<RankObj>();
	        	List<RankObj> nonRanked = new ArrayList<RankObj>();
	        	for (int i = 0; i < rankObjs.length; i++) 
	        	{
	        		RankObj currRankObj = rankObjs[i];
	        		if(currRankObj.IsValid)
	        		{
	        			ranked.add(currRankObj);
	        		}
	        		else
	        		{
	        			nonRanked.add(currRankObj);
	        		}
				}
	        	Comparator<RankObj> rankComparator = new Comparator<RankObj>() 
				{
	
					public int compare(RankObj arg0, RankObj arg1) 
					{
						return DoubleHelper.compare(arg0.Rank, arg1.Rank);
					}

					@Override
					public Comparator<RankObj> reversed() {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public Comparator<RankObj> thenComparing(
							Comparator<? super RankObj> other) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public <U> Comparator<RankObj> thenComparing(
							Function<? super RankObj, ? extends U> keyExtractor,
							Comparator<? super U> keyComparator) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public <U extends Comparable<? super U>> Comparator<RankObj> thenComparing(
							Function<? super RankObj, ? extends U> keyExtractor) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public Comparator<RankObj> thenComparingInt(
							ToIntFunction<? super RankObj> keyExtractor) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public Comparator<RankObj> thenComparingLong(
							ToLongFunction<? super RankObj> keyExtractor) {
						// TODO Auto-generated method stub
						return null;
					}

					@Override
					public Comparator<RankObj> thenComparingDouble(
							ToDoubleFunction<? super RankObj> keyExtractor) {
						// TODO Auto-generated method stub
						return null;
					}
				};
	        	Collections.sort(ranked, rankComparator);
	        	Collections.sort(nonRanked, rankComparator);
	
	            int i = 0;
	            for (; i < ranked.size(); i++)
	            {
	                RankObj rankObj = ranked.get(i);
	                ranks[rankObj.Index] = i + 1;
	            }
	
	            //
	            // rank non-valid
	            //
	            for (int j = 0; j < nonRanked.size(); j++)
	            {
	                ranks[nonRanked.get(j).Index] = i + 1;
	                i++;
	            }
	        }
	        return ranks;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }

    public static int[] GetRanks(int nObj, double[][] objs)
    {
    	try
    	{
	        int intPopSize = objs.length;
	        //
	        // Step 1. Calculate epsilon values for each pair of individuals
	        //
	        double[] m_dblC = new double[1];
	        double[][] indicatorArr = CalculateDominateIndicatorValues(objs, nObj, m_dblC);
	        //
	        // Step 2. Add epsilon values for each individual
	        //
	        double[] epsilonArr = new double[intPopSize];
	        for (int i = 0; i < intPopSize; i++)
	        {
	            for (int j = 0; j < intPopSize; j++)
	            {
	                if (i == j)
	                {
	                    continue;
	                }
	                //
	                // The exponential amplifies influence of dominating individuals
	                //
	                epsilonArr[i] -= Math.exp(-indicatorArr[i][j] / M_DBL_KAPPA * m_dblC[0]);
	            }
	        }
	        //
	        // Step 3. Calculate rankings
	        //
	        int[] ranks = new int[intPopSize];
	        for (int i = 0; i < intPopSize; i++)
	        {
	            int worst = Min(epsilonArr);
	            ranks[worst] = intPopSize - i;
	
	            // assign very large value to worst so that 
	            // is no longer considered
	            epsilonArr[worst] = Double.MAX_VALUE;
	
	            // update values of individuals in list
	            for (int j = 0; j < intPopSize; j++)
	            {
	                // ignore worst
	                if (epsilonArr[j] == Double.MAX_VALUE)
	                {
	                    continue;
	                }
	
	                epsilonArr[j] += Math.exp(-indicatorArr[j][worst] / M_DBL_KAPPA * m_dblC[0]);
	            }
	        }
	        return ranks;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }

    /// <summary>
    ///   Returns the index of the minimum value in array
    /// </summary>
    /// <param name = "array"></param>
    private static int Min(double[] array)
    {
    	try
    	{
	        int intMin = 0;
	        for (int i = 1; i < array.length; i++)
	        {
	            intMin = array[i] < array[intMin] ? i : intMin;
	        }
	        return intMin;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return 0;
    }

    /// <summary>
    ///   calculates indicator values for each pair of individuals
    /// </summary>
    private static double[][] CalculateDominateIndicatorValues(
        double[][] objs,
        int nObj,
        double[] dblC)
    {
    	try
    	{
	    	double[][] normalisedObj = NormaliseObjectives(objs, nObj);
	        dblC[0] = 0.01; // to prevent a value of 0, if all individuals are the same
	        int N = normalisedObj.length;
	        double[][] E = new double[N][N];
	        for (int i = 0; i < N; i++)
	        {
	            for (int j = 0; j < N; j++)
	            {
	                if (i == j)
	                {
	                    continue;
	                }
	                E[i][ j] = HVolume(normalisedObj[j], normalisedObj[i], nObj);
	                dblC[0] = Math.abs(E[i][j]) > dblC[0] ? Math.abs(E[i][j]) : dblC[0];
	            }
	        }
	        return E;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }

    /// <summary>
    ///   Calculates the size of the region that is dominated by b and not
    ///   dominated by a. Let c be the region dominated by a and b, i.e. c is such
    ///   that c[i] = Max{a[i], b[i]}. The area dominated by a and b is, H(a,b) =
    ///   HV(a) + HV(b) - HV(c). Therefore the area dominated by b, but not a is
    ///   HV(a,b)- HV(a) = HV(b) - HV(c).
    /// </summary>
    /// <param name = "a"></param>
    /// <param name = "b"></param>
    /// <param name = "nObj"></param>
    private static double HVolume(double[] a, double[] b, int nObj)
    {
        //Step 1. determine vector c
        double[] c = new double[nObj];
        for (int i = 0; i < nObj; i++)
        {
            c[i] = b[i] > a[i] ? b[i] : a[i];
        }

        //Step 2. calculate hypervolume of b and c
        double HVb = 1, HVc = 1;
        for (int i = 0; i < nObj; i++)
        {
            HVb *= (M_DBL_Z - b[i]);
            HVc *= (M_DBL_Z - c[i]);
        }

        //Step 3. calculate and return final value
        return HVb - HVc;
    }

    /// <summary>
    ///   Normalises the individual function values to the [0,1] range
    /// </summary>
    /// <param name = "pop"></param>
    /// <param name = "nObj"></param>
    private static double[][] NormaliseObjectives(
    		double[][] objs, 
    		int nObj)
    {
    	try
    	{
	        int popLength = objs.length;
	        double[] max = GetNadir(objs, nObj);
	        double[] min = GetUtopia(objs, nObj);
	        double[][] normalisedObj = new double[popLength][];
	
	        for (int j = 0; j < popLength; j++)
	        {
	            //F = pop[j].FitnessArr;
	            normalisedObj[j] = new double[nObj];
	            for (int i = 0; i < nObj; i++)
	            {
	                normalisedObj[j][i] = (objs[j][i] - min[i]) /
	                                    (max[i] - min[i] + 1.0E-100);
	            }
	        }
	        return normalisedObj;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }

    /// <summary>
    ///   Returns nadir point, which is the point X such that
    ///   x_i is the maximum of all the x_i values in all solutions
    ///   in the population
    /// </summary>
    /// <param name = "pop"></param>
    /// <param name = "nObj"></param>
    private static double[] GetNadir(
    		double[][] objs, 
    		int nObj)
    {
    	try
    	{
	        double[] nadir = objs[0].clone();
	        for (int j = 1; j < objs.length; j++)
	        {
	            for (int i = 0; i < nObj; i++)
	            {
	                if (objs[j][i] > nadir[i])
	                {
	                    nadir[i] = objs[j][i];
	                }
	            }
	        }
	        return nadir;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }

    /// <summary>
    ///   Returns nadir point, which is the point X such that
    ///   x_i is the minimum of all the x_i values in all solutions
    ///   in the population
    /// </summary>
    /// <param name = "pop"></param>
    /// <param name = "nObj"></param>
    private static double[] GetUtopia(
    		double[][] objs, 
    		int nObj)
    {
    	try
    	{
	        double[] utopia = objs[0].clone();
	        for (int j = 1; j < objs.length; j++)
	        {
	            for (int i = 0; i < nObj; i++)
	            {
	                if (objs[j][i] < utopia[i])
	                {
	                    utopia[i] = objs[j][i];
	                }
	            }
	        }
	        return utopia;
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return null;
    }
}

