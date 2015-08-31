package org.tdar.struts.action.workspace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.SerializationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.integration.ColumnType;
import org.tdar.core.service.integration.DataIntegrationService;
import org.tdar.core.service.integration.IntegrationColumn;
import org.tdar.core.service.integration.IntegrationContext;
import org.tdar.core.service.integration.ModernIntegrationDataResult;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.interceptor.annotation.PostOnly;

import com.opensymphony.xwork2.Preparable;

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
public class LegacyWorkspaceController extends AuthenticationAware.Base implements Preparable {

    private static final long serialVersionUID = -3538370664425794045L;
    @Autowired
    private transient AuthorizationService authorizationService;

    @Autowired
    private transient DataIntegrationService dataIntegrationService;

    @Autowired
    private transient BookmarkedResourceService bookmarkedResourceService;

    @Autowired
    private transient ResourceService resourceService;

    @Autowired
    private transient PersonalFilestoreService filestoreService;

    @Autowired
    private transient SerializationService serializationService;

    private List<Resource> bookmarkedResources;
    private Set<Ontology> sharedOntologies;
    private List<IntegrationColumn> integrationColumns;
    private List<Long> tableIds;
    private List<DataTable> selectedDataTables;
    private ModernIntegrationDataResult result;

    private Long ticketId;

    /**
     * Pass through actions that will go to <action-name>.ftl or <action-name>.jsp
     */
    @Actions({
            @Action(value = "select-tables")
    })
    @Override
    public String execute() {
        Map<Ontology, List<DataTable>> suggestions = dataIntegrationService.getIntegrationSuggestions(getBookmarkedDataTables(), false);
        setSharedOntologies(suggestions.keySet());
        // in the future we could use the Map to prompt the user with suggestions
        return SUCCESS;
    }

    // by default SUCCESS maps to <action-name>.ftl
    @Action(value = "select-columns",
            results = {
                    @Result(name = SUCCESS, location = "select-columns.ftl"),
                    @Result(name = INPUT, location = "select-tables.ftl")
            })
    @PostOnly
    public String selectColumns() {
        // FIXME: do we want to log this step? Perhaps, but there's no resource being modified, and resource parameter isn't nullable.
        if (CollectionUtils.isEmpty(tableIds)) {
            addActionError(getText("workspaceController.selectTables"));
            return INPUT;
        }
        setSharedOntologies(dataIntegrationService.getIntegrationSuggestions(getSelectedDataTables(), true).keySet());
        return SUCCESS;
    }

    // TODO: remove feature toggle when feature complete
    public boolean getLeftJoinDataIntegrationFeatureEnabled() {
        return TdarConfiguration.getInstance().getLeftJoinDataIntegrationFeatureEnabled();
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
    @PostOnly
    public String filterDataValues() {
        try {
            // each column could have its own distinct ontology in the future. at the moment we assume that
            // each pair of columns has a shared common ontology
            getLogger().debug("integration columns: {}", getIntegrationColumns());

            for (IntegrationColumn integrationColumn : getIntegrationColumns()) {
                if (!integrationColumn.isIntegrationColumn()) {
                    continue;
                }

                // rehydrate all of the resources being passed in, we just had empty beans with ids
                List<DataTableColumn> hydrated = getGenericService().loadFromSparseEntities(integrationColumn.getColumns(), DataTableColumn.class);
                hydrated.removeAll(Collections.singletonList(null));
                integrationColumn.setColumns(hydrated);
                getLogger().info("hydrated columns {}", hydrated);
                Ontology defaultOntology = null;

                // for each DataTableColumn, grab the shared ontology if it exists; setup mappings
                for (DataTableColumn column : hydrated) {
                    getLogger().info("{} ({})", column, column.getMappedOntology());
                    getLogger().info("{} ({})", column, column.getDefaultCodingSheet());
                    defaultOntology = column.getMappedOntology();
                    if (defaultOntology != null) {
                        getLogger().debug("default ontology: {}", defaultOntology.getTitle());
                        integrationColumn.setSharedOntology(defaultOntology);
                    }

                    dataIntegrationService.updateMappedCodingRules(column);
                }
                Set<OntologyNode> flattenedNodes = dataIntegrationService.getFilteredOntologyNodes(integrationColumn);
                flattenedNodes.addAll(defaultOntology.getOntologyNodes());
                ArrayList<OntologyNode> sortedList = new ArrayList<OntologyNode>(flattenedNodes);
                Collections.sort(sortedList, Ontology.IMPORT_ORDER_COMPARATOR);
                integrationColumn.setFlattenedOntologyNodeList(sortedList);
            }
            if (getLogger().isTraceEnabled()) {
                getLogger().trace("intermediate: {}",
                        dataIntegrationService.serializeIntegrationContext(getIntegrationColumns(), getGenericService().merge(getAuthenticatedUser())));
            }
        } catch (Exception e) {
            addActionErrorWithException(e.getMessage(), e);
            return INPUT;
        }
        return SUCCESS;
    }

    public String getIntegrationColumnData() {
        String data = "null";
        try {
            data = serializationService.convertToJson(getIntegrationColumns());
        } catch (IOException e) {
            getLogger().warn("failed to generate integration column json", e);
        }
        return data;
    }

    @Action(value = "display-filtered-results",
            results = {
                    @Result(name = SUCCESS, location = "display-filtered-results.ftl"),
                    @Result(name = INPUT, location = "filter.ftl")
            })
    @PostOnly
    public String displayFilteredResults() {
        getLogger().trace("XXX: DISPLAYING FILTERED RESULTS :XXX");
        String integrationContextXml = "";
        try {
            integrationContextXml = dataIntegrationService.serializeIntegrationContext(getIntegrationColumns(),
                    getGenericService().merge(getAuthenticatedUser()));
            resourceService.logResourceModification(null, getAuthenticatedUser(), "display filtered results (payload: tableToDisplayColumns)",
                    integrationContextXml);
        } catch (Exception e) {
            getLogger().error("could not serialize to XML", e);
        }
        getLogger().trace(integrationContextXml);
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
            IntegrationContext context = new IntegrationContext(getAuthenticatedUser(), getIntegrationColumns());
            context.setDataTables(getSelectedDataTables());
            ModernIntegrationDataResult result = dataIntegrationService.generateModernIntegrationResult(context, this);
            PersonalFilestoreTicket ticket = dataIntegrationService.storeResult(result);
            setTicketId(ticket.getId());
            getLogger().debug("result:{}", result);
            setResult(result);
        } catch (Throwable e) {
            addActionErrorWithException(e.getMessage(), e);
            return INPUT;
        }
        return SUCCESS;
    }

    public List<Resource> getBookmarkedResources() {
        if (bookmarkedResources == null) {
            bookmarkedResources = bookmarkedResourceService.findBookmarkedResourcesByPerson(getAuthenticatedUser(),
                    Arrays.asList(Status.ACTIVE, Status.DRAFT));
        }

        for (Resource res : bookmarkedResources) {
            authorizationService.applyTransientViewableFlag(res, getAuthenticatedUser());
        }
        return bookmarkedResources;
    }

    public List<Dataset> getBookmarkedDatasets() {
        List<Dataset> datasets = new ArrayList<Dataset>();
        for (Resource resource : getBookmarkedResources()) {
            if ((resource instanceof Dataset) && resource.isActive()) {
                Dataset dataset = (Dataset) resource;
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
            selectedDataTables = getGenericService().findAll(DataTable.class, tableIds);
        }
        return selectedDataTables;
    }

    public List<List<DataTableColumn>> getDataTableColumnIntegrationSuggestions() {
        return dataIntegrationService.getIntegrationColumnSuggestions(getSelectedDataTables());
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
            integrationColumns = new ArrayList<>();
        }
        return integrationColumns;
    }

    /**
     * Remove null items from the integrationColumns list as well as the integrationColumn.columns lists. As struts populates the object graph for integration
     * columns, it may have introduced null list items.
     */
    private void cleanupIntegrationColumns() {
        Iterator<IntegrationColumn> iterator = getIntegrationColumns().iterator();
        while (iterator.hasNext()) {
            IntegrationColumn column = iterator.next();
            if ((column == null) || (column.getColumns().size() == 0)) {
                getLogger().debug("removing null column");
                iterator.remove();
            } else {
                column.getColumns().removeAll(Collections.singletonList(null));
            }
        }
    }

    public IntegrationColumn getBlankIntegrationColumn() {
        return new IntegrationColumn(ColumnType.DISPLAY);
    }

    public Set<Ontology> getSharedOntologies() {
        return sharedOntologies;
    }

    public void setSharedOntologies(Set<Ontology> sharedOntologies) {
        this.sharedOntologies = sharedOntologies;
    }

    // ensure that the integration columns are cleaned up prior to executing an action
    public void prepare() {
        getLogger().trace("prepare integrationColumns(before):{}", integrationColumns);
        cleanupIntegrationColumns();
        getLogger().trace("prepare integrationColumns (after):{}", integrationColumns);
    }

    public ModernIntegrationDataResult getResult() {
        return result;
    }

    public void setResult(ModernIntegrationDataResult result) {
        this.result = result;
    }

}
