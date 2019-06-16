package org.tdar.search.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.datatable.ColumnVisibility;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.service.SearchUtils;

public class DataValueDocumentConverter extends AbstractSolrDocumentConverter {

    /*
     * See solr/configsets/default/conf/dataMappings-schema.xml
     */
    public static List<SolrInputDocument> convert(InformationResource ir, DatasetDao datasetDao) {
        List<SolrInputDocument> docs = new ArrayList<>();
        Map<DataTableColumn, String> data = datasetDao.getMappedDataForInformationResource(ir);
        if (data != null) {
            for (DataTableColumn key : data.keySet()) {
                if (key == null) {
                    continue;
                }
                String mapValue = data.get(key);
                if (key.getName() == null || StringUtils.isBlank(mapValue)) {
                    continue;
                }

                SolrInputDocument doc = createDocument(key, ir, mapValue);
                if (doc != null) {
                    docs.add(doc);
                }
            }

        }
        return docs;

    }


    public static SolrInputDocument createDocument(DataTableColumn key, InformationResource ir, String mapValue) {
        if (key.getVisible() == ColumnVisibility.HIDDEN) {
            return null;
        }

        String solrId = SearchUtils.createKey(ir) + "-" + key.getId();
        String keyName = key.getName();
        SolrInputDocument doc = new SolrInputDocument();
        if(logger.isTraceEnabled()){
            logger.trace("Indexing dataset value:  tdarId:{}\t solrId:{}\t title:{}", ir.getId(), solrId, ir.getTitle());
        }
        logSetField(doc,QueryFieldNames.ID, ir.getId());
        logSetField(doc,QueryFieldNames.CLASS, ir.getClass().getName());
        logSetField(doc,QueryFieldNames._ID, solrId);
        logSetField(doc,QueryFieldNames.NAME, keyName);
        logSetField(doc,QueryFieldNames.PROJECT_ID, ir.getProject().getId());
        logSetField(doc,QueryFieldNames.COLUMN_ID, key.getId());
        logSetField(doc,QueryFieldNames.VALUE, mapValue);

        return doc;
    }

    private static void logSetField(SolrInputDocument doc, String key, Object value) {
        if(logger.isTraceEnabled()){
            logger.trace("Indexing dataset doc:      field:{}\t value:{}", key, value);
        }
        doc.setField(key, value);
    }
}
