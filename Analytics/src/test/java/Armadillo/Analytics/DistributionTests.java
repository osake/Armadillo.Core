package Armadillo.Analytics;

import java.util.Arrays;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.junit.Assert;
import org.junit.Test;

import Armadillo.Analytics.Stat.Distributions.BinomialDist;
import Armadillo.Analytics.Stat.Distributions.GammaDist;
import Armadillo.Analytics.Stat.Distributions.LogNormalDist;
import Armadillo.Analytics.Stat.Distributions.PoissonDist;
import Armadillo.Analytics.Stat.Random.IRng;
import Armadillo.Analytics.Stat.Random.RngBase;
import Armadillo.Analytics.Stat.Random.RngMersenneTwister;
import Armadillo.Analytics.Stat.Random.RngWrapper;
import Armadillo.Core.Console;
import Armadillo.Core.Logger;
import Armadillo.Core.Concurrent.ILoopBody;
import Armadillo.Core.Concurrent.Parallel;

public class DistributionTests 
{

	@Test
	public void testGammaRand()
	{
		try
		{
			//
			// get samples
			//
			final double dblAlpha = 10;
			final double dblBeta = 2;
			final RngMersenneTwister rng = new RngMersenneTwister();
			int intSampleSize = (int)1e6;
			final double[] rngList = new double[intSampleSize];
			Parallel.For(0, intSampleSize, new ILoopBody<Integer>() 
					{
						@Override
						public void run(Integer i) 
						{
							
							rngList[i] = GammaDist.NextDoubleStatic(dblAlpha, dblBeta, rng);					
						}
					}, 20);
			
			double dblAverage = 0;
			for (int i = 0; i < intSampleSize; i++) 
			{
				dblAverage += rngList[i];
			}
			dblAverage /=intSampleSize;
			double dblStdDev = 0;
			for (int i = 0; i < intSampleSize; i++) 
			{
				dblStdDev +=  Math.pow(dblAverage - rngList[i], 2);
			}
			dblStdDev = Math.sqrt(dblStdDev / (intSampleSize - 1)); 
			double dblAlphaSample = Math.pow(dblAverage / dblStdDev, 2);
			double dblError = 1 - (dblAlpha - Math.abs(dblAlphaSample - dblAlpha)) / dblAlpha;
			
			if(dblError > 1e-2)
			{
				Console.writeLine("Error");
				Assert.assertTrue("Error", false);
			}
			double dblBetaSample =  dblAverage / Math.pow(dblStdDev, 2);
			dblError = 1 - (dblBeta - Math.abs(dblBetaSample - dblBeta)) / dblBeta;
			if(dblError > 1e-1)
			{
				Console.writeLine("Error");
				Assert.assertTrue("Error", false);
			}			
			Console.writeLine("Finish test");
		}
		catch(Exception ex)
		{
			Logger.log(ex);
			Assert.assertTrue("Error", false);
		}
	}		
	
	@Test
	public void testLogNormalRand(){
		try{
			//
			// get samples
			//
			final double dblMu = 10;
			final LogNormalDist ln = new LogNormalDist(dblMu, 1);
			int intSampleSize = (int)1e6;
			final RngMersenneTwister rng = new RngMersenneTwister();
			final double[] rngList1 = new double[intSampleSize];
			Parallel.For(0, intSampleSize, new ILoopBody<Integer>() {
				@Override
				public void run(Integer i) {
					rngList1[i] = ln.NextDouble(rng);					
				}
			}, 20);
			
			double dblAverage = 0;
			for (int i = 0; i < intSampleSize; i++) {
				dblAverage += rngList1[i];
			}
			dblAverage /=intSampleSize;
			double dblStdDev = 0;
			for (int i = 0; i < intSampleSize; i++) {
				dblStdDev +=  Math.pow(dblAverage - rngList1[i], 2);
			}
			dblStdDev = Math.sqrt(dblStdDev / (intSampleSize - 1)); 
			double dblMean = ln.getNumericalMean();
			double dblError = 1 - (dblMean - Math.abs(dblAverage - dblMean)) / dblMean;
			if(dblError > 1e-2){
				Console.writeLine("Error");
				Assert.assertTrue("Error", false);
			}
			double dblStdDev0 =  Math.sqrt(ln.getNumericalVariance());
			dblError = 1 - (dblStdDev0 - Math.abs(dblStdDev - dblStdDev0)) / dblStdDev0;
			if(dblError > 1e-1){
				Console.writeLine("Error");
				Assert.assertTrue("Error", false);
			}			
			Console.writeLine("Finish test");
		}
		catch(Exception ex){
			Logger.log(ex);
			Assert.assertTrue("Error", false);
		}
	}	
	
	@Test
	public void testPissonRand(){
		try{
			//
			// get samples
			//
			int intSampleSize = (int)1e6;
			final IRng rng = new RngMersenneTwister();
			final double dblLambda = 100;
			final double[] rngList1 = new double[intSampleSize];
			Parallel.For(0, intSampleSize, new ILoopBody<Integer>() {
				@Override
				public void run(Integer i) {
					rngList1[i] = PoissonDist.NextInt(dblLambda, rng);					
				}
			}, 20);
			
			double dblAverage = 0;
			for (int i = 0; i < intSampleSize; i++) {
				dblAverage += rngList1[i];
			}
			dblAverage /=intSampleSize;
			
			double dblError = 1 - (dblLambda - Math.abs(dblAverage - dblLambda)) / dblLambda;
			if(dblError > 1e-3){
				Console.writeLine("Error");
				Assert.assertTrue("Error", false);
			}
			Console.writeLine("Finish test");
		}
		catch(Exception ex){
			Logger.log(ex);
			Assert.assertTrue("Error", false);
		}
	}	
	
	@Test
	public void compareAsyncRand(){
		try{
			//
			// get samples
			//
			int intSampleSize = (int)1e5;
			double[] rngList1 = null;
			
			DateTime start = DateTime.now();
			for (int k = 0; k < 10; k++) {
				RngMersenneTwister rng = new RngMersenneTwister(123);
				rngList1 = new double[intSampleSize];
				for (int i = 0; i < intSampleSize; i++) {
					rngList1[i] = rng.nextUniform();
				}
			}
			DateTime end = DateTime.now();
			Console.writeLine(
					"Total seconds sync = " + 
					Seconds.secondsBetween(start, end).getSeconds());
			
			start = DateTime.now();
			double[] rngList2 = null;
			for (int k = 0; k < 10; k++) {
				final RngMersenneTwister rng2 = new RngMersenneTwister(123);
				final double[] rngList2_ = new double[intSampleSize];
				Parallel.For(0, intSampleSize, new ILoopBody<Integer>() {
					
					public void run(Integer i) {
						rngList2_[i] = rng2.nextUniform();
					}
				});
				rngList2 = rngList2_;
			}
			end = DateTime.now();
			Console.writeLine(
					"Total seconds async = " + 
					Seconds.secondsBetween(start, end).getSeconds());
			
			double dblMean1 = 0;
			double dblMean2 = 0;
			for (int i = 0; i < intSampleSize; i++) {
				dblMean1 += rngList1[i];
				dblMean2 += rngList2[i];
			}
			
			dblMean1 /=intSampleSize;
			dblMean2 /=intSampleSize;
			Console.writeLine("Mean1[" + dblMean1 + 
							"] Mean2[" + dblMean2 + "]");
			
			if(Math.abs(dblMean1 - dblMean2) > 1e-20){
				Console.writeLine("Error");
				Assert.assertTrue("Error", false);
			}
			
			Arrays.sort(rngList1);
			Arrays.sort(rngList2);
			
			for (int i = 0; i < intSampleSize; i++) {
				double dblRng1 = rngList1[i];
				double dblRng2 = rngList2[i];
				double dblError = Math.abs(dblRng1 - dblRng2);
				if(dblError > 1e-2){
					Console.writeLine("Error");
					Assert.assertTrue("Error", false);
				}
			}
			Console.writeLine("End of test");
		}
		catch(Exception ex){
			Logger.log(ex);
			Assert.assertTrue("Error", false);
		}
	}
	
	@Test
	public void compareRand(){
		
		int intSampleSize = (int)1e6;
		RngMersenneTwister rng0 = new RngMersenneTwister(123);
		
		DateTime start = DateTime.now();
		for (int k = 0; k < 3; k++) {
			int intSeed = Math.min(1, (int)rng0.nextDouble() * 100);
			IRng rng = new RngMersenneTwister(intSeed);
			double[] rngList = new double[intSampleSize];
			for (int i = 0; i < intSampleSize; i++) {
				rngList[i] = rng.nextDouble();
			}
			rng = new RngMersenneTwister(intSeed);
			for (int i = 0; i < intSampleSize; i++) {
				double dblError = Math.abs(rngList[i] - rng.nextDouble());
				boolean blnCheck = dblError < 1e-10;
				Assert.assertTrue("Error", blnCheck);
			}
		}
		DateTime end = DateTime.now();
		Console.writeLine(
				"Total seconds rand1 = " + 
				Seconds.secondsBetween(start, end).getSeconds());

		
		start = DateTime.now();
		for (int k = 0; k < 3; k++) {
			int intSeed = Math.min(1, (int)rng0.nextDouble() * 100);
			IRng rng = new RngBase(intSeed);
			double[] rngList = new double[intSampleSize];
			for (int i = 0; i < intSampleSize; i++) {
				rngList[i] = rng.nextDouble();
			}
			rng = new RngBase(intSeed);
			for (int i = 0; i < intSampleSize; i++) {
				double dblError = Math.abs(rngList[i] - rng.nextDouble());
				if(dblError > 1e-10){
					Assert.assertTrue("Error", false);
					Console.writeLine("Error");
				}
			}
		}
		end = DateTime.now();
		Console.writeLine(
				"Total seconds rand2 = " + 
				Seconds.secondsBetween(start, end).getSeconds());
		
		
		start = DateTime.now();
		for (int k = 0; k < 3; k++) {
			int intSeed = Math.min(1, (int)rng0.nextDouble() * 100);
			IRng rng = new RngWrapper(intSeed);
			double[] rngList = new double[intSampleSize];
			for (int i = 0; i < intSampleSize; i++) {
				rngList[i] = rng.nextDouble();
			}
			rng = new RngWrapper(intSeed);
			for (int i = 0; i < intSampleSize; i++) {
				double dblError = Math.abs(rngList[i] - rng.nextDouble());
				if(dblError > 1e-10){
					Console.writeLine("Error");
					Assert.assertTrue("Error", false);
				}
			}
		}
		end = DateTime.now();
		Console.writeLine(
				"Total seconds rand3 = " + 
				Seconds.secondsBetween(start, end).getSeconds());
		
		Console.writeLine("Finish with test");
	}
	
	@Test
	public void compareBinomialDist(){
		
		//
		// get samples
		//
		int intSampleSize = (int)1e5;
		RngMersenneTwister rng = new RngMersenneTwister();
		double[] rngList = new double[intSampleSize];
		for (int i = 0; i < intSampleSize; i++) {
			rngList[i] = rng.nextUniform();
		}
		
		BinomialDistribution binomial1 = new BinomialDistribution(100, 0.1);
		BinomialDist binomial2 = new BinomialDist(100, 0.1, rng);
		
		DateTime start = DateTime.now();
		for (int i = 0; i < intSampleSize; i++) {
			double dblCurrRand1 = rngList[i]; 
			int intInv = binomial1.inverseCumulativeProbability(dblCurrRand1);
			double dblCurrRnad2 = binomial1.cumulativeProbability(intInv);
			if(Math.abs(dblCurrRand1 - dblCurrRnad2) > 0.25){
				Console.writeLine("Invalid value");
				Assert.assertTrue("Error", false);
			}
		}
		DateTime end = DateTime.now();
		Console.writeLine(
				"Total seconds bin1 = " + 
				Seconds.secondsBetween(start, end).getSeconds());
		
		start = DateTime.now();
		for (int i = 0; i < intSampleSize; i++) {
			double dblCurrRand1 = rngList[i]; 
			int intInv = binomial2.CdfInv(dblCurrRand1);
			double dblCurrRnad2 = binomial2.Cdf(intInv);
			if(Math.abs(dblCurrRand1 - dblCurrRnad2) > 0.25){
				Console.writeLine("Invalid value");
				Assert.assertTrue("Error", false);
			}
		}
		end = DateTime.now();
		Console.writeLine(
				"Total seconds bin2 = " + 
				Seconds.secondsBetween(start, end).getSeconds());
		
		start = DateTime.now();
		for (int i = 0; i < intSampleSize; i++) {
			double dblCurrRand1 = rngList[i]; 
			int intInv = binomial2.CdfInv(dblCurrRand1);
			double dblCurrRnad1 = binomial1.probability(intInv);
			double dblCurrRnad2 = binomial2.Pdf(intInv);
			if(Math.abs(dblCurrRnad1 - dblCurrRnad2) > 1e-6){
				Console.writeLine("Invalid value");
				Assert.assertTrue("Error", false);
			}
		}
		end = DateTime.now();
		Console.writeLine(
				"Total seconds bin3 = " + 
				Seconds.secondsBetween(start, end).getSeconds());		
		
	}	
	
	@Test
	public void comparePoissDist(){
		
		//
		// get samples
		//
		int intSampleSize = (int)1e5;
		RngMersenneTwister rng = new RngMersenneTwister();
		double[] rngList = new double[intSampleSize];
		
		for (int i = 0; i < intSampleSize; i++) {
			rngList[i] = rng.nextUniform();
		}
		
		PoissonDistribution poiss1 = new PoissonDistribution(100);
		PoissonDist poiss2 = new PoissonDist(100, rng);
		
		DateTime start = DateTime.now();
		for (int i = 0; i < intSampleSize; i++) {
			double dblCurrRand1 = rngList[i]; 
			int intInv = poiss1.inverseCumulativeProbability(dblCurrRand1);
			double dblCurrRnad2 = poiss1.cumulativeProbability(intInv);
			if(Math.abs(dblCurrRand1 - dblCurrRnad2) > 0.25){
				Console.writeLine("Invalid value");
				Assert.assertTrue("Error", false);
			}
		}
		DateTime end = DateTime.now();
		Console.writeLine(
				"Total seconds poiss1 = " + 
				Seconds.secondsBetween(start, end).getSeconds());
		
		start = DateTime.now();
		for (int i = 0; i < intSampleSize; i++) {
			double dblCurrRand1 = rngList[i]; 
			int intInv = poiss2.CdfInv(dblCurrRand1);
			double dblCurrRnad2 = poiss2.Cdf(intInv);
			if(Math.abs(dblCurrRand1 - dblCurrRnad2) > 0.25){
				Console.writeLine("Invalid value");
				Assert.assertTrue("Error", false);
			}
		}
		end = DateTime.now();
		Console.writeLine(
				"Total seconds poiss2 = " + 
				Seconds.secondsBetween(start, end).getSeconds());
		
		start = DateTime.now();
		for (int i = 0; i < intSampleSize; i++) {
			double dblCurrRand1 = rngList[i]; 
			int intInv = poiss2.CdfInv(dblCurrRand1);
			double dblCurrRnad1 = poiss1.probability(intInv);
			double dblCurrRnad2 = poiss2.Pdf(intInv);
			if(Math.abs(dblCurrRnad1 - dblCurrRnad2) > 1e-6){
				Console.writeLine("Invalid value");
				Assert.assertTrue("Error", false);
			}
		}
		end = DateTime.now();
		Console.writeLine(
				"Total seconds poiss3 = " + 
				Seconds.secondsBetween(start, end).getSeconds());		
		
	}

	@Test
	public void compareGamaDist()
	{
		
		//
		// get samples
		//
		int intSampleSize = (int)1e5;
		RngMersenneTwister rng = new RngMersenneTwister();
		double[] rngList = new double[intSampleSize];
		
		for (int i = 0; i < intSampleSize; i++) {
			rngList[i] = rng.nextUniform();
		}
		
		GammaDistribution gamma1 = new GammaDistribution(10, 10);
		GammaDist gamma2 = new GammaDist(10, 10, rng);
		
		DateTime start = DateTime.now();
		for (int i = 0; i < intSampleSize; i++) {
			double dblCurrRand1 = rngList[i]; 
			double dblInv = gamma1.inverseCumulativeProbability(dblCurrRand1);
			double dblCurrRnad2 = gamma1.cumulativeProbability(dblInv);
			if(Math.abs(dblCurrRand1 - dblCurrRnad2) > 1e-6){
				Console.writeLine("Invalid value");
				Assert.assertTrue("Error", false);
			}
		}
		DateTime end = DateTime.now();
		Console.writeLine(
				"Total seconds gamma1 = " + 
				Seconds.secondsBetween(start, end).getSeconds());
		
		start = DateTime.now();
		for (int i = 0; i < intSampleSize; i++) {
			double dblCurrRand1 = rngList[i]; 
			double dblInv = gamma2.CdfInv(dblCurrRand1);
			double dblCurrRnad2 = gamma2.Cdf(dblInv);
			if(Math.abs(dblCurrRand1 - dblCurrRnad2) > 1e-6){
				Console.writeLine("Invalid value");
				Assert.assertTrue("Error", false);
			}
		}
		end = DateTime.now();
		Console.writeLine(
				"Total seconds gamma2 = " + 
				Seconds.secondsBetween(start, end).getSeconds());
		
		start = DateTime.now();
		for (int i = 0; i < intSampleSize; i++) {
			double dblCurrRand1 = rngList[i]; 
			double intInv = gamma2.CdfInv(dblCurrRand1);
			double dblCurrRnad1 = gamma1.probability(intInv);
			double dblCurrRnad2 = gamma2.Pdf(intInv);
			if(Math.abs(dblCurrRnad1 - dblCurrRnad2) > 1e-6){
				Console.writeLine("Invalid value");
				Assert.assertTrue("Error", false);
			}
		}
		end = DateTime.now();
		Console.writeLine(
				"Total seconds gamma3 = " + 
				Seconds.secondsBetween(start, end).getSeconds());		
	}
	
	@Test
	public void compareLogNormalDist()
	{
		try
		{
			//
			// get samples
			//
			int intSampleSize = (int)1e5;
			RngMersenneTwister rng = new RngMersenneTwister();
			double[] rngList = new double[intSampleSize];
			
			for (int i = 0; i < intSampleSize; i++) 
			{
				rngList[i] = rng.nextUniform();
			}
			
			LogNormalDistribution ln1 = new LogNormalDistribution(10, 1);
			LogNormalDist ln2 = new LogNormalDist(10, 1, rng);
			
			DateTime start = DateTime.now();
			for (int i = 0; i < intSampleSize; i++) {
				double dblCurrRand1 = rngList[i]; 
				double dblInv = ln1.inverseCumulativeProbability(dblCurrRand1);
				double dblCurrRnad2 = ln1.cumulativeProbability(dblInv);
				if(Math.abs(dblCurrRand1 - dblCurrRnad2) > 1e-6){
					Console.writeLine("Invalid value");
					Assert.assertTrue("Error", false);
				}
			}
			DateTime end = DateTime.now();
			Console.writeLine(
					"Total seconds ln1 = " + 
					Seconds.secondsBetween(start, end).getSeconds());
			
			start = DateTime.now();
			for (int i = 0; i < intSampleSize; i++)
			{
				double dblCurrRand1 = rngList[i]; 
				double dblInv = ln2.CdfInv(dblCurrRand1);
				double dblCurrRnad2 = ln2.Cdf(dblInv);
				if(Math.abs(dblCurrRand1 - dblCurrRnad2) > 1e-6)
				{
					Console.writeLine("Invalid value");
					Assert.assertTrue("Error", false);
				}
			}
			end = DateTime.now();
			Console.writeLine(
					"Total seconds ln2 = " + 
					Seconds.secondsBetween(start, end).getSeconds());
			
			start = DateTime.now();
			for (int i = 0; i < intSampleSize; i++) {
				double dblCurrRand1 = rngList[i]; 
				double intInv = ln2.CdfInv(dblCurrRand1);
				double dblCurrRnad1 = ln1.density(intInv);
				double dblCurrRnad2 = ln2.Pdf(intInv);
				if(Math.abs(dblCurrRnad1 - dblCurrRnad2) > 1e-6)
				{
					Console.writeLine("Invalid value");
					Assert.assertTrue("Error", false);
				}
			}
			end = DateTime.now();
			Console.writeLine(
					"Total seconds ln3 = " + 
					Seconds.secondsBetween(start, end).getSeconds());	
		}
		catch(Exception ex)
		{
			Logger.log(ex);
		}
	}
	
}
