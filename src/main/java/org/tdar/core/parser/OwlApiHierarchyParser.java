package org.tdar.core.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.resource.ontology.OwlOntologyConverter;
import org.tdar.utils.MessageHelper;

/**
 * $Id$
 * 
 * Uses OWL-API to parse OWL ontologies (supposedly this should be done via
 * Reasoners but haven't gotten that far into OWL yet).
 * 
 * Generates a Map of OWLClass-s to OntologyNode-s.
 * 
 * Use one parser instance per ontology - this class needs to maintain state as
 * it recursively generates ontology node labels.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

public class OwlApiHierarchyParser implements OntologyParser {

    private final OWLOntology owlOntology;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
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
        for (OWLClass owlClass : allClasses) {
            if (owlClass.getSuperClasses(owlOntology).isEmpty()) {
                rootClasses.add(owlClass);
            }
            SortedSet<OWLClass> subclasses = new TreeSet<OWLClass>();
            owlHierarchyMap.put(owlClass, subclasses);
            for (OWLClassExpression subclassDescription : owlClass.getSubClasses(owlOntology)) {
                OWLClass subclass = subclassDescription.asOWLClass();
                subclasses.add(subclass);
            }
        }
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

    public List<OntologyNode> generate() {
        int startIndex = 1;

        for (OWLClass rootClass : rootClasses) {
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

    private int generateIntervalLabels(OWLClass owlClass, int index) {
        Set<String> synonymLabels = new HashSet<String>();
        OntologyNode node = new OntologyNode();
        node.setOntology(ontology);
        IRI iri = owlClass.getIRI();
        node.setImportOrder(extractImportOrder(owlClass));
        node.setIri(iri.getFragment());
        String uri_string = iri.toURI().toString();
        //FIXME: the OWL API does not appear to support IRIs that start with numbers... 
        // https://github.com/owlcs/owlapi/wiki/Documentation
        // this is a workaround

        // this is a backup for parsing older ontologies that have degenerate IRIs eg. those with  () in them
        if (StringUtils.isBlank(node.getIri()) && StringUtils.indexOf(uri_string, "#")> 0){
//            logger.info(iri);
//            logger.info(owlClass);
          node.setIri(StringUtils.substring(uri_string, uri_string.indexOf("#")+1));
        logger.info(uri_string);
        }
        String displayName = extractNodeLabel(owlClass);
        if (allSynonymLabels.contains(displayName))
            return index;
        for (OWLClassExpression equiv : owlClass.getEquivalentClasses(owlOntology)) {
            // making the assumption that we see the "real" node before we see
            // the synonyms
            for (OWLClass clas : equiv.getClassesInSignature()) {
                synonymLabels.add(extractNodeLabel(clas));
                logger.trace(clas.getIRI().getFragment() + " - " + iri.getFragment());
            }
        }
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
        for (OWLClassExpression owlClassExpression : owlClass.getSuperClasses(owlOntology)) {
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
            throw new TdarRecoverableRuntimeException(MessageHelper.getMessage("owlApiHierarchyParser.blank_iri", node));
        }
        node.setIntervalEnd(Integer.valueOf(index));
        return index + 1;
    }

    private String extractNodeLabel(OWLClass owlClass) {
        String txt = "";

        for (OWLAnnotation ann : owlClass.getAnnotations(owlOntology)) {
            if (ann.getProperty().isLabel()) {
                String annTxt = ann.getValue().toString();
                annTxt = annTxt.replaceAll("^\"", "");
                txt = annTxt.replaceAll("\"$", "");
            }
        }
        return txt;
    }

    private Long extractImportOrder(OWLClass owlClass) {
        String txt = "";
        for (OWLAnnotation ann : owlClass.getAnnotations(owlOntology)) {
            if (ann.getProperty().isComment()) {
                logger.trace("{}", ann.getValue());
                String annTxt = ann.getValue().toString();
                annTxt = StringUtils.replace(annTxt, "\"", ""); // owl parser
                                                                // adds quotes

                if (annTxt.startsWith(OwlOntologyConverter.TDAR_ORDER_PREFIX)) {
                    txt = annTxt.substring(OwlOntologyConverter.TDAR_ORDER_PREFIX.length());
                }
            }
        }
        logger.trace(txt);
        if (!StringUtils.isBlank(txt) && StringUtils.isNumeric(txt)) {
            return Long.parseLong(txt);
        } else {
            return -1L;
        }
    }

}
