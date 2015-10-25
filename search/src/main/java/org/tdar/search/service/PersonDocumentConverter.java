package org.tdar.search.service;

import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.utils.PersistableUtils;

public class PersonDocumentConverter {

    public static SolrInputDocument convert(Person person) {
        SolrInputDocument doc = new SolrInputDocument();
        doc.setField("id", "Person-" + person.getId());
        doc.setField("status", person.getStatus());
        doc.setField("firstName", person.getFirstName());
        doc.setField("lastName", person.getLastName());
        doc.setField("properName", person.getProperName());
        doc.setField("registered", person.isRegistered());
        if (person instanceof TdarUser) {
            TdarUser user = (TdarUser) person;
            doc.setField("contributor", user.isContributor());
        }
        if (PersistableUtils.isNotNullOrTransient(person.getInstitution())) {
            doc.setField("institutionName", person.getInstitutionName());
            doc.setField("institution_id", person.getInstitution().getId());
        }
        return doc;
    }
}
