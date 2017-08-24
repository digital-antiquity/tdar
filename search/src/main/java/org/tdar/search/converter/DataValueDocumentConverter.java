package org.tdar.search.converter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.service.SearchUtils;

public class DataValueDocumentConverter extends AbstractSolrDocumentConverter {

    /*
     * See solr/configsets/default/conf/dataMappings-schema.xml
     */
    public static List<SolrInputDocument> convert(InformationResource ir, ResourceService resourceService) {
        List<SolrInputDocument> docs = new ArrayList<>();
        Map<DataTableColumn, String> data = resourceService.getMappedDataForInformationResource(ir, TdarConfiguration.getInstance().isProductionEnvironment());
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
                docs.add(doc);
            }

        }
        return docs;

    }

    public static SolrInputDocument createDocument(DataTableColumn key, InformationResource ir, String mapValue) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField(QueryFieldNames.ID, ir.getId());
        doc.setField(QueryFieldNames.CLASS, ir.getClass().getName());
        doc.setField(QueryFieldNames._ID, SearchUtils.createKey(ir) + "-" + key.getId());
        String keyName = key.getName();
        doc.setField(QueryFieldNames.NAME, keyName);
        doc.setField(QueryFieldNames.PROJECT_ID, ir.getProject().getId());
        doc.setField(QueryFieldNames.COLUMN_ID, key.getId());
        doc.setField(QueryFieldNames.VALUE, mapValue);
        return doc;
    }
}
