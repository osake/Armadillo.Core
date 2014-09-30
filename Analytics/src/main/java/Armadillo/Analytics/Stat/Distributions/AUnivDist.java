package Armadillo.Analytics.Stat.Distributions;

import Armadillo.Analytics.Stat.Random.IRng;

public abstract class AUnivDist implements IDist
{
    /// <summary>
    /// Random number generator
    /// </summary>
    protected IRng m_rng;

    public AUnivDist(IRng rng)
    {
        m_rng = rng;
    }

}