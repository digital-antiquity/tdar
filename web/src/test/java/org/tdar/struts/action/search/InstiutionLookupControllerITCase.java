package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.entity.Institution;
import org.tdar.struts.action.AbstractIntegrationControllerTestCase;
import org.tdar.struts.action.lookup.InstitutionLookupAction;

import com.opensymphony.xwork2.Action;

public class InstiutionLookupControllerITCase extends AbstractIntegrationControllerTestCase {

    @Autowired
    private InstitutionLookupAction controller;

    @Before
    public void initController() {
        controller = generateNewInitializedController(InstitutionLookupAction.class);
        controller.setRecordsPerPage(99);
    }

    @Test
    @Rollback(true)
    public void testInstitutionWithAcronym() throws SolrServerException, IOException {
        Institution inst = new Institution("Arizona State University (ASU)");
        genericService.saveOrUpdate(inst);
        genericService.saveOrUpdate(inst);
        searchIndexService.indexAll(getAdminUser(), Institution.class);
        controller.setInstitution("ASU");
        String result = controller.lookupInstitution();
        assertEquals("result should be success", Action.SUCCESS, result);
        List<Institution> institutions = controller.getResults();
        logger.debug("institutions: {} ", institutions);
        assertTrue("inst list should contain acronym item", institutions.contains(inst));
    }

    @Test
    @Rollback(true)
    public void testInstitutionLookupWithNoResults() throws SolrServerException, IOException {
        searchIndexService.indexAll(getAdminUser(), Institution.class);
        controller.setInstitution("fdaksfddfde");
        controller.lookupInstitution();
        List<Institution> institutions = controller.getResults();
        assertEquals("person list should be empty", institutions.size(), 0);
    }

    @Test
    public void testInstitutionLookupWithOneResult() throws SolrServerException, IOException {
        searchIndexService.indexAll(getAdminUser(), Institution.class);
        controller.setInstitution("tfqa");
        controller.lookupInstitution();
        List<Institution> institutions = controller.getResults();
        assertTrue("only one result expected", institutions.size() == 1);
    }

    // given test script, searching 'digital' should return multiple results that start with 'Digital Antiquity'
    @Test
    public void testInstitutionLookupWithMultiple() throws SolrServerException, IOException {
        searchIndexService.indexAll(getAdminUser(), Institution.class);
        controller.setInstitution("University");
        controller.lookupInstitution();
        List<Institution> institutions = controller.getResults();
        assertTrue("more than one result expected", institutions.size() > 1);
    }

    // searching for string witn blanks should yield zero results.
    @Test
    @Rollback(true)
    public void testInstitutionLookupWithBlanks() throws SolrServerException, IOException {
        searchIndexService.indexAll(getAdminUser(), Institution.class);
        String blanks = "    ";
        controller.setInstitution(blanks);
        controller.lookupInstitution();
        List<Institution> results = controller.getResults();
        Assert.assertEquals("expecting zero results", 0, results.size());
    }

    public void initControllerFields() {
        controller = generateNewController(InstitutionLookupAction.class);
        init(controller);
    }

    @Test
    @Rollback(true)
    public void testMultiWordInstitution() throws SolrServerException, IOException {
        String name1 = "U.S. Department of the Interior";
        String term = "U.S. Department of";

        List<Institution> insts = setupInstitutionsForLookup();
        controller.setInstitution(term);
        controller.lookupInstitution();
        List<Institution> results = controller.getResults();
        logger.debug("results:{}", results);
        assertTrue(results.contains(insts.get(0)));
        assertTrue(results.contains(insts.get(insts.size() - 1)));
    }

    @Test
    @Rollback(true)
    public void testPrefixUppercase() throws SolrServerException, IOException {
        List<Institution> insts = setupInstitutionsForLookup();
        String lookingFor = "U.S.";
        initControllerFields();
        controller.setInstitution(lookingFor);
        controller.lookupInstitution();
        List<Institution> results = controller.getResults();
        logger.debug("results:{}", results);
        for (Indexable indx : results) {
            Institution inst = (Institution) indx;
            logger.info(inst.getName());
            assertTrue("expecting 'u.s.' contained in " + inst.getName(), inst.getName().toLowerCase().contains(lookingFor.toLowerCase()));
        }
    }

    @Test
    @Rollback(true)
    public void testPrefixLowercase() throws SolrServerException, IOException {
        List<Institution> insts = setupInstitutionsForLookup();
        initControllerFields();
        controller.setInstitution("u.s.");
        controller.lookupInstitution();
        List<Institution> results = controller.getResults();
        logger.debug("results:{}", results);
        for (Indexable indx : results) {
            Institution inst = (Institution) indx;
            assertTrue(inst.getName().toLowerCase().contains("u.s."));
        }
    }

    @Test
    @Rollback(true)
    public void testPrefixWithoutPunctuationMatchesPunctuation() throws SolrServerException, IOException {
        List<Institution> insts = setupInstitutionsForLookup();
        initControllerFields();
        controller.setInstitution("US");
        controller.lookupInstitution();
        List<Institution> results = controller.getResults();
        logger.debug("results:{}", results);
        for (Indexable indx : results) {
            Institution inst = (Institution) indx;
            assertTrue(inst.getName().toLowerCase().contains("u.s."));
        }

    }

    private List<Institution> setupInstitutionsForLookup() throws SolrServerException, IOException {
        String name1 = "U.S. Department of the Interior";
        searchIndexService.indexAll(getAdminUser(), Institution.class);
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
