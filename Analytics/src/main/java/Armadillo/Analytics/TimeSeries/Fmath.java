package Armadillo.Analytics.TimeSeries;

public class Fmath {

	public static double[] selectionSort(double[] aa) 
	{
        int index = 0;
        int lastIndex = -1;
        int n = aa.length;
        double hold = 0.0D;
        double[] bb = new double[n];
        for (int i = 0; i < n; i++)
        {
            bb[i] = aa[i];
        }

        while (lastIndex != n - 1)
        {
            index = lastIndex + 1;
            for (int i = lastIndex + 2; i < n; i++)
            {
                if (bb[i] < bb[index])
                {
                    index = i;
                }
            }
            lastIndex++;
            hold = bb[index];
            bb[index] = bb[lastIndex];
            bb[lastIndex] = hold;
        }
        return bb;
     }
	
	public static double[] selectionSort(Double[] aa) 
	{
        int index = 0;
        int lastIndex = -1;
        int n = aa.length;
        double hold = 0.0D;
        double[] bb = new double[n];
        for (int i = 0; i < n; i++)
        {
            bb[i] = aa[i];
        }

        while (lastIndex != n - 1)
        {
            index = lastIndex + 1;
            for (int i = lastIndex + 2; i < n; i++)
            {
                if (bb[i] < bb[index])
                {
                    index = i;
                }
            }
            lastIndex++;
            hold = bb[index];
            bb[index] = bb[lastIndex];
            bb[lastIndex] = hold;
        }
        return bb;
     }	

	public static boolean isOdd(int x) 
	{
        boolean test = true;
        if (x%2 == 0.0D)
        {
            test = false;
        }
        return test;
	}

	public static float[] selectionSort(float[] aa) 
	{
        int index = 0;
        int lastIndex = -1;
        int n = aa.length;
        float hold = 0.0F;
        float[] bb = new float[n];
        for (int i = 0; i < n; i++)
        {
            bb[i] = aa[i];
        }

        while (lastIndex != n - 1)
        {
            index = lastIndex + 1;
            for (int i = lastIndex + 2; i < n; i++)
            {
                if (bb[i] < bb[index])
                {
                    index = i;
                }
            }
            lastIndex++;
            hold = bb[index];
            bb[index] = bb[lastIndex];
            bb[lastIndex] = hold;
        }
        return bb;
       }

	public static int[] selectionSort(int[] aa) 
	{
        int index = 0;
        int lastIndex = -1;
        int n = aa.length;
        int hold = 0;
        int[] bb = new int[n];
        for (int i = 0; i < n; i++)
        {
            bb[i] = aa[i];
        }

        while (lastIndex != n - 1)
        {
            index = lastIndex + 1;
            for (int i = lastIndex + 2; i < n; i++)
            {
                if (bb[i] < bb[index])
                {
                    index = i;
                }
            }
            lastIndex++;
            hold = bb[index];
            bb[index] = bb[lastIndex];
            bb[lastIndex] = hold;
        }
        return bb;
      }

}
