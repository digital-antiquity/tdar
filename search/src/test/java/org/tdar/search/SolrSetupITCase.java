package org.tdar.search;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.search.service.index.SearchIndexService;

import com.opensymphony.xwork2.interceptor.annotations.Before;

public class SolrSetupITCase extends AbstractWithIndexIntegrationTestCase {

    @Autowired
    private SolrClient template;

    @Autowired
    SearchIndexService searchIndexService;

    @Before
    public void index() {
        searchIndexService.indexAll(new QuietIndexReciever(), getAdminUser());
    }

    @Test
    public void testPeople() throws SolrServerException, IOException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setParam("fl", "id,score");
        String string = "S*";

        solrQuery.setParam("q", "name_autocomplete:" + string);
        QueryResponse rsp = template.query("people", solrQuery);
        SolrDocumentList docs = rsp.getResults();
        logger.debug("{}", docs);
    }

    @Test
    public void testInstitutions() throws SolrServerException, IOException {
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setParam("fl", "id,score");
        String string = "University\\ of*";

        solrQuery.setParam("q", "name_autocomplete:" + string);
        QueryResponse rsp = template.query("institutions", solrQuery);
        SolrDocumentList docs = rsp.getResults();
        logger.debug("{}", docs);
    }

}
