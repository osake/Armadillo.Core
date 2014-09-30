package Armadillo.Analytics;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import Armadillo.Core.Logger;
import Armadillo.Analytics.Stat.VariableSelector;
import Armadillo.Analytics.Stat.Random.RngWrapper;

public class TestVariableSelector 
{
	@Test
	public void tesetSelctVariables()
	{
    	try
    	{
    		//
    		// select variables which best contributes to the regression
    		//
	    	RngWrapper rng = new RngWrapper();
	    	List<double[]> xDataAll = new ArrayList<double[]>();
	    	List<double[]> xData = new ArrayList<double[]>();
	    	List<Double> yData = new ArrayList<Double>();
	    	final int intBetas = 10;
	    	final int intDummyVars = 5;
	    	final int intSamples = 5000;
	    	final int intVariablesToSelect = 10;
	    	double[] betaArr = new double[intBetas];
	    	for (int i = 0; i < intBetas; i++) 
	    	{
				betaArr[i] = 100 * rng.nextDouble();
			}
	        for (int i = 0; i < intSamples; i++)
	        {
	        	double dblY = 0;
	        	double[] xArr = new double[intBetas];
	        	double[] xArrAll = new double[intBetas + intDummyVars];
	        	for (int j = 0; j < betaArr.length; j++) 
	        	{
	        		double dblCurrRng = j + 0.01 * rng.nextDouble()/2;
	        		xArr[j] = dblCurrRng;
	        		xArrAll[j] = dblCurrRng;
				}
	        	for (int j = betaArr.length; j < betaArr.length + intDummyVars; j++) 
	        	{
	        		double dblCurrRng = rng.nextDouble();
	        		xArrAll[j] = dblCurrRng;
	        		dblY+= dblCurrRng * betaArr[j - betaArr.length];
				}
	        	
	        	xData.add(xArr);
	        	xDataAll.add(xArrAll);
	            yData.add(dblY);
	        }
	        
	        List<Integer> bestVariables = VariableSelector.SelectBestVariablesLocal(xDataAll, yData, intVariablesToSelect);
	        Assert.assertTrue("too many variables selected ", bestVariables.size() <= intVariablesToSelect);
	        Assert.assertTrue("not variables found", bestVariables.size() > 0);
	        
	        for (int intBestVar : bestVariables) 
	        {
	        	Assert.assertTrue("Invalid variable selection [" +
	        				intBestVar + "]", intBestVar >= betaArr.length);
			}
    	}
    	catch(Exception ex)
    	{
    		Logger.log(ex);
    		Assert.assertTrue(ex.toString(), false);
    	}
	}
}
