package org.tdar.search.entity;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.service.EntityService;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.SearchResult;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.search.service.query.CreatorSearchService;
import org.tdar.utils.MessageHelper;

public class InstitutionSearchITCase extends AbstractWithIndexIntegrationTestCase {

    @Autowired
    SearchIndexService searchIndexService;

    @Autowired
    EntityService entityService;


    @Autowired
    CreatorSearchService<Institution> creatorSearchService;

    private final int MIN = 2;

    @Override
    public void reindex() {
        searchIndexService.purgeAll(LookupSource.INSTITUTION);
        searchIndexService.indexAll(getAdminUser(), LookupSource.INSTITUTION);
    };

    public List<Institution> setupInstitutionSearch() throws SolrServerException, IOException {
        ArrayList<Institution> insts = new ArrayList<>();
        String[] names = new String[] { "US Air Force", "Vandenberg Air Force Base", "Air Force Base" };
        for (String name : names) {
            Institution institution = new Institution(name);
            updateAndIndex(institution);
            insts.add(institution);
        }
        return insts;
    }

    @Test
    public void testNamePhrase() throws ParseException, SolrServerException, IOException {
    	LuceneSearchResultHandler<Institution> result = new SearchResult<>();
		creatorSearchService.searchInstitution("Arizona State Unive", result, MessageHelper.getInstance());
    }
    
    @Test
    @Rollback
    public void testInstitutionMultiWordSearch() throws ParseException, SolrServerException, IOException {
        String term = "Arizona State";
        searchInstitution(term);
    }

    @Test
    @Rollback
    public void testInstitutionSearch() throws ParseException, SolrServerException, IOException {
        String term = "arizona";
        searchInstitution(term);
    }

    private SearchResult<Institution> searchInstitution(String term) throws ParseException, SolrServerException, IOException {
        return searchInstitution(term, MIN,true);
    }

    private SearchResult<Institution> searchInstitution(String term,boolean testResults) throws ParseException, SolrServerException, IOException {
        return searchInstitution(term, MIN, testResults);
    }

    private SearchResult<Institution> searchInstitution(String term, int min, boolean testResults) throws ParseException, SolrServerException, IOException {
        SearchResult<Institution> result = new SearchResult<>();
        creatorSearchService.findInstitution(term, result, MessageHelper.getInstance(), min);
        logger.debug("{}", result.getResults());
        if (testResults) {
            assertResultsOkay(term, result);
        }
        return result;
    }

    @Test
    @Rollback
    public void testInstitutionSearchWordPlacement() throws SolrServerException, IOException, ParseException {
        List<Institution> insts = setupInstitutionSearch();
        SearchResult<Institution> result = searchInstitution("Air Force");
        assertTrue(CollectionUtils.containsAll(result.getResults(), insts));

        result = searchInstitution("Force");
        assertTrue(CollectionUtils.containsAll(result.getResults(), insts));

    }

    @Test
    @Rollback
    public void testInstitutionSearchCaseInsensitive() throws SolrServerException, IOException, ParseException {
        List<Institution> insts = setupInstitutionSearch();
        SearchResult<Institution> result = searchInstitution("air force");
        assertTrue(CollectionUtils.containsAll(result.getResults(), insts));

        result = searchInstitution("force");
        assertTrue(CollectionUtils.containsAll(result.getResults(), insts));
    }

    private void updateAndIndex(Indexable doc) throws SolrServerException, IOException {
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
    }

    @Test
    @Rollback(true)
    public void testInstitutionWithAcronym() throws SolrServerException, IOException, ParseException {
        Institution inst = new Institution("Arizona State University (ASU)");
        genericService.saveOrUpdate(inst);
        genericService.saveOrUpdate(inst);
        searchIndexService.indexAll(getAdminUser(), LookupSource.INSTITUTION);
        SearchResult<Institution> result = searchInstitution("ASU");
        List<Institution> institutions = result.getResults();
        logger.debug("institutions: {} ", institutions);
        assertTrue("inst list should contain acronym item", institutions.contains(inst));
    }

    @Test
    @Rollback(true)
    public void testInstitutionLookupWithNoResults() throws SolrServerException, IOException, ParseException {
        searchAssertEmpty("fdaksfddfde");
    }

    @Test
    public void testInstitutionLookupWithOneResult() throws SolrServerException, IOException, ParseException {
        SearchResult<Institution> result = searchInstitution("tfqa");
        List<Institution> institutions = result.getResults();
        assertTrue("only one result expected", institutions.size() == 1);
    }

    // given test script, searching 'digital' should return multiple results that start with 'Digital Antiquity'
    @Test
    public void testInstitutionLookupWithMultiple() throws SolrServerException, IOException, ParseException {
        SearchResult<Institution> result = searchInstitution("University");
        List<Institution> institutions = result.getResults();
        assertTrue("more than one result expected", institutions.size() > 1);
    }

    // searching for string witn blanks should yield zero results.
    @Test
    @Rollback(true)
    public void testInstitutionLookupWithBlanks() throws SolrServerException, IOException, ParseException {
        String blanks = "    ";
        searchAssertEmpty(blanks);
    }

    private void searchAssertEmpty(String blanks) throws ParseException, SolrServerException, IOException {
        SearchResult<Institution> result = new SearchResult<>();
        creatorSearchService.findInstitution(blanks, result, MessageHelper.getInstance(), 1);
        List<Institution> results = result.getResults();
        Assert.assertEquals("expecting zero results", 0, results.size());
    }

    @Test
    @Rollback(true)
    public void testMultiWordInstitution() throws SolrServerException, IOException, ParseException {
        String name1 = "U.S. Department of the Interior";
        String term = "U.S. Department of";

        List<Institution> insts = setupInstitutionsForLookup();
        SearchResult<Institution> result = searchInstitution(term);
        List<Institution> results = result.getResults();
        logger.debug("results:{}", results);
        assertTrue(results.contains(insts.get(0)));
        assertTrue(results.contains(insts.get(insts.size() - 1)));
    }

    @Test
    @Rollback(true)
    public void testPrefixUppercase() throws SolrServerException, IOException, ParseException {
        List<Institution> insts = setupInstitutionsForLookup();
        String lookingFor = "U.S.";
        SearchResult<Institution> result = searchInstitution(lookingFor);
        List<Institution> results = result.getResults();
        logger.debug("results:{}", results);
        for (Institution inst : results) {
            logger.info(inst.getName());
            assertTrue("expecting 'u.s.' contained in " + inst.getName(), inst.getName().toLowerCase().contains(lookingFor.toLowerCase()));
        }
    }

    @Test
    @Rollback(true)
    public void testPrefixLowercase() throws SolrServerException, IOException, ParseException {
        List<Institution> insts = setupInstitutionsForLookup();
        SearchResult<Institution> result = searchInstitution("u.s.");
        List<Institution> results = result.getResults();
        logger.debug("results:{}", results);
        for (Institution inst : results) {
            assertTrue(inst.getName().toLowerCase().contains("u.s."));
        }
    }

    @Test
    @Rollback(true)
    public void testPrefixWithoutPunctuationMatchesPunctuation() throws SolrServerException, IOException, ParseException {
        Institution in1 = new Institution("U.S. Depoartment of Agriculture");
        Institution in2 = new Institution("US Depoartment of Agriculture");
        genericService.save(in1);
        genericService.save(in2);
        SearchResult<Institution> results = searchInstitution("US",false);
        logger.debug("results:{}", results);
        boolean seenPeriod = false;
        boolean seenWithoutPeriod = false;
        for (Institution inst : results.getResults()) {
            logger.debug("{}", inst.getName());
            if (inst.getName().toLowerCase().contains("u.s.")) {
                seenPeriod = true;
            }
            if (inst.getName().toLowerCase().contains("us")) {
                seenWithoutPeriod = true;
            }
        }
        assertTrue("seen U.S.",seenPeriod);
        assertTrue("seen US",seenWithoutPeriod);

    }

    private List<Institution> setupInstitutionsForLookup() throws SolrServerException, IOException {
        String name1 = "U.S. Department of the Interior";
        List<Institution> insts = Arrays.asList(new Institution(name1),
                new Institution("National Geographic Society (U.S.)"), new Institution("Robertson Research (U.S.)"),
                new Institution("Southeastern Archaeological Center (U.S.)"), new Institution("Southwestern Anthropological Research Group (U.S.)"),
                new Institution("U.S. Department of Agricultural Forest Service"));
        genericService.save(insts);
        searchIndexService.index(insts.toArray(new Institution[0]));
        return insts;
    }

    private void assertResultsOkay(String term, SearchResult<Institution> controller_) {
        assertNotEmpty(controller_.getResults());
        for (Institution inst : controller_.getResults()) {
            assertTrue(String.format("Creator: %s should match %s", inst, term), inst.getProperName().toLowerCase().contains(term.toLowerCase()));
        }
        logger.info("{}", controller_.getResults());
    }

}
