package Armadillo.Analytics.TextMining;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import Armadillo.Core.Console;
import Armadillo.Core.Text.TokenWrapper;

public class StopWords
{
    private static final String STOP_WORDS_FILE_NAME = "C:\\HC\\Config\\TextMining\\StopWords.txt";


    public HashMap<String, String> StopWordsSet;
    public static StopWords OwnInstance;

    static 
    {
        OwnInstance = new StopWords();
    }

    public StopWords(){
    	this(STOP_WORDS_FILE_NAME);
    }

    public StopWords(String strStopWordsFile)
    {
        LoadStopWords(strStopWordsFile);
    }

    public boolean Contains(String strToken)
    {
        return StopWordsSet.containsKey(strToken);
    }

    public static void CleanStopWords(
        ADocument document,
        double dblPercentile,
        int[] columns)
    {
        for (int i = 0; i < columns.length; i++)
        {
        	int intColId = columns[i];
            //
            // get token frequencies
            //
        	HashMap<TokenWrapper, Double> tokenFreq =
                new HashMap<TokenWrapper, Double>();
            for (Entry<TokenWrapper, Integer> keyValuePair :
                document.TokenStatistics().getTokenFrequencies().get(intColId).entrySet())
            {
                double dblTotalValue;
                if(!tokenFreq.containsKey(keyValuePair.getKey()))
                {
                    dblTotalValue = keyValuePair.getValue();
                }
                else
                {
                    dblTotalValue = tokenFreq.get(keyValuePair.getKey()) + keyValuePair.getValue();
                }
                tokenFreq.put(keyValuePair.getKey(), dblTotalValue);
            }

            //
            // get top percentile words
            //
            List<Entry<TokenWrapper, Double>> valuePairs = new ArrayList<Entry<TokenWrapper, Double>>();
            double dblSumValues = 0;
            for(Entry<TokenWrapper, Double> kvp : tokenFreq.entrySet()){
            	valuePairs.add(kvp);
            	dblSumValues += kvp.getValue();
            }
            
			Collections.sort(valuePairs, new Comparator<Entry<TokenWrapper, Double>>() {

				@Override
				public int compare(
						Entry<TokenWrapper, Double> item1,
						Entry<TokenWrapper, Double> item2) {
					
			        if(item1.getValue() > 
			        	item2.getValue()){
			        	return -1;
			        }
			        if(item1.getValue() < 
			        		item2.getValue()){
			        	return 1;
			        }
			        
			        return 0;
				}
			});
            
            
            //valuePairs = valuePairs.OrderBy(x => -x.Value).ToList();
            double dblAcumValue = 0;
            HashMap<TokenWrapper, Object> freqWords = new HashMap<TokenWrapper, Object>();
            for (Entry<TokenWrapper, Double> keyValuePair : valuePairs)
            {
                dblAcumValue += keyValuePair.getValue() / dblSumValues;
                if (dblAcumValue <= dblPercentile)
                {
                    freqWords.put(keyValuePair.getKey(), new Object());
                    Console.writeLine("Removing common word " + 
                        keyValuePair.getKey() + " = " +
                        keyValuePair.getValue());
                }
                else
                {
                    break;
                }
            }

            //
            // remote frequent words
            //
            for (int i2 = 0; i2 < document.Data.length(); i2++)
            {
            	ArrayList<TokenWrapper> selectedTokens = new ArrayList<TokenWrapper>();
                for (TokenWrapper strToken : document.Data.getDataArray()[i2].Columns[intColId])
                {
                    if(!freqWords.containsKey(strToken))
                    {
                        selectedTokens.add(strToken);
                    }
                }
                document.Data.getDataArray()[i2].Columns[intColId] = selectedTokens.toArray(new TokenWrapper[0]);
            }
        }
    }

    /// <summary>
    /// Load the stopwords from file to the hashtable where they are indexed.
    /// </summary>
    /// <param name="strStopWordsFile"></param>
    private void LoadStopWords(String strStopWordsFile)
    {
        // Initialize hashtable to proper size given known number of
        // stopwords in the file and a default 75% load factor with
        // 10 extra slots for spare room.
        StopWordsSet = new HashMap<String, String>();
        try
        {
            // Open stopword file for reading
        	BufferedReader sr = new BufferedReader(new FileReader(strStopWordsFile));
            // Read in stopwords, one per line, until file is empty
            String line;
            while ((line = sr.readLine()) != null)
            {
                // Index word into the hashtable with
                // the default empty String as a "dummy" value.
                StopWordsSet.put(line, "");
            }
            sr.close();
        }
        catch (Exception ex)
        {
            Console.writeLine("\nCould not load stopwords file: " + strStopWordsFile + ". " +
                ex);
        }
    }
}
