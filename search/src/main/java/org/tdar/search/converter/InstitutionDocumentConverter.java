package org.tdar.search.converter;

import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.entity.Institution;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.service.SearchUtils;

public class InstitutionDocumentConverter extends AbstractSolrDocumentConverter {

    public static SolrInputDocument convert(Institution inst) {
        
        SolrInputDocument doc = convertPersistable(inst);
        doc.setField(QueryFieldNames.NAME, inst.getName());
        doc.setField(QueryFieldNames.NAME_AUTOCOMPLETE, SearchUtils.prepareAutoCompleteField(inst.getName()));
        doc.setField(QueryFieldNames.ACRONYM, inst.getAcronym());
        return doc;
    }
}
