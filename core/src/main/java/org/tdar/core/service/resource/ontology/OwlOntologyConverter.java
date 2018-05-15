package org.tdar.core.service.resource.ontology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.exception.TdarRuntimeException;
import org.tdar.core.service.FreemarkerService;
import org.tdar.filestore.FilestoreObjectType;

/**
 * Converts a text formatted Ontology into an OWL XML ontology
 * 
 */
public class OwlOntologyConverter {

    private OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String IRI_INVALID_CHARACTERS_REGEX = "[^\\w~.-]";
    public static final Pattern TAB_PREFIX_PATTERN = Pattern.compile("^(\\t+).*$");
    public static final String SYNONYM_SPLIT_REGEX = ";";
    public static final Pattern SYNONYM_PATTERN = Pattern.compile("^(.+)[\\(\\[](.+)[\\)\\]](.*)$");
    public static final Pattern DESCRIPTION_PATTERN = Pattern.compile("^(.+)\\{(.+)\\}$");

    public static final String TDAR_ORDER_PREFIX = "TDAROrder-";
    public static final String TDAR_DESCRIPTION_PREFIX = "TDARDescription-";
    public static final String TDAR_NODE_ENTRY = "TDARNode";

    /**
     * Load an OWLOntology from an IRI
     * 
     * @param iri
     * @return
     */
    private OWLOntology loadFromIRI(IRI iri) {
        try {
            return owlOntologyManager.loadOntologyFromOntologyDocument(iri);
        } catch (UnparsableOntologyException exception) {
            logger.error("Couldn't parse ontology from iri " + iri, exception);
            throw new TdarRuntimeException(exception);
        } catch (OWLOntologyCreationException exception) {
            logger.debug("Ontology already exists in manager, attempt to get it instead", exception);
            return owlOntologyManager.getOntology(iri);
        }

    }

    /**
     * Convert a String label to an IRI
     * 
     * @param label_
     * @return
     */
    public static String labelToFragmentId(String label_) {
        if (StringUtils.isBlank(label_)) {
            return "";
        }
        String label = label_.trim().replaceAll(IRI_INVALID_CHARACTERS_REGEX, "_");
        if (label.matches("^(\\d).*")) {
            label = "_" + label;
        }
        return label;
    }

    /**
     * Convert an @link InformationResourceFileVersion (to an OWLOntology)
     * 
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public OWLOntology toOwlOntology(InformationResourceFileVersion file) throws FileNotFoundException {
        File transientFile = TdarConfiguration.getInstance().getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, file);
        if ((file != null) && transientFile.exists()) {
            IRI iri = IRI.create(transientFile);
            return loadFromIRI(iri);
        }
        logger.debug("could not find ontology file for: {}", file);
        return null;

    }

    /**
     * Returns an OWL XML String, given a tab-separated
     * FIXME: continue to refactor this.
     * 
     * @author qyan
     * @param inputString
     * @return
     */
    public String toOwlXml(Long ontologyId, String inputString, FreemarkerService freemarkerService) {
        if (StringUtils.isBlank(inputString)) {
            return "";
        }
        List<OntologyNode> nodes = new ArrayList<>();
        Map<String, List<OntologyNode>> uniqueIriSet = new HashMap<>();
        List<OntologyNode> parentNodes = new ArrayList<>();
        long order = -1;
        String line;
        BufferedReader reader = new BufferedReader(new StringReader(inputString));
        try {
            while ((line = reader.readLine()) != null) {
                order++;
                logger.trace("processing line {}:\t{}", order, line);
                if (StringUtils.isEmpty(line.trim())) {
                    continue;
                }

                // validate that we don't start with a space start with
                if (line.startsWith(" ")) {
                    throw new TdarRecoverableRuntimeException("owlOntologyConverter.start_invalid_char", Arrays.asList(line));
                }

                int currentDepth = getNumberOfPrefixTabs(line);
                // remove tabs and replace all repeated non-word characters ([a-zA-Z_0-9]) with single "_". sanitized label for OWL use, and a description.
                Matcher descriptionMatcher = DESCRIPTION_PATTERN.matcher(line.trim());
                String description = null;
                if (descriptionMatcher.matches()) {
                    line = descriptionMatcher.group(1);
                    description = descriptionMatcher.group(2);
                }
                Matcher m = SYNONYM_PATTERN.matcher(line.trim());
                logger.trace(line);
                Set<OntologyNode> synonymNodes = new HashSet<>();
                if (m.matches()) {
                    line = m.group(1);
                    // handle multiple synonyms
                    for (String synonym : m.group(2).split(SYNONYM_SPLIT_REGEX)) {
                        if (StringUtils.isBlank(synonym)) {
                            continue;
                        }
                        OntologyNode synonymNode = new OntologyNode(labelToFragmentId(synonym), synonym.trim());
                        synonymNodes.add(synonymNode);
                    }
                }
                OntologyNode currentNode = new OntologyNode(labelToFragmentId(line), line.trim());
                currentNode.setSynonymNodes(synonymNodes);
                currentNode.setDescription(description);
                addToDuplicateCheck(uniqueIriSet, currentNode);
                for (OntologyNode synonym : synonymNodes) {
                    addToDuplicateCheck(uniqueIriSet, synonym);
                }
                nodes.add(currentNode);
                currentNode.setImportOrder(order);
                if (currentDepth == 0) {
                    parentNodes.clear();
                } else {
                    int numberOfAvailableParents = parentNodes.size();
                    // current depth may be degenerate (i.e., current depth of 4 but most immediate parent is of depth 2).
                    int parentIndex = ((currentDepth > numberOfAvailableParents)
                            // degenerate depth, pick closest parent.
                            ? numberOfAvailableParents
                            // normal parent
                            : currentDepth)
                            // parent off-by-one
                            - 1;
                    if (parentIndex == -1) {
                        logger.error("Parent index was set to -1.  parentList: {}, line: {}. resetting parentIndex to 0", parentNodes, line);
                        parentIndex = 0;
                    } else if (parentIndex >= parentNodes.size()) {
                        logger.error("Parent index was exceeds parentList size.  parentList: {}. resetting parentIndex to 0", parentNodes, line);
                        parentIndex = 0;
                    }
                    currentNode.setParentNode(parentNodes.get(parentIndex));
                }
                if (parentNodes.size() + 1 < currentDepth) {
                    logger.debug("parentNode list:{}", parentNodes);
                    logger.debug("adding node to position {}: {}", currentDepth, currentNode);
                    throw new TdarRecoverableRuntimeException("owlOntologyConverter.bad_depth", Arrays.asList(currentDepth, currentNode.getDisplayName()));
                }
                parentNodes.add(currentDepth, currentNode);
            }

        } catch (IOException e) {
            throw new TdarRecoverableRuntimeException("owlOntologyConverter.error_parsing");
        } finally {
            IOUtils.closeQuietly(reader);
        }

        testOntologyNodesUnique(uniqueIriSet);
        uniqueIriSet.clear();
        Map<String, Object> map = new HashMap<>();
        map.put("baseUrl", TdarConfiguration.getInstance().getBaseUrl());
        map.put("id", ontologyId);
        map.put("ontlogyNodes", nodes);
        try {
            String result = freemarkerService.render("owl-ontology.ftl", map);
            logger.debug(result);
            return result;
        } catch (IOException e) {
            throw new TdarRecoverableRuntimeException("owlOntologyConverter.error_writing");
        }
    }

    private void addToDuplicateCheck(Map<String, List<OntologyNode>> uniqueIriSet, OntologyNode currentNode) {
        List<OntologyNode> key = uniqueIriSet.get(currentNode.getIri());
        logger.trace("{} {}", currentNode.getIri(), key);
        if (key == null) {
            key = new ArrayList<OntologyNode>();
            uniqueIriSet.put(currentNode.getIri(), key);
        }
        key.add(currentNode);
    }

    /**
     * Tests that the List of OntologyNodes are unique.
     * 
     * @param nodeList
     */
    public void testOntologyNodesUnique(Map<String, List<OntologyNode>> uniqueSet) {
        StringBuilder dups = new StringBuilder();
        int dupCount = 0;
        for (Entry<String, List<OntologyNode>> entry : uniqueSet.entrySet()) {
            if (CollectionUtils.isNotEmpty(entry.getValue()) && (CollectionUtils.size(entry.getValue()) > 1)) {
                dupCount++;
                if (dups.length() != 0) {
                    dups.append(", ");
                }
                dups.append(entry.getKey());
            }
        }

        logger.debug("incoming: {} duplicates: {}", uniqueSet.size(), dupCount);
        if (dups.length() > 0) {
            logger.debug("duplicates: {}", dups);
            throw new TdarRecoverableRuntimeException("owlOntologyConverter.node_names_unique", Arrays.asList(dupCount, dups.toString()));
        }
    }

    /**
     * Returns the number of tabs at the beginning of this string.
     * 
     * @param line
     * @return
     */
    public static int getNumberOfPrefixTabs(String line) {
        Matcher matcher = TAB_PREFIX_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1).length();
        }
        return 0;
    }

}
