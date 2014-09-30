package Armadillo.Analytics;

import org.junit.Assert;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.junit.Test;

import Armadillo.Analytics.SpecialFunctions.InvNormalFunct;
import Armadillo.Analytics.Stat.Distributions.BivNormalDistStd;
import Armadillo.Analytics.Stat.Distributions.UnivNormalDistStd;
import Armadillo.Analytics.Stat.Random.RngMersenneTwister;
import Armadillo.Core.Console;

public class NormalDistTests {

	@Test
	public void testBivNormal(){
		
		double dblVal = BivNormalDistStd.CdfStatic(0.0, 0.0, 0.5);
		Assert.assertTrue("Invalid value", Math.abs(dblVal - 1.0 / 3) < 1e-3);
		
		dblVal = BivNormalDistStd.CdfStatic(0.0, 6.0, 0.5);
		Assert.assertTrue("Invalid value", Math.abs(dblVal - 1.0 / 2) < 1e-3);
		
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void benchmarkCdfNormal()
	{
		int intTestSize = (int)1e5;
		RngMersenneTwister rng = new RngMersenneTwister();
		double[] rngList = new double[intTestSize];
		for (int i = 0; i < intTestSize; i++) 
		{
			rngList[i] = rng.nextDouble();
		}
		
		DateTime start = DateTime.now();
		for (int i = 0; i < 30; i++) 
		{
			for (int j = 0; j < rngList.length; j++) 
			{
			
				UnivNormalDistStd.CdfStatic(rngList[j]);
			}
		}
		DateTime end = DateTime.now();
		Console.writeLine(
				"Total seconds1 = " + 
				Seconds.secondsBetween(start, end).getSeconds());		
		
		start = DateTime.now();
		for (int i = 0; i < 30; i++) {
			for (int j = 0; j < rngList.length; j++) {
			
				UnivNormalDistStd.CdfStatic2(rngList[j]);
			}
		}
		end = DateTime.now();
		Console.writeLine(
				"Total seconds2 = " + 
				Seconds.secondsBetween(start, end).getSeconds());		
		
		start = DateTime.now();
		for (int i = 0; i < 30; i++) {
			for (int j = 0; j < rngList.length; j++) {
			
				UnivNormalDistStd.CdfStatic3(rngList[j]);
			}
		}
		end = DateTime.now();
		Console.writeLine(
				"Total seconds3 = " + 
				Seconds.secondsBetween(start, end).getSeconds());		
		
		
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void benchmarkInvNormal()
	{
		int intTestSize = (int)100;
		int intSampleSize = (int)1e4;
		RngMersenneTwister rng = new RngMersenneTwister();
		double[] rngList = new double[intTestSize];
		double[] samples = new double[intSampleSize];
		for (int i = 0; i < intTestSize; i++) {
			rngList[i] = rng.nextUniform();
		}
		
		for (int j = 0; j < rngList.length; j++) {
		
			DateTime start = DateTime.now();
			for (int k = 0; k < 20; k++) {
				for (int i = 0; i < intTestSize; i++) {
					if(i < intSampleSize){
						samples[i] = InvNormalFunct.InvNormal(rngList[i]);
					}
				}
			}
			DateTime end = DateTime.now();
			Console.writeLine(
					"Total seconds1 = " + 
					Seconds.secondsBetween(start, end).getSeconds());
			
			
			start = DateTime.now();
			for (int k = 0; k < 20; k++) {
				for (int i = 0; i < intTestSize; i++) {
					InvNormalFunct.InvNormal2(rngList[i]);
				}
			}
			end = DateTime.now();
			Console.writeLine(
					"Total seconds2 = " + 
					Seconds.secondsBetween(start, end).getSeconds());		
		
			double dblMaxError = -Double.MAX_VALUE;
			start = DateTime.now();
			for (int k = 0; k < 20; k++) {
				for (int i = 0; i < intTestSize; i++) {
					double dblVal = InvNormalFunct.InvNormal3(rngList[i]);
					if(i < intSampleSize){
						double dblError = Math.abs(samples[i] - dblVal);
						if(dblError > 1e-15){
							dblMaxError = Math.max(dblMaxError, dblError);
						}
					}
				}
			}
			Console.writeLine("dblMaxError = " + dblMaxError);
			end = DateTime.now();
			Console.writeLine(
					"Total seconds3 = " + 
					Seconds.secondsBetween(start, end).getSeconds());
		}
	}
}