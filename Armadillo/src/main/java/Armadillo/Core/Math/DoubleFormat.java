package Armadillo.Core.Math;

public enum DoubleFormat {
	
	/**
	 * Formatting, using thousands separator and 2 digits precision
	 */
	CURRENCYAMOUNT("%,.2f"),
	/**
	 * Rounded double, using thousands separator.
	 */
	CURRENCYAMOUNTROUNDED("%,.0f"),
	/**
	 * Suitable for numbers in the range 10^3 to 10^-3.
	 */
	FRACTION("%.5f"),
	

	/**
	 * Scientific formatting showing only 3 digits total. 
	 */
	SCIENTIFICSHORT("%.2e"),
	
	/**
	 * Scientific formatting that corresponds to 
	 * (approximately) single precision (7 decimal fractions/fraction digits)
	 */
	SCIENTIFICSINGLE("%.7e"),
	/**
	 * Scientific formatting that corresponds to (approximately) 
	 * double precision (15 decimal fractions/fraction digits)
	 */
	SCIENTIFICDOUBLE("%.15e");
	
	/**
	 * The format string that can be used by a format function.
	 * @see java.util.Formatter {@link java.util.Formatter#format(String, Object[])} 
	 * 
	 */
	public final String formatString;
	
	/**
	 * Constructor setting the format string
	 * @param formatString format string
	 * 
	 */
	DoubleFormat(String formatString) {
		this.formatString = formatString;
	}

	/**
	 * Returns a formatted string representation of 
	 * the specified <tt>Double</tt> instance, using the format string
	 * of this instance.
	 * 
	 * @param d the Double value to be formatted
	 * @return a formatted representation.
	 */
	public String format(Double d) {
		return String.format(formatString, d);
	}

}