package org.tdar.search.index.bridge;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.search.index.TdarIndexNumberFormatter;
import org.tdar.search.query.QueryFieldNames;

/**
 * 
 * $Id$
 * 
 * A {@link FieldBridge} for indexing {@link LatitudeLongitudeBox}.
 * 
 * This adds bounding box coordinates to the index document and
 * calculates minx' and maxx' to allow boxes to be drawn across
 * the prime meridian.
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
public class LatLongClassBridge implements FieldBridge {

    @Override
    public void set(String name, Object value, Document doc, LuceneOptions opts) {

        LatitudeLongitudeBox box = (LatitudeLongitudeBox) value;

        double minx = box.getMinObfuscatedLongitude();
        double maxx = box.getMaxObfuscatedLongitude();
        double miny = box.getMinObfuscatedLatitude();
        double maxy = box.getMaxObfuscatedLatitude();

        opts.addFieldToDocument(QueryFieldNames.MINX, TdarIndexNumberFormatter.format(minx), doc);
        opts.addFieldToDocument(QueryFieldNames.MAXX, TdarIndexNumberFormatter.format(maxx), doc);
        opts.addFieldToDocument(QueryFieldNames.MINY, TdarIndexNumberFormatter.format(miny), doc);
        opts.addFieldToDocument(QueryFieldNames.MAXY, TdarIndexNumberFormatter.format(maxy), doc);
        opts.addFieldToDocument(QueryFieldNames.SCALE, TdarIndexNumberFormatter.format(box.getScale()), doc);
        // required for searching boxes that cross the prime meridian
        if (minx > maxx) {
            opts.addFieldToDocument(QueryFieldNames.MINXPRIME, TdarIndexNumberFormatter.format(minx - 360), doc);
            opts.addFieldToDocument(QueryFieldNames.MAXXPRIME, TdarIndexNumberFormatter.format(360 + maxx), doc);
        }

    }
}
