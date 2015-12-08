package org.tdar.search.converter;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.utils.PersistableUtils;

public class PersonDocumentConverter extends AbstractSolrDocumentConverter{

    public static SolrInputDocument convert(Person person) {
        SolrInputDocument doc = convertPersistable(person);
//        addField(doc, QueryFieldNames.FIRST_NAME, person.getFirstName());
//        addField(doc, QueryFieldNames.LAST_NAME, person.getLastName());
        doc.setField(QueryFieldNames.FIRST_NAME, person.getFirstName());
        doc.setField(QueryFieldNames.LAST_NAME, person.getLastName());
        doc.setField(QueryFieldNames.FIRST_NAME_SORT, person.getFirstName());
        doc.setField(QueryFieldNames.LAST_NAME_SORT, person.getLastName());
        doc.setField(QueryFieldNames.NAME_AUTOCOMPLETE, person.getProperName());
        doc.setField(QueryFieldNames.NAME, person.getProperName());
        doc.setField(QueryFieldNames.NAME_PHRASE, person.getProperName());
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

    private static void addField(SolrInputDocument doc, String fieldName, final String text) {
        String txt = StringUtils.trim(text).toLowerCase();
        doc.setField(fieldName, txt);
        
    }
}
