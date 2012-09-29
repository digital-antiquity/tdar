package org.tdar.struts.action;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.tdar.core.bean.resource.DataValueOntologyNodeMapping;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.bean.resource.dataTable.DataTableColumnType;
import org.tdar.core.service.OntologyService;

/**
 * $Id$
 * 
 * @author Adam Brin
 * @version $Revision$
 */
public class OntologyControllerITCase extends AbstractAdminControllerITCase {

    @Autowired
    private OntologyController controller;
    @Autowired
    private OntologyService ontologyService;

    public final static String TAB_ONTOLOGY_FILE = "/ontology/tabOntologyFile.txt";
    public final static String UPDATED_TAB_ONTOLOGY_FILE = "/ontology/updatedTabOntologyFile.txt";

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.struts.action.AbstractControllerITCase#getController()
     */
    @Override
    protected TdarActionSupport getController() {
        return controller;
    }

    @Test
    @Rollback
    public void testControllerLoadsTabText() throws IOException {
        controller = super.generateNewInitializedController(OntologyController.class);
        controller.prepare();
        Ontology ont = controller.getOntology();
        ont.setTitle("test ontology for ordering");
        ont.setDescription("test");
        ont.markUpdated(getUser());
        String ontText = IOUtils.toString(getClass().getResourceAsStream(TAB_ONTOLOGY_FILE));
        assertNotNull("input should be the same", ontText);
        controller.setFileInputMethod("text");
        controller.setFileTextInput(ontText);
        controller.save();
        Long id = ont.getId();
        controller = generateNewInitializedController(OntologyController.class);
        Collection<InformationResourceFileVersion> currentVersions = ont.getLatestVersions();
        controller.setResourceId(id);
        controller.prepare();
        controller.loadBasicMetadata();
        controller.loadCustomMetadata();
        controller.setFileInputMethod("text");
        assertEquals(ontText, controller.getFileTextInput());
        controller.save();
        assertEquals("versions should be the same", currentVersions, controller.getResource().getLatestVersions());
        controller.setResourceId(id);
        controller.prepare();
        controller.loadBasicMetadata();
        controller.loadCustomMetadata();
        controller.setFileInputMethod("text");
        controller.setFileTextInput(controller.getFileTextInput() + "a");
        controller.save();
        assertFalse("versions should not be the same", currentVersions.equals(controller.getResource().getLatestVersions()));
    }

    @Test
    @Rollback
    public void testOntologyWithReservedChars() throws IOException {
        String ontText = IOUtils.toString(getClass().getResourceAsStream("/ontology/nodes_with_bad_chars_and_weird_percents.txt"));
        Long id = loadOntologyFromText(ontText);
        Ontology ont = ontologyService.find(id);
        Map<String, OntologyNode> map = new HashMap<String, OntologyNode>();
        for (OntologyNode node : ont.getOntologyNodes()) {
            map.put(node.getIri(), node);
        }
        assertEquals("<Fish Element Additional>", map.get("_Fish_Element_Additional_").getDisplayName());
        assertEquals("Navicular (Central) & Cuboid", map.get("Navicular_(Central)_Cuboid").getDisplayName());
        assertEquals("4th Tarsal", map.get("Navicular_(Central)_Cuboid").getSynonyms().iterator().next());
    }

    @Test
    @Rollback
    public void testMappedOntologyUpdate() throws IOException {

        String ontText = IOUtils.toString(getClass().getResourceAsStream(TAB_ONTOLOGY_FILE));
        int originalFileLength = ontText.split("([\r\n]+)").length;
        Long id = loadOntologyFromText(ontText);
        Ontology ont = ontologyService.find(id);
        logger.debug(ont.getTitle());
        List<OntologyNode> ontologyNodes = new ArrayList<OntologyNode>(ont.getOntologyNodes());
        assertNotNull(ontologyNodes);
        logger.info(ontologyNodes);
        setupMappingsForTest(ontologyNodes);
        Collection<InformationResourceFileVersion> latestVersions = ont.getLatestVersions();
        controller = super.generateNewInitializedController(OntologyController.class);
        controller.setResourceId(id);
        controller.prepare();
        ont = controller.getOntology();
        ontText = IOUtils.toString(getClass().getResourceAsStream(UPDATED_TAB_ONTOLOGY_FILE));
        int updatedFileLength = ontText.split("([\r\n]+)").length;
        assertNotNull(ontText);
        controller.setFileInputMethod("text");
        controller.setFileTextInput(ontText);
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
        logger.info(ontologyNodes.size() + " (" + originalFileLength + ")" + " - " + updatedOntologyNodes.size() + " (" + updatedFileLength + ")");
        assertEquals("original file matches same number of parsed ontologyNodes", originalFileLength, ontologyNodes.size());
        assertEquals("updated file matches same number of parsed ontologyNodes", updatedFileLength, updatedOntologyNodes.size());
        assertSame("Original id was transferred for exact match",
                displayNameToNode.get("Tool").getId(),
                updatedDisplayNameToNode.get("Tool").getId());
        assertSame("Original id was transferred for exact match",
                displayNameToNode.get("Core").getId(), updatedDisplayNameToNode.get("Core").getId());
        assertThat(displayNameToNode.get("Other Tool").getImportOrder(), is(not(updatedDisplayNameToNode.get("Unknown Tool").getImportOrder())));
        assertFalse(latestVersions.equals(ont.getLatestUploadedVersions()));
    }

    private Long loadOntologyFromText(String ontText) {
        controller = super.generateNewInitializedController(OntologyController.class);
        controller.prepare();
        Ontology ont = controller.getOntology();
        ont.setTitle("test ontology for ordering");
        ont.setDescription("test");
        ont.markUpdated(getUser());
        assertNotNull(ontText);
        controller.setFileInputMethod("text");
        controller.setFileTextInput(ontText);
        controller.save();
        Long id = ont.getId();
        return id;
    }

    @Test
    @Rollback(true)
    public void testUnMappedOntologyUpdate() throws IOException {
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
        controller.save();
        Long id = ont.getId();
        ont = ontologyService.find(id);
        logger.debug(ont.getTitle());
        assertNotNull(ont.getOntologyNodes());
        List<OntologyNode> ontologyNodes = new ArrayList<OntologyNode>(ont.getOntologyNodes());
        assertNotNull(ontologyNodes);
        logger.info(ontologyNodes);

        controller = super.generateNewInitializedController(OntologyController.class);
        controller.setResourceId(id);
        controller.prepare();
        ont = controller.getOntology();
        ontText = IOUtils.toString(getClass().getResourceAsStream(UPDATED_TAB_ONTOLOGY_FILE));
        assertNotNull(ontText);
        controller.setFileInputMethod("text");
        controller.setFileTextInput(ontText);
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

    public void setupMappingsForTest(List<OntologyNode> ontologyNodes) {
        // create dataset
        Dataset dataset = new Dataset();
        dataset.setTitle("test");
        dataset.markUpdated(getTestPerson());
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
        dataTableColumn.setDataTable(dataTable);
        genericService.save(dataTableColumn);
        // create mapping
        DataValueOntologyNodeMapping dataValueOntologyNodeMapping = new DataValueOntologyNodeMapping();
        dataValueOntologyNodeMapping.setDataTableColumn(dataTableColumn);
        dataValueOntologyNodeMapping.setDataValue("Tool");
        dataValueOntologyNodeMapping.setOntologyNode(ontologyNodes.get(0));
        genericService.save(dataValueOntologyNodeMapping);
        HashSet<DataValueOntologyNodeMapping> mappings = new HashSet<DataValueOntologyNodeMapping>();
        mappings.add(dataValueOntologyNodeMapping);
        ontologyNodes.get(0).setDataValueOntologyNodeMappings(mappings);
        genericService.save(ontologyNodes);
    }

    @Test
    @Rollback(true)
    public void createOntologyOrderTest() throws IOException {
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
        controller.save();
        Long id = ont.getId();
        ont = ontologyService.find(id);
        logger.debug(ont.getTitle());
        String[] split = ontText.split("[\\r|\\n+]");
        List<OntologyNode> ontologyNodes = ont.getOntologyNodes();
        assertNotNull(ontologyNodes);
        OntologyService.sortOntologyNodesByImportOrder(ontologyNodes);
        logger.trace(ont.getOntologyNodes());
        int i = 0;
        for (String line : split) {
            if (!StringUtils.isBlank(line)) {
                String nodeLabel = line.trim();
                ArrayList<String> synonyms = new ArrayList<String>();
                Matcher matcher = OntologyService.SYNONYM_PATTERN.matcher(line);
                if (matcher.matches()) {
                    nodeLabel = matcher.group(1).trim();
                    Set<String> nodeSynonyms = ontologyNodes.get(i).getSynonyms();
                    logger.debug("node synonyms for " + ontologyNodes.get(i).getDisplayName() + ": " + nodeSynonyms);
                    assertEquals(nodeSynonyms.size(), matcher.group(2).split(OntologyService.SYNONYM_SPLIT_REGEX).length);
                    for (String synonym : matcher.group(2).split(OntologyService.SYNONYM_SPLIT_REGEX)) {
                        synonym = synonym.trim();
                        synonyms.add(synonym);
                        logger.trace("checking for " + synonym);
                        assertTrue(nodeSynonyms.contains(synonym));
                    }
                }
                String nodeFragment = ontologyService.labelToFragmentId(nodeLabel);
                logger.info("text:" + line + " <--> parsed:" + ontologyNodes.get(i).getIri());
                assertEquals(nodeLabel, ontologyNodes.get(i).getDisplayName().trim());
                assertEquals(nodeFragment, ontologyNodes.get(i).getIri().trim());
                i++;
            }
        }

        assertEquals(i, ontologyNodes.size());
    }

}
