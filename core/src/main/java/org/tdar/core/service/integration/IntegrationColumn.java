package org.tdar.core.service.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.Sequenceable;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.dao.resource.OntologyNodeDao;
import org.tdar.utils.PersistableUtils;

/**
 * 
 * Data transfer object used to maintain ontology related metadata for data integration.
 * 
 * @author Adam Brin
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class IntegrationColumn implements Serializable, Sequenceable<IntegrationColumn> {

    private static final long serialVersionUID = -1497513480240727318L;

    private String name;
    private Ontology sharedOntology;
    private List<DataTableColumn> columns = new ArrayList<>();
    private List<OntologyNode> filteredOntologyNodes = new ArrayList<>();
    private Set<OntologyNode> ontologyNodesForSelect = new HashSet<>();
    private HashMap<OntologyNode, OntologyNode> parentMap = new HashMap<>();
    private Integer sequenceNumber;
    private DataTableColumn tempTableDataTableColumn;
    private ColumnType columnType;

    // true if NULLS should be included with the integration results
    private boolean nullIncluded = true;
    @Deprecated
    private List<OntologyNode> flattenedOntologyNodeList;

    public IntegrationColumn() {
    }

    public IntegrationColumn(ColumnType columnType, DataTableColumn... incomingColumns) {
        this.columnType = columnType;
        this.columns.addAll(Arrays.asList(incomingColumns));
    }

    public IntegrationColumn(Ontology ontology, List<DataTableColumn> dataTableColumns) {
        this.columnType = ColumnType.INTEGRATION;
        this.sharedOntology = ontology;
        this.columns.addAll(dataTableColumns);
    }

    public ColumnType getColumnType() {
        return columnType;
    }

    public void setColumnType(ColumnType columnType) {
        this.columnType = columnType;
    }

    public boolean isDisplayColumn() {
        return columnType == ColumnType.DISPLAY;
    }

    public boolean isCountColumn() {
        return columnType == ColumnType.COUNT;
    }

    public boolean isIntegrationColumn() {
        return columnType == ColumnType.INTEGRATION;
    }

    @Override
    public String toString() {
        String add = String.format("(ontology: %s | columns: %s | nodes: %s)", sharedOntology, PersistableUtils.extractIds(getColumns()),
                PersistableUtils.extractIds(getFilteredOntologyNodes()));
        return String.format("%s (%s) %s", getName(), columnType, add);
    }

    public void setColumns(List<DataTableColumn> columns) {
        this.columns = columns;
    }

    @XmlElementWrapper(name = "dataTableColumns")
    @XmlElement(name = "dataTableColumn")
    public List<DataTableColumn> getColumns() {
        return columns;
    }

    @XmlElementWrapper(name = "filteredOntologyNodes")
    @XmlElement(name = "ontologyNode")
    /**
     * The list of OntologyNodes specified by the "filter" process (specifying which nodes should be actually used in the integration)
     * 
     * @return
     */
    public List<OntologyNode> getFilteredOntologyNodes() {
        return filteredOntologyNodes;
    }

    /**
     * The list of OntologyNodes specified by the "filter" process (specifying which nodes should be actually used in the integration).
     * When we set the filtered ontology nodes, we sort them and then try and get the parent
     * child relationships set into a custom hierarchy map that's used by getMappedOntologyNodeDisplayName
     * which is used when we pull data back out of the DB for display
     */
    public void setFilteredOntologyNodes(List<OntologyNode> filteredOntologyNodes) {
        filteredOntologyNodes.removeAll(Collections.singletonList(null));
        this.filteredOntologyNodes = filteredOntologyNodes;
    }

    /**
     * Create a Map of Node -> node that provides the nearest parent of the selected ontology nodes
     * 
     * @return
     */
    private Map<OntologyNode, OntologyNode> getNearestParentMap() {
        if (parentMap.isEmpty()) {
            // loop through all of the ontology nodes from the select statement
            // if the node has a parent inside the filtered node set, then evaluate the distance between
            // the intervals (shorter is better). maintain the parent as the shortest.
            for (OntologyNode node : getOntologyNodesForSelect()) {
                int distance = Integer.MAX_VALUE;
                for (OntologyNode filteredOntologyNode : filteredOntologyNodes) {
                    if (filteredOntologyNode != null && node.isChildOf(filteredOntologyNode)) {
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
            if (col.getDataTable().equals(table)) {
                return col;
            }
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
        if (column == null) { // || column.getDefaultCodingSheet() == null
            return null;
        }
        Map<String, OntologyNode> termToNodeMap = column.getDefaultCodingSheet().getTermToOntologyNodeMap();
        OntologyNode node = termToNodeMap.get(value);
        if (node == null) {
            node = column.getDefaultCodingSheet().getDefaultOntology().getNodeByName(value);
        }
        if (getOntologyNodesForSelect().contains(node)) {
            child = node;
        }
        if (child == null) {
            return null;
        }

        if (filteredOntologyNodes.contains(child)) {
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

    @XmlTransient
    /**
     * This is the fully-formed list of ontology nodes that has the filter/flatten process applied the node Hierarchy
     * includes all childen that are necessary for the hierarchical query.
     * 
     * @return
     */
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

    /**
     * Transforms the Ontology nodes associated with the specified Ontology to be hierarchical (parent/child set) and have transient
     * mappings for dataTableColumns that have actual data (lack of entries in the map are equivalent to lack of data)
     * 
     * @return
     */
    @Deprecated
    public List<OntologyNode> getFlattenedOntologyNodeList() {
        return flattenedOntologyNodeList;
    }

    public DataTableColumn getTempTableDataTableColumn() {
        return tempTableDataTableColumn;
    }

    public void setTempTableDataTableColumn(DataTableColumn dataTableColumn) {
        this.tempTableDataTableColumn = dataTableColumn;
    }

    public void buildNodeChildHierarchy(OntologyNodeDao ontologyNodeDao) {
        ontologyNodesForSelect = ontologyNodeDao.getAllChildren(filteredOntologyNodes);
    }

    @Deprecated
    public void setFlattenedOntologyNodeList(List<OntologyNode> flattenedOntologyNodeList) {
        this.flattenedOntologyNodeList = flattenedOntologyNodeList;
    }

}
