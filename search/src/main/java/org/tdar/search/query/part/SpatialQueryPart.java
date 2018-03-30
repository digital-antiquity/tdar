package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Transient;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.search.query.QueryFieldNames;

import com.opensymphony.xwork2.TextProvider;

/**
 * $Id$
 * 
 * Given bounding box search limits this creates a query statement which
 * searches for any item with an overlapping bounding area. To account for
 * boxes that cross the Prime Meridian we extend the min and max longitude to
 * -540 and 540 respectively and use the computed longitude projections (for
 * objects whose min longitude was recorded as larger than it's max longitude).
 * 
 * https://cwiki.apache.org/confluence/display/solr/Spatial+Search
 * https://lucene.apache.org/core/4_10_3/spatial/org/apache/lucene/spatial/query/SpatialOperation.html#BBoxIntersects
 * https://books.google.com/books?id=M9mtCAAAQBAJ&pg=PA148&lpg=PA148&dq=solr+overlap++spatial&source=bl&ots=-DXL-VUt8q&sig=ohwbM4S5FFXj5PJvjy8I3FIcXZ0&hl=en&sa=X&ved=0ahUKEwixhbLomLbJAhWBQYgKHfhrCjAQ6AEIWTAJ#v=onepage&q=solr%20overlap%20%20spatial&f=false
 * http://stackoverflow.com/questions/16526843/solr-4-spatial-search-polygon-vs-polygon-search-with-distance-parameter
 * 
 * @version $Rev$
 */
public class SpatialQueryPart extends FieldQueryPart<LatitudeLongitudeBox> {

    public static final int SCALE_RANGE = 2;

    private Operator operator = Operator.AND;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    private boolean ignoreScale;

    public SpatialQueryPart() {

    }

    public SpatialQueryPart(LatitudeLongitudeBox... boxes) {
        add(boxes);
    }

    public SpatialQueryPart(Collection<LatitudeLongitudeBox> boxes) {
        this(boxes.toArray(new LatitudeLongitudeBox[0]));
    }

    @Override
    public String generateQueryString() {
        StringBuilder q = new StringBuilder();

        for (LatitudeLongitudeBox box : getFieldValues()) {
            if (!box.isInitialized()) {
                continue;
            }
            if (q.length() > 0) {
                q.append(" " + operator.name() + " ");
            }

            logger.trace("crosses primeMeridian: {}, crosses antiMeridian: {}", box.crossesPrimeMeridian(), box.crossesDateline());
            // If the search bounds cross the antimeridian, we need to split up the spatial limit into two separate
            // boxes because the degrees change from positive to negative.

            // *** NOTE *** ENVELOPE uses following pattern minX, maxX, maxy, minY *** //
            Double minLong = box.getObfuscatedWest();
            Double maxLat = box.getObfuscatedNorth();
            Double minLat = box.getObfuscatedSouth();
            Double maxLong = box.getObfuscatedEast();
            if (box.crossesDateline() && !box.crossesPrimeMeridian()) {
                q.append(String.format(" %s:\"Intersects(ENVELOPE(%.9f,%.9f,%.9f,%.9f)) distErrPct=0.025\" OR"
                        + "  %s:\"Intersects(ENVELOPE(%.9f,%.9f,%.9f,%.9f)) distErrPct=0.025\" ", QueryFieldNames.ACTIVE_LATITUDE_LONGITUDE_BOXES,
                        minLong, -180d, maxLat, minLat,
                        QueryFieldNames.ACTIVE_LATITUDE_LONGITUDE_BOXES,
                        180d, minLong, maxLat, minLat));

            } else if (box.crossesPrimeMeridian()) {
                q.append(String.format(" %s:\"Intersects(ENVELOPE(%.9f,%.9f,%.9f,%.9f)) distErrPct=0.025\" ", QueryFieldNames.ACTIVE_LATITUDE_LONGITUDE_BOXES,
                        minLong, maxLong, maxLat, minLat));
            } else {
                if (minLat > maxLat) {
                    Double t = maxLat;
                    maxLat = minLat;
                    minLat = t;
                }
                q.append(String.format(" %s:\"Intersects(ENVELOPE(%.9f,%.9f,%.9f,%.9f)) distErrPct=0.025\" ", QueryFieldNames.ACTIVE_LATITUDE_LONGITUDE_BOXES,
                        minLong, maxLong, maxLat, minLat));
            }

            if (!ignoreScale) {
                q.append(String.format(" AND %s:[%s TO %s] ", QueryFieldNames.SCALE, 0,
                        box.getScale() + SCALE_RANGE));
            }
        }

        return q.toString();
    }

    @Override
    public String getDescription(TextProvider provider) {
        if (getFieldValues().isEmpty()) {
            return provider.getText("spatialQueryPart.empty_description");
        }

        List<String> latlongs = new ArrayList<String>();
        for (LatitudeLongitudeBox box : getFieldValues()) {
            latlongs.add(box.toString());
        }
        String seperator = " " + operator.name().toLowerCase() + " ";
        List<String> vals = new ArrayList<>();
        vals.add(StringUtils.join(latlongs, seperator));
        String fmt = provider.getText("spatialQueryPart.resource_located", vals);
        return fmt;
    }

    @Override
    public String getDescriptionHtml(TextProvider provider) {
        return StringEscapeUtils.escapeHtml4(getDescription(provider));
    }

    @Override
    public void add(LatitudeLongitudeBox... values) {
        for (LatitudeLongitudeBox box : values) {
            if (box.isInitialized()) {
                super.add(box);
            }
        }
    }

    public void ignoreScale(boolean b) {
        this.ignoreScale = b;

    }

}
