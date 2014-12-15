package org.tdar.search.index.bridge;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.FieldBridge;
import org.hibernate.search.bridge.LuceneOptions;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.service.GenericService;
import org.tdar.search.query.QueryFieldNames;

/**
 * 
 * $Id$
 * 
 * A {@link FieldBridge} for indexing Related Data between resources and data sets.
 * 
 * This takes the data in the relatedDatasetData() map on InformationResource entities and adds it as field-value pairs on that resource's lucene document.
 * 
 * @version $Rev$
 * 
 */
public class StringMapBridge implements FieldBridge {

    @Override
    public void set(String name, Object value, Document doc, LuceneOptions opts) {
        if (value == null) {
            return;
        }
        @SuppressWarnings("unchecked")
        Map<Object, String> map = (Map<Object, String>) value;
        for (Object key : map.keySet()) {
            if (key == null) {
                continue;
            }
            String keyName = "";
            if (key instanceof DataTableColumn) {
                keyName = ((DataTableColumn) key).getName();
            } else {
                keyName = GenericService.extractStringValue(key);
            }
            String mapValue = map.get(key);
            if (keyName == null || StringUtils.isBlank(mapValue)) {
                continue;
            }
            opts.addFieldToDocument(QueryFieldNames.DATA_VALUE_PAIR, keyName + ":" + mapValue, doc);
        }
    }

}
