package org.tdar.search.service;

import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.entity.Institution;

public class InstitutionDocumentConverter {

    public static SolrInputDocument convert(Institution inst) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField("id", "Institution-" + inst.getId());
        doc.setField("status", inst.getStatus());
        doc.setField("name", inst.getName());
        doc.setField("name_autocomplete", inst.getProperName());
        return doc;
    }
}
