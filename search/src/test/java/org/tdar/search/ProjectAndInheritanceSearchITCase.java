package org.tdar.search;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.search.bean.ObjectType;
import org.tdar.search.bean.ReservedSearchParameters;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.SearchResult;

public class ProjectAndInheritanceSearchITCase extends AbstractResourceSearchITCase {

    protected static final Long DOCUMENT_INHERITING_CULTURE_ID = 4230L;
    protected static final Long DOCUMENT_INHERITING_NOTHING_ID = 4231L;

    @Test
    @Rollback(true)
    public void testForInheritedCulturalInformationFromProject() throws ParseException, SolrServerException, IOException {
        searchIndexService.indexAll(new QuietIndexReciever(),Arrays.asList( LookupSource.RESOURCE), getAdminUser());
        ReservedSearchParameters rparams = new ReservedSearchParameters();
        rparams.setObjectTypes(Arrays.asList(ObjectType.DOCUMENT, ObjectType.IMAGE));
        SearchResult<Resource> result = doSearch("Archaic",null,null,rparams);
        assertTrue("'Archaic' defined inparent project should be found in information resource", resultsContainId(result,DOCUMENT_INHERITING_CULTURE_ID));
        assertFalse("A child document that inherits nothing from parent project should not appear in results", resultsContainId(result,DOCUMENT_INHERITING_NOTHING_ID));
    }

    
    @Test
    @Rollback(true)
    public void testForProjectWithChild() throws ParseException, SolrServerException, IOException {
        Project project = createAndSaveNewProject("test with child");
        Dataset dataset = createAndSaveNewDataset();
        dataset.setProject(project);
        CultureKeyword ck = new CultureKeyword("Etruscan");
        genericService.saveOrUpdate(ck);
        dataset.getCultureKeywords().add(ck);
        genericService.saveOrUpdate(dataset);
        searchIndexService.index(dataset, project);
        SearchParameters rparams = new SearchParameters();
        rparams.getApprovedCultureKeywordIdLists().add(new ArrayList<String>());
        rparams.getApprovedCultureKeywordIdLists().get(0).add(ck.getId().toString());
        rparams.setObjectTypes(Arrays.asList(ObjectType.PROJECT));
        SearchResult<Resource> result = doSearch(null,null,rparams, null);
        assertTrue(result.getResults().contains(project));
    }

}
