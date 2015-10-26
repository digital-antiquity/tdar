package org.tdar.search.converter;

import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.entity.Institution;

public class InstitutionDocumentConverter extends AbstractSolrDocumentConverter {

    public static SolrInputDocument convert(Institution inst) {
        
        SolrInputDocument doc = convertPersistable(inst);
        doc.setField("name", inst.getName());
        doc.setField("name_autocomplete", inst.getProperName());
        return doc;
    }
}
