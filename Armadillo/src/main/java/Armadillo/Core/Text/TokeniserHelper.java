package Armadillo.Core.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import Armadillo.Core.Logger;

public class TokeniserHelper {
	
	public static String[] tokenise(String str, String strDelimiters) {
		
		try{
			
			StringTokenizer tokeniser = new StringTokenizer(str, strDelimiters);
			List<String> tokenList = new ArrayList<String>();
			while(tokeniser.hasMoreElements())
			{
				String strCurrToken = tokeniser.nextToken();
				if(!StringHelper.IsNullOrEmpty(strCurrToken)){
					strCurrToken = strCurrToken.trim();
					if(!StringHelper.IsNullOrEmpty(strCurrToken)){
					
						tokenList.add(strCurrToken);
					}
				}
			}
			String[] tokensArr = tokenList.toArray(new String[tokenList.size()]);
			return tokensArr;
		}
		catch(Exception ex){
			Logger.log(ex);
		}
		return null;
	}
}
