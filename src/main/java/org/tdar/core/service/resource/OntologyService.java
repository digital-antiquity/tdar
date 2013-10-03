package org.tdar.core.service.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.resource.OntologyDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.exception.TdarRuntimeException;
import org.tdar.core.parser.OwlApiHierarchyParser;
import org.tdar.utils.Pair;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Transactional service providing persistence access to OntologyS as well as
 * OWL access to Ontology files.
 * 
 * @author Allen Lee
 * @version $Revision$
 * @latest $Id$
 */
@Service
@Transactional
public class OntologyService extends AbstractInformationResourceService<Ontology, OntologyDao> {

    // FIXME: allow all valid characters for ifragments in http://www.ietf.org/rfc/rfc3987.txt ?
    // some of these throw errors out from the OWLParser, (e.g., / and ?)
    // IRI = scheme ":" ihier-part [ "?" iquery ] [ "#" ifragment ]
    // ipchar = iunreserved / pct-encoded / sub-delims / ":"
    // iunreserved = ALPHA / DIGIT / "-" / "." / "_" / "~" / ucschar
    // ifragment = *( ipchar / "/" / "?" )
    // regex that matches if String contains one or more characters invalid in an IRI
    public final static String IRI_INVALID_CHARACTERS_REGEX = "[^\\w~.-]";
    public final static Pattern TAB_PREFIX_PATTERN = Pattern.compile("^(\\t+).*$");
    public static final String SYNONYM_SPLIT_REGEX = ";";
    public final static Pattern SYNONYM_PATTERN = Pattern.compile("^(.+)[\\(\\[](.+)[\\)\\]](.*)$");

    public final static String ONTOLOGY_START_STRING_FORMAT = "<?xml version=\"1.0\"?>\n" + "<rdf:RDF\n"
            + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n"
            + "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" + "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n"
            + "xmlns=\"http://www.tdar.org/ontologies/%d#\"\n" + "xmlns:tdar=\"http://www.tdar.org/ontologies#\"\n"
            + "xml:base=\"http://www.tdar.org/ontologies/%d\">\n" + "<owl:Ontology rdf:about=\"\"/>\n";

    private final static String OWL_CLASS_FORMAT = " <owl:Class rdf:ID=\"%s\">\n\t<rdfs:label><![CDATA[%s]]></rdfs:label>\n%s%s\n</owl:Class>\n";

    private final static String OWL_SAME_AS = "<owl:equivalentClass rdf:resource=\"#%s\"/>\n";

    // subclass format will be embedded into the above OWL_CLASS_FORMAT (if
    // necessary)
    private final static String OWL_SUBCLASS_FORMAT = "<rdfs:subClassOf rdf:resource=\"#%s\"/>\n";

    private final static String ONTOLOGY_END_STRING = "</rdf:RDF>";
    // private final static String NORMALIZE_REGEX =
    // "((\\d+)(st|rd|nd|th)|(first|second|third|fourth|fifth|sixth)|([ivlxcdm]+)|([\\(|\\[|\\{].+)[\\)|\\]|\\}]|or|and|\\\\)|(\\.|,|\\:)";

    public static final String TDAR_ORDER_PREFIX = "TDAROrder-";

    private int defaultAcceptableEditDistance = 4;

    private OWLOntologyManager owlOntologyManager = OWLManager.createOWLOntologyManager();

    public int getDefaultAcceptableEditDistance() {
        return defaultAcceptableEditDistance;
    }

    public void setDefaultAcceptableEditDistance(int acceptableEditDistance) {
        this.defaultAcceptableEditDistance = acceptableEditDistance;
    }

    public List<Ontology> findSparseOntologyList() {
        return getDao().findSparseResourceBySubmitterType(null, ResourceType.ONTOLOGY);
    }

    public void testOntologyNodesUnique(ArrayList<String> nodeList) {
        Set<String> uniqueSet = new HashSet<String>(nodeList);

        logger.debug("unique: {} incoming: {}", uniqueSet.size(), nodeList.size());
        if (nodeList.size() != uniqueSet.size()) {
            throw new TdarRecoverableRuntimeException(String.format("Ontology node names must be unique, %s incoming %s unique names",
                    nodeList.size(), uniqueSet.size()));
        }

    }

    /**
     * Parses the OWL file associated with the given Ontology and indexes them
     * in the database.
     * 
     * @param ontology
     */
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
                parser = new OwlApiHierarchyParser(ontology, toOwlOntology(latestUploadedFile));
            } catch (FileNotFoundException e) {
                logger.warn("file not found: {}", e);
                throw new TdarRecoverableRuntimeException(String.format("file not found %s", latestUploadedFile.getFilename()), e);
            }
            List<OntologyNode> incomingOntologyNodes = parser.generate();
            getLogger().debug("created {} ontology nodes from {}", incomingOntologyNodes.size(), latestUploadedFile.getFilename());
            // start reconciliation process
            if (numberOfMappedValues > 0) {
                getLogger().debug("had mapped values, reconciling {} with {}", existingOntologyNodes, incomingOntologyNodes);
                reconcile(existingOntologyNodes, incomingOntologyNodes);
            }
            getDao().removeReferencesToOntologyNodes(existingOntologyNodes);
            getDao().delete(existingOntologyNodes);
            getDao().saveOrUpdate(incomingOntologyNodes);
            logger.debug("existing ontology nodes: {}", ontology.getOntologyNodes());
            ontology.getOntologyNodes().addAll(incomingOntologyNodes);
            // ontology.setOntologyNodes(incomingOntologyNodes);
            owlOntologyManager.removeOntology(parser.getOwlOntology());
        }
    }

    /**
     * Modifies both lists of OntologyNodeS in place. After returning, incomingOntologyNodes
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
                existing = synonymsSet.get(incoming.getDisplayName());
            }

            if (existing == null) {
                continue;
            }
            Long original = existing.getId();
            Long in = incoming.getId();
            incoming = getDao().merge(incoming, existing);
            // incoming.setId(existing.getId());
            // getDao().detachFromSession(existing);
            // incoming = getDao().merge(incoming);
            getLogger().trace(in + " " + incoming.getDisplayName() + " -> " + incoming.getId() +
                    " <--> e:" + existing.getId() + " " + existing.getDisplayName() +
                    " ->" + original);
            incomingOntologyNodes.set(index, incoming);
            existingOntologyNodes.remove(existing);
            existingSet.remove(existing.getIri());
            synonymsSet.remove(existing.getDisplayName());
            for (String synonym : existing.getSynonyms()) {
                synonymsSet.remove(synonym);
            }
            // existingOntologyNodes.set(existingIndex, null);
            // }
            // for (int existingIndex = 0; existingIndex < existingOntologyNodes.size(); existingIndex++) {
            // OntologyNode existing = existingOntologyNodes.get(existingIndex);
            // if (existing == null)
            // continue;
            // getLogger().trace("checking {} | {} ", existing.getDisplayName(), incoming.getDisplayName());
            // if (incoming.isEquivalentTo(existing)) {
            // Long original = existing.getId();
            // Long in = incoming.getId();
            // incoming = getDao().merge(incoming, existing);
            // // incoming.setId(existing.getId());
            // // getDao().detachFromSession(existing);
            // // incoming = getDao().merge(incoming);
            // getLogger().trace(in + " " + incoming.getDisplayName() + " -> " + incoming.getId() +
            // " <--> e:" + existing.getId() + " " + existing.getDisplayName() +
            // " ->" + original);
            // incomingOntologyNodes.set(index, incoming);
            // existingOntologyNodes.set(existingIndex, null);
            // }
            // }
        }
        existingOntologyNodes.removeAll(Collections.singleton(null));
        getLogger().debug("existing ontology nodes: {}", existingOntologyNodes);
        getLogger().debug("incoming ontology nodes: {}", incomingOntologyNodes);
    }

    public void walkOntologyTree(Element element, int order) {
        // FIXME: when we start dealing with more complex ontologies, we may
        // need to introduce
        // our own parsing technique beyond just getting the 1st annotation

        Namespace tdarNamespace = new Namespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        for (int i = 0, size = element.nodeCount(); i < size; i++) {
            Node node = element.node(i);

            if (node instanceof Element) {
                Element nodeElement = (Element) node;
                if (nodeElement.getName().equalsIgnoreCase("class")) {
                    Element orderNode = DocumentHelper.createElement(new QName("comment", tdarNamespace));
                    orderNode.setText(TDAR_ORDER_PREFIX + String.valueOf(order));
                    order++;
                    nodeElement.add(orderNode);
                }

                walkOntologyTree((Element) node, order);
            }
        }
    }

    public OntModel toOntModel(Ontology ontology) throws FileNotFoundException {
        Collection<InformationResourceFileVersion> files = ontology.getLatestVersions();
        if (files.size() != 1) {
            throw new TdarRecoverableRuntimeException("expected only one IRFileVersion, but found: " + files.size());
        }
        for (InformationResourceFileVersion irFile : files) {
            File file = TdarConfiguration.getInstance().getFilestore().retrieveFile(irFile);
            if (file.exists()) {
                OntModel ontologyModel = ModelFactory.createOntologyModel();
                String url = ontology.getUrl();
                if (url == null)
                    url = "";
                try {
                    ontologyModel.read(new FileReader(file), url);
                    return ontologyModel;
                } catch (FileNotFoundException exception) {
                    // this should never happen since we're explicitly checking file.exists()...
                    throw new RuntimeException(exception);
                }
            }
        }
        return null;
    }

    /**
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
                getLogger().trace("{}", stringWriter);
            } catch (IOException e) {
                logger.debug("cannot write ontology xml to file:", e);
            }
        }
        return tempFile;
    }

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
        getLogger().debug("could not find ontology file for: {}", file);
        return null;

    }

    protected OWLOntology loadFromIRI(IRI iri) {
        try {
            return owlOntologyManager.loadOntologyFromOntologyDocument(iri);
        } catch (UnparsableOntologyException exception) {
            getLogger().error("Couldn't parse ontology from iri " + iri, exception);
            throw new TdarRuntimeException(exception);
        } catch (OWLOntologyCreationException exception) {
            getLogger().debug("Ontology already exists in manager, attempt to get it instead", exception);
            return owlOntologyManager.getOntology(iri);
        }

    }

    public String labelToFragmentId(String label_) {
        if (StringUtils.isBlank(label_))
            return "";
        String label = label_.trim().replaceAll(IRI_INVALID_CHARACTERS_REGEX, "_");
        if (label.matches("^(\\d).*")) {
            label = "_" + label;
        }
        return label;
    }

    /**
     * Returns the number of tabs at the beginning of this string.
     * 
     * @param line
     * @return
     */
    public int getNumberOfPrefixTabs(String line) {
        // regex solution
        Matcher matcher = TAB_PREFIX_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1).length();
        }
        return 0;
    }

    /**
     * Returns an OWL XML String, given a tab-separated FIXME: continue to
     * refactor this.
     * 
     * @author qyan
     * @param inputString
     * @return
     */
    public String toOwlXml(Long ontologyId, String inputString) {
        if (StringUtils.isBlank(inputString))
            return "";

        String startStr = String.format(ONTOLOGY_START_STRING_FORMAT, ontologyId, ontologyId);

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
                getLogger().trace(line);
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
                    // root node
                    // format style
                    builder.append(String.format(OWL_CLASS_FORMAT, line, displayLabel, synonymProperty, ""));
                    // builder.append(OWL_CLASS_START_TAG).append(line).append(OWL_CLASS_START_TAG_CLOSE).append(OWL_CLASS_CLOSE);
                    // clear out parent list since we are processing a new root
                    // node.
                    parentList.clear();
                } else {
                    // child node
                    int numberOfAvailableParents = parentList.size();
                    // current depth may be degenerate (i.e., current depth of 4 but
                    // most immediate parent is of depth 2).
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
            throw new TdarRecoverableRuntimeException("there was an error processing your ontology");
        } finally {
            IOUtils.closeQuietly(reader);
        }

        testOntologyNodesUnique(nodeList);
        return builder.append(ONTOLOGY_END_STRING).toString();
    }

    /**
     * Returns a sorted mapping between strings and lists of ontology nodes with
     * labels similar to the string key.
     * 
     * @param ontologyNodes
     *            the ontology nodes to be used in the mapping
     * @param codingRules
     * @return
     */
    private static final Comparator<Pair<OntologyNode, Integer>> ONTOLOGY_NODE_COMPARATOR = new Comparator<Pair<OntologyNode, Integer>>() {
        @Override
        public int compare(Pair<OntologyNode, Integer> a, Pair<OntologyNode, Integer> b) {
            // Do not use a case insensitive sort here as this will lose elements (Tibia + tibia -> tibia)
            int comparison = a.getSecond().compareTo(b.getSecond());
            if (comparison == 0) {
                return a.getFirst().getDisplayName().compareTo(b.getFirst().getDisplayName());
            }
            return comparison;
        }
    };

    public SortedMap<String, List<OntologyNode>> applySuggestions(Collection<CodingRule> codingRules, List<OntologyNode> ontologyNodes) {
        // FIXME: need to figure out if we are going to place the suggestions on the coding rules only or populate the returned Map as well.
        TreeMap<String, List<OntologyNode>> suggestions = new TreeMap<String, List<OntologyNode>>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1.equalsIgnoreCase(o2)) {
                    // when two strings are equalsIgnoreCase, we want lowercase before uppercase, so
                    // reverse the comparison
                    return o2.compareTo(o1);
                }
                return String.CASE_INSENSITIVE_ORDER.compare(o1, o2);
            }
        });

        for (CodingRule codingRule : codingRules) {
            if (StringUtils.isBlank(codingRule.getTerm())) {
                getLogger().warn("found blank column value while generating suggestions, shouldn't happen");
                continue;
            }
            List<Pair<OntologyNode, Integer>> rankedOntologyNodes = new ArrayList<Pair<OntologyNode, Integer>>();
            String normalizedColumnValue = normalize(codingRule.getTerm());
            for (OntologyNode ontologyNode : ontologyNodes) {
                String displayName = ontologyNode.getDisplayName();
                if (StringUtils.isBlank(displayName)) {
                    getLogger().warn("blank ontology node display name for node {}, shouldn't happen", ontologyNode.getId());
                    continue;
                }
                displayName = normalize(displayName);
                // special case for exact matches
                if (normalizedColumnValue.equals(displayName)) {
                    rankedOntologyNodes.clear();
                    rankedOntologyNodes.add(Pair.create(ontologyNode, 0));
                    break;
                }
                int similarity = calculateSimilarity(normalizedColumnValue, displayName);
                if (similarity != -1) {
                    rankedOntologyNodes.add(Pair.create(ontologyNode, similarity));
                } else {
                    // check synonym similarities
                    for (String synonym : ontologyNode.getSynonyms()) {
                        synonym = normalize(synonym);
                        if (normalizedColumnValue.equals(synonym)) {
                            rankedOntologyNodes.clear();
                            rankedOntologyNodes.add(Pair.create(ontologyNode, 0));
                            break;
                        }
                        similarity = calculateSimilarity(normalizedColumnValue, synonym);
                        if (similarity != -1) {
                            rankedOntologyNodes.add(Pair.create(ontologyNode, similarity));
                        }
                    }
                }
            }
            Collections.sort(rankedOntologyNodes, ONTOLOGY_NODE_COMPARATOR);
            // FIXME: case sensitivity change above
            codingRule.setSuggestions(Pair.allFirsts(rankedOntologyNodes));
            suggestions.put(codingRule.getTerm(), codingRule.getSuggestions());
            getLogger().trace("{} {}", codingRule, codingRule.getSuggestions());
        }
        return suggestions;
    }

    /*
     * The Normalize Regex looks at the following cases: Roman Numerals, 1st,
     * 2nd, First, Second, (paranthetical statments) and strips them, only if
     * they don't represent the entire values, values are also converted to
     * lowercase and trimmed at the beginning and end of the process. The
     * following dataset was used for a bunch of examples:
     * http://beta.tdar.org/dataset/column-ontology?resourceId=3411 Columns
     * (Element, Species, and Gnaw)
     */
    protected String normalize(String value) {
        return value.toLowerCase().trim();
        // String ret = value.toLowerCase().trim();
        // ret = ret.replaceAll("_", " ");
        // if (!ret.matches("^" + NORMALIZE_REGEX + "$")) {
        // ret = ret.replaceAll("^" + NORMALIZE_REGEX, "");
        // ret = ret.replaceAll(NORMALIZE_REGEX + "$", "");
        // ret = ret.replaceAll("\\s" + NORMALIZE_REGEX + "\\s", "");
        // }
        // return ret.trim();
    }

    // FIXME: if needed elsewhere, extract or lift to appropriate parent or
    // utility service class.
    // FIXME: needs to return more information than boolean if we want to
    // provide rankings
    protected int calculateSimilarity(String columnValue, String ontologyLabel) {
        if (ontologyLabel.contains(columnValue) || columnValue.contains(ontologyLabel)) {
            return 1;
        }
        int levenshteinDistance = StringUtils.getLevenshteinDistance(columnValue, ontologyLabel);
        int lengthDifference = Math.abs(columnValue.length() - ontologyLabel.length());
        getLogger().trace("distance [" + columnValue + "]->[" + ontologyLabel + "]=" + levenshteinDistance + " : " + lengthDifference);
        // take into account the actual length of the string, if it < acceptableEditDistance we should adjust acceptableEditDistance
        // accordingly
        int acceptableEditDistance = defaultAcceptableEditDistance;
        if (columnValue.length() < defaultAcceptableEditDistance) {
            // FIXME: adjust for size 3 if needed
            acceptableEditDistance = Math.max(columnValue.length() - 2, 0);
        }
        if (levenshteinDistance <= acceptableEditDistance) {
            return levenshteinDistance;
        } else {
            return -1;
        }
    }

    protected boolean isSimilarEnough(String columnValue, String ontologyLabel) {
        return calculateSimilarity(columnValue, ontologyLabel) != -1;
    }

    public List<OntologyNode> getChildren(List<OntologyNode> allNodes, OntologyNode parent) {
        List<OntologyNode> toReturn = new ArrayList<OntologyNode>();
        for (OntologyNode currentNode : allNodes) {
            if (currentNode.getIndex().equals(parent.getIndex() + "." + currentNode.getIntervalStart()))
                toReturn.add(currentNode);
        }
        getLogger().trace("returning: {}", toReturn);
        return toReturn;
    }

    public List<OntologyNode> getRootElements(List<OntologyNode> allNodes) {
        List<OntologyNode> toReturn = new ArrayList<OntologyNode>();
        for (OntologyNode currentNode : allNodes) {
            if (currentNode.getIndex().indexOf(".") == -1)
                toReturn.add(currentNode);
        }
        return toReturn;
    }

    public int getNumberOfMappedDataValues(DataTableColumn dataTableColumn) {
        return getDao().getNumberOfMappedDataValues(dataTableColumn);
    }

    public boolean isOntologyMapped(DataTableColumn dataTableColumn) {
        return getDao().getNumberOfMappedDataValues(dataTableColumn) > 0;
    }

}
