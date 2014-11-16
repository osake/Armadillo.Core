package Armadillo.Analytics.Stat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import org.joda.time.DateTime;

import Armadillo.Analytics.TimeSeries.IForecasterWrapper;
import Armadillo.Analytics.TimeSeries.ListHelper;
import Armadillo.Analytics.TimeSeries.TimeSeriesHelper;
import Armadillo.Core.Console;
import Armadillo.Core.DateHelper;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;

public class VariableSelector 
{
    public static List<Integer> SelectBestVariablesLocal(
            List<double[]> xVars0,
            List<Double> yVars0,
            int intVarsToSelect)
    {
    	try
    	{
	    	SortedMap<DateTime, double[]> xVarsMap = new TreeMap<DateTime, double[]>();
	    	SortedMap<DateTime, Double> yVarsMap = new TreeMap<DateTime, Double>();
	    	DateTime currDate = DateHelper.MIN_DATE_JODA;
	    	for (int i = 0; i < xVars0.size(); i++) 
	    	{
				xVarsMap.put(currDate, xVars0.get(i));
				yVarsMap.put(currDate, yVars0.get(i));
				currDate = currDate.plusDays(1);
			}
	    	return SelectBestVariablesLocal(
	    			xVarsMap, 
	    			yVarsMap, 
	    			intVarsToSelect);
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return new ArrayList<Integer>();
    }
	
    public static List<Integer> SelectBestVariablesLocal(
            SortedMap<DateTime, double[]> xVars0,
            SortedMap<DateTime, Double> yVars0,
            int intVarsToSelect)
    {
    	try
    	{
	    	int intNumVars = xVars0.get(
	    			xVars0.firstKey()).length;
	    	List<String> featuresNames = new ArrayList<String>();
	    	for (int i = 0; i < intNumVars; i++) 
	    	{
	    		featuresNames.add("var_" + i);
			}
	    	
	    	return SelectBestVariablesLocal(
	                xVars0,
	                yVars0,
	                intVarsToSelect,
	                featuresNames,
	                -1,
	                false,
	                0,
	                null,
	                true,
	                false);
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    	}
    	return new ArrayList<Integer>();
    }
	
    public static List<Integer> SelectBestVariablesLocal(
            SortedMap<DateTime, double[]> xVars0,
            SortedMap<DateTime, Double> yVars0,
            int intVarsToSelect,
            List<String> featuresNames,
            int intLowerBoundVariable,
            boolean blnUseReturns,
            int intForecast,
            List<Integer> ignoreList,
            boolean blnVerbose,
            boolean blnUseNnet)
        {
            try
            {
                //
                // validate
                //
                if (xVars0.size() != yVars0.size())
                {
                    throw new HCException("Invalid vector size");
                }

                if (xVars0.get(xVars0.firstKey()).length != featuresNames.size())
                {
                    throw new HCException("Invalid feature names");
                }

                boolean blnCheckIgnoreList = ignoreList != null && ignoreList.size() > 0;
                		
                SortedMap<DateTime, Double> yVarsOri0 = 
                		TimeSeriesHelper.CloneMap(yVars0);
                double[] dblDelta = new double[1];
                double[] dblMinVal = new double[1];
                if (blnUseReturns)
                {
                    yVars0 = TimeSeriesHelper.GetLogReturns(yVars0, intForecast);
                    yVars0 = TimeSeriesHelper.NormalizeFeatures(yVars0, dblDelta, dblMinVal);
                    List<DateTime> dateSet = new ArrayList<DateTime>(yVars0.keySet());
                    xVars0 = TimeSeriesHelper.FilterMapByKeys(xVars0, dateSet);
                    yVars0 = TimeSeriesHelper.FilterMapByKeys(yVars0, dateSet);
                    yVarsOri0 = TimeSeriesHelper.FilterMapByKeys(yVarsOri0, dateSet);
                }

                if(xVars0 == null || xVars0.size() == 0 ||
                   yVars0 == null || yVars0.size() == 0)
                {
                    return new ArrayList<Integer>();
                }

                for(DateTime dateTime : xVars0.keySet())
                {
                	if(!yVars0.containsKey(dateTime))
                	{
                        throw new HCException("Dates do not match");
                	}
                }

                int intVars = xVars0.get(xVars0.firstKey()).length;
                if (intVarsToSelect > intVars)
                {
                    Logger.Log("Invalid number of vars to select");
                    List<Integer> results = new ArrayList<Integer>();
                    for (int i = 0; i < intVars; i++)
                    {
                        results.add(i);
                    }
                    return results;
                }

                if (featuresNames.size() != intVars)
                {
                    throw new HCException("Invalid number of variables names");
                }

                List<double[]> xVars = new ArrayList<double[]>(xVars0.values());
                List<double[]> xVarsTmp = new ArrayList<double[]>();
                for (int i = intForecast; i < xVars.size(); i++)
                {
                    xVarsTmp.add(xVars.get(i));
                }
                List<Double> yVars = new ArrayList<Double>(yVars0.values());
                List<Double> yVarsOri = new ArrayList<Double>(yVarsOri0.values());

                //
                // get var set
                //
                Hashtable<Integer, Object> varSet = new Hashtable<Integer, Object>();
                for (int i = 0; i < intVars; i++)
                {
                    if(blnCheckIgnoreList)
                    {
	                    if (!ignoreList.contains(i))
	                    {
	                        varSet.put(i, new Object());
	                    }
                    }
                    else
                    {
                        varSet.put(i, new Object());
                    }
                }

                //
                // initialize with default residuals as y
                //
                List<Double> prevResiduals = new ArrayList<Double>();
                for (int i = intForecast; i < yVars.size(); i++)
                {
                    prevResiduals.add(yVars.get(i));
                }
                List<Integer> selectedVariablesFinal = new ArrayList<Integer>();

                //
                // add lower bound variable
                //
                List<Double> prevErrorsSquare = null;
                IForecasterWrapper prevRegression = null;
                double dblPrevAdjustedCoeffDeterm = Double.NaN;
                if (intLowerBoundVariable >= 0)
                {
                    selectedVariablesFinal.add(intLowerBoundVariable);
                    if (!varSet.containsKey(intLowerBoundVariable))
                    {
                        throw new HCException("Variable not found");
                    }
                    varSet.remove(intLowerBoundVariable);

                    List<double[]> xVarsSubset = TimeSeriesHelper.SelectVariables(
                        xVars,
                        selectedVariablesFinal);
                    prevRegression = GetForecaster(xVarsSubset, yVars, blnUseNnet);
                    prevResiduals = GetResiduals(
                        blnUseReturns,
                        intForecast,
                        dblDelta[0],
                        dblMinVal[0],
                        yVarsOri,
                        yVars,
                        xVarsSubset,
                        prevRegression);
                    prevErrorsSquare = new ArrayList<Double>();
                    for(double dblVal : prevResiduals)
                    {
                    	prevErrorsSquare.add(dblVal * dblVal);
                    }
                    //prevErrorsSquare = (from n in prevResiduals select n * n).ToList();
                    dblPrevAdjustedCoeffDeterm = TimeSeriesHelper.GetAdjustedCoeffDeterm(
                        yVarsOri,
                        prevResiduals,
                        prevRegression.length() - 1);
                }

                //
                // iterate each variable, until required variables are found
                //
                HashMap<Integer, Double> rankingsMap = new HashMap<Integer, Double>();
                String strIgnoreReason = "";
                while (varSet.size() > 0)
                {
                    //
                    // get correlation with residuals
                    //
                    int intSelectedCorrelVar = TimeSeriesHelper.GetMostCorrelatedSymbol(
                        varSet,
                        xVarsTmp,
                        prevResiduals);
                    varSet.remove(intSelectedCorrelVar);

                    //
                    // get current model error
                    //
                    ArrayList<Integer> unionList = new ArrayList<Integer>(selectedVariablesFinal);
                    unionList.add(intSelectedCorrelVar);
                    List<double[]> xVarsSubset = TimeSeriesHelper.SelectVariables(
                        xVars,
                        unionList);

                    IForecasterWrapper currRegression = GetForecaster(xVarsSubset, yVars, blnUseNnet);
                    List<Double> currResiduals = GetResiduals(
                        blnUseReturns,
                        intForecast,
                        dblDelta[0],
                        dblMinVal[0],
                        yVarsOri,
                        yVars,
                        xVarsSubset,
                        currRegression);

//                    List<Double> errorsSquare = (from n in currResiduals
//                                                 select n * n).ToList();
                    List<Double> errorsSquare = new ArrayList<Double>();
                    for(double dblVal : currResiduals)
                    {
                    	errorsSquare.add(dblVal * dblVal);
                    }

                    double dblCurrAdjustedCoeffDeterm = TimeSeriesHelper.GetAdjustedCoeffDeterm(
                        yVarsOri,
                        currResiduals,
                        currRegression.length() - 1);

                    String strMessage;
                    if (prevErrorsSquare != null)
                    {
                        double dblAvgErrSquare = ListHelper.average(errorsSquare);
                        double dblPrevAvgErrSquare = ListHelper.average(prevErrorsSquare);

                        boolean blnAddVariable;
                        if (dblAvgErrSquare > dblPrevAvgErrSquare)
                        {
                            strIgnoreReason = "dblAvgErr[" +
                                              dblAvgErrSquare + "] > dblPrevAvgErr[" +
                                              dblPrevAvgErrSquare + "]";
                            blnAddVariable = false;
                        }
                        else if (HypothesisTests.IsMeanDiffEqualTo(
                            0.05,
                            0,
                            prevErrorsSquare,
                            errorsSquare))
                        {
                            double[] dblF = new double[1];
                            double[] dblCriticalValue = new double[1];
                            if (!HypothesisTests.IsModel2BetterThanModel1(
                                prevResiduals.size(),
                                dblPrevAvgErrSquare,
                                dblAvgErrSquare,
                                prevRegression.length() - 1,
                                currRegression.length() - 1,
                                dblF,
                                dblCriticalValue))
                            {
                                double dblImprovement = (dblPrevAvgErrSquare - dblAvgErrSquare) / dblPrevAvgErrSquare;
                                rankingsMap.put(intSelectedCorrelVar, dblImprovement);
                                strIgnoreReason = "Old model is better than new";
                                blnAddVariable = false;
                            }
                            else
                            {
                                blnAddVariable = true;
                            }

                        }
                        else
                        {
                            blnAddVariable = true;
                        }


                        if (blnAddVariable)
                        {
                            if (dblCurrAdjustedCoeffDeterm < dblPrevAdjustedCoeffDeterm)
                            {
                                strIgnoreReason = "dblCurrAdjustedCoeffDeterm[" + dblCurrAdjustedCoeffDeterm +
                                    "] < dblPrevAdjustedCoeffDeterm[" + dblPrevAdjustedCoeffDeterm + "]";
                                blnAddVariable = false;
                            }
                        }

                        if (blnVerbose)
                        {
                            strMessage = "Mean error diff [" + (dblPrevAvgErrSquare - dblAvgErrSquare) + "]";
                            Console.WriteLine(strMessage);
                            Logger.Log(strMessage);
                        }

                        if (blnAddVariable)
                        {
                            selectedVariablesFinal.add(intSelectedCorrelVar);
                            prevErrorsSquare = errorsSquare;
                            prevRegression = currRegression;
                            dblPrevAdjustedCoeffDeterm = dblCurrAdjustedCoeffDeterm;
                            List<Double> selectedVarVector = TimeSeriesHelper.GetVector(xVarsTmp, intSelectedCorrelVar);
                            IForecasterWrapper regression = GetForecaster2(selectedVarVector, prevResiduals, blnUseNnet);
                            prevResiduals = regression.GetErrors();

                            if (blnVerbose)
                            {
                                strMessage = "Added variable [" + intSelectedCorrelVar + "][" +
                                             featuresNames.get(intSelectedCorrelVar) +
                                             "]. Avg error = " + ListHelper.average(errorsSquare);
                                Console.WriteLine(strMessage);
                                Logger.Log(strMessage);
                            }
                        }
                        else
                        {
                            if (blnVerbose)
                            {
                                strMessage = "Ignored variable [" + intSelectedCorrelVar + "]" + "[" +
                                             featuresNames.get(intSelectedCorrelVar) + "]";
                                Console.WriteLine(strMessage);
                                Logger.Log(strMessage);
                                strMessage = "Ignore reason [" + strIgnoreReason + "]";
                                Console.WriteLine(strMessage);
                                Logger.Log(strMessage);
                            }
                        }
                    }
                    else
                    {
                        //
                        // lower bound variable
                        //
                        selectedVariablesFinal.add(intSelectedCorrelVar);
                        prevErrorsSquare = errorsSquare;
                        prevRegression = currRegression;
                        dblPrevAdjustedCoeffDeterm = dblCurrAdjustedCoeffDeterm;
                        List<Double> selectedVarVector = TimeSeriesHelper.GetVector(xVarsTmp, intSelectedCorrelVar);
                        IForecasterWrapper regression = GetForecaster2(selectedVarVector, prevResiduals, blnUseNnet);
                        prevResiduals = regression.GetErrors();

                        if (blnVerbose)
                        {
                            strMessage = "Added variable [" + intSelectedCorrelVar + "][" +
                                         featuresNames.get(intSelectedCorrelVar) + "]" +
                                         ". Avg error [" + ListHelper.average(errorsSquare) + "]";
                            Console.WriteLine(strMessage);
                            Logger.Log(strMessage);
                        }
                    }

                    if (selectedVariablesFinal.size() >= intVarsToSelect)
                    {
                        break;
                    }
                    int intPerc = selectedVariablesFinal.size() * 100 / intVarsToSelect;
                    if (blnVerbose)
                    {
                        strMessage = "JobsDone [" + intPerc + "]% [" + selectedVariablesFinal.size() + "/" +
                                      intVarsToSelect + "] .  [" + selectedVariablesFinal.size() +
                                     "] variables selected. [" + varSet.size() + "] variables to select";
                                     ;
                        Console.WriteLine(strMessage);
                        Logger.Log(strMessage);
                    }
                }
                int intTotalVars = selectedVariablesFinal.size();
                @SuppressWarnings("unchecked")
				Entry<Integer, Double>[] rankingsList = (Entry<Integer, Double>[])rankingsMap.entrySet().toArray(
                		new Map.Entry[rankingsMap.size()]);
                
                Arrays.sort(rankingsList, new Comparator<Entry<Integer, Double>>()
    					{
						    public int compare( Entry<Integer, Double> a, Entry<Integer, Double> b )
						    {
						        return a.getValue().compareTo(b.getValue());
						    }

							@Override
							public Comparator<Entry<Integer, Double>> reversed() {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public Comparator<Entry<Integer, Double>> thenComparing(
									Comparator<? super Entry<Integer, Double>> other) {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public <U> Comparator<Entry<Integer, Double>> thenComparing(
									Function<? super Entry<Integer, Double>, ? extends U> keyExtractor,
									Comparator<? super U> keyComparator) {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public <U extends Comparable<? super U>> Comparator<Entry<Integer, Double>> thenComparing(
									Function<? super Entry<Integer, Double>, ? extends U> keyExtractor) {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public Comparator<Entry<Integer, Double>> thenComparingInt(
									ToIntFunction<? super Entry<Integer, Double>> keyExtractor) {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public Comparator<Entry<Integer, Double>> thenComparingLong(
									ToLongFunction<? super Entry<Integer, Double>> keyExtractor) {
								// TODO Auto-generated method stub
								return null;
							}

							@Override
							public Comparator<Entry<Integer, Double>> thenComparingDouble(
									ToDoubleFunction<? super Entry<Integer, Double>> keyExtractor) {
								// TODO Auto-generated method stub
								return null;
							}
						});	
                
                //rankingsList.Sort((a,b) => -a.Value.CompareTo(b.Value));
                for (int i = intTotalVars, intCounter = 0; i < intVarsToSelect; i++, intCounter++)
                {
                    if (intCounter < rankingsList.length)
                    {
                        selectedVariablesFinal.add(rankingsList[intCounter].getKey());
                    }
                }
                return selectedVariablesFinal;
            }
            catch (Exception ex)
            {
                Logger.Log(ex);
            }
            return null;
        }
    
    
    private static List<Double> GetResiduals(
            boolean blnUseReturns,
            int intForecast,
            double dblDelta,
            double dblMinVal,
            List<Double> yVarsOri,
            List<Double> yVars,
            List<double[]> xVars,
            IForecasterWrapper regression)
        {
            List<Double> residuals;
            if (blnUseReturns)
            {
                residuals = new ArrayList<Double>();
                for (int i = intForecast; i < yVars.size(); i++)
                {
                    double dblCurrReturnForecast = regression.Forecast(xVars.get(i));
                    double dblCurrForecast = Math.exp(dblMinVal + dblCurrReturnForecast * dblDelta) *
                                             yVarsOri.get(i - intForecast);
                    double dblReal = yVarsOri.get(i);
                    double dblError = dblCurrForecast - dblReal;
                    residuals.add(dblError);
                }
            }
            else
            {
                residuals = regression.GetErrors();
            }
            return residuals;
        }    

    private static IForecasterWrapper GetForecaster(
            List<double[]> xVars,
            List<Double> yVars,
            boolean blnUseNnettoSelect)
        {
//            if (blnUseNnettoSelect)
//            {
//                return new NnetEarlyStopEnsemble(xVars, yVars);
//            }
            return new Regression(xVars, yVars);

        }

    private static IForecasterWrapper GetForecaster2(
            List<Double> xVars,
            List<Double> yVars,
            boolean blnUseNnettoSelect)
        {
//            if (blnUseNnettoSelect)
//            {
//                return new NnetEarlyStopEnsemble(xVars, yVars);
//            }
    		List<double[]> xVarsList = new ArrayList<double[]>();
    		for(double dblVal : xVars)
    		{
    			xVarsList.add(new double[] {dblVal });
    		}
            return new Regression(xVarsList, yVars);

        }
    
}
