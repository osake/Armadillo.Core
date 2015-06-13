package Armadillo.Analytics.TextMining;

import Armadillo.Core.Text.TokenWrapper;

public class Soundex 
{
    private static int m_soundexlength = 6;
    private TagLinkTokenCheap m_stringMetric;

    public Soundex()
    {
        m_stringMetric = new TagLinkTokenCheap();
    }

    public double GetStringMetric(
        TokenWrapper t,
        TokenWrapper u)
    {
    	TokenWrapper code1 = new TokenWrapper(CalcSoundEx(t.Token));
    	TokenWrapper code2 = new TokenWrapper(CalcSoundEx(u.Token));
        return m_stringMetric.GetRawMetric(code1, code2);
    }

    public static String CalcSoundEx(String wordString)
    {
        //ensure soundexLen is in a valid range
        if (m_soundexlength > 10)
        {
            m_soundexlength = 10;
        }
        if (m_soundexlength < 4)
        {
            m_soundexlength = 4;
        }

        //check for empty input
        if (wordString.length() == 0)
        {
            return ("");
        }

        //remove case
        wordString = wordString.toUpperCase();

        /* Clean and tidy
    */
        String wordStr = wordString;
        wordStr = wordStr.replace("[^A-Z]", " "); // rpl non-chars w space
        wordStr = wordStr.replace("\\s+", ""); // remove spaces

        //check for empty input again the previous clean and tidy could of shrunk it to zero.
        if (wordStr.length() == 0)
        {
            return ("");
        }

        /* The above improvements
     * may change this first letter
    */
        char firstLetter = wordStr.charAt(0);

        // uses the assumption that enough valid characters are in the first 4 times the soundex required length
        if (wordStr.length() > (m_soundexlength*4) + 1)
        {
            wordStr = "-" + wordStr.substring(1, m_soundexlength*4);
        }
        else
        {
            wordStr = "-" + wordStr.substring(1);
        }
        // Begin Classic SoundEx
        /*
    1) B,P,F,V
    2) C,S,K,G,J,Q,X,Z
    3) D,T
    4) L
    5) M,N
    6) R
    */
        wordStr = wordStr.replace("[AEIOUWH]", "0");
        wordStr = wordStr.replace("[BPFV]", "1");
        wordStr = wordStr.replace("[CSKGJQXZ]", "2");
        wordStr = wordStr.replace("[DT]", "3");
        wordStr = wordStr.replace("[L]", "4");
        wordStr = wordStr.replace("[MN]", "5");
        wordStr = wordStr.replace("[R]", "6");

        // Remove extra equal adjacent digits
        int wsLen = wordStr.length();
        char lastChar = '-';
        String tmpStr = "-";
        for (int i = 1; i < wsLen; i++)
        {
            char curChar = wordStr.charAt(i);
            if (curChar != lastChar)
            {
                tmpStr += curChar;
                lastChar = curChar;
            }
        }
        wordStr = tmpStr;
        wordStr = wordStr.substring(1); /* Drop first letter code   */
        wordStr = wordStr.replace("0", ""); /* remove zeros             */
        wordStr += "000000000000000000"; /* pad with zeros on right  */
        wordStr = firstLetter + "-" + wordStr; /* Add first letter of word */
        wordStr = wordStr.substring(0, m_soundexlength); /* size to taste     */
        return (wordStr);
    }
}
