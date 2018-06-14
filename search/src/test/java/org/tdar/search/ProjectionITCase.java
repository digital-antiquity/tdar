package org.tdar.search;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.search.bean.AdvancedSearchQueryObject;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.query.ProjectionModel;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResult;
import org.tdar.search.query.facet.FacetWrapper;
import org.tdar.utils.MessageHelper;

public class ProjectionITCase extends AbstractResourceSearchITCase {

    @Test
    @Rollback
    public void testExpermientalProjectionModel() throws SearchException, SearchIndexException, IOException, ParseException {
        SearchResult<Resource> result = new SearchResult<>(10000);
        result.setProjectionModel(ProjectionModel.LUCENE);
        FacetWrapper facetWrapper = new FacetWrapper();
        facetWrapper.facetBy(QueryFieldNames.RESOURCE_TYPE, ResourceType.class);
        result.setFacetWrapper(facetWrapper);
        AdvancedSearchQueryObject asqo = new AdvancedSearchQueryObject();
        // asqo.getReservedParams().getStatuses().addAll(Arrays.asList(Status.DRAFT,Status.DELETED));
        result.setAuthenticatedUser(getAdminUser());
        resourceSearchService.buildAdvancedSearch(asqo, null, result, MessageHelper.getInstance());
        boolean seenCreator = false;
        for (Resource r : result.getResults()) {
            logger.debug("{} {}", r, r.isViewable());
            if (r instanceof InformationResource) {
                InformationResource ir = (InformationResource) r;
                logger.debug("\t{}", ir.getProject());
            }
            if (CollectionUtils.isNotEmpty(r.getPrimaryCreators())) {
                seenCreator = true;
            }
            logger.debug("\t{}", r.getActiveLatitudeLongitudeBoxes());
            logger.debug("\t{}", r.getPrimaryCreators());
        }
        assertTrue(seenCreator);
    }

}
