/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.Sequenceable;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;

/**
 * ProxyClass to help with passing integration data back and forth from Struts
 * 
 * @author Adam Brin
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class IntegrationColumn implements Serializable, Sequenceable<IntegrationColumn> {

    private static final long serialVersionUID = -1497513480240727318L;
    @SuppressWarnings("unused")
    private transient Logger logger = LoggerFactory.getLogger(getClass());
    private String name;
    private Ontology sharedOntology;
    private List<DataTableColumn> columns = new ArrayList<DataTableColumn>();
    private List<OntologyNode> filteredOntologyNodes = new ArrayList<OntologyNode>();
    private Set<OntologyNode> ontologyNodesForSelect = new HashSet<OntologyNode>();
    private HashMap<OntologyNode, OntologyNode> parentMap = new HashMap<OntologyNode, OntologyNode>();
    private Integer sequenceNumber;

    public enum ColumnType {
        INTEGRATION, DISPLAY
    }

    private ColumnType columnType;

    /*
     * whether NULLS get pulled back with the integration results
     */
    private boolean nullIncluded = true;
    private List<OntologyNode> flattenedOntologyNodeList;

    public IntegrationColumn() {
    }

    public IntegrationColumn(ColumnType columnType, DataTableColumn... incomingColumns) {
        this.columnType = columnType;
        this.columns.addAll(Arrays.asList(incomingColumns));
    }

    public ColumnType getColumnType() {
        return columnType;
    }

    public void setColumnType(ColumnType columnType) {
        this.columnType = columnType;
    }

    public boolean isDisplayColumn() {
        if (columnType == ColumnType.DISPLAY) {
            return true;
        }
        return false;
    }

    public boolean isIntegrationColumn() {
        if (columnType == ColumnType.INTEGRATION) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", columnType, getColumns());

    }

    public void setColumns(List<DataTableColumn> columns) {
        columns.removeAll(Collections.singletonList(null));
        this.columns = columns;
    }

    @XmlElementWrapper(name = "dataTableColumns")
    @XmlElement(name = "dataTableColumn")
    public List<DataTableColumn> getColumns() {
        columns.removeAll(Collections.singletonList(null));
        return columns;
    }

    @XmlElementWrapper(name = "mappingRules")
    @XmlElement(name = "mappingRule")
    public List<CodingRule> getCodingRules() {
        List<CodingRule> rules = new ArrayList<CodingRule>();
        for (DataTableColumn column : getColumns()) {
            if (column.getDefaultCodingSheet() == null) {
                continue;
            }
            rules.addAll(column.getDefaultCodingSheet().getCodingRules());
        }
        return rules;
    }

    @XmlElementWrapper(name = "filteredOntologyNodes")
    @XmlElement(name = "ontologyNode")
    public List<OntologyNode> getFilteredOntologyNodes() {
        filteredOntologyNodes.removeAll(Collections.singletonList(null));
        return filteredOntologyNodes;
    }

    public void setFilteredOntologyNodes(List<OntologyNode> filteredOntologyNodes) {
        filteredOntologyNodes.removeAll(Collections.singletonList(null));
        this.filteredOntologyNodes = filteredOntologyNodes;
        /*
         * when we set the filtered ontology nodes, we sort them and then try and get the parent
         * child relationships set into a custom hierarchy map that's used by getMappedOntologyNodeDisplayName
         * which is used when we pull data back out of the DB for display
         */
    }

    @SuppressWarnings("unused")
    private boolean isChildOf(OntologyNode parent, OntologyNode child) {
        return parent != null && parent.getIntervalStart() < child.getIntervalStart() && parent.getIntervalEnd() > child.getIntervalEnd();
    }

    public Map<OntologyNode, OntologyNode> getNearestParentMap() {
        if (parentMap.isEmpty()) {
            /*
             * loop through all of the ontology nodes from the select statement
             * if the node has a parent inside the filtered node set, then evaluate the distance betwee
             * the intervals (shorter is better). maintain the parent as the shortest.
             */
            for (OntologyNode node : getOntologyNodesForSelect()) {
                int distance = 1000000;
                for (OntologyNode filteredOntologyNode : getFilteredOntologyNodes()) {
                    if (node.isChildOf(filteredOntologyNode)) {
                        int localDistance = filteredOntologyNode.getIntervalEnd() - filteredOntologyNode.getIntervalStart();
                        if (distance > localDistance) {
                            parentMap.put(node, filteredOntologyNode);
                            distance = localDistance;
                        }
                    }
                }
            }
        }
        return parentMap;
    }

    /*
     * utility method to get just the column for the table
     */
    public DataTableColumn getColumnForTable(DataTable table) {
        for (DataTableColumn col : getColumns()) {
            if (col.getDataTable().equals(table))
                return col;
        }
        return null;
    }

    /**
     * Returns a lookup / association list / substitution Map<OntologyNode, OntologyNode> pairing, where
     * all nodes in the Ontology exist in the keySet, and the mapped value represents the appropriately aggregated
     * OntologyNode substitution for the given key. As a concrete example, given the ontology below:
     * 
     * <pre>
     * Wine
     *      WhiteWine
     *          Zinfandel
     *      RedWine
     *          Merlot
     *          Cabernet
     * </pre>
     * 
     * If the user selected Wine and RedWine, the mappings would look like this:
     * 
     * Zinfandel -> Wine
     * WhiteWine -> Wine
     * Wine -> Wine
     * RedWine -> RedWine
     * Merlot -> RedWine
     * Cabernet -> RedWine
     * 
     * Returns the OntologyNode that the given value in the given DataTableColumn has been mapped to. If the node has been
     * explicitly selected during the filter process, it will return it directly.
     * Otherwise if the node is a child node of an explicitly selected OntologyNode, it will return the first explicitly selected parent node.
     * 
     * 
     * @return
     */
    public OntologyNode getMappedOntologyNode(String value, DataTableColumn column) {
        // Find the node that matches the string from the select statement
        OntologyNode child = null;
        Map<String, OntologyNode> termToNodeMap = column.getDefaultCodingSheet().getTermToOntologyNodeMap();
        // Map<OntologyNode, List<String>> mappedDataValues = column.getNodeToDataValueMap();
        OntologyNode node = termToNodeMap.get(value);
        if (getOntologyNodesForSelect().contains(node)) {
            child = node;
        }
        // for (OntologyNode node : getOntologyNodesForSelect()) {
        // List<String> mappedValues = mappedDataValues.get(node);
        // if (mappedValues != null && mappedValues.contains(value)) {
        // child = node;
        // }
        // }

        if (child == null) {
            return null;
        }

        if (getFilteredOntologyNodes().contains(child)) {
            return child;
        }

        Map<OntologyNode, OntologyNode> nearestParents = getNearestParentMap();

        return nearestParents.get(child);
    }

    public boolean isNullIncluded() {
        return nullIncluded;
    }

    public void setNullIncluded(boolean nullIncluded) {
        this.nullIncluded = nullIncluded;
    }

    public void setName(String name) {
        this.name = name;
    }

    /*
     * By default, use the combination of all of the column names as the name
     */
    public String getName() {
        if (StringUtils.isEmpty(name)) {
            List<String> names = new ArrayList<String>();
            for (DataTableColumn column : getColumns()) {
                names.add(column.getDisplayName());
            }
            return StringUtils.join(names, ", ");
        }
        return name;
    }

    /**
     * @param allChildren
     */
    public void setOntologyNodesForSelect(Set<OntologyNode> allChildren) {
        ontologyNodesForSelect = allChildren;
    }

    @XmlTransient
    public Set<OntologyNode> getOntologyNodesForSelect() {
        return ontologyNodesForSelect;
    }

    @Override
    public int compareTo(IntegrationColumn other) {
        return sequenceNumber.compareTo(other.sequenceNumber);
    }

    @Override
    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSharedOntology(Ontology sharedOntology) {
        this.sharedOntology = sharedOntology;
    }

    public Ontology getSharedOntology() {
        return sharedOntology;
    }

    public List<OntologyNode> getFlattenedOntologyNodeList() {
        if (flattenedOntologyNodeList == null) {
            flatten();
        }
        return flattenedOntologyNodeList;
    }

    public void flatten() {
        if (sharedOntology == null || isDisplayColumn())
            return;
        List<OntologyNode> ontologyNodes = sharedOntology.getSortedOntologyNodes();
        SortedMap<Integer, List<OntologyNode>> ontologyNodeParentChildMap = sharedOntology.toOntologyNodeMap();

        for (OntologyNode ontologyNode : ontologyNodes) {
            boolean[] columnHasValueArray = new boolean[columns.size()];
            // assume integrationColumns and data value maps have already been
            // populated
            for (int index = 0; index < columns.size(); index++) {
                columnHasValueArray[index] = false;
                DataTableColumn column = columns.get(index);
                CodingSheet codingSheet = column.getDefaultCodingSheet();
                if (codingSheet == null) {
                    continue;
                }
                // check mapping first to see if the value should be translated a second
                // time to the common ontology format.

                // check if distinctValues has any values in common with mapped data values
                // if the two lists are disjoint (nothing in common), then there is no
                // data value if one of the distinct values is already equivalent to the ontology
                // node label, go with that.

                List<CodingRule> rules = codingSheet.findRuleMappedToOntologyNode(ontologyNode);
                if (CollectionUtils.isNotEmpty(rules)) {
                    for (CodingRule rule : rules) {
                        if (rule != null && rule.isMappedToData(column)) {
                            columnHasValueArray[index] = true;
                        }
                    }
                }

            }
            List<OntologyNode> children = ontologyNodeParentChildMap.get(Integer.valueOf(ontologyNode.getIntervalStart()));
            ontologyNode.setParent(CollectionUtils.isNotEmpty(children));
            ontologyNode.setColumnHasValueArray(columnHasValueArray);
        }
        flattenedOntologyNodeList = ontologyNodes;
    }

}
