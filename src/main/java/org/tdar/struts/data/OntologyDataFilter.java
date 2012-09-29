package org.tdar.struts.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SortedMap;

import org.apache.log4j.Logger;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;

/**
 * $Id$
 * 
 * An OntologyDataFilter encompasses one set of integration columns and carries
 * data to filter those integration columns via a unified ontology.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class OntologyDataFilter implements Serializable {

    private static final long serialVersionUID = -6302957159933312986L;

    private ArrayList<DataTableColumn> integrationColumns = new ArrayList<DataTableColumn>();

    @SuppressWarnings("unused")
    private transient Logger logger = Logger.getLogger(getClass());

    // FIXME: in the future allow for arbitrary ontology -> DataTableColumn
    // combinations
    // in that future, this would be the unified ontology.
    private Ontology commonOntology;

    private ArrayList<OntologyNodeData> flattenedOntologyNodeList;

    private LinkedHashMap<DataTableColumn, List<String>> distinctColumnValueMap = new LinkedHashMap<DataTableColumn, List<String>>();

    public void put(DataTableColumn column, List<String> distinctValues) {
        integrationColumns.add(column);
        distinctColumnValueMap.put(column, distinctValues);
    }

    public void flatten() {
        SortedMap<Integer, List<OntologyNode>> ontologyNodeMap = commonOntology.toOntologyNodeMap();
        // logger.debug("ontology node map: " + ontologyNodeMap);
        ArrayList<OntologyNode> ontologyNodes = new ArrayList<OntologyNode>(commonOntology.getSortedOntologyNodes());
        flattenedOntologyNodeList = new ArrayList<OntologyNodeData>(ontologyNodes.size());
        // generate OntologyNodeData with extra metadata for the page
        // FIXME: first pass naive algorithm, should iterate over the
        // DataTableColumns instead?
        for (OntologyNode ontologyNode : ontologyNodes) {
            OntologyNodeData nodeData = new OntologyNodeData(ontologyNode);
            boolean[] columnHasValueArray = new boolean[integrationColumns.size()];
            // assume integrationColumns and data value maps have already been
            // populated
            for (int index = 0; index < integrationColumns.size(); index++) {
                DataTableColumn column = integrationColumns.get(index);
                // check mapping first to see if the value should be translated a second
                // time
                // to the common ontology format.
                List<String> mappedDataValues = ontologyNode.getMappedDataValues(column);
                List<String> distinctValues = distinctColumnValueMap.get(column);
                // check if distinctValues has any values in common with mapped data
                // values
                // if the two lists are disjoint (nothing in common), then there is no
                // data value
                // if one of the distinct values is already equivalent to the ontology
                // node label, go with that.
                // logger.debug("distinct values: " + distinctValues);
                // logger.debug("mapped data values: " + mappedDataValues);
                // logger.debug("node label: " + ontologyNode.getLabel());
                columnHasValueArray[index] = distinctValues.contains(ontologyNode.getIri()) || !Collections.disjoint(mappedDataValues, distinctValues);
            }
            nodeData.setColumnHasValueArray(columnHasValueArray);
            List<OntologyNode> children = ontologyNodeMap.get(Integer.valueOf(nodeData.getIntervalStart()));
            nodeData.setParent(children != null && children.size() > 1);
            // nodeData.setParent(ontologyNodeMap.containsKey(
            // ontologyNode.getIntervalStart()) );

            flattenedOntologyNodeList.add(nodeData);
        }
    }

    public List<DataTableColumn> getIntegrationColumns() {
        return integrationColumns;
    }

    public Ontology getCommonOntology() {
        return commonOntology;
    }

    public void setCommonOntology(Ontology commonOntology) {
        this.commonOntology = commonOntology;
    }

    public List<OntologyNodeData> getFlattenedOntologyNodeList() {
        if (flattenedOntologyNodeList == null) {
            flatten();
        }
        return flattenedOntologyNodeList;
    }

    /**
     * Returns a unique ID for this IntegrationData object, combination of all
     * column ids.
     * 
     * @return
     */
    public String getColumnIds() {
        StringBuilder columnIds = new StringBuilder();
        for (Iterator<DataTableColumn> iter = integrationColumns.iterator(); iter.hasNext();) {
            DataTableColumn column = iter.next();
            columnIds.append(column.getId());
            if (iter.hasNext()) {
                columnIds.append("_");
            }
        }
        return columnIds.toString();
    }

}
