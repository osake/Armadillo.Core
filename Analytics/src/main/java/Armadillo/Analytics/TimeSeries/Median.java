package Armadillo.Analytics.TimeSeries;

public class Median 
{
    // MEDIANS
    // Median of a 1D array of double, aa


    // Median of a 1D array of long, aa
    public static long median(long[] aa)
    {
        int n = aa.length;
        int nOverTwo = n/2;
        long med;
        long[] bb = (long[]) aa.clone();
        if (Fmath.isOdd(n))
        {
            med = bb[nOverTwo];
        }
        else
        {
            med = (bb[nOverTwo - 1] + bb[nOverTwo])/2;
        }
        return med;
    }

    // Median of a 1D array of doubles, aa
    public static double median(Double[] aa)
    {
        int n = aa.length;
        int nOverTwo = n/2;
        double med = 0.0D;
        double[] bb = Fmath.selectionSort(aa);
        if (Fmath.isOdd(n))
        {
            med = bb[nOverTwo];
        }
        else
        {
            med = (bb[nOverTwo - 1] + bb[nOverTwo])/2.0D;
        }

        return med;
    }
    
    // Median of a 1D array of doubles, aa
    public static double median(double[] aa)
    {
        int n = aa.length;
        int nOverTwo = n/2;
        double med = 0.0D;
        double[] bb = Fmath.selectionSort(aa);
        if (Fmath.isOdd(n))
        {
            med = bb[nOverTwo];
        }
        else
        {
            med = (bb[nOverTwo - 1] + bb[nOverTwo])/2.0D;
        }

        return med;
    }


    // Median of a 1D array of floats, aa
    public static float median(float[] aa)
    {
        int n = aa.length;
        int nOverTwo = n/2;
        float med = 0.0F;
        float[] bb = Fmath.selectionSort(aa);
        if (Fmath.isOdd(n))
        {
            med = bb[nOverTwo];
        }
        else
        {
            med = (bb[nOverTwo - 1] + bb[nOverTwo])/2.0F;
        }

        return med;
    }

    // Median of a 1D array of int, aa
    public static double median(int[] aa)
    {
        int n = aa.length;
        int nOverTwo = n/2;
        double med = 0.0D;
        int[] bb = Fmath.selectionSort(aa);
        if (Fmath.isOdd(n))
        {
            med = bb[nOverTwo];
        }
        else
        {
            med = (bb[nOverTwo - 1] + bb[nOverTwo])/2.0D;
        }

        return med;
    }

}
