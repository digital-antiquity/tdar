package org.tdar.search.index;

import java.text.DecimalFormat;

/**
 * 
 * $Id$
 * 
 * Lucene stores strings. It is not aware of numerical types and thus 
 * when it sorts it does so lexicographically. In order to do correct 
 * comparisons and sorts on numerical data using Lucene queries we need
 * to ensure that the values are indexed in such a way that lexicographic
 * order gives us the correct results. To do this, we prepend a '0' to negative
 * numbers and a '1' to positive numbers and invert the negatives.    
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 *
 */
public class TdarIndexNumberFormatter {

	private static final char NEGATIVE_PREFIX = '0';
	// NB: NEGATIVE_PREFIX must be < POSITIVE_PREFIX
	private static final char POSITIVE_PREFIX = '1';
	public static final int MAX_ALLOWED = 999999999;
	public static final int MIN_ALLOWED = -1000000000;
	private static final String FORMAT = "0000000000.##############";

	public static String format(Number i) {
	     if ((i.intValue() < MIN_ALLOWED) || (i.intValue() > MAX_ALLOWED)) {
	         throw new IllegalArgumentException("out of allowed range");
	     }
	     char prefix;
	     if (i.floatValue() < 0) {
	         prefix = NEGATIVE_PREFIX;
	         i = MAX_ALLOWED + i.doubleValue() + 1;
	     } else {
	         prefix = POSITIVE_PREFIX;
	     }
	     DecimalFormat fmt = new DecimalFormat(FORMAT);
	     return prefix + fmt.format(i);
	}
	
}
