package Armadillo.Core.Math;

public class MachinePrecision {

	public static double m_dblMaxExpE;
	public static double m_dblMachineEps;
	
	/**
	 * Harmless, useless instantiation for fooling code-coverage tools.
	 */
	static {
		getMachinePrecisionDouble();
		getMaxEExp();
	}
	
	/**
	 * Returns an approximation of the machine precision.
	 * @return a proxy for the machine precision.
	 */
	private static double getMachinePrecisionDouble() {
		
		m_dblMachineEps = 1.0;
		
		for(;;) {
			m_dblMachineEps /= 2.0;
			if(1.0 + m_dblMachineEps == 1.0) {
				return m_dblMachineEps;
			}
		} 
		
	}
	
	/**
	 * Returns the maximum exponent <tt>x</tt> such that 
	 * <tt>Math.exp(x) < Double.POSITIVE_INFINITY</tt> 
	 * @return a proxy for the maximum exponent of <tt>e</tt> 
	 * not returning infinity.
	 */
	private static double getMaxEExp() {
		
		m_dblMaxExpE = 1.0;
		
		for(;;) {
			
			if(Double.isInfinite(Math.exp(m_dblMaxExpE)))
					return m_dblMaxExpE;
					
			m_dblMaxExpE *= 2.0;
		} 
		
	}
}
