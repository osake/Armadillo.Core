package Armadillo.Analytics.Optimisation.Base.Solvers.NelderMead.DataStructures;

import java.util.Comparator;

public class NmVertexComparator implements Comparator<ANmVertex>
{
    /// <summary>
    ///   Sort vertex by values
    /// </summary>
    /// <param name = "o">
    ///   Vertext to compare with
    /// </param>
    /// <returns>
    ///   Compare value
    /// </returns>
	@Override
    public int compare(ANmVertex a, ANmVertex o)
    {
       double difference = a.Value - o.Value;
        if (difference < 0)
        {
            return 1;
        }
        if (difference > 0)
        {
            return -1;
        }
        return 0;
    }
}