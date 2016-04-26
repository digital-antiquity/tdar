package org.tdar.core.service.resource.ontology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.utils.Pair;

/*
 * Given a set of @link CodingRule entries and @link OntologyNode entries, provide suggestions for an autocomplete about likely matches of CodingRule values and OntologyNode labels
 */
public class OntologyNodeSuggestionGenerator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private int defaultAcceptableEditDistance = 4;

    /*
     * The defaultAcceptableEditDistance is used to help act as a sanity check for the levenshtein distance. If the column is too small, the edit difference
     * helps ensure that crazy suggestions are not suggested when tDAR tries to offer suggseted matches for Ontology values
     */
    public int getDefaultAcceptableEditDistance() {
        return defaultAcceptableEditDistance;
    }

    /**
     * Setup a Map of @link CodingRule values and @link OntologyNode entries based upon the likely matching OntologyNode entries for each unique value. This
     * uses a Levenshtein similarity calculation to assist in more brute force (exact / contains) matching.
     * 
     * @param codingRules
     * @param ontologyNodes
     * @return
     */
    public SortedMap<String, List<OntologyNode>> applySuggestions(Collection<CodingRule> codingRules, List<OntologyNode> ontologyNodes) {
        TreeMap<String, List<OntologyNode>> suggestions = new TreeMap<>(new Comparator<String>() {
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
                logger.warn("found blank column value while generating suggestions, shouldn't happen");
                continue;
            }
            List<Pair<OntologyNode, Integer>> rankedOntologyNodes = new ArrayList<Pair<OntologyNode, Integer>>();
            String normalizedColumnValue = normalize(codingRule.getTerm());
            for (OntologyNode ontologyNode : ontologyNodes) {
                String displayName = ontologyNode.getDisplayName();
                if (StringUtils.isBlank(displayName) && TdarConfiguration.getInstance().isProductionEnvironment()) {
                    logger.warn("blank ontology node display name for node {}, shouldn't happen", ontologyNode.getId());
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
            logger.trace("{} {}", codingRule, codingRule.getSuggestions());
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
    public String normalize(String value) {
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

    /**
     * Calculate the actual similarity of the @link CodingRule value and the @link OntologyNode label.
     * 
     * @param columnValue
     * @param ontologyLabel
     * @return
     */
    protected int calculateSimilarity(String columnValue, String ontologyLabel) {
        if (ontologyLabel.contains(columnValue) || columnValue.contains(ontologyLabel)) {
            return 1;
        }
        int levenshteinDistance = StringUtils.getLevenshteinDistance(columnValue, ontologyLabel);
        int lengthDifference = Math.abs(columnValue.length() - ontologyLabel.length());
        logger.trace("distance [" + columnValue + "]->[" + ontologyLabel + "]=" + levenshteinDistance + " : " + lengthDifference);
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

    /**
     * Calculate the actual similarity of the @link CodingRule value and the @link OntologyNode label. Returns true if there's any similarity (not -1)
     * 
     * @param columnValue
     * @param ontologyLabel
     * @return
     */
    public boolean isSimilarEnough(String columnValue, String ontologyLabel) {
        return calculateSimilarity(columnValue, ontologyLabel) != -1;
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

}
