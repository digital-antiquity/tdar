package org.tdar.struts.action.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONArray;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.Indexable;
import org.tdar.core.bean.JsonModel;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.collection.ResourceCollection.CollectionType;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAnnotationKey;
import org.tdar.core.bean.resource.ResourceAnnotationType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.Status;
import org.tdar.search.query.SortOption;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.resource.ProjectController;

public class LookupControllerITCase extends AbstractIntegrationTestCase {

    @Autowired
    private LookupController controller;

    @Before
    public void initController() {
        controller = generateNewInitializedController(LookupController.class);
        controller.setRecordsPerPage(99);
    }

    String[] collectionNames = new String[] { "Kalaupapa National Historical Park, Hawaii", "Kaloko-Honokohau National Historical Park, Hawaii", "Kapsul",
            "KBP Artifact Photographs", "KBP Field Notes", "KBP Level Maps", "KBP Maps", "KBP Profiles", "KBP Reports", "KBP Site Photographs",
            "Kharimkotan 1", "Kienuka", "Kintigh - Carp Fauna Coding Sheets", "Kintigh - Cibola Excavation", "Kintigh - Cibola Research",
            "Kintigh - Cibola Survey Projects", "Kintigh - Context Ontologies", "Kintigh - Fauna Ontologies", "Kintigh - HARP Coding Sheets",
            "Kintigh - Quantitative and Formal Methods Class - Assignments & Data", "Kleis", "Klinko", "Kokina 1", "Kompaneyskyy 1",
            "Kuril Biocomplexity Research", "Kuybyshevskaya 1",
            "Spielmann/Kintigh - Fauna Ontologies - Current" };

    @Test
    @Rollback(true)
    public void testCollectionLookup() {
        List<ResourceCollection> collections = new ArrayList<ResourceCollection>();
        for (String collectionName : collectionNames) {
            ResourceCollection e = new ResourceCollection(collectionName, collectionName, SortOption.TITLE, CollectionType.SHARED, true, getBasicUser());
            collections.add(e);
            e.markUpdated(getBasicUser());

        }
        genericService.save(collections);
        searchIndexService.index(collections.toArray(new ResourceCollection[0]));
        controller.setTerm("Kin");
        controller.lookupResourceCollection();
        for (Indexable collection_ : controller.getResults()) {
            ResourceCollection collection = (ResourceCollection) collection_;
            logger.info("{}", collection);
            if (collection != null) {
                assertFalse(collection.getTitle().equals("Kleis"));
            }
        }
        initController();
        controller.setTerm("Kintigh - C");
        controller.lookupResourceCollection();
        for (Indexable collection_ : controller.getResults()) {
            ResourceCollection collection = (ResourceCollection) collection_;
            logger.info("{}", collection);
            if (collection != null) {
                assertTrue(collection.getTitle().contains("Kintigh - C"));
            }
        }

    }

    @Test
    @Rollback(true)
    public void testInvisibleCollectionLookupFoundByBasicOwner() {
        ResourceCollection e = setupResourceCollectionForPermissionsTests(getAdminUser(), false, getBasicUser(), GeneralPermissions.VIEW_ALL);
        controller.setTerm("test");
        controller.lookupResourceCollection();
        assertTrue(controller.getResults().contains(e));
    }


    @Test
    @Rollback(true)
    public void testInvisibleCollectionLookupFoundByBasicUser() {
        ResourceCollection e = setupResourceCollectionForPermissionsTests(getAdminUser(), false, getBasicUser(), GeneralPermissions.VIEW_ALL);
        init(controller, getBasicUser());
        controller.setTerm("test");
        controller.lookupResourceCollection();
        assertTrue(controller.getResults().contains(e));
    }

    @Test
    @Rollback(true)
    public void testInvisibleCollectionLookupFoundByBasicUserForModification() {
        ResourceCollection e = setupResourceCollectionForPermissionsTests(getAdminUser(), false, getBasicUser(), GeneralPermissions.VIEW_ALL);
        init(controller, getBasicUser());
        controller.setTerm("test");
        controller.setPermission(GeneralPermissions.ADMINISTER_GROUP);
        controller.lookupResourceCollection();
        assertFalse(controller.getResults().contains(e));
    }

    private ResourceCollection setupResourceCollectionForPermissionsTests(Person owner, boolean visible, Person user, GeneralPermissions permission) {
        assertFalse(getSessionUser().equals(getAdminUser()));
        ResourceCollection e = new ResourceCollection("a test", "a Name", SortOption.TITLE, CollectionType.SHARED, visible, owner);
        e.markUpdated(owner);
        genericService.save(e);
        if (user != null) {
            AuthorizedUser au = new AuthorizedUser(user, permission);
            au.setResourceCollection(e);
            genericService.save(au);
            e.getAuthorizedUsers().add(au);
        }
        searchIndexService.index(e);
        return e;
    }

    @Test
    @Rollback(true)
    public void testLookupByTitle() throws InstantiationException, IllegalAccessException {
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
        }
        searchIndexService.indexAll(Resource.class);
        controller.setResourceTypes(Arrays.asList(ResourceType.CODING_SHEET));
        controller.setTerm("Taxonomic Level");
        controller.setRecordsPerPage(10);
        controller.lookupResource();
        logger.info("{}", controller.getResults());
        logger.info("{}", sheets);
        assertTrue(controller.getResults().containsAll(sheets));

        controller = generateNewInitializedController(LookupController.class, getBasicUser());
        controller.setRecordsPerPage(10);
        controller.setResourceTypes(Arrays.asList(ResourceType.CODING_SHEET));
        controller.setTerm("Taxonomic Level");
        controller.setSortCategoryId(85l);
        controller.lookupResource();
        logger.info("{}", controller.getResults());
        assertTrue(controller.getResults().containsAll(sheets));
        Resource col = ((Resource) controller.getResults().get(0));
        assertEquals("Taxonomic Level 1", col.getName());

        controller = generateNewInitializedController(LookupController.class, getBasicUser());
        controller.setRecordsPerPage(1000);
        controller.setResourceTypes(Arrays.asList(ResourceType.CODING_SHEET));
        controller.setSortCategoryId(85l);
        controller.lookupResource();
        logger.info("{}", controller.getResults());
        assertTrue(controller.getResults().containsAll(sheets));
    }

    @Test
    public void testResourceLookupByType() {
        searchIndexService.indexAll(Resource.class);
        // get back all documents
        controller.setResourceTypes(Arrays.asList(ResourceType.DOCUMENT));
        controller.lookupResource();
        List<Indexable> resources = controller.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testResourceLookupByProjectId() {
        searchIndexService.indexAll(Resource.class);
        controller.setProjectId(3073L);
        controller.lookupResource();
        List<Indexable> resources = controller.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testKeywordLookup() {
        searchIndexService.indexAll(GeographicKeyword.class, CultureKeyword.class);
        controller.setKeywordType("culturekeyword");
        controller.setTerm("Folsom");
        controller.lookupKeyword();
        List<Indexable> resources = controller.getResults();
        assertTrue("at least one document", resources.size() >= 1);
    }

    @Test
    public void testAnnotationLookup() {
        ResourceAnnotationKey key = new ResourceAnnotationKey();
        key.setKey("ISSN");
        key.setResourceAnnotationType(ResourceAnnotationType.IDENTIFIER);
        genericService.save(key);
        ResourceAnnotationKey key2 = new ResourceAnnotationKey();
        key2.setKey("ISBN");
        key2.setResourceAnnotationType(ResourceAnnotationType.IDENTIFIER);
        genericService.save(key2);

        searchIndexService.indexAll(ResourceAnnotationKey.class);
        controller.setTerm("IS");
        controller.lookupAnnotationKey();
        List<Indexable> resources = controller.getResults();
        assertTrue("at least one document", resources.size() == 2);

        // FIXME: not properly simulating new page request
        controller.setTerm("ZZ");
        controller.lookupAnnotationKey();
        resources = controller.getResults();
        assertEquals("ZZ should return no results", 0, resources.size());
    }

    @Test
    @Rollback(value = true)
    public void testDeletedResourceFilteredForNonAdmins() throws Exception {
        initControllerForDashboard();

        searchIndexService.indexAll(Resource.class);
        controller.setUseSubmitterContext(true);
        Project proj = createProjectViaProjectController("project to be deleted");

        searchIndexService.index(proj);

        controller.lookupResource();
        assertTrue(controller.getResults().contains(proj));

        // now delete the resource and makes sure it doesn't show up for the common rabble
        logger.debug("result contents before delete: {}", controller.getResults());
        proj.setStatus(Status.DELETED);
        genericService.saveOrUpdate(proj);
        initController();
        controller.setUseSubmitterContext(true);

        searchIndexService.index(proj);
        controller.lookupResource();
        logger.debug("result contents after delete: {}", controller.getResults());
        assertFalse(controller.getResults().contains(proj));

        // now pretend that it's an admin visiting the dashboard. Even though they can view/edit everything, deleted items
        // won't show up in their dashboard unless they are the submitter or have explicitly been given access rights, so we update the project's submitter
        proj.setSubmitter(getAdminUser());
        genericService.saveOrUpdate(proj);
        searchIndexService.index(proj);
        controller = generateNewController(LookupController.class);
        init(controller, getAdminUser());
        controller.setUseSubmitterContext(false);
        controller.setIncludedStatuses(new ArrayList<Status>(Arrays.asList(Status.values())));
        controller.setRecordsPerPage(10000000);
        controller.lookupResource();
        assertTrue(controller.getResults().contains(proj));
    }

    // setup the controller similar to how struts will do it when receiving an ajax call from the dashboard
    private void initControllerForDashboard() {
        controller.getResourceTypes().add(null);
        controller.getIncludedStatuses().add(null);
        controller.setSortField(SortOption.RELEVANCE);
        controller.setTerm(null);
        controller.setProjectId((String) null);
        controller.setCollectionId(null);
    }

    // more accurately model how struts will create a project by having the controller do it
    private Project createProjectViaProjectController(String title) throws TdarActionException {
        ProjectController projectController = generateNewInitializedController(ProjectController.class);
        projectController.prepare();
        Project project = projectController.getProject();
        project.setTitle(title);
        project.setDescription(title);
        projectController.setServletRequest(getServletPostRequest());
        projectController.save();
        Assert.assertNotNull(project.getId());
        assertTrue(project.getId() != -1L);
        return project;
    }

    // TODO: need filtered test (e.g. only ontologies in a certain project)

    public void initControllerFields() {
        searchIndexService.indexAll();
        List<String> types = new ArrayList<String>();
        types.add("DOCUMENT");
        types.add("ONTOLOGY");
        types.add("CODING_SHEET");
        types.add("IMAGE");
        types.add("DATASET");
        controller.setResourceTypes(Arrays.asList(ResourceType.DOCUMENT,
                ResourceType.ONTOLOGY, ResourceType.IMAGE, ResourceType.DATASET,
                ResourceType.CODING_SHEET));
    }

    @Test
    public void testResourceLookup() {
        initControllerFields();
        controller.setTitle("HARP");
        controller.lookupResource();

        JSONArray jsonArray = new JSONArray();
        for (Indexable persistable : controller.getResults()) {
            if (persistable instanceof JsonModel) {
                jsonArray.add(((JsonModel) persistable).toJSON());
            }
        }
        String json = jsonArray.toString();
        logger.debug("resourceLookup results:{}", json);
        // assertTrue(json.contains("iTotalRecords"));
        assertTrue(json.contains("HARP"));
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
        entityService.saveOrUpdateAll(docs);
        searchIndexService.indexAll(Resource.class);

        // login as an admin
        controller = generateNewController(LookupController.class);
        init(controller, getAdminUser());
        controller.setRecordsPerPage(Integer.MAX_VALUE);
        for (Document doc : docs) {
            controller.setTitle(doc.getTitle());
            controller.lookupResource();
            if (doc.isActive() || doc.isDraft()) {
                assertTrue(String.format("looking for '%s' when filtering by %s", doc, controller.getIncludedStatuses()), controller.getResults().contains(doc));
            } else {
                assertFalse(String.format("looking for '%s' when filtering by %s", doc, controller.getIncludedStatuses()), controller.getResults()
                        .contains(doc));

            }
        }

        for (Document doc : docs) {
            controller.setTitle(doc.getTitle());
            controller.setIncludedStatuses(controller.getAllStatuses());
            controller.lookupResource();
            assertTrue(String.format("looking for '%s' when filtering by %s", doc, controller.getIncludedStatuses()), controller.getResults().contains(doc));
        }

    }

    @Test
    @Rollback
    public void testSanitizedPersonRecords() {

        // important! normally the SessionSecurityInterceptor would mark the session as readonly, but we need to do it manually in a test
        genericService.markReadOnly();

        searchIndexService.indexAll(Person.class);
        // "log out"
        controller = generateNewController(LookupController.class);
        initAnonymousUserinit(controller);
        controller.setRecordsPerPage(Integer.MAX_VALUE);
        controller.setMinLookupLength(0);
        controller.lookupPerson();
        assertTrue(controller.getResults().size() > 0);
        for (Indexable result : controller.getResults()) {
            assertNull(((Person) result).getEmail());
        }

        // normally these two requests would belong to separate hibernate sessions. We flush the session here so that the we don't get back the
        // same cached objects that the controller sanitized in the previous lookup.
        genericService.clearCurrentSession();
        genericService.markReadOnly();

        // okay now "log in" and make sure that email lookup is still working
        controller = generateNewInitializedController(LookupController.class);
        controller.setRecordsPerPage(Integer.MAX_VALUE);
        controller.setMinLookupLength(0);
        String email = "james.t.devos@asu.edu";
        controller.setEmail(email);
        controller.lookupPerson();
        assertEquals(1, controller.getResults().size());
        Person jim = (Person) controller.getResults().get(0);
        assertEquals(email, jim.getEmail());

    }
    
    @Test
    @Rollback
    //special characters need to be escaped or stripped prior to search
    public void testLookupWithSpecialCharactors() {
        setTextFields("l[]bl aw\\");
        controller.setMinLookupLength(0);
        controller.lookupPerson();
        controller.lookupInstitution();
        controller.lookupKeyword();
        controller.lookupResource();
        controller.lookupResourceCollection();
    }
    
    
    //we don't care about making sense,  we just want to catch parsing errors.
    private void setTextFields(String str) {
        controller.setFirstName(str);
        controller.setLastName(str);
        controller.setInstitution(str);
        controller.setEmail(str);
        controller.setTerm(str);
        controller.setTitle(str);
    }
    

}
