package Armadillo.Analytics.TimeSeries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import org.joda.time.DateTime;

import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.Math.RollingWindowStdDev;
import Armadillo.Core.Math.TsRow2D;

public class Outliers 
{
    public static final int DEFAULT_SAMPLE_SIZE = 30;
    public static final int DEFAULT_OUTLIER_THRESHOLD = 6;
    private static final int MIN_SAMPLE_SIZE = 4;

    public static List<Double> CorrectOutliers(
        List<Double> data)
    {
        return CorrectOutliers(data, DEFAULT_OUTLIER_THRESHOLD);
    }

    public static List<Double> CorrectOutliers(
        List<Double> data,
        double dblThreshold)
    {
        try
        {
            if(data == null ||
                data.size() == 0)
            {
                return new ArrayList<Double>();
            }
            DateTime currDate = new DateTime();
            ArrayList<TsRow2D> tsRows = new ArrayList<TsRow2D>();
            for (double dblVal : data)
            {
            	TsRow2D currRow = new TsRow2D(currDate, dblVal);
                tsRows.add(currRow);
                currDate = currDate.plusDays(1);
            }
            int intSampleSize = Math.min(DEFAULT_SAMPLE_SIZE, tsRows.size()/2);
            List<TsRow2D> result = CorrectOutliers(tsRows,
                intSampleSize,
                dblThreshold);
            List<Double> results = new ArrayList<Double>();
            for (TsRow2D tsRow2D : result) 
            {
            	results.add(tsRow2D.Fx);
			}
            //var values = (from n in result select n.Fx).ToList();
            return results;
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        return new ArrayList<Double>();
    }

    public static List<TsRow2D> correctOutliersTsRows(
        List<TsRow2D> data)
    {
        return CorrectOutliers(data, DEFAULT_SAMPLE_SIZE);
    }

    public static List<TsRow2D> CorrectOutliersTsRows(
        List<TsRow2D> data,
        double dblOutlierThreshold)
    {
        return CorrectOutliers(data, DEFAULT_SAMPLE_SIZE, dblOutlierThreshold);
    }

    public static List<TsRow2D> CorrectOutliers(
        List<TsRow2D> data,
        int intSampleSize)
    {
        return CorrectOutliers(data, intSampleSize, DEFAULT_OUTLIER_THRESHOLD);
    }

    public static List<TsRow2D> CorrectOutliers(
        List<TsRow2D> data,
        int intSampleSize,
        double dblOutliersThreshold)
    {
        try
        {
            List<TsRow2D> outliers;
            if (intSampleSize >= data.size() - 2)
            {
                outliers = GetOutliersSmallSample(data, dblOutliersThreshold);
            }
            else
            {
                outliers = GetOutliers(data,
                                       dblOutliersThreshold,
                                       intSampleSize);
            }

            if(outliers == null || outliers.size() == 0)
            {
                return new ArrayList<TsRow2D>(data);
            }

            Hashtable<Long, TsRow2D> outliersMap = new Hashtable<Long, TsRow2D>(); 
            for (TsRow2D tsRow2D : outliers) 
            {
            	outliersMap.put(tsRow2D.getTime().getMillis(), tsRow2D);
			}
//            Dictionary<Long, TsRow2D> outliersMap =
//                outliers.ToDictionary(t => t.Time, t => t);
            ArrayList<TsRow2D> outData = new ArrayList<TsRow2D>();
            if (outliersMap.size() > 0)
            {
            	RollingWindowRegression rollingWindoRegression = new RollingWindowRegression(intSampleSize);
            	RollingWindowStdDev rollingWindowTsFunction = new RollingWindowStdDev(intSampleSize);

                for (TsRow2D tsRow2D : data)
                {
                    double dblValue;
                    if (outliersMap.containsKey(tsRow2D.Time) &&
                        rollingWindoRegression.IsReady())
                    {
                        double dblPrediction = rollingWindoRegression.Predict(
                            rollingWindoRegression.XList.get(rollingWindoRegression.XList.size() - 1) + 1);
                        dblPrediction = Math.max(dblPrediction, rollingWindowTsFunction.Min());
                        dblPrediction = Math.min(dblPrediction, rollingWindowTsFunction.Max());

                        Console.writeLine(Outliers.class.getName() + 
                            " corrected value [" + dblPrediction + "]. Old [" +
                            tsRow2D.Fx + "]");

                        outData.add(
                            new TsRow2D(
                                    tsRow2D.Time,
                                    dblPrediction
                                ));
                        dblValue = dblPrediction;
                    }
                    else
                    {
                        outData.add(tsRow2D);
                        dblValue = tsRow2D.Fx;
                    }
                    rollingWindoRegression.Update(tsRow2D.Time, tsRow2D.Fx);
                    rollingWindowTsFunction.Update(tsRow2D.Time, dblValue);
                }
            }
            else
            {
                outData.addAll(data);
            }
            return outData;
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return new ArrayList<TsRow2D>();
    }

    public static boolean IsOutlier(
        List<Double> dblList,
        double dblVal)
    {
    	ArrayList<TsRow2D> data = new ArrayList<TsRow2D>();
        DateTime baseDate = new DateTime();
        for (int i = 0; i < dblList.size(); i++)
        {
            data.add(
                new TsRow2D(baseDate, dblList.get(i)));
            baseDate = baseDate.plusSeconds(1);
        }

        baseDate = baseDate.plusSeconds(1);
        TsRow2D tsRow = new TsRow2D(baseDate, dblVal);
        return IsOutlier(data, tsRow);
    }

    public static boolean ContainsOutlier(
        List<TsRow2D> data)
    {
        return ContainsOutlier(data, DEFAULT_OUTLIER_THRESHOLD);
    }

    public static boolean ContainsOutlier(
        List<TsRow2D> data,
        double dblOutlierThreshold)
    {
        try
        {
            List<TsRow2D> outliers = GetOutliers(data, dblOutlierThreshold, DEFAULT_SAMPLE_SIZE);
            return outliers != null && outliers.size() > 0;
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return false;
    }

    public static boolean IsOutlier(
        List<TsRow2D> data,
        TsRow2D tsRow2D)
    {
        try
        {
            data = new ArrayList<TsRow2D>(data);
            data.add(tsRow2D);
            List<TsRow2D> outliers = GetOutliers(data, DEFAULT_OUTLIER_THRESHOLD, DEFAULT_SAMPLE_SIZE);
            for (TsRow2D tsRow2D2 : outliers) 
            {
            	if(tsRow2D2.Time == tsRow2D.Time)
            	{
            		return true;
            	}
            	
			}
            return false;
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        return false;
    }

    public static List<TsRow2D> GetOutliers(List<TsRow2D> data)
    {
        return GetOutliers(data, DEFAULT_OUTLIER_THRESHOLD, DEFAULT_SAMPLE_SIZE);
    }

    public static List<TsRow2D> GetOutliers(
        List<TsRow2D> data,
        double dblOutlierThreshold,
        int intSampleSize)
    {
        try
        {
            if (intSampleSize >= data.size() - 2)
            {
                return GetOutliersSmallSample(data, dblOutlierThreshold);
            }

            List<TsRow2D> derivatives = TimeSeriesHelper.GetDerivative(1, data);
            
            List<TsRow2D> derivativesTsList = GetOutliers0(
                    derivatives,
                    dblOutlierThreshold,
                    intSampleSize);
            Hashtable<Long, Double> ouliersDerivatives = new Hashtable<Long, Double>(); 
            for (TsRow2D tsRow2D : derivativesTsList) 
            {
            	ouliersDerivatives.put(tsRow2D.getTime().getMillis(), tsRow2D.Fx);
			}
            
//            Dictionary<Long, double> ouliersDerivatives = GetOutliers0(
//                derivatives,
//                dblOutlierThreshold,
//                intSampleSize)
//                .ToDictionary(t => t.Time, t => t.Fx);

            
            List<TsRow2D> dataTsList = GetOutliers0(
            		data,
                    dblOutlierThreshold,
                    intSampleSize);
            Hashtable<Long, TsRow2D> outliersMap = new Hashtable<Long, TsRow2D>(); 
            for (TsRow2D tsRow2D : dataTsList) 
            {
            	outliersMap.put(tsRow2D.getTime().getMillis(), tsRow2D);
			}
            
//            Dictionary<Long, TsRow2D> outliersMap = GetOutliers0(data, dblOutlierThreshold,
//                                                                     intSampleSize)
//                .ToDictionary(t => t.Time, t => t);

            List<TsRow2D> foundOutliers = new ArrayList<TsRow2D>();
            boolean blnOutlierMode = false;
            for (int i = 0; i < data.size(); i++)
            {
                TsRow2D tsRow2D = data.get(i);
                DateTime time = tsRow2D.getTime();

                if (outliersMap.containsKey(time))
                {
                    TsRow2D outlier = outliersMap.get(time);
                    if (ouliersDerivatives.containsKey(time))
                    {
                        foundOutliers.add(outlier);
                        blnOutlierMode = true;
                    }
                    else if (blnOutlierMode)
                    {
                        //
                        // outlayers are together, but the derivative did not find it
                        //
                        foundOutliers.add(outlier);
                    }
                }
                else
                {
                    blnOutlierMode = false;
                }
            }
            return foundOutliers;
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return new ArrayList<TsRow2D>();
    }

    private static List<TsRow2D> GetOutliersSmallSample(
        List<TsRow2D> data, 
        double dblOutlierThreshold)
    {
        try
        {
            if (data.size() < MIN_SAMPLE_SIZE)
            {
                return new ArrayList<TsRow2D>();
            }
            RollingWindowTsFunction rollingWindowTsFunction =
                new RollingWindowTsFunction(MIN_SAMPLE_SIZE);
            Hashtable<Long, Object> dateValidator = new Hashtable<Long, Object>();
            List<TsRow2D> outLiers = new ArrayList<TsRow2D>();
            List<Double> testData = new ArrayList<Double>();
            for (int i = 0; i < data.size(); i++)
            {
                rollingWindowTsFunction.Update(
                    data.get(i).Time, data.get(i).Fx);
                testData.add(data.get(i).Fx);
            }
            double dblMedian = Median.median(testData.toArray(new Double[0]));
            
            List<Double> r = new ArrayList<Double>();
            for (Double double1 : testData) 
            {
				r.add(Math.abs(double1 - dblMedian));
			}
            
            //List<Double> r = (from n in testData select Math.Abs(n - dblMedian)).ToList();
            
            
            
            double dblMedianR = Median.median(r.toArray(new Double[0]));

            for (int i = 0; i < data.size(); i++)
            {
                TsRow2D currRow = data.get(i);
                double dblR = Math.abs(currRow.Fx - dblMedian);
                if (dblR >= dblOutlierThreshold*dblMedianR &&
                    !dateValidator.containsKey(currRow.Time))
                {
                    outLiers.add(currRow);
                    dateValidator.put(currRow.getTime().getMillis(), new Object());
                }
            }
            
            Collections.sort(outLiers, new Comparator<TsRow2D>() 
					{
				@Override
				public int compare(
						TsRow2D item1,
						TsRow2D item2) 
				{
					return item1.Time.compareTo(item2.Time);
					
				}
				});

            //outLiers.Sort((a, b) => a.Time.CompareTo(b.Time));
            return outLiers;
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return new ArrayList<TsRow2D>();
    }

    private static List<TsRow2D> GetOutliers0(
        List<TsRow2D> data,
        double dblOutlierThreshold,
        int intSampleSize)
    {
        try
        {
            if (data.size() <= intSampleSize)
            {
                return new ArrayList<TsRow2D>();
            }

            RollingWindowTsFunction rollingWindowTsFunction =
                new RollingWindowTsFunction(intSampleSize);
            Hashtable<Long, Object> dateValidator = new Hashtable<Long, Object>();
            List<TsRow2D> outLiers = new ArrayList<TsRow2D>();
            for (int i = 0; i < data.size(); i++)
            {
                rollingWindowTsFunction.Update(
                    data.get(i).Time, data.get(i).Fx);
                if (rollingWindowTsFunction.IsReady())
                {
                	List<Double> testData = new ArrayList<Double>();
                	for (TsRow2D tsRow2D : rollingWindowTsFunction.Data) 
                	{
                		testData.add(tsRow2D.Fx);
					}
                	
//                    List<Double> testData = (from n in
//                                                 rollingWindowTsFunction.Data
//                                             select n.Fx).ToList();
                    double dblMedian = Median.median(testData.toArray(new Double[0]));
                    
                    List<Double> r = new ArrayList<Double>();
                    for (Double double1 : testData) 
                    {
						r.add(Math.abs(double1 - dblMedian));
					}
                    
                    //List<Double> r = (from n in testData select Math.Abs(n - dblMedian)).ToList();
                    
                    double dblMedianR = Median.median(r.toArray(new Double[0]));
                    for (int j = 0; j < rollingWindowTsFunction.Data.size(); j++)
                    {
                        TsRow2D currRow = rollingWindowTsFunction.Data.get(j);
                        double dblR = Math.abs(currRow.Fx - dblMedian);
                        if (dblR >= dblOutlierThreshold*dblMedianR &&
                            !dateValidator.containsKey(currRow.Time))
                        {
                            //Console.WriteLine("Outlayer found. median [" + dblMedian + "]. val[" +
                            //                  currRow.Fx + "]" +
                            //                  dblR + ">=" + (dblOutlierThreshold*dblMedianR));
                            outLiers.add(currRow);
                            dateValidator.put(currRow.getTime().getMillis(), new Object());
                        }
                    }
                }
            }
            //outLiers.Sort((a, b) => a.Time.CompareTo(b.Time));
            
            Collections.sort(outLiers, new Comparator<TsRow2D>() 
					{
				@Override
				public int compare(
						TsRow2D item1,
						TsRow2D item2) 
				{
					return item1.Time.compareTo(item2.Time);
					
				}
				});
            
            return outLiers;
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return new ArrayList<TsRow2D>();
    }

}
