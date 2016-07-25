package org.tdar.core.service.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.integration.IntegrationOntologySearchResult;
import org.tdar.core.dao.integration.search.OntologySearchFilter;
import org.tdar.core.dao.resource.OntologyDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.exception.TdarRuntimeException;
import org.tdar.core.parser.OwlApiHierarchyParser;
import org.tdar.core.service.FreemarkerService;
import org.tdar.core.service.ServiceInterface;
import org.tdar.core.service.resource.ontology.OntologyNodeWrapper;
import org.tdar.core.service.resource.ontology.OwlOntologyConverter;
import org.tdar.filestore.FilestoreObjectType;


/**
 * Transactional service providing persistence access to OntologyS as well as OWL access to Ontology files.
 * 
 * @author Allen Lee
 * @version $Revision$
 * @latest $Id$
 */
@Service
@Transactional
public class OntologyService extends ServiceInterface.TypedDaoBase<Ontology, OntologyDao> {

    private OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    FreemarkerService freemarkerService;

    /**
     * Find all ontologies, but return them with sparse objects (Title, Description only)
     * 
     * @return
     */
    @Transactional(readOnly=true)
    public List<Ontology> findSparseOntologyList() {
        return getDao().findSparseResourceBySubmitterType(null, ResourceType.ONTOLOGY);
    }

    /**
     * Parses the OWL file associated with the given Ontology and indexes it into a representation of @link OntologyNodes the database.
     * 
     * @param ontology
     */
    @Transactional(readOnly = false)
    public void shred(Ontology ontology) {
        InformationResourceFileVersion latestUploadedFile = ontology.getLatestUploadedVersion();
        // Collection<InformationResourceFileVersion> latestVersions = ontology.getLatestVersions(VersionType.UPLOADED);
        // check if the existing ontology had any previously mapped ontology nodes
        int numberOfMappedValues = getDao().getNumberOfMappedDataValues(ontology);
        List<OntologyNode> existingOntologyNodes = ontology.getOntologyNodes();
        // FIXME: should short-circuit if there's no ontology nodes to reconcile.
        getLogger().debug("FILE: {} {}", latestUploadedFile.getFilename(), latestUploadedFile.getFileVersionType());
        /*
         * two cases:
         * 1) where there's an OWL file uploaded by the user (unlikely, but possible)
         * 2) tab-delimited text uploaded by the user is UPLOADED_ARCHIVAL, the generated OWL file is UPLOADED
         */
        if (latestUploadedFile.getExtension().contains("owl")) {
            getLogger().debug("examining file: {}", latestUploadedFile);
            OwlApiHierarchyParser parser;
            try {
                OwlOntologyConverter converter = new OwlOntologyConverter();
                parser = new OwlApiHierarchyParser(ontology, converter.toOwlOntology(latestUploadedFile));
            } catch (FileNotFoundException e) {
                logger.warn("file not found: {}", e);
                throw new TdarRecoverableRuntimeException("error.file_not_found", e, Arrays.asList(latestUploadedFile.getFilename()));
            }
            List<OntologyNode> incomingOntologyNodes = parser.generate();
            getLogger().debug("created {} ontology nodes from {}", incomingOntologyNodes.size(), latestUploadedFile.getFilename());
            // start reconciliation process
            if (numberOfMappedValues > 0) {
                getLogger().debug("had mapped values, reconciling {} with {}", existingOntologyNodes, incomingOntologyNodes);
                reconcile(existingOntologyNodes, incomingOntologyNodes);
            } else {
                getLogger().debug("has no mappings... deleting ontology and replacing");
            }
            getDao().removeReferencesToOntologyNodes(existingOntologyNodes);
            getDao().delete(existingOntologyNodes);
            getDao().saveOrUpdate(incomingOntologyNodes);
            logger.debug("existing ontology nodes: {}", ontology.getOntologyNodes());
            ontology.getOntologyNodes().addAll(incomingOntologyNodes);
            owlOntologyManager.removeOntology(parser.getOwlOntology());
        }
    }

    /**
     * Modifies both lists of @link OntologyNode entries in place. After returning, incomingOntologyNodes
     * should contain all the reconciled incoming ontology nodes, and existingOntologyNodes should contain
     * all the ontology nodes that need to be deleted.
     * 
     * @param existingOntologyNodes
     * @param incomingOntologyNodes
     */
    private void reconcile(List<OntologyNode> existingOntologyNodes, List<OntologyNode> incomingOntologyNodes) {
        getLogger().debug("existing ontology nodes: {}", existingOntologyNodes);
        getLogger().debug("incoming ontology nodes: {}", incomingOntologyNodes);

        HashMap<String, OntologyNode> existingSet = new HashMap<>();
        HashMap<String, OntologyNode> synonymsSet = new HashMap<>();
        for (OntologyNode node : existingOntologyNodes) {
            existingSet.put(node.getIri(), node);
            for (String synonym : node.getSynonyms()) {
                synonymsSet.put(synonym, node);
            }
            synonymsSet.put(node.getDisplayName(), node);
        }

        for (int index = 0; index < incomingOntologyNodes.size(); index++) {
            // check to see if incoming has an equivalent in the existing nodes
            // if so, steal the ID
            OntologyNode incoming = incomingOntologyNodes.get(index);
            /*
             * Equivalency logic is as follows:
             * test equivalency of incoming and existing. If there is a match (the first match) then take it, and stop evaluating.
             * A potential problem here is if synonyms have matches to multiple nodes. Look at "Other Tool/Unknown Tool" in
             * ontology test cases
             */
            OntologyNode existing = existingSet.get(incoming.getIri());
            if (existing == null) {
                // ONLY MAP synonym to node if node remains umapped; e.g. if Dentary is a Synonym of Mandible, and Dentary becomes it's own Node, don't use the
                // id for Mandible
                OntologyNode synonym = synonymsSet.get(incoming.getDisplayName());
                if (synonym != null && existingSet.get(synonym.getIri()) == null) {
                    existing = synonym;
                }
            }

            if (existing == null) {
                continue;
            }
            Long original = existing.getId();
            Long incomingId = incoming.getId();
            incoming = getDao().merge(incoming, existing);

            getLogger().trace("{} {} -> {} <--> e: {} {} -> {} ", incomingId, incoming.getDisplayName(), incomingId, original, existing.getDisplayName(),
                    original);
            incomingOntologyNodes.set(index, incoming);
            existingOntologyNodes.remove(existing);
            existingSet.remove(existing.getIri());
            synonymsSet.remove(existing.getDisplayName());
            for (String synonym : existing.getSynonyms()) {
                synonymsSet.remove(synonym);
            }

        }
        existingOntologyNodes.removeAll(Collections.singleton(null));
        getLogger().debug("existing ontology nodes: {}", existingOntologyNodes);
        getLogger().debug("incoming ontology nodes: {}", incomingOntologyNodes);
    }

    /**
     * Takes an Ontology and finds the OWL @link InformationResourceFileVersion of the @link Ontology and returns the OntologyModel
     * 
     * @param ontology
     * @return
     * @throws FileNotFoundException
     */
    @Transactional(readOnly=true)
    public OntModel toOntModel(Ontology ontology) throws FileNotFoundException {
        Collection<InformationResourceFileVersion> files = ontology.getLatestVersions();
        int size = files.size();
        if (size != 1) {
            throw new TdarRecoverableRuntimeException("ontologyService.could_not_determine_which_file", Arrays.asList(size));
        }
        for (InformationResourceFileVersion irFile : files) {
            File file = TdarConfiguration.getInstance().getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, irFile);
            if (file.exists()) {
                OntModel ontologyModel = ModelFactory.createOntologyModel();
                String url = ontology.getUrl();
                if (url == null) {
                    url = "";
                }
                try {
                    ontologyModel.read(new FileReader(file), url);
                    return ontologyModel;
                } catch (FileNotFoundException exception) {
                    // this should never happen since we're explicitly checking file.exists()...
                    throw new TdarRuntimeException(exception);
                }
            }
        }
        return null;
    }

    /**
     * Filters a List of @link OntologyNode entries to to just the direct children of the @link OntologyNode.
     * 
     * @param allNodes
     * @param parent
     * @return
     */
    @Transactional(readOnly=true)
    public List<OntologyNode> getChildren(List<OntologyNode> allNodes, OntologyNode parent) {
        List<OntologyNode> toReturn = new ArrayList<>();
        if (parent == null) {
            throw new TdarRecoverableRuntimeException("ontologyService.parent_node_not_defined");
        }
        for (OntologyNode currentNode : allNodes) {
            if (currentNode.getIndex().equals(parent.getIndex() + "." + currentNode.getIntervalStart())) {
                toReturn.add(currentNode);
            }
        }
        getLogger().trace("returning: {}", toReturn);
        return toReturn;
    }

    /**
     * Returns @link OntologyNode entries that are at the root of their trees (i.e. first Branches; their parent is the root).
     * 
     * @param allNodes
     * @return
     */
    @Transactional(readOnly=true)
    public List<OntologyNode> getRootElements(List<OntologyNode> allNodes) {
        List<OntologyNode> toReturn = new ArrayList<>();
        for (OntologyNode currentNode : allNodes) {
            String index = currentNode.getIndex();
            if (StringUtils.isNotBlank(index) && StringUtils.isNumeric(index)) {
                toReturn.add(currentNode);
            }
        }
        return toReturn;
    }

    /**
     * Find the number of @link CodingRule entries that refer to the @link DataTableColumn
     * 
     * @param dataTableColumn
     * @return
     */
    @Transactional
    public int getNumberOfMappedDataValues(DataTableColumn dataTableColumn) {
        return getDao().getNumberOfMappedDataValues(dataTableColumn);
    }

    /**
     * Check if the @link DataTableColumn has any associations with an @link Ontology
     * 
     * @param dataTableColumn
     * @return
     */
    @Transactional
    public boolean isOntologyMapped(DataTableColumn dataTableColumn) {
        return getDao().getNumberOfMappedDataValues(dataTableColumn) > 0;
    }

    /**
     * Converts a Text representation of an @link Ontology using TABs to an RDF/XML/OWL Ontology
     * 
     * @param id
     * @param fileTextInput
     * @return
     */
    @Transactional(readOnly=true)
    public String toOwlXml(Long id, String fileTextInput) {
        OwlOntologyConverter converter = new OwlOntologyConverter();
        return converter.toOwlXml(id, fileTextInput, freemarkerService);
    }

    @Transactional(readOnly = true)
    public IntegrationOntologySearchResult findOntologies(OntologySearchFilter searchFilter) {
        return getDao().findOntologies(searchFilter);
    }

    @Transactional(readOnly=true)
    public OntologyNodeWrapper prepareOntologyJson(Ontology ontology) {
        List<OntologyNode> nodes = ontology.getSortedOntologyNodes();
        Collections.reverse(nodes);
        Map<Long, OntologyNodeWrapper> tree = new HashMap<>();
        OntologyNodeWrapper root = null;
        Set<OntologyNodeWrapper> roots = new HashSet<>();
        for (OntologyNode node : nodes) {
            OntologyNodeWrapper value = new OntologyNodeWrapper(node);
            if (!node.getIndex().contains(".")) {
                root = value;
                roots.add(value);
            }
            tree.put(node.getId(), value);
        }
        for (OntologyNode node : nodes) {
            for (OntologyNode c : nodes) {
                if (c == node) {
                    continue;
                }

                if (c.isChildOf(node)) {
                    OntologyNodeWrapper e = tree.get(c.getId());
                    if (c.getParentNode() != null) {
                        int cIndex = StringUtils.countMatches(c.getParentNode().getIndex(), ".");
                        int index_ = StringUtils.countMatches(node.getIndex(), ".");
                        if (cIndex > index_) {
                            continue;
                        } else {
                            OntologyNodeWrapper wrap = tree.get(c.getParentNode().getId());
                            wrap.getChildren().remove(e);
                            if (wrap.getChildren().isEmpty()) {
                                wrap.setChildren(null);
                            }
                        }
                    }
                    c.setParentNode(node);
                    OntologyNodeWrapper wrapper = tree.get(node.getId());
                    if (wrapper.getChildren() == null) {
                        wrapper.setChildren(new ArrayList<>());
                    }
                    wrapper.getChildren().add(e);
                }
            }
        }

        if (roots.size() > 1) {
            OntologyNodeWrapper wrapper = new OntologyNodeWrapper();
            wrapper.setId(-1L);
            wrapper.setDisplayName("");
            wrapper.getChildren().addAll(roots);
            root = wrapper;
        }
        return root;
    }
}
