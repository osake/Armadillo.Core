package Armadillo.Core.Text;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

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

		@Override
		public Comparator<TokenWrapper> reversed() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Comparator<TokenWrapper> thenComparing(
				Comparator<? super TokenWrapper> other) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <U> Comparator<TokenWrapper> thenComparing(
				Function<? super TokenWrapper, ? extends U> keyExtractor,
				Comparator<? super U> keyComparator) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <U extends Comparable<? super U>> Comparator<TokenWrapper> thenComparing(
				Function<? super TokenWrapper, ? extends U> keyExtractor) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Comparator<TokenWrapper> thenComparingInt(
				ToIntFunction<? super TokenWrapper> keyExtractor) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Comparator<TokenWrapper> thenComparingLong(
				ToLongFunction<? super TokenWrapper> keyExtractor) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Comparator<TokenWrapper> thenComparingDouble(
				ToDoubleFunction<? super TokenWrapper> keyExtractor) {
			// TODO Auto-generated method stub
			return null;
		}

		public static <T extends Comparable<? super T>> Comparator<T> reverseOrder() {
			// TODO Auto-generated method stub
			return null;
		}

		public static <T extends Comparable<? super T>> Comparator<T> naturalOrder() {
			// TODO Auto-generated method stub
			return null;
		}

		public static <T> Comparator<T> nullsFirst(
				Comparator<? super T> comparator) {
			// TODO Auto-generated method stub
			return null;
		}

		public static <T> Comparator<T> nullsLast(
				Comparator<? super T> comparator) {
			// TODO Auto-generated method stub
			return null;
		}

		public static <T, U> Comparator<T> comparing(
				Function<? super T, ? extends U> keyExtractor,
				Comparator<? super U> keyComparator) {
			// TODO Auto-generated method stub
			return null;
		}

		public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
				Function<? super T, ? extends U> keyExtractor) {
			// TODO Auto-generated method stub
			return null;
		}

		public static <T> Comparator<T> comparingInt(
				ToIntFunction<? super T> keyExtractor) {
			// TODO Auto-generated method stub
			return null;
		}

		public static <T> Comparator<T> comparingLong(
				ToLongFunction<? super T> keyExtractor) {
			// TODO Auto-generated method stub
			return null;
		}

		public static <T> Comparator<T> comparingDouble(
				ToDoubleFunction<? super T> keyExtractor) {
			// TODO Auto-generated method stub
			return null;
		}

    }

