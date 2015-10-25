package org.tdar.search;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.service.GenericService;
import org.tdar.search.service.PersonDocumentConverter;

public class SolrSetupITCase extends AbstractIntegrationTestCase {

    @Autowired
    private SolrClient template;
    
    @Autowired
    GenericService genericService;
    
    @Test
    public void test() throws SolrServerException, IOException {
//        template.
        List<Person> findAll = genericService.findAll(Person.class);
        for (Person person : findAll) {
            template.deleteById("Person-" + person.getId());
            SolrInputDocument document = PersonDocumentConverter.convert(person);
            template.add(document);
            logger.debug("adding: " + person.getId() + " " + person.getProperName());
        }
        template.commit();
    }
}
