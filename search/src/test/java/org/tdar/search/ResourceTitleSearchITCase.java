package org.tdar.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.search.bean.ObjectType;
import org.tdar.search.bean.ReservedSearchParameters;
import org.tdar.search.bean.SearchParameters;
import org.tdar.search.index.LookupSource;
import org.tdar.search.query.SearchResult;
import org.tdar.utils.PersistableUtils;

public class ResourceTitleSearchITCase extends AbstractResourceSearchITCase {

    @Test
    public void testTitleCaseSensitivity() throws SolrServerException, IOException, ParseException {
        Document doc = createAndSaveNewResource(Document.class);
        doc.setTitle("usaf");
        updateAndIndex(doc);
        SearchParameters sp = new SearchParameters();
        sp.setTitles(Arrays.asList("USAF"));
        SearchResult<Resource> result = doSearch(null,null,sp,null);
        
        assertTrue(result.getResults().contains(doc));
        doc.setTitle("USAF");
        updateAndIndex(doc);
        sp = new SearchParameters();
        sp.setTitles(Arrays.asList("usaf"));
        result = doSearch(null,null,sp,null);
        assertTrue(result.getResults().contains(doc));
    }


    @SuppressWarnings("deprecation")
    @Test
    @Rollback(true)
    public void testTitleSiteCodeMatching() throws SolrServerException, IOException, ParseException {
        List<String> titles = Arrays
                .asList("1. Pueblo Grande (AZ U:9:1(ASM)): Unit 12, Gateway and 44th Streets: SSI Kitchell Testing, Photography Log (PHOTO) Data (1997)",
                        "2. Archaeological Testing at Pueblo Grande (AZ U:9:1(ASM)): Unit 15, The Former Maricopa County Sheriff's Substation, Washington and 48th Streets, Phoenix, Arizona -- DRAFT REPORT (1999)",
                        "3. Phase 2 Archaeological Testing at Pueblo Grande (AZ U:9:1(ASM)): Unit 15, the Former Maricopa County Sheriff’s Substation, Washington and 48th Streets, Phoenix, Arizona -- DRAFT REPORT (1999)",
                        "4. Final Data Recovery And Burial Removal At Pueblo Grande (AZ U:9:1(ASM)): Unit 15, The Former Maricopa Counry Sheriff's Substation, Washington And 48th Streets, Phoenix, Arizona (2008)",
                        "5. Pueblo Grande (AZ U:9:1(ASM)): Unit 15, Washington and 48th Streets: Soil Systems, Inc. Kitchell Development Testing and Data Recovery (The Former Maricopa County Sheriff's Substation) ",
                        "6. Archaeological Testing of Unit 13 at Pueblo Grande, AZ U:9:1(ASM), Arizona Federal Credit Union Property, 44th and Van Buren Streets, Phoenix, Maricopa County, Arizona (1998)",
                        "7. Archaeological Testing And Burial Removal Of Unit 11 At Pueblo Grande, AZ U:9:1(ASM), DMB Property, 44th And Van Buren Streets, Phoenix, Maricopa County, Arizona -- DRAFT REPORT (1998)",
                        "8. Pueblo Grande (AZ U:9:1(ASM)): Unit 13, Northeast Corner of Van Buren and 44th Streets: Soil Systems, Inc. AZ Federal Credit Union Testing and Data Recovery Project ",
                        "9. POLLEN AND MACROFLORAL ANAYSIS AT THE WATER USERS SITE, AZ U:6:23(ASM), ARIZONA (1990)",
                        "10. Partial Data Recovery and Burial Removal at Pueblo Grande (AZ U:9:1(ASM)): Unit 15, The Former Maricopa County Sheriff's Substation, Washington and 48th Streets, Phoenix, Arizona -- DRAFT REPORT (2002)",
                        "11. MACROFLORAL AND PROTEIN RESIDUE ANALYSIS AT SITE AZ U:15:18(ASM), CENTRAL ARIZONA (1996)",
                        "12. Pueblo Grande (AZ U:9:1(ASM)) Soil Systems, Inc. Master Provenience Table: Projects, Unit Numbers, and Feature Numbers (2008)");

        List<Document> docs = new ArrayList<>();
        List<Document> badMatches = new ArrayList<>();
        for (String title : titles) {
            Document doc = new Document();
            doc.setTitle(title);
            doc.setDescription(title);
            doc.markUpdated(getBasicUser());
            genericService.save(doc);
            if (title.contains("MACROFLORAL")) {
                badMatches.add(doc);
            }
            docs.add(doc);
        }
        genericService.synchronize();
//        searchIndexService.indexCollection(docs);
        SearchResult<Resource> result = doSearch("AZ U:9:1(ASM)");
        List<Resource> results = result.getResults();
        for (Resource r : result.getResults()) {
            logger.debug("results: {}", r);
        }
        
        assertTrue("controller should not contain titles with MACROFLORAL", CollectionUtils.containsAny(results, badMatches));
        assertTrue("controller should not contain titles with MACROFLORAL",
                CollectionUtils.containsAll(results.subList(results.size() - 3, results.size()), badMatches));

    }


    @Test
    @Rollback
    public void testTitleSearch() throws InstantiationException, IllegalAccessException, ParseException, SolrServerException, IOException {
        Document doc = createDocumentWithContributorAndSubmitter();
        String title = "the archaeology of class and war";
        doc.setTitle(title);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        SearchParameters sp = new SearchParameters();
        sp.getTitles().add(title);
        SearchResult<Resource> result = doSearch(null,null,sp, null);

        logger.info("{}", result.getResults());
        assertEquals("only one result expected", 1L, result.getResults().size());
        assertEquals(doc, result.getResults().iterator().next());
    }

    @Test
    @Rollback
    public void testLuceneOperatorInSearch() throws InstantiationException, IllegalAccessException, ParseException, SolrServerException, IOException {
        Document doc = createDocumentWithContributorAndSubmitter();
        String title = "the archaeology of class ( AND ) war";
        doc.setTitle(title);
        genericService.saveOrUpdate(doc);
        searchIndexService.index(doc);
        SearchParameters sp = new SearchParameters();
        sp.getAllFields().add(title);
        SearchResult<Resource> result = doSearch(null,null,sp, null);
        logger.info("{}", result.getResults());
        assertEquals("only one result expected", 1L, result.getResults().size());
        assertEquals(doc, result.getResults().iterator().next());
    }


    @SuppressWarnings("deprecation")
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
            }
            if (title.contains("Taxonomic Level")) {
                logger.info("{} {}", cs, cs.getCategoryVariable().getId());
                sheets.add(cs);
            }
            cs = null;

        }
        genericService.saveOrUpdate(allSheets);
        genericService.synchronize();
        List<Long> sheetIds = PersistableUtils.extractIds(sheets);
        sheets = null;
        genericService.synchronize();
        searchIndexService.indexAll(getAdminUser(), LookupSource.RESOURCE);
        ReservedSearchParameters params = new ReservedSearchParameters();
        params.setObjectTypes(Arrays.asList(ObjectType.CODING_SHEET));
        SearchResult<Resource> result = performSearch("Taxonomic Level", null, null, null, null, null, params, 10);
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
    @Rollback
    public void testTitle() throws ParseException, SolrServerException, IOException {
        // FIXME: magic numbers
        Long projectId = 139L;
        Project project = genericService.find(Project.class, projectId);
        String projectTitle = project.getTitle();
        SearchParameters sp = new SearchParameters();
        sp.getTitles().add(projectTitle);
        SearchResult<Resource> result = doSearch(null,null,sp, null);
        result.getResults().contains(project);
    }

    @SuppressWarnings("deprecation")
    @Test
    @Rollback
    public void testTitleRelevancy() throws SolrServerException, IOException, ParseException {
        //(11R5)-1
        //1/4
        //4\"
        String exact = "Modoc Rock Shelter, IL (11R5)-1984 Fauna dataset Main Trench 1/4\" Screen";
        //Modoc Rock Shelter, IL (11R5)-1984 Fauna dataset Main Trench 1/4\" Screen
        List<String> titles = Arrays.asList(
                "Coding sheet for Element ("+exact+")",
                "Coding sheet for Recovery ("+exact+")",
                "Coding sheet for CulturalAffiliation ("+exact+")",
                "Coding sheet for Resource Type ("+exact+")",
                "Coding sheet for Taxon ("+exact+")",
                "Coding sheet for Portion3 ("+exact+")",
                "Coding sheet for Resource Type ("+exact+")",
                "Coding sheet for ContextType ("+exact+")",
                "Coding sheet for LevelType ("+exact+")",
                "Coding sheet for Site ("+exact+")",
                "Coding sheet for LevelType ("+exact+")",
                exact,
                "Coding sheet for Taxon (Modoc Rock Shelter (11R5), Randolph County, IL-1984, Main Trench 1/4\" screen fauna)");


        List<Resource> docs = new ArrayList<>();
        for (String title : titles) {
            Resource doc = new CodingSheet();
            if (title.equals(exact)) {
                doc = new Dataset();
                doc.setDescription("a");
            } else {
                doc.setDescription(exact);
            }
            doc.setTitle(title);
            doc.markUpdated(getBasicUser());
            genericService.saveOrUpdate(doc);
            docs.add(doc);
        }
        genericService.synchronize();
        searchIndexService.indexCollection(docs);
        SearchResult<Resource> result = doSearch(exact);
        @SuppressWarnings("unused")
        List<Resource> results = result.getResults();
        for (Resource r : result.getResults()) {
            logger.debug("results: {}", r);
        }
        assertEquals(exact,result.getResults().get(0).getTitle());
    }

}
