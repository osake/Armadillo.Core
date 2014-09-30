package Armadillo.Analytics.TextMining;

import Armadillo.Core.Text.TokenWrapper;

public class TagLinkPrefix
{
    public static double GetStringMetric(
        TokenWrapper T, 
        TokenWrapper U)
    {
        int intGoal = Math.min(2, Math.min(T.Token.length(), U.Token.length()));
        if (intGoal == 0)
        {
            return 0.0;
        }
        //int matched = 0;
        for (int i = 0; i < intGoal; i++)
        {
            char tChar = T.charAt(i);
            char uChar = U.charAt(i);
            if (tChar == uChar)
            {
                return 1.0;
            }
        }
        return 0.0;
    }
}
