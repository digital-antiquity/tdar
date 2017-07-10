package org.tdar.core.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.resource.ontology.OwlOntologyConverter;

/**
 * Uses OWL-API to parse OWL ontologies into a Map<OWLClass, OntologyNode>.
 * 
 * This class is not thread-safe, use one parser instance per ontology. It maintains state as it recursively generates ontology node labels.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 */
public class OwlApiHierarchyParser implements OntologyParser {

    private final OWLOntology owlOntology;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<OWLClass> rootClasses = new ArrayList<OWLClass>();
    private final HashMap<OWLClass, Set<OWLClass>> owlHierarchyMap = new HashMap<OWLClass, Set<OWLClass>>();
    private final HashMap<OWLClass, OntologyNode> classNodeMap = new HashMap<OWLClass, OntologyNode>();
    private Set<String> allSynonymLabels = new HashSet<String>();
    private Ontology ontology;

    public OwlApiHierarchyParser(Ontology ontology, OWLOntology owlOntology) {
        this(owlOntology);
        this.ontology = ontology;
    }

    public OwlApiHierarchyParser(final OWLOntology owlOntology) {
        this.owlOntology = owlOntology;
        List<OWLClass> allClasses = new ArrayList<OWLClass>(owlOntology.getClassesInSignature());
        sortOwlClassCollection(owlOntology, allClasses);
        for (OWLClass owlClass_ : allClasses) {
            OWLClass owlClass = resolveSynonyms(owlClass_, allClasses);
            if (isClassOrEquivalentRoot(owlClass, owlOntology)) {
                rootClasses.add(owlClass);
            }
            SortedSet<OWLClass> subclasses = new TreeSet<OWLClass>();
            owlHierarchyMap.put(owlClass, subclasses);
            EntitySearcher.getSubClasses(owlClass, owlOntology).forEach(subclassDescription -> {
                OWLClass subclass = subclassDescription.asOWLClass();
                subclasses.add(subclass);
            });
        }
    }

    private OWLClass resolveSynonyms(OWLClass owlClass, List<OWLClass> allClasses) {
        if (logger.isTraceEnabled()) {
            logger.trace(" > {} ({})" , owlClass, allClasses);
        }
        String ann = extractAnnotation(owlClass, OwlOntologyConverter.TDAR_NODE_ENTRY);
        // NULL if not there, blank if there (because of "starts-with" trim)
        if (ann != null) {
            return owlClass;
        }
        //TDARNode
        OWLClassExpression oc  = null;
        try {
            oc = EntitySearcher.getEquivalentClasses(owlClass, owlOntology).filter(eq -> 
            (extractAnnotation(eq.asOWLClass(), OwlOntologyConverter.TDAR_NODE_ENTRY) != null))
        .findFirst().get();
        } catch (NoSuchElementException nse ) {
            logger.error("node should never be null when trying to find synonyms: {}", owlClass);
            return owlClass;
        }
        return oc.asOWLClass();
    }

    private boolean isClassOrEquivalentRoot(OWLClass owlClass, OWLOntology owlOntology2) {
        long count = EntitySearcher.getSuperClasses(owlClass, owlOntology).count();
        if (count > 0) {
            if (logger.isTraceEnabled()) {
                logger.trace("({}) not empty (direct)", owlClass);
            }
            return false;
        }

        return true;
    }

    public void sortOwlClassCollection(final OWLOntology owlOntology, List<OWLClass> allClasses) {
        Collections.sort(allClasses, new Comparator<OWLClass>() {

            @Override
            public int compare(OWLClass o1, OWLClass o2) {
                Long order1 = extractImportOrder(o1);
                Long order2 = extractImportOrder(o2);
                return order1.compareTo(order2);
            }
        });
    }

    public List<OWLClass> getRootClasses() {
        return rootClasses;
    }

    public Map<OWLClass, Set<OWLClass>> getOwlHierarchyMap() {
        return owlHierarchyMap;
    }

    public Map<OWLClass, OntologyNode> getOwlClassNodeMap() {
        return classNodeMap;
    }

    public OWLOntology getOwlOntology() {
        return owlOntology;
    }

    public Ontology getOntology() {
        return ontology;
    }

    @Override
    public List<OntologyNode> generate() {
        int startIndex = 1;
        for (OWLClass rootClass : rootClasses) {
            logger.debug("ROOT CLASSE: {}", rootClass);
            startIndex = generateIntervalLabels(rootClass, startIndex);
        }
        return new ArrayList<OntologyNode>(classNodeMap.values());
    }

    /**
     * Recursively generates interval labels using PrePost encoding for nodes in
     * this ontology. Also sets up a dewey index encoding the structure of the
     * ontology.
     * 
     * FIXME: weird logic required to ensure that subsequent siblings don't get
     * off-by-one ++ as the recursion bubbles back up. Should refactor/clean up
     * at some point in the not-too-distant future.
     * 
     * @param owlClass
     * @param index
     * @return an integer representing the interval end for the given owl node.
     */

    @SuppressWarnings("deprecation")
    private int generateIntervalLabels(OWLClass owlClass, int index_) {
        int index = index_;
        Set<String> synonymLabels = new HashSet<String>();
        OntologyNode node = new OntologyNode();
        node.setOntology(ontology);
        IRI iri = owlClass.getIRI();
        node.setImportOrder(extractImportOrder(owlClass));
        node.setDescription(extractDescription(owlClass));
        node.setIri(iri.getFragment());
        String uri_string = iri.toURI().toString();
        // FIXME: the OWL API does not appear to support IRIs that start with numbers...
        // https://github.com/owlcs/owlapi/wiki/Documentation
        // this is a workaround

        // this is a backup for parsing older ontologies that have degenerate IRIs eg. those with () in them
        logger.trace("node: {}", node);
        if (StringUtils.isBlank(node.getIri()) && (StringUtils.indexOf(uri_string, "#") > 0)) {
            node.setIri(StringUtils.substring(uri_string, uri_string.indexOf("#") + 1));
            logger.info(uri_string);
        }
        String displayName = extractNodeLabel(owlClass);
        if (allSynonymLabels.contains(displayName)) {
            logger.trace("skipping: {}", displayName);
            return index;
        }

        EntitySearcher.getEquivalentClasses(owlClass, owlOntology).forEach(equiv -> {
            // making the assumption that we see the "real" node before we see the synonyms
            for (OWLClass syn : equiv.getClassesInSignature()) {
                String label = extractNodeLabel(syn);
                synonymLabels.add(label);
                logger.trace("{} - {} [{}]", syn.getIRI().getFragment(), iri.getFragment(), label);
            }
        });
        node.setSynonyms(synonymLabels);
        allSynonymLabels.addAll(synonymLabels);
        if (!StringUtils.isBlank(displayName)) {
            node.setDisplayName(displayName);
        } else {
            node.setDisplayName(iri.getFragment());
        }
        node.setUri(uri_string);
        node.setIntervalStart(Integer.valueOf(index));
        String indexString = String.valueOf(index);
        Iterator<OWLClassExpression> iterator = EntitySearcher.getSuperClasses(owlClass, owlOntology).iterator();
        
        while (iterator.hasNext()) {
            OWLClassExpression owlClassExpression = iterator.next();
            OWLClass parentClass = owlClassExpression.asOWLClass();
            OntologyNode parentNode = classNodeMap.get(parentClass);
            // append parent indices recursively
            if (parentNode != null) {
                indexString = parentNode.getIndex() + "." + indexString;
            }
        }
        node.setIndex(indexString);

        classNodeMap.put(owlClass, node);
        // FIXME: first sibling hack to make sure siblings don't get off-by-one.
        // should be fixable in the recursion
        boolean firstSibling = true;
        List<OWLClass> siblings = new ArrayList<OWLClass>(owlHierarchyMap.get(owlClass));
        sortOwlClassCollection(owlOntology, siblings);
        for (OWLClass childClass : siblings) {
            index = generateIntervalLabels(childClass, (firstSibling) ? index + 1 : index);
            firstSibling = false;
        }
        logger.trace("{}", node);

        if (StringUtils.isBlank(node.getIri())) {
            List<Object> vals = new ArrayList<>();
            vals.add(node);
            throw new TdarRecoverableRuntimeException("owlApiHierarchyParser.blank_iri", vals);
        }
        node.setIntervalEnd(Integer.valueOf(index));
        return index + 1;
    }

    private String extractNodeLabel(OWLClass owlClass) {
        String txt = "";
        Iterator<OWLAnnotation> iterator = EntitySearcher.getAnnotations(owlClass, owlOntology).iterator();
        while (iterator.hasNext()) {
            OWLAnnotation ann = iterator.next();
            if (ann.getProperty().isLabel()) {
                txt = extractStringValue(ann);
            }
        }
        return txt;
    }

    private String extractStringValue(OWLAnnotation ann) {
        String txt;
        OWLAnnotationValue value = ann.getValue();
        String annTxt = value.toString();
        if (value instanceof OWLLiteral) {
            annTxt = ((OWLLiteral) value).getLiteral();
        }
        
        annTxt = annTxt.replaceAll("^\"", "");
        txt = annTxt.replaceAll("\"$", "");
        return txt;
    }

    private Long extractImportOrder(OWLClass owlClass) {
        String txt = extractAnnotation(owlClass, OwlOntologyConverter.TDAR_ORDER_PREFIX);
        logger.trace(txt);
        if (!StringUtils.isBlank(txt) && StringUtils.isNumeric(txt)) {
            return Long.parseLong(txt);
        } else {
            return -1L;
        }
    }

    private String extractDescription(OWLClass owlClass) {
        String txt = extractAnnotation(owlClass, OwlOntologyConverter.TDAR_DESCRIPTION_PREFIX);
        logger.trace(txt);
        return txt;
    }

    private String extractAnnotation(OWLClass owlClass, String key) {
        Iterator<OWLAnnotation> iterator = EntitySearcher.getAnnotations(owlClass, owlOntology).iterator();
        while (iterator.hasNext()) {
            OWLAnnotation ann = iterator.next();
            if (ann.getProperty().isComment()) {
                logger.trace("{}", ann.getValue());
                String annTxt = extractStringValue(ann);
                if (annTxt.startsWith(key)) {
                    return annTxt.substring(key.length());
                }
            }
        }
        return null;
    }

}
