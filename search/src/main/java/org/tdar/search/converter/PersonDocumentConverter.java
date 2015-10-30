package org.tdar.search.converter;

import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.utils.PersistableUtils;

public class PersonDocumentConverter extends AbstractSolrDocumentConverter{

    public static SolrInputDocument convert(Person person) {
        SolrInputDocument doc = convertPersistable(person);
        doc.setField("firstName", person.getFirstName());
        doc.setField("lastName", person.getLastName());
        doc.setField("name_autocomplete", person.getProperName());
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
