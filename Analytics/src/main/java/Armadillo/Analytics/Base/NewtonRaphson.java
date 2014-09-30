package Armadillo.Analytics.Base;

/**
 * This class implements the <tt>numerical</tt> routine 'rtsafe' as given in 
 * <it>Numerical Recipes in C++</it> (see [1] Chap 9, pp370).
 * A combination of bisection with Newton-Raphson algorithm for root-finding 
 * of scalar functions.
 * It is adopted to work as a function rather than a procedure.
 * <p>
 * An instance of this class is constructed by specifying the scalar function 
 * whose root(s) will be searched for. In addition, in order to make use of Newton-Raphson algorithm, 
 * its first order derivative must be specified.
 * Subsequently, the method {@link #rtsafe(double, double, double)} finds the root
 * the function in a specified interval, <tt>[x1,x2]</tt>.
 *  
 * <p>
 * <b>References:</b>
 * <ul>
 * <li><b>[1]:</b> W.H. Press, S.A. Teukolsky, W.T. Vetterling, B.P. Flannery 
 * <it>Numerical Recipes in C++</it>, 
 * Cambridge University Press, second edition (2002).
 * </ul>
 * @author Manuel Reenders
 * @version CVS $Revision: 1.5 $
 * @since CVS $Date: 2012/11/21 12:22:53 $
 * @see math.numerics.NewtonRaphson.Result
 */
public class NewtonRaphson {
    
	/**
	 * Here ITMAX is the maximum allowed number of iterations
	 */
	private static final int ITMAX = 100;

	/**
	 * The function whose root(s) will be searched.
	 */
    private final IScalarFunctionX func;

    /**
     * The derivative of the function.
     */
    private final IScalarFunctionX dFunc;
    
    /**
     * Counter keeping track of the recursion-depth.
     */
    private int recursionCounter = 0;
    
    /**
     * maximum recursion for finding root outside the specified interval
     */
    private static final int RECURSIONMAX = 20;

    /**
     * Constructor specifying the function whose root(s) are to be obtained 
     * and its first derivative.
     * @param function scalar function (one-dimensional argument)
     * @param firstDerivativeOfFunction the first derivative of the specified function
     */
    public NewtonRaphson(IScalarFunctionX function,
            IScalarFunctionX firstDerivativeOfFunction) {
        func = function;
        dFunc = firstDerivativeOfFunction;

    }

  
    /**
     * Method named after 'rtsafe' of Numerical Recipes. 
     * <p> 
     * Using a combination of Newton-Raphson an bisection, find the root of the specified function <tt>func</tt>
     * bracketed between <tt>x1</tt> and <tt>x2</tt>. The root will be refined until its accuracy is known with 
     * &plusmn <tt>xacc</tt>. 
     * The function rtsafe returns an
     * instance of class <tt>NewtonRaphsonOutput</tt>.
     * If no root can be found in the interval <tt>[x1, x2]</tt> then the interval will 
     * be widened to <tt>[0.5 * x1, 2.0 * x2]</tt> (recursively) until a root 
     * is found or RECURSIONMAX exceeded.
     *  
     * @param x1 left boundary of the interval [x1,x2] (x2>x1)
     * @param x2 right boundary of the interval [x1,x2]
     * @param xacc accuracy for the root
     * @return an instance of <tt>NewtonRaphsonOutput</tt>, gathering the results of
     * this root-finding method 
     * @throws Exception 
     * @throws TooManyIterationsException if the number of iterations 
     * exceeds {@link #getMaximumIterations()}.
     * @throws IllegalArgumentException if the widening of the interval does not 
     * lead to a root within RECURSIONMAX.
     */
    public Result rtsafe(double x1, double x2, double xacc) throws Exception {

    	int iter = 0;

        double xH, xL, rts;

        double fL = func.apply(x1);
        double fH = func.apply(x2);

        //System.out.println("fL = " + fL + ", fH = " + fH);
        //System.out.println("xL = " + x1 + ", xH = " + x2);
        
        if ((fL > 0.0 && fH > 0.0) || (fL < 0.0 && fH < 0.0)) {
           
        	if (recursionCounter++ < RECURSIONMAX) {
        		
        		// Continue widening the range until root is
                // bracketed....(unless there is no root..)
                // System.out.println("Widening the range in NewtonRaphson
                // rtsafe, recursion = " + recursionCounter);
                return rtsafe(x1 * 0.5, x2 * 2.0, xacc);
            } else {
                throw new IllegalArgumentException(
                        "Root must be bracketed in rtsafe. Recursion = "
                                + recursionCounter + ", x2 = " + x2);
            }
        }
        if (fL == 0.0) {
            // System.out.println("rtsafe 1");
            return new Result(x1, x1, x2, xacc, iter);
        }
        // return x1;
        if (fH == 0.0) {
            // System.out.println("rtsafe 2");
            return new Result(x2, x1, x2, xacc, iter);
        }
        // return x2;
        if (fL < 0.0) { // Orient the search so that func(x1) < 0.
            xL = x1;
            xH = x2;

        } else {
            xH = x1;
            xL = x2;
        }
        rts = 0.5 * (x1 + x2); // Initialize the guess for root,
        double dxold = Math.abs(x2 - x1); // the "stepsize before last,"
        double dx = dxold; // and the last step.
        double f = func.apply(rts);
        double df = dFunc.apply(rts);
        for (int j = 0; j < ITMAX; j++) {
            iter = j;
            if ((((rts - xH) * df - f) * ((rts - xL) * df - f) >= 0.0)
                    || (Math.abs(2.0 * f) > Math.abs(dxold * df))) { /* Bisection instead. 
					Bisect if Newton out of range (first check) 
					or not decreasing fast enough  (second check).
            	*/
                dxold = dx;
                dx = 0.5 * (xH - xL);
                rts = xL + dx;
                if (xL == rts) {
                    //System.out.println("rtsafe 3");
                    return new Result(rts, x1, x2, xacc, iter);
                }
                // Change in root is negligible.
            } else { // Newton step is acceptable. Take it.
                dxold = dx;
                dx = f / df;
                double temp = rts;
                rts -= dx;
                if (temp == rts) {
                    // System.out.println("rtsafe 4");
                    return new Result(rts, x1, x2, xacc, iter);

                }
            }
            if (Math.abs(dx) < xacc) {
                // System.out.println("rtsafe 5");
                return new Result(rts, x1, x2, xacc, iter);
            }
            // Convergence criterion.

            f = func.apply(rts); // The one new function evaluation per
                                 // iteration.
            df = dFunc.apply(rts); // idem.
            if (f < 0.0) // Maintain the bracket of the root.
                xL = rts;
            else
                xH = rts;
            iter++;
        }
        throw new Exception("Too many iterations in rtsafe. Iter = "
                + iter);
    }

    /**
     * Returns the maximum number of iterations allowed for 
     * {@link #rtsafe}.
     * 
     * @return the maximum number of iterations
     */
    public double getMaximumIterations() {
    	return ITMAX;
    }
    
    /**
     * Data only class, containing the results of a 
     * {@link NewtonRaphson#rtsafe(double, double, double)} root-finding algorithm.
     * The names of the attributes resemble those used in Numerical Recipes [1] for
     * the safe Newton-Raphson root-finding routine <tt>rtsafe</tt> (see [1] Chap 9, pp370)
     * <p>
     * This data class is immutable.
     * 
     * <p>
     * <b>References:</b>
     * <ul>
     * <li><b>[1]:</b> W.H. Press, S.A. Teukolsky, W.T. Vetterling, B.P. Flannery 
     * <it>Numerical Recipes in C++</it>, 
     * Cambridge University Press, second edition (2002).
     * </ul>
     * @author Manuel Reenders
     * @version CVS $Revision: 1.5 $
     * @since CVS $Date: 2012/11/21 12:22:53 $
     * @see math.numerics.NewtonRaphson
     */
    public class Result {

    	/**
    	 * The number of iterations performed.
    	 */
        private final int iter;

        /**
         * The found root.
         */
        private final double root;

        /**
         * Left boundary of the bracketing interval, might be lower than the
         * initial specified input value.
         */
        private final double x1;

        /**
         * Right boundary of the bracketing interval, might be higher than the
         * initial specified input value. 
         */
        private final double x2;

        /**
         * The specified accuracy of the root.
         */
        private final double xacc;

       
        
        /**
         * Constructor setting all attributes.
         * 
         * @param root the root	
         * @param x1 left boundary 
         * @param x2 right boundary
         * @param xacc the accuracy of <tt>rts</tt>
         * @param iter the number of performed iterations
         */
        private Result(double root, double x1, double x2, double xacc,
                int iter) {
            
        	this.iter = iter;
            this.root = root;
            this.x1 = x1;
            this.x2 = x2;
            this.xacc = xacc;
           
        }
        
        /**
         * Returns the function whose root has been computed.
         * @return the function whose root has been computed.
         */
        public IScalarFunctionX getFunc() {
            return func;
        }
        
        /**
         * Returns the derivative of <tt>func</tt>.
         * @return the derivative of <tt>func</tt>.
         */
        public IScalarFunctionX getDFunc() {
            return dFunc;
        }
       

        /**
         * The number of iterations performed.
         * @return the number of iterations performed.
         */
        public int getIter() {
            return iter;
        }

        /**
         * Returns the computed root of <tt>func</tt>.
         * @return the computed root.
         */
        public double getRoot() {
            return root;
        }

        /**
         * Returns the left boundary of the bracketing interval; might be lower than the
         * initial specified input value.
         * @return the left boundary of the bracketing interval.
         */
        public double getX1() {
            return x1;
        }

        /**
         * Returns the right boundary of the bracketing interval, might be higher than the
         * initial specified input value. 
         *
         * @return the right boundary of the bracketing interval.
         */
        public double getX2() {
            return x2;
        }

        /**
         * Returns the specified accuracy of the root.
         * 
         * @return the specified accuracy of the root.
         */
        public double getXacc() {
            return xacc;
        }

        @Override
        public String toString() {
        	
        	StringBuilder sb = new StringBuilder("NewtonRaphson result:");
        	sb.append("\nRoot = " + getRoot());
        	sb.append("\nNumber of iterations: " + getIter());
        	sb.append("\nAccuracy of the root: " + getXacc());
        	sb.append("\nBracketing interval [lower,upper]: [" + getX1() + ", " + getX2() + "]");
        	
        	return sb.toString();
        }
    }
}
