package org.tdar.search.query;

/**
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 *
 */
public class SpatialLimit {
	
	private double minx;
	private double maxx;
	private double miny;
	private double maxy;
	
	public SpatialLimit(double minx, double maxx, double miny, double maxy) {
		this.minx = minx;
		this.maxx = maxx;
		this.miny = miny;
		this.maxy = maxy;
	}

	public double getMinx() {
		return minx;
	}

	public double getMaxx() {
		return maxx;
	}

	public double getMiny() {
		return miny;
	}

	public double getMaxy() {
		return maxy;
	}
	
}
