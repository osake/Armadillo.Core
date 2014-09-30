package Armadillo.Analytics.Stat.Random;

import java.util.Random;

public class RandomFactory {

    /// <summary>
    /// This Object is wsed to seed new 
    /// random number generators
    /// </summary>
    private static final Random m_globalRandom = new Random(
        12345);

    private static final Object m_lockObject = new Object();


    /// <summary>
    /// Create new random number generator
    /// </summary>
    /// <returns>
    /// Random number generator
    /// </returns>
    public static RngBase Create()
    {
        return new RngBase();
    }

	static int getRandomSeed() {
		int intSeed;
        synchronized (m_lockObject)
        {
            intSeed = m_globalRandom.nextInt();
        }
		return intSeed;
	}

    public static RngBase Create(int intSeed)
    {
        return new RngBase(intSeed);
    }

}
