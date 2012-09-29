package org.tdar.struts.action.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.resource.CategoryType;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.DataValueOntologyNodeMapping;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFile.FileAction;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.bean.resource.dataTable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.dataTable.MeasurementUnit;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.external.auth.InternalTdarRights;
import org.tdar.core.service.resource.OntologyService;
import org.tdar.struts.WriteableSession;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.FileProxy;

/**
 * $Id$
 * <p>
 * Manages CRUD requests for Dataset metadata including column-level metadata that enables translation of a column via a CodingSheet, mapping of
 * column data values to nodes within an ontology, and association of individual rows within a table to Resources within tdar (e.g., a Mimbres image database).
 * </p>
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Component
@Scope("prototype")
@Namespace("/dataset")
public class DatasetController extends AbstractInformationResourceController<Dataset> {

    private static final long serialVersionUID = 2874916865886637108L;

    public static final String SAVE_VIEW = "SAVE_VIEW";
    public static final String SAVE_MAP_THIS = "SAVE_MAP_THIS";
    public static final String SAVE_MAP_NEXT = "SAVE_MAP_NEXT";

    public enum PostSaveColumnMapActions implements HasLabel {
        SAVE_VIEW("Save, and go to the view page", "Save, and go to the view page"),
        SAVE_MAP_THIS("Save, and continue to edit this page", "Save, and continue to edit this page"),
        SAVE_MAP_NEXT("Save, and go to ontology mapping", "Save, and go to next column mapping");

        private String label;
        private String ontologyLabel;

        private PostSaveColumnMapActions(String label, String ontologyLabel) {
            this.setLabel(label);
            this.setOntologyLabel(ontologyLabel);
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getOntologyLabel() {
            return ontologyLabel;
        }

        public void setOntologyLabel(String ontologyLabel) {
            this.ontologyLabel = ontologyLabel;
        }
    }

    // column metadata incoming data
    // Each list contains some specific piece of metadata for the given data table, where
    // the index of the list maps to the ordering of the column names
    private DataTable dataTable;
    private List<DataTableColumn> dataTableColumns;

    private List<List<CategoryVariable>> subcategories = new ArrayList<List<CategoryVariable>>();
    private PostSaveColumnMapActions postSaveAction = PostSaveColumnMapActions.SAVE_VIEW;
    // ontology mapped columns
    private DataTableColumn dataTableColumn;
    // incoming data
    private List<String> dataColumnValues = new ArrayList<String>();
    private List<Long> ontologyNodeIds = new ArrayList<Long>();
    private List<String> ontologyNodeNames = new ArrayList<String>();

    // stores the next data table column's ontology mappings to visit within this dataset when mapping data column values to ontology nodes.
    // i.e., a cursor into ontologyMappedColumns
    private Long nextColumnId;

    // outgoing ontology-related data
    private List<DataTableColumn> ontologyMappedColumns;
    private List<String> ontologyMappedColumnStatus;

    private List<OntologyNode> ontologyNodes;
    private List<String> distinctColumnValues;

    private SortedMap<String, List<OntologyNode>> suggestions;

    private List<String> ontologyNames = new ArrayList<String>();
    private List<String> codingSheetNames = new ArrayList<String>();

    private Long dataTableId;

    @Actions({ @Action("citations") })
    @Override
    public String execute() {
        if (isNullOrNew()) {
            return REDIRECT_HOME;
        }
        return SUCCESS;
    }

    @SkipValidation
    @Action(value = "column-ontology", results = {
            @Result(name = SUCCESS, location = "column-ontology.ftl"),
            @Result(name = INPUT, type = "redirect", location = "view?id=${resource.id}")
    })
    public String loadOntologyMappedColumns() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        getLogger().debug("loading ontology mapped columns for {}", getPersistable());
        DataTableColumn column = getDataTableColumn();
        if (column == null) {
            getLogger().warn("No ontologies have been associated with data table columns in this dataset, redirecting to view");
            return INPUT;
        }
        Ontology ontology = column.getDefaultOntology();
        ontologyNodes = ontology.getOntologyNodes();
        logger.debug("{}", ontologyNodes);
        if (CollectionUtils.isEmpty(ontologyNodes)) {
            getLogger().debug("ontology {} did not have any nodes, trying to shred again.", ontology);
            getOntologyService().shred(ontology);
            ontologyNodes = ontology.getOntologyNodes();
        }
        Map<Long, OntologyNode> ontologyNodeMap = ontology.getIdToNodeMap();
        OntologyService.sortOntologyNodesByImportOrder(ontologyNodes);
        List<String> distinctColumnValues = getDistinctColumnValues();
        // generate suggestions for all distinct column values or only those
        // columns that aren't already mapped?
        logger.debug("{}", distinctColumnValues);
        suggestions = getOntologyService().generateSuggestions(distinctColumnValues, ontologyNodes);
        // load existing ontology mappings
        Map<String, Long> valueToOntologyNodeIdMap = column.getValueToOntologyNodeIdMap();
        // iteration order here is important - use the same iteration order as
        // the suggestions SortedMap, because
        // that's the data structure we'll iterate over in the output template.
        for (String columnValue : suggestions.keySet()) {
            // logger.info(columnValue + " " + suggestions.get(columnValue));
            Long ontologyNodeId = valueToOntologyNodeIdMap.get(columnValue);
            String ontologyNodeName = "";
            try {
                ontologyNodeName = ontologyNodeMap.get(ontologyNodeId).getDisplayName();
            } catch (NullPointerException npe) {
            }
            if (ontologyNodeId == null) {
                // check suggestions
                List<OntologyNode> suggestedOntologyNodes = suggestions.get(columnValue);
                // only if the suggested ontology nodes
                if (!suggestedOntologyNodes.isEmpty() && suggestedOntologyNodes.size() == 1) {
                    // FIXME: we have no default setting here that separates
                    // "unmapped from 'first-run'" therfore disabling
                    // ontologyNodeId = suggestedOntologyNodes.get(0).getId();
                    // ontologyNodeName =
                    // suggestedOntologyNodes.get(0).getLabel();
                }
            }
            logger.trace(" -- " + ontologyNodeId + ":" + ontologyNodeName + " - " + columnValue);
            ontologyNodeIds.add(ontologyNodeId);
            ontologyNodeNames.add(ontologyNodeName);

        }
        // set up next column index and roll over to 0 if we reach the end of the list.
        int nextColumnIndex = (getOntologyMappedColumns().indexOf(column) + 1) % getOntologyMappedColumns().size();
        if (nextColumnIndex > 0) {
            nextColumnId = getOntologyMappedColumns().get(nextColumnIndex).getId();
        }
        return SUCCESS;
    }

    @WriteableSession
    @SkipValidation
    @Action(value = "save-data-ontology-mapping", results = {
            @Result(name = SAVE_VIEW, type = "redirect", location = "view?id=${resource.id}"),
            @Result(name = SAVE_MAP_NEXT, type = "redirect", location = "column-ontology", params = { "columnId", "${nextColumnId}" }),
            @Result(name = SAVE_MAP_THIS, type = "redirect", location = "column-ontology", params = { "columnId", "${columnId}" }),
            @Result(name = INPUT, location = "column-ontology.ftl") })
    public String saveDataValueOntologyNodeMapping() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);

        DataTableColumn dataTableColumn = getDataTableColumn();
        if (dataTableColumn == null) {
            getLogger().warn("No data table column set, this should never happen...");
            addActionError("No data table column set");
            return INPUT;
        }
        try {
            getLogger().debug("saving data value -> ontology node mappings for " + dataTableColumn);
            getLogger().debug(" matching column values: " + dataColumnValues + " - node ids " + ontologyNodeIds);
            ArrayList<DataValueOntologyNodeMapping> mappings = new ArrayList<DataValueOntologyNodeMapping>();
            for (int index = 0; index < dataColumnValues.size(); index++) {
                String dataValue = dataColumnValues.get(index);
                Long ontologyNodeId = ontologyNodeIds.get(index);

                OntologyNode node = getOntologyNodeService().find(ontologyNodeId);
                if (node == null) {
                    getLogger().trace("no OntologyNode mapping specified for data value: " + dataValue);
                    continue;
                }
                DataValueOntologyNodeMapping mapping = new DataValueOntologyNodeMapping();
                mapping.setDataTableColumn(dataTableColumn);
                mapping.setDataValue(dataValue);
                mapping.setOntologyNode(node);
                mappings.add(mapping);
            }
            getDatasetService().delete(dataTableColumn.getValueToOntologyNodeMapping());
            // getOntologyNodeService().save(mappings);
            dataTableColumn.setValueToOntologyNodeMapping(mappings);
            logger.debug("VALUE-TO-ONTOLOGY-MAPPING->" + mappings);
            getDatasetService().save(mappings);
            getDatasetService().save(dataTableColumn);
            // FIXME: this appears needed to make tests pass, but why???
            for (DataValueOntologyNodeMapping mapping : mappings) {
                OntologyNode node = mapping.getOntologyNode();
                if (node.getDataValueOntologyNodeMappings() == null) {
                    node.setDataValueOntologyNodeMappings(new HashSet<DataValueOntologyNodeMapping>());
                }
                mapping.getOntologyNode().getDataValueOntologyNodeMappings().add(mapping);
            }
            getPersistable().markUpdated(getAuthenticatedUser());
            getDatasetService().save(getPersistable());
            // serialize the saved state to the revision log

            getDatasetService().serializeDataValueOntologyNodeMapping(dataTableColumn, getAuthenticatedUser());

            // logic to redirect to the next ontology mapped data table column after saving the current one.
            List<DataTableColumn> ontologyMappedColumns = getOntologyMappedColumns();

            // set up next column index and roll over to 0 if we reach the end of the list.
            int nextColumnIndex = (ontologyMappedColumns.indexOf(dataTableColumn) + 1) % ontologyMappedColumns.size();
            if (nextColumnIndex == 0)
                return SAVE_VIEW;
            nextColumnId = ontologyMappedColumns.get(nextColumnIndex).getId();
            getLogger().debug("next column id: " + nextColumnId);
        } catch (Throwable tde) {
            logger.error(tde.getMessage(), tde);
            addActionErrorWithException(tde.getMessage(), tde);
            return INPUT;
        }

        // FIXME: can we replace this with return getPostSaveAction().getResultName() or something like that?
        // the only thing we lose is the nextColumnId check which we *could* embed in PostSaveActionEnum.getResultName
        // when will nextColumnId == -1?
        switch (getPostSaveAction()) {
            case SAVE_MAP_THIS:
                return SAVE_MAP_THIS;
            case SAVE_MAP_NEXT:
                if (nextColumnId != -1) {
                    return SAVE_MAP_NEXT;
                }
            case SAVE_VIEW:
                return SAVE_VIEW;
        }
        return SAVE_VIEW;
    }

    @Action(value = "reimport", results = { @Result(name = SUCCESS, type = "redirect", location = "view?id=${resource.id}") })
    @WriteableSession
    public String reimport() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        getDatasetService().reprocess(getPersistable());
        return SUCCESS;
    }

    /**
     * Retranslates the given dataset.  
     * XXX: does this need a WritableSession? 
     */
    @Action(value = "retranslate", results = { @Result(name = SUCCESS, type = "redirect", location = "view?id=${resource.id}") })
    @WriteableSession
    public String retranslate() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        for (DataTable table : getPersistable().getDataTables()) {
            getDatasetService().retranslate(table.getDataTableColumns());
        }
        getDatasetService().createTranslatedFile(getPersistable());
        return SUCCESS;
    }

    @SkipValidation
    @WriteableSession
    @Action(value = "save-column-metadata", results = { @Result(name = SUCCESS, type = "redirect", location = "view?id=${resource.id}"),
            @Result(name = SAVE_VIEW, type = "redirect", location = "view?id=${resource.id}"),
            @Result(name = SAVE_MAP_NEXT, type = "redirect", location = "column-ontology?id=${resource.id}"),
            @Result(name = SAVE_MAP_THIS, type = "redirect", location = "columns?id=${resource.id}"),
            @Result(name = INPUT, location = "edit-column-metadata.ftl") })
    public String saveColumnMetadata() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);
        boolean hasOntologies = false;
        try {
            List<DataTableColumn> columnsToTranslate = new ArrayList<DataTableColumn>();
            List<DataTableColumn> columnsToMap = new ArrayList<DataTableColumn>();
            for (DataTableColumn column : dataTableColumns) {
                boolean needToRetranslate = false;
                boolean needToRemap = false;
                logger.debug("{}", column);
                DataTableColumn existing = getDataTable().getColumnById(column.getId());
                if (existing == null) {
                    existing = getDataTable().getColumnByName(column.getName());
                    if (existing == null) {
                        throw new TdarRecoverableRuntimeException(String.format("could not find column named %s with id %s", column.getName(),
                                column.getId()));
                    }
                }
                column.setDefaultCodingSheet(getGenericService().rehydrateSparseIdBean(column.getDefaultCodingSheet(), CodingSheet.class));
                column.setDefaultOntology(getGenericService().rehydrateSparseIdBean(column.getDefaultOntology(), Ontology.class));
                // FIXME: can we simplify this logic? Perhaps push into DataTableColumn?
                // incoming ontology or coding sheet from the web was not null but the column encoding type was set to something that
                // doesn't support either, we set it to null
                // incoming ontology or coding sheet is explicitly set to null
                if (column.getDefaultOntology() != null) {
                    if (column.getColumnEncodingType().isSupportsOntology()) {
                        hasOntologies = true;
                    }
                    else {
                        column.setDefaultOntology(null);
                        logger.debug("setting default ontology to null on column {} (encoding type: {}", column, column.getColumnEncodingType());
                    }
                }
                needToRetranslate = ObjectUtils.notEqual(column.getDefaultCodingSheet(), existing.getDefaultCodingSheet());
                if (column.getDefaultCodingSheet() != null && !column.getColumnEncodingType().isSupportsCodingSheet()) {
                    column.setDefaultCodingSheet(null);
                    logger.debug("setting default coding sheet to null on column {} (encoding type: {})", column, column.getColumnEncodingType());
                }
                existing.setDefaultOntology(column.getDefaultOntology());
                existing.setDefaultCodingSheet(column.getDefaultCodingSheet());

                existing.setCategoryVariable(getGenericService().rehydrateSparseIdBean(column.getCategoryVariable(), CategoryVariable.class));
                CategoryVariable subcategoryVariable = getGenericService().rehydrateSparseIdBean(column.getTempSubCategoryVariable(), CategoryVariable.class);

                if (subcategoryVariable != null) {
                    existing.setCategoryVariable(subcategoryVariable);
                }
                // check if values have changed
                needToRemap = existing.hasDifferentMappingMetadata(column);
                // copy off all of the values that can be directly copied from the bean
                existing.copyUserMetadataFrom(column);
                if (!existing.isValid()) {
                    throw new TdarRecoverableRuntimeException("invalid column: " + existing);
                }
                logger.debug("need to re-translate {}", needToRetranslate);
                logger.debug("need to re-map {}", needToRemap);
                if (needToRemap) {
                    columnsToMap.add(existing);
                }
                // a column had a coding sheet but now it shouldn't, so tell the db
                // service to 'untranslate' it.
                if (needToRetranslate) {
                    columnsToTranslate.add(existing);
                }
                logger.trace("{}", existing);
                getGenericService().update(existing);
            }
            getPersistable().markUpdated(getAuthenticatedUser());
            getDatasetService().save(getPersistable());
            getDatasetService().updateMappings(getPersistable().getProject(), columnsToMap);
            if (!columnsToTranslate.isEmpty()) {
                // create the translation file for this dataset.
                logger.debug("creating translated file");
                getDatasetService().retranslate(columnsToTranslate);
                getDatasetService().createTranslatedFile(getPersistable());
            }
            getDatasetService().logDataTableColumns(getDataTable(), "data column metadata registration", getAuthenticatedUser());
        } catch (Throwable tde) {
            logger.error(tde.getMessage(), tde);
            addActionErrorWithException(tde.getMessage(), tde);
            return INPUT;
        }
        switch (getPostSaveAction()) {
            case SAVE_MAP_THIS:
                return SAVE_MAP_THIS;
            case SAVE_MAP_NEXT:
                if (hasOntologies) {
                    return SAVE_MAP_NEXT;
                }
            case SAVE_VIEW:
                return SAVE_VIEW;
        }
        return SUCCESS;
    }

    @SkipValidation
    @Action(value = "columns", results = { @Result(name = SUCCESS, location = "edit-column-metadata.ftl") })
    public String editColumnMetadata() throws TdarActionException {
        checkValidRequest(RequestType.MODIFY_EXISTING, this, InternalTdarRights.EDIT_ANYTHING);

        if (getPersistable().getLatestVersions().isEmpty()) {
            addActionError("You should upload a data file before attempting to register column metadata.");
            return INPUT;
        }

        if (CollectionUtils.isEmpty(getPersistable().getDataTables())) {
            addActionError("No data tables were found for this resource.");
            return INPUT;
        }
        // load existing column metadata if any.
        DataTable currentDataTable = getDataTable();

        for (DataTableColumn column : currentDataTable.getSortedDataTableColumns()) {
            CategoryVariable categoryVariable = column.getCategoryVariable();
            if (categoryVariable == null) {
                subcategories.add(null);
            } else {
                if (categoryVariable.getType() == CategoryType.CATEGORY) {
                    // make sure that the subcategories get populated with the
                    // children of the parent even though none were selected.
                    subcategories.add(new ArrayList<CategoryVariable>(categoryVariable.getSortedChildren()));
                } else { // category is a subcategory
                    subcategories.add(new ArrayList<CategoryVariable>(categoryVariable.getParent().getSortedChildren()));
                }
            }
        }
        setDataTableColumns(currentDataTable.getSortedDataTableColumns());

        getLogger().debug("passing off to Freemarker");
        return SUCCESS;
    }

    protected void processUploadedFiles(List<InformationResourceFile> uploadedFiles) throws IOException {
        // FIXME: replace with uploadedFiles.get(0) and push logic into the message service?
        InformationResourceFile datasetFile = getPersistable().getFirstInformationResourceFile();
        if (datasetFile == null) {
            getLogger().debug("dataset file is null, nothing to process.");
            return;
        }
        if (datasetFile.isProcessed()) {
            getLogger().debug("dataset file already processed, returning.");
            return;
        }
        try {
            // previously translated files, if any, should have already been removed by the InformationResourceFileService.
            getDatasetService().convertDataFile(datasetFile);
        } catch (TdarRecoverableRuntimeException exception) {
            getLogger().debug("Couldn't convert data file with id:" + datasetFile.getId(), exception);
            getFileProxies().clear(); 
            throw exception;
        }
        getDatasetService().saveOrUpdate(getPersistable());
    }

    @Override
    protected void loadCustomMetadata() {
        loadInformationResourceProperties();
        if (!getPersistable().getInformationResourceFiles().isEmpty()) {
            setConfidential(getPersistable().getInformationResourceFiles().iterator().next().isConfidential());
        }
    }

    @Override
    protected String save(Dataset dataset) {
        getLogger().debug("Saving dataset: {}", dataset);
        // save basic metadata
        super.saveBasicResourceMetadata();

        super.saveInformationResourceProperties();
        getDatasetService().saveOrUpdate(dataset);
        // HACK: implicitly cache fullUsers via call to getProjectAsJson() as workaround for TDAR-1162. This is the software equivalent of turning the radio up
        // to mask weird sounds your engine is making
        // logger.debug(getProjectAsJson());
        handleUploadedFiles();
        boolean fileChanged = false;
        for (FileProxy proxy : getFileProxies()) {
            if (proxy.getAction().equals(FileAction.ADD) || proxy.getAction().equals(FileAction.REPLACE)) {
                fileChanged = true;
            }
        }
        // logger.debug("{}", getFileProxies());
        if (fileChanged) {
            setSaveSuccessPath("columns");
        }
        return SUCCESS;
    }

    public List<Dataset> getAllSubmittedDatasets() {
        return getDatasetService().findBySubmitter(getAuthenticatedUser());
    }

    public List<DataTableColumn> getOntologyMappedColumns() {
        if (ontologyMappedColumns == null) {
            ontologyMappedColumns = getDataTableService().findOntologyMappedColumns(getPersistable());
        }
        return ontologyMappedColumns;
    }

    /**
     * Returns the current data table to be viewed (only by the
     * editColumnMetadata method / columns action). If there is no current data
     * table specified via an incoming dataTableId query parameter, will return
     * the first DataTable in this Dataset's DataTables Set.
     * 
     * @return
     */
    public DataTable getDataTable() {
        logger.trace(dataTable + " dtID:" + dataTableId);
        if (dataTable == null) {
            if (dataTableId != null) {
                this.dataTable = getDataTableService().find(dataTableId);
            } else {
                Set<DataTable> dataTables = getPersistable().getDataTables();
                if (!CollectionUtils.isEmpty(dataTables)) {
                    dataTable = dataTables.iterator().next();
                }
            }
        }
        return dataTable;
    }

    public void setDataTableId(Long dataTableId) {
        if (dataTableId == null || dataTableId == -1L) {
            logger.error("Trying to set data table id to null or -1: " + dataTableId);
            return;
        }
        this.dataTableId = dataTableId;
        // this.dataTable = getDataTableService().find(dataTableId);
    }

    public List<String> getDistinctColumnValues() {
        if (distinctColumnValues == null) {
            loadDistinctColumnValues();
        }
        return distinctColumnValues;
    }

    private void loadDistinctColumnValues() {
        if (getDataTableColumn() == null) {
            distinctColumnValues = Collections.emptyList();
        }
        distinctColumnValues = getDataTableService().findAllDistinctValues(dataTableColumn);
    }

    public DataTableColumn getDataTableColumn() {
        if (dataTableColumn == null) {
            getOntologyMappedColumns();
            if (!CollectionUtils.isEmpty(ontologyMappedColumns)) {
                dataTableColumn = ontologyMappedColumns.get(0);
            }
        }
        return dataTableColumn;
    }

    public void setColumnId(Long columnId) {
        if (columnId == null || columnId == -1L) {
            logger.warn("Trying to set data table column id to null or -1: " + columnId);
            return;
        }
        this.dataTableColumn = getDataTableService().findDataTableColumn(columnId);
        if (dataTableColumn != null && getResource() == null) {
            setId(dataTableColumn.getDataTable().getDataset().getId());
        }
    }

    public List<List<CategoryVariable>> getSubcategories() {
        return subcategories;
    }

    public String getContentDisposition() {
        return String.format("filename=\"dataset_%s.xls\"", getPersistable().getId());
    }

    public List<OntologyNode> getOntologyNodes() {
        return ontologyNodes;
    }

    public void setDataColumnValues(List<String> matchingColumnValues) {
        this.dataColumnValues = matchingColumnValues;
    }

    public void setOntologyNodeIds(List<Long> matchingOntologyNodes) {
        this.ontologyNodeIds = matchingOntologyNodes;
    }

    public boolean isOntologyLinked() {
        return getDatasetService().canLinkDataToOntology(getPersistable());
    }

    public List<String> getDataColumnValues() {
        return dataColumnValues;
    }

    public List<Long> getOntologyNodeIds() {
        return ontologyNodeIds;
    }

    public SortedMap<String, List<OntologyNode>> getSuggestions() {
        return suggestions;
    }

    public Long getNextColumnId() {
        return nextColumnId;
    }

    public List<String> getCodingSheetNames() {
        return codingSheetNames;
    }

    public List<String> getOntologyNames() {
        return ontologyNames;
    }

    public List<String> getOntologyNodeNames() {
        return ontologyNodeNames;
    }

    public void setOntologyNodeNames(List<String> ontologyNodeNames) {
        this.ontologyNodeNames = ontologyNodeNames;
    }

    public List<String> getOntologyMappedColumnStatus() {
        ontologyMappedColumnStatus = new ArrayList<String>();
        for (DataTableColumn column : getOntologyMappedColumns()) {
            String status = "";
            if (getOntologyService().isOntologyMappedToDataTableColumn(column) == 0) {
                status = "unmapped";
            }
            ontologyMappedColumnStatus.add(status);
        }
        logger.trace("{}", ontologyMappedColumnStatus);
        return ontologyMappedColumnStatus;
    }

    @Override
    public Set<String> getValidFileExtensions() {
        return analyzer.getExtensionsForType(ResourceType.DATASET);
    }

    public List<MeasurementUnit> getAllMeasurementUnits() {
        return Arrays.asList(MeasurementUnit.values());
    }

    public List<DataTableColumnEncodingType> getAllColumnEncodingTypes() {
        return Arrays.asList(DataTableColumnEncodingType.values());
    }

    public void setDataset(Dataset dataset) {
        setPersistable(dataset);
    }

    public Dataset getDataset() {
        return getPersistable();
    }

    public Long getDataTableId() {
        return dataTableId;
    }

    public Class<Dataset> getPersistableClass() {
        return Dataset.class;
    }

    public List<DataTableColumn> getDataTableColumns() {
        return dataTableColumns;
    }

    public void setDataTableColumns(List<DataTableColumn> dataTableColumns) {
        this.dataTableColumns = dataTableColumns;
    }

    public PostSaveColumnMapActions getPostSaveAction() {
        return postSaveAction;
    }

    public void setPostSaveAction(PostSaveColumnMapActions postSaveAction) {
        this.postSaveAction = postSaveAction;
    }

    public List<PostSaveColumnMapActions> getAllSaveActions() {
        return Arrays.asList(PostSaveColumnMapActions.values());
    }
}
