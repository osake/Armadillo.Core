package Armadillo.Analytics;

import org.junit.Assert;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.junit.Test;
import Armadillo.Analytics.Base.FastMath;
import Armadillo.Analytics.Stat.Random.RngMersenneTwister;
import Armadillo.Core.Console;

public class MathFunctionsTests 
{
	public void benchmarkExp(){
		//
		// seem to show promising performance!
		//
		int intSampleSize = (int)5e6;
		RngMersenneTwister rng = new RngMersenneTwister();
		double[] rngList = new double[intSampleSize];
		
		for (int i = 0; i < intSampleSize; i++) {
			rngList[i] = 4 * rng.nextUniform() - 2;
		}
		
		for (int k = 0; k < 10; k++) {
			
			DateTime start = DateTime.now();
			for (int j = 0; j < 40; j++) {
				for (int i = 0; i < intSampleSize; i++) {
					FastMath.exp(rngList[i]);
				}
			}
			DateTime end = DateTime.now();
			Console.writeLine(
					"Total seconds exp1 = " + 
					Seconds.secondsBetween(start, end).getSeconds());
			
			start = DateTime.now();
			for (int j = 0; j < 40; j++) {
				for (int i = 0; i < intSampleSize; i++) {
					Math.exp(rngList[i]);
				}
			}
			end = DateTime.now();
			Console.writeLine(
					"Total seconds exp2 = " + 
					Seconds.secondsBetween(start, end).getSeconds());
			
			for (int i = 0; i < intSampleSize; i++) {
				double dblError = Math.abs(FastMath.exp(rngList[i]) - Math.exp(rngList[i]));
				if(dblError > 1e-10){
					Console.writeLine("Error " + dblError);
					Assert.assertTrue("Error", false);
				}
			}
		}
		Console.writeLine("Finish test exp");
	}
	
	public void benchmarkPow()
	{
		//
		// same performance
		//
		int intSampleSize = (int)5e6;
		RngMersenneTwister rng = new RngMersenneTwister();
		double[] rngList = new double[intSampleSize];
		
		for (int i = 0; i < intSampleSize; i++) {
			rngList[i] = 4 * rng.nextUniform() - 2;
		}
		
		DateTime start = DateTime.now();
		for (int j = 0; j < 40; j++) {
			for (int i = 0; i < intSampleSize; i++) {
				FastMath.pow(rngList[i], rngList[i]);
			}
		}
		DateTime end = DateTime.now();
		Console.writeLine(
				"Total seconds pow1 = " + 
				Seconds.secondsBetween(start, end).getSeconds());
		
		start = DateTime.now();
		for (int j = 0; j < 40; j++) {
			for (int i = 0; i < intSampleSize; i++) {
				Math.pow(rngList[i], rngList[i]);
			}
		}
		end = DateTime.now();
		Console.writeLine(
				"Total seconds pow2 = " + 
				Seconds.secondsBetween(start, end).getSeconds());
		
		for (int i = 0; i < intSampleSize; i++) {
			double dblError = Math.abs(
					FastMath.pow(rngList[i], rngList[i]) - 
					Math.pow(rngList[i], rngList[i]));
			if(dblError > 1e-10){
				Console.writeLine("Error " + dblError);
				Assert.assertTrue("Error", false);
			}
		}
		Console.writeLine("Finish test pow");
	}
	
	@Test
	public void benchmarkLog(){
		//
		// heuristic works very bad!
		//
		int intSampleSize = (int)5e6;
		RngMersenneTwister rng = new RngMersenneTwister();
		double[] rngList = new double[intSampleSize];
		
		for (int i = 0; i < intSampleSize; i++) {
			rngList[i] = 15 * rng.nextUniform();
		}
		
		DateTime start = DateTime.now();
		for (int j = 0; j < 20; j++) {
			for (int i = 0; i < intSampleSize; i++) {
				FastMath.log(rngList[i]);
			}
		}
		DateTime end = DateTime.now();
		Console.writeLine(
				"Total seconds log1 = " + 
				Seconds.secondsBetween(start, end).getSeconds());
		
		start = DateTime.now();
		for (int j = 0; j < 20; j++) {
			for (int i = 0; i < intSampleSize; i++) {
				Math.log(rngList[i]);
			}
		}
		end = DateTime.now();
		Console.writeLine(
				"Total seconds log2 = " + 
				Seconds.secondsBetween(start, end).getSeconds());
		
		Console.writeLine("Finish test log");
	}

}
