package Armadillo.Analytics;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.junit.Assert;
import org.junit.Test;

import Armadillo.Analytics.Stat.Histogram;
import Armadillo.Analytics.Stat.Distributions.BetaDist;
import Armadillo.Analytics.Stat.Distributions.GammaDist;
import Armadillo.Analytics.Stat.Random.IRanNumGenerator;
import Armadillo.Analytics.Stat.Random.RngMersenneTwister;
import Armadillo.Core.Console;

public class BetaDistributionTests 
{

	@Test
	public void TestBetaSamplingBenchmark(){
		int intSize = (int)1e6;
		double dblAlpha = 2;
		double dblBeta = 5;
		final IRanNumGenerator rng = new RngMersenneTwister();
		BetaDist betaDist = new BetaDist(dblAlpha, dblBeta);
		DateTime logTime = DateTime.now();
/*		for (int i = 0; i < intSize; i++) {
			double dblGamma11 = GammaDist.NextDoubleStatic(dblAlpha, 1, rng);
			double dblGamma2 = GammaDist.NextDoubleStatic(dblBeta, 1, rng);
			double dblCurrBeta = dblGamma11 / (dblGamma11  + dblGamma2);
		} 
*/		
		Console.writeLine("Secs1 = " + Seconds.secondsBetween(
				logTime, 
				DateTime.now()).getSeconds());
		
		logTime = DateTime.now();
		for (int i = 0; i < intSize; i++) {
			betaDist.NextDouble(rng);
		}
		Console.writeLine("Secs2 = " + Seconds.secondsBetween(logTime, DateTime.now()).getSeconds());
	}
	
	@Test
	public void TestBetaSampling(){
		double dblAlpha = 2;
		double dblBeta = 5;
		//GammaDist gamma;
		final IRanNumGenerator rng = new RngMersenneTwister();
		//BetaDist betaDist = new BetaDist(dblAlpha, dblBeta);
		//int intSampleSize = 5;
		double[] array = new double[(int)5e6];
		//double[] arraySum = new double[array.length];
		double dblMean = 0;
		double dblMeanSum = 0;
		//int intCounter = 0;
		for (int i = 0; i < array.length; i++) {
//			double dblGamma11 = GammaDist.NextDoubleStatic(intSampleSize * dblAlpha, 1, rng);
//			double dblGamma12 = GammaDist.NextDoubleStatic(dblAlpha, 1, rng);
//			double dblGamma2 = GammaDist.NextDoubleStatic(dblBeta, 1, rng);
//			double dblCurrBeta = dblGamma11 / ((dblGamma11 / intSampleSize) + dblGamma2);
			
			//double dblBetaSum = 0;
			//double dblBetaSum2 = 0;
			//for (int j = 0; j < intSampleSize; j++) {
				//double dblCurrrRng = betaDist.NextDouble(rng);
				//dblBetaSum += dblCurrrRng;
				double dblGamma11 = GammaDist.NextDoubleStatic(dblAlpha, 1, rng);
				//double dblGamma12 = GammaDist.NextDoubleStatic(dblAlpha, 1, rng);
				double dblGamma2 = GammaDist.NextDoubleStatic(dblBeta, 1, rng);
				double dblCurrBeta = dblGamma11 / (dblGamma11  + dblGamma2);
			//}
			
			//arraySum[i] = dblBetaSum; 
			array[i] = dblCurrBeta;
			dblMean += array[i];
			//dblMeanSum += arraySum[i];
		}
		
		dblMean = dblMean / array.length;
		dblMeanSum = dblMeanSum / array.length;
		double dblSumSq = 0;
		//double dblSumSqSum = 0;
		for (int i = 0; i < array.length; i++) {
			dblSumSq += Math.pow(array[i] - dblMean, 2);
			//dblSumSqSum += Math.pow(arraySum[i] - dblMeanSum, 2);
		}
		double dblVar = dblSumSq / (array.length - 1);
		//double dblVarSum = dblSumSqSum / (array.length - 1);
		
		double dblValidator = dblMean*(1.0 - dblMean);
		double dbl = (dblValidator / dblVar) - 1.0;
		double dblAlpha2 = dblMean * dbl;
		double dblBeta2 = (1.0 - dblMean) *dbl;
		double dblAlphaDiff = Math.abs(dblAlpha - dblAlpha2);
		Assert.assertTrue(dblAlphaDiff < 1e-2);

		//double dblValidatorSum = dblMeanSum*(1.0 - dblMeanSum);
		//double dblSum = (dblValidatorSum / dblVarSum) - 1.0;
		//double dblAlpha2Sum = dblMeanSum * dblSum;
		//double dblBeta2Sum = (1.0 - dblMeanSum) *dblSum;
		
		
		double dblBetaDiff = Math.abs(dblBeta - dblBeta2);
		Assert.assertTrue(dblBetaDiff < 1e-2);
		
		int intBins = 50;
		double[][] histogram = Histogram.GetHistogram(array, intBins);
		for (int i = 0; i < histogram.length; i++) {
			Console.writeLine(
					histogram[i][0] + "," + 
							histogram[i][1]);
		}
		
//		Console.writeLine("Beta:");
//		histogram = Histogram.GetHistogram(arraySum, intBins);
//		for (int i = 0; i < histogram.length; i++) {
//			Console.writeLine(
//					histogram[i][0] + "," + 
//							histogram[i][1]);
//		}		
	}
}
