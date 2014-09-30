package Armadillo.Analytics.Stat;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Analytics.Stat.Distributions.ChiSquareDist;
import Armadillo.Analytics.Stat.Distributions.FDist;
import Armadillo.Analytics.Stat.Distributions.TStudentDist;
import Armadillo.Analytics.TimeSeries.ListHelper;
import Armadillo.Core.Logger;

public class HypothesisTests 
{
    public static boolean IsNormallyDistributed(List<Double> data)
    {
        return IsNormallyDistributed(data, 0.05);
    }

    public static boolean IsNormallyDistributed(List<Double> data,
        double dblAlpha)
    {
    	try
    	{
	        data = new ArrayList<Double>(data);
	        double dblSkewness = StatsHelper.momentSkewness(data);
	        double dblKurtosis = StatsHelper.kurtosis(data);
	        double dblJarkeBera = (data.size()/6.0)*(Math.pow(dblSkewness, 2) +
	                                                (Math.pow(dblKurtosis - 3.0, 2)/4.0));
	        double dblCriticalValue = ChiSquareDist.invchisquaredistribution(
	            2, dblAlpha);
	
	        boolean blnRejectNullHyp = dblJarkeBera < dblCriticalValue;
	        return blnRejectNullHyp;
        }
        catch(Exception ex)
        {
            Logger.Log(ex);
        }
    	return false;
    }
    
    public static boolean IsMeanDiffEqualTo(
            double dblAlpha,
            double dblTestValue, 
            List<Double> list1, 
            List<Double> list2)
        {
            try
            {
                double dblVar1 = StatsHelper.getVarianceFromList(list1);
                double dblVar2 = StatsHelper.getVarianceFromList(list2);
                int intN1 = list1.size();
                int intN2 = list2.size();

                double dblVar = (((intN1 - 1)*dblVar1) + ((intN2 - 1)*dblVar2))/
                                (intN1 + intN2 - 2);
                double dblDen = Math.sqrt(dblVar*((1.0/intN1) + (1.0/intN2)));
                double dblT = (ListHelper.average(list1) - 
                		ListHelper.average(list2) - dblTestValue)/dblDen;
                int intDof = intN1 + intN2 - 2;
                double dblAlphaTail = dblAlpha/2.0;
                double dblLowerBound = TStudentDist.CdfInvStatic(intDof, dblAlphaTail);
                double dblUpperBound = TStudentDist.CdfInvStatic(intDof, 1.0 - dblAlphaTail);
                return dblT >= dblLowerBound && dblT <= dblUpperBound;
            }
            catch(Exception ex)
            {
                Logger.Log(ex);
            }
            return false;
        }


        public static boolean IsModel2BetterThanModel1(
            Regression regression1,
            Regression regression2, 
            double[] dblF, 
            double[] dblCriticalValue)
        {
            int intP1 = regression1.Weights.length - 1;
            int intP2 = regression2.Weights.length - 1;
            List<Double> errors1 = regression1.GetErrors();
            List<Double> errors2 = regression2.GetErrors();
            return IsModel2BetterThanModel1(
                intP1,
                intP2,
                errors1,
                errors2,
                dblF,
                dblCriticalValue);
        }

        public static boolean IsModel2BetterThanModel1(
            int intVars1, 
            int intVars2, 
            List<Double> errors1, 
            List<Double> errors2, 
            double[] dblF, 
            double[] dblCriticalValue)
        {
        	try
        	{
	        	double dblRss1 = 0;
	        	double dblRss2 = 0;
	        	for (int i = 0; i < errors1.size(); i++) 
	        	{
	        		dblRss1 += errors1.get(i) * errors1.get(i);
				}
	        	for (int i = 0; i < errors2.size(); i++) 
	        	{
	        		dblRss2 += errors2.get(i) * errors2.get(i);
	        	}
	            return IsModel2BetterThanModel1(
	                errors1.size(),
	                dblRss1,
	                dblRss2,
	                intVars1,
	                intVars2,
	                dblF,
	                dblCriticalValue);
        	}
        	catch(Exception ex)
        	{
        		Logger.log(ex);
        	}
        	return false;
        }

        public static boolean IsModel2BetterThanModel1(
            int intN, 
            double dblRss1, 
            double dblRss2, 
            int intVars1, 
            int intVars2, 
            double[] dblF, 
            double[] dblCriticalValue)
        {
            dblF[0] = 0;
            dblCriticalValue[0] = 0;
            try
            {
                dblF[0] = ((dblRss1 - dblRss2) / (intVars2 - intVars1)) / (dblRss2 / (intN - intVars2));
                int intF1 = intVars2 - intVars1;
                int intF2 = intN - intVars2;
                if (intF1 <= 0 ||
                    intF2 <= 0)
                {
                    return false;
                }
                dblCriticalValue[0] = FDist.fdistribution(intF1, intF2, 0.05);

                //
                // null hypothesis: Model2 does not provide better fit than model1
                //
                boolean blnRejectNullHyp = dblF[0] > dblCriticalValue[0];
                return blnRejectNullHyp;
            }
            catch(Exception ex)
            {
                Logger.Log(ex);
            }
            return false;
       }
}
