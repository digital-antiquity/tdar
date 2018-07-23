package org.tdar.db.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.exception.TdarRuntimeException;
import org.tdar.filestore.FileStoreFileProxy;
import org.tdar.filestore.FilestoreObjectType;

/**
 * Converts a text formatted Ontology into an OWL XML ontology
 * 
 */
public class OwlOntologyConverter {

    private OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();

    protected final Logger logger = LoggerFactory.getLogger(getClass());

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
    public OWLOntology toOwlOntology(FileStoreFileProxy file) throws FileNotFoundException {
        File transientFile = TdarConfiguration.getInstance().getFilestore().retrieveFile(FilestoreObjectType.RESOURCE, file);
        if ((file != null) && transientFile.exists()) {
            IRI iri = IRI.create(transientFile);
            return loadFromIRI(iri);
        }
        logger.debug("could not find ontology file for: {}", file);
        return null;

    }


    protected void addToDuplicateCheck(Map<String, List<TOntologyNode>> uniqueIriSet, TOntologyNode currentNode) {
        List<TOntologyNode> key = uniqueIriSet.get(currentNode.getIri());
        logger.trace("{} {}", currentNode.getIri(), key);
        if (key == null) {
            key = new ArrayList<>();
            uniqueIriSet.put(currentNode.getIri(), key);
        }
        key.add(currentNode);
    }

    /**
     * Tests that the List of OntologyNodes are unique.
     * 
     * @param nodeList
     */
    public void testOntologyNodesUnique(Map<String, List<TOntologyNode>> uniqueSet) {
        StringBuilder dups = new StringBuilder();
        int dupCount = 0;
        for (Entry<String, List<TOntologyNode>> entry : uniqueSet.entrySet()) {
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
