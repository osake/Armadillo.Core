package Armadillo.Analytics;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import Armadillo.Core.Logger;
import Armadillo.Analytics.Stat.HypothesisTests;
import Armadillo.Analytics.Stat.Regression;
import Armadillo.Analytics.Stat.Distributions.UnivNormalDistStd;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public class TestHypothesisTests 
{

    @Test
    public void testIsModelBetterThan()
    {
    	try
    	{
	    	RngWrapper rng = new RngWrapper();
	    	List<double[]> xDataAll = new ArrayList<double[]>();
	    	List<double[]> xDataAllDummy1 = new ArrayList<double[]>();
	    	List<double[]> xDataAllDummy2 = new ArrayList<double[]>();
	    	List<double[]> xData1 = new ArrayList<double[]>();
	    	List<double[]> xData2 = new ArrayList<double[]>();
	    	List<Double> yData = new ArrayList<Double>();
	        for (int i = 0; i < 5000; i++)
	        {
	            double dblX1 = rng.nextDouble();
	            double dblX2 = rng.nextDouble();
	            xDataAll.add(new double[]  
	            		{
	            			dblX1,
	            			dblX2
	            		});
	            xDataAllDummy1.add(new double[]  
	            		{
	            			dblX1,
	            			0.5 + rng.nextDouble() * 0.1 / 2.0
	            		});
	            xDataAllDummy2.add(new double[]  
	            		{
	            			dblX2,
	            			0.5 + rng.nextDouble() * 0.1 / 2.0
	            		});
	            xData1.add(new double[]  
	            		{
	            			dblX1
	            		});
	            xData2.add(new double[]  
	            		{
	            			dblX2
	            		});
	            double dblY = dblX1 + 0.7 * dblX2 + 1.1;
	            yData.add(dblY);
	        }
	        
	        Regression regression1 = new Regression(xData1, yData);
	        Regression regression2 = new Regression(xDataAll, yData);
	        
	        double[] dblF = new double[1];
	        double[] dblCriticalValue = new double[1];
			boolean blnIsModel2BetterThanModel1 = HypothesisTests.IsModel2BetterThanModel1(regression1, regression2, dblF, dblCriticalValue);
			Assert.assertTrue("Model 2 is not better", blnIsModel2BetterThanModel1);
			
	        regression1 = new Regression(xData2, yData);
			blnIsModel2BetterThanModel1 = HypothesisTests.IsModel2BetterThanModel1(regression1, regression2, dblF, dblCriticalValue);
			Assert.assertTrue("Model 2 is not better", blnIsModel2BetterThanModel1);
			
	        regression1 = new Regression(xData1, yData);
	        regression2 = new Regression(xDataAllDummy1, yData);
			blnIsModel2BetterThanModel1 = HypothesisTests.IsModel2BetterThanModel1(regression1, regression2, dblF, dblCriticalValue);
			Assert.assertFalse("Model 2 is better", blnIsModel2BetterThanModel1);

	        regression1 = new Regression(xData2, yData);
	        regression2 = new Regression(xDataAllDummy2, yData);
			blnIsModel2BetterThanModel1 = HypothesisTests.IsModel2BetterThanModel1(regression1, regression2, dblF, dblCriticalValue);
			Assert.assertFalse("Model 2 is better", blnIsModel2BetterThanModel1);
        }
        catch(Exception ex)
        {
            Logger.Log(ex);
	        Assert.assertTrue(ex.toString(), false);
        }
    }
	
    @Test
    public void testMeanDiff()
    {
    	try
    	{
	    	RngWrapper rng = new RngWrapper();
	    	List<Double> normal1 = new ArrayList<Double>();
	    	List<Double> normal2 = new ArrayList<Double>();
	        double dblMeanValue = 2.5;
	        for (int i = 0; i < 1000; i++)
	        {
	            double dblRandom = rng.nextDouble();
	            normal1.add(dblMeanValue + 1.5  * UnivNormalDistStd.CdfInvStatic(dblRandom));
	            dblRandom = rng.nextDouble();
	            normal2.add(UnivNormalDistStd.CdfInvStatic(dblRandom));            
	        }
	        double dblAlpha = 0.05;
	        boolean blnIsEqualsTo = HypothesisTests.IsMeanDiffEqualTo(
	                dblAlpha,
	                dblMeanValue, 
	                normal1, 
	                normal2);
	        
	        Assert.assertTrue("Mean is not equals", blnIsEqualsTo);
	        
	        blnIsEqualsTo = HypothesisTests.IsMeanDiffEqualTo(
	                dblAlpha,
	                dblMeanValue + 0.5, 
	                normal1, 
	                normal2);
	        
	        Assert.assertFalse("Mean is equals", blnIsEqualsTo);
        }
        catch(Exception ex)
        {
            Logger.Log(ex);
	        Assert.assertTrue(ex.toString(), false);
        }
    }
    	
    @Test
    public void testNormallyDistributed()
    {
    	try
    	{
	    	RngWrapper rng = new RngWrapper();
	    	List<Double> randoms = new ArrayList<Double>();
	    	List<Double> normal = new ArrayList<Double>();
	        for (int i = 0; i < 500000; i++)
	        {
	            double dblRandom = rng.nextDouble();
	            randoms.add(dblRandom);
	            normal.add(UnivNormalDistStd.CdfInvStatic(dblRandom));
	        }
	        boolean blnRandom = !HypothesisTests.IsNormallyDistributed(randoms);
	        boolean blnNormal = HypothesisTests.IsNormallyDistributed(normal);
	
	        Assert.assertTrue("Invalid result", blnNormal && blnRandom);
        }
        catch(Exception ex)
        {
            Logger.Log(ex);
	        Assert.assertTrue(ex.toString(), false);
        }
    }	
}
