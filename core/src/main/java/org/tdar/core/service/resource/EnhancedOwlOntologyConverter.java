package org.tdar.core.service.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.core.service.FreemarkerService;
import org.tdar.exception.TdarRecoverableRuntimeException;
import org.tdar.parser.OwlOntologyConverter;
import org.tdar.parser.TOntologyNode;

public class EnhancedOwlOntologyConverter extends OwlOntologyConverter {

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
        List<TOntologyNode> nodes = new ArrayList<>();
        Map<String, List<TOntologyNode>> uniqueIriSet = new HashMap<>();
        List<TOntologyNode> parentNodes = new ArrayList<>();
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
                Set<TOntologyNode> synonymNodes = new HashSet<>();
                if (m.matches()) {
                    line = m.group(1);
                    // handle multiple synonyms
                    for (String synonym : m.group(2).split(SYNONYM_SPLIT_REGEX)) {
                        if (StringUtils.isBlank(synonym)) {
                            continue;
                        }
                        TOntologyNode synonymNode = new TOntologyNode(labelToFragmentId(synonym), synonym.trim());
                        synonymNodes.add(synonymNode);
                    }
                }
                TOntologyNode currentNode = new TOntologyNode(labelToFragmentId(line), line.trim());
                currentNode.setSynonymNodes(synonymNodes);
                currentNode.setDescription(description);
                addToDuplicateCheck(uniqueIriSet, currentNode);
                for (TOntologyNode synonym : synonymNodes) {
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

}
