package Armadillo.Analytics.Base;

import Armadillo.Core.Logger;

public class CoreConstants {
        
	public static final String COMPANY_NAME = "Armadillo";

        public static final String DATA_PATH = "Data";

        /// <summary>
        /// Error allowed by the binary search precision.
        /// The smaller the error, the greather the number of iterations 
        /// required by the binary serch algorithm
        /// </summary>
        public static final double DBL_BINARY_SEARCH_PRECISION = 1E-3;

        /// <summary>
        /// Number of iterations carried out by binary search
        /// </summary>
        public static final int INT_BINARY_SEARCH_ITERATIONS = 100;

        //public static final IDataRequest SHARED =null;
        //public static final ulong HWM = 5000;

        public static String ApplicationDataPath()
        {
        	try {
				throw new Exception();
			} catch (Exception e) {
				Logger.log(e);
			}
        	return "";
        }
}
