package org.tdar.struts.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
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
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.Image;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.Project;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.SensoryData;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.service.FilestoreService;
import org.tdar.filestore.PersonalFilestoreFile;
import org.tdar.struts.data.IntegrationColumn;
import org.tdar.struts.data.IntegrationColumn.ColumnType;
import org.tdar.struts.data.IntegrationDataResult;
import org.tdar.utils.Pair;
import org.tdar.utils.resource.PartitionedResourceResult;

/**
 * $Id$
 * 
 * Data integration activities in the workspace.
 * 
 * @author Allen Lee, Adam Brin
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
    private List<IntegrationColumn> integrationColumns;
    private List<Long> tableIds;
    private List<DataTable> selectedDataTables;
    // private List<String> ontologyNodeFilterSelections;

    private Long ticketId;

    @Autowired
    private FilestoreService filestoreService;
    private List<IntegrationDataResult> integrationDataResults = new ArrayList<IntegrationDataResult>();
    private String integrationDataResultsFilename;
    private transient InputStream integrationDataResultsInputStream;

    private Map<List<OntologyNode>, Map<DataTable, Integer>> pivotData;

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
            // getLogger().debug("processing " + integrationColumnIds + " column ids.");
            // if (CollectionUtils.isEmpty(integrationColumnIds)) {
            // getLogger().warn("returning to select columns, no integration conditions supplied.");
            // addActionError("returning to select columns, no integration conditions supplied.");
            // return INPUT;
            // }
            // each column could have its own distinct ontology in the future. at the moment we assume that
            // each pair of columns has a shared common ontology
            logger.debug("integration columns: {}", getIntegrationColumns());

            for (IntegrationColumn integrationColumn : getIntegrationColumns()) {
                if (integrationColumn.isDisplayColumn()) {
                    continue;
                }

                // OntologyDataFilter dataFilter = new OntologyDataFilter();
                logger.info("total columns : {}", integrationColumn.getColumns());
                for (DataTableColumn column : integrationColumn.getColumns()) {
                    if (column != null && column.getId() != null && column.getId() > 0) {
                        logger.info("dehydrate: {}", column);
                    }
                }
                // rehydrate all of the resources being passed in, we just had empty beans with ids
                List<DataTableColumn> hydrated = getGenericService().rehydrateSparseIdBeans(integrationColumn.getColumns(), DataTableColumn.class);
                integrationColumn.setColumns(hydrated);
                logger.info("hydrated columns {}", hydrated);
                Ontology defaultOntology = null;

                // for each DataTableColumn, grab the shared ontology if it exists; setup mappings
                for (DataTableColumn column : hydrated) {
                    logger.info("{} ({})", column, column.getDefaultOntology());
                    defaultOntology = column.getDefaultOntology();
                    if (defaultOntology != null) {
                        getLogger().debug("default ontology: {}", defaultOntology.getTitle());
                        integrationColumn.setSharedOntology(defaultOntology);
                    }
                    List<String> distinctValues = getDataTableService().findAllDistinctValues(column);
                    getLogger().debug("distinct values for column: " + distinctValues);
                    integrationColumn.put(column, distinctValues);
                }
                // generate distinct value maps in integration data list
            }
            logger.debug("intermediate: {}", getDataIntegrationService().serializeIntegrationContext(getIntegrationColumns(), getAuthenticatedUser()));
        } catch (Exception e) {
            addActionErrorWithException(e.getMessage(), e);
            return INPUT;
        }
        return SUCCESS;
    }

    @Action(value = "display-filtered-results",
            results = {
                    @Result(name = SUCCESS, location = "display-filtered-results.ftl"),
                    @Result(name = INPUT, location = "filter.ftl")
            })
    public String displayFilteredResults() {
        getLogger().trace("XXX: DISPLAYING FILTERED RESULTS :XXX");
        String integrationContextXml = getDataIntegrationService().serializeIntegrationContext(getIntegrationColumns(), getAuthenticatedUser());
        try {
            // ADD ERROR CHECKING LOGIC

            // // ok, at this point we have the integration columns that we're interested in + the ontology
            // // nodes that we want to use to filter values of interest and for aggregation.
            // // getLogger().debug("table columns are: " + tableToIntegrationColumns);
            // // getLogger().debug("ontology node hierarchy map: " + tableToOntologyHierarchyMap);
            // if (CollectionUtils.isEmpty(tableToIntegrationColumns)) {
            // addActionError("Either no integration columns or filter values were selected, please go back and select both");
            // return INPUT;
            // }
            //
            Pair<List<IntegrationDataResult>, Map<List<OntologyNode>, Map<DataTable, Integer>>> generatedIntegrationData = getDataIntegrationService()
                    .generateIntegrationData(getIntegrationColumns(), getSelectedDataTables());
            
            integrationDataResults = generatedIntegrationData.getFirst();
            setPivotData(generatedIntegrationData.getSecond());
            PersonalFilestoreTicket ticket = getDataIntegrationService().toExcel(getIntegrationColumns(), generatedIntegrationData,
                    getAuthenticatedUser());
            File file = File.createTempFile("integration", ".xml");
            FileUtils.writeStringToFile(file, integrationContextXml);
            filestoreService.store(ticket, file, "integration-context.xml");
            setTicketId(ticket.getId());
        } catch (Throwable e) {
            e.printStackTrace();
            addActionErrorWithException(e.getMessage(), e);
            return INPUT;
        }
        logResourceModification(null, "display filtered results (payload: tableToDisplayColumns)", integrationContextXml);
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

    public List<Project> getBookmarkedProjects() {
        return getPartitionedBookmarkedResources().getResourcesOfType(Project.class);
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

    public void setIntegrationColumns(List<IntegrationColumn> integrationColumns) {
        this.integrationColumns = integrationColumns;
    }

    public List<IntegrationColumn> getIntegrationColumns() {
        if (integrationColumns == null) {
            integrationColumns = new ArrayList<IntegrationColumn>();
        }
        Iterator<IntegrationColumn> iterator = integrationColumns.iterator();
        while (iterator.hasNext()) {
            IntegrationColumn column = iterator.next();
            if (column == null || column.getColumns().size() == 0) {
                logger.debug("removing null column");
                iterator.remove();
            }
        }
        return integrationColumns;
    }

    public IntegrationColumn getBlankIntegrationColumn() {
        return new IntegrationColumn(ColumnType.DISPLAY);
    }

    public void setPivotData(Map<List<OntologyNode>, Map<DataTable, Integer>> pivotData) {
        this.pivotData = pivotData;
    }

    public Map<List<OntologyNode>, Map<DataTable, Integer>> getPivotData() {
        return pivotData;
    }
}
