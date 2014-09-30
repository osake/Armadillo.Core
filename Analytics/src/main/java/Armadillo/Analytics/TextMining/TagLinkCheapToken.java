package Armadillo.Analytics.TextMining;

import Armadillo.Core.Text.TokenWrapper;

public class TagLinkCheapToken {

    private double m_dblAlfa;
    private int m_intWindowSize;


    public TagLinkCheapToken()
    {
        m_intWindowSize = TextMiningConstants.CHEAP_LINK_TOKEN_WINDOW;
        m_dblAlfa = TextMiningConstants.DBL_CHEAP_LINK_TOKEN_ALPHA;
    }

    public double GetStringMetric(TokenWrapper t, TokenWrapper u)
    {
        if (t.Token.length() == 0 || u.Token.length() == 0)
        {
            return 0.0;
        }
        // get min string length
        int uLength = u.Token.length();
        int minLength = Math.min(t.Token.length(), uLength) - 1;
        int sampleSize = (int) ((minLength)*m_dblAlfa);
        int partition = (int)((minLength) / ((double)(sampleSize - 1)));
        int actualPosition = 0;
        double matched = 0;
        if (sampleSize == 0)
        {
            sampleSize++;
        }
        if (partition == 0)
        {
            partition++;
        }
        for (int i = 0; i < sampleSize; i++)
        {
            char tChar = t.charAt(actualPosition);
            for (int j = Math.max(actualPosition - m_intWindowSize, 0);
                 j < Math.min(actualPosition + m_intWindowSize + 1, uLength);
                 j++)
            {
                char uChar = u.charAt(j);
                if (tChar == uChar)
                {
                    matched++;
                    break;
                }
            }
            actualPosition += partition;
        }
        return (matched)/sampleSize;
    }
}
