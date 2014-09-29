package Armadillo.Core.Text;

import java.util.ArrayList;
import java.util.List;

import Armadillo.Core.KeyValuePair;
import Armadillo.Core.Logger;
import Armadillo.Core.ParserHelper;
import Armadillo.Core.Text.StringHelper;
import Armadillo.Core.Text.TokeniserHelper;

public class Tokeniser {
    
    public static String[] Tokenise(
        String str,
        boolean blnIgnoreDigits)
    {
        try
        {
            if(StringHelper.IsNullOrEmpty(str))
            {
                return new String[0];
            }

            String[] substrings = TokeniserHelper.tokenise(str, "\t\b\n ");

            List<String> substringsList = new ArrayList<String>(substrings.length);
            for (int i = 0; i < substrings.length; i++)
            {
                String strCurrStr = substrings[i];
                if (!StringHelper.IsNullOrEmpty(substrings[i]))
                {
                    strCurrStr = StringHelper.RemoveCommonSymbols(strCurrStr);
                    if (!StringHelper.IsNullOrEmpty(strCurrStr))
                    {
                        double[] dblParsedNumber = new double[1];
                        if (Character.isDigit(strCurrStr.charAt(0)) &&
                            ParserHelper.IsNumeric(strCurrStr, dblParsedNumber))
                        {
                            substringsList.add(dblParsedNumber[0] + "");
                        }
                        else
                        {
                            List<KeyValuePair<String, Boolean>> digitLeters = SplitDigitLetters(strCurrStr);
                            if (digitLeters != null &&
                                digitLeters.size() > 0)
                            {
                                for (int j = 0; j < digitLeters.size(); j++)
                                {
                                    KeyValuePair<String, Boolean> kvp = digitLeters.get(j);
                                    if (kvp.getValue())
                                    {
                                        if (ParserHelper.IsNumeric(kvp.getKey(), dblParsedNumber))
                                        {
                                            substringsList.add(dblParsedNumber[0] + "");
                                        }
                                        else
                                        {
                                            TokeniseAndAdd(kvp.getKey(), substringsList, blnIgnoreDigits);
                                        }
                                    }
                                    else
                                    {
                                        TokeniseAndAdd(kvp.getKey(), substringsList, blnIgnoreDigits);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return substringsList.toArray(new String[0]);
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return new String[0];
    }

    private static void TokeniseAndAdd(
        String str, 
        List<String> substringsList,
        boolean blnIgnoreDigits)
    {
        String[] tokens = Tokenise0(str, blnIgnoreDigits);
        if(tokens != null &&
            tokens.length > 0 &&
            !(tokens.length == 1 &&
            StringHelper.IsNullOrEmpty(tokens[0])))
        {
        	for(String strCurrTok : tokens){
        		substringsList.add(strCurrTok);
        	}
        }
    }

    private static String[] GetEmptyTokenSet()
    {
        try
        {
            String[] arr = new String[1];
            arr[0] = "";
            return arr;
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return new String[0];
    }

    public static TokenWrapper[] TokeniseAndWrap(
        String strInput,
        String[] strStopWordsArr)
    {
        try
        {
            if (strStopWordsArr == null)
            {
                return TokeniseAndWrap(
                    strInput);
            }
            String[] tokens = Tokenise(strInput,
                                       strStopWordsArr,
                                       false);
            return WrapTokens(tokens);
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return new TokenWrapper[0];
    }

    public static String[] Tokenise(
        String strInput,
        String[] strStopWordsArr)
    {
        return Tokenise(
            strInput,
            strStopWordsArr,
            false);
    }

    public static String[] Tokenise(
        String strInput,
        String[] strStopWordsArr,
        boolean blnIgnoreNumbers)
    {
        try
        {
            if (strStopWordsArr == null)
            {
                return Tokenise(strInput,
                                blnIgnoreNumbers);
            }

            String[] tokenArr = Tokenise(
                strInput, 
                blnIgnoreNumbers);

            ArrayList<String> tokenList =
                new ArrayList<String>(tokenArr.length);

            for (String strToken : tokenArr)
            {
                boolean blnAddToken = true;
                for (String strStopWord : strStopWordsArr)
                {
                    if (!strStopWord.equals(""))
                    {
                        if (strStopWord.equals(strToken))
                        {
                            blnAddToken = false;
                        }
                    }
                }
                if (blnAddToken)
                {
                    tokenList.add(strToken);
                }
            }
            tokenArr = tokenList.toArray(new String[0]);
            return tokenArr;
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return new String[0];
    }


    public static TokenWrapper[] TokeniseAndWrap(String strInput)
    {
        String[] tokens = Tokenise(strInput, false);
        TokenWrapper[] tokenWraps = WrapTokens(tokens);
        return tokenWraps;
    }

    public static TokenWrapper[] WrapTokens(String[] tokens)
    {
        try
        {
        	TokenWrapper[] tokenWraps = new TokenWrapper[tokens.length];
            for (int i = 0; i < tokens.length; i++)
            {
                String strToken = tokens[i];
                tokenWraps[i] = new TokenWrapper(strToken);
            }
            return tokenWraps;
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return new TokenWrapper[0];
    }

    public static String[] Tokenise(
        String strInput)
    {
        return Tokenise(strInput, false);
    }

    public static List<KeyValuePair<String,Boolean>> SplitDigitLetters(
        String strInput)
    {
        try
        {
            if (StringHelper.IsNullOrEmpty(strInput))
            {
                return null;
            }
            strInput = strInput.toLowerCase();
            List<KeyValuePair<String, Boolean>> tokens = new ArrayList<KeyValuePair<String, Boolean>>();
            int cursor = 0;
            int length = strInput.length();
            while (cursor < length)
            {
                char ch = strInput.charAt(cursor);
                if (ch == ' ')
                {
                    cursor++;
                }
                else if (!Character.isDigit(ch))
                {
                    String word = "";
                    while (cursor < length &&
                           !Character.isDigit(strInput.charAt(cursor)))
                    {
                        word += strInput.charAt(cursor);
                        cursor++;
                    }
                    tokens.add(new KeyValuePair<String, Boolean>(word, false));
                }
                else if (!Character.isLetter(ch))
                {
                    String word = "";
                    while (cursor < length &&
                           !Character.isLetter(strInput.charAt(cursor)))
                    {
                        word += strInput.charAt(cursor);
                        cursor++;
                    }
                    tokens.add(new KeyValuePair<String, Boolean>(word, true));
                }
                else
                {
                    cursor++;
                }
            }
            if (tokens.size() == 0)
            {
                return null;
            }
            return tokens;
        }
        catch (Exception ex)
        {
            Logger.log(ex);
        }
        return null;
    }


    private static String[] Tokenise0(
        String strInput,
        boolean blnIgnoreNumbers)
    {
    	
    
        try
        {
            if (StringHelper.IsNullOrEmpty(strInput))
            {
            	String[] tokenArray = new String[1];
                tokenArray[0] = "";
                return tokenArray;
            }
            strInput = strInput.toLowerCase();
            List<String> tokens = new ArrayList<String>();
            int cursor = 0;
            int length = strInput.length();
            while (cursor < length)
            {
                char ch = strInput.charAt(cursor);
                if (ch == ' ')
                {
                    cursor++;
                }
                else if (Character.isLetter(ch))
                {
                    String word = "";
                    while (cursor < length &&
                           Character.isLetter(strInput.charAt(cursor)))
                    {
                        word += strInput.charAt(cursor);
                        cursor++;
                    }
                    tokens.add(word);
                }
                else if (Character.isDigit(ch))
                {
                    String word = "";
                    while (cursor < length &&
                           Character.isDigit(strInput.charAt(cursor)))
                    {
                        word += strInput.charAt(cursor);
                        cursor++;
                    }
                    tokens.add(word);
                }
                else
                {
                    cursor++;
                }
            }
            if (tokens.size() == 0)
            {
                return GetEmptyTokenSet();
            }
            ArrayList<String> outTokens = new ArrayList<String>();
            for (int i = 0; i < tokens.size(); i++)
            {
                String strToken = tokens.get(i);
                if (!StringHelper.IsNullOrEmpty(strToken))
                {
                    if ((blnIgnoreNumbers &&
                         StringHelper.AllLetters(strToken)) ||
                        !blnIgnoreNumbers)
                    {
                        outTokens.add(strToken);
                    }
                }
            }
            return outTokens.toArray(new String[0]);
            
        }
        catch(Exception ex)
        {
            Logger.log(ex);
        }
        return new String[0];
    }
}