package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;

public class InstiutionLookupControllerITCase extends AbstractIntegrationTestCase {

    @Autowired
    private LookupController controller;

    @Before
    public void initController() {
        controller = generateNewInitializedController(LookupController.class);
        controller.setRecordsPerPage(99);
    }

    @Test
    @Rollback(true)
    public void testInstitutionAlone() {
        Person person = new Person("a test", "person", "");
        Institution inst = new Institution("TQF");
        genericService.saveOrUpdate(person);
        person.setInstitution(inst);
        genericService.saveOrUpdate(inst);
        searchIndexService.indexAll(Person.class);
        controller.setInstitution("TQF");
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<Indexable> people = controller.getResults();
        assertTrue("person list should have exactly one item", people.contains(person));
    }

    @Test
    @Rollback(true)
    public void testValidInstitutionWithSpace() {
        searchIndexService.indexAll(Person.class);
        controller.setInstitution("University of");
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<Indexable> people = controller.getResults();
        logger.info("{}", people);
        assertEquals("person list should have exactly two items", 2, people.size());
    }

    @Test
    @Rollback(true)
    public void testInstitutionEmpty() {
        searchIndexService.indexAll(Person.class);
        // FIXME: should not need to be quoted
        controller.setInstitution("University ABCD");
        String result = controller.lookupPerson();
        assertEquals("result should be success", LookupController.SUCCESS, result);
        List<Indexable> people = controller.getResults();
        assertEquals("person list should have 0 item(s)", 0, people.size());
    }

    @Test
    @Rollback(true)
    public void testInstitutionLookupWithNoResults() {
        searchIndexService.indexAll(Institution.class);
        controller.setInstitution("fdaksfddfde");
        controller.lookupInstitution();
        List<Indexable> institutions = controller.getResults();
        assertEquals("person list should be empty", institutions.size(), 0);
    }

    @Test
    public void testInstitutionLookupWithOneResult() {
        searchIndexService.indexAll(Institution.class);
        controller.setInstitution("tfqa");
        controller.lookupInstitution();
        List<Indexable> institutions = controller.getResults();
        assertTrue("only one result expected", institutions.size() == 1);
    }

    // given test script, searching 'digital' should return multiple results that start with 'Digital Antiquity'
    @Test
    public void testInstitutionLookupWithMultiple() {
        searchIndexService.indexAll(Institution.class);
        controller.setInstitution("University");
        controller.lookupInstitution();
        List<Indexable> institutions = controller.getResults();
        assertTrue("more than one result expected", institutions.size() > 1);
    }

    // searching for string witn blanks should yield zero results.
    @Test
    @Rollback(true)
    public void testInstitutionLookupWithBlanks() {
        searchIndexService.indexAll(Institution.class);
        String blanks = "    ";
        controller.setInstitution(blanks);
        controller.lookupInstitution();
        List<Indexable> results = controller.getResults();
        Assert.assertEquals("expecting zero results", 0, results.size());
    }

    public void initControllerFields() {
        controller = generateNewController(LookupController.class);
        init(controller);
    }

    @Test
    @Rollback(true)
    public void testMultiWordInstitution() {
        String name1 = "U.S. Department of the Interior";
        String term = "U.S. Department of";

        List<Institution> insts = setupInstitutionsForLookup();
        controller.setInstitution(term);
        controller.lookupInstitution();
        List<Indexable> results = controller.getResults();
        logger.debug("results:{}", results);
        assertTrue(results.contains(insts.get(0)));
        assertTrue(results.contains(insts.get(insts.size() - 1)));
    }

    @Test
    @Rollback(true)
    public void testPrefixUppercase() {
        List<Institution> insts = setupInstitutionsForLookup();
        String lookingFor = "U.S.";
        initControllerFields();
        controller.setInstitution(lookingFor);
        controller.lookupInstitution();
        List<Indexable> results = controller.getResults();
        logger.debug("results:{}", results);
        for (Indexable indx : results) {
            Institution inst = (Institution) indx;
            logger.info(inst.getName());
             assertTrue("expecting 'u.s.' contained in " + inst.getName(), inst.getName().toLowerCase().contains(lookingFor.toLowerCase()));
        }
    }

    @Test
    @Rollback(true)
    public void testPrefixLowercase() {
        List<Institution> insts = setupInstitutionsForLookup();
        initControllerFields();
        controller.setInstitution("u.s.");
        controller.lookupInstitution();
        List<Indexable> results = controller.getResults();
        logger.debug("results:{}", results);
        for (Indexable indx : results) {
            Institution inst = (Institution) indx;
            assertTrue(inst.getName().toLowerCase().contains("u.s."));
        }
    }

    @Test
    @Rollback(true)
    public void testPrefixWithoutPunctuationMatchesPunctuation() {
        List<Institution> insts = setupInstitutionsForLookup();
        initControllerFields();
        controller.setInstitution("US");
        controller.lookupInstitution();
        List<Indexable> results = controller.getResults();
        logger.debug("results:{}", results);
        for (Indexable indx : results) {
            Institution inst = (Institution) indx;
            assertTrue(inst.getName().toLowerCase().contains("u.s."));
        }

    }

    private List<Institution> setupInstitutionsForLookup() {
        String name1 = "U.S. Department of the Interior";
        searchIndexService.indexAll(Institution.class);
        List<Institution> insts = Arrays.asList(new Institution(name1),
                new Institution("National Geographic Society (U.S.)")
                , new Institution("Robertson Research (U.S.)")
                , new Institution("Southeastern Archaeological Center (U.S.)")
                , new Institution("Southwestern Anthropological Research Group (U.S.)")
                , new Institution("U.S. Department of Agricultural Forest Service"));
        genericService.save(insts);
        searchIndexService.index(insts.toArray(new Institution[0]));
        return insts;
    }

}
