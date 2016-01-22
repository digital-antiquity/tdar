package org.tdar.search.converter;

import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.PersistableUtils;

public class PersonDocumentConverter extends AbstractSolrDocumentConverter{

    public static SolrInputDocument convert(Person person) {
        SolrInputDocument doc = convertPersistable(person);
        doc.setField(QueryFieldNames.FIRST_NAME, person.getFirstName());
        doc.setField(QueryFieldNames.LAST_NAME, person.getLastName());
        doc.setField(QueryFieldNames.NAME, person.getProperName());
        doc.setField(QueryFieldNames.EMAIL, person.getEmail());
        doc.setField(QueryFieldNames.REGISTERED, person.isRegistered());
        if (person instanceof TdarUser) {
            TdarUser user = (TdarUser) person;
            doc.setField(QueryFieldNames.USERNAME, user.getUsername());
            doc.setField(QueryFieldNames.CONTIRBUTOR, user.isContributor());
        }
        if (PersistableUtils.isNotNullOrTransient(person.getInstitution())) {
            doc.setField(QueryFieldNames.INSTITUTION_NAME, person.getInstitutionName());
            doc.setField(QueryFieldNames.INSTITUION_ID, person.getInstitution().getId());
        }
        return doc;
    }

}
