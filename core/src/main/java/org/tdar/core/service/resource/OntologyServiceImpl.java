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
import java.util.Iterator;
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
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.dao.integration.IntegrationOntologySearchResult;
import org.tdar.core.dao.integration.search.OntologySearchFilter;
import org.tdar.core.dao.resource.OntologyDao;
import org.tdar.core.service.FreemarkerService;
import org.tdar.core.service.ServiceInterface;
import org.tdar.core.service.resource.ontology.OntologyNodeWrapper;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.exception.TdarRuntimeException;
import org.tdar.filestore.FilestoreObjectType;
import org.tdar.parser.OwlApiHierarchyParser;
import org.tdar.parser.OwlOntologyConverter;
import org.tdar.parser.TOntologyNode;
import org.tdar.utils.PersistableUtils;

/**
 * Transactional service providing persistence access to OntologyS as well as OWL access to Ontology files.
 * 
 * @author Allen Lee
 * @version $Revision$
 * @latest $Id$
 */
@Service
@Transactional
public class OntologyServiceImpl extends ServiceInterface.TypedDaoBase<Ontology, OntologyDao> implements OntologyService {

    private OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    FreemarkerService freemarkerService;

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.OntologyService#findSparseOntologyList()
     */
    @Override
    @Transactional(readOnly = true)
    public List<Ontology> findSparseOntologyList() {
        return getDao().findSparseResourceBySubmitterType(null, ResourceType.ONTOLOGY);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.OntologyService#shred(org.tdar.core.bean.resource.Ontology)
     */
    @Override
    @Transactional(readOnly = false)
    public void shred(Ontology ontology) {
        InformationResourceFileVersion latestUploadedFile = ontology.getLatestUploadedVersion();
        // Collection<InformationResourceFileVersion> latestVersions = ontology.getLatestVersions(VersionType.UPLOADED);
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
                parser = new OwlApiHierarchyParser(converter.toOwlOntology(latestUploadedFile));
            } catch (FileNotFoundException e) {
                logger.warn("file not found: {}", e);
                throw new TdarRecoverableRuntimeException("error.file_not_found", e, Arrays.asList(latestUploadedFile.getFilename()));
            }
            List<TOntologyNode> incomingOntologyNodes = parser.generate();
            getLogger().debug("created {} ontology nodes from {}", incomingOntologyNodes.size(), latestUploadedFile.getFilename());
            // start reconciliation process
            getLogger().debug("had mapped values, reconciling {} with {}", existingOntologyNodes, incomingOntologyNodes);
            reconcile(ontology, incomingOntologyNodes);
            logger.debug("existing ontology nodes: {}", ontology.getOntologyNodes());
            getDao().saveOrUpdate(existingOntologyNodes);

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
    private void reconcile(Ontology ontology, List<TOntologyNode> incomingOntologyNodes) {
        List<OntologyNode> existingOntologyNodes = ontology.getOntologyNodes();
        getLogger().debug("existing ontology nodes: {}", existingOntologyNodes);
        getLogger().debug("incoming ontology nodes: {}", incomingOntologyNodes);

        HashMap<String, OntologyNode> existingSet = new HashMap<>();
        for (OntologyNode node : ontology.getOntologyNodes()) {
            existingSet.put(node.getIri(), node);
        }
        List<OntologyNode> toRemove = new ArrayList<>();
        List<Long> seenIds = new ArrayList<>();
        List<TOntologyNode> incomingList = new ArrayList<>(incomingOntologyNodes);
        seenIds.addAll(reconcileExistingMatchesByIrI(existingSet, incomingList));
        seenIds.addAll(reconcileExistingMatchingOnSynonyms(ontology, existingSet,incomingList));

        
        Iterator<OntologyNode> iterator = ontology.getOntologyNodes().iterator();
        while (iterator.hasNext()) {
            OntologyNode node = iterator.next();
            if (!seenIds.contains(node.getId()) && PersistableUtils.isNotTransient(node)) {
                toRemove.add(node);
                iterator.remove();
            }
        }
        getDao().removeReferencesToOntologyNodes(toRemove);

        getLogger().debug("existing ontology nodes: {}", existingOntologyNodes);
        getLogger().debug("removing ontology nodes: {}", toRemove);
        getLogger().debug("incoming ontology nodes: {}", incomingOntologyNodes);
        getDao().delete(toRemove);
    }

    private Set<Long> reconcileExistingMatchingOnSynonyms(Ontology ontology, HashMap<String, OntologyNode> existingSet, List<TOntologyNode> incomingList) {
        Set<Long> seenIds = new HashSet<>();
        Iterator<TOntologyNode> iterator = incomingList.iterator();
        while (iterator.hasNext()) {
            TOntologyNode incoming = iterator.next();
            OntologyNode existing = null;
            for (OntologyNode existing_ : existingSet.values()) {
                for (String syn : existing_.getSynonyms()) {
                    if (StringUtils.equals(syn, incoming.getDisplayName()) || incoming.getSynonyms().contains(syn)) {
                        existing = existing_;
                        break;
                    }
                    if (existing != null) {
                        break;
                    }
                }
            }
            
            if (incoming.getDisplayName().contains("Mandible") || incoming.getDisplayName().contains("Dentary") ) {
                logger.debug("existing: {}; incoming: {} / {} ",existing, incoming.getIri(), incoming.getDisplayName());
            }
            
            if (existing != null) {
                Long original = existing.getId();
                seenIds.add(original);
                iterator.remove();
            } else {
                existing = new OntologyNode();
                existing.setOntology(ontology);
                ontology.getOntologyNodes().add(existing);
            }
            copyIncomingToExisting(incoming, existing);

            existingSet.remove(incoming.getDisplayName());
        }
        return seenIds;
    }

    private Set<Long> reconcileExistingMatchesByIrI(HashMap<String, OntologyNode> existingSet, List<TOntologyNode> incomingList) {
        Set<Long> seenIds = new HashSet<>();
        Iterator<TOntologyNode> iterator = incomingList.iterator();
        while (iterator.hasNext()) {
            TOntologyNode incoming = iterator.next();
            OntologyNode existing = existingSet.get(incoming.getIri());
            if (incoming.getDisplayName().contains("Mandible") || incoming.getDisplayName().contains("Dentary") ) {
                logger.debug("existing: {}; incoming: {} / {} ",existing, incoming.getIri(), incoming.getDisplayName());
            }
            
            if (existing != null) {
                Long original = existing.getId();
                seenIds.add(original);
                copyIncomingToExisting(incoming, existing);
                iterator.remove();
            }

            existingSet.remove(incoming.getIri());
        }
        return seenIds;
    }

    private void copyIncomingToExisting(TOntologyNode incoming, OntologyNode existing) {
        existing.setImportOrder(incoming.getImportOrder());
        existing.setDescription(incoming.getDescription());
        existing.setDisplayName(incoming.getDisplayName());
        existing.setIndex(incoming.getIndex());
        existing.setIri(incoming.getIri());
        existing.setIntervalStart(incoming.getIntervalStart());
        existing.setIntervalEnd(incoming.getIntervalEnd());
        existing.setUri(incoming.getUri());
        existing.setSynonyms(incoming.getSynonyms());
    }

    private OntologyNode getExisting(HashMap<String, OntologyNode> existingSet, HashMap<String, OntologyNode> synonymsSet, TOntologyNode incoming) {
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
        return existing;
    }

    private HashMap<String, OntologyNode> buildSynonyms(List<OntologyNode> existingOntologyNodes, HashMap<String, OntologyNode> existingSet) {
        HashMap<String, OntologyNode> synonymsSet = new HashMap<>();
        for (OntologyNode node : existingOntologyNodes) {
            existingSet.put(node.getIri(), node);
            for (String synonym : node.getSynonyms()) {
                synonymsSet.put(synonym, node);
            }
            synonymsSet.put(node.getDisplayName(), node);
        }
        return synonymsSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.OntologyService#toOntModel(org.tdar.core.bean.resource.Ontology)
     */
    @Override
    @Transactional(readOnly = true)
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.OntologyService#getChildren(java.util.List, org.tdar.core.bean.resource.OntologyNode)
     */
    @Override
    @Transactional(readOnly = true)
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.OntologyService#getRootElements(java.util.List)
     */
    @Override
    @Transactional(readOnly = true)
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

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.OntologyService#getNumberOfMappedDataValues(org.tdar.core.bean.resource.datatable.DataTableColumn)
     */
    @Override
    @Transactional
    public int getNumberOfMappedDataValues(DataTableColumn dataTableColumn) {
        return getDao().getNumberOfMappedDataValues(dataTableColumn);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.OntologyService#isOntologyMapped(org.tdar.core.bean.resource.datatable.DataTableColumn)
     */
    @Override
    @Transactional
    public boolean isOntologyMapped(DataTableColumn dataTableColumn) {
        return getDao().getNumberOfMappedDataValues(dataTableColumn) > 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.OntologyService#toOwlXml(java.lang.Long, java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public String toOwlXml(Long id, String fileTextInput) {
        EnhancedOwlOntologyConverter converter = new EnhancedOwlOntologyConverter();
        return converter.toOwlXml(id, fileTextInput, freemarkerService);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.OntologyService#findOntologies(org.tdar.core.dao.integration.search.OntologySearchFilter)
     */
    @Override
    @Transactional(readOnly = true)
    public IntegrationOntologySearchResult findOntologies(OntologySearchFilter searchFilter) {
        return getDao().findOntologies(searchFilter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.tdar.core.service.resource.OntologyService#prepareOntologyJson(org.tdar.core.bean.resource.Ontology)
     */
    @Override
    @Transactional(readOnly = true)
    public OntologyNodeWrapper prepareOntologyJson(Ontology ontology) {
        List<OntologyNode> nodes = ontology.getSortedOntologyNodes();
        Collections.reverse(nodes);
        Map<Long, OntologyNodeWrapper> tree = new HashMap<>();
        OntologyNodeWrapper root = null;
        Set<OntologyNodeWrapper> roots = new HashSet<>();
        for (OntologyNode node : nodes) {
            logger.debug("{}", node);
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
