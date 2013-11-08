package org.tdar.core.service.resource.ontology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.exception.TdarRuntimeException;
import org.tdar.utils.MessageHelper;
/**
 * Converts a text formatted Ontology into an OWL XML ontology
 * 
 * FIXME: convert to use FREEMARKER
 */
public class OwlOntologyConverter {

    private static final String CLASS = "class";

    private static final String COMMENT = "comment";

    private OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public final static String IRI_INVALID_CHARACTERS_REGEX = "[^\\w~.-]";
    public final static Pattern TAB_PREFIX_PATTERN = Pattern.compile("^(\\t+).*$");
    public static final String SYNONYM_SPLIT_REGEX = ";";
    public final static Pattern SYNONYM_PATTERN = Pattern.compile("^(.+)[\\(\\[](.+)[\\)\\]](.*)$");
    private static final Namespace tdarNamespace = new Namespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

    public final static String ONTOLOGY_START_STRING_FORMAT = "<?xml version=\"1.0\"?>\n" + "<rdf:RDF\n"
            + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"
            + "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" + "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
            + "xmlns=\"%s/ontologies/%d#\"\n" + "xmlns:tdar=\"%s/ontologies#\"\n"
            + "xml:base=\"%s/ontologies/%d\">\n" + "<owl:Ontology rdf:about=\"\"/>\n";

    private final static String OWL_CLASS_FORMAT = " <owl:Class rdf:ID=\"%s\">\n\t<rdfs:label><![CDATA[%s]]></rdfs:label>\n%s%s\n</owl:Class>\n";

    private final static String OWL_SAME_AS = "<owl:equivalentClass rdf:resource=\"#%s\"/>\n";

    // subclass format will be embedded into the above OWL_CLASS_FORMAT (if necessary)
    private final static String OWL_SUBCLASS_FORMAT = "<rdfs:subClassOf rdf:resource=\"#%s\"/>\n";

    private final static String ONTOLOGY_END_STRING = "</rdf:RDF>";

    public static final String TDAR_ORDER_PREFIX = "TDAROrder-";
    
    /**
     * Load an OWLOntology from an IRI
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
     * @param label_
     * @return
     */
    public static String labelToFragmentId(String label_) {
        if (StringUtils.isBlank(label_))
            return "";
        String label = label_.trim().replaceAll(IRI_INVALID_CHARACTERS_REGEX, "_");
        if (label.matches("^(\\d).*")) {
            label = "_" + label;
        }
        return label;
    }
    
    /**
     * Convert an @link InformationResourceFileVersion (to an OWLOntology)
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public OWLOntology toOwlOntology(InformationResourceFileVersion file) throws FileNotFoundException {
        File transientFile = TdarConfiguration.getInstance().getFilestore().retrieveFile(file);
        if (file != null && transientFile.exists()) {
            File tempFile = storeImportOrder(file);
            if (tempFile != null) {
                IRI iri = IRI.create(tempFile);
                return loadFromIRI(iri);
            } else {
                IRI iri = IRI.create(transientFile);
                return loadFromIRI(iri);
            }
            // IRI iri = IRI.create(file.getFile());
        }
        logger.debug("could not find ontology file for: {}", file);
        return null;

    }

    /**
     * Iterate through an OWLOntology and append the TDARImportOrder- prefixed rdfs:comment for each entry, this is used to properly render the @link Ontology later on
     * @param file
     * @return
     */
    private File storeImportOrder(InformationResourceFileVersion file) {
        File tempFile = null;
        Document ontologyXML = null;
        try {
            ontologyXML = DocumentHelper.parseText(FileUtils.readFileToString(TdarConfiguration.getInstance().getFilestore().retrieveFile(file)));
        } catch (Exception e) {
            logger.debug("cannot store import order:", e);

        }

        if (ontologyXML != null) {
            walkOntologyTree(ontologyXML.getRootElement(), 0);

            OutputFormat format = OutputFormat.createPrettyPrint();
            StringWriter stringWriter = new StringWriter();
            XMLWriter writer = new XMLWriter(stringWriter, format) {
                @Override
                protected String escapeAttributeEntities(String text) {
                    return text;
                }
            };
            try {
                writer.write(ontologyXML);
            } catch (IOException e) {
                logger.debug("cannot write ontology xml:", e);

            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    logger.debug("cannot close xml writer:", e);
                }
            }
            try {
                tempFile = File.createTempFile("temp", ".xml", TdarConfiguration.getInstance().getTempDirectory());
                FileUtils.writeStringToFile(tempFile, stringWriter.toString());
                logger.trace("{}", stringWriter);
            } catch (IOException e) {
                logger.debug("cannot write ontology xml to file:", e);
            }
        }
        return tempFile;
    }

    /**
     * Returns an OWL XML String, given a tab-separated 
     * FIXME: continue to refactor this.
     * 
     * @author qyan
     * @param inputString
     * @return
     */
    public String toOwlXml(Long ontologyId, String inputString) {
        if (StringUtils.isBlank(inputString))
            return "";
        String baseUrl = TdarConfiguration.getInstance().getBaseUrl();
        String startStr = String.format(ONTOLOGY_START_STRING_FORMAT, baseUrl, ontologyId, baseUrl, baseUrl, ontologyId);

        // XXX: rough guesstimate that XML verbosity will increase input string
        // length by a factor of 3.
        // Should test this empirically.
        StringBuilder builder = new StringBuilder(inputString.length() * 3);
        builder.append(startStr);

        ArrayList<String> parentList = new ArrayList<String>();
        ArrayList<String> nodeList = new ArrayList<String>();

        String line;
        BufferedReader reader = new BufferedReader(new StringReader(inputString));
        try {
            while ((line = reader.readLine()) != null) {
                if (StringUtils.isEmpty(line.trim()))
                    continue;
                int currentDepth = getNumberOfPrefixTabs(line);
                // remove tabs and replace all repeated non-word characters
                // ([a-zA-Z_0-9]) with single "_".
                // sanitized label for OWL use, and a description.
                // need to figure out how to store it into OWL XML.
                Matcher m = SYNONYM_PATTERN.matcher(line.trim());
                logger.trace(line);
                ArrayList<String> synonyms = new ArrayList<String>();
                StringBuilder synonymProperty = new StringBuilder();
                if (m.matches()) {
                    line = m.group(1);
                    // handle multiple synonyms
                    for (String synonym : m.group(2).split(SYNONYM_SPLIT_REGEX)) {
                        if (StringUtils.isBlank(synonym))
                            continue;
                        synonyms.add(synonym.trim());
                    }
                    for (String synonym : synonyms) {
                        synonymProperty.append(String.format(OWL_SAME_AS, labelToFragmentId(synonym)));
                    }
                }
                String displayLabel = line.trim();
                line = labelToFragmentId(line);
                nodeList.add(line);
                if (currentDepth == 0) {
                    // root node format style
                    builder.append(String.format(OWL_CLASS_FORMAT, line, displayLabel, synonymProperty, ""));
                    // clear out parent list since we are processing a new root node.
                    parentList.clear();
                } else {
                    // child node
                    int numberOfAvailableParents = parentList.size();
                    // current depth may be degenerate (i.e., current depth of 4 but most immediate parent is of depth 2).
                    int parentIndex = ((currentDepth > numberOfAvailableParents)
                            // degenerate depth, pick closest parent.
                            ? numberOfAvailableParents
                            // normal parent
                            : currentDepth)
                            // parent off-by-one
                            - 1;
                    if (parentIndex == -1) {
                        logger.error("Parent index was set to -1.  parentList: {}, line: {}. resetting parentIndex to 0", parentList, line);
                        parentIndex = 0;
                    } else if (parentIndex >= parentList.size()) {
                        logger.error("Parent index was exceeds parentList size.  parentList: {}. resetting parentIndex to 0", parentList, line);
                        parentIndex = 0;
                    }
                    String parentString = parentList.get(parentIndex);
                    // format style
                    builder.append(String.format(OWL_CLASS_FORMAT, line, displayLabel, synonymProperty, String.format(OWL_SUBCLASS_FORMAT, parentString)));
                }
                for (String synonym : synonyms) {
                    builder.append(String.format(OWL_CLASS_FORMAT, labelToFragmentId(synonym), synonym, String.format(OWL_SAME_AS, line), ""));
                }
                parentList.add(currentDepth, line);
            }
        } catch (IOException e) {
            throw new TdarRecoverableRuntimeException(MessageHelper.getMessage("owlOntologyConverter.error_parsing"));
        } finally {
            IOUtils.closeQuietly(reader);
        }

        testOntologyNodesUnique(nodeList);
        return builder.append(ONTOLOGY_END_STRING).toString();
    }

    /**
     * Tests that the List of OntologyNodes are unique.
     * @param nodeList
     */
    public void testOntologyNodesUnique(ArrayList<String> nodeList) {
        Set<String> uniqueSet = new HashSet<String>(nodeList);

        logger.debug("unique: {} incoming: {}", uniqueSet.size(), nodeList.size());
        if (nodeList.size() != uniqueSet.size()) {
            throw new TdarRecoverableRuntimeException(MessageHelper.getMessage("owlOntologyConverter.node_names_unique",nodeList.size(), uniqueSet.size()));
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

    /**
     * Walks an OWL Ontology tree and adds an "rdfs:comment" which stores the tDAR import order for the node based on a traversal of the tree
     * @param element
     * @param order
     */
    public void walkOntologyTree(Element element, int order) {
        for (int i = 0, size = element.nodeCount(); i < size; i++) {
            Node node = element.node(i);

            if (node instanceof Element) {
                Element nodeElement = (Element) node;
                if (nodeElement.getName().equalsIgnoreCase(CLASS)) {
                    Element orderNode = DocumentHelper.createElement(new QName(COMMENT, tdarNamespace));
                    orderNode.setText(TDAR_ORDER_PREFIX + String.valueOf(order));
                    order++;
                    nodeElement.add(orderNode);
                }

                walkOntologyTree((Element) node, order);
            }
        }
    }

}
