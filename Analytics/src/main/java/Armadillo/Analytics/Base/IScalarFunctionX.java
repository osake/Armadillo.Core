package Armadillo.Analytics.Base;

	/**
	 * Interface that represents a scalar function object <tt>f(x)</tt>: 
	 * a function that takes a single argument and returns a single value. 
	 * Design heavily based on the Colt library.
	 * 
	 * @author
	 * @version CVS $Revision: 1.1 $
	 * @since CVS $Date: 2010/12/07 14:44:37 $
	 */
	public interface IScalarFunctionX {
	    
		/**
	     * Applies a scalar function to a scalar argument.
	     * 
	     * @param x the argument passed to the function 
	     * @return the result of the function
	     */
		double apply(double x);
	}

