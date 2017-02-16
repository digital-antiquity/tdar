package org.tdar.experimental;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.dao.TdarNamedQueries;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.ReflectionService;
import org.tdar.core.service.resource.ResourceService;

public class HqlITCase extends AbstractIntegrationTestCase {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    ResourceService resourceService;

    @Autowired
    ReflectionService reflectionService;

    @Autowired
    GenericKeywordService genericKeywordService;

    private Session session;

    @Before
    public void before() {
        session = getCurrentSession();
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetProjects() {
        Assert.assertNotNull(sessionFactory);
        Query query = getCurrentSession().createQuery("from Resource where id = :id");
        query.setParameter("id", 1L);
        List<Resource> list = query.getResultList();
        assertNotNull(query);
        assertNotNull(list);
        assertTrue(list.size() > 0);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testManyToOne() {
        String queryFormat = "from Resource where %s = :val";
        Query query = session.createQuery(String.format(queryFormat, "submitter_id"));
        query.setParameter("val", 8422L);
        logger.debug("query is:{}", query.getQueryString());
        List list = query.getResultList();
        assertTrue(list.size() > 0);
    }

    @Test
    public void testOneToMany() {
        String str = "select resource from Resource resource inner join resource.resourceCreators rc";
        Query query = session.createQuery(str);
        query.getResultList();
    }

    @Test
    public void testCartesianJoin() {
        Query qp = session.createQuery("from Person");
        Query qr = session.createQuery("from CodingSheet");
        Query qpxr = session.createQuery("select person from Person person, CodingSheet");
        logger.debug("p:{}  r:{}  PxR:{}", new Object[] { qp.list().size(), qr.list().size(), qpxr.list().size() });

        Query codingSheetAuthors = session
                .createQuery("select distinct person from Person person, CodingSheet codingSheet where codingSheet.submitter = person");
        logger.debug("coding sheet authors:{}", codingSheetAuthors.list());
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testInnerJoins() {
        Query query1 = session.createQuery("from Resource resource inner join resource.resourceCreators rc");
        Query query2 = session.createQuery("from Resource");
        List list1 = query1.list();
        List list2 = query2.list();
        logger.debug("resourcecount-join:{}  resourcecount-all:{}", list1.size(), list2.size());
        logger.debug("list1 type:{},\t list2 type:{}", list1.get(0).getClass().getName(), list2.get(0).getClass().getName());
    }

    @Test
    public void testInnerJoinFetch() {
        // note to jim: the difference in the resultant sql is HUGE.
        Query query1 = session.createQuery("from Resource resource inner join  resource.resourceCreators");
        Query query2 = session.createQuery("from Resource resource inner join fetch resource.resourceCreators");
        logger.debug("-------------QUERY 1 ----------");
        logger.debug("size:{}", query1.list());
        logger.debug("-------------QUERY 2 ----------");
        logger.debug("size:{}", query2.list());
    }

    @Test
    public void testSimpleSelect() {
        Query query = session.createQuery("from Resource");
        logger.debug("size:{}", query.getResultList().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testManyToManyParm() {

        // Get the Resource.cultureKeywords field.
        Set<Field> set = reflectionService.findFieldsReferencingClass(Resource.class, CultureKeyword.class);
        Assert.assertEquals(1, set.size());
        Field cultureKeywordField = set.iterator().next();
        logger.debug("Field that references cultureKeyword:{}", cultureKeywordField);

        // this is the list of ids of the keywords we want to find references to.
        // TODO: it would be better to explicitly create our own keywords, and resources that reference them, in this test rather than relying on test database.
        List<Long> idlist = Arrays.asList(120L, 8L, 40L, 25L);
        Set<CultureKeyword> expectedCultureKeywords = new HashSet<CultureKeyword>(genericService.findAll(CultureKeyword.class, idlist));

        // "from %1$s r1 where exists (from %1$s r2 inner join r2.%2$s ck where r2.id = r1.id and ck.id in (:idlist))";
        String hql = String.format(TdarNamedQueries.QUERY_HQL_MANY_TO_MANY_REFERENCES, Resource.class.getSimpleName(), cultureKeywordField.getName());
        Query query = session.createQuery(hql);
        query.setParameter("idlist", idlist);

        List<Resource> results = query.getResultList();
        logger.debug("keywords: {}", results);
        Assert.assertTrue("list shouldn't be empty", CollectionUtils.isNotEmpty(results));
        assertTrue(Collection.class.isAssignableFrom(cultureKeywordField.getType()));

        // Okay, so if this worked, for each of the resources in the results list, the values for the field designated by cultureKeywordField references at
        // least one of the in our id list. Go through each result and confirm this is true.
        for (Resource resource : results) {
            Collection<CultureKeyword> cultureKeywords = reflectionService.callFieldGetter(resource, cultureKeywordField);
            Set<CultureKeyword> cultureKeywordSet = new HashSet<CultureKeyword>(cultureKeywords);
            logger.debug("resource:'{}'\t keywords:{}", resource, cultureKeywordSet);
            cultureKeywordSet.retainAll(expectedCultureKeywords);
            if (cultureKeywordSet.isEmpty()) {
                String fmt = "The following resource does not reference any expected cultureKeywords.  \n\t resource:%s \n\t keywords:%s \n\t expected one of:%s)";
                String err = String.format(fmt, resource, cultureKeywordSet, expectedCultureKeywords);
                fail(err);
            }

        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testManyToOneParm() {
        String hql = String.format(TdarNamedQueries.QUERY_HQL_MANY_TO_ONE_REFERENCES, Resource.class.getSimpleName(), "submitter");
        Query query = session.createQuery(hql);
        query.setParameter("idlist", Arrays.asList(6L, 38L));
        List results = query.getResultList();
        logger.debug("keywords: {}", results);
        Assert.assertTrue("list shouldn't be empty", CollectionUtils.isNotEmpty(results));

    }

    @SuppressWarnings({ "unchecked", "unused" })
    @Test
    public void testQuerySparseEmptyProjects() {
        String hql1 = "select new Project(project.id, project.title) from Project project where (submitter.id=:submitter) and id not in (select resource.project.id from InformationResource resource where resource.status='ACTIVE' and resource.project.id is not null) and (status='ACTIVE' or status='DRAFT')";
        String hql2 = "select new Project(project.id, project.title) from Project project where (submitter.id=:submitter) and not exists(select 1 from InformationResource ir where ir.status='ACTIVE' and ir.project.id = project.id) and project.status in ('ACTIVE', 'DRAFT')";

        List<Project> list1 = session.getNamedQuery(TdarNamedQueries.QUERY_SPARSE_EMPTY_PROJECTS).setParameter("submitter", 8092).list();
        List<Project> list2 = session.createQuery(hql2).setParameter("submitter", 8092).list();
        assertTrue(CollectionUtils.isNotEmpty(list1));
        assertTrue(list1.containsAll(list2));
        assertTrue(list2.containsAll(list1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetReferenceCountManyToMany() {
        Set<Field> set = reflectionService.findFieldsReferencingClass(Resource.class, CultureKeyword.class);
        Field cultureKeywordField = set.iterator().next();
        String hql = String.format(TdarNamedQueries.QUERY_HQL_COUNT_MANY_TO_MANY_REFERENCES_MAP, Resource.class.getSimpleName(), cultureKeywordField.getName());
        Query query = getCurrentSession().createQuery(hql);
        query.setParameter("idlist", Arrays.asList(120L, 8L, 40L, 25L));
        List<Object> list = query.getResultList();
        logger.debug("list size:{}, contents:{}", list.size(), list.toString());

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetReferenceCountManyToOne() {
        String hql = String.format(TdarNamedQueries.QUERY_HQL_COUNT_MANY_TO_ONE_REFERENCES_MAP, Resource.class.getSimpleName(), "submitter");
        Query query = session.createQuery(hql);
        query.setParameter("idlist", Arrays.asList(6L, 38L));
        List results = query.getResultList();
        logger.debug("keywords: {}", results);
        Assert.assertTrue("list shouldn't be empty", CollectionUtils.isNotEmpty(results));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testGetKeywordCounts() {
        String hql = TdarNamedQueries.QUERY_KEYWORD_COUNT_CULTURE_KEYWORD_CONTROLLED;
        Query query = session.getNamedQuery(hql);
        List results = query.getResultList();
        logger.debug("keywords: {}", results);
        for (Object obj : results) {
            Object[] singleResult = (Object[]) obj;
            CultureKeyword ck = (CultureKeyword) singleResult[0];
            Long count = (Long) singleResult[1];
            logger.debug("{}:{}", ck.getLabel(), count);

        }

        Assert.assertTrue("list shouldn't be empty", CollectionUtils.isNotEmpty(results));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testInitDataset() {
        // notice, no "distinct" in query.
        String hql = "from DataTable as dt left join fetch dt.dataTableColumns where dt.displayName = 'qryBone'";
        Query query = session.createQuery(hql);
        // query.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        // query.setRecordsPerPage(10); //implicitly applies "distinct" functionality
        query.setFirstResult(0); // implicitly applies "distinct"
        List results = query.getResultList();
        getLogger().debug("result size:{}", results.size());
        for (Object item : results) {
            getLogger().debug("result[{}] ihc:{}", item, System.identityHashCode(item));
        }
        DataTable dt = (DataTable) results.get(0);
        getLogger().debug("number of columns:{}", dt.getDataTableColumns().size());

    }

    @Test
    public void testCriteriaBuilder() {

    }

}
