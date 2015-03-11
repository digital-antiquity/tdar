package org.tdar.search.query.part;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Transient;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.Query;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.MustJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.RangeTerminationExcludable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.service.search.Operator;
import org.tdar.search.index.TdarIndexNumberFormatter;
import org.tdar.search.index.bridge.LatLongClassBridge;
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
 * 
 * @see {@link LatLongClassBridge}
 * @see {@link TdarIndexNumberFormatter}
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 */
public class SpatialQueryPart extends FieldQueryPart<LatitudeLongitudeBox> {

    private static final double MERIDIAN = 180d;
    public static final int SCALE_RANGE = 2;
    private static final double MIN_LUCENE = -540d;
    private static final double MAX_LUCENE = 540d;

    private Operator operator = Operator.AND;

    @Transient
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    /*
     * trying to say either:
     * (a) minX and minY are inside the bouding box OR
     * (b) minX is before and maxX is after (i.e. item completely covers)
     * 
     * NOTE: this is brittle, and only setup on a bounding box that does not cover the dateline.
     * 
     * FIXME: replace with spatial lucene
     */
    private static final String SPATIAL_QUERY_FORMAT = "((" + QueryFieldNames.MINX + ":[%1$s TO %2$s]^1 AND " + QueryFieldNames.MAXX + ":[%1$s TO %2$s]^1) " +
            " OR (" + QueryFieldNames.MINX + ":[" + MIN_LUCENE + " TO %2$s] AND " + QueryFieldNames.MAXX + ":[%1$s TO " + MAX_LUCENE + "])) " +
            "AND ((" + QueryFieldNames.MINY + ":[%3$s TO %4$s]^1 AND " + QueryFieldNames.MAXY + ":[%3$s TO %4$s]^1) " +
            "OR (" + QueryFieldNames.MINY + ":[" + MIN_LUCENE + " TO %4$s] AND " + QueryFieldNames.MAXY + ":[%3$s TO " + MAX_LUCENE + "]))";

    private static final String SPATIAL_QUERY_FORMAT_PRIME = "((((" + QueryFieldNames.MINX + ":[%6$s TO %2$s]) AND (" + QueryFieldNames.MAXX
            + ":[%1$s TO %5$s])) " +
            "OR ((" + QueryFieldNames.MINXPRIME + ":[%6$s TO %2$s]) AND (" + QueryFieldNames.MAXX + ":[%1$s TO %5$s])) " +
            "OR ((" + QueryFieldNames.MINX + ":[%6$s TO %2$s]) AND (" + QueryFieldNames.MAXXPRIME + ":[%1$s TO %5$s]))) " +
            "AND (" + QueryFieldNames.MINY + ":[%6$s TO %4$s]) AND (" + QueryFieldNames.MAXY + ":[%3$s TO %5$s]))";

    public SpatialQueryPart() {

    }

    public SpatialQueryPart(LatitudeLongitudeBox... boxes) {
        add(boxes);
    }

    public SpatialQueryPart(Collection<LatitudeLongitudeBox> boxes) {
        this(boxes.toArray(new LatitudeLongitudeBox[0]));
    }

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

    public Query generateQuery(QueryBuilder builder) {

        BooleanJunction<?> bool = builder.bool();
        for (LatitudeLongitudeBox spatialLimit : getFieldValues()) {
            if (!spatialLimit.isInitialized()) {
                continue;
            }

            logger.trace("crosses primeMeridian: {}, crosses antiMeridian: {}", spatialLimit.crossesPrimeMeridian(), spatialLimit.crossesDateline());
            // If the search bounds cross the antimeridian, we need to split up the spatial limit into two separate
            // boxes because the degrees change from positive to negative.
            Query query = null;
            if (spatialLimit.crossesDateline() && !spatialLimit.crossesPrimeMeridian()) {
                Query prime1 = createPrimeQuery(builder, spatialLimit.getMinObfuscatedLongitude(), 180d, spatialLimit.getMinObfuscatedLatitude(), spatialLimit.getMaxObfuscatedLatitude(),MAX_LUCENE, MIN_LUCENE);
                Query prime2 = createPrimeQuery(builder, -180d, spatialLimit.getMaxObfuscatedLongitude(), spatialLimit.getMinObfuscatedLatitude(), spatialLimit.getMaxObfuscatedLatitude(), MAX_LUCENE, MIN_LUCENE);
                query = builder.bool().should(prime1).should(prime2).createQuery();
            } else {
                query = createNormQuery(builder, spatialLimit);
            }
            MustJunction must = builder.bool().must(query).must(builder.range().onField(QueryFieldNames.SCALE).from(TdarIndexNumberFormatter.MIN_ALLOWED).to(spatialLimit.getScale() + SCALE_RANGE).createQuery());
            if (getOperator() == Operator.AND) {
                bool = bool.must(must.createQuery());
            } else {
                bool = bool.should(must.createQuery());
            }
        }
        return bool.createQuery();
    }

    private Query createPrimeQuery(QueryBuilder builder, Double d1, Double d2, Double d3, Double d4, Double d5, Double d6) {
        

        RangeTerminationExcludable q1 = builder.range().onField(QueryFieldNames.MINX).from(d6).to(d2);
        RangeTerminationExcludable q2 = builder.range().onField(QueryFieldNames.MAXX).from(d1).to(d5);
        RangeTerminationExcludable q3 = builder.range().onField(QueryFieldNames.MINXPRIME).from(d6).to(d2);
        RangeTerminationExcludable q4 = builder.range().onField(QueryFieldNames.MAXXPRIME).from(d1).to(d5);
        RangeTerminationExcludable q5 = builder.range().onField(QueryFieldNames.MINY).from(d6).to(d4);
        RangeTerminationExcludable q6 = builder.range().onField(QueryFieldNames.MAXY).from(d3).to(d5);

        MustJunction b1 = builder.bool().must(q1.createQuery()).must(q2.createQuery());
        MustJunction b2 = builder.bool().must(q3.createQuery()).must(q2.createQuery());
        MustJunction b3 = builder.bool().must(q1.createQuery()).must(q4.createQuery());
        MustJunction b4 = builder.bool().must(q5.createQuery()).must(q6.createQuery());
        return builder.bool().should(b1.createQuery()).should(b2.createQuery()).should(b3.createQuery()).should(b4.createQuery()).createQuery();
    };

    
    private org.apache.lucene.search.Query createNormQuery(org.hibernate.search.query.dsl.QueryBuilder builder, LatitudeLongitudeBox box) {
            // should be boolean w/and?
        RangeTerminationExcludable q1 = builder.range().boostedTo(1).onField(QueryFieldNames.MINX).andField(QueryFieldNames.MAXX).from(box.getMinObfuscatedLongitude()).to(box.getMaxObfuscatedLongitude());

        RangeTerminationExcludable q2 = builder.range().onField(QueryFieldNames.MINX).from(MIN_LUCENE).to(box.getMaxObfuscatedLongitude());
        RangeTerminationExcludable q3 = builder.range().onField(QueryFieldNames.MAXX).from(box.getMinObfuscatedLongitude()).to(MAX_LUCENE);
        
        // should be boolean w/and?
        RangeTerminationExcludable q4 = builder.range().boostedTo(1).onField(QueryFieldNames.MINY).andField(QueryFieldNames.MAXY).from(box.getMinObfuscatedLatitude()).to(box.getMaxObfuscatedLatitude());

        RangeTerminationExcludable q5 = builder.range().onField(QueryFieldNames.MINY).from(MIN_LUCENE).to(box.getMaxObfuscatedLatitude());
        RangeTerminationExcludable q6 = builder.range().onField(QueryFieldNames.MAXY).from(box.getMinObfuscatedLatitude()).to(MAX_LUCENE);

        MustJunction b1 = builder.bool().must(q2.createQuery()).must(q3.createQuery());
        MustJunction b2 = builder.bool().must(q5.createQuery()).must(q6.createQuery());
        BooleanJunction b3 = builder.bool().should(q1.createQuery()).should(b1.createQuery());
        BooleanJunction b4 = builder.bool().should(q4.createQuery()).should(b2.createQuery());
        return builder.bool().must(b3.createQuery()).must(b4.createQuery()).createQuery();
    };

    @Override
    public String generateQueryString() {
        StringBuilder q = new StringBuilder();

        for (LatitudeLongitudeBox spatialLimit : getFieldValues()) {
            if (!spatialLimit.isInitialized()) {
                continue;
            }
            if (q.length() > 0) {
                q.append(" " + operator.name() + " ");
            }

            String format = SPATIAL_QUERY_FORMAT;

            logger.trace("crosses primeMeridian: {}, crosses antiMeridian: {}", spatialLimit.crossesPrimeMeridian(), spatialLimit.crossesDateline());
            // If the search bounds cross the antimeridian, we need to split up the spatial limit into two separate
            // boxes because the degrees change from positive to negative.
            if (spatialLimit.crossesDateline() && !spatialLimit.crossesPrimeMeridian()) {
                format = SPATIAL_QUERY_FORMAT_PRIME;
                q.append(
                        String.format(
                                format,
                                TdarIndexNumberFormatter.format(spatialLimit.getMinObfuscatedLongitude()),
                                TdarIndexNumberFormatter.format(180d),
                                TdarIndexNumberFormatter.format(spatialLimit.getMinObfuscatedLatitude()),
                                TdarIndexNumberFormatter.format(spatialLimit.getMaxObfuscatedLatitude()),
                                MAX_LUCENE,
                                MIN_LUCENE
                                ));
                q.append(" OR ");
                
                
                
                q.append(
                        String.format(
                                format,
                                TdarIndexNumberFormatter.format(-180d),
                                TdarIndexNumberFormatter.format(spatialLimit.getMaxObfuscatedLongitude()),
                                TdarIndexNumberFormatter.format(spatialLimit.getMinObfuscatedLatitude()),
                                TdarIndexNumberFormatter.format(spatialLimit.getMaxObfuscatedLatitude()),
                                MAX_LUCENE,
                                MIN_LUCENE
                                ));

            } else {
                q.append(
                        String.format(
                                format,
                                TdarIndexNumberFormatter.format(spatialLimit.getMinObfuscatedLongitude()),
                                TdarIndexNumberFormatter.format(spatialLimit.getMaxObfuscatedLongitude()),
                                TdarIndexNumberFormatter.format(spatialLimit.getMinObfuscatedLatitude()),
                                TdarIndexNumberFormatter.format(spatialLimit.getMaxObfuscatedLatitude()),
                                MAX_LUCENE,
                                MIN_LUCENE
                                )
                        );
            }
            q.append(String.format(" AND %s:[%s TO %s] ", QueryFieldNames.SCALE, TdarIndexNumberFormatter.MIN_ALLOWED,
                    TdarIndexNumberFormatter.format(spatialLimit.getScale() + SCALE_RANGE)));

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
}
