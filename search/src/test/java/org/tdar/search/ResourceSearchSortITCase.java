package org.tdar.search;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.query.SearchResult;

public class ResourceSearchSortITCase extends AbstractResourceSearchITCase {


    // note: relevance sort broken out into SearchRelevancyITCase
    @Test
    @Rollback
    public void testSortFieldTitle() throws ParseException, SearchException, SearchIndexException, IOException {
        Long alphaId = -1L;
        Long omegaId = -1L;
        Project p = new Project();
        p.setTitle("test project");
        p.setDescription("test descr");
        p.setStatus(Status.ACTIVE);
        p.markUpdated(getUser());
        List<String> titleList = Arrays.asList(new String[] { "a", "b", "c", "d" });
        genericService.save(p);
        for (String title : titleList) {
            Document doc = new Document();
            doc.markUpdated(getUser());
            doc.setTitle(title);
            doc.setDescription(title);
            doc.setDate(2341);
            doc.setProject(p);
            doc.setStatus(Status.ACTIVE);
            genericService.save(doc);
            if (alphaId == -1) {
                alphaId = doc.getId();
            }
            omegaId = doc.getId();
        }
        reindex();
        setSortThenCheckFirstResult("sorting by title asc", SortOption.TITLE, p.getId(), alphaId);
        setSortThenCheckFirstResult("sorting by title desc", SortOption.TITLE_REVERSE, p.getId(), omegaId);
    }

    @Test
    @Rollback
    public void testSortFieldProject() throws InstantiationException, IllegalAccessException, ParseException, SearchException, SearchIndexException, IOException,SearchException, SearchIndexException {
        searchIndexService.purgeAll();
        Project project = createAndSaveNewProject("my project");
        Project project2 = createAndSaveNewProject("my project 2");
        Image a = createAndSaveNewInformationResource(Image.class, project, getBasicUser(), "a");
        Image b = createAndSaveNewInformationResource(Image.class, project, getBasicUser(), "b");
        Image c = createAndSaveNewInformationResource(Image.class, project, getBasicUser(), "c");

        Image d = createAndSaveNewInformationResource(Image.class, project2, getBasicUser(), "d");
        Image e = createAndSaveNewInformationResource(Image.class, project2, getBasicUser(), "e");
        Image aa = createAndSaveNewInformationResource(Image.class, project2, getBasicUser(), "a");
        List<Resource> res = Arrays.asList(project, project2, a, b, c, d, e, aa);
        searchIndexService.indexCollection(res);

        SearchResult<Resource> result = doSearch("", null, null, null, SortOption.PROJECT);
        List<Resource> results = result.getResults();
        for (Resource r : results) {
            if (r instanceof InformationResource) {
                InformationResource ir = (InformationResource)r;
                logger.debug("{} {} {}", r.getId(), ir.getProjectTitle() + r.getName(), ir.getProjectId());
            } else {
                logger.debug("{} {}", r.getId(), r.getName());
            }
        }
        int i = results.indexOf(project);
        assertEquals(i + 1, results.indexOf(a));
        assertEquals(i + 2, results.indexOf(b));
        assertEquals(i + 3, results.indexOf(c));
        assertEquals(i + 4, results.indexOf(project2));
        assertEquals(i + 5, results.indexOf(aa));
        assertEquals(i + 6, results.indexOf(d));
        assertEquals(i + 7, results.indexOf(e));
    }

    @Test
    @Rollback
    public void testSortFieldDate() throws ParseException, SearchException, SearchIndexException, IOException {
        Long alphaId = -1L;
        Long omegaId = -1L;
        Project p = new Project();
        p.setTitle("test project");
        p.setDescription("test description");
        p.markUpdated(getUser());
        List<Integer> dateList = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5, 19, 39 });
        genericService.save(p);
        for (Integer date : dateList) {
            Document doc = new Document();
            doc.markUpdated(getUser());
            doc.setDate(date);
            doc.setTitle("hello" + date);
            doc.setDescription(doc.getTitle());
            doc.setProject(p);
            genericService.save(doc);
            if (alphaId == -1) {
                logger.debug("setting id for doc:{}", doc.getId());
                alphaId = doc.getId();
            }
            omegaId = doc.getId();
        }
        reindex();

        setSortThenCheckFirstResult("sorting by datecreated asc", SortOption.DATE, p.getId(), alphaId);
        setSortThenCheckFirstResult("sorting by datecreated desc", SortOption.DATE_REVERSE, p.getId(), omegaId);
    }

}
