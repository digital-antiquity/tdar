package org.tdar.core.service;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.lucene.queryParser.ParseException;
import org.hibernate.search.FullTextQuery;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.struts.action.search.AbstractSearchControllerITCase;

public class SearchServiceITCase extends AbstractSearchControllerITCase {

    private ResourceQueryBuilder resourceQueryBuilder = new ResourceQueryBuilder();

    public static class SortTestStruct {
        public SortTestStruct(Class<? extends Indexable> type, QueryBuilder queryBuilder) {
            this.type = type;
            this.qb = queryBuilder;
        }

        public Class<? extends Indexable> type;
        public QueryBuilder qb;
        public Map<SortOption, Comparator<?>> comparators = new HashMap<SortOption, Comparator<?>>();

    }

    public static abstract class DesignatedComparable<T> implements Comparator<T> {
        public final int compare(T obj1, T obj2) {
            Comparable item1 = null;
            Comparable item2 = null;
            if (obj1 != null)
                item1 = getComparableFor(obj1);
            if (obj2 != null)
                item2 = getComparableFor(obj2);

            return ObjectUtils.compare(item1, item2);
        }

        public abstract Comparable getComparableFor(T t);
    }

    private static List<SortTestStruct> sortTests = new ArrayList<SortTestStruct>();

    @SuppressWarnings("unchecked")
    public static Comparator<Resource> titleComparator = ComparatorUtils.nullLowComparator(new Comparator<Resource>() {
        public int compare(Resource item1, Resource item2) {
            return item1.getTitleSort().compareTo(item2.getTitleSort());
        }
    });

    @SuppressWarnings("unchecked")
    public static Comparator<Resource> idComparator = ComparatorUtils.nullLowComparator(new Comparator<Resource>() {
        public int compare(Resource item1, Resource item2) {
            return item1.getId().compareTo(item2.getId());
        }
    });

    // kill me now.
    public static Comparator<Resource> projectComparator = new Comparator<Resource>() {
        public int compare(Resource item1, Resource item2) {
            String title1 = getProjectTitle(item1);
            String title2 = getProjectTitle(item2);

            return title1.compareTo(title2);
        }

    };

    public static String getProjectTitle(Resource item1) {
        String title1 = "";
        if (item1 == null)
            return title1;
        if (item1 instanceof Project) {
            title1 = ((Project) item1).getProjectTitle();
        }
        if (item1 instanceof InformationResource) {
            title1 = ((InformationResource) item1).getProjectTitle();
        }
        return title1;
    }

    @Before
    public void prepare() {
        reindex();
    }

    @BeforeClass
    public static void before() {
        SortTestStruct resourceInfo = new SortTestStruct(Resource.class, new ResourceQueryBuilder());
        resourceInfo.comparators.put(SortOption.TITLE, titleComparator);
        resourceInfo.comparators.put(SortOption.TITLE_REVERSE, titleComparator);
        resourceInfo.comparators.put(SortOption.ID, idComparator);
        resourceInfo.comparators.put(SortOption.ID_REVERSE, idComparator);

        sortTests.add(resourceInfo);

    }

    @Test
    public void testYearSorting() throws ParseException {
        resourceQueryBuilder = new ResourceQueryBuilder();
        // get information resources only
        resourceQueryBuilder
                .setRawQuery("+(resourceType:DOCUMENT resourceType:CODING_SHEET resourceType:IMAGE resourceType:SENSORY_DATA resourceType:DATASET resourceType:ONTOLOGY)");
        Comparator<Resource> yearComparator = new Comparator<Resource>() {
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

    @Test
    public void testDateSorting() throws ParseException {
        DesignatedComparable<Resource> dateCreatedComparator = new DesignatedComparable<Resource>() {
            public Comparable getComparableFor(Resource t) {
                return t.getDateCreated();
            }
        };
        DesignatedComparable<Resource> dateUpdatedComparator = new DesignatedComparable<Resource>() {
            public Comparable getComparableFor(Resource t) {
                return t.getDateUpdated();
            }
        };

        assertSortOrder(SortOption.DATE_UPDATED, dateUpdatedComparator);
        assertSortOrder(SortOption.DATE_UPDATED_REVERSE, dateUpdatedComparator);
    }

    @Test
    public void testResourceTypeSorting() throws ParseException {
        DesignatedComparable<Resource> resourceTypeComparator = new DesignatedComparable<Resource>() {
            public Comparable getComparableFor(Resource t) {
                return t.getResourceTypeSort();
            }
        };
        assertSortOrder(SortOption.RESOURCE_TYPE, resourceTypeComparator);
        assertSortOrder(SortOption.RESOURCE_TYPE_REVERSE, resourceTypeComparator);

    }

    @Test
    // the sortInfo data structure has all the info on all the fields we sort by, which querybuilders to use, and what comparators to use
    // to assert that the searchService successfully sorted the results.
    public void testAllSortFields() throws ParseException {

        for (SortTestStruct sortTestInfo : sortTests) {
            searchIndexService.indexAll(getAdminUser(), sortTestInfo.type);
            for (Map.Entry<SortOption, Comparator<?>> entry : sortTestInfo.comparators.entrySet()) {
                // assumption: an empty queryBuilder returns alldocs
                SortOption sortOption = entry.getKey();
                FullTextQuery ftq = searchService.search(sortTestInfo.qb, entry.getKey());
                List results = ftq.list();
                assertFalse("list should not be empty", results.isEmpty());
                Comparator comparator = entry.getValue();

                for (int i = 0; i < results.size() - 2; i++) {
                    logger.info("now testing sorting for {}.{}", sortTestInfo.type.getSimpleName(), sortOption.getSortField());
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
    public void testTitleSort() throws ParseException {
        assertSortOrder(SortOption.TITLE, titleComparator);
        assertSortOrder(SortOption.TITLE_REVERSE, titleComparator);
    }

    @Test
    public void testIdSort() throws ParseException {
        assertSortOrder(SortOption.ID, idComparator);
        assertSortOrder(SortOption.ID_REVERSE, idComparator);
    }

    // @Test
    public void testProjectSort() throws ParseException {
        assertSortOrder(SortOption.PROJECT, projectComparator);
    }

    private void assertSortOrder(SortOption sortOption, Comparator<Resource> comparator) throws ParseException {
        FullTextQuery ftq = searchService.search(resourceQueryBuilder, sortOption);
        List<Resource> resources = ftq.list();
        assertFalse("results should not be empty", resources.isEmpty());
        // ArrayList<Resource> toCompare = new ArrayList<Resource>(resources);
        // Collections.sort(toCompare, comparator);
        // ListUtils.isEqualList(resources, toCompare);
        for (int i = 0; i < resources.size() - 2; i++) {
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
