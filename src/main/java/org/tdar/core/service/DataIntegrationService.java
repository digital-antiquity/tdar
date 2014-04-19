package org.tdar.core.service;

import java.io.File;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.Person;
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
import org.tdar.core.service.integration.DataIntegrationWorkbook;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.db.model.abstracts.TargetDatabase;
import org.tdar.filestore.personal.PersonalFilestore;
import org.tdar.struts.data.FileProxy;
import org.tdar.struts.data.IntegrationColumn;
import org.tdar.struts.data.IntegrationContext;
import org.tdar.struts.data.IntegrationDataResult;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.Pair;

import au.com.bytecode.opencsv.CSVWriter;

import com.opensymphony.xwork2.TextProvider;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

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
    private XmlService xmlService;

    public void setTdarDataImportDatabase(TargetDatabase tdarDataImportDatabase) {
        this.tdarDataImportDatabase = tdarDataImportDatabase;
    }

    /**
     * For a specific @link DataTable, perform the DataIntegration just on that @link DataTable
     * 
     * @param table
     * @param integrationColumns
     * @param pivot
     * @return
     */
    public IntegrationDataResult generateIntegrationResult(final DataTable table, final List<IntegrationColumn> integrationColumns,
            final Map<List<OntologyNode>, Map<DataTable, Integer>> pivot) {
        // formulate a SELECT statement
        String selectSql = tdarDataImportDatabase.generateOntologyEnhancedSelect(table, integrationColumns, pivot);

        IntegrationDataResult integrationDataResult = new IntegrationDataResult();
        integrationDataResult.setDataTable(table);
        integrationDataResult.setIntegrationColumns(integrationColumns);
        logger.debug(selectSql);

        List<String[]> rowDataList = new ArrayList<String[]>();
        // if we have a "WHERE clause, then we can actually do something (otherwise, we probably have an empty filter list
        if (selectSql.toLowerCase().contains(" where ")) {
            rowDataList = tdarDataImportDatabase.query(selectSql, new ParameterizedRowMapper<String[]>() {
                @Override
                public String[] mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
                    // grab the data from this result set, populate the IntegrationDataResult

                    // FIXME: create temporary postgres table, populate it with integration results, update it as necessary,
                    // then stream it into the generated excel file

                    ArrayList<String> values = new ArrayList<String>();
                    values.add(table.getDataset().getTitle());
                    ArrayList<OntologyNode> ontologyNodes = new ArrayList<OntologyNode>();
                    int resultSetPosition = 1;
                    for (IntegrationColumn integrationColumn : integrationColumns) {
                        // note SQL iterator is 1 based; java iterator is 0 based
                        DataTableColumn column = table.getColumnByName(resultSet.getMetaData().getColumnName(resultSetPosition));
                        String value = "";
                        if (column != null) { // RAW VALUE
                            value = tdarDataImportDatabase.getResultSetValueAsString(resultSet, resultSetPosition, column);
                        }
                        if ((column != null) && !integrationColumn.isDisplayColumn() && StringUtils.isEmpty(value)) {
                            value = MessageHelper.getMessage("database.null_empty_integration_value");
                        }
                        values.add(value);
                        ontologyNodes.add(OntologyNode.NULL); // initialize the array so we have columns line up
                        if ((column != null) && !integrationColumn.isDisplayColumn()) { // MAPPED VALUE if not display column
                            String mappedVal = null;
                            // FIXME: get the appropriately aggregated OntologyNode for the given value, add a method in DataIntegrationService
                            // OntologyNode mappedOntologyNode = integrationColumn.getMappedOntologyNode(value, column);
                            OntologyNode mappedOntologyNode = getMappedOntologyNode(value, column, integrationColumn);
                            if (mappedOntologyNode != null) {
                                mappedVal = mappedOntologyNode.getDisplayName();
                                ontologyNodes.set(ontologyNodes.size() - 1, mappedOntologyNode);
                            }
                            if (mappedVal == null) {
                                mappedVal = MessageHelper.getMessage("database.null_empty_mapped_value");
                            }
                            values.add(mappedVal);
                        }
                        resultSetPosition++;
                    }
                    if (pivot.get(ontologyNodes) == null) {
                        pivot.put(ontologyNodes, new HashMap<DataTable, Integer>());
                    }
                    Integer groupCount = pivot.get(ontologyNodes).get(table);
                    if (groupCount == null) {
                        pivot.get(ontologyNodes).put(table, 0);
                        groupCount = 0;
                    }
                    pivot.get(ontologyNodes).put(table, groupCount + 1);
                    return values.toArray(new String[0]);
                }
            });
        }
        integrationDataResult.setRowData(rowDataList);
        return integrationDataResult;
    }

    /**
     * sets transient boolean on CodingRule to mark that it's mapped
     * 
     * @param column
     */
    public void updateMappedCodingRules(DataTableColumn column) {
        if (column == null) {
            return;
        }
        CodingSheet codingSheet = column.getDefaultCodingSheet();
        logger.info("col {}", column);
        logger.info("sheet {}", codingSheet);
        if (codingSheet != null) {
            logger.info("any rules {}", CollectionUtils.isEmpty(codingSheet.getCodingRules()));
        }
        if ((codingSheet == null) || CollectionUtils.isEmpty(codingSheet.getCodingRules())) {
            return;
        }
        logger.info("select distinct values");
        List<String> values = tdarDataImportDatabase.selectDistinctValues(column);
        logger.info("values: {} ", values);
        logger.info("iterating over coding rules");
        for (CodingRule rule : codingSheet.getCodingRules()) {
            logger.info("term: {} ", rule.getTerm());
            if (values.contains(rule.getTerm())) {
                rule.setMappedToData(column);
                logger.trace("\t{}", rule);
            }
        }
    }

    /**
     * Returns the appropriate OntologyNode mapped to the given String value for the given IntegrationColumn.
     * 
     * If the OntologyNode exists in the
     * 
     * @param value
     * @param column
     * @param integrationColumn
     * @return
     */
    public OntologyNode getMappedOntologyNode(String value, DataTableColumn column, IntegrationColumn integrationColumn) {
        // FIXME: this lookup should be moved out of this method, otherwise we are creating / recreating these data structures over and over
        // and over and over
        CodingSheet codingSheet = column.getDefaultCodingSheet();
        Set<OntologyNode> filteredOntologyNodes = new HashSet<OntologyNode>(integrationColumn.getFilteredOntologyNodes());
        Map<OntologyNode, OntologyNode> closestParentMap = integrationColumn.getNearestParentMap();
        Map<String, OntologyNode> termToNodeMap = codingSheet.getTermToOntologyNodeMap();
        OntologyNode matchingNode = termToNodeMap.get(value);
        if (filteredOntologyNodes.contains(matchingNode)) {
            return matchingNode;
        }
        else {
            return closestParentMap.get(matchingNode);
        }
    }

    /**
     * Based on the set of @link IntegrationColumns generate the results of the data integration managed by the @link IntegrationDataResult
     * 
     * @param integrationColumns
     * @param tables
     * @return
     */
    public Pair<List<IntegrationDataResult>, Map<List<OntologyNode>, Map<DataTable, Integer>>> generateIntegrationData(
            List<IntegrationColumn> integrationColumns, List<DataTable> tables) {

        // for each column, rehydrate the column and selected ontology nodes
        for (IntegrationColumn integrationColumn : integrationColumns) {
            integrationColumn.setColumns(genericDao.loadFromSparseEntities(integrationColumn.getColumns(), DataTableColumn.class));
            logger.trace("before: {} - {}", integrationColumn, integrationColumn.getFilteredOntologyNodes());
            integrationColumn.setFilteredOntologyNodes(genericDao.loadFromSparseEntities(integrationColumn.getFilteredOntologyNodes(), OntologyNode.class));
            // for each of the integration columns, grab the unique set of all children within an ontology

            // that is, even if child is not selected, should get all children for query and pull up
            integrationColumn.setOntologyNodesForSelect(ontologyNodeDao.getAllChildren(integrationColumn.getFilteredOntologyNodes()));

            logger.trace("after: {} - {}", integrationColumn, integrationColumn.getFilteredOntologyNodes());
            logger.info("integration column: {}", integrationColumn);
        }

        // generate projections from each column, first aggregate across common display columns and integration columns
        List<IntegrationDataResult> results = new ArrayList<IntegrationDataResult>();
        // keeps track of all the columns that we need to select out from this data table
        // now iterate through all tables and generate the column lists.
        // use the OntologyNodes on the integration columns. each DataTable should have one integration column in it...
        Map<List<OntologyNode>, Map<DataTable, Integer>> pivot = new LinkedHashMap<List<OntologyNode>, Map<DataTable, Integer>>();

        for (DataTable table : tables) {
            // generate results per table
            IntegrationDataResult integrationDataResult = generateIntegrationResult(table, integrationColumns, pivot);
            results.add(integrationDataResult);
        }
        // Collections.sort(pivot);
        return Pair.create(results, pivot);
    }

    /**
     * Writes the results of the Data Integration to an Excel file and stores it in the @link PersonalFilestore for later distribution
     * 
     * @param integrationColumns
     * @param generatedIntegrationData
     * @param person
     * @return
     */
    public PersonalFilestoreTicket toExcel(TextProvider provider, List<IntegrationColumn> integrationColumns,
            Pair<List<IntegrationDataResult>, Map<List<OntologyNode>, Map<DataTable, Integer>>> generatedIntegrationData,
            Person person) {
        List<IntegrationDataResult> integrationDataResults = generatedIntegrationData.getFirst();
        if (CollectionUtils.isEmpty(integrationDataResults)) {
            return null;
        }

        DataIntegrationWorkbook integrationProxy = new DataIntegrationWorkbook(provider, excelService, person, generatedIntegrationData);
        integrationProxy.setIntegrationColumns(integrationColumns);
        integrationProxy.setIntegrationDataResults(integrationDataResults);
        integrationProxy.generate();
        PersonalFilestoreTicket ticket = integrationProxy.getTicket();
        genericDao.save(ticket);

        try {
            File resultFile = integrationProxy.writeToTempFile();
            PersonalFilestore filestore = filestoreService.getPersonalFilestore(person);
            filestore.store(ticket, resultFile, integrationProxy.getFileName());
        } catch (Exception iox) {
            logger.error("an error occurred when producing the integration excel file: {}", iox);
            throw new TdarRecoverableRuntimeException("dataIntegrationService.could_not_save_file");
        }

        return ticket;
    }

    /**
     * Convert the integration context to XML for persistance in the @link PersonalFilestore and logging
     * 
     * @param integrationColumns
     * @param creator
     * @return
     * @throws Exception
     */
    public String serializeIntegrationContext(List<IntegrationColumn> integrationColumns, Person creator) throws Exception {
        StringWriter sw = new StringWriter();
        xmlService.convertToXML(new IntegrationContext(creator, integrationColumns), sw);
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
    public CodingSheet createGeneratedCodingSheet(TextProvider provider, DataTableColumn column, Person submitter) {
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
    @SuppressWarnings(value = "NP_NULL_ON_SOME_PATH", justification = "null check earlier in the method")
    public CodingSheet createGeneratedCodingSheet(TextProvider provider, DataTableColumn column, Person submitter, Ontology ontology) {
        if (column == null) {
            logger.debug("{} tried to create an identity coding sheet for {} with no values", submitter, column);
        }
        CodingSheet codingSheet = new CodingSheet();
        codingSheet.setGenerated(true);
        codingSheet.setTitle(provider.getText("dataIntegrationService.generated_coding_sheet_title", Arrays.asList(column.getDisplayName())));
        codingSheet.markUpdated(submitter);
        codingSheet.setDate(Calendar.getInstance().get(Calendar.YEAR));
        codingSheet.setDefaultOntology(ontology);
        codingSheet.setCategoryVariable(ontology.getCategoryVariable());

        codingSheet.setDescription(provider.getText(
                "dataIntegrationService.generated_coding_sheet_description",
                Arrays.asList(TdarConfiguration.getInstance().getSiteAcronym(), column, column.getDataTable().getDataset().getTitle(), column.getDataTable()
                        .getDataset()
                        .getId(), codingSheet.getDateCreated())));
        genericDao.save(codingSheet);
        // generate identity coding rules
        List<String> dataColumnValues = tdarDataImportDatabase.selectNonNullDistinctValues(column);
        Set<CodingRule> rules = new HashSet<CodingRule>();
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

            dataTableColumns = table.getDataTableColumns();

            for (DataTableColumn column : dataTableColumns) {
                if (column.getDefaultOntology() != null) {
                    if (!dataTableAutoMap.containsKey(column.getDefaultOntology())) {
                        dataTableAutoMap.put(column.getDefaultOntology(), new ArrayList<DataTableColumn>());
                    }
                    dataTableAutoMap.get(column.getDefaultOntology()).add(column);
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
            return Collections.EMPTY_MAP;
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
            for (Entry<Ontology, List<DataTable>> entrySet : allOntologies.entrySet()) {
                if (entrySet.getValue().size() == bookmarkedDataTables.size()) {
                    toReturn.put(entrySet.getKey(), entrySet.getValue());
                }
            }
            return toReturn;
        }

        return allOntologies;
    }
}
