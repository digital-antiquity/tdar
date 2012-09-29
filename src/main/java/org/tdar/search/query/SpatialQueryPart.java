package org.tdar.search.query;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.search.index.TdarIndexNumberFormatter;
import org.tdar.search.index.bridge.LatLongClassBridge;

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

    @Transient
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());
    private static final String MIN_LUCENE = TdarIndexNumberFormatter.format(-540);
    private static final String MAX_LUCENE = TdarIndexNumberFormatter.format(540);


    private static final String SPATIAL_QUERY_FORMAT = "((" + QueryFieldNames.MINX + ":[%1$s TO %2$s] AND " + QueryFieldNames.MAXX + ":[%1$s TO %2$s]) " +
            " OR (" + QueryFieldNames.MINX + ":[" + MIN_LUCENE + " TO %2$s] AND " + QueryFieldNames.MAXX + ":[%1$s TO " + MAX_LUCENE + "])) " +
            "AND ((" + QueryFieldNames.MINY + ":[%3$s TO %4$s] AND " + QueryFieldNames.MAXY + ":[%3$s TO %4$s]) " +
            "OR (" + QueryFieldNames.MINY + ":[" + MIN_LUCENE + " TO %4$s] AND " + QueryFieldNames.MAXY + ":[%3$s TO " + MAX_LUCENE + "]))";

    private static final String SPATIAL_QUERY_FORMAT_PRIME = "((((" + QueryFieldNames.MINX + ":[%6$s TO %2$s]) AND (" + QueryFieldNames.MAXX
            + ":[%1$s TO %5$s])) " +
            "OR ((" + QueryFieldNames.MINXPRIME + ":[%6$s TO %2$s]) AND (" + QueryFieldNames.MAXX + ":[%1$s TO %5$s])) " +
            "OR ((" + QueryFieldNames.MINX + ":[%6$s TO %2$s]) AND (" + QueryFieldNames.MAXXPRIME + ":[%1$s TO %5$s]))) " +
            "AND (" + QueryFieldNames.MINY + ":[%6$s TO %4$s]) AND (" + QueryFieldNames.MAXY + ":[%3$s TO %5$s]))";
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
    private List<LatitudeLongitudeBox> spatialLimits = new ArrayList<LatitudeLongitudeBox>();



    public SpatialQueryPart() {

    }

    public SpatialQueryPart(LatitudeLongitudeBox box) {
        addSpatialLimit(box);
    }
    
    public void addSpatialLimit(LatitudeLongitudeBox limit) {
        spatialLimits.add(limit);
    }

    @Override
    public String generateQueryString() {
        StringBuilder q = new StringBuilder();

        for (LatitudeLongitudeBox spatialLimit : spatialLimits) {
            if (q.length() > 0)
                q.append(" AND ");

            String format = SPATIAL_QUERY_FORMAT;

            logger.trace("crosses primeMeridian: {}, crosses antiMeridian: {}" ,spatialLimit.crossesPrimeMeridian(), spatialLimit.crossesDateline());
            //If the search bounds cross the antimeridian, we need to split up the spatial limit into two separate 
            //boxes because the degrees change from positive to negative. 
            if(spatialLimit.crossesDateline() && !spatialLimit.crossesPrimeMeridian()) {
                format = SPATIAL_QUERY_FORMAT_PRIME;
                q.append(
                        String.format(
                                format,
                                TdarIndexNumberFormatter.format(spatialLimit.getMinObfuscatedLongitude()),
                                TdarIndexNumberFormatter.format(180d),
                                TdarIndexNumberFormatter.format(spatialLimit.getMinObfuscatedLatitude()),
                                TdarIndexNumberFormatter.format(spatialLimit.getMaxObfuscatedLatitude()),
                                TdarIndexNumberFormatter.format(540),
                                TdarIndexNumberFormatter.format(-540)
                                ));
                q.append(" OR ");
                q.append(
                        String.format(
                                format,
                                TdarIndexNumberFormatter.format(-180d),
                                TdarIndexNumberFormatter.format(spatialLimit.getMaxObfuscatedLongitude()),
                                TdarIndexNumberFormatter.format(spatialLimit.getMinObfuscatedLatitude()),
                                TdarIndexNumberFormatter.format(spatialLimit.getMaxObfuscatedLatitude()),
                                TdarIndexNumberFormatter.format(540),
                                TdarIndexNumberFormatter.format(-540)
                                ));
                
            } else {
                q.append(
                        String.format(
                                format,
                                TdarIndexNumberFormatter.format(spatialLimit.getMinObfuscatedLongitude()),
                                TdarIndexNumberFormatter.format(spatialLimit.getMaxObfuscatedLongitude()),
                                TdarIndexNumberFormatter.format(spatialLimit.getMinObfuscatedLatitude()),
                                TdarIndexNumberFormatter.format(spatialLimit.getMaxObfuscatedLatitude()),
                                TdarIndexNumberFormatter.format(540),
                                TdarIndexNumberFormatter.format(-540)
                                )
                        );
            }
            
        }
        return q.toString();
    }

}
