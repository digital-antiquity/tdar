package org.tdar.struts.action.resource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.datatable.DataTableColumnType;
import org.tdar.core.service.resource.OntologyService;
import org.tdar.core.service.resource.ontology.OwlOntologyConverter;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.action.TdarActionSupport;
import org.tdar.utils.Pair;

/**
 * $Id$
 * 
 * @author Adam Brin
 * @version $Revision$
 */
public class OntologyControllerITCase extends AbstractResourceControllerITCase {

    @Autowired
    private OntologyController controller;
    @Autowired
    private OntologyService ontologyService;

    public final static String TAB_ONTOLOGY_FILE = "/ontology/tabOntologyFile.txt";
    public final static String UPDATED_TAB_ONTOLOGY_FILE = "/ontology/updatedTabOntologyFile.txt";


    @Test
    @Rollback
    public void testControllerLoadsTabText() throws Exception {
        controller = generateNewInitializedController(OntologyController.class);
        controller.prepare();
        Ontology ont = controller.getOntology();
        ont.setTitle("test ontology for ordering");
        ont.setDescription("test");
        ont.markUpdated(getUser());
        String ontText = IOUtils.toString(getClass().getResourceAsStream(TAB_ONTOLOGY_FILE));
        assertNotNull("input should be the same", ontText);
        controller.setFileInputMethod("text");
        controller.setFileTextInput(ontText);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Long id = ont.getId();
        controller = generateNewInitializedController(OntologyController.class);
        Collection<InformationResourceFileVersion> currentVersions = ont.getLatestVersions();
        controller.setId(id);
        controller.prepare();
        controller.edit();
        controller.setFileInputMethod("text");
        assertEquals(ontText, controller.getFileTextInput());
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        assertEquals("versions should be the same", currentVersions, controller.getResource().getLatestVersions());
        controller = generateNewInitializedController(OntologyController.class);
        controller.setId(id);
        controller.prepare();
        controller.edit();
        controller.setFileInputMethod("text");
        controller.setFileTextInput(controller.getFileTextInput() + "a");
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        assertFalse("versions should not be the same", currentVersions.equals(controller.getResource().getLatestVersions()));
    }

    @Test
    @Rollback
    public void testOntologyWithReservedChars() throws Exception {
        String ontText = IOUtils.toString(getClass().getResourceAsStream("/ontology/nodes_with_bad_chars_and_weird_percents.txt"));
        Long id = loadOntologyFromText(ontText);
        Ontology ont = ontologyService.find(id);
        Map<String, OntologyNode> map = new HashMap<String, OntologyNode>();
        for (OntologyNode node : ont.getOntologyNodes()) {
            map.put(node.getIri(), node);
        }
        logger.debug("iri -> nodes: {}", map);
        OntologyNode node = map.get("Navicular__Central____Cuboid");
        assertNotNull(node);
        assertEquals("Navicular (Central) & Cuboid", node.getDisplayName());
        assertEquals("4th Tarsal2", node.getSynonyms().iterator().next());
        assertEquals("<Fish Element Additional>", map.get("_Fish_Element_Additional_").getDisplayName());
        assertEquals("Clavicle % Clavicle.clavicle", map.get("Clavicle___Clavicle.clavicle").getDisplayName());
    }

    @Test
    @Rollback(false)
    public void testMappedOntologyUpdate() throws Exception {
        String ontText = IOUtils.toString(getClass().getResourceAsStream(TAB_ONTOLOGY_FILE));
        final int originalFileLength = ontText.split("([\r\n]+)").length;
        final Long id = loadOntologyFromText(ontText);
        Ontology ont = ontologyService.find(id);
        logger.debug(ont.getTitle());
        final List<OntologyNode> ontologyNodes = new ArrayList<OntologyNode>(ont.getOntologyNodes());
        assertNotNull(ontologyNodes);
        logger.info("ontology nodes: {}", ontologyNodes);
        final Pair<Dataset, CodingSheet> returned = setupMappingsForTest(ont);
        final CodingSheet generatedSheet = returned.getSecond();
        final Collection<InformationResourceFileVersion> latestVersions = ont.getLatestVersions();
        controller = super.generateNewInitializedController(OntologyController.class);
        controller.setId(id);
        controller.prepare();
        ont = controller.getOntology();
        ontText = IOUtils.toString(getClass().getResourceAsStream(UPDATED_TAB_ONTOLOGY_FILE));
        final int updatedFileLength = ontText.split("([\r\n]+)").length;
        assertNotNull(ontText);
        controller.setFileInputMethod("text");
        controller.setFileTextInput(ontText);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        setVerifyTransactionCallback(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                Ontology ont = ontologyService.find(id);
                logger.debug(ont.getTitle());
                List<OntologyNode> updatedOntologyNodes = ont.getOntologyNodes();
                logger.debug("previous ontology nodes: {}", ontologyNodes);
                logger.debug("updated ontology nodes: {}", updatedOntologyNodes);
                assertFalse(new TreeSet<OntologyNode>(updatedOntologyNodes).equals(new TreeSet<OntologyNode>(ontologyNodes)));
                Map<String, OntologyNode> displayNameToNode = new HashMap<String, OntologyNode>();
                Map<String, OntologyNode> updatedDisplayNameToNode = new HashMap<String, OntologyNode>();
                for (OntologyNode node : ontologyNodes) {
                    displayNameToNode.put(node.getDisplayName(), node);
                }
                for (OntologyNode updatedNode : updatedOntologyNodes) {
                    updatedDisplayNameToNode.put(updatedNode.getDisplayName(), updatedNode);
                }
                getLogger().debug("displayNameToNode: {}", displayNameToNode);
                getLogger().debug("updatedDisplayNameToNode: {}", updatedDisplayNameToNode);
                logger.info(ontologyNodes.size() + " (" + originalFileLength + ")" + " - " + updatedOntologyNodes.size() + " (" + updatedFileLength + ")");
                assertEquals("original file matches same number of parsed ontologyNodes", originalFileLength, ontologyNodes.size());
                assertEquals("updated file matches same number of parsed ontologyNodes", updatedFileLength, updatedOntologyNodes.size());
                assertEquals("Original id was transferred for exact match",
                        displayNameToNode.get("Tool").getId(),
                        updatedDisplayNameToNode.get("Tool").getId());
                assertEquals("Original id was transferred for exact match",
                        displayNameToNode.get("Core").getId(), updatedDisplayNameToNode.get("Core").getId());
                assertThat(displayNameToNode.get("Other Tool").getImportOrder(), is(not(updatedDisplayNameToNode.get("Unknown Tool").getImportOrder())));
                assertFalse(latestVersions.equals(ont.getLatestUploadedVersions()));
                assertEquals("Uploading new ontology didn't preserve coding rule -> ontology node references properly", ont.getNodeByName("Tool"),
                        generatedSheet.getCodingRuleByCode("Tool").getOntologyNode());

                for (InformationResourceFile file : ont.getInformationResourceFiles()) {
                    for (InformationResourceFileVersion version : file.getInformationResourceFileVersions()) {
                        genericService.forceDelete(version);
                    }
                    genericService.delete(file);
                }
                genericService.forceDelete(returned.getFirst());
                genericService.forceDelete(generatedSheet);
                genericService.forceDelete(ont);
                return null;
            }
        });

    }

    private Long loadOntologyFromText(String ontText) throws TdarActionException {
        controller = super.generateNewInitializedController(OntologyController.class);
        controller.prepare();
        Ontology ont = controller.getOntology();
        ont.setTitle("test ontology for ordering");
        ont.setDescription("test");
        ont.markUpdated(getUser());
        assertNotNull(ontText);
        controller.setFileInputMethod("text");
        controller.setFileTextInput(ontText);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Long id = ont.getId();
        return id;
    }

    @Test
    @Rollback
    public void testUnMappedOntologyUpdate() throws Exception {
        controller = super.generateNewInitializedController(OntologyController.class);
        controller.prepare();
        Ontology ont = controller.getOntology();
        ont.setTitle("test ontology for ordering");
        ont.setDescription("test");
        ont.markUpdated(getUser());
        String ontText = IOUtils.toString(getClass().getResourceAsStream(TAB_ONTOLOGY_FILE));
        assertNotNull(ontText);
        controller.setFileInputMethod("text");
        controller.setFileTextInput(ontText);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Long id = ont.getId();
        ont = ontologyService.find(id);
        logger.debug(ont.getTitle());
        assertNotNull(ont.getOntologyNodes());
        List<OntologyNode> ontologyNodes = new ArrayList<OntologyNode>(ont.getOntologyNodes());
        assertNotNull(ontologyNodes);
        logger.info("{}", ontologyNodes);

        controller = super.generateNewInitializedController(OntologyController.class);
        controller.setId(id);
        controller.prepare();
        ont = controller.getOntology();
        ontText = IOUtils.toString(getClass().getResourceAsStream(UPDATED_TAB_ONTOLOGY_FILE));
        assertNotNull(ontText);
        controller.setFileInputMethod("text");
        controller.setFileTextInput(ontText);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        ont = ontologyService.find(id);
        logger.debug(ont.getTitle());
        List<OntologyNode> updatedOntologyNodes = ont.getOntologyNodes();
        logger.debug("previous ontology nodes: " + ontologyNodes);
        logger.debug("updated ontology nodes: " + updatedOntologyNodes);
        Map<String, OntologyNode> displayNameToNode = new HashMap<String, OntologyNode>();
        Map<String, OntologyNode> updatedDisplayNameToNode = new HashMap<String, OntologyNode>();
        for (OntologyNode node : ontologyNodes) {
            displayNameToNode.put(node.getDisplayName(), node);
        }
        for (OntologyNode updatedNode : updatedOntologyNodes) {
            updatedDisplayNameToNode.put(updatedNode.getDisplayName(), updatedNode);
        }
        getLogger().debug("displayNameToNode: " + displayNameToNode);
        getLogger().debug("updatedDisplayNameToNode: " + updatedDisplayNameToNode);

        assertTrue("Original id was transferred for exact match",
                !displayNameToNode.get("Tool").getId().equals(updatedDisplayNameToNode.get("Tool").getId()));
        assertTrue("Original id was transferred for exact match",
                !displayNameToNode.get("Core").getId().equals(updatedDisplayNameToNode.get("Core").getId()));
        assertThat(displayNameToNode.get("Other Tool").getImportOrder(), is(not(updatedDisplayNameToNode.get("Unknown Tool").getImportOrder())));
    }

    public Pair<Dataset, CodingSheet> setupMappingsForTest(Ontology ontology) {
        // create dataset
        Dataset dataset = new Dataset();
        dataset.setTitle("test");
        dataset.setDate(2134);
        dataset.setDescription("test");
        dataset.markUpdated(getUser());
        genericService.save(dataset);
        // create data table
        DataTable dataTable = new DataTable();
        dataTable.setName("test");
        dataTable.setDataset(dataset);
        genericService.save(dataTable);
        // create data table column
        DataTableColumn dataTableColumn = new DataTableColumn();
        dataTableColumn.setName("test");
        dataTableColumn.setDisplayName("test");
        dataTableColumn.setColumnDataType(DataTableColumnType.VARCHAR);
        dataTableColumn.setColumnEncodingType(DataTableColumnEncodingType.UNCODED_VALUE);
        dataTableColumn.setDataTable(dataTable);
        genericService.save(dataTableColumn);
        dataTable.getDataTableColumns().add(dataTableColumn);
        dataset.getDataTables().add(dataTable);
        // FIXME: this won't work since it expects the DataTableColumn to be a real column with data in the data import db
        // CodingSheet codingSheet = dataIntegrationService.createGeneratedCodingSheet(dataTableColumn, getUser(), ontology);
        CodingSheet codingSheet = new CodingSheet();
        codingSheet.setTitle("generated coding sheet for test column");
        codingSheet.setDescription("system generated identity coding sheet");
        codingSheet.markUpdated(getUser());
        codingSheet.setGenerated(true);
        codingSheet.setDefaultOntology(ontology);
        dataTableColumn.setDefaultCodingSheet(codingSheet);
        genericService.save(codingSheet);
        genericService.save(dataTableColumn);
        // create mapping
        OntologyNode toolNode = ontology.getNodeByName("Tool");
        CodingRule rule = new CodingRule(codingSheet, "Tool");
        rule.setOntologyNode(toolNode);
        genericService.save(rule);
        codingSheet.getCodingRules().add(rule);
        return Pair.create(dataset, codingSheet);
    }

    @Test
    @Rollback
    public void createOntologyOrderTest() throws Exception {
        controller = super.generateNewInitializedController(OntologyController.class);
        controller.prepare();
        Ontology ont = controller.getOntology();
        ont.setTitle("test ontology for ordering");
        ont.setDescription("test");
        ont.markUpdated(getUser());
        String ontText = IOUtils.toString(getClass().getResourceAsStream(TAB_ONTOLOGY_FILE));
        assertNotNull(ontText);
        controller.setFileInputMethod("text");
        controller.setFileTextInput(ontText);
        controller.setServletRequest(getServletPostRequest());
        controller.save();
        Long id = ont.getId();
        ont = ontologyService.find(id);
        logger.debug(ont.getTitle());
        String[] split = ontText.split("[\\r|\\n+]");
        List<OntologyNode> ontologyNodes = ont.getSortedOntologyNodesByImportOrder();
        assertNotNull(ontologyNodes);
        logger.trace("{}", ontologyNodes);
        int i = 0;
        for (String line : split) {
            if (!StringUtils.isBlank(line)) {
                String nodeLabel = line.trim();
                ArrayList<String> synonyms = new ArrayList<String>();
                Matcher matcher = OwlOntologyConverter.SYNONYM_PATTERN.matcher(line);
                if (matcher.matches()) {
                    nodeLabel = matcher.group(1).trim();
                    Set<String> nodeSynonyms = ontologyNodes.get(i).getSynonyms();
                    logger.debug("node synonyms for " + ontologyNodes.get(i).getDisplayName() + ": " + nodeSynonyms);
                    assertEquals(nodeSynonyms.size(), matcher.group(2).split(OwlOntologyConverter.SYNONYM_SPLIT_REGEX).length);
                    for (String synonym : matcher.group(2).split(OwlOntologyConverter.SYNONYM_SPLIT_REGEX)) {
                        synonym = synonym.trim();
                        synonyms.add(synonym);
                        logger.trace("checking for " + synonym);
                        assertTrue(nodeSynonyms.contains(synonym));
                    }
                }
                String nodeFragment = OwlOntologyConverter.labelToFragmentId(nodeLabel);
                logger.info("text:" + line + " <--> parsed:" + ontologyNodes.get(i).getIri());
                assertEquals(nodeLabel, ontologyNodes.get(i).getDisplayName().trim());
                assertEquals(nodeFragment, ontologyNodes.get(i).getIri().trim());
                i++;
            }
        }

        assertEquals(i, ontologyNodes.size());
    }

}
