package Armadillo.Analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;

import org.junit.Test;

import Armadillo.Analytics.TextMining.DataWrapper;
import Armadillo.Analytics.TextMining.MstDistanceObj;
import Armadillo.Analytics.TextMining.Searcher;
import Armadillo.Analytics.TextMining.StopWords;
import Armadillo.Analytics.TextMining.TagLink;
import Armadillo.Core.Console;
import Armadillo.Core.Text.TokenWrapper;

public class TextMiningTests {

	@Test
	public void testDocument(){
		
		DataWrapper dataWrapper = getDataWrapper();
		Assert.assertTrue(dataWrapper.length() == getItemList().size());
	}

	@Test
	public void testSearcher()
	{
		DataWrapper dataWrapper = getDataWrapper();
		Searcher searcher = new Searcher(dataWrapper);
		List<MstDistanceObj> results = searcher.Search("Jose Horacio Camacho Hernandez");
		
		List<String> itemsList = getItemList();
		if(results != null && results.size() > 0)
		{
			for(MstDistanceObj mstDistanceObj : results)
			{
				Console.writeLine("result = " + itemsList.get(mstDistanceObj.Y) + ". Score = " +
						mstDistanceObj.Score);
			}
		}
		Assert.assertTrue(dataWrapper.length() - 1 == results.size());
	}
	
	@Test
	public void testSearcher2()
	{
		DataWrapper dataWrapper = getDataWrapper();
		Searcher searcher = new Searcher(dataWrapper);
		List<MstDistanceObj> results = searcher.Search("test2");
		
		List<String> itemsList = getItemList();
		if(results != null && results.size() > 0)
		{
			for(MstDistanceObj mstDistanceObj : results)
			{
				Console.writeLine("result = " + itemsList.get(mstDistanceObj.Y) + ". Score = " +
						mstDistanceObj.Score);
			}
		}
		Assert.assertTrue(results.size() == 1);
	}
	
	
	private DataWrapper getDataWrapper()
	{
		List<String> itemsList = getItemList();
		DataWrapper dataWrapper = new DataWrapper(itemsList, ',');
		return dataWrapper;
	}

	private List<String> getItemList() 
	{
		List<String> itemsList = new ArrayList<String>();
		itemsList.add("Horacio Camacho");
		itemsList.add("Camacho Horacio");
		itemsList.add("Camacho Horatio");
		itemsList.add("Jose Hernandez");
		itemsList.add("test test");
		itemsList.add("Jose horatio Hernandez");
		return itemsList;
	}
	
	@Test
	public void testStopWords(){
		Assert.assertTrue(StopWords.OwnInstance.Contains("the"));
	}
	
	@Test
	public void testTagLink(){
		TagLink tagLink = new TagLink();
		String strT = "Horacio Camacho";
		String strU = "Camacho Horacio";
		double dblScore = tagLink.GetStringMetric(strT, strU);
		Assert.assertTrue(dblScore > 0.99);
		strT = "Horacio Camacho";
		strU = "Camacho Horatio";
		dblScore = tagLink.GetStringMetric(strT, strU);
		Assert.assertTrue(dblScore > 0.9);
		strT = "Horacio Camacho";
		strU = "Jose Hernandez";
		dblScore = tagLink.GetStringMetric(strT, strU);
		Assert.assertTrue(dblScore < 0.1);
		strT = "Horacio Camacho Hdez";
		strU = "Jose horatio Hernandez";
		dblScore = tagLink.GetStringMetric(strT, strU);
		Assert.assertTrue(dblScore > 0.35);
	}
	
	@Test
	public void testTokenWrapper(){
		
		HashMap<TokenWrapper, Object> tokenMap = new HashMap<TokenWrapper, Object>();
		tokenMap.put(new TokenWrapper("test"), new Object());
		boolean blnContainsKey = tokenMap.containsKey(new TokenWrapper("test"));
		Assert.assertTrue(blnContainsKey);
		blnContainsKey = tokenMap.containsKey(new TokenWrapper("test1"));
		Assert.assertTrue(!blnContainsKey);	
	}
	
}
