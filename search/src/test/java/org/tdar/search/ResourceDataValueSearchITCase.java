package org.tdar.search;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.bean.DataValue;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.converter.DataValueDocumentConverter;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.CoreNames;
import org.tdar.search.service.query.ResourceSearchService;
import org.tdar.utils.MessageHelper;

public class ResourceDataValueSearchITCase extends AbstractWithIndexIntegrationTestCase {

    @Autowired
    private SolrClient template;

    @Autowired
    ResourceSearchService resourceSearchService;

    @Test
    @Rollback
    public void testAllActive() throws SearchException, SearchIndexException, IOException, SolrServerException {
        Dataset dataset = createAndSaveNewDataset();
        Project project = createAndSaveNewProject("test project");
        DataTableColumn dtc = setup(dataset, project);

        SolrInputDocument doc = DataValueDocumentConverter.createDocument(dtc, dataset, "Concorde");
        template.add(CoreNames.DATA_MAPPINGS, doc);
        template.commit(CoreNames.DATA_MAPPINGS);
        logger.debug("{}", doc);
        SolrQuery params = new SolrQuery();
        params.setQuery("*:*");

        QueryResponse query = template.query(CoreNames.DATA_MAPPINGS, params);
        logger.debug("{}", query);
        assertTrue(query.toString().contains(dtc.getId().toString()));

        // search exact
        searchFor(dataset, null, "Concorde");
        // search lowercase
        searchFor(dataset, null, "concorde");

    }

    @Test
    @Rollback
    public void testFieldedSearch() throws SearchException, SearchIndexException, IOException, SolrServerException {
        Dataset dataset = createAndSaveNewDataset();
        Project project = createAndSaveNewProject("test project");
        DataTableColumn dtc = setup(dataset, project);
        SolrInputDocument doc = DataValueDocumentConverter.createDocument(dtc, dataset, "Concorde");
        template.add(CoreNames.DATA_MAPPINGS, doc);
        template.commit(CoreNames.DATA_MAPPINGS);
        // search exact
        searchFor(dataset, dtc, "Concorde");
        // search lowercase
        searchFor(dataset, dtc, "concorde");

    }

    private DataTableColumn setup(Dataset dataset, Project project) {
        dataset.setProject(project);
        DataTable dt = new DataTable();
        dt.setName("est");
        dataset.getDataTables().add(dt);
        genericService.save(dataset);
        genericService.save(dt);
        DataTableColumn dtc = new DataTableColumn();
        dtc.setDataTable(dt);
        dtc.setColumnEncodingType(DataTableColumnEncodingType.UNCODED_VALUE);
        dtc.setName("test");
        dtc.setDisplayName("Test");
        dt.getDataTableColumns().add(dtc);
        genericService.saveOrUpdate(dt);
        genericService.saveOrUpdate(dtc);
        dataset.setStatus(Status.ACTIVE);
        return dtc;
    }

    private void searchFor(Dataset dataset, DataTableColumn dtc, String term) throws IOException, SearchException, SearchIndexException {
        SearchResult result = performSearch(term,dataset, dtc, null, 100);
        result.getResults().forEach(r -> {
            logger.debug(" - {}", r);
        });
        assertNotEmpty("should have results", result.getResults());
        assertTrue(result.getResults().contains(dataset));
    }

    public SearchResult performSearch(String term, InformationResource dataset, DataTableColumn dtc, TdarUser user, int max) throws IOException, SearchException, SearchIndexException {
        SearchResult<Resource> result = new SearchResult<>(max);
        AdvancedSearchQueryObject asqo = new AdvancedSearchQueryObject();
        SearchParameters e = new SearchParameters();
        if (dtc == null) {
            e.getAllFields().add(term);
        } else {
            e.getDataValues().add(new DataValue(dataset.getProjectId(), dtc.getId(), dtc.getName(), term));
        }
        asqo.getSearchParameters().add(e);
        asqo.setMultiCore(false);

        resourceSearchService.buildAdvancedSearch(asqo, user, result, MessageHelper.getInstance());
        // (TdarUser user, ResourceLookupObject look, LuceneSearchResultHandler<Resource> result,
        // TextProvider support) throws SearchException, IOE
        return result;
    }

}
