package org.tdar.struts.action;

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
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.DataValueOntologyNodeMapping;
import org.tdar.core.bean.resource.Dataset;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.bean.resource.dataTable.DataTableColumnEncodingType;
import org.tdar.core.bean.resource.dataTable.MeasurementUnit;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.OntologyService;
import org.tdar.transform.DcTransformer;
import org.tdar.transform.ModsTransformer;

/**
 * $Id$
 * <p>
 * Manages requests to create/delete/edit a Dataset and its associated metadata.
 * 
 * This class is getting fairly big due to the complexity of column metadata registration, coding sheet translation, and column ontology mapping. Consider
 * extracting some functionality into a separate URL space and controller class.
 * 
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

    private Integer datasetFormatId;

    private String datasetAvailability;

    // column metadata incoming data
    // Each list contains some specific piece of metadata for the given data table, where
    // the index of the list maps to the ordering of the column names
    private DataTable dataTable;
    private List<DataTableColumnEncodingType> columnEncodingTypes = new ArrayList<DataTableColumnEncodingType>();
    private List<MeasurementUnit> measurementUnits = new ArrayList<MeasurementUnit>();
    private List<String> columnDescriptions = new ArrayList<String>();
    private List<Long> categoryVariableIds = new ArrayList<Long>();
    private List<Long> subcategoryIds = new ArrayList<Long>();
    private List<Long> codingSheetIds = new ArrayList<Long>();
    private List<Long> ontologyIds = new ArrayList<Long>();

    private List<CodingSheet> allCodingSheets;
    private List<Ontology> allOntologies;
    private List<List<CategoryVariable>> subcategories = new ArrayList<List<CategoryVariable>>();

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

    @Autowired
    private transient ModsTransformer.DatasetTransformer datasetModsTransformer;
    @Autowired
    private transient DcTransformer.DatasetTransformer datasetDcTransformer;

    private List<String> ontologyNames = new ArrayList<String>();
    private List<String> codingSheetNames = new ArrayList<String>();

    private Long dataTableId;

    @Actions({ @Action("citations") })
    @Override
    public String execute() {
        if (isNullOrNewResource()) {
            return REDIRECT_HOME;
        }
        return SUCCESS;
    }

    @Action(value = "column-ontology", results = { @Result(name = SUCCESS, location = "column-ontology.ftl"), @Result(name = INPUT, location = "view.ftl") })
    public String loadOntologyMappedColumns() {
        getLogger().debug("loading ontology mapped columns for " + resource);
        DataTableColumn column = getDataTableColumn();
        if (column == null) {
            addActionError("No ontologies have been associated with data table columns in this dataset.");
            return INPUT;
        }
        if (isNullOrNewResource()) {
            return REDIRECT_HOME;
        }
        Ontology ontology = column.getDefaultOntology();
        ontologyNodes = ontology.getOntologyNodes();
        logger.debug("{}", ontologyNodes);
        if (CollectionUtils.isEmpty(ontologyNodes)) {
            getLogger().debug("ontology " + ontology + " did not have any nodes, trying to shred again.");
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
        return SUCCESS;
    }

    @Action(value = "save-data-ontology-mapping", results = {
            @Result(name = "redirect-view", type = "redirect", location = "view?resourceId=${resource.id}"),
            @Result(name = SUCCESS, type = "redirect", location = "column-ontology", params = { "columnId", "${nextColumnId}" }),
            @Result(name = INPUT, location = "column-ontology.ftl") })
    public String saveDataValueOntologyNodeMapping() {
        if (isNullOrNewResource()) {
            return REDIRECT_HOME;
        }
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
                if (mapping.getOntologyNode().getDataValueOntologyNodeMappings() == null) {
                    mapping.getOntologyNode().setDataValueOntologyNodeMappings(new HashSet<DataValueOntologyNodeMapping>());
                }
                mapping.getOntologyNode().getDataValueOntologyNodeMappings().add(mapping);
            }
            resource.markUpdated(getAuthenticatedUser());
            getDatasetService().save(resource);
            // serialize the saved state to the revision log
            getDatasetService().serializeDataValueOntologyNodeMapping(dataTableColumn, getAuthenticatedUser());

            // logic to redirect to the next ontology mapped data table column after saving the current one.
            List<DataTableColumn> ontologyMappedColumns = getOntologyMappedColumns();

            // set up next column index and roll over to 0 if we reach the end of the list.
            int nextColumnIndex = (ontologyMappedColumns.indexOf(dataTableColumn) + 1) % ontologyMappedColumns.size();
            if (nextColumnIndex == 0)
                return "redirect-view";
            nextColumnId = ontologyMappedColumns.get(nextColumnIndex).getId();
            getLogger().debug("next column id: " + nextColumnId);
        } catch (Throwable tde) {
            logger.error(tde.getMessage(), tde);
            addActionErrorWithException(tde.getMessage(), tde);
            return INPUT;
        }
        return SUCCESS;
    }

    @Action(value = "retranslate", results = { @Result(name = SUCCESS, type = "redirect", location = "view?resourceId=${resource.id}") })
    public String retranslate() {
        if (isNullOrNewResource()) {
            return REDIRECT_HOME;
        }
        for (DataTable table : resource.getDataTables()) {
            for (DataTableColumn column : table.getDataTableColumns()) {
                CodingSheet codingSheet = column.getDefaultCodingSheet();
                if (codingSheet != null) {
                    getDatasetService().translate(column, codingSheet);
                }
            }
        }
        getDatasetService().createTranslatedFile(resource);

        return SUCCESS;
    }

    @Action(value = "save-column-metadata", results = { @Result(name = SUCCESS, type = "redirect", location = "view?resourceId=${resource.id}"),
            @Result(name = "match", type = "redirect", location = "column-ontology?resourceId=${resource.id}"),
            @Result(name = INPUT, location = "edit-column-metadata.ftl") })
    public String saveColumnMetadata() {
        if (isNullOrNewResource()) {
            return REDIRECT_HOME;
        }
        try {
            int columnIndex = 0;
            boolean translateFileOutOfSync = false;
            boolean hasOntologies = false;
            for (DataTableColumn column : getDataTable().getSortedDataTableColumns()) {
                String description = columnDescriptions.get(columnIndex);
                Long categoryVariableId = categoryVariableIds.get(columnIndex);
                DataTableColumnEncodingType columnEncodingType = columnEncodingTypes.get(columnIndex);
                MeasurementUnit measurementUnit = measurementUnits.get(columnIndex);
                Long subcategoryVariableId = subcategoryIds.get(columnIndex);

                // the form hides the ontology/coding sheet mappings for incompatible column encoding types so we ignore any values from the form for
                // incompatible
                // encoding types under the assumption that the user meant for these values to be 'cleared' s
                Long ontologyId = null;
                Long codingSheetId = null;
                if (columnEncodingType != null && columnEncodingType.isSupportsOntology()) {
                    ontologyId = ontologyIds.get(columnIndex);
                }
                if (columnEncodingType != null && columnEncodingType.isSupportsCodingSheet()) {
                    codingSheetId = codingSheetIds.get(columnIndex);
                }

                Ontology ontology = getOntologyService().find(ontologyId);
                if (ontology != null) {
                    hasOntologies = true;
                }
                column.setDefaultOntology(ontology);

                // check if incoming coding sheet is already this column's default coding sheet.
                CodingSheet defaultCodingSheet = column.getDefaultCodingSheet();
                CodingSheet codingSheet = getCodingSheetService().find(codingSheetId);
                // only translate the column if its existing default coding sheet is null or if its default coding sheet's id is different from the
                // incoming coding sheet's id.
                if (defaultCodingSheet == null || !defaultCodingSheet.getId().equals(codingSheetId)) {
                    if (getDatasetService().translate(column, codingSheet)) {
                        translateFileOutOfSync = true;
                    }
                } else {
                    getLogger().debug("Not translating column: " + column + " - has same default coding sheet: " + codingSheet);
                }

                // a column had a coding sheet but now it shouldn't, so tell the db
                // service to 'untranslate' it.
                if (defaultCodingSheet != null && codingSheet == null) {
                    getDatasetService().untranslate(column);
                    translateFileOutOfSync = true;
                }

                column.setDefaultCodingSheet(codingSheet);

                // and how will people provide alternative mappings for this data table? create resource relationship between coding sheets and ontologies and
                // this
                // data table column? that's brittle still...
                column.setColumnEncodingType(columnEncodingType);
                column.setMeasurementUnit(measurementUnit);
                column.setDescription(description);
                CategoryVariable categoryVariable = getCategoryVariableService().find(subcategoryVariableId);
                if (categoryVariable == null) {
                    categoryVariable = getCategoryVariableService().find(categoryVariableId);
                }
                column.setCategoryVariable(categoryVariable);
                logger.trace("{}", column);
                getDataTableService().update(column);
                columnIndex++;
            }
            resource.markUpdated(getAuthenticatedUser());
            getDatasetService().save(resource);

            if (translateFileOutOfSync) {
                // create the translation file for this dataset.
                logger.debug("creating translated file");
                getDatasetService().createTranslatedFile(resource);

            }
            getDatasetService().logDataTableColumns(getDataTable(),
                    "data column metadata registration - " + (translateFileOutOfSync ? "translated" : "no translation"), getAuthenticatedUser());
            if (hasOntologies) {
                return "match";
            }

        } catch (Throwable tde) {
            logger.error(tde.getMessage(), tde);
            addActionErrorWithException(tde.getMessage(), tde);
            return INPUT;
        }

        return SUCCESS;
    }

    @Action(value = "columns", results = { @Result(name = SUCCESS, location = "edit-column-metadata.ftl") })
    public String editColumnMetadata() {
        if (isNullOrNewResource()) {
            logger.warn("Trying to map column metadata but resource was null.");
            return REDIRECT_HOME;
        }

        if (resource.getLatestVersions().size() == 0) {
            addActionError("You should upload a data file before attempting to register column metadata.");
            return INPUT;
        }

        if (CollectionUtils.isEmpty(resource.getDataTables())) {
            addActionError("No data tables were found for this resource.");
            return INPUT;
        }
        // load existing column metadata if any.
        DataTable currentDataTable = getDataTable();
        for (DataTableColumn column : currentDataTable.getSortedDataTableColumns()) {
            CategoryVariable categoryVariable = column.getCategoryVariable();
            // category variables are slightly complicated.
            // if there is no category variable specified, easy, just set null on both ids.
            if (categoryVariable == null) {
                categoryVariableIds.add(null);
                subcategoryIds.add(null);
                subcategories.add(null);
            } else {
                // if there is a category variable, check to see if it has a parent.
                Long rootParentId = categoryVariable.getRootParentId();
                Long categoryVariableId = categoryVariable.getId();
                if (rootParentId == null) {
                    categoryVariableIds.add(categoryVariableId);
                    subcategoryIds.add(null);
                    // make sure that the subcategories get populated with the
                    // children of the parent even though none were selected.
                    subcategories.add(getCategoryVariableService().findAllSubcategories(categoryVariableId));
                } else {
                    categoryVariableIds.add(rootParentId);
                    subcategoryIds.add(categoryVariableId);
                    subcategories.add(getCategoryVariableService().findAllSubcategories(rootParentId));
                }
            }
            measurementUnits.add(column.getMeasurementUnit());
            columnEncodingTypes.add(column.getColumnEncodingType());

            addId(codingSheetIds, column.getDefaultCodingSheet());
            addId(ontologyIds, column.getDefaultOntology());
            // columnConfidentials.add( column.isConfidential() ? "Yes" : "No");
            columnDescriptions.add(column.getDescription());

            Ontology ontology = column.getDefaultOntology();
            if (ontology != null) {
                ontologyNames.add(ontology.getTitle() + "(tDAR ID:" + ontology.getId() + ")");
            } else {
                ontologyNames.add("");
            }
            CodingSheet codingSheet = column.getDefaultCodingSheet();
            if (codingSheet != null) {
                codingSheetNames.add(codingSheet.getTitle() + " (tDAR ID:" + codingSheet.getId() + ")");
            } else {
                codingSheetNames.add("");
            }

        }
        getLogger().debug("passing off to Freemarker");
        return SUCCESS;
    }

    private void addId(List<Long> ids, Persistable persistable) {
        ids.add((persistable == null) ? null : persistable.getId());
    }

    protected void processUploadedFile() throws IOException {
        InformationResourceFile datasetFile = resource.getFirstInformationResourceFile();
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
            getLogger().error("Couldn't convert data file with id:" + datasetFile.getId(), exception);
            // addActionError(exception.getMessage());
            throw exception;
        }
        getDatasetService().saveOrUpdate(resource);
    }

    @Override
    protected void loadCustomMetadata() {
        loadInformationResourceProperties();
        if (!resource.getInformationResourceFiles().isEmpty()) {
            setConfidential(resource.getInformationResourceFiles().iterator().next().isConfidential());
        }
    }

    @Override
    protected Dataset createResource() {
        return new Dataset();
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
        logger.debug(getProjectAsJson());
        handleUploadedFiles();
        return SUCCESS;
    }

    @Override
    protected Dataset loadResourceFromId(Long datasetId) {
        Dataset dataset = getDatasetService().find(datasetId);
        if (dataset != null) {
            setProject(dataset.getProject());
        }
        return dataset;
    }

    public Dataset getDataset() {
        return resource;
    }

    public void setDataset(Dataset dataset) {
        this.resource = dataset;
    }

    public String getDatasetAvailability() {
        return datasetAvailability;
    }

    public void setDatasetAvailability(String datasetAvailability) {
        this.datasetAvailability = datasetAvailability;
    }

    public Integer getDatasetFormatId() {
        return datasetFormatId;
    }

    public void setDatasetFormatId(Integer datasetFormatId) {
        this.datasetFormatId = datasetFormatId;
    }

    // Outgoing data
    public List<Dataset> getAllSubmittedDatasets() {
        return getDatasetService().findBySubmitter(getAuthenticatedUser());
    }

    public List<CodingSheet> getAllCodingSheets() {
        if (allCodingSheets == null) {
            allCodingSheets = getCodingSheetService().findSparseCodingSheetListBySubmitter(getAuthenticatedUser());
        }
        return allCodingSheets;
    }

    public List<Ontology> getAllOntologies() {
        if (allOntologies == null) {
            // abrin 2010-08-19: changed findBySubmitter( getAuthenticatedUser()
            // ); to findAll() to allow all users
            // to see all ontologies.
            // TODO: this is a temporary fix, but it should show all 'public'
            // ontologies, or find a better way to
            // handle shared access so that this can better integrate into tDAR
            allOntologies = getOntologyService().findSparseOntologyListBySubmitter(getAuthenticatedUser());
        }
        return allOntologies;
    }

    public List<DataTableColumn> getOntologyMappedColumns() {
        if (ontologyMappedColumns == null) {
            ontologyMappedColumns = getDataTableService().findOntologyMappedColumns(getDataset());
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
                Set<DataTable> dataTables = getDataset().getDataTables();
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
            setResourceId(dataTableColumn.getDataTable().getDataset().getId());
        }
    }

    public List<DataTableColumnEncodingType> getColumnEncodingTypes() {
        return columnEncodingTypes;
    }

    public void setColumnEncodingTypes(List<DataTableColumnEncodingType> columnEncodingTypes) {
        this.columnEncodingTypes = columnEncodingTypes;
    }

    public List<String> getColumnDescriptions() {
        return columnDescriptions;
    }

    public void setColumnDescriptions(List<String> columnDescriptions) {
        this.columnDescriptions = columnDescriptions;
    }

    public List<Long> getCategoryVariableIds() {
        return categoryVariableIds;
    }

    public void setCategoryVariableIds(List<Long> categoryVariableIds) {
        this.categoryVariableIds = categoryVariableIds;
    }

    public List<Long> getSubcategoryIds() {
        return subcategoryIds;
    }

    public void setSubcategoryIds(List<Long> subcategoryIds) {
        this.subcategoryIds = subcategoryIds;
    }

    public void setMeasurementUnits(List<MeasurementUnit> measurementUnits) {
        this.measurementUnits = measurementUnits;
    }

    public List<MeasurementUnit> getMeasurementUnits() {
        return measurementUnits;
    }

    public List<Long> getCodingSheetIds() {
        return codingSheetIds;
    }

    public void setCodingSheetIds(List<Long> codingSheetIds) {
        this.codingSheetIds = codingSheetIds;
    }

    public List<Long> getOntologyIds() {
        return ontologyIds;
    }

    public void setOntologyIds(List<Long> ontologyIds) {
        this.ontologyIds = ontologyIds;
    }

    public List<List<CategoryVariable>> getSubcategories() {
        return subcategories;
    }

    public String getContentDisposition() {
        return String.format("filename=\"dataset_%s.xls\"", getDataset().getId());
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
        return getDatasetService().canLinkDataToOntology(getDataset());
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

    @Override
    public DcTransformer<Dataset> getDcTransformer() {
        return datasetDcTransformer;
    }

    @Override
    public ModsTransformer<Dataset> getModsTransformer() {
        return datasetModsTransformer;
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
}
