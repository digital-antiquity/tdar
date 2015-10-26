package org.tdar.search;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.service.GenericService;
import org.tdar.search.service.SearchIndexService;

public class SolrSetupITCase extends AbstractIntegrationTestCase {

    @Autowired
    private SolrClient template;
    
    @Autowired
    GenericService genericService;
    
    @Autowired
    SearchIndexService searchIndexService;
    
    @Test
    public void testPeople() throws SolrServerException, IOException {
        searchIndexService.indexAllPeople();
        SolrQuery solrQuery = new  SolrQuery();
        solrQuery.setParam("fl","id,score");
        String string = "S*";
        
        solrQuery.setParam("q","name_autocomplete:"+ string);
        QueryResponse rsp = template.query("people",solrQuery);
        SolrDocumentList docs = rsp.getResults();
        logger.debug("{}", docs);
    }

    @Test
    public void testInstitutions() throws SolrServerException, IOException {
        searchIndexService.indexAllInstitutions();
        SolrQuery solrQuery = new  SolrQuery();
        solrQuery.setParam("fl","id,score");
        String string = "University o*";
        
        solrQuery.setParam("q","name_autocomplete:"+ string);
        QueryResponse rsp = template.query("institutions",solrQuery);
        SolrDocumentList docs = rsp.getResults();
        logger.debug("{}", docs);
    }

}
