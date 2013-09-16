package org.tdar.search.index.bridge;

import java.util.Map;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.GenericService;
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
public class StringMapBridge implements FieldBridge {

    @Override
    public void set(String name, Object value, Document doc, LuceneOptions opts) {

        @SuppressWarnings("unchecked")
        Map<Object, String> map = (Map<Object, String>) value;
        for (Object key : map.keySet()) {
            String keyName = "";
            if (key instanceof DataTableColumn) {
                keyName = ((DataTableColumn) key).getName();
            } else {
                keyName = GenericService.extractStringValue(key);
            }

            opts.addFieldToDocument(QueryFieldNames.DATA_VALUE_PAIR, keyName + ":" + map.get(key), doc);
        }
    }

}
