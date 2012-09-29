package org.tdar.search.query;

import org.tdar.core.bean.coverage.CoverageType;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 *
 */
public class TemporalLimit {
	
	private int fromYear;
	private int toYear;
	private CoverageType dateType;
	
	public TemporalLimit(CoverageType dateType, int fromYear, int toYear) {
		this.dateType = dateType;
		this.fromYear = fromYear;
		this.toYear = toYear;
	}

	public int getFromYear() {
		return fromYear;
	}

	public int getToYear() {
		return toYear;
	}

	public CoverageType getDateType() {
		return dateType;
	}
	
}
