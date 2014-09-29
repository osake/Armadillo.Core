package Armadillo.Core.Text;

import java.util.Comparator;

import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.Text.StringHelper;

    public class TokenWrapper  implements Comparable<TokenWrapper>,
		Comparator<TokenWrapper>
    {
    	
    	
        public int length()
        {
            return Token.length();
        }

        public String Token;
        
        public int HashCode;

        public TokenWrapper()
        {
        }

        public TokenWrapper(String strToken)
        {
        	try{
	            if (StringHelper.IsNullOrEmpty(strToken))
	            {
	                throw new HCException("Invalid token");
	            }
	            SetToken(strToken);
        	}
            catch(Exception ex){
            	Logger.log(ex);
            }
        }

        public void SetToken(String strToken)
        {
            Token = strToken.intern();
            HashCode = strToken.hashCode();
        }

		@Override
        public int compareTo(TokenWrapper other)
        {
            return Compare(this, other);
        }

        public int Compare(Object x, Object y)
        {
            return Compare(
                (TokenWrapper) x,
                (TokenWrapper) y);
        }

		@Override
        public int compare(TokenWrapper x, TokenWrapper y)
        {
            return x.Token.compareTo(y.Token);
        }

        public boolean Equals(TokenWrapper x, TokenWrapper y)
        {
            //if (x.HashCode != y.HashCode)
            //{
            //    return false;
            //}
            return x.Token.equals(y.Token);
        }

        @Override
        public int hashCode()
        {
            return HashCode;
        }

        @Override
        public boolean equals(Object other)
        {
            return Equals(this, (TokenWrapper)other);
        }

        @Override
        public String toString()
        {
            return Token;
        }

		public char charAt(int actualPosition) {
			return Token.charAt(actualPosition);
		}

    }

