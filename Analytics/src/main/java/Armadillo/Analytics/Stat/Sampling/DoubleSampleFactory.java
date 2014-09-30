package Armadillo.Analytics.Stat.Sampling;

/**
 * Light factory for creating samples of type <tt>Double</tt>.
 * In order to save on object space the sample implementation
 * should not extend some abstract base class. The downside is that
 * their is some degree of code-duplication.
 * 
 * @author 
 * @version CVS $Revision: 1.4 $
 * @since CVS $Date: 2011/08/31 11:39:51 $
 */
public class DoubleSampleFactory extends ASampleFactory<Double> {

	/** Singleton double sample factory */
	private final static DoubleSampleFactory SINGLETON = new DoubleSampleFactory();
	
	public DoubleSampleFactory() {
	}
	
	public static DoubleSampleFactory getFactory() {
		return SINGLETON;
	}
	
	class DoubleSample_LLR1 implements ISample<Double> {
		

		/**
		 * Every sample has a value.
		 * But value in this case is stored as <tt>double</tt> primitive,
		 * not as Double, for memory saving purposes.
		 */
		private final double value;
		
		/**
		 * 
		 * @param doubleValue the value (as <tt>double</tt> primitive) 
		 */
		DoubleSample_LLR1(double doubleValue) {
			this.value = doubleValue;
		}
		
		public Double getValue() {
			return value;
		}
		
		public double getLikelihoodRatio() {
			return 1.0;
		}
	
		/**
		 * This class is immutable, therefore the duplicate method 
		 * is allowed to return the <tt>this</tt> reference.
		 */
		public DoubleSample_LLR1 duplicate() {
			return this;
		}
	
		/**
	     * Ordering is based on natural order.
	     * This implementation is inconsistent with equals.
	     * <p>
	     * If <tt>compareTo</tt> returns 0, it does not necessarily mean that 
	     * the compared instance is equal to this instance.
	     * Only <tt>getValue</tt> is used in the comparison. 
	     */
	    public int compareTo(ISample<Double> other) {
	    	
	        double otherValue = other.getValue();
	        
	        return this.value > otherValue ? 1 : (value < otherValue ? -1 : 0);
	    }
		
	    
		@Override
		public String toString() {
			
			return "(Double value = " + getValue() + ", " +
					"Likelihood ratio = " + getLikelihoodRatio() + ")";
			
		}
	}
	
	class DoubleSample implements ISample<Double> {
		

		/**
		 * Every sample has a value.
		 * But value in this case is stored as <tt>double</tt> primitive,
		 * not as Double, for memory saving purposes.
		 */
		private final double value;
		
		/**
		 * The likelihood ratio.
		 */
		private final double likelihoodRatio;
		
		/**
		 * 
		 * @param doubleValue the value (as <tt>double</tt> primitive)
		 * @param likelihoodRatio the likelihood ratio
		 * @param source the source 
		 */
		DoubleSample(double doubleValue, double likelihoodRatio) {
			this.value = doubleValue;
			this.likelihoodRatio = likelihoodRatio;
		}
		
		public double getLikelihoodRatio() {
			return likelihoodRatio;
		}
		
		/**
		 * This class is immutable, therefore the duplicate method 
		 * is allowed to return the <tt>this</tt> reference.
		 */
		public DoubleSample duplicate() {
			return this;
		}
		
		public Double getValue() {
			return value;
		}
		
		 /**
	     * Ordering is based on natural order.
	     * This implementation is inconsistent with equals.
	     * <p>
	     * If <tt>compareTo</tt> returns 0, it does not necessarily mean that 
	     * the compared instance is equal to this instance.
	     * Only <tt>getValue</tt> is used in the comparison. 
	     */
	    public int compareTo(ISample<Double> other) {
	    	
	        double otherValue = other.getValue();
	        
	        return this.value > otherValue ? 1 : (value < otherValue ? -1 : 0);
	    }
		
	    
		@Override
		public String toString() {
			
			return "(Double value = " + getValue() + ", " +
					"Likelihood ratio = " + getLikelihoodRatio() + ")";
			
		}
	}
	
	/**
	 * Returns a new <tt>Double</tt> sample.
	 * @param value the value as <tt>double</tt> primitive
	 * @param likelihoodRatio the likelihood ratio
	 * @return a new <tt>Double</tt> sample
	 * @throws IllegalArgumentException in case input of value or likelihoodRatio is
	 * incorrect.
	 */
	public ISample<Double> newSample(double value, double likelihoodRatio) {
		
		checkValue(value);
		checkLikelihoodRatio(likelihoodRatio);
		
		if(likelihoodRatio == 1.0) {
			return new DoubleSample_LLR1(value);
		}
		
		return new DoubleSample(value, likelihoodRatio);
	}
	
	/**
	 * Returns a new <tt>Double</tt> sample with likelihood 
	 * ratio equal to one.
	 * @param value the value as <tt>double</tt>primitive
	 * @return a new <tt>Double</tt> sample
	 * @throws IllegalArgumentException in case input of value is
	 * incorrect.
	 */
	public ISample<Double> newSample(double value) {
		
		checkValue(value);
		
		
		return new DoubleSample_LLR1(value);
		
	}
	
	/**
	 * Returns a new <tt>Double</tt> sample.
	 * @param value the value as <tt>Double</tt> instance.
	 * @param likelihoodRatio the likelihood ratio
	 * @return a new <tt>Double</tt> sample
	 * @throws IllegalArgumentException in case input of value or likelihoodRatio is
	 * incorrect.
	 */
	public ISample<Double> newSample(Double value, double likelihoodRatio) {
			
		checkValue(value);
		checkLikelihoodRatio(likelihoodRatio);
		
		
		if(likelihoodRatio == 1.0) {
			return new DoubleSample_LLR1(value);
		}
		
		return new DoubleSample(value, likelihoodRatio);
	}
	
	
	@Override
	protected void checkValue(Double value) {
		// check for null!
		if(value == null) {
			throw new IllegalArgumentException("Null value not allowed: value=" 
					+ value + ".");
		}
	}

	protected void checkValue(double value) {
		// every primitive double is allowed.
	}
}
