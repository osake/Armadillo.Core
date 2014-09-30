package Armadillo.Analytics.Stat;

public class Histogram {
    
	//
	// do not get confused with the bean categories.
	// delta = (max-min) / numBeans
	// then delta1 = delta, delta2 = delta1 + delta,...
	// the bean counter start then from range [0 to delta1]... [deltaN to max]
	//
	public static double[][] GetHistogram(
			double[] dblDataArray, 
			int intBinCount)
    {
		//
        // get Min and Max values
		//
        double dblMinValue = Double.MAX_VALUE;
        double dblMaxValue = -Double.MAX_VALUE;
        for (int i = 0; i < dblDataArray.length; i++) 
        {
        	dblMinValue = Math.min(dblMinValue, dblDataArray[i]);
            dblMaxValue = Math.max(dblMaxValue, dblDataArray[i]);
		}
        return GetHistogram(
            dblDataArray,
            intBinCount,
            dblMinValue,
            dblMaxValue);
    }

    public static double[][] GetHistogram(
        double[] dblDataArray,
        int intBinCount,
        double dblMinValue,
        double dblMaxValue)
    {
        double[][] dblHistogramArray = new double[intBinCount][];
        double dblDelta = (dblMaxValue - dblMinValue)/intBinCount;
        double dblCurrentBinValue = dblMinValue;
        // load bin values
        for (int i = 0; i < intBinCount; i++)
        {
        	double[] currBin = new double[2];
        	dblHistogramArray[i] = currBin;
            dblHistogramArray[i][0] = dblCurrentBinValue;
            dblCurrentBinValue += dblDelta;
        }

        for (int i = 0; i < dblDataArray.length; i++)
        {
            double dblDataValue = dblDataArray[i];
            double dblIndex = (dblDataValue - dblMinValue - 0.000000001)/dblDelta;
            int intBinIndex = -1;
            if (dblIndex > Integer.MAX_VALUE)
            {
                intBinIndex = intBinCount - 1;
            }
            else
            {
                intBinIndex = (int) ((dblDataValue - dblMinValue - 0.000000001)/dblDelta);
                if (intBinIndex < 0)
                {
                    // by default set the initial bin as zero
                    intBinIndex = 0;
                }
            }
            if (intBinIndex >= intBinCount)
            {
                intBinIndex = intBinCount - 1;
            }
            dblHistogramArray[intBinIndex][1]++;
        }
        return dblHistogramArray;
    }


}
