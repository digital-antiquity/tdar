package org.tdar.search.converter;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.datatable.ColumnVisibility;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.resource.DatasetDao;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.service.SearchUtils;

import javax.annotation.Nonnull;

public class DataValueDocumentConverter extends AbstractSolrDocumentConverter {

    /*
     * See solr/configsets/default/conf/dataMappings-schema.xml
     */
    public static List<SolrInputDocument> convert(InformationResource ir, DatasetDao datasetDao) {
        Map<DataTableColumn, String> data = datasetDao.getMappedDataForInformationResource(ir);
        return data.entrySet().stream()
                .filter(e -> e.getKey() != null
                        && e.getKey().getVisible() != ColumnVisibility.HIDDEN
                        && StringUtils.isNotBlank(e.getKey().getName())
                        && StringUtils.isNotBlank(e.getValue()) )
                .map( e -> createDocument(e.getKey(), ir, e.getValue()))
                .collect(Collectors.toList());
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
        setField(doc,QueryFieldNames.ID, ir.getId());
        setField(doc,QueryFieldNames.CLASS, ir.getClass().getName());
        setField(doc,QueryFieldNames._ID, solrId);
        setField(doc,QueryFieldNames.NAME, keyName);
        setField(doc,QueryFieldNames.PROJECT_ID, ir.getProject().getId());
        setField(doc,QueryFieldNames.COLUMN_ID, key.getId());

        // FIXME: We should arguably use DataTableColumn.delimiterValue instead, but it is not exposed by the UI.
        String delim = TdarConfiguration.getInstance().getDatasetCellDelimiter();
        String[] vals = StringUtils.split(mapValue, delim);
        for (int i = 0; i < vals.length; i++) {
            vals[i] = StringUtils.trim(vals[i]);
        }
        setField(doc, QueryFieldNames.VALUE, vals);

        return doc;
    }

    private static void setField(SolrInputDocument doc, String key, Object value) {
        if(logger.isTraceEnabled()){
            logger.trace("Indexing dataset doc:      field:{}\t value:{}", key, value);
        }
        doc.setField(key, value);
    }
}
