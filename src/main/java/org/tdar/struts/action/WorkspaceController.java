package org.tdar.struts.action;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.FilestoreService;
import org.tdar.filestore.PersonalFilestoreFile;
import org.tdar.struts.data.IntegrationDataResult;
import org.tdar.struts.data.OntologyDataFilter;
import org.tdar.utils.SimpleSerializer;
import org.tdar.utils.resource.PartitionedResourceResult;

/**
 * $Id$
 * 
 * Data integration activities in the workspace.
 * 
 * @author Allen Lee, Huiping Cao
 * @version $Rev$
 */
@ParentPackage("secured")
@Namespace("/workspace")
@Component
@Scope("prototype")
public class WorkspaceController extends AuthenticationAware.Base {

    private static final long serialVersionUID = -3538370664425794045L;

    private List<Resource> bookmarkedResources;
    private PartitionedResourceResult partitionedBookmarkedResources;
    private List<Long> tableIds;
    private List<DataTable> selectedDataTables;
    private List<String> ontologyNodeFilterSelections;

    private List<String> integrationRules;
    private List<String> displayRules;

    private Long ticketId;

    @Autowired
    private FilestoreService filestoreService;

    private List<OntologyDataFilter> ontologyDataFilters = new ArrayList<OntologyDataFilter>();

    private List<IntegrationDataResult> integrationDataResults = new ArrayList<IntegrationDataResult>();

    private String integrationDataResultsFilename;
    private transient InputStream integrationDataResultsInputStream;

    /**
     * Pass through actions that will go to <action-name>.ftl or <action-name>.jsp
     */
    @Actions({
            @Action(value = "select-tables"),
            @Action("list")
    })
    @Override
    public String execute() {
        return SUCCESS;
    }

    // by default SUCCESS maps to <action-name>.ftl
    @Action(value = "select-columns",
            results = {
                    @Result(name = SUCCESS, location = "select-columns.ftl"),
                    @Result(name = INPUT, location = "select-tables.ftl")
    })
    public String selectColumns() {
        // FIXME: do we want to log this step? Perhaps, but there's no resource being modified, and resource parameter isn't nullable.
        if (CollectionUtils.isEmpty(tableIds)) {
            addActionError("Please select the tables that you'd like to integrate.");
            return INPUT;
        }
        return SUCCESS;
    }

    /*
     * figure out which data table columns were selected in the previous page as the integration condition, and then which columns were selected as display
     * attributes selectedDataTableColumnIds etc. should have integration_condition and display_attribute parameters each string in integration_condition is of
     * the form tableId_columnId=tableId_columnId
     */

    @Action(value = "filter",
            results = {
                    @Result(name = SUCCESS, location = "filter.ftl"),
                    @Result(name = INPUT, location = "select-columns.ftl")
    })
    public String filterDataValues() {

        try {
            List<List<Long>> integrationColumnIds = getIntegrationColumnIds();
            getLogger().debug("processing " + integrationColumnIds + " column ids.");
            if (CollectionUtils.isEmpty(integrationColumnIds)) {
                getLogger().warn("returning to select columns, no integration conditions supplied.");
                addActionError("returning to select columns, no integration conditions supplied.");
                return INPUT;
            }
            // each column could have its own distinct ontology in the future. at the moment we assume that
            // each pair of columns has a shared common ontology
            for (List<Long> columnIds : integrationColumnIds) {
                // there's one ontology data filter object encompassing each set of integration columns
                OntologyDataFilter dataFilter = new OntologyDataFilter();
                for (Long id : columnIds) {
                    DataTableColumn column = getDataTableService().findDataTableColumn(id);
                    Ontology defaultOntology = column.getDefaultOntology();
                    if (defaultOntology != null) {
                        getLogger().debug("default ontology: " + defaultOntology.getTitle());
                        dataFilter.setCommonOntology(defaultOntology);
                    }
                    List<String> distinctValues = getDataTableService().findAllDistinctValues(column);
                    getLogger().debug("distinct values for column: " + distinctValues);
                    dataFilter.put(column, distinctValues);
                }
                // generate distinct value maps in integration data list
                ontologyDataFilters.add(dataFilter);
            }
            // the display attributes are a map of table names to a list of column names (that ostensibly belong to the table)
            Map<DataTable, List<DataTableColumn>> displayAttributes = getDisplayAttributeMap();

            SimpleSerializer ss = new SimpleSerializer();
            ss.addToWhitelist(DataTableColumn.class, "categoryVariable", "columnDataType", "columnEncodingType", "columnEncodingType", "measurementUnit",
                    "name");
            ss.addToWhitelist(CategoryVariable.class, "description", "label", "parent", "type");
            ss.addToWhitelist(OntologyDataFilter.class, "integrationColumns");
            Map<String, Object> payload = new HashMap<String, Object>();
            payload.put("dataFilter", ontologyDataFilters);
            payload.put("displayAttributes", displayAttributes);
            String xmlPayload = ss.toXml(payload);
            logResourceModification(null, "displaying filter form. see payload for selected integration/display variable", xmlPayload);
        } catch (Exception e) {
            addActionErrorWithException(e.getMessage(), e);
            return INPUT;
        }
        return SUCCESS;
    }

    /**
     * Incoming data to this action:
     * 
     * <ul>
     * <li>the ontologyCheckbox_* action parameters encode the ids that are necessary to run the data integration process in the form
     * paramString_(integration-column-id1_integration-column-id2)+...ontology_node_id
     * <li>a Map<String, List<String>> of table names to column names is stored on the session and represents the columns that should just be displayed /
     * selected from the given table.
     * </ul>
     * 
     * @return
     */
    @Action(value = "display-filtered-results",
            results = {
                    @Result(name = SUCCESS, location = "display-filtered-results.ftl"),
                    @Result(name = INPUT, location = "filter.ftl")
    })
    public String displayFilteredResults() {
        getLogger().trace("XXX: DISPLAYING FILTERED RESULTS :XXX");
        try {
            Map<String, List<OntologyNode>> columnIdsToOntologyNodeMap = new HashMap<String, List<OntologyNode>>();

            if (!CollectionUtils.isEmpty(ontologyNodeFilterSelections)) {
                for (String key : ontologyNodeFilterSelections) {
                    if (!StringUtils.isBlank(key)) {
                        // parse key - it's always in the format, column id_column id_column id_ontologyNodeId
                        getLogger().trace("key is: " + key);
                        // last id is the ontology node id
                        int lastUnderscoreIndex = key.lastIndexOf("_");
                        String ontologyNodeId = key.substring(lastUnderscoreIndex + 1);
                        String columnIds = key.substring(0, lastUnderscoreIndex);
                        // use columnIds as a key to the data structure that will maintain all filtered nodes
                        getLogger().trace("column ids: " + columnIds + " ontology node id: " + ontologyNodeId);
                        OntologyNode selectedOntologyNode = getOntologyNodeService().find(Long.valueOf(ontologyNodeId));
                        getLogger().trace("selected ontology node: " + selectedOntologyNode);
                        List<OntologyNode> selectedOntologyNodes = columnIdsToOntologyNodeMap.get(columnIds);
                        if (selectedOntologyNodes == null) {
                            selectedOntologyNodes = new ArrayList<OntologyNode>();
                            columnIdsToOntologyNodeMap.put(columnIds, selectedOntologyNodes);
                        }
                        selectedOntologyNodes.add(selectedOntologyNode);
                    }
                }
            } else {
                ontologyNodeFilterSelections = new ArrayList<String>();
            }
            // log the selected ontology nodes.
            SimpleSerializer ontologySerializer = new SimpleSerializer();
            ontologySerializer.addToWhitelist(OntologyNode.class, "index", "label");
            String xmlSelectedOntologies = ontologySerializer.toXml(columnIdsToOntologyNodeMap);
            logResourceModification(null, "display filtered results (payload: selectedOntologies)", xmlSelectedOntologies);

            getLogger().trace("column ids to ontology node map: " + columnIdsToOntologyNodeMap);
            // after we find all selected ontology nodes, figure out display attribute set
            // and then render integration data results for display attributes and integrated columns
            Map<DataTable, List<DataTableColumn>> tableToIntegrationColumns = new HashMap<DataTable, List<DataTableColumn>>();
            Map<DataTable, Map<OntologyNode, List<OntologyNode>>> tableToOntologyHierarchyMap = new HashMap<DataTable, Map<OntologyNode, List<OntologyNode>>>();
            // Map<DataTable, Set<OntologyNode>> tableToAllOntologyNodes = new HashMap<DataTable, Set<OntologyNode>>();

            List<List<DataTableColumn>> integrationColumnGroups = new ArrayList<List<DataTableColumn>>();

            for (Map.Entry<String, List<OntologyNode>> entry : columnIdsToOntologyNodeMap.entrySet()) {
                String columnIds = entry.getKey();
                List<OntologyNode> ontologyNodes = entry.getValue();
                // generate integration data results for each column
                List<DataTableColumn> integrationColumns = new ArrayList<DataTableColumn>();
                integrationColumnGroups.add(integrationColumns);

                for (String columnId : StringUtils.split(columnIds, '_')) {
                    DataTableColumn column = getDataTableService().findDataTableColumn(Long.valueOf(columnId));
                    integrationColumns.add(column);
                }
                // after all integration columns have been added, add them to the map of tables to columns
                // aggregate all columns together
                for (DataTableColumn column : integrationColumns) {
                    DataTable table = column.getDataTable();
                    List<DataTableColumn> columns = tableToIntegrationColumns.get(table);
                    if (columns == null) {
                        columns = new ArrayList<DataTableColumn>();
                        tableToIntegrationColumns.put(table, columns);
                    }
                    columns.add(column);
                    // add ontology node hierarchies to the tableToOntologyHierarchyMap
                    Map<OntologyNode, List<OntologyNode>> hierarchyMap = tableToOntologyHierarchyMap.get(table);
                    if (hierarchyMap == null) {
                        hierarchyMap = new HashMap<OntologyNode, List<OntologyNode>>();
                        tableToOntologyHierarchyMap.put(table, hierarchyMap);
                    }
                    hierarchyMap.putAll(getOntologyNodeService().getHierarchyMap(ontologyNodes));
                }
            }

            // ok, at this point we have the integration columns that we're interested in + the ontology
            // nodes that we want to use to filter values of interest and for aggregation.
            // getLogger().debug("table columns are: " + tableToIntegrationColumns);
            // getLogger().debug("ontology node hierarchy map: " + tableToOntologyHierarchyMap);
            if (CollectionUtils.isEmpty(tableToIntegrationColumns)) {
                addActionError("Either no integration columns or filter values were selected, please go back and select both");
                return INPUT;
            }

            Map<DataTable, List<DataTableColumn>> displayAttributeMap = getDisplayAttributeMap();
            integrationDataResults = getDataIntegrationService().generateIntegrationData(
                    tableToIntegrationColumns,
                    tableToOntologyHierarchyMap,
                    displayAttributeMap);
            // getSessionData().setIntegrationDataResults(integrationDataResults);

            PersonalFilestoreTicket ticket = getDataIntegrationService().toExcel(integrationColumnGroups, displayAttributeMap, integrationDataResults,
                    getAuthenticatedUser());
            setTicketId(ticket.getId());

            // now we log the selected integration columns and selected display columns
            SimpleSerializer dataTableSerializer = new SimpleSerializer();
            dataTableSerializer.addToWhitelist(DataTable.class, "name");
            dataTableSerializer.addToWhitelist(DataTableColumn.class, "categoryVariable", "columnDataType", "columnEncodingType", "columnEncodingType",
                    "measurementUnit", "name");
            dataTableSerializer.addToWhitelist(CategoryVariable.class, "description", "label", "parent", "type");
            String xmlTableToIntegrationColumns = dataTableSerializer.toXml(tableToIntegrationColumns);
            logResourceModification(null, "display filtered resulsts (payload: tableToIntegrationColumns", xmlTableToIntegrationColumns);
        } catch (Throwable e) {
            addActionErrorWithException(e.getMessage(), e);
            return INPUT;
        }
        // FIXME: not sure this does anything
        // SimpleSerializer serializer = new SimpleSerializer();
        // String xmlDisplayColumns = serializer.toXml(getSessionData().getTableNamesToDisplayColumnNamesMap());
        // logResourceModification(null, "display filtered results (payload: tableToDisplayColumns)", xmlDisplayColumns);

        return SUCCESS;
    }

    private long integrationDataResultsContentLength;

    @Action(value = "download", results = {
            @Result(name = SUCCESS, type = "stream",
                    params = {
                            "contentType", "application/vnd.ms-excel",
                            "inputName", "integrationDataResultsInputStream",
                            "contentDisposition", "attachment;filename=\"${integrationDataResultsFilename}\"",
                            "contentLength", "${integrationDataResultsContentLength}"
            }),
            @Result(name = INPUT, type = "redirect", location = "select-tables")
    })
    public String downloadIntegrationDataResults() {
        // create temporary file
        try {
            List<PersonalFilestoreFile> files = filestoreService.retrieveAllPersonalFilestoreFiles(getTicketId());
            integrationDataResultsInputStream = new FileInputStream(files.get(0).getFile());
            integrationDataResultsContentLength = files.get(0).getFile().length();
            integrationDataResultsFilename = files.get(0).getFile().getName();
        } catch (IOException exception) {
            addActionErrorWithException("Unable to access file.", exception);
        }

        return SUCCESS;
    }

    /**
     * Returns a Map of the table name to a list of the column names that should be displayed from that table.
     * 
     * tbid_tbname 1: <colid_colname 1> <colid_colname 2>, etc tbid_tbname 2:
     * <colid_colname 1> <colid_colname 2>, etc
     * 
     * @param parameters
     * @return
     */
    private Map<DataTable, List<DataTableColumn>> getDisplayAttributeMap() {
        Map<DataTable, List<DataTableColumn>> tableNameToColumnNameMap = new HashMap<DataTable, List<DataTableColumn>>();

        getLogger().debug("Incoming Display Rules:" + getDisplayRules());

        for (String displayRule : getDisplayRules()) {
            getLogger().debug("Display Rule:" + displayRule);
            if (StringUtils.isEmpty(displayRule)) {
                continue;
            }
            if (StringUtils.isNumeric(displayRule)) {
                DataTableColumn column = getDataTableService().findDataTableColumn(Long.parseLong(displayRule));
                DataTable table = column.getDataTable();
                if (tableNameToColumnNameMap.get(table) == null) {
                    tableNameToColumnNameMap.put(table, new ArrayList<DataTableColumn>());
                }
                tableNameToColumnNameMap.get(table).add(column);
            } else {
                throw new TdarRecoverableRuntimeException("could not parse display rules, " + displayRules);
            }

        }
        getLogger().debug("{}", tableNameToColumnNameMap);
        return tableNameToColumnNameMap;
    }

    /**
     * Returns a list of lists representing the equivalent column IDs to be integrated.
     */
    // valid row should look like : 5649=5938
    public List<List<Long>> getIntegrationColumnIds() {
        List<List<Long>> integrationColumnIds = new ArrayList<List<Long>>();
        List<String> integrationSteps = getIntegrationRules();
        getLogger().debug("steps:" + integrationSteps.size());

        int numPerStep = 0;
        for (int stepNum = 0; stepNum < integrationSteps.size(); stepNum++) {
            String integrationStep = integrationSteps.get(stepNum);
            getLogger().debug("step:" + integrationStep);
            List<Long> columnIds = new ArrayList<Long>();
            if (StringUtils.isBlank(integrationStep))
                continue;

            if (integrationStep.trim().endsWith("=")) {
                throw new TdarRecoverableRuntimeException("Your integration appeared to be missing columns for at least one integration variable");
            }

            String[] columns = StringUtils.split(integrationStep, "=");

            if (numPerStep == 0) { // haven't seen anything, setting
                numPerStep = columns.length;
                // otherwise, have seen something, should never just be 1
                // should have consistent # of steps too
            } else if (numPerStep != columns.length) {
                throw new TdarRecoverableRuntimeException("Your integration appeared to be missing columns for at least one integration variable");
            }

            for (int colNum = 0; colNum < columns.length; colNum++) {
                String column = columns[colNum];
                if (StringUtils.isEmpty(column) || !StringUtils.isNumeric(column)) {
                    throw new TdarRecoverableRuntimeException("Step #:" + stepNum + " had trouble interpreting integration rules:" + integrationStep
                            + " a column is either empty or the data was corrupted");
                }
                columnIds.add(Long.parseLong(column));
            }
            integrationColumnIds.add(columnIds);
        }
        return integrationColumnIds;
    }

    public List<Resource> getBookmarkedResources() {
        if (bookmarkedResources == null) {
            bookmarkedResources = getBookmarkedResourceService().findResourcesByPerson(getAuthenticatedUser());
        }
        return bookmarkedResources;
    }

    public PartitionedResourceResult getPartitionedBookmarkedResources() {
        if (partitionedBookmarkedResources == null) {
            partitionedBookmarkedResources = new PartitionedResourceResult(getBookmarkedResources());
        }
        return partitionedBookmarkedResources;
    }

    public List<Document> getBookmarkedDocuments() {
        return getPartitionedBookmarkedResources().getResourcesOfType(Document.class);
    }

    public List<CodingSheet> getBookmarkedCodingSheets() {
        return getPartitionedBookmarkedResources().getResourcesOfType(CodingSheet.class);
    }

    public List<SensoryData> getBookmarkedSensoryData() {
        return getPartitionedBookmarkedResources().getResourcesOfType(SensoryData.class);
    }

    public List<Image> getBookmarkedImages() {
        return getPartitionedBookmarkedResources().getResourcesOfType(Image.class);
    }

    public List<Ontology> getBookmarkedOntologies() {
        return getPartitionedBookmarkedResources().getResourcesOfType(Ontology.class);
    }

    public List<Dataset> getBookmarkedDatasets() {
        List<Dataset> datasets = new ArrayList<Dataset>();
        for (Dataset dataset : getPartitionedBookmarkedResources().getResourcesOfType(Dataset.class)) {
            if (getEntityService().canViewConfidentialInformation(getAuthenticatedUser(), dataset)) {
                datasets.add(dataset);
            } else if (!dataset.hasConfidentialFiles()) {
                datasets.add(dataset);
            }
        }
        return datasets;
    }

    public Set<DataTable> getBookmarkedDataTables() {
        Set<DataTable> dataTables = new HashSet<DataTable>();
        for (Dataset d : getBookmarkedDatasets()) {
            dataTables.addAll(d.getDataTables());
        }
        return dataTables;
    }

    public List<Long> getTableIds() {
        return tableIds;
    }

    public void setTableIds(List<Long> tableIds) {
        this.tableIds = tableIds;
    }

    public List<DataTable> getSelectedDataTables() {
        if (selectedDataTables == null) {
            selectedDataTables = getDataTableService().findAllFromIdList(tableIds);
        }
        return selectedDataTables;
    }

    public List<OntologyDataFilter> getOntologyDataFilters() {
        return ontologyDataFilters;
    }

    public List<IntegrationDataResult> getIntegrationDataResults() {
        return integrationDataResults;
    }

    public String getIntegrationDataResultsFilename() {
        return integrationDataResultsFilename;
    }

    public InputStream getIntegrationDataResultsInputStream() {
        return integrationDataResultsInputStream;
    }

    public long getIntegrationDataResultsContentLength() {
        return integrationDataResultsContentLength;
    }

    public List<String> getIntegrationRules() {
        // if (integrationRules == null) { return new ArrayList<String>(); }
        return integrationRules;
    }

    public void setIntegrationRules(List<String> integrationRules) {
        this.integrationRules = integrationRules;
    }

    public List<String> getDisplayRules() {
        return displayRules;
    }

    public void setDisplayRules(List<String> displayRules) {
        this.displayRules = displayRules;
    }

    private HashMap<Ontology, List<DataTableColumn>> dataTableAutoMap = new HashMap<Ontology, List<DataTableColumn>>();

    public List<List<DataTableColumn>> getDataTableColumnIntegrationSuggestions() {

        // iterate through all of the columns and get a map of the ones associated
        // with any ontology.

        for (DataTable table : getSelectedDataTables()) {
            for (DataTableColumn column : table.getDataTableColumns()) {
                if (column.getDefaultOntology() != null) {
                    if (!dataTableAutoMap.containsKey(column.getDefaultOntology())) {
                        dataTableAutoMap.put(column.getDefaultOntology(), new ArrayList<DataTableColumn>());
                    }
                    dataTableAutoMap.get(column.getDefaultOntology()).add(column);
                }
            }
        }

        // okay now we have a map of the data table columns,
        List<List<DataTableColumn>> columnAutoList = new ArrayList<List<DataTableColumn>>();
        for (Ontology key : dataTableAutoMap.keySet()) {
            ArrayList<Long> seen = new ArrayList<Long>();
            ArrayList<Long> seen2 = new ArrayList<Long>();
            List<DataTableColumn> columnList = new ArrayList<DataTableColumn>();
            List<DataTableColumn> columnList2 = new ArrayList<DataTableColumn>();
            // go through the hashMap and try and pair out by set of rules
            // assuming that there is one column per table at a time
            // and there might be a case where there are more than one
            for (DataTableColumn column : dataTableAutoMap.get(key)) {
                if (!seen.contains(column.getDataTable().getId())) {
                    columnList.add(column);
                    seen.add(column.getDataTable().getId());
                } else if (!seen2.contains(column.getDataTable().getId())) {
                    columnList2.add(column);
                    seen2.add(column.getDataTable().getId());
                } // give up
            }

            // might want to tune this to some logic like:
            // if just one table, then anything with an ontology
            // if more than one, just show lists with at least two ontologies
            if (columnList.size() > 0)
                columnAutoList.add(columnList);
            if (columnList2.size() > 0)
                columnAutoList.add(columnList2);
        }
        return columnAutoList;
    }

    /**
     * @param ontologyNodeFilterSelections
     *            the ontologyNodeFilterSelections to set
     */
    public void setOntologyNodeFilterSelections(List<String> ontologyNodeFilterSelections) {
        this.ontologyNodeFilterSelections = ontologyNodeFilterSelections;
    }

    /**
     * @return the ontologyNodeFilterSelections
     */
    public List<String> getOntologyNodeFilterSelections() {
        return ontologyNodeFilterSelections;
    }

    /**
     * @param ticketId
     *            the ticketId to set
     */
    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    /**
     * @return the ticketId
     */
    public Long getTicketId() {
        return ticketId;
    }

}
