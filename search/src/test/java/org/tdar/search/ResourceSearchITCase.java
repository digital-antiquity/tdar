package org.tdar.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.AbstractWithIndexIntegrationTestCase;
import org.tdar.TestConstants;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.SortOption;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.entity.ResourceCreatorRole;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.query.SearchResult;
import org.tdar.search.query.SearchResultHandler.ProjectionModel;
import org.tdar.search.query.builder.QueryBuilder;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.service.CreatorSearchService;
import org.tdar.search.service.ReservedSearchParameters;
import org.tdar.search.service.ResourceSearchService;
import org.tdar.search.service.SearchService;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;

public class ResourceSearchITCase extends AbstractWithIndexIntegrationTestCase {

    @Autowired
    SearchService searchService;

    @Autowired
    CreatorSearchService creatorSearchService;

    public static final String REASON = "because";

    @Test
    @Rollback(true)
    public void testCreatorOwnerQueryPart() throws ParseException, SolrServerException, IOException {
        QueryBuilder rqb = creatorSearchService.generateQueryForRelatedResources(getAdminUser(), null, MessageHelper.getInstance());
        Document authorDocument = new Document();
        authorDocument.setTitle("author");
        authorDocument.setDescription(REASON);
        authorDocument.markUpdated(getBasicUser());
        genericService.saveOrUpdate(authorDocument);
        authorDocument.getResourceCreators().add(new ResourceCreator(getAdminUser(), ResourceCreatorRole.AUTHOR));
        genericService.saveOrUpdate(authorDocument);
        searchIndexService.index(authorDocument);

        Document contribDocument = new Document();
        contribDocument.setTitle("contrib");
        contribDocument.setDescription(REASON);
        contribDocument.markUpdated(getBasicUser());
        genericService.saveOrUpdate(contribDocument);
        contribDocument.getResourceCreators().add(new ResourceCreator(getAdminUser(), ResourceCreatorRole.CONTACT));
        genericService.saveOrUpdate(contribDocument);
        searchIndexService.index(contribDocument);

        Document ownerDocument = new Document();
        ownerDocument.setTitle("owner");
        ownerDocument.setDescription(REASON);
        ownerDocument.markUpdated(getAdminUser());
        genericService.saveOrUpdate(ownerDocument);
        searchIndexService.index(ownerDocument);

        Document hiddenDocument = new Document();
        hiddenDocument.setTitle("hidden");
        hiddenDocument.setDescription(REASON);
        hiddenDocument.markUpdated(getAdminUser());
        genericService.saveOrUpdate(hiddenDocument);
        hiddenDocument.getResourceCreators().add(new ResourceCreator(getBasicUser(), ResourceCreatorRole.AUTHOR));
        genericService.saveOrUpdate(authorDocument);
        searchIndexService.index(hiddenDocument);

        assertFalse(rqb.isEmpty());
        SearchResult result = new SearchResult();
        result.setProjectionModel(ProjectionModel.HIBERNATE_DEFAULT);
        result.setSortField(SortOption.RELEVANCE);
        searchService.handleSearch(rqb, result, MessageHelper.getInstance());
        for (Resource r : (List<Resource>) (List<?>) result.getResults()) {
            List<Long> authorIds = new ArrayList<Long>();
            for (ResourceCreator cr : r.getContentOwners()) {
                authorIds.add(cr.getCreator().getId());
            }
            logger.debug("result: {} id:{} [s:{} | {}]", r.getTitle(), r.getId(), r.getSubmitter().getId(), authorIds);
        }
        assertFalse(result.getResults().contains(hiddenDocument));
        assertFalse(result.getResults().contains(contribDocument));
        assertTrue(result.getResults().contains(authorDocument));
        assertTrue(result.getResults().contains(ownerDocument));
    }

    private static final String L_BL_AW = "l[]bl aw\\";

    @Test
    @Rollback(true)
    public void testSelectedResourceLookup() throws SolrServerException, IOException, ParseException {
        ResourceCollection collection = new ResourceCollection("test", "test", SortOption.TITLE, CollectionType.SHARED, true, getUser());
        collection.markUpdated(getUser());
        Ontology ont = createAndSaveNewInformationResource(Ontology.class);
        genericService.saveOrUpdate(collection);
        collection.getResources().add(ont);
        // babysitting bidirectional relationshi[
        genericService.saveOrUpdate(collection);
        ont.getResourceCollections().add(collection);
        genericService.saveOrUpdate(ont);
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        ReservedSearchParameters params = new ReservedSearchParameters();
        params.setResourceTypes(Arrays.asList(ResourceType.ONTOLOGY));
        SearchResult result = performSearch("", null, collection.getId(), null, null, null, params, 100);
        assertFalse(result.getResults().isEmpty());
        assertTrue(result.getResults().contains(ont));
    }
    
    @Override
    public void reindex() {
        searchIndexService.purgeAll(Arrays.asList(Resource.class));
        searchIndexService.indexAll(getAdminUser(), Resource.class);
    }

    @Test
    @Rollback(true)
    public void testModifyEditor() throws SolrServerException, IOException, ParseException {
        ReservedSearchParameters params = new ReservedSearchParameters();

        SearchResult result = performSearch("", null, null, null, null, getEditorUser(), params, GeneralPermissions.MODIFY_METADATA, 1000);
        logger.debug("results:{}", result.getResults());
        List<Long> ids = PersistableUtils.extractIds(result.getResults());

        result = performSearch("", null, null, null, null, getAdminUser(), params, GeneralPermissions.MODIFY_METADATA, 1000);
        logger.debug("results:{}", result.getResults());
        List<Long> ids2 = PersistableUtils.extractIds(result.getResults());
        Assert.assertArrayEquals(ids.toArray(), ids2.toArray());
    }

    @Test
    @Rollback(true)
    public void testLookupByTitle() throws InstantiationException, IllegalAccessException, SolrServerException, IOException, ParseException {
        String[] titles = new String[] { "CARP Fauna Side or Symmetry", "CARP Fauna Completeness (Condition)", "CARP Fauna Origin of Fragmentation",
                "CARP Fauna Proximal-Distal", " CARP Fauna Dorsal-Ventral", "CARP Fauna Fusion", "CARP Fauna Burning", "CARP Fauna Bone Artifacts",
                "CARP Fauna Gnawing", "CARP Fauna Natural Modification", "CARP Fauna Element", "CARP Fauna Butchering",
                "CARP Fauna Species Alternate Ontology - Scientific Name", "Carp Elements", "CARP Condition", "HARP Fauna Condition Coding Sheet",
                "HARP Fauna Element Coding Sheet", "HARP Fauna Species Coding Sheet", "HARP Fauna Side Coding Sheet", "EMAP_fauna_taxon", "EMAP_fauna_taxa",
                "EMAP_fauna_taxa", "EMAP_fauna_element", "Powell_coding_mammal_taxa", "Powell_coding_nonmammal_taxa", "Powell_coding_symmetry",
                "Powell_coding_side", "Powell_coding_sex", "EMAP_breakage", "EMAP_fauna_element", "Region Coding Sheet (Valley of Mexico Project)",
                "Valley of Mexico Region Coding Sheet V. 2", "HARP Fauna Burning Coding Sheet", "HARP Fauna Butchering Coding Sheet",
                "HARP Fauna Post-depositional Processes Coding Sheet", "EMAP fauna breakage codes", "EMAP fauna class codes", "EMAP fauna element codes",
                "EMAP fauna modification codes", "EMAP fauna period codes", "EMAP fauna taxon codes", "Koster Site Fauna Burning Coding Sheet",
                "HARP Fauna Dorsal/Ventral Coding Sheet", "HARP Fauna Proximal/Distal Coding Sheet", "Koster Site Fauna Certainty Coding Sheet",
                "Koster Site Fauna Analyst Coding Sheet", "Koster Site Fauna Species Coding Sheet (Test)", "Koster Site Fauna Side Coding Sheet",
                "Koster Site Fauna Integrity Coding Sheet", "Koster Site Fauna Portion Coding Sheet", "Koster Site Fauna Element Coding Sheet",
                "Koster Site Fauna Feature/Midden Coding Sheet", "Koster Site Fauna Horizon Feature/Midden Coding Sheet",
                "Koster Site Fauna Other Integ Coding Sheet", "Koster Site Species Coding Sheet", "Durrington Walls - Coding Sheet - Fauna -  Fusion  ",
                "Knowth - Coding Sheet - Fauna - Fusion", "Knowth - Coding Sheet - Fauna - Species", "GQ burning coding sheet", "Koster burning",
                "Koster Burning test2", "HARP Fauna Fusion Coding Sheet", "HARP Fauna Modification Coding Sheet",
                "CARP Fauna Species Alternate Ontology - Common Name", "CARP Fauna Species Scientifc (Common)", "Species Coding Sheet (TAG Workpackage 2)",
                "Bone Coding Sheet  (TAG workpackage 2)", "Chew type Coding Sheet (TAG Workpackage 2)", "Condition Coding Sheet (TAG Workpackage 2)",
                "Erosion Coding Sheet (TAG Workshop Package 2)", "Size Coding Sheet (TAG Workpackage 2)", "Zone Coding Sheet (TAG Workpackage 2)",
                "RCAP Coding Sheet - Context", "GQ butchering coding sheet", "GQ dorsal-ventral coding key", "GQ Element coding key", "GQ Fusion coding key",
                "GQ origin fragmentation coding key", "GQ sex coding key", "GQ Modification coding key", "GQ Proximal-distal coding key", "GQ side coding key",
                "GQ Time period coding key", "GQ species coding key", "GQ condition coding key", "Preservation-Lookup", "Pueblo Blanco Temporal Codes",
                "Pueblo Blanco Species codes", "Pueblo Colorado Temporal Periods", "OLD Taxon coding sheet for CCAC - needs to be deleted",
                "String Code Coding Sheet - Text Box", "String Code Test Coding Sheet from CSV", "CCAC Taxon Coding Sheet",
                "OUTDATED CCAC element coding sheet - needs deletion", "OUTDATED Part coding sheet for CCAC - needs to be deleted", "Site Coding Sheet",
                "New Bridge & Carlin Sites Taxon Coding Sheet SMDraft", "Subperiod I & II Coding Sheet (Valley of Mexico Project)",
                "Occupation Coding Sheet (Valley of Mexico)", "Survey Code Coding Sheet (Valley of Mexico)", "Region Coding Sheet (Valley of Mexico)",
                "Period Coding Sheet (Valley of Mexico Project)", "Phase/Period codes for Taraco Archaeological Survey",
                "Environmental Zones for Taraco Peninsula Site Database", "sutype", "sutype", "Spitalfields Project Periods Coding Sheet",
                "Museum of London Archaeology fauna bone part coding sheet", "Museum of London Archaeology fauna bone modification codes",
                "Kitchell Mortuary Vessel Data Coding Sheet", "Alexandria Period Pre/Post 1680 Aggregation Coding Sheet",
                "Side coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Fusion coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Breakage coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Modification coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Length coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Thickness coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "FAT coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "FAP coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "SUDesc coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "SUType coding sheet for Crow Canyon Archaeological Center fauna through 2008", "Albert Porter ComponID coding sheet, CCAC fauna through 2008",
                "Albert Porter ComponID coding sheet, CCAC fauna", "Albert Porter ComponID coding sheet, CCAC fauna",
                "Woods Canyon ComponID coding sheet, CCAC fauna", "Castle Rock ComponID coding sheet, CCAC fauna",
                "Shields Pueblo ComponID coding sheet, CCAC fauna", "Yellow Jacket Pueblo ComponID coding sheet, CCAC fauna",
                "Sand Canyon ComponID coding sheet, CCAC fauna", "FeTyp coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Length coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Modification coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Side coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "SUDesc coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "SUType coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Thickness coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Thickness coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Breakage coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "FAP coding sheet for Crow Canyon Archaeological Center fauna through 2008", "Woods Canyon ComponID coding sheet, CCAC fauna",
                "Albert Porter ComponID coding sheet, CCAC fauna through 2008", "FAT coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Fusion coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Element coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Part coding sheet for Crow Canyon Archaeological Center fauna through 2008",
                "Taxon coding sheet for Crow Canyon Archaeological Center fauna through 2008", "DAI - CTYPE",
                "Sand Canyon locality testing project ComponID coding sheet, CCAC fauna", "DAI - SIZE", "TUTORIAL Color Coding Sheet",
                "TUTORIAL Element Coding Sheet", "TUTORIAL Element Coding Sheet", "TUTORIAL Element Coding Sheet", "TUTORIAL Screen Size",
                "TUTORIAL Coding Sheet Size", "TUTORIAL Coding Sheet Context", "TUTORIAL Coding Sheet Context", "DAI - SHAPE", "NSF2002 - Date codes",
                "DAI - DATES", "DAI - PART", "DAI - TSG", "DAI -TT", "NSF2002 - Temper type", "NSF2002 - Part codes", "NSF2002 - Size codes",
                "NSF2002 - Shape codes", "Soil Systems, Inc. General Artifact Coding Sheet", "Soil Systems, Inc. Ceramic Temper Coding Sheet",
                "Soil Systems, Inc. Ceramic Ware and Type Coding Sheet", "Soil Systems, Inc .Cremation Interment Type Coding Sheet",
                "Soil Systems, Inc. Vessel Form Coding Sheet", "Soil Systems, Inc. Vessel Rim Angle Coding Sheet",
                "Soil Systems, Inc. Vessel Rim Fillet Coding Sheet", "Soil Systems, Inc. Vessel Rim Lip Shape Coding Sheet",
                "Soil Systems, Inc. Sherd Temper Coding Sheet", "Soil Systems, Inc. Structural Unit Type Coding Sheet",
                "Soil Systems, Inc. Feature Type Coding Sheet", "Soil Systems, Inc. Collection Unit Size Coding Sheet",
                "Soil Systems, Inc. Collection Unit Type Coding Sheet", "Soil Systems, Inc. Collection Unit Method Coding Sheet",
                "Soil Systems, Inc. Provenience Elevation Reference Coding Sheet", "Soil Systems, Inc. Context Coding Sheet",
                "Soil Systems, Inc. Provenience Integrity Coding Sheet", "asd", "Soil Systems, Inc. Inhumation Alcove Position Coding Sheet",
                "Soil Systems, Inc. Inhumation Arm Position Coding Sheet", "Soil Systems, Inc. Inhumation Body Position Coding Sheet",
                "Body Position Codes from SSI Inhumation Form", "Soil Systems, Inc. Inhumation Burial Pit Integrity Coding Sheet",
                "Soil Systems, Inc. Inhumation Burning Coding Sheet", "Soil Systems, Inc. Cremation Fill Type Coding Sheet",
                "Soil Systems, Inc. Cremation Pit Burning Coding Sheet", "Soil Systems, Inc. Cremation Pit Integrity Coding Sheet",
                "Soil Systems, Inc. Cremation Grave Orientation Coding Sheet", "Soil Systems, Inc. Inhumation Grave Planview Shape Coding Sheet",
                "Soil Systems, Inc. Inhumation Grave Profile Shape Coding Sheet", "Soil Systems, Inc. Cremation Grave Type Coding Sheet",
                "Soil Systems, Inc. Inhumation Head Facing Coding Sheet", "Soil Systems, Inc. Inhumation Head Location Coding Sheet",
                "Soil Systems, Inc. Inhumation Impressions Coding Sheet", "Soil Systems, Inc. Inhumation Grave Fill Type Coding Sheet",
                "Soil Systems, Inc. Inhumation Pit Integrity Coding Sheet", "Soil Systems, Inc. Inhumation Leg Positions Coding Sheet",
                "Soil Systems, Inc. Cremation Location for Remains Coding Sheet", "Soil Systems, Inc. Inhumation Color of Minerals & Staining Coding Sheet",
                "Soil Systems, Inc. Inhumation Location of Minerals & Staining on the Body Coding Sheet",
                "Soil Systems, Inc. Inhumation Minerals & Staining Coding Sheets", "Soil Systems, Inc. Inhumation & Cremation Multiple Burial Coding Sheet",
                "Soil Systems, Inc. Inhuamtion Skeletal Disturbance Type Coding Sheet", "Soil Systems, Inc. Inhumation Pit Disturbance Type Coding Sheet",
                "Pit Disturbance Type Codes from SSI Inhumation Form", "Soil Systems, Inc. Inhumation Skeletal Preservation Coding Sheet",
                "Soil Systems, Inc. Inhumation Superstructure Position Coding Sheet", "Soil Systems, Inc. Inhumation Superstructure Type Coding Sheet",
                "Soil Systems, Inc. Inhumation Surrounding Fill Coding Sheet", "tet", "Soil Systems, Inc. Ornament Type Coding Sheet",
                "Soil Systems, Inc. Ornament Material Type Coding Sheet", "Soil Systems, Inc. Ornament Shape Coding Sheet",
                "Soil Systems, Inc. Ornament Condition Coding Sheet", "Soil Systems, Inc. Ornament Burning Coding Sheet",
                "Soil Systems, Inc. Shell Ornament Umbo Shape Coding Sheet", "Soil Systems, Inc. Ornament Decoration (other than shell umbo) Coding Sheet",
                "Soil Systems, Inc. Shell Ornament Drilling Method Coding Sheet", "Soil Systems, Inc. Faunal Species Coding Sheet",
                "Soil Systems, Inc. Faunal Elements Coding Sheet", "Soil Systems, Inc. Faunal Bone Portion Coding Sheet",
                "Soil Systems, Inc. Faunal Front/Hind Coding Sheet", "Soil Systems, Inc. Faunal Proximal/Distal Coding Sheet",
                "Soil Systems, Inc. Faunal Anterior/Posterior Coding Sheet", "Soil Systems, Inc. Faunal Medial/Lateral Coding Sheet",
                "Soil Systems, Inc. Faunal Dorsal/Ventral Coding Sheet", "Soil Systems, Inc. Faunal Superior/Inferior Coding Sheet",
                "Soil Systems, Inc. Faunal Upper/Lower Coding Sheet", "Soil Systems, Inc. Faunal Element With Teeth Coding Sheet",
                "Soil Systems, Inc. Faunal Bone Side Coding Sheet", "Soil Systems, Inc. Faunal Sex Coding Sheet",
                "Soil Systems, Inc. Faunal Element Size Coding Sheet", "Soil Systems, Inc. Fauna Age Coding Sheet",
                "Soil Systems, Inc. Faunal Remains Condition (Completeness) Coding Sheet", "Soil Systems, Inc. Faunal Remains Burning Coding Sheet",
                "Soil Systems, Inc. Faunal Remains Modification Coding Sheet", "Soil Systems, Inc. Faunal Artifact Type Coding Sheet",
                "Soil Systems, Inc. Faunal Historic Period Coding Sheet", "Soil Systems, Inc. Lithic Material Type Coding Sheet",
                "Soil Systems, Inc. Lithic Rough Sort Artifact Type Coding Sheet", "Soil Systems, Inc. Projectile Point Analysis Basal Edge Form Coding Sheet",
                "Soil Systems, Inc. Projectile Point Basal Grinding Coding Sheet", "Soil Systems, Inc. Projectile Point Analysis Basal Thinning Coding Sheet",
                "Soil Systems, Inc. Projectile Point Analysis Blade Shape Coding Sheet", "Soil Systems, Inc. Projectile Point Analysis Condition Coding Sheet",
                "Soil Systems, Inc. Projectile Point Cross-Section Coding Sheet", "Proj Point General Form Codes from SSI",
                "Proj Point Grain Size Codes from SSI", "Proj Point Notch Codes from SSI", "Proj Point Retouch Pattern Codes from SSI",
                "Proj Point Retouch Type Codes from SSI", "Proj Point Serrations Codes from SSI",
                "Soil Systems, Inc. Projectile Point Stem Shape Coding Sheet", "Soil Systems, Inc. Projectile Point Fracture Type Coding Sheet",
                "Soil Systems, Inc. Projectile Point Resharpening Coding Sheet", "Soil Systems, Inc. Artifact Type Coding Sheet",
                "Motif Classification and Attributes", "Soil Systems, Inc. Pueblo Grande Burial Time Period Assignments Coding Sheet",
                "Soil Systems, Inc. Pueblo Grande Age at death coding sheet", "Soil Systems, Inc. Pueblo Grande Sex Identification Coding Sheet",
                "Soil Systems, Inc. Pueblo Grande Burial Types Coding Sheet", "HARP Fauna Element Coding Sheet",
                "Soil Systems, Inc. Lithic Condition Coding Sheet", "Soil Systems, Inc. Flotation/Botanical Taxon Coding Sheet",
                "Soil Systems, Inc. Flotation/Botanical Part Coding Sheet", "Soil Systems, Inc. Flotation/Botanical Condition Coding Sheet",
                "Soil Systems, Inc. Flotation/Botanical Specimen Completeness Coding Sheet",
                "Soil Systems, Inc. Flotation/Botanical Analysis Type Coding Sheet", "Raw Material Guide", "Soil Systems, Inc. Presence/Absence Coding Sheet",
                "Soil Systems, Inc. True/False Coding Sheet", "Soil Systems, Inc. Cremation Grave Shape Coding Sheet",
                "Soil Systems, Inc. Inhumation Skeletal Completeness Codes", "EMAP - Ceramics Data Sheet", "EMAP - Analytic Unit Coding Sheet",
                "EMAP - Projectile Points - Material Coding Sheet", "EMAP - Projectile Points - Form Coding Sheet", "Tosawihi Bifaces Material Color Codes",
                "Taxonomic Level 1" };
        Integer[] cats = new Integer[] { 83, 67, 79, 81, 72, 75, 70, 63, 76, 79, 73, 70, 85, 73, 67, 78, 73, 85, 83, 6, 6, 85, 6, 85, 85, 83, 83, 6, 64, 73,
                null, null, 70, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 198, null,
                null, 75, 75, 85, 70, 70, 70, 75, 70, 85, 85, 85, 73, 76, 79, null, 67, null, 191, 70, 72, 73, 75, 64, 82, 78, 81, 83, 192, 85, 67, 78, null,
                85, 192, 85, null, null, null, 73, 81, null, 85, 192, 192, 191, 191, 192, null, null, null, null, 61, 67, 78, 39, 192, 83, 75, 64, 78, 77,
                null, 196, 196, 191, 191, 192, 192, 192, 192, 192, 192, 192, 192, 196, 77, 78, 83, 196, 191, null, null, 64, 191, 192, 192, 191, 75, 73, 81,
                85, 49, 192, null, 63, 73, null, null, null, null, 191, 191, null, null, 192, 42, null, 238, 238, 42, 238, 39, 11, 238, 49, 11, 39, 39, 39, 39,
                238, 198, 196, 198, 198, 198, 214, 191, 78, null, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
                11, 11, 11, 11, 11, 11, 11, 11, null, null, null, null, null, null, 223, null, 223, 85, 73, 81, 198, 81, 62, 198, 72, 214, 214, 73, 83, 82, 73,
                61, 67, 70, 78, 63, 6, 53, 56, 52, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 166, null, 11, 11, 11, 11, 73, 52, 170, 250, 250, 250, 250, null,
                null, null, 11, 11, 36, null, 53, 52, 56, 85, 85 };

        List<CodingSheet> sheets = new ArrayList<CodingSheet>();

        List<CodingSheet> allSheets = new ArrayList<CodingSheet>();
        for (int i = 0; i < titles.length; i++) {
            String title = titles[i];
            Integer cat = cats[i];
            CodingSheet cs = createAndSaveNewInformationResource(CodingSheet.class, getUser(), title);
            allSheets.add(cs);
            if (cat != null) {
                cs.setCategoryVariable(genericService.find(CategoryVariable.class, (long) cat));
                genericService.saveOrUpdate(cs);
            }
            if (title.contains("Taxonomic Level")) {
                logger.info("{} {}", cs, cs.getCategoryVariable().getId());
                sheets.add(cs);
            }
            cs = null;
            genericService.synchronize();

        }
        List<Long> sheetIds = PersistableUtils.extractIds(sheets);
        sheets = null;
        genericService.synchronize();
        genericService.findAll(CodingSheet.class);
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        ReservedSearchParameters params = new ReservedSearchParameters();
        params.setResourceTypes(Arrays.asList(ResourceType.CODING_SHEET));
        SearchResult result = performSearch("Taxonomic Level", null, null, null, null, null, params, 10);
        logger.info("{}", result.getResults());
        logger.info("{}", sheetIds);
        assertTrue(PersistableUtils.extractIds(result.getResults()).containsAll(sheetIds));

        result = performSearch("Taxonomic Level", null, null, null, 85l, getBasicUser(), params, 10);
        logger.info("{}", result.getResults());
        assertTrue(PersistableUtils.extractIds(result.getResults()).containsAll(sheetIds));
        Resource col = ((Resource) result.getResults().get(0));
        assertEquals("Taxonomic Level 1", col.getName());

        result = performSearch(null, null, null, null, 85l, getBasicUser(), params, 1000);
        logger.info("{}", result.getResults());
        assertTrue(PersistableUtils.extractIds(result.getResults()).containsAll(sheetIds));
        genericService.synchronize();

    }

    @Test
    public void testResourceLookupByType() throws SolrServerException, IOException, ParseException {
        searchIndexService.indexAll(getAdminUser(), Resource.class);
        // get back all documents
        ReservedSearchParameters params = new ReservedSearchParameters();
        params.setResourceTypes(Arrays.asList(ResourceType.DOCUMENT));
        SearchResult result = performSearch("", null, null, null, null, getEditorUser(), params, 1000);

        List<Indexable> resources = result.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testResourceLookupByTdarId() throws SolrServerException, IOException, ParseException {
        // get back all documents
        SearchResult result = performSearch(TestConstants.TEST_DOCUMENT_ID, null, null, null, null, null, null, 1000);

        List<Indexable> resources = result.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testResourceLookupByProjectId() throws SolrServerException, IOException, ParseException {
        SearchResult result = performSearch("", 3073L, null, null, null, null, null, 1000);

        List<Indexable> resources = result.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    @Rollback(value = true)
    public void testDeletedResourceFilteredForNonAdmins() throws Exception {
        Project proj = createProject("project to be deleted");

        searchIndexService.index(proj);
        SearchResult result = performSearch("", null, null, true, null, null, null, 1000);

        List<Indexable> results = result.getResults();

        List<Long> ids = PersistableUtils.extractIds(results);

        logger.debug("list type:{}  contents:{}", results.getClass(), results);
        Long projectId = proj.getId();
        assertTrue(ids.contains(projectId));

        // now delete the resource and makes sure it doesn't show up for the common rabble
        logger.debug("result contents before delete: {}", result.getResults());
        proj.setStatus(Status.DELETED);
        genericService.saveOrUpdate(proj);
        searchIndexService.index(proj);

        result = performSearch("", null, null, true, null, null, null, 1000);

        ids = PersistableUtils.extractIds(result.getResults());

        logger.debug("result contents after delete: {}", result.getResults());
        assertFalse(ids.contains(projectId));

        // now pretend that it's an admin visiting the dashboard. Even though they can view/edit everything, deleted items
        // won't show up in their dashboard unless they are the submitter or have explicitly been given access rights, so we update the project's submitter
        proj.setSubmitter(getAdminUser());
        genericService.saveOrUpdate(proj);
        searchIndexService.index(proj);

        ReservedSearchParameters params = new ReservedSearchParameters();
        params.setStatuses(new ArrayList<Status>(Arrays.asList(Status.values())));
        result = performSearch("", null, null, null, null, getAdminUser(), params, 1000);
        ids = PersistableUtils.extractIds(result.getResults());
        assertTrue(ids.contains(projectId));
    }

    // more accurately model how struts will create a project by having the controller do it
    private Project createProject(String title) {
        Project project = new Project();
        project.setTitle(title);
        project.setDescription(title);
        project.markUpdated(getAdminUser());
        genericService.save(project);
        Assert.assertNotNull(project.getId());
        assertTrue(project.getId() != -1L);
        return project;
    }

    // TODO: need filtered test (e.g. only ontologies in a certain project)

    public ReservedSearchParameters initControllerFields() {
        searchIndexService.indexAll(getAdminUser());
        List<String> types = new ArrayList<String>();
        types.add("DOCUMENT");
        types.add("ONTOLOGY");
        types.add("CODING_SHEET");
        types.add("IMAGE");
        types.add("DATASET");
        ReservedSearchParameters params = new ReservedSearchParameters();
        params.setResourceTypes(Arrays.asList(ResourceType.DOCUMENT,
                ResourceType.ONTOLOGY, ResourceType.IMAGE, ResourceType.DATASET,
                ResourceType.CODING_SHEET));
        return params;
    }

    @Test
    public void testResourceLookup() throws IOException, SolrServerException, ParseException {
        ReservedSearchParameters params = initControllerFields();
        SearchResult result = performSearch("HARP", null, null, null, null, null, params, 1000);

        boolean seen = true;
        for (Indexable idx : result.getResults()) {
            if (!StringUtils.containsIgnoreCase( ((Resource) idx).getTitle(),"HARP")) {
                seen = false;
            }
        }
        assertFalse(result.getResults().size() == 0);
        assertTrue(seen);
    }

    @Test
    @Rollback(value = true)
    public void testAdminDashboardAnyStatus() throws Exception {
        // have a regular user create a document in each status (except deleted)that should be visible when an admin looks for document with "any" status
        Document activeDoc = createAndSaveNewInformationResource(Document.class, getUser());
        activeDoc.setTitle("testActiveDoc");
        activeDoc.setStatus(Status.ACTIVE); // probably unnecessary
        Document draftDoc = createAndSaveNewInformationResource(Document.class, getUser());
        draftDoc.setTitle("testDraftDoc");
        draftDoc.setStatus(Status.DRAFT);
        Document flaggedDoc = createAndSaveNewInformationResource(Document.class, getUser());
        flaggedDoc.setTitle("testFlaggedaDoc");
        flaggedDoc.setStatus(Status.FLAGGED);
        List<Document> docs = Arrays.asList(activeDoc, draftDoc, flaggedDoc);
        genericService.saveOrUpdate(docs);
        searchIndexService.indexAll(getAdminUser(), Resource.class);

        // login as an admin
        for (Document doc : docs) {
            SearchResult result = performSearch(doc.getTitle(), getAdminUser(), Integer.MAX_VALUE);
            if (doc.isActive() || doc.isDraft()) {
                assertTrue(String.format("looking for '%s' when filtering ", doc),
                        result.getResults().contains(doc));
            } else {
                assertFalse(String.format("looking for '%s' when filtering ", doc), result.getResults().contains(doc));

            }
        }

        for (Document doc : docs) {
            ReservedSearchParameters params = new ReservedSearchParameters();
            params.setStatuses(Arrays.asList(Status.values()));
            SearchResult result = performSearch(doc.getTitle(), null, null, null, null, getAdminUser(), params, Integer.MAX_VALUE);
            assertTrue(String.format("looking for '%s' when filtering", doc), result.getResults().contains(doc));
        }

    }

    @Autowired
    private ResourceSearchService resourceSearchService;

    public SearchResult performSearch(String term, TdarUser user, int max) throws ParseException, SolrServerException, IOException {
        return performSearch(term, null, null, null, null, user, null, null, max);
    }

    public SearchResult performSearch(String term, Long projectId, Long collectionId, Boolean includeParent, Long categoryId, TdarUser user,
            ReservedSearchParameters reservedSearchParameters, int max) throws ParseException, SolrServerException, IOException {
        return performSearch(term, projectId, collectionId, includeParent, categoryId, user, reservedSearchParameters, null, max);
    }

    public SearchResult performSearch(String term, Long projectId, Long collectionId, Boolean includeParent, Long categoryId, TdarUser user,
            ReservedSearchParameters reservedSearchParameters, GeneralPermissions permission, int max) throws ParseException, SolrServerException, IOException {
        SearchResult result = new SearchResult();
        logger.debug("{}, {}", resourceSearchService, MessageHelper.getInstance());
        result.setRecordsPerPage(max);
        ResourceQueryBuilder q = resourceSearchService.lookupResource(term, projectId, includeParent, collectionId, categoryId, user,
                reservedSearchParameters, permission, MessageHelper.getInstance());
        searchService.handleSearch(q, result, MessageHelper.getInstance());
        return result;
    }
}
