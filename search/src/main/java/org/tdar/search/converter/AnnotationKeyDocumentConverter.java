package org.tdar.search.converter;

import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.search.query.QueryFieldNames;


public class AnnotationKeyDocumentConverter extends AbstractSolrDocumentConverter {

    /*
     * See solr/configsets/default/conf/annotationKeys-schema.xml
     */

    public static SolrInputDocument convert(ResourceAnnotationKey key) {
        
        SolrInputDocument doc = convertPersistable(key);
        doc.setField(QueryFieldNames.NAME, key.getKey());
        doc.setField(QueryFieldNames.TYPE, key.getAnnotationDataType());
        return doc;
    }
}
