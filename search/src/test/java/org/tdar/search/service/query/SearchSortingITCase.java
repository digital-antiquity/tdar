package org.tdar.search.service.query;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.search.QuietIndexReciever;
import org.tdar.search.bean.ObjectType;
import org.tdar.search.exception.SearchException;
import org.tdar.search.exception.SearchIndexException;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SearchResult;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.TitleSortComparator;

@SuppressWarnings("unchecked")
public class SearchSortingITCase extends AbstractWithIndexIntegrationTestCase {

    @Autowired
    SearchService<Resource> searchService;

    public static class SortTestStruct {
        public SortTestStruct(LookupSource type) {
            this.type = type;
            this.qb = setupQueryBuilder();

        }

        public LookupSource type;
        public QueryBuilder qb;
        public Map<SortOption, Comparator<?>> comparators = new HashMap<SortOption, Comparator<?>>();

    }

    @SuppressWarnings("rawtypes")
    public static abstract class DesignatedComparable<T> implements Comparator<T> {
        @Override
        public final int compare(T obj1, T obj2) {
            Comparable item1 = null;
            Comparable item2 = null;
            if (obj1 != null) {
                item1 = getComparableFor(obj1);
            }
            if (obj2 != null) {
                item2 = getComparableFor(obj2);
            }

            return ObjectUtils.compare(item1, item2);
        }

        public abstract Comparable getComparableFor(T t);
    }

    private static List<SortTestStruct> sortTests = new ArrayList<SortTestStruct>();

    public static Comparator<Resource> titleComparator = ComparatorUtils.nullLowComparator(new TitleSortComparator());

    public static Comparator<Resource> idComparator = ComparatorUtils.nullLowComparator(new Comparator<Resource>() {
        @Override
        public int compare(Resource item1, Resource item2) {
            return item1.getId().compareTo(item2.getId());
        }
    });

    // kill me now.
    public static Comparator<Resource> projectComparator = new Comparator<Resource>() {
        @Override
        public int compare(Resource item1, Resource item2) {
            String title1 = getProjectTitle(item1);
            String title2 = getProjectTitle(item2);

            return title1.compareTo(title2);
        }

    };

    public static String getProjectTitle(Resource item1) {
        String title1 = "";
        if (item1 == null) {
            return title1;
        }
        if (item1 instanceof Project) {
            title1 = ((Project) item1).getProjectTitle();
        }
        if (item1 instanceof InformationResource) {
            title1 = ((InformationResource) item1).getProjectTitle();
        }
        return title1;
    }

    @BeforeClass
    public static void before() {
        SortTestStruct resourceInfo = new SortTestStruct(LookupSource.RESOURCE);
        resourceInfo.comparators.put(SortOption.TITLE, titleComparator);
        resourceInfo.comparators.put(SortOption.TITLE_REVERSE, titleComparator);
        resourceInfo.comparators.put(SortOption.ID, idComparator);
        resourceInfo.comparators.put(SortOption.ID_REVERSE, idComparator);

        sortTests.add(resourceInfo);

    }

    @Test
    public void testYearSorting() throws ParseException, SearchException, SearchIndexException, IOException {
        Comparator<Resource> yearComparator = new Comparator<Resource>() {
            @Override
            public int compare(Resource arg0, Resource arg1) {
                InformationResource ir1 = (InformationResource) arg0;
                InformationResource ir2 = (InformationResource) arg1;
                logger.debug("comparing {} vs. {}", ir1.getDate(), ir2.getDate());
                return ObjectUtils.compare(ir1.getDate(), ir2.getDate());
            }
        };
        assertSortOrder(SortOption.DATE, yearComparator);
        assertSortOrder(SortOption.DATE_REVERSE, yearComparator);
    }

    static ResourceQueryBuilder setupQueryBuilder() {
        ResourceQueryBuilder resourceQueryBuilder = new ResourceQueryBuilder();
        // get information resources only
        FieldQueryPart<String> fqp = new FieldQueryPart<>(QueryFieldNames.RESOURCE_TYPE, Operator.OR,
                Arrays.asList(ResourceType.DATASET.name(), ResourceType.DOCUMENT.name(), ResourceType.IMAGE.name(), ResourceType.CODING_SHEET.name(),
                        ResourceType.ONTOLOGY.name()));
        resourceQueryBuilder.append(fqp);
        return resourceQueryBuilder;
    }

    @SuppressWarnings({ "unused", "rawtypes" })
    @Test
    public void testDateSorting() throws ParseException, SearchException, SearchIndexException, IOException {
        DesignatedComparable<Resource> dateCreatedComparator = new DesignatedComparable<Resource>() {
            @Override
            public Comparable getComparableFor(Resource t) {
                return t.getDateCreated();
            }
        };
        DesignatedComparable<Resource> dateUpdatedComparator = new DesignatedComparable<Resource>() {
            @Override
            public Comparable getComparableFor(Resource t) {
                Date now = t.getDateUpdated();
                Date nearest = DateUtils.round(now, Calendar.SECOND);
                return nearest;
            }
        };

        assertSortOrder(SortOption.DATE_UPDATED, dateUpdatedComparator);
        assertSortOrder(SortOption.DATE_UPDATED_REVERSE, dateUpdatedComparator);
    }

    @Test
    public void testResourceTypeSorting() throws ParseException, SearchException, SearchIndexException, IOException {
        DesignatedComparable<Resource> resourceTypeComparator = new DesignatedComparable<Resource>() {
            @SuppressWarnings("rawtypes")
            @Override
            public Comparable getComparableFor(Resource t) {
                return ObjectType.from(t.getResourceType()).getSortName();
            }
        };
        assertSortOrder(SortOption.RESOURCE_TYPE, resourceTypeComparator);
    }

    @Test
    // the sortInfo data structure has all the info on all the fields we sort by, which querybuilders to use, and what comparators to use
    // to assert that the searchService successfully sorted the results.
    @SuppressWarnings({ "rawtypes" })
    public void testAllSortFields() throws ParseException, SearchException, SearchIndexException, IOException {

        for (SortTestStruct sortTestInfo : sortTests) {

            getSearchIndexService().indexAll(new QuietIndexReciever(), Arrays.asList(sortTestInfo.type), getAdminUser());
            for (Map.Entry<SortOption, Comparator<?>> entry : sortTestInfo.comparators.entrySet()) {
                // assumption: an empty queryBuilder returns alldocs
                SortOption sortOption = entry.getKey();

                SearchResult result = new SearchResult();
                result.setSortField(entry.getKey());
                // sortTestInfo.qb.append(new FieldQueryPart<>(QueryFieldNames.NAME,"*"));;
                searchService.handleSearch(sortTestInfo.qb, result, MessageHelper.getInstance());
                List results = result.getResults();
                assertFalse("list should not be empty", results.isEmpty());
                Comparator comparator = entry.getValue();

                for (int i = 0; i < (results.size() - 2); i++) {
                    logger.info("now testing sorting for {}.{}", sortTestInfo.type, sortOption);
                    Object item1 = results.get(i);
                    Object item2 = results.get(i + 1);
                    String msg = String.format("when sorting by %s, item1:[%s] should appear before item2:[%s] ", sortOption, item1, item2);
                    if (sortOption.isReversed()) {
                        assertTrue(msg, comparator.compare(item1, item2) >= 0);
                    } else {
                        assertTrue(msg, comparator.compare(item1, item2) <= 0);
                    }
                }
            }
        }
    }

    @Test
    public void testTitleSort() throws ParseException, SearchException, SearchIndexException, IOException {
        assertSortOrder(SortOption.TITLE, titleComparator);
        assertSortOrder(SortOption.TITLE_REVERSE, titleComparator);
    }

    @Test
    public void testIdSort() throws ParseException, SearchException, SearchIndexException, IOException {
        assertSortOrder(SortOption.ID, idComparator);
        assertSortOrder(SortOption.ID_REVERSE, idComparator);
    }

    // @Test
    public void testProjectSort() throws ParseException, SearchException, SearchIndexException, IOException {
        assertSortOrder(SortOption.PROJECT, projectComparator);
    }

    private void assertSortOrder(SortOption sortOption, Comparator<Resource> comparator)
            throws ParseException, SearchException, SearchIndexException, IOException {
        SearchResult<Resource> result = new SearchResult<>();
        result.setSortField(sortOption);
        searchService.handleSearch(setupQueryBuilder(), result, MessageHelper.getInstance());
        List<Resource> resources = result.getResults();
        assertFalse("results should not be empty", resources.isEmpty());
        for (int i = 0; i < (resources.size() - 2); i++) {
            Resource item1 = resources.get(i);
            Resource item2 = resources.get(i + 1);
            String msg = String.format("when sorting by %s, item1:[%s] should appear before item2:[%s] ", sortOption, item1, item2);
            if (sortOption.isReversed()) {
                assertTrue(msg, comparator.compare(item1, item2) >= 0);
            } else {
                assertTrue(msg, comparator.compare(item1, item2) <= 0);
            }
        }
    }

}
