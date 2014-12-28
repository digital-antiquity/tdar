package org.tdar.core.service;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.FileProxy;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.CodingRule;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.VersionType;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.dao.resource.DataTableColumnDao;
import org.tdar.core.dao.resource.OntologyNodeDao;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.integration.IntegrationColumn;
import org.tdar.core.service.integration.IntegrationContext;
import org.tdar.core.service.integration.ModernDataIntegrationWorkbook;
import org.tdar.core.service.integration.ModernIntegrationDataResult;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.filestore.personal.PersonalFilestore;
import org.tdar.utils.Pair;

import au.com.bytecode.opencsv.CSVWriter;

import com.opensymphony.xwork2.TextProvider;

/**
 * $Id$
 * 
 * Provides data integration functionality for datasets that have been translated and mapped to ontologies.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Service
public class DataIntegrationService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TargetDatabase tdarDataImportDatabase;

    @Autowired
    private GenericDao genericDao;

    @Autowired
    private DataTableColumnDao dataTableColumnDao;

    @Autowired
    private PersonalFilestoreService filestoreService;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private InformationResourceService informationResourceService;

    @Autowired
    private OntologyNodeDao ontologyNodeDao;

    @Autowired
    private SerializationService serializationService;

    public void setTdarDataImportDatabase(TargetDatabase tdarDataImportDatabase) {
        this.tdarDataImportDatabase = tdarDataImportDatabase;
    }

    /**
     * sets transient booleans on CodingRule to mark that it's mapped
     * 
     * 
     * @param column
     */
    public void updateMappedCodingRules(DataTableColumn column) {
        if (column == null) {
            return;
        }
        logger.debug("col {}", column);
        CodingSheet codingSheet = column.getDefaultCodingSheet();
        if ((codingSheet == null) || CollectionUtils.isEmpty(codingSheet.getCodingRules())) {
            logger.debug("aborting, no coding rules or coding sheet {}", codingSheet);
            return;
        }
        logger.info("selecting distinct values from column");
        List<String> values = tdarDataImportDatabase.selectDistinctValues(column);
        logger.info("values: {} ", values);
        logger.info("matching coding rule terms to column values");
        for (CodingRule rule : codingSheet.getCodingRules()) {
            if (values.contains(rule.getTerm())) {
                logger.debug("mapping rule {} to column {}", rule, column);
                rule.setMappedToData(column);
            }
        }
    }

    private void hydrateIntegrationColumn(IntegrationColumn integrationColumn) {
        List<DataTableColumn> dataTableColumns = genericDao.loadFromSparseEntities(integrationColumn.getColumns(), DataTableColumn.class);
        dataTableColumns.removeAll(Collections.singletonList(null));
        integrationColumn.setColumns(dataTableColumns);
        List<OntologyNode> filteredOntologyNodes = integrationColumn.getFilteredOntologyNodes();
        if (CollectionUtils.isNotEmpty(filteredOntologyNodes)) {
            filteredOntologyNodes.removeAll(Collections.singletonList(null));
        }

        logger.debug("before: {} - {}", integrationColumn, filteredOntologyNodes);
        filteredOntologyNodes = genericDao.loadFromSparseEntities(filteredOntologyNodes, OntologyNode.class);
        integrationColumn.setFilteredOntologyNodes(filteredOntologyNodes);
        // for each of the integration columns, grab the unique set of all children within an ontology

        // that is, even if child is not selected, should get all children for query and pull up

        integrationColumn.buildNodeChildHierarchy(ontologyNodeDao);

        logger.debug("after: {} - {}", integrationColumn, filteredOntologyNodes);
        logger.info("integration column: {}", integrationColumn);
    }

    /**
     * Convert the integration context to XML for persistance in the @link PersonalFilestore and logging
     * 
     * @param integrationColumns
     * @param creator
     * @return
     * @throws Exception
     */
    public String serializeIntegrationContext(List<IntegrationColumn> integrationColumns, TdarUser creator) throws Exception {
        StringWriter sw = new StringWriter();
        serializationService.convertToXML(new IntegrationContext(creator, integrationColumns), sw);
        return sw.toString();
    }

    /**
     * For a specified @link DataTableColumn, find @link CodingRule entires mapped to the @link CodingSheet and Column that actually have data in the tdardata
     * database.
     * 
     * @param column
     * @return
     */
    @Transactional
    public List<CodingRule> findMappedCodingRules(DataTableColumn column) {
        List<String> distinctColumnValues = tdarDataImportDatabase.selectNonNullDistinctValues(column);
        return dataTableColumnDao.findMappedCodingRules(column, distinctColumnValues);
    }

    /**
     * @see #createGeneratedCodingSheet(DataTableColumn, Person, Ontology)
     * 
     * @param column
     * @param submitter
     * @return
     */
    @Transactional
    public CodingSheet createGeneratedCodingSheet(TextProvider provider, DataTableColumn column, TdarUser submitter) {
        return createGeneratedCodingSheet(provider, column, submitter, column.getDefaultOntology());
    }

    /**
     * When a user maps a @link DataTableColumn to an @link Ontology without a @link CodingSheet specifically chosen, create one on-the-fly from the @link
     * OntologyNode values.
     * 
     * @param column
     * @param submitter
     * @param ontology
     * @return
     */
    @Transactional
    public CodingSheet createGeneratedCodingSheet(TextProvider provider, DataTableColumn column, TdarUser submitter, Ontology ontology) {
        if (column == null) {
            logger.debug("{} tried to create an identity coding sheet for {} with no values", submitter, column);
        }
        CodingSheet codingSheet = new CodingSheet();
        codingSheet.setGenerated(true);
        codingSheet.setAccount(column.getDataTable().getDataset().getAccount());
        codingSheet.setTitle(provider.getText("dataIntegrationService.generated_coding_sheet_title", Arrays.asList(column.getDisplayName())));
        codingSheet.markUpdated(submitter);
        codingSheet.setDate(Calendar.getInstance().get(Calendar.YEAR));
        codingSheet.setDefaultOntology(ontology);
        codingSheet.setCategoryVariable(ontology.getCategoryVariable());
        codingSheet.setDescription(provider.getText(
                "dataIntegrationService.generated_coding_sheet_description",
                Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym(), column, column.getDataTable().getDataset().getTitle(),
                        column.getDataTable().getDataset().getId(), codingSheet.getDateCreated())));
        genericDao.save(codingSheet);
        // generate identity coding rules
        List<String> dataColumnValues = tdarDataImportDatabase.selectNonNullDistinctValues(column);
        Set<CodingRule> rules = new HashSet<>();
        for (int index = 0; index < dataColumnValues.size(); index++) {
            String dataValue = dataColumnValues.get(index);
            CodingRule rule = new CodingRule(codingSheet, dataValue);
            rules.add(rule);
        }
        genericDao.save(rules);
        try {
            String baseFileName = codingSheet.getTitle().replace(" ", "_");
            String csvText = convertCodingSheetToCSV(codingSheet, rules);
            FileProxy fileProxy = new FileProxy(baseFileName + ".csv", FileProxy.createTempFileFromString(csvText), VersionType.UPLOADED);
            fileProxy.addVersion(new FileProxy(baseFileName + ".txt", FileProxy.createTempFileFromString(csvText), VersionType.UPLOADED_TEXT));

            informationResourceService.processMetadataForFileProxies(codingSheet, fileProxy);
        } catch (Exception e) {
            logger.debug("could not process coding sheet", e);
        }

        return codingSheet;
    }

    /**
     * @see #convertCodingSheetToCSV(CodingSheet, Collection)
     * 
     * @param sheet
     * @return
     */
    public String convertCodingSheetToCSV(CodingSheet sheet) {
        return convertCodingSheetToCSV(sheet, sheet.getCodingRules());
    }

    /**
     * Given a @link CodingSheet and a set of @link CodingRule entries, create a CSV File
     * 
     * @param sheet
     * @param rules
     * @return
     */
    public String convertCodingSheetToCSV(CodingSheet sheet, Collection<CodingRule> rules) {
        // not all coding sheets have their rules directly attached at the moment (eg the generated ones)
        StringWriter sw = new StringWriter();
        CSVWriter writer = new CSVWriter(sw);
        for (CodingRule rule : rules) {
            writer.writeNext(new String[] { rule.getCode(), rule.getTerm(), rule.getDescription() });
        }
        IOUtils.closeQuietly(writer);
        return sw.toString();
    }

    /**
     * Iterate over every {@link DataTableColumn} in every {@link DataTable} and find ones that have shared {@link Ontology} entries. Return those back in Lists
     * of Lists.
     * 
     * @param selectedDataTables
     * @return
     */
    public List<List<DataTableColumn>> getIntegrationColumnSuggestions(Collection<DataTable> selectedDataTables) {
        // iterate through all of the columns and get a map of the ones associated
        // with any ontology.
        HashMap<Ontology, List<DataTableColumn>> dataTableAutoMap = new HashMap<>();

        for (DataTable table : selectedDataTables) {
            List<DataTableColumn> dataTableColumns;

            // FIXME: not sure if this is correct
            if (TdarConfiguration.getInstance().getLeftJoinDataIntegrationFeatureEnabled()) {
                dataTableColumns = table.getLeftJoinColumns();
            } else {
                dataTableColumns = table.getDataTableColumns();
            }
            for (DataTableColumn column : dataTableColumns) {
                Ontology ontology = column.getDefaultOntology();
                if (ontology != null) {
                    List<DataTableColumn> columns = dataTableAutoMap.get(ontology);
                    if (columns == null) {
                        columns = new ArrayList<>();
                        dataTableAutoMap.put(ontology, columns);
                    }
                    columns.add(column);
                }
            }
        }

        // okay now we have a map of the data table columns,
        List<List<DataTableColumn>> columnAutoList = new ArrayList<>();
        for (Ontology key : dataTableAutoMap.keySet()) {
            Pair<ArrayList<Long>, ArrayList<DataTableColumn>> set1 = new Pair<>(new ArrayList<Long>(), new ArrayList<DataTableColumn>());
            Pair<ArrayList<Long>, ArrayList<DataTableColumn>> set2 = new Pair<>(new ArrayList<Long>(), new ArrayList<DataTableColumn>());

            // go through the hashMap and try and pair out by set of rules assuming that there is one column per table at a time
            // and there might be a case where there are more than one
            for (DataTableColumn column : dataTableAutoMap.get(key)) {
                Long dataTableIld = column.getDataTable().getId();
                if (!set1.getFirst().contains(dataTableIld)) {
                    set1.getSecond().add(column);
                    set1.getFirst().add(dataTableIld);
                } else if (!set2.getFirst().contains(dataTableIld)) {
                    set2.getSecond().add(column);
                    set2.getFirst().add(dataTableIld);
                } // give up
            }

            // might want to tune this to some logic like:
            // if just one table, then anything with an ontology if more than one, just show lists with at least two ontologies
            if (set1.getSecond().size() > 0) {
                columnAutoList.add(set1.getSecond());
            }
            if (set2.getSecond().size() > 0) {
                columnAutoList.add(set2.getSecond());
            }
        }
        return columnAutoList;
    }

    public Map<Ontology, List<DataTable>> getIntegrationSuggestions(Collection<DataTable> bookmarkedDataTables, boolean showOnlyShared) {
        HashMap<Ontology, List<DataTable>> allOntologies = new HashMap<>();
        if (CollectionUtils.isEmpty(bookmarkedDataTables)) {
            return Collections.emptyMap();
        }
        for (DataTable table : bookmarkedDataTables) {
            for (DataTableColumn column : table.getDataTableColumns()) {
                Ontology ontology = column.getMappedOntology();
                if (ontology != null) {
                    List<DataTable> values = allOntologies.get(ontology);
                    if (values == null) {
                        values = new ArrayList<>();
                    }
                    values.add(table);
                    allOntologies.put(ontology, values);
                }
            }
        }
        if (showOnlyShared) {
            HashMap<Ontology, List<DataTable>> toReturn = new HashMap<>();
            for (Entry<Ontology, List<DataTable>> entry : allOntologies.entrySet()) {
                if (entry.getValue().size() == bookmarkedDataTables.size()) {
                    toReturn.put(entry.getKey(), entry.getValue());
                }
            }
            return toReturn;
        }

        return allOntologies;
    }

    @Transactional(readOnly = false)
    public void getColumnDetails(IntegrationColumn integrationColumn) {
        // rehydrate all of the resources being passed in, we just had empty beans with ids
        integrationColumn.setSharedOntology(genericDao.loadFromSparseEntity(integrationColumn.getSharedOntology(), Ontology.class));
        integrationColumn.setColumns(genericDao.loadFromSparseEntities(integrationColumn.getColumns(), DataTableColumn.class));

        // for each DataTableColumn, grab the shared ontology if it exists; setup mappings
        for (DataTableColumn column : integrationColumn.getColumns()) {
            logger.info("{} ({})", column, column.getDefaultOntology());
            logger.info("{} ({})", column, column.getDefaultCodingSheet());
            updateMappedCodingRules(column);
        }
    }

    /**
     * Compute the node participation for the DataTableColumns described by the specified list of ID's. The method
     * returns the results in a map of (flattened) OntologyNode lists by DataTableColumn.
     *
     * @param dataTableColumnIds
     * @return
     */
    @Transactional(readOnly = true)
    public Map<DataTableColumn, List<OntologyNode>> getNodeParticipationByColumn(List<Long> dataTableColumnIds) {
        Map<DataTableColumn, List<OntologyNode>> nodesByColumn = new HashMap<>();
        Map<Ontology, ArrayList<DataTableColumn>> columnsByOntology = new HashMap<>();

        // First, get a set of distinct ontologies
        List<DataTableColumn> dataTableColumns = genericDao.findAll(DataTableColumn.class, dataTableColumnIds);

        for (DataTableColumn dataTableColumn : dataTableColumns) {
            nodesByColumn.put(dataTableColumn, new ArrayList<OntologyNode>());
            Ontology mappedOntology = dataTableColumn.getDefaultOntology();

            if (mappedOntology == null) {
                continue;
            }

            ArrayList<DataTableColumn> columns = columnsByOntology.get(mappedOntology);
            if (columns == null) {
                columns = new ArrayList<>();
                columnsByOntology.put(mappedOntology, columns);
            }
            columns.add(dataTableColumn);
        }

        // so now we can start making Integration Columns.
        List<IntegrationColumn> integrationColumns = new ArrayList<>();
        for (Ontology ontology : columnsByOntology.keySet()) {
            logger.debug("ontology: {}", ontology);
            IntegrationColumn integrationColumn = new IntegrationColumn(ontology, columnsByOntology.get(ontology));
            getColumnDetails(integrationColumn);
            integrationColumns.add(integrationColumn);

            // Basically we are transposing flattened node list; Instead of a list of nodes with column presence info,
            // we are making a map of columns with node presence info.
            for (OntologyNode node : integrationColumn.getFlattenedOntologyNodeList()) {
                Map<DataTableColumn, Boolean> columnHasValueMap = node.getColumnHasValueMap();
                for (DataTableColumn column : integrationColumn.getColumns()) {
                    if (columnHasValueMap.containsKey(column) && columnHasValueMap.get(column) == Boolean.TRUE) {
                        List<OntologyNode> ontologyNodes = nodesByColumn.get(column);
                        ontologyNodes.add(node);
                    }
                }
            }
        }
        return nodesByColumn;
    }

    @Transactional
    public ModernIntegrationDataResult generateModernIntegrationResult(IntegrationContext context, TextProvider provider) {
        context.setDataTables(genericDao.loadFromSparseEntities(context.getDataTables(), DataTable.class));
        for (IntegrationColumn integrationColumn : context.getIntegrationColumns()) {
            hydrateIntegrationColumn(integrationColumn);
        }

        ModernIntegrationDataResult result = tdarDataImportDatabase.generateIntegrationResult(context, provider, excelService);
        return result;
    }

    @Transactional
    public PersonalFilestoreTicket storeResult(ModernIntegrationDataResult result) {
        ModernDataIntegrationWorkbook workbook = result.getWorkbook();
        PersonalFilestoreTicket ticket = workbook.getTicket();
        genericDao.save(ticket);

        try {
            File resultFile = workbook.writeToTempFile();
            PersonalFilestore filestore = filestoreService.getPersonalFilestore(result.getIntegrationContext().getCreator());
            filestore.store(ticket, resultFile, workbook.getFileName());
        } catch (Exception exception) {
            logger.error("an error occurred when producing the integration excel file", exception);
            throw new TdarRecoverableRuntimeException("dataIntegrationService.could_not_save_file");
        }

        return ticket;
    }
}
