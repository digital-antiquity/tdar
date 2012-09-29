package org.tdar.search.query;

import java.util.ArrayList;
import java.util.List;

import org.tdar.index.TdarIndexNumberFormatter;
import org.tdar.index.bridge.LatLongClassBridge;

/**
 * $Id$
 * 
 * Given bounding box search limits this creates a query statement which
 * searches for any item with an overlapping bounding area. To account for
 * boxes that cross the Prime Meridian we extend the min and max longitude to
 * -540 and 540 respectively and use the computed longitude projections (for
 * objects whose min longitude was recorded as larger than it's max longitude).
 * 
 * 
 * @see {@link LatLongClassBridge}
 * @see {@link TdarIndexNumberFormatter}
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 */
public class SpatialQueryPart implements QueryPart {

    private static final String SPATIAL_QUERY_FORMAT = "(((minx:[%1$s TO %2$s]) AND (maxx:[%1$s TO %2$s])) " +
            "AND (miny:[%3$s TO %4$s]) AND (maxy:[%3$s TO %4$s]))";

    private static final String SPATIAL_QUERY_FORMAT_PRIME = "((((minx:[%6$s TO %2$s]) AND (maxx:[%1$s TO %5$s])) " +
            "OR ((minxPrime:[%6$s TO %2$s]) AND (maxx:[%1$s TO %5$s])) " +
            "OR ((minx:[%6$s TO %2$s]) AND (maxxPrime:[%1$s TO %5$s]))) " +
            "AND (miny:[%6$s TO %4$s]) AND (maxy:[%3$s TO %5$s]))";

    /*
     * ((
     * (
     * ((minx:[00999999460 TO 00999999887.966919]) AND (maxx:[00999999887.8872681 TO 10000000540]))
     * OR ((minxPrime:[00999999460 TO 00999999887.966919]) AND (maxx:[00999999887.8872681 TO 10000000540]))
     * OR ((minx:[00999999460 TO 00999999887.966919]) AND (maxxPrime:[00999999887.8872681 TO 10000000540]))
     * )
     * AND (miny:[00999999460 TO 10000000033.46581674573002]) AND (maxy:[10000000033.42571077612917 TO 10000000540])))
     * AND (( status:(draft) ) )
     */
    private List<SpatialLimit> spatialLimits;

    public SpatialQueryPart() {
        spatialLimits = new ArrayList<SpatialLimit>();
    }

    public void addSpatialLimit(SpatialLimit limit) {
        spatialLimits.add(limit);
    }

    @Override
    public String generateQueryString() {
        StringBuilder q = new StringBuilder();

        for (SpatialLimit spatialLimit : spatialLimits) {
            if (q.length() > 0)
                q.append(" AND ");

            String format = SPATIAL_QUERY_FORMAT;
            if (spatialLimit.getMinx() > 0 && spatialLimit.getMaxx() < 0 || spatialLimit.getMinx() < 0 && spatialLimit.getMaxx() > 0)
                format = SPATIAL_QUERY_FORMAT_PRIME;

            q.append(
                    String.format(
                            format,
                            TdarIndexNumberFormatter.format(spatialLimit.getMinx()),
                            TdarIndexNumberFormatter.format(spatialLimit.getMaxx()),
                            TdarIndexNumberFormatter.format(spatialLimit.getMiny()),
                            TdarIndexNumberFormatter.format(spatialLimit.getMaxy()),
                            TdarIndexNumberFormatter.format(540),
                            TdarIndexNumberFormatter.format(-540)
                            )
                    );
        }
        return q.toString();
    }

}
