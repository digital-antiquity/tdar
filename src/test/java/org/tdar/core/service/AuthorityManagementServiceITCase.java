package org.tdar.core.service;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.hibernate.ScrollableResults;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.TestConstants;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Dedupable;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.AuthorityManagementService.AuthorityManagementLog;

public class AuthorityManagementServiceITCase extends AbstractIntegrationTestCase {

    boolean oldActuallySend;

    @Autowired
    AuthorityManagementService authorityManagementService;

    @Autowired
    EntityService entityService;

    @Autowired
    GenericService genericService;

    @Autowired
    GenericKeywordService genericKeywordService;

    @Autowired
    private XmlService xmlService;

    @Test
    public void testCreatorPersonReferrers() {

        // a better test would be to make some references with a new person, and makes sure that those references are found.
        Map<Field, ScrollableResults> referrers = authorityManagementService.getReferrers(Creator.class, new HashSet<Long>(Arrays.asList(6L)));
        Assert.assertFalse(referrers.isEmpty());
    }

    @Test
    @Rollback
    public void testKeywordReferrerCount() throws InstantiationException, IllegalAccessException {
        // find any geokeyword's reference count, add it to a new resource, and then assert that the reference count increased;
        GeographicKeyword keyword = genericService.findAll(GeographicKeyword.class).iterator().next();
        long origCount = authorityManagementService.getTotalReferrerCount(keyword.getClass(), Arrays.asList(keyword.getId()));
        Document document = createAndSaveNewInformationResource(Document.class);
        document.getGeographicKeywords().add(keyword);
        resourceService.save(document);
        long newCount = authorityManagementService.getTotalReferrerCount(keyword.getClass(), Arrays.asList(keyword.getId()));
        logger.debug("original count:{}\tnew count:{}", origCount, newCount);
        Assert.assertTrue("reference count should have increased by one and only one. ref1:" + origCount + " ref2:" + newCount,
                newCount == origCount + 1);
    }

    @Test
    @Rollback
    public void testFindInstitutionByName() {
        Institution inst = new Institution("University of TEST");
        ResourceCreator creator = new ResourceCreator(inst, ResourceCreatorRole.AUTHOR);
        entityService.findOrSaveResourceCreator(creator);
        assertEquals(TestConstants.TEST_INSTITUTION_ID, creator.getCreator().getId());
    }

    @Test
    @Rollback
    public void testReferrerCountMap() throws Exception {

        List<GeographicKeyword> allKeywords = genericService.findAll(GeographicKeyword.class);
        GeographicKeyword keyword = allKeywords.iterator().next();
        List<Long> ids = new ArrayList<Long>();
        for (GeographicKeyword gk : allKeywords) {
            ids.add(gk.getId());
        }

        Map<Long, Long> origMap = authorityManagementService.getReferrerCountMaps(keyword.getClass(), ids);

        Document document = createAndSaveNewInformationResource(Document.class);
        document.getGeographicKeywords().addAll(allKeywords);
        resourceService.save(document);
        Map<Long, Long> newMap = authorityManagementService.getReferrerCountMaps(keyword.getClass(), ids);

        for (Map.Entry<Long, Long> entry : origMap.entrySet()) {
            Long id = entry.getKey();
            Long origCount = entry.getValue();
            Long newCount = newMap.get(id);
            logger.debug("keyword id:{}  oldcount:{}  newcount:{}", new Object[] { id, origCount, newCount });
            Assert.assertTrue("itemcount should increase by 1 and only 1", origCount + 1 == newCount);
        }

        Assert.assertTrue(!origMap.isEmpty());

    }

    @Test
    @Rollback
    public void testDedupeManyToOne() throws Exception {
        Person authority = new Person("John", "Doe", "authority_record@bar.com");
        Person dupe1 = new Person("John", "Dough", "johndough@bar.com");
        Person dupe2 = new Person("John", "D'oh", "johndoh@bar.com");
        genericService.save(authority);
        genericService.save(dupe1);
        genericService.save(dupe2);

        Long authorityId = authority.getId();
        Long dupe1Id = dupe1.getId();
        Long dupe2Id = dupe2.getId();

        // create some many-to-one references
        Document d1 = createAndSaveNewInformationResource(Document.class);
        d1.setSubmitter(dupe1);

        Document d2 = createAndSaveNewInformationResource(Document.class);
        d2.setUpdatedBy(dupe2);
        resourceService.save(d1);
        resourceService.save(d2);
        
        // ResourceCreator is trickier than the others because the creator field may refer to Institution or Person
        ResourceCreator resourceCreator = new ResourceCreator(dupe1, ResourceCreatorRole.AUTHOR);
        resourceCreator.setSequenceNumber(1);
        d1.getResourceCreators().add(resourceCreator);

        AuthorizedUser user1 = new AuthorizedUser(dupe1, GeneralPermissions.ADMINISTER_GROUP);
        ResourceCollection resourceCollection = genericService.findAll(ResourceCollection.class).iterator().next();
        resourceCollection.getAuthorizedUsers().add(user1);
        entityService.save(user1);

        // great, now lets do some deduping;
        Set<Long> dupeIds = new HashSet<Long>(Arrays.asList(dupe1Id, dupe2Id));
        authorityManagementService.updateReferrers(Person.class, dupeIds, authorityId, false);

        // makes sure that the dupes no longer exist
        Assert.assertEquals("dupe should be deleted:" + dupe1, Status.DUPLICATE, entityService.find(dupe1Id).getStatus());
        Assert.assertEquals("dupe should be deleted:" + dupe2, Status.DUPLICATE, entityService.find(dupe2Id).getStatus());

        // todo: make sure that the authority replaced all the dupes of the former referrers
        d1 = genericService.find(Document.class, d1.getId());
        d2 = genericService.find(Document.class, d2.getId());
        user1 = genericService.find(AuthorizedUser.class, user1.getId());

        Assert.assertEquals("authority should have replaced dupe", authority, d1.getSubmitter());
        Assert.assertEquals("authority should have replaced dupe", authority, d2.getUpdatedBy());
        Assert.assertEquals("authority should have replaced dupe", authority, user1.getUser());
    }

    @Test
    @Rollback
    public void testDedupeManyToMany() throws Exception {

        OtherKeyword authority = createAndSaveKeyword(OtherKeyword.class, "authority keyword", "this is the authority keyword");
        OtherKeyword dupe1 = createAndSaveKeyword(OtherKeyword.class, "dupe keyword", "this is not the authority keyword");
        OtherKeyword dupe2 = createAndSaveKeyword(OtherKeyword.class, "another dupe keyword", "this is not the authority keyword");
        Long authorityId = authority.getId(), dupe1Id = dupe1.getId(), dupe2Id = dupe2.getId();

        // okay, now let's make some references
        Document doc1 = createAndSaveNewInformationResource(Document.class);
        Document doc2 = createAndSaveNewInformationResource(Document.class);

        doc1.getOtherKeywords().add(dupe1);
        doc2.getOtherKeywords().add(dupe1);
        doc2.getOtherKeywords().add(dupe2);
        doc2.getOtherKeywords().add(authority);

        resourceService.save(doc1);
        resourceService.save(doc2);

        // great, now lets do some deduping;
        Set<Long> dupeIds = new HashSet<Long>(Arrays.asList(dupe1Id, dupe2Id));
        authorityManagementService.updateReferrers(OtherKeyword.class, dupeIds, authorityId, false);

        // makes sure that the dupes no longer exist
        Assert.assertEquals("dupe should be deleted:" + dupe1, Status.DUPLICATE, genericKeywordService.find(OtherKeyword.class, dupe1Id).getStatus());
        Assert.assertEquals("dupe should be deleted:" + dupe2, Status.DUPLICATE, genericKeywordService.find(OtherKeyword.class, dupe2Id).getStatus());

        // todo: make sure that the authority replaced all the dupes of the former referrers
        doc1 = genericService.find(Document.class, doc1.getId());
        doc2 = genericService.find(Document.class, doc2.getId());

        Assert.assertTrue("authority should replace dupe", doc1.getOtherKeywords().contains(authority));
        Assert.assertTrue("authority should replace dupe", doc2.getOtherKeywords().contains(authority));
    }

    @Test
    @Rollback
    public void testDedupeKeywordsSynonyms() throws InstantiationException, IllegalAccessException {
        // ensure the service adds synonyms to authority when deduping
        testDedupeKeywordSynonyms(GeographicKeyword.class);
        testDedupeKeywordSynonyms(InvestigationType.class);
        testDedupeKeywordSynonyms(MaterialKeyword.class);
        testDedupeKeywordSynonyms(OtherKeyword.class);
        testDedupeKeywordSynonyms(SiteNameKeyword.class);
        testDedupeKeywordSynonyms(TemporalKeyword.class);
        testDedupeKeywordSynonyms(CultureKeyword.class);
        testDedupeKeywordSynonyms(SiteTypeKeyword.class);

    }

    @Test
    @Rollback
    public void testDedupeInstitutionSynonyms() {
        Institution authority = new Institution("The Real McCoy LLC.");
        Institution dupe = new Institution("Department of Redundancy Department");
        saveAndTestDedupeSynonym(Institution.class, authority, dupe);
    }

    @Test
    @Rollback
    public void testLogSerialization() throws SecurityException, NoSuchFieldException {
        Person person = entityService.find(1L);
        Set<Person> dupes = new HashSet<Person>(genericService.findRandom(Person.class, 5));
        dupes.remove(person);

        AuthorityManagementLog<Person> result = new AuthorityManagementLog<Person>(person, dupes);
        Person firstDupe = dupes.iterator().next();
        Document d = new Document();
        // WE ARE TESTING LOGGING AND NOTHING ELSE -- yes, THIS IS INSANE
        result.add(d, Document.class.getDeclaredField("edition"), firstDupe);
        logger.debug("result: {}", result);
        try {
            String xml = xmlService.convertToXML(result);
            logger.debug(xml);
        } catch (Exception e) {
            logger.debug("grr: {} ", e);
            Assert.fail();
        }
    }

    private <K extends Keyword> void testDedupeKeywordSynonyms(Class<K> type) throws InstantiationException, IllegalAccessException {
        K authority = type.newInstance();
        K dupe = type.newInstance();
        authority.setDefinition("auth def");
        authority.setLabel("auth label");
        dupe.setDefinition("dupe def");
        dupe.setLabel("dupe label");
        saveAndTestDedupeSynonym(type, authority, dupe);
    }

    private <T extends Keyword> T createAndSaveKeyword(Class<T> type, String label, String definition) {
        T keyword;
        try {
            keyword = type.newInstance();
            keyword.setLabel(label);
            keyword.setDefinition(definition);
            genericService.save(keyword);
        } catch (Exception ex) {
            logger.error("create keyword failed", ex);
            return null;
        }
        return keyword;
    }

    private <D extends Dedupable<?>> void saveAndTestDedupeSynonym(Class<D> type, D authority, D dupe) {
        genericService.save(authority);
        genericService.save(dupe);
        authorityManagementService.updateReferrers(type, new HashSet<Long>(Arrays.asList(dupe.getId())), authority.getId(), false);
//        dupe = null;
        String message = "authority should have synonym '" + dupe + "' after deduping " + type.getSimpleName() + " record";
        Assert.assertTrue(message, authority.getSynonyms().contains(dupe));
    }
}
